package com.cafe.caferestaurant.servlets;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.oauth2.model.Userinfo;

import com.cafe.caferestaurant.entities.Utilisateur;
import com.cafe.caferestaurant.enums.Role;
import com.cafe.caferestaurant.services.AuthService;
import com.cafe.caferestaurant.utils.PasswordUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpRequestFactory;
/**
 * Servlet Google OAuth 2.0
 *
 * Flux :
 *  1. GET /auth/google          → redirige vers Google pour autorisation
 *  2. GET /auth/google/callback → reçoit le code, échange contre un token,
 *                                 récupère le profil, crée/charge l'utilisateur,
 *                                 place la session, redirige selon le rôle.
 *
 * Configuration requise dans web.xml ou via @WebServlet :
 *   CLIENT_ID     = votre Google Client ID
 *   CLIENT_SECRET = votre Google Client Secret
 *   REDIRECT_URI  = https://votre-domaine/auth/google/callback
 *
 * Dépendances Maven à ajouter dans pom.xml :
 *   <dependency>
 *     <groupId>com.google.oauth-client</groupId>
 *     <artifactId>google-oauth-client</artifactId>
 *     <version>1.34.1</version>
 *   </dependency>
 *   <dependency>
 *     <groupId>com.google.apis</groupId>
 *     <artifactId>google-api-services-oauth2</artifactId>
 *     <version>v2-rev157-1.25.0</version>
 *   </dependency>
 *   <dependency>
 *     <groupId>com.google.http-client</groupId>
 *     <artifactId>google-http-client-gson</artifactId>
 *     <version>1.43.3</version>
 *   </dependency>
 */
@WebServlet(urlPatterns = {"/auth/google", "/auth/google/callback"})
public class GoogleAuthServlet extends HttpServlet {

    // ─── OAuth config (à externaliser dans web.xml / .env) ──────────────────
    private static final String CLIENT_ID     = "VOTRE_GOOGLE_CLIENT_ID";
    private static final String CLIENT_SECRET = "VOTRE_GOOGLE_CLIENT_SECRET";
    private static final String REDIRECT_URI  = "http://localhost:8080/cafe-manager/auth/google/callback";
    private static final String APP_NAME      = "Café Manager";

    private static final List<String> SCOPES = Arrays.asList(
            "https://www.googleapis.com/auth/userinfo.email",
            "https://www.googleapis.com/auth/userinfo.profile"
    );

    private final AuthService authService = new AuthService();

    // =========================================================================
    //  ÉTAPE 1 : Rediriger vers Google
    // =========================================================================

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String pathInfo = req.getServletPath();

        if ("/auth/google".equals(pathInfo)) {
            // Générer un state anti-CSRF
            String state = UUID.randomUUID().toString();
            req.getSession().setAttribute("oauth_state", state);

            // Conserver le mode (login ou register)
            String mode = req.getParameter("mode");
            if (mode != null) req.getSession().setAttribute("oauth_mode", mode);

            // Construire l'URL d'autorisation Google
            String authUrl = new GoogleAuthorizationCodeRequestUrl(
                    CLIENT_ID, REDIRECT_URI, SCOPES)
                    .setAccessType("online")
                    .setState(state)
                    .build();

            resp.sendRedirect(authUrl);

        } else if ("/auth/google/callback".equals(pathInfo)) {
            handleCallback(req, resp);
        }
    }

    // =========================================================================
    //  ÉTAPE 2 : Traiter le callback Google
    // =========================================================================

    private void handleCallback(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        HttpSession session = req.getSession(false);
        String contextPath  = req.getContextPath();

        // ── Vérification state anti-CSRF ─────────────────────────────────────
        String returnedState = req.getParameter("state");
        String savedState    = session != null
                ? (String) session.getAttribute("oauth_state") : null;

        if (savedState == null || !savedState.equals(returnedState)) {
            resp.sendRedirect(contextPath + "/login.xhtml?error=csrf");
            return;
        }

        // ── Vérifier s'il n'y a pas eu d'erreur côté Google ──────────────────
        String error = req.getParameter("error");
        if (error != null) {
            resp.sendRedirect(contextPath + "/login.xhtml?error=google_denied");
            return;
        }

        // ── Échanger le code d'autorisation contre un token ──────────────────
        String code = req.getParameter("code");

        try {
            GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance(),
                    CLIENT_ID,
                    CLIENT_SECRET,
                    code,
                    REDIRECT_URI
            ).execute();

            // ── Récupérer les infos du profil Google ─────────────────────────
            GoogleCredentials credentials = GoogleCredentials
                    .create(new AccessToken(tokenResponse.getAccessToken(), null));

            HttpRequestFactory requestFactory = new NetHttpTransport()
                    .createRequestFactory(new HttpCredentialsAdapter(credentials));

            GenericUrl url = new GenericUrl(
                    "https://www.googleapis.com/oauth2/v2/userinfo?access_token="
                            + tokenResponse.getAccessToken());

            HttpResponse response = requestFactory.buildGetRequest(url).execute();
            Userinfo googleUser = response.parseAs(Userinfo.class);


            // ── Créer ou charger l'utilisateur en base ───────────────────────
            Utilisateur user = authService.findByEmail(googleUser.getEmail());

            if (user == null) {
                // Nouvelle inscription via Google → rôle CLIENT par défaut
                user = new Utilisateur();
                user.setEmail(googleUser.getEmail());
                user.setNom(googleUser.getFamilyName() != null
                        ? googleUser.getFamilyName() : "");
                user.setPrenom(googleUser.getGivenName() != null
                        ? googleUser.getGivenName() : "");
                user.setMotDePasse(PasswordUtil.hash(UUID.randomUUID().toString())); // mdp aléatoire
                user.setRole(Role.CLIENT);
                user.setStatutCompte(true);
                user.setGoogleId(googleUser.getId());
                authService.register(user);
            } else if (user.getGoogleId() == null) {
                // Compte existant — lier l'identifiant Google
                user.setGoogleId(googleUser.getId());
                authService.update(user);
            }

            // ── Vérifier si le compte est actif ──────────────────────────────
            if (!user.isStatutCompte()) {
                resp.sendRedirect(contextPath + "/login.xhtml?error=disabled");
                return;
            }

            // ── Stocker en session ────────────────────────────────────────────
            if (session == null) session = req.getSession(true);
            session.removeAttribute("oauth_state");
            session.removeAttribute("oauth_mode");
            session.setAttribute("currentUser", user);
            session.setAttribute("userRole",    user.getRole().name());

            // ── Rediriger selon le rôle ──────────────────────────────────────
            String target = switch (user.getRole()) {
                case ADMIN   -> contextPath + "/admin/dashboard.xhtml";
                case SERVEUR -> contextPath + "/serveur/commandes.xhtml";
                case CLIENT  -> contextPath + "/client/menu.xhtml";
            };
            resp.sendRedirect(target);

        } catch (Exception e) {
            getServletContext().log("Erreur OAuth Google : " + e.getMessage(), e);
            resp.sendRedirect(contextPath + "/login.xhtml?error=oauth_failed");
        }
    }
}