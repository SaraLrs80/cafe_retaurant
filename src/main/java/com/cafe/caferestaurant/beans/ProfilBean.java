package com.cafe.caferestaurant.beans;

import com.cafe.caferestaurant.dao.UtilisateurDAO;
import com.cafe.caferestaurant.entities.Utilisateur;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;

import java.io.Serializable;

/**
 * Bean JSF — Sauvegarde du profil client.
 * Chemin : src/main/java/com/cafe/caferestaurant/beans/ProfilBean.java
 */
@Named("profilBean")
@RequestScoped
public class ProfilBean implements Serializable {

    private final UtilisateurDAO dao = new UtilisateurDAO();

    /**
     * Sauvegarde les modifications du profil (nom, prénom, téléphone).
     */
    public String sauvegarder() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) ctx.getExternalContext().getSession(false);
        Utilisateur user = session != null ? (Utilisateur) session.getAttribute("currentUser") : null;

        if (user == null) {
            return "/pages/auth/login?faces-redirect=true";
        }

        try {
            dao.update(user);
            // Mettre à jour la session avec les nouvelles infos
            session.setAttribute("currentUser", user);
            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_INFO,
                    "Profil mis à jour avec succès.", null));
        } catch (Exception e) {
            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Erreur lors de la mise à jour.", null));
        }
        return null;
    }
}
