package com.cafe.caferestaurant.beans;

import com.cafe.caferestaurant.dao.CommandeDAO;
import com.cafe.caferestaurant.entities.Commande;
import com.cafe.caferestaurant.entities.Utilisateur;
import com.cafe.caferestaurant.enums.StatutCommande;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Named("clientCommandeBean")
@SessionScoped
public class ClientCommandeBean implements Serializable {

    private final CommandeDAO dao = new CommandeDAO();

    private List<Commande> mesCommandes;
    private Commande selectedCommande;
    private boolean showDetails = false;
    private String filtreStatut = "";

    @PostConstruct
    public void init() {
        charger();
    }

    private void charger() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) ctx.getExternalContext().getSession(false);
        Utilisateur user = session != null ? (Utilisateur) session.getAttribute("currentUser") : null;

        if (user != null) {
            Long userId = user.getIdUser();
            if (filtreStatut == null || filtreStatut.isEmpty()) {
                mesCommandes = dao.findByClient(userId);
            } else {
                mesCommandes = dao.findByClientAndStatut(userId, StatutCommande.valueOf(filtreStatut));
            }
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

    public String annulerCommande(Commande c) {
        c.setStatut(StatutCommande.ANNULEE);
        dao.update(c);
        charger();
        return null;
    }

    public String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return dateTime.format(formatter);
    }

    public List<Commande> getMesCommandes() {
        return mesCommandes;
    }

    public Commande getSelectedCommande() {
        return selectedCommande;
    }

    public boolean isShowDetails() {
        return showDetails;
    }

    public String getFiltreStatut() {
        return filtreStatut;
    }

    public void setFiltreStatut(String filtreStatut) {
        this.filtreStatut = filtreStatut;
    }
}
