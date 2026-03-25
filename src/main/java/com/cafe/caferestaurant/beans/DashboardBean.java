package com.cafe.caferestaurant.beans;

import com.cafe.caferestaurant.dao.CommandeDAO;
import com.cafe.caferestaurant.dao.ReservationDAO;
import com.cafe.caferestaurant.dao.TableDAO;
import com.cafe.caferestaurant.entities.Commande;
import com.cafe.caferestaurant.entities.Reservation;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;

/**
 * Bean JSF — Données du tableau de bord admin.
 * RequestScoped : recalculé à chaque chargement de page → données toujours fraîches.
 */
@Named("dashboardBean")
@RequestScoped
public class DashboardBean implements Serializable {

    // ── DAO ───────────────────────────────────────────────────────────────────
    private final ReservationDAO reservationDAO = new ReservationDAO();
    private final CommandeDAO    commandeDAO    = new CommandeDAO();
    private final TableDAO       tableDAO       = new TableDAO();

    // ── Données chargées au démarrage ─────────────────────────────────────────
    private long   nbReservationsJour;
    private long   nbCommandesEnCours;
    private double chiffreAffairesJour;
    private long   nbTablesDisponibles;
    private String platPopulaire;
    private long   nbFoisCommandePlatPopulaire;

    private List<Reservation> reservationsJour;
    private List<Commande>    commandesRecentes;

    // ── Initialisation ────────────────────────────────────────────────────────

    @PostConstruct
    public void init() {
        nbReservationsJour         = reservationDAO.countReservationsAujourdhui();
        nbCommandesEnCours         = commandeDAO.countCommandesEnCours();
        chiffreAffairesJour        = commandeDAO.chiffreAffairesAujourdhui();
        nbTablesDisponibles        = tableDAO.countTablesDisponibles();
        platPopulaire              = commandeDAO.findPlatLePlusCommande();
        nbFoisCommandePlatPopulaire = commandeDAO.countPlatLePlusCommande();
        reservationsJour           = reservationDAO.findReservationsAujourdhui();
        commandesRecentes          = commandeDAO.findCommandesRecentes(5);
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public long   getNbReservationsJour()          { return nbReservationsJour; }
    public long   getNbCommandesEnCours()           { return nbCommandesEnCours; }
    public double getChiffreAffairesJour()          { return chiffreAffairesJour; }
    public long   getNbTablesDisponibles()          { return nbTablesDisponibles; }
    public String getPlatPopulaire()                { return platPopulaire != null ? platPopulaire : "—"; }
    public long   getNbFoisCommandePlatPopulaire()  { return nbFoisCommandePlatPopulaire; }
    public List<Reservation> getReservationsJour()  { return reservationsJour; }
    public List<Commande>    getCommandesRecentes() { return commandesRecentes; }
}
