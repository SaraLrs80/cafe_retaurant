package com.cafe.caferestaurant.beans;

import com.cafe.caferestaurant.dao.CommandeDAO;
import com.cafe.caferestaurant.dao.ReservationDAO;
import com.cafe.caferestaurant.entities.Commande;
import com.cafe.caferestaurant.entities.Reservation;
import com.cafe.caferestaurant.entities.Utilisateur;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;

import java.io.Serializable;
import java.util.List;

/**
 * Bean JSF — Tableau de bord CLIENT.
 * RequestScoped : données fraîches à chaque chargement.
 * Chemin : src/main/java/com/cafe/caferestaurant/beans/ClientDashboardBean.java
 */
@Named("clientDashboardBean")
@RequestScoped
public class ClientDashboardBean implements Serializable {

    private final ReservationDAO reservationDAO = new ReservationDAO();
    private final CommandeDAO    commandeDAO    = new CommandeDAO();

    // ── Données du client connecté ────────────────────────────────────────────
    private Utilisateur currentUser;

    // ── Statistiques personnelles ─────────────────────────────────────────────
    private long   nbReservationsActives;
    private long   nbCommandesEnCours;
    private long   nbCommandesTotal;
    private long   nbReservationsTotal;

    // ── Listes ───────────────────────────────────────────────────────────────
    private List<Reservation> prochainesReservations;
    private List<Commande>    dernieresCommandes;

    // ── Init ──────────────────────────────────────────────────────────────────

    @PostConstruct
    public void init() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) ctx.getExternalContext().getSession(false);

        if (session != null) {
            currentUser = (Utilisateur) session.getAttribute("currentUser");
        }

        if (currentUser != null) {
            Long userId = currentUser.getIdUser();

            nbReservationsActives  = reservationDAO.countReservationsActivesClient(userId);
            nbCommandesEnCours     = commandeDAO.countCommandesEnCoursClient(userId);
            nbCommandesTotal       = commandeDAO.countCommandesTotalClient(userId);
            nbReservationsTotal    = reservationDAO.countReservationsTotalClient(userId);

            prochainesReservations = reservationDAO.findProchainesReservationsClient(userId, 5);
            dernieresCommandes     = commandeDAO.findDernieresCommandesClient(userId, 5);
        }
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public Utilisateur          getCurrentUser()              { return currentUser; }
    public long                 getNbReservationsActives()    { return nbReservationsActives; }
    public long                 getNbCommandesEnCours()       { return nbCommandesEnCours; }
    public long                 getNbCommandesTotal()         { return nbCommandesTotal; }
    public long                 getNbReservationsTotal()      { return nbReservationsTotal; }
    public List<Reservation>    getProchainesReservations()   { return prochainesReservations; }
    public List<Commande>       getDernieresCommandes()       { return dernieresCommandes; }
}
