package com.cafe.caferestaurant.beans;

import com.cafe.caferestaurant.dao.CommandeDAO;
import com.cafe.caferestaurant.dao.MenuDAO;
import com.cafe.caferestaurant.entities.Commande;
import com.cafe.caferestaurant.entities.LigneCommande;
import com.cafe.caferestaurant.entities.Menu;
import com.cafe.caferestaurant.entities.Utilisateur;
import com.cafe.caferestaurant.enums.StatutCommande;
import com.cafe.caferestaurant.enums.TypeCommande;
import com.cafe.caferestaurant.utils.HibernateUtil;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpSession;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Bean JSF — Menu CLIENT (consultation + commande en ligne).
 * Chemin : src/main/java/com/cafe/caferestaurant/beans/ClientMenuBean.java
 */
@Named("clientMenuBean")
@SessionScoped
public class ClientMenuBean implements Serializable {

    private final MenuDAO     menuDAO     = new MenuDAO();
    private final CommandeDAO commandeDAO = new CommandeDAO();

    private List<Menu>       plats           = new ArrayList<>();
    private String           filtreCategorie = "";
    private List<PanierItem> panier          = new ArrayList<>();
    private boolean          showPanier      = false;

    @PostConstruct
    public void init() {
        charger();
    }

    private void charger() {
        try {
            if (filtreCategorie == null || filtreCategorie.isEmpty()) {
                plats = menuDAO.findDisponibles();
            } else {
                plats = menuDAO.findDisponiblesByCategorie(filtreCategorie);
            }
        } catch (Exception e) {
            plats = new ArrayList<>();
        }
    }

    public String filtrer(String categorie) {
        this.filtreCategorie = categorie;
        charger();
        return null;
    }

    // ── Panier ────────────────────────────────────────────────────────────

    public String ajouterAuPanier(Menu plat) {
        for (PanierItem item : panier) {
            if (item.getMenu().getIdMenu().equals(plat.getIdMenu())) {
                item.setQuantite(item.getQuantite() + 1);
                item.recalculer();
                return null;
            }
        }
        panier.add(new PanierItem(plat, 1));
        return null;
    }

    public String retirerDuPanier(Long idMenu) {
        panier.removeIf(i -> i.getMenu().getIdMenu().equals(idMenu));
        return null;
    }

    public String diminuerQuantite(Long idMenu) {
        for (PanierItem item : panier) {
            if (item.getMenu().getIdMenu().equals(idMenu)) {
                if (item.getQuantite() > 1) {
                    item.setQuantite(item.getQuantite() - 1);
                    item.recalculer();
                } else {
                    panier.remove(item);
                }
                return null;
            }
        }
        return null;
    }

    public String augmenterQuantite(Long idMenu) {
        for (PanierItem item : panier) {
            if (item.getMenu().getIdMenu().equals(idMenu)) {
                item.setQuantite(item.getQuantite() + 1);
                item.recalculer();
                return null;
            }
        }
        return null;
    }

    public String togglePanier() {
        showPanier = !showPanier;
        return null;
    }

    public String fermerPanier() {
        showPanier = false;
        return null;
    }

    public BigDecimal getTotalPanier() {
        return panier.stream()
                .map(PanierItem::getSousTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getNbArticlesPanier() {
        return panier.stream().mapToInt(PanierItem::getQuantite).sum();
    }

    // ── Valider la commande ───────────────────────────────────────────────

    public String validerCommande() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) ctx.getExternalContext().getSession(false);
        Utilisateur user = session != null
                ? (Utilisateur) session.getAttribute("currentUser") : null;

        if (user == null) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Vous devez être connecté pour commander.", null));
            return null;
        }

        if (panier.isEmpty()) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                    "Votre panier est vide.", null));
            return null;
        }

        // Utiliser UN SEUL EntityManager pour toute la transaction
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();

            // Recharger l'utilisateur dans ce contexte de persistance
            Utilisateur managedUser = em.find(Utilisateur.class, user.getIdUser());

            // Créer la commande
            Commande commande = new Commande();
            commande.setDateCommande(LocalDateTime.now());
            commande.setTypeCommande(TypeCommande.EN_LIGNE);
            commande.setStatut(StatutCommande.EN_ATTENTE);
            commande.setClient(managedUser);
            commande.setServeur(null);
            commande.setTable(null);

            commande.setMontantTotal(getTotalPanier());

            // Persister la commande d'abord
            em.persist(commande);
            em.flush(); // forcer l'INSERT pour obtenir l'idCommande

            // Créer les lignes de commande
            List<LigneCommande> lignes = new ArrayList<>();
            for (PanierItem item : panier) {
                // Recharger le Menu dans ce même EntityManager
                Menu managedMenu = em.find(Menu.class, item.getMenu().getIdMenu());

                LigneCommande ligne = new LigneCommande();
                ligne.setCommande(commande);
                ligne.setMenu(managedMenu);
                ligne.setQuantite(item.getQuantite());
                ligne.setPrixUnitaire(managedMenu.getPrix());
                em.persist(ligne);
                lignes.add(ligne);
            }

            commande.setLignes(lignes);
            em.merge(commande);

            em.getTransaction().commit();

            // Vider le panier et fermer le modal
            panier.clear();
            showPanier = false;

            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Commande passée avec succès !", null));

            return "/pages/client/commandes.xhtml?faces-redirect=true";

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Erreur : " + e.getMessage(), null));
            return null;
        } finally {
            em.close();
        }
    }

    // ── Getters / Setters ─────────────────────────────────────────────────
    public List<Menu>       getPlats()                   { return plats; }
    public String           getFiltreCategorie()         { return filtreCategorie; }
    public void             setFiltreCategorie(String v) { this.filtreCategorie = v; }
    public List<PanierItem> getPanier()                  { return panier; }
    public boolean          isShowPanier()               { return showPanier; }

    // ── Classe interne PanierItem ─────────────────────────────────────────

    public static class PanierItem implements Serializable {
        private Menu       menu;
        private int        quantite;
        private BigDecimal sousTotal;

        public PanierItem(Menu menu, int quantite) {
            this.menu     = menu;
            this.quantite = quantite;
            recalculer();
        }

        public void recalculer() {
            this.sousTotal = menu.getPrix().multiply(BigDecimal.valueOf(quantite));
        }

        public Menu       getMenu()             { return menu; }
        public int        getQuantite()         { return quantite; }
        public void       setQuantite(int v)    { this.quantite = v; recalculer(); }
        public BigDecimal getSousTotal()        { return sousTotal; }
    }
}