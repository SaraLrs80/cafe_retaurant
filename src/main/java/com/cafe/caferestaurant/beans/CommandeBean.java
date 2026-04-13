package com.cafe.caferestaurant.beans;

import com.cafe.caferestaurant.dao.CommandeDAO;
import com.cafe.caferestaurant.entities.Commande;
import com.cafe.caferestaurant.enums.StatutCommande;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;

@Named("commandeBean")
@SessionScoped
public class CommandeBean implements Serializable {

    private final CommandeDAO dao = new CommandeDAO();

    private List<Commande> commandes;
    private Commande       selectedCommande;
    private boolean        showDetails  = false;
    private String         filtreStatut = "";

    @PostConstruct
    public void init() {
        charger();
    }

    private void charger() {
        if (filtreStatut == null || filtreStatut.isEmpty()) {
            commandes = dao.findAll();
        } else {
            commandes = dao.findByStatut(StatutCommande.valueOf(filtreStatut));
        }
    }

    public String filtrer() {
        charger();
        return null;
    }

    public String voirDetails(Commande c) {
        selectedCommande = dao.findByIdWithLignes(c.getIdCommande());
        showDetails = true;
        return null;
    }

    public String fermerDetails() {
        showDetails = false;
        return null;
    }

    /**
     * CORRIGÉ : avance le statut EN_ATTENTE → EN_COURS → SERVIE → PAYEE.
     * CommandeDAO.update() gère déjà la libération de la table quand SERVIE/PAYEE/ANNULEE.
     * On recharge la liste après chaque changement.
     */
    public String avancerStatut(Commande c) {
        StatutCommande actuel = c.getStatut();
        StatutCommande suivant = switch (actuel) {
            case EN_ATTENTE -> StatutCommande.EN_COURS;
            case EN_COURS   -> StatutCommande.SERVIE;
            case SERVIE     -> StatutCommande.PAYEE;
            default         -> actuel; // PAYEE et ANNULEE ne bougent plus
        };

        if (suivant != actuel) {   // ← CORRIGÉ : n'appelle update() que si le statut change vraiment
            c.setStatut(suivant);
            dao.update(c);
            charger(); // recharge la liste depuis la base après la mise à jour
        }
        return null;
    }

    public String annulerCommande(Commande c) {
        c.setStatut(StatutCommande.ANNULEE);
        dao.update(c);  // CommandeDAO.update() libère la table si SUR_PLACE
        charger();
        return null;
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────
    public List<Commande> getCommandes()               { return commandes; }
    public Commande       getSelectedCommande()        { return selectedCommande; }
    public boolean        isShowDetails()              { return showDetails; }
    public String         getFiltreStatut()            { return filtreStatut; }
    public void           setFiltreStatut(String v)    { this.filtreStatut = v; }
}
