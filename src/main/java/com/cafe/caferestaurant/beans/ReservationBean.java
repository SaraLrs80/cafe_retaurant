package com.cafe.caferestaurant.beans;

import com.cafe.caferestaurant.dao.ReservationDAO;
import com.cafe.caferestaurant.dao.TableDAO;
import com.cafe.caferestaurant.dao.UtilisateurDAO;
import com.cafe.caferestaurant.entities.Reservation;
import com.cafe.caferestaurant.entities.TableRestaurant;
import com.cafe.caferestaurant.entities.Utilisateur;
import com.cafe.caferestaurant.enums.Role;
import com.cafe.caferestaurant.enums.StatutReservation;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * ReservationBean — version fusionnée Manar + Sara.
 *
 * - Garde selectedDateStr / selectedHeureStr (String) de Manar
 *   → nécessaire pour le formulaire JSF client ET admin
 * - Garde filtreDate (LocalDate) de Sara → plus propre pour le filtre admin
 * - Garde findDisponibles() + findByRole(CLIENT) de Sara → plus correct
 * - Garde findAll() en mode édition de Manar → nécessaire pour afficher
 *   la table/client déjà sélectionné
 */
@Named("reservationBean")
@SessionScoped
public class ReservationBean implements Serializable {

    private final ReservationDAO reservationDAO = new ReservationDAO();
    private final TableDAO       tableDAO       = new TableDAO();
    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();

    private List<Reservation>     reservations;
    private List<TableRestaurant> tablesDisponibles;
    private List<Utilisateur>     clients;

    private Reservation selectedReservation;
    private boolean     showForm      = false;
    private boolean     modeEdition   = false;
    private String      filtreStatut  = "";
    private String      filtreDateStr = "";

    // ── Champs String pour JSF (input date et heure) ──────────────────────────
    private String selectedDateStr  = "";
    private String selectedHeureStr = "";

    @PostConstruct
    public void init() {
        charger();
        // Tables disponibles uniquement + seulement les clients (Sara)
        tablesDisponibles = tableDAO.findDisponibles();
        clients           = utilisateurDAO.findByRole(Role.CLIENT);
    }

    private void charger() {
        reservations = reservationDAO.findAll();
    }

    // ── Filtres ───────────────────────────────────────────────────────────────

    public String filtrer() {
        if (filtreDateStr != null && !filtreDateStr.isEmpty()) {
            try {
                reservations = reservationDAO.findByDate(LocalDate.parse(filtreDateStr));
            } catch (Exception e) {
                charger();
            }
        } else if (filtreStatut != null && !filtreStatut.isEmpty()) {
            reservations = reservationDAO.findByStatut(StatutReservation.valueOf(filtreStatut));
        } else {
            charger();
        }
        return null;
    }

    public String filtrerAujourdhui() {
        filtreDateStr = LocalDate.now().toString();
        filtreStatut  = "";
        reservations  = reservationDAO.findReservationsAujourdhui();
        return null;
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    public String nouvelleReservation() {
        selectedReservation = new Reservation();
        selectedReservation.setStatut(StatutReservation.EN_ATTENTE);
        selectedDateStr  = LocalDate.now().toString();
        selectedHeureStr = "12:00";
        // Tables disponibles uniquement pour nouvelle réservation (Sara)
        tablesDisponibles = tableDAO.findDisponibles();
        clients           = utilisateurDAO.findByRole(Role.CLIENT);
        modeEdition = false;
        showForm    = true;
        return null;
    }

    public String editer(Reservation r) {
        selectedReservation = r;
        selectedDateStr  = r.getDateReservation()  != null ? r.getDateReservation().toString()  : "";
        selectedHeureStr = r.getHeureReservation() != null ? r.getHeureReservation().toString() : "";
        // findAll en mode édition pour afficher la table/client actuellement
        // sélectionnés même s'ils ne sont plus "disponibles" (Manar)
        tablesDisponibles = tableDAO.findAll();
        clients           = utilisateurDAO.findByRole(Role.CLIENT);
        modeEdition = true;
        showForm    = true;
        return null;
    }

    public String sauvegarder() {
        // Recharger avant validation JSF (Manar)
        tablesDisponibles = tableDAO.findAll();
        clients           = utilisateurDAO.findByRole(Role.CLIENT);
        try {
            if (selectedDateStr != null && !selectedDateStr.isEmpty())
                selectedReservation.setDateReservation(LocalDate.parse(selectedDateStr));
            if (selectedHeureStr != null && !selectedHeureStr.isEmpty())
                selectedReservation.setHeureReservation(LocalTime.parse(selectedHeureStr));

            if (modeEdition) {
                reservationDAO.update(selectedReservation);
            } else {
                reservationDAO.save(selectedReservation);
            }
            showForm = false;
            charger();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String confirmer(Reservation r) {
        r.setStatut(StatutReservation.CONFIRMEE);
        reservationDAO.update(r);
        charger();
        return null;
    }

    public String annuler(Reservation r) {
        r.setStatut(StatutReservation.ANNULEE);
        reservationDAO.update(r);
        charger();
        return null;
    }

    public String fermerForm() {
        showForm = false;
        return null;
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────
    public List<Reservation>     getReservations()             { return reservations; }
    public List<TableRestaurant> getTablesDisponibles()        { return tablesDisponibles; }
    public List<Utilisateur>     getClients()                  { return clients; }
    public Reservation           getSelectedReservation()      { return selectedReservation; }
    public void                  setSelectedReservation(Reservation r) { this.selectedReservation = r; }
    public boolean               isShowForm()                  { return showForm; }
    public boolean               isModeEdition()               { return modeEdition; }
    public String                getFiltreStatut()             { return filtreStatut; }
    public void                  setFiltreStatut(String v)     { this.filtreStatut = v; }
    public String                getFiltreDateStr()            { return filtreDateStr; }
    public void                  setFiltreDateStr(String v)    { this.filtreDateStr = v; }
    public String                getSelectedDateStr()          { return selectedDateStr; }
    public void                  setSelectedDateStr(String v)  { this.selectedDateStr = v; }
    public String                getSelectedHeureStr()         { return selectedHeureStr; }
    public void                  setSelectedHeureStr(String v) { this.selectedHeureStr = v; }
}
