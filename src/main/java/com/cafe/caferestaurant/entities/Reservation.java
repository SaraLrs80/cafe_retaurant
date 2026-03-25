package com.cafe.caferestaurant.entities;

import com.cafe.caferestaurant.enums.StatutReservation;
import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "reservation")
public class Reservation implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reservation")
    private Long idReservation;

    @Column(name = "date_reservation", nullable = false)
    private LocalDate dateReservation;

    @Column(name = "heure_reservation", nullable = false)
    private LocalTime heureReservation;

    @Column(name = "nombre_personnes", nullable = false)
    private int nombrePersonnes;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutReservation statut;

    @ManyToOne
    @JoinColumn(name = "id_client")
    private Utilisateur client;

    @ManyToOne
    @JoinColumn(name = "id_table")
    private TableRestaurant table;

    public Long getIdReservation()               { return idReservation; }
    public void setIdReservation(Long v)         { this.idReservation = v; }

    public LocalDate getDateReservation()        { return dateReservation; }
    public void setDateReservation(LocalDate v)  { this.dateReservation = v; }

    public LocalTime getHeureReservation()       { return heureReservation; }
    public void setHeureReservation(LocalTime v) { this.heureReservation = v; }

    public int getNombrePersonnes()              { return nombrePersonnes; }
    public void setNombrePersonnes(int v)        { this.nombrePersonnes = v; }

    public StatutReservation getStatut()              { return statut; }
    public void setStatut(StatutReservation v)        { this.statut = v; }

    public Utilisateur getClient()               { return client; }
    public void setClient(Utilisateur v)         { this.client = v; }

    public TableRestaurant getTable()            { return table; }
    public void setTable(TableRestaurant v)      { this.table = v; }

    public String getDateReservationFormatted() {
        if (dateReservation == null) return "";
        return dateReservation.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public String getHeureReservationFormatted() {
        if (heureReservation == null) return "";
        return heureReservation.format(DateTimeFormatter.ofPattern("HH:mm"));
    }
}