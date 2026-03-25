package com.cafe.caferestaurant.beans;

import com.cafe.caferestaurant.dao.TableDAO;
import com.cafe.caferestaurant.enums.StatutTable;
import com.cafe.caferestaurant.entities.TableRestaurant;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;

@Named("tableBean")
@SessionScoped
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
        selectedTable.setStatut(StatutTable.DISPONIBLE);
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

    public String supprimer(Long id) {
        dao.delete(id);
        charger();
        return null;
    }

    public String annuler() {
        showForm = false;
        return null;
    }

    // ── Compteurs pour les stats ──────────────────────────────────────────────
    public long getNbDisponibles() {
        return tables.stream()
            .filter(t -> t.getStatut() == StatutTable.DISPONIBLE).count();
    }

    public long getNbOccupees() {
        return tables.stream()
            .filter(t -> t.getStatut() == StatutTable.OCCUPEE).count();
    }

    public long getNbReservees() {
        return tables.stream()
            .filter(t -> t.getStatut() == StatutTable.RESERVEE).count();
    }

    public int getTotalTables() {
        return tables.size();
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────
    public List<TableRestaurant> getTables()                   { return tables; }
    public TableRestaurant       getSelectedTable()            { return selectedTable; }
    public void                  setSelectedTable(TableRestaurant t) { this.selectedTable = t; }
    public boolean               isShowForm()                  { return showForm; }
    public boolean               isModeEdition()               { return modeEdition; }
}
