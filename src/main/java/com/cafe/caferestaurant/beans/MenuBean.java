package com.cafe.caferestaurant.beans;

import com.cafe.caferestaurant.dao.MenuDAO;
import com.cafe.caferestaurant.entities.Menu;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;

@Named("menuBean")
@SessionScoped
public class MenuBean implements Serializable {

    private final MenuDAO dao = new MenuDAO();

    private List<Menu> plats;
    private Menu       selectedPlat;
    private boolean    showForm       = false;
    private boolean    modeEdition    = false;
    private String     filtreCategorie = "";

    @PostConstruct
    public void init() {
        charger();
    }

    private void charger() {
        if (filtreCategorie == null || filtreCategorie.isEmpty()) {
            plats = dao.findAll();
        } else {
            plats = dao.findByCategorie(filtreCategorie);
        }
    }

    public String filtrer(String categorie) {
        this.filtreCategorie = categorie;
        try {
            if (categorie == null || categorie.isEmpty()) {
                plats = dao.findAll();
            } else {
                // Passe l'enum directement au DAO
                plats = dao.findByCategorie(categorie);
            }
        } catch (Exception e) {
            plats = dao.findAll();
        }
        return null;
    }

    public String nouveauPlat() {
        selectedPlat = new Menu();
        selectedPlat.setDisponibilite(true);
        modeEdition = false;
        showForm    = true;
        return null;
    }

    public String editer(Menu p) {
        selectedPlat = p;
        modeEdition  = true;
        showForm     = true;
        return null;
    }

    public String sauvegarder() {
        try {
            if (modeEdition) {
                dao.update(selectedPlat);
            } else {
                dao.save(selectedPlat);
            }
            showForm = false;
            charger();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String toggleDisponibilite(Menu p) {
        p.setDisponibilite(!p.isDisponibilite());
        dao.update(p);
        charger();
        return null;
    }

    public String supprimer(Long id) {
        dao.delete(id);
        charger();
        return null;
    }

    public String annuler() {
        showForm = false;
        return null;
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────
    public List<Menu> getPlats()                        { return plats; }
    public Menu       getSelectedPlat()                 { return selectedPlat; }
    public void       setSelectedPlat(Menu m)           { this.selectedPlat = m; }
    public boolean    isShowForm()                      { return showForm; }
    public boolean    isModeEdition()                   { return modeEdition; }
    public String     getFiltreCategorie()              { return filtreCategorie; }
    public void       setFiltreCategorie(String v)      { this.filtreCategorie = v; }
}
