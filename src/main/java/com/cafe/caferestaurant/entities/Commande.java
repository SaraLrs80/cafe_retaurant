package com.cafe.caferestaurant.entities;

import com.cafe.caferestaurant.enums.StatutCommande;
import com.cafe.caferestaurant.enums.TypeCommande;
import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Entity
@Table(name = "commande")
public class Commande implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_commande")
    private Long idCommande;

    @Column(name = "date_commande")
    private LocalDateTime dateCommande;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_commande", length = 20, nullable = false)
    private TypeCommande typeCommande;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", length = 20, nullable = false)
    private StatutCommande statut;

    @Column(name = "montant_total")
    private BigDecimal montantTotal;

    @ManyToOne
    @JoinColumn(name = "id_table")
    private TableRestaurant table;

    @ManyToOne
    @JoinColumn(name = "id_client")
    private Utilisateur client;

    @ManyToOne
    @JoinColumn(name = "id_serveur")
    private Utilisateur serveur;

    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LigneCommande> lignes;

    public Long getIdCommande()                    { return idCommande; }
    public void setIdCommande(Long v)              { this.idCommande = v; }

    public LocalDateTime getDateCommande()         { return dateCommande; }
    public void setDateCommande(LocalDateTime v)   { this.dateCommande = v; }

    public TypeCommande getTypeCommande()          { return typeCommande; }
    public void setTypeCommande(TypeCommande v)    { this.typeCommande = v; }

    public StatutCommande getStatut()              { return statut; }
    public void setStatut(StatutCommande v)        { this.statut = v; }

    public BigDecimal getMontantTotal()            { return montantTotal; }
    public void setMontantTotal(BigDecimal v)      { this.montantTotal = v; }

    public TableRestaurant getTable()              { return table; }
    public void setTable(TableRestaurant v)        { this.table = v; }

    public Utilisateur getClient()                 { return client; }
    public void setClient(Utilisateur v)           { this.client = v; }

    public Utilisateur getServeur()                { return serveur; }
    public void setServeur(Utilisateur v)          { this.serveur = v; }

    public List<LigneCommande> getLignes()         { return lignes; }
    public void setLignes(List<LigneCommande> v)   { this.lignes = v; }

    public String getDateCommandeFormatted() {
        if (dateCommande == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return dateCommande.format(formatter);
    }
}