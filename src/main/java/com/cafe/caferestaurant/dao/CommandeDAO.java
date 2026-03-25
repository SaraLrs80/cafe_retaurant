package com.cafe.caferestaurant.dao;

import com.cafe.caferestaurant.entities.Commande;
import com.cafe.caferestaurant.entities.LigneCommande;
import com.cafe.caferestaurant.enums.StatutCommande;
import com.cafe.caferestaurant.utils.HibernateUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import java.time.LocalDate;
import java.util.List;

/**
 * DAO Commande — version fusionnée Manar + Sara.
 * Base de données : colonnes enum migrées en varchar → em.merge/persist OK.
 */
public class CommandeDAO {

    // ══════════════════════════════════════════════════════════════════════
    // MÉTHODES ADMIN
    // ══════════════════════════════════════════════════════════════════════

    public long countCommandesEnCours() {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT COUNT(c) FROM Commande c WHERE c.statut IN :statuts", Long.class)
                    .setParameter("statuts", List.of(StatutCommande.EN_ATTENTE, StatutCommande.EN_COURS))
                    .getSingleResult();
        } catch (Exception e) { return 0; } finally { em.close(); }
    }

    public double chiffreAffairesAujourdhui() {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            Double result = em.createQuery(
                            "SELECT SUM(c.montantTotal) FROM Commande c WHERE c.statut = :s " +
                                    "AND CAST(c.dateCommande AS date) = :today", Double.class)
                    .setParameter("s", StatutCommande.PAYEE)
                    .setParameter("today", LocalDate.now())
                    .getSingleResult();
            return result != null ? result : 0.0;
        } catch (Exception e) { return 0.0; } finally { em.close(); }
    }

    public String findPlatLePlusCommande() {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT m.nom FROM LigneCommande lc JOIN lc.menu m " +
                                    "GROUP BY m.nom ORDER BY SUM(lc.quantite) DESC", String.class)
                    .setMaxResults(1).getSingleResult();
        } catch (NoResultException e) { return "—"; } finally { em.close(); }
    }

    public long countPlatLePlusCommande() {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            Long r = em.createQuery(
                            "SELECT SUM(lc.quantite) FROM LigneCommande lc JOIN lc.menu m " +
                                    "GROUP BY m.nom ORDER BY SUM(lc.quantite) DESC", Long.class)
                    .setMaxResults(1).getSingleResult();
            return r != null ? r : 0;
        } catch (NoResultException e) { return 0; } finally { em.close(); }
    }

    public List<Commande> findCommandesRecentes(int limit) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT c FROM Commande c ORDER BY c.dateCommande DESC", Commande.class)
                    .setMaxResults(limit).getResultList();
        } finally { em.close(); }
    }

    public List<Commande> findAll() {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT DISTINCT c FROM Commande c " +
                                    "LEFT JOIN FETCH c.client LEFT JOIN FETCH c.table " +
                                    "ORDER BY c.dateCommande DESC", Commande.class)
                    .getResultList();
        } finally { em.close(); }
    }

    public List<Commande> findByStatut(StatutCommande statut) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT c FROM Commande c WHERE c.statut = :statut " +
                                    "ORDER BY c.dateCommande DESC", Commande.class)
                    .setParameter("statut", statut).getResultList();
        } finally { em.close(); }
    }

    public Commande findByIdWithLignes(Long id) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT c FROM Commande c " +
                                    "LEFT JOIN FETCH c.lignes l LEFT JOIN FETCH l.menu " +
                                    "WHERE c.idCommande = :id", Commande.class)
                    .setParameter("id", id).getSingleResult();
        } catch (NoResultException e) { return null; } finally { em.close(); }
    }

    // ── update via em.merge (base migrée en varchar) ──────────────────────
    public void update(Commande c) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(c);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException("Erreur update Commande", e);
        } finally { em.close(); }
    }

    // ══════════════════════════════════════════════════════════════════════
    // SAVE — avec cascade LigneCommande (Manar)
    // ══════════════════════════════════════════════════════════════════════

    public void save(Commande c) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(c);
            if (c.getLignes() != null) {
                for (LigneCommande ligne : c.getLignes()) {
                    if (ligne.getCommande() == null) ligne.setCommande(c);
                    if (!em.contains(ligne.getMenu())) ligne.setMenu(em.merge(ligne.getMenu()));
                    em.persist(ligne);
                }
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Erreur save Commande: " + e.getMessage(), e);
        } finally { em.close(); }
    }

    // ══════════════════════════════════════════════════════════════════════
    // MÉTHODES CLIENT (Manar)
    // ══════════════════════════════════════════════════════════════════════

    public long countCommandesEnCoursClient(Long idClient) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT COUNT(c) FROM Commande c WHERE c.client.idUser = :id " +
                                    "AND c.statut IN :statuts", Long.class)
                    .setParameter("id", idClient)
                    .setParameter("statuts", List.of(StatutCommande.EN_ATTENTE, StatutCommande.EN_COURS))
                    .getSingleResult();
        } catch (Exception e) { return 0; } finally { em.close(); }
    }

    public long countCommandesTotalClient(Long idClient) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT COUNT(c) FROM Commande c WHERE c.client.idUser = :id", Long.class)
                    .setParameter("id", idClient).getSingleResult();
        } catch (Exception e) { return 0; } finally { em.close(); }
    }

    public List<Commande> findDernieresCommandesClient(Long idClient, int limit) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT c FROM Commande c WHERE c.client.idUser = :id " +
                                    "ORDER BY c.dateCommande DESC", Commande.class)
                    .setParameter("id", idClient)
                    .setMaxResults(limit).getResultList();
        } finally { em.close(); }
    }

    public List<Commande> findByClient(Long idClient) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT c FROM Commande c WHERE c.client.idUser = :id " +
                                    "ORDER BY c.dateCommande DESC", Commande.class)
                    .setParameter("id", idClient).getResultList();
        } finally { em.close(); }
    }

    public List<Commande> findByClientAndStatut(Long idClient, StatutCommande statut) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT c FROM Commande c WHERE c.client.idUser = :id " +
                                    "AND c.statut = :statut ORDER BY c.dateCommande DESC", Commande.class)
                    .setParameter("id", idClient)
                    .setParameter("statut", statut).getResultList();
        } finally { em.close(); }
    }
}