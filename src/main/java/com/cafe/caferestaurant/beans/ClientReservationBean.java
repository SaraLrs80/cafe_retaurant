package com.cafe.caferestaurant.beans;

import com.cafe.caferestaurant.dao.ReservationDAO;
import com.cafe.caferestaurant.dao.TableDAO;
import com.cafe.caferestaurant.entities.Reservation;
import com.cafe.caferestaurant.entities.TableRestaurant;
import com.cafe.caferestaurant.entities.Utilisateur;
import com.cafe.caferestaurant.enums.StatutReservation;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Named("clientReservationBean")
@SessionScoped
public class ClientReservationBean implements Serializable {

    private final ReservationDAO reservationDAO = new ReservationDAO();
    private final TableDAO tableDAO = new TableDAO();

    private List<Reservation> mesReservations = new ArrayList<>();
    private List<TableRestaurant> tablesDisponibles = new ArrayList<>();

    private Reservation selectedReservation;
    private boolean showForm = false;
    private boolean modeEdition = false;

    private String dateReservationStr;
    private String heureReservationStr;

    @PostConstruct
    public void init() {
        charger();
        chargerTablesDisponibles();
    }

    private void charger() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) ctx.getExternalContext().getSession(false);
        Utilisateur user = session != null ? (Utilisateur) session.getAttribute("currentUser") : null;

        if (user != null) {
            mesReservations = reservationDAO.findByClient(user.getIdUser());
        } else {
            mesReservations = new ArrayList<>();
        }
    }

    private void chargerTablesDisponibles() {
        List<TableRestaurant> tables = tableDAO.findDisponibles();
        tablesDisponibles = (tables != null) ? tables : new ArrayList<>();
    }

    public String nouvelleReservation() {
        selectedReservation = new Reservation();
        selectedReservation.setStatut(StatutReservation.EN_ATTENTE);
        selectedReservation.setNombrePersonnes(1);
        selectedReservation.setTable(null);

        dateReservationStr = LocalDate.now().plusDays(1).toString();
        heureReservationStr = getHeureParDefaut();

        chargerTablesDisponibles();

        modeEdition = false;
        showForm = true;
        return null;
    }

    public String editer(Reservation r) {
        if (r == null) {
            return null;
        }

        selectedReservation = r;

        dateReservationStr = (r.getDateReservation() != null)
                ? r.getDateReservation().toString()
                : LocalDate.now().plusDays(1).toString();

        heureReservationStr = (r.getHeureReservation() != null)
                ? r.getHeureReservation().withSecond(0).withNano(0).toString()
                : getHeureParDefaut();

        List<TableRestaurant> tables = tableDAO.findAll();
        tablesDisponibles = (tables != null) ? tables : new ArrayList<>();

        modeEdition = true;
        showForm = true;
        return null;
    }

    public String sauvegarder() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) ctx.getExternalContext().getSession(false);
        Utilisateur user = session != null ? (Utilisateur) session.getAttribute("currentUser") : null;

        if (user == null) {
            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Utilisateur non connecté.",
                    null
            ));
            return null;
        }

        try {
            if (selectedReservation == null) {
                ctx.addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        "Réservation introuvable.",
                        null
                ));
                return null;
            }

            if (dateReservationStr == null || dateReservationStr.trim().isEmpty()) {
                ctx.addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        "La date est obligatoire.",
                        null
                ));
                return null;
            }

            if (heureReservationStr == null || heureReservationStr.trim().isEmpty()) {
                ctx.addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        "L'heure est obligatoire.",
                        null
                ));
                return null;
            }

            if (selectedReservation.getNombrePersonnes() <= 0) {
                ctx.addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        "Le nombre de personnes doit être supérieur à 0.",
                        null
                ));
                return null;
            }

            if (selectedReservation.getTable() == null) {
                ctx.addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        "Veuillez choisir une table.",
                        null
                ));
                return null;
            }

            LocalDate dateReservation = LocalDate.parse(dateReservationStr);
            LocalTime heureReservation = LocalTime.parse(heureReservationStr);

            if (dateReservation.isBefore(LocalDate.now())) {
                ctx.addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        "La date ne peut pas être dans le passé.",
                        null
                ));
                return null;
            }

            if (selectedReservation.getTable().getCapacite() < selectedReservation.getNombrePersonnes()) {
                ctx.addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        "La table choisie ne convient pas au nombre de personnes.",
                        null
                ));
                return null;
            }

            selectedReservation.setDateReservation(dateReservation);
            selectedReservation.setHeureReservation(heureReservation);
            selectedReservation.setClient(user);

            if (modeEdition) {
                reservationDAO.update(selectedReservation);
                ctx.addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_INFO,
                        "Réservation modifiée avec succès.",
                        null
                ));
            } else {
                selectedReservation.setStatut(StatutReservation.EN_ATTENTE);
                reservationDAO.save(selectedReservation);
                ctx.addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_INFO,
                        "Réservation créée avec succès.",
                        null
                ));
            }

            showForm = false;
            modeEdition = false;
            selectedReservation = null;

            charger();
            chargerTablesDisponibles();

        } catch (Exception e) {
            e.printStackTrace();
            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Erreur lors de la sauvegarde : " + e.getMessage(),
                    null
            ));
        }

        return null;
    }

    public String annuler(Reservation r) {
        if (r == null) {
            return null;
        }

        try {
            r.setStatut(StatutReservation.ANNULEE);
            reservationDAO.update(r);
            charger();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_INFO,
                            "Réservation annulée avec succès.",
                            null
                    ));

        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            "Erreur lors de l'annulation.",
                            null
                    ));
        }

        return null;
    }

    public String fermerForm() {
        showForm = false;
        modeEdition = false;
        selectedReservation = null;
        return null;
    }

    public String augmenterNombrePersonnes() {
        if (selectedReservation != null) {
            selectedReservation.setNombrePersonnes(selectedReservation.getNombrePersonnes() + 1);

            if (selectedReservation.getTable() != null
                    && selectedReservation.getTable().getCapacite() < selectedReservation.getNombrePersonnes()) {
                selectedReservation.setTable(null);
            }
        }
        return null;
    }

    public String diminuerNombrePersonnes() {
        if (selectedReservation != null && selectedReservation.getNombrePersonnes() > 1) {
            selectedReservation.setNombrePersonnes(selectedReservation.getNombrePersonnes() - 1);
        }
        return null;
    }

    public List<TableRestaurant> getTablesCompatibles() {
        List<TableRestaurant> resultat = new ArrayList<>();

        if (tablesDisponibles == null || selectedReservation == null) {
            return resultat;
        }

        int nb = selectedReservation.getNombrePersonnes();
        if (nb <= 0) {
            nb = 1;
        }

        for (TableRestaurant table : tablesDisponibles) {
            if (table != null && table.getCapacite() >= nb) {
                resultat.add(table);
            }
        }

        return resultat;
    }

    private String getHeureParDefaut() {
        LocalTime now = LocalTime.now();

        if (now.isBefore(LocalTime.of(12, 0))) {
            return "12:30";
        } else if (now.isBefore(LocalTime.of(14, 0))) {
            return "13:00";
        } else if (now.isBefore(LocalTime.of(19, 0))) {
            return "19:30";
        } else if (now.isBefore(LocalTime.of(21, 0))) {
            return "20:00";
        } else {
            return "12:30";
        }
    }

    public List<Reservation> getMesReservations() {
        return mesReservations;
    }

    public List<TableRestaurant> getTablesDisponibles() {
        return tablesDisponibles;
    }

    public Reservation getSelectedReservation() {
        return selectedReservation;
    }

    public void setSelectedReservation(Reservation selectedReservation) {
        this.selectedReservation = selectedReservation;
    }

    public boolean isShowForm() {
        return showForm;
    }

    public boolean isModeEdition() {
        return modeEdition;
    }

    public String getDateReservationStr() {
        return dateReservationStr;
    }

    public void setDateReservationStr(String dateReservationStr) {
        this.dateReservationStr = dateReservationStr;
    }

    public String getHeureReservationStr() {
        return heureReservationStr;
    }

    public void setHeureReservationStr(String heureReservationStr) {
        this.heureReservationStr = heureReservationStr;
    }
}