package com.cafe.caferestaurant.beans;

import com.cafe.caferestaurant.dao.UtilisateurDAO;
import com.cafe.caferestaurant.enums.Role;
import com.cafe.caferestaurant.entities.Utilisateur;
import com.cafe.caferestaurant.utils.PasswordUtil;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;

@Named("utilisateurBean")
@SessionScoped
public class UtilisateurBean implements Serializable {

    private final UtilisateurDAO dao = new UtilisateurDAO();

    private List<Utilisateur> utilisateurs;
    private Utilisateur selectedUser;
    private boolean showForm = false;
    private boolean modeEdition = false;
    private String filtreRole = "";
    private String motDePasse;

    @PostConstruct
    public void init() {
        charger();
    }

    private void charger() {
        if (filtreRole == null || filtreRole.isEmpty()) {
            utilisateurs = dao.findAll();
        } else {
            utilisateurs = dao.findByRole(Role.valueOf(filtreRole));
        }
    }

    public String filtrer() {
        charger();
        return null;
    }

    public String nouveauUtilisateur() {
        selectedUser = new Utilisateur();
        selectedUser.setStatutCompte(true);
        selectedUser.setRole(Role.CLIENT);
        modeEdition = false;
        showForm = true;
        return null;
    }

    public String editer(Utilisateur u) {
        selectedUser = u;
        modeEdition = true;
        showForm = true;
        return null;
    }

    public String sauvegarder() {
        try {
            if (modeEdition) {
                dao.update(selectedUser);
            } else {
                selectedUser.setMotDePasse(PasswordUtil.hash(motDePasse));
                dao.save(selectedUser);
            }
            showForm = false;
            motDePasse = null;
            charger();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String toggleStatut(Utilisateur u) {
        u.setStatutCompte(!u.isStatutCompte());
        dao.update(u);
        charger();
        return null;
    }

    public String annuler() {
        showForm = false;
        motDePasse = null;
        return null;
    }

    // ── Getters / Setters ────────────────────────────────────────────────────
    public List<Utilisateur> getUtilisateurs()          { return utilisateurs; }
    public Utilisateur       getSelectedUser()           { return selectedUser; }
    public void              setSelectedUser(Utilisateur u) { this.selectedUser = u; }
    public boolean           isShowForm()                { return showForm; }
    public boolean           isModeEdition()             { return modeEdition; }
    public String            getFiltreRole()             { return filtreRole; }
    public void              setFiltreRole(String v)     { this.filtreRole = v; }
    public String            getMotDePasse()             { return motDePasse; }
    public void              setMotDePasse(String v)     { this.motDePasse = v; }
}
