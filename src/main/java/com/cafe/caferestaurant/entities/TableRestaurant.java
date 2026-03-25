package com.cafe.caferestaurant.entities;

import com.cafe.caferestaurant.enums.StatutTable;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "table_restaurant")
public class TableRestaurant implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_table")
    private Long idTable;

    @Column(name = "numero_table", nullable = false, unique = true)
    private int numeroTable;

    @Column(name = "capacite", nullable = false)
    private int capacite;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", columnDefinition = "statut_table_enum")
    private StatutTable statut;

    @Column(name = "emplacement")
    private String emplacement;
    // ── equals/hashCode sur idTable — indispensable pour JSF selectOneMenu ───
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TableRestaurant)) return false;
        TableRestaurant that = (TableRestaurant) o;
        return Objects.equals(idTable, that.idTable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idTable);
    }

    public Long getIdTable()              { return idTable; }
    public void setIdTable(Long v)        { this.idTable = v; }

    public int getNumeroTable()           { return numeroTable; }
    public void setNumeroTable(int v)     { this.numeroTable = v; }

    public int getCapacite()              { return capacite; }
    public void setCapacite(int v)        { this.capacite = v; }

    public StatutTable getStatut()        { return statut; }
    public void setStatut(StatutTable v)  { this.statut = v; }

    public String getEmplacement()        { return emplacement; }
    public void setEmplacement(String v)  { this.emplacement = v; }
    @Override
    public String toString() {
        return "TableRestaurant{" +
                "idTable=" + idTable +
                ", numeroTable=" + numeroTable +
                ", capacite=" + capacite +
                ", statut=" + statut +
                ", emplacement='" + emplacement + '\'' +
                '}';
    }
}