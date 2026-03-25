package com.cafe.caferestaurant.services;

import com.cafe.caferestaurant.dao.UtilisateurDAO;
import com.cafe.caferestaurant.entities.Utilisateur;
import com.cafe.caferestaurant.utils.PasswordUtil;

/**
 * Service d'authentification.
 * Fait le lien entre les Beans JSF / Servlets et la couche DAO Hibernate.
 */
public class AuthService {

    private final UtilisateurDAO dao = new UtilisateurDAO();

    /**
     * Vérifier les identifiants et retourner l'utilisateur ou null.
     */
    public Utilisateur authenticate(String email, String rawPassword) {
        Utilisateur user = dao.findByEmail(email.toLowerCase());
        if (user == null) return null;
        // Vérifier le hash BCrypt
        if (!PasswordUtil.verify(rawPassword, user.getMotDePasse())) return null;
        return user;
    }

    /**
     * Vérifier si un email est déjà enregistré.
     */
    public boolean emailExists(String email) {
        return dao.findByEmail(email.toLowerCase()) != null;
    }

    /**
     * Enregistrer un nouvel utilisateur.
     */
    public boolean register(Utilisateur user) {
        try {
            dao.save(user);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Trouver un utilisateur par email.
     */
    public Utilisateur findByEmail(String email) {
        return dao.findByEmail(email.toLowerCase());
    }

    /**
     * Mettre à jour un utilisateur (ex : lier un googleId).
     */
    public void update(Utilisateur user) {
        dao.update(user);
    }
}