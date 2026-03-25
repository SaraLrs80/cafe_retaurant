package com.cafe.caferestaurant.filters;

import com.cafe.caferestaurant.entities.Utilisateur;
import com.cafe.caferestaurant.enums.Role;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Filtre de sécurité — contrôle l'accès aux pages selon le rôle.
 *
 * Règles :
 *   /pages/admin/*   → ADMIN uniquement
 *   /pages/serveur/* → SERVEUR uniquement
 *   /pages/client/*  → CLIENT uniquement (ADMIN peut aussi accéder pour tests)
 *   /pages/auth/*    → accessible à tous (login, inscription)
 *   /pages/public/*  → accessible à tous (landing, access-denied)
 *   /auth/google     → OAuth Google, accessible à tous
 *
 * Chemin : src/main/java/com/cafe/caferestaurant/filters/AuthFilter.java
 */
@WebFilter(urlPatterns = {"/*"})
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  request  = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        HttpSession         session  = request.getSession(false);

        String contextPath = request.getContextPath();
        String requestURI  = request.getRequestURI();

        // ── 1. Ressources statiques & pages publiques ──────────────────────
        if (isPublicResource(requestURI)) {
            chain.doFilter(req, res);
            return;
        }

        // ── 2. Récupérer l'utilisateur connecté ───────────────────────────
        Utilisateur currentUser = session != null
                ? (Utilisateur) session.getAttribute("currentUser") : null;

        // ── 3. Non authentifié → rediriger vers login ──────────────────────
        if (currentUser == null) {
            response.sendRedirect(contextPath + "/pages/auth/login.xhtml");
            return;
        }

        Role role = currentUser.getRole();

        // ── 4. Contrôle d'accès par préfixe d'URL ─────────────────────────

        // Pages ADMIN : réservées à ADMIN uniquement
        if (requestURI.contains("/pages/admin/") && role != Role.ADMIN) {
            response.sendRedirect(contextPath + "/pages/public/access-denied.xhtml");
            return;
        }

        // Pages SERVEUR : réservées à SERVEUR uniquement
        if (requestURI.contains("/pages/serveur/") && role != Role.SERVEUR) {
            response.sendRedirect(contextPath + "/pages/public/access-denied.xhtml");
            return;
        }

        // Pages CLIENT : réservées à CLIENT uniquement
        // (retirez "&& role != Role.ADMIN" si vous ne voulez pas que l'admin y accède)
        if (requestURI.contains("/pages/client/")
                && role != Role.CLIENT
                && role != Role.ADMIN) {
            response.sendRedirect(contextPath + "/pages/public/access-denied.xhtml");
            return;
        }

        // ── 5. Accès autorisé ─────────────────────────────────────────────
        chain.doFilter(req, res);
    }

    // ── Ressources accessibles sans authentification ──────────────────────
    private boolean isPublicResource(String uri) {
        return uri.endsWith("/")
                || uri.endsWith("/index.xhtml")
                || uri.contains("/pages/public/")
                || uri.contains("/pages/auth/")
                || uri.contains("/auth/google")
                // ✅ JSF resources (images, CSS, JS via h:graphicImage, h:outputStylesheet...)
                || uri.contains("javax.faces.resource")   // couvre /javax.faces.resource/...
                || uri.contains("jakarta.faces.resource")  // ← AJOUTE ÇA pour Jakarta EE
                || uri.contains("/resources/css/")
                || uri.contains("/resources/images/")
                || uri.contains("/resources/js/")
                || uri.endsWith(".css")
                || uri.endsWith(".js")
                || uri.endsWith(".png")
                || uri.endsWith(".jpg")
                || uri.endsWith(".jpeg")
                || uri.endsWith(".ico")
                || uri.endsWith(".svg");
    }
}