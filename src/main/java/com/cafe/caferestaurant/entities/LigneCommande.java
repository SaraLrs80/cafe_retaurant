package com.cafe.caferestaurant.entities;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "ligne_commande")
public class LigneCommande implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ligne")
    private Long idLigne;

    @Column(name = "quantite", nullable = false)
    private int quantite;

    @Column(name = "prix_unitaire")
    private BigDecimal prixUnitaire;

    @ManyToOne
    @JoinColumn(name = "id_commande")
    private Commande commande;

    @ManyToOne
    @JoinColumn(name = "id_menu")
    private Menu menu;

    public Long getIdLigne()                  { return idLigne; }
    public void setIdLigne(Long v)            { this.idLigne = v; }

    public int getQuantite()                  { return quantite; }
    public void setQuantite(int v)            { this.quantite = v; }

    public BigDecimal getPrixUnitaire()       { return prixUnitaire; }
    public void setPrixUnitaire(BigDecimal v) { this.prixUnitaire = v; }

    public Commande getCommande()             { return commande; }
    public void setCommande(Commande v)       { this.commande = v; }

    public Menu getMenu()                     { return menu; }
    public void setMenu(Menu v)               { this.menu = v; }

    public BigDecimal getTotalLigne() {
        if (prixUnitaire == null) {
            return BigDecimal.ZERO;
        }
        return prixUnitaire.multiply(BigDecimal.valueOf(quantite));
    }
}