package com.cafe.caferestaurant.beans;

import com.cafe.caferestaurant.dao.CommandeDAO;
import com.cafe.caferestaurant.dao.MenuDAO;
import com.cafe.caferestaurant.dao.TableDAO;
import com.cafe.caferestaurant.entities.*;
import com.cafe.caferestaurant.enums.StatutCommande;
import com.cafe.caferestaurant.enums.StatutTable;
import com.cafe.caferestaurant.enums.TypeCommande;
import com.cafe.caferestaurant.utils.HibernateUtil;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Named("serveurCommandeBean")
@SessionScoped
public class ServeurCommandeBean implements Serializable {

    private final CommandeDAO commandeDAO = new CommandeDAO();
    private final MenuDAO     menuDAO     = new MenuDAO();
    private final TableDAO    tableDAO    = new TableDAO();
    @Inject
    private AuthBean authBean;
    // Listes
    private List<Commande>        commandes;
    private List<Menu>            menuDisponible;
    private List<TableRestaurant> tablesDisponibles;

    // Nouvelle commande en cours de création
    private Commande              nouvelleCommande;
    private List<LigneCommande>   lignesTemp = new ArrayList<>();
    private Long                  selectedMenuId;
    private int                   quantiteTemp = 1;
    private Long                  selectedTableId;
    private String                typeCommande = "SUR_PLACE";

    // UI state
    private boolean showFormCommande = false;
    private boolean showDetails      = false;
    private Commande selectedCommande;

    // Filtre
    private String filtreStatut = "";

    @PostConstruct
    public void init() {
        charger();
        menuDisponible    = menuDAO.findAll().stream()
                .filter(Menu::isDisponibilite).toList();
        tablesDisponibles = tableDAO.findAll();
    }

    private void charger() {
        if (filtreStatut != null && !filtreStatut.isEmpty()) {
            commandes = commandeDAO.findByStatut(StatutCommande.valueOf(filtreStatut));
        } else {
            commandes = commandeDAO.findAll();
        }
    }

    public String filtrer() {
        charger();
        return null;
    }

    // ── Nouvelle commande ────────────────────────────────────────────────────

    public String ouvrirFormCommande() {
        nouvelleCommande  = new Commande();
        lignesTemp        = new ArrayList<>();
        selectedMenuId    = null;
        quantiteTemp      = 1;
        selectedTableId   = null;
        typeCommande      = "SUR_PLACE";
        tablesDisponibles = tableDAO.findDisponibles();
        menuDisponible    = menuDAO.findAll().stream()
                .filter(Menu::isDisponibilite).toList();
        showFormCommande  = true;
        return null;
    }

    public String fermerForm() {
        showFormCommande = false;
        lignesTemp       = new ArrayList<>();
        return null;
    }

    // Ajouter un article à la commande temporaire
    public String ajouterArticle() {
        if (selectedMenuId == null || quantiteTemp <= 0) return null;

        Menu menu = menuDAO.findById(selectedMenuId);
        if (menu == null) return null;

        // Vérifier si le plat est déjà dans la liste
        for (LigneCommande lc : lignesTemp) {
            if (lc.getMenu().getIdMenu().equals(selectedMenuId)) {
                lc.setQuantite(lc.getQuantite() + quantiteTemp);
                lc.setPrixUnitaire(menu.getPrix());
                selectedMenuId = null;
                quantiteTemp   = 1;
                return null;
            }
        }

        // Nouveau plat
        LigneCommande ligne = new LigneCommande();
        ligne.setMenu(menu);
        ligne.setQuantite(quantiteTemp);
        ligne.setPrixUnitaire(menu.getPrix());
        lignesTemp.add(ligne);

        selectedMenuId = null;
        quantiteTemp   = 1;
        return null;
    }

    public String supprimerArticle(int index) {
        if (index >= 0 && index < lignesTemp.size()) {
            lignesTemp.remove(index);
        }
        return null;
    }

    // Calculer le total temporaire
    public BigDecimal getTotalTemp() {
        return lignesTemp.stream()
                .map(l -> l.getPrixUnitaire().multiply(BigDecimal.valueOf(l.getQuantite())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Enregistrer la commande
    public String enregistrerCommande() {
        if (lignesTemp.isEmpty()) return null;

        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();

            // Table
            TableRestaurant table = null;
            if ("SUR_PLACE".equals(typeCommande) && selectedTableId != null) {
                table = em.find(TableRestaurant.class, selectedTableId);
                if (table != null) {
                    table.setStatut(StatutTable.OCCUPEE);
                    em.merge(table);
                }
            }

            // INSERT commande via SQL natif
            jakarta.persistence.Query q = em.createNativeQuery(
                    "INSERT INTO commande " +
                            "(date_commande, type_commande, statut, montant_total, id_table, id_client, id_serveur) " +
                            "VALUES (:dateCommande, :typeCommande, :statut, :montantTotal, :idTable, :idClient, :idServeur)");
            q.setParameter("dateCommande", LocalDateTime.now());
            q.setParameter("typeCommande", typeCommande);
            q.setParameter("statut",       StatutCommande.EN_ATTENTE.name());
            q.setParameter("montantTotal", getTotalTemp());
            q.setParameter("idTable",      table != null ? table.getIdTable() : (Long) null);
            q.setParameter("idClient",     (Long) null);
            Long idServeur = authBean.getCurrentUser() != null
                    ? authBean.getCurrentUser().getIdUser()
                    : null;
            q.setParameter("idServeur", idServeur);
            q.executeUpdate();

            // Récupérer l'id généré
            Long idCommande = ((Number) em.createNativeQuery(
                            "SELECT id_commande FROM commande ORDER BY id_commande DESC LIMIT 1")
                    .getSingleResult()).longValue();

            // INSERT lignes
            for (LigneCommande lc : lignesTemp) {
                jakarta.persistence.Query ql = em.createNativeQuery(
                        "INSERT INTO ligne_commande (quantite, prix_unitaire, id_commande, id_menu) " +
                                "VALUES (:quantite, :prixUnitaire, :idCommande, :idMenu)");
                ql.setParameter("quantite",     lc.getQuantite());
                ql.setParameter("prixUnitaire", lc.getPrixUnitaire());
                ql.setParameter("idCommande",   idCommande);
                ql.setParameter("idMenu",       lc.getMenu().getIdMenu());
                ql.executeUpdate();
            }

            em.getTransaction().commit();
            showFormCommande = false;
            lignesTemp       = new ArrayList<>();
            charger();
        } catch (Exception e) {
            em.getTransaction().rollback();
            e.printStackTrace();
        } finally { em.close(); }
        return null;
    }

    // ── Actions sur commande existante ───────────────────────────────────────

    public String voirDetails(Commande c) {
        selectedCommande = commandeDAO.findByIdWithLignes(c.getIdCommande());
        showDetails      = true;
        return null;
    }

    public String fermerDetails() {
        showDetails = false;
        return null;
    }

    public String avancerStatut(Commande c) {
        StatutCommande suivant = switch (c.getStatut()) {
            case EN_ATTENTE -> StatutCommande.EN_COURS;
            case EN_COURS   -> StatutCommande.SERVIE;
            case SERVIE     -> StatutCommande.PAYEE;
            default         -> c.getStatut();
        };
        c.setStatut(suivant);
        commandeDAO.update(c);
        charger();
        return null;
    }

    public String annulerCommande(Commande c) {
        c.setStatut(StatutCommande.ANNULEE);
        commandeDAO.update(c);
        charger();
        return null;
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────
    public List<Commande>        getCommandes()            { return commandes; }
    public List<Menu>            getMenuDisponible()       { return menuDisponible; }
    public List<TableRestaurant> getTablesDisponibles()    { return tablesDisponibles; }
    public List<LigneCommande>   getLignesTemp()           { return lignesTemp; }
    public Commande              getNouvelleCommande()     { return nouvelleCommande; }
    public Commande              getSelectedCommande()     { return selectedCommande; }
    public boolean               isShowFormCommande()      { return showFormCommande; }
    public boolean               isShowDetails()           { return showDetails; }

    public Long    getSelectedMenuId()               { return selectedMenuId; }
    public void    setSelectedMenuId(Long v)         { this.selectedMenuId = v; }
    public int     getQuantiteTemp()                 { return quantiteTemp; }
    public void    setQuantiteTemp(int v)            { this.quantiteTemp = v; }
    public Long    getSelectedTableId()              { return selectedTableId; }
    public void    setSelectedTableId(Long v)        { this.selectedTableId = v; }
    public String  getTypeCommande()                 { return typeCommande; }
    public void    setTypeCommande(String v)         { this.typeCommande = v; }
    public String  getFiltreStatut()                 { return filtreStatut; }
    public void    setFiltreStatut(String v)         { this.filtreStatut = v; }
}