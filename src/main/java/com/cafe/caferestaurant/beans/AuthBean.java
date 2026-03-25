package com.cafe.caferestaurant.beans;

import com.cafe.caferestaurant.entities.Utilisateur;
import com.cafe.caferestaurant.enums.Role;
import com.cafe.caferestaurant.services.AuthService;
import com.cafe.caferestaurant.utils.PasswordUtil;

import jakarta.faces.application.FacesMessage;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpSession;

import java.io.Serializable;
import java.util.Date;

/**
 * Managed Bean JSF — Authentification (connexion + inscription)
 * Gestion des rôles : ADMIN / SERVEUR / CLIENT
 */
@Named("authBean")
@SessionScoped
public class AuthBean implements Serializable {

    // ─── Fields bound to the JSF form ────────────────────────────────────────

    private String  email;
    private String  password;
    private String  nom;
    private String  prenom;
    private String  telephone;
    private Date    dateNaissance;

    // Utilisateur connecté (stocké en session)
    private Utilisateur currentUser;

    // ─── Service ──────────────────────────────────────────────────────────────

    private final AuthService authService = new AuthService();

    // =========================================================================
    //  CONNEXION
    // =========================================================================

    /**
     * Action JSF : vérifier les identifiants et rediriger selon le rôle.
     */
    public String login() {
        FacesContext ctx = FacesContext.getCurrentInstance();

        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR, "Veuillez remplir tous les champs.", null));
            return null;
        }

        Utilisateur user = authService.authenticate(email.trim(), password);

        if (user == null) {
            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Email ou mot de passe incorrect.", null));
            return null;
        }

        if (!user.isStatutCompte()) {
            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Votre compte est désactivé. Contactez l'administrateur.", null));
            return null;
        }

        // Stocker l'utilisateur en session
        this.currentUser = user;
        ExternalContext ext = ctx.getExternalContext();
        HttpSession session = (HttpSession) ext.getSession(true);
        session.setAttribute("currentUser", user);
        session.setAttribute("userRole",    user.getRole().name());

        // Redirection selon le rôle
        return switch (user.getRole()) {
            case ADMIN -> "/pages/admin/dashboard.xhtml?faces-redirect=true";
            case SERVEUR -> "/pages/serveur/dashboard.xhtml?faces-redirect=true";
            case CLIENT -> "/pages/client/dashboard.xhtml?faces-redirect=true";
        };

    }

    // =========================================================================
    //  INSCRIPTION  (CLIENT uniquement — les autres rôles sont créés par l'admin)
    // =========================================================================

    /**
     * Action JSF : créer un nouveau compte client.
     */
    public String register() {
        FacesContext ctx = FacesContext.getCurrentInstance();

        // Vérifier si l'email existe déjà
        if (authService.emailExists(email.trim())) {
            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Cette adresse email est déjà utilisée.", null));
            return null;
        }

        // Construire l'entité Utilisateur
        Utilisateur newUser = new Utilisateur();
        newUser.setNom(nom.trim());
        newUser.setPrenom(prenom.trim());
        newUser.setEmail(email.trim().toLowerCase());
        newUser.setMotDePasse(PasswordUtil.hash(password));   // BCrypt
        newUser.setTelephone(telephone);
        newUser.setDateNaissance(dateNaissance);
        newUser.setRole(Role.CLIENT);
        newUser.setStatutCompte(true);

        boolean saved = authService.register(newUser);

        if (!saved) {
            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Une erreur est survenue. Veuillez réessayer.", null));
            return null;
        }

        // Réinitialiser les champs
        clearFields();

        // Rediriger vers la connexion avec message de succès
        ctx.addMessage(null, new FacesMessage(
                FacesMessage.SEVERITY_INFO,
                "Compte créé avec succès ! Vous pouvez vous connecter.", null));

// Dans login() remplace le switch par
        return "/pages/auth/success.xhtml?faces-redirect=true";    }

    // =========================================================================
    //  DÉCONNEXION
    // =========================================================================

    public String logout() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        ExternalContext ext = ctx.getExternalContext();
        HttpSession session = (HttpSession) ext.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        currentUser = null;
        return "/pages/auth/login.xhtml?faces-redirect=true";
    }

    // =========================================================================
    //  HELPERS
    // =========================================================================

    private void clearFields() {
        email         = null;
        password      = null;
        nom           = null;
        prenom        = null;
        telephone     = null;
        dateNaissance = null;
    }

    // =========================================================================
    //  GETTERS / SETTERS
    // =========================================================================

    public String  getEmail()         { return email; }
    public void    setEmail(String v) { this.email = v; }

    public String  getPassword()         { return password; }
    public void    setPassword(String v) { this.password = v; }

    public String  getNom()         { return nom; }
    public void    setNom(String v) { this.nom = v; }

    public String  getPrenom()         { return prenom; }
    public void    setPrenom(String v) { this.prenom = v; }

    public String  getTelephone()         { return telephone; }
    public void    setTelephone(String v) { this.telephone = v; }

    public Date    getDateNaissance()         { return dateNaissance; }
    public void    setDateNaissance(Date v)   { this.dateNaissance = v; }

    public Utilisateur getCurrentUser()           { return currentUser; }
    public void        setCurrentUser(Utilisateur u) { this.currentUser = u; }
}