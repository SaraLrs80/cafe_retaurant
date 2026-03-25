package com.cafe.caferestaurant.entities;

import com.cafe.caferestaurant.enums.CategorieMenu;
import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "menu")
public class Menu implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_menu")
    private Long idMenu;

    @Column(name = "nom", nullable = false, length = 120)
    private String nom;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "prix", nullable = false)
    private BigDecimal prix;

    @Enumerated(EnumType.STRING)
    @Column(name = "categorie", nullable = false, length = 20)
    private CategorieMenu categorie;

    @Column(name = "disponibilite", nullable = false)
    private boolean disponibilite;

    @Column(name = "image_url")
    private String imageUrl;

    public Long getIdMenu()                    { return idMenu; }
    public void setIdMenu(Long v)              { this.idMenu = v; }

    public String getNom()                     { return nom; }
    public void setNom(String v)               { this.nom = v; }

    public String getDescription()             { return description; }
    public void setDescription(String v)       { this.description = v; }

    public BigDecimal getPrix()                { return prix; }
    public void setPrix(BigDecimal v)          { this.prix = v; }

    public CategorieMenu getCategorie()        { return categorie; }
    public void setCategorie(CategorieMenu v)  { this.categorie = v; }

    public boolean isDisponibilite()           { return disponibilite; }
    public void setDisponibilite(boolean v)    { this.disponibilite = v; }

    public String getImageUrl()                { return imageUrl; }
    public void setImageUrl(String v)          { this.imageUrl = v; }
}