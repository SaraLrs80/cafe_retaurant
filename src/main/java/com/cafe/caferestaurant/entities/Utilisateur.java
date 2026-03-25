package com.cafe.caferestaurant.entities;

import com.cafe.caferestaurant.enums.Role;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * Entité Hibernate — table `utilisateur` en base.
 * Représente les 3 rôles : ADMIN, SERVEUR, CLIENT.
 */
@Entity
@Table(name = "utilisateur",
        uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class Utilisateur implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_user")
    private Long idUser;

    @Column(nullable = false, length = 80)
    private String nom;

    @Column(nullable = false, length = 80)
    private String prenom;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "mot_de_passe", nullable = false, length = 255)
    private String motDePasse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10,name = "role", columnDefinition = "role_enum")
    private Role role;

    @Column(length = 20)
    private String telephone;

    @Temporal(TemporalType.DATE)
    @Column(name = "date_naissance")
    private Date dateNaissance;

    @Column(name = "statut_compte", nullable = false)
    private boolean statutCompte = true;

    /** Identifiant Google OAuth — null si connexion classique uniquement. */
    @Column(name = "google_id", length = 100)
    private String googleId;


    // ── equals/hashCode sur idUser — indispensable pour JSF selectOneMenu ────
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Utilisateur)) return false;
        Utilisateur that = (Utilisateur) o;
        return Objects.equals(idUser, that.idUser);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idUser);
    }
    // ─── Getters / Setters ────────────────────────────────────────────────────

    public Long    getIdUser()                      { return idUser; }
    public void    setIdUser(Long v)                { this.idUser = v; }

    public String  getNom()                         { return nom; }
    public void    setNom(String v)                 { this.nom = v; }

    public String  getPrenom()                      { return prenom; }
    public void    setPrenom(String v)              { this.prenom = v; }

    public String  getEmail()                       { return email; }
    public void    setEmail(String v)               { this.email = v; }

    public String  getMotDePasse()                  { return motDePasse; }
    public void    setMotDePasse(String v)          { this.motDePasse = v; }

    public Role getRole()                        { return role; }
    public void    setRole(Role v)                  { this.role = v; }

    public String  getTelephone()                   { return telephone; }
    public void    setTelephone(String v)           { this.telephone = v; }

    public Date    getDateNaissance()               { return dateNaissance; }
    public void    setDateNaissance(Date v)         { this.dateNaissance = v; }

    public boolean isStatutCompte()                 { return statutCompte; }
    public void    setStatutCompte(boolean v)       { this.statutCompte = v; }

    public String  getGoogleId()                    { return googleId; }
    public void    setGoogleId(String v)            { this.googleId = v; }
}