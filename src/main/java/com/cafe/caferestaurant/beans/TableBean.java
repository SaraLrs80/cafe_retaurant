package com.cafe.caferestaurant.beans;

import com.cafe.caferestaurant.dao.TableDAO;
import com.cafe.caferestaurant.enums.StatutTable;
import com.cafe.caferestaurant.entities.TableRestaurant;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;   // ← CHANGEMENT CLÉ : était SessionScoped
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;

@Named("tableBean")
@ViewScoped  // ← Relit la base à chaque requête → statuts toujours à jour  (j'ai changer de request)
public class TableBean implements Serializable {

    private final TableDAO dao = new TableDAO();

    private List<TableRestaurant> tables;
    private TableRestaurant selectedTable;
    private boolean showForm    = false;
    private boolean modeEdition = false;

    @PostConstruct
    public void init() {
        charger();
    }

    private void charger() {
        tables = dao.findAll();
    }

    public String nouvelleTable() {
        selectedTable = new TableRestaurant();
        selectedTable.setNumeroTable(dao.getNextNumeroTable());
        selectedTable.setStatut(StatutTable.DISPONIBLE);
        selectedTable.setActive(true);
        modeEdition = false;
        showForm    = true;
        return null;
    }

    public String editer(TableRestaurant t) {
        selectedTable = t;
        modeEdition   = true;
        showForm      = true;
        return null;
    }

    public String sauvegarder() {
        try {
            if (modeEdition) {
                dao.update(selectedTable);
            } else {
                dao.save(selectedTable);
            }
            showForm = false;
            charger();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void supprimer(Long idTable) {
        try {
            dao.delete(idTable);
            charger();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Succès", "Table désactivée avec succès"));
        } catch (IllegalStateException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Suppression impossible",
                            "Cette table a des réservations actives. Annulez-les d'abord."));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Erreur", "Une erreur inattendue s'est produite."));
        }
    }

    public String annuler() {
        showForm = false;
        return null;
    }

    // ── Compteurs calculés depuis la liste fraîche ───────────────────────────
    public long getNbDisponibles() {
        return tables.stream().filter(t -> t.getStatut() == StatutTable.DISPONIBLE).count();
    }

    public long getNbOccupees() {
        return tables.stream().filter(t -> t.getStatut() == StatutTable.OCCUPEE).count();
    }

    public long getNbReservees() {
        return tables.stream().filter(t -> t.getStatut() == StatutTable.RESERVEE).count();
    }

    public int getTotalTables() {
        return tables.size();
    }

    // ── Getters / Setters ────────────────────────────────────────────────────
    public List<TableRestaurant> getTables()                        { return tables; }
    public TableRestaurant       getSelectedTable()                 { return selectedTable; }
    public void                  setSelectedTable(TableRestaurant t){ this.selectedTable = t; }
    public boolean               isShowForm()                       { return showForm; }
    public boolean               isModeEdition()                    { return modeEdition; }
}