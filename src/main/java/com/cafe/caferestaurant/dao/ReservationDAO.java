package com.cafe.caferestaurant.dao;

import com.cafe.caferestaurant.entities.Reservation;
import com.cafe.caferestaurant.enums.StatutReservation;
import com.cafe.caferestaurant.utils.HibernateUtil;
import jakarta.persistence.EntityManager;

import java.time.LocalDate;
import java.util.List;

/**
 * DAO Reservation — toutes les méthodes admin + client.
 * Chemin : src/main/java/com/cafe/caferestaurant/dao/ReservationDAO.java
 */
public class ReservationDAO {

    // ══════════════════════════════════════════════════════════════════════
    // MÉTHODES ADMIN (existantes)
    // ══════════════════════════════════════════════════════════════════════

    public long countReservationsAujourdhui() {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT COUNT(r) FROM Reservation r WHERE r.dateReservation = :today", Long.class)
                    .setParameter("today", LocalDate.now()).getSingleResult();
        } catch (Exception e) { return 0; } finally { em.close(); }
    }

    public List<Reservation> findReservationsAujourdhui() {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT r FROM Reservation r LEFT JOIN FETCH r.client LEFT JOIN FETCH r.table " +
                                    "WHERE r.dateReservation = :today ORDER BY r.heureReservation ASC", Reservation.class)
                    .setParameter("today", LocalDate.now()).getResultList();
        } finally { em.close(); }
    }

    public List<Reservation> findAll() {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT r FROM Reservation r LEFT JOIN FETCH r.client LEFT JOIN FETCH r.table " +
                                    "ORDER BY r.dateReservation DESC, r.heureReservation DESC", Reservation.class)
                    .getResultList();
        } finally { em.close(); }
    }

    public List<Reservation> findByDate(LocalDate date) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT r FROM Reservation r LEFT JOIN FETCH r.client LEFT JOIN FETCH r.table " +
                                    "WHERE r.dateReservation = :date ORDER BY r.heureReservation ASC", Reservation.class)
                    .setParameter("date", date).getResultList();
        } finally { em.close(); }
    }

    public List<Reservation> findByStatut(StatutReservation statut) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT r FROM Reservation r LEFT JOIN FETCH r.client LEFT JOIN FETCH r.table " +
                                    "WHERE r.statut = :statut ORDER BY r.dateReservation DESC", Reservation.class)
                    .setParameter("statut", statut).getResultList();
        } finally { em.close(); }
    }

    public void save(Reservation r) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(r);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException("Erreur save Reservation", e);
        } finally { em.close(); }
    }

    public void update(Reservation r) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(r);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException("Erreur update Reservation", e);
        } finally { em.close(); }
    }

    public void delete(Long id) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            Reservation r = em.find(Reservation.class, id);
            if (r != null) em.remove(r);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException("Erreur delete Reservation", e);
        } finally { em.close(); }
    }

    // ══════════════════════════════════════════════════════════════════════
    // MÉTHODES CLIENT
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Réservations actives (EN_ATTENTE + CONFIRMEE) d'un client.
     */
    public long countReservationsActivesClient(Long idClient) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT COUNT(r) FROM Reservation r WHERE r.client.idUser = :id " +
                                    "AND r.statut IN :statuts", Long.class)
                    .setParameter("id", idClient)
                    .setParameter("statuts",
                            List.of(StatutReservation.EN_ATTENTE, StatutReservation.CONFIRMEE))
                    .getSingleResult();
        } catch (Exception e) { return 0; } finally { em.close(); }
    }

    /**
     * Total réservations d'un client.
     */
    public long countReservationsTotalClient(Long idClient) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT COUNT(r) FROM Reservation r WHERE r.client.idUser = :id", Long.class)
                    .setParameter("id", idClient).getSingleResult();
        } catch (Exception e) { return 0; } finally { em.close(); }
    }

    /**
     * Prochaines réservations d'un client (date >= aujourd'hui).
     */
    public List<Reservation> findProchainesReservationsClient(Long idClient, int limit) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT r FROM Reservation r LEFT JOIN FETCH r.table " +
                                    "WHERE r.client.idUser = :id AND r.dateReservation >= :today " +
                                    "ORDER BY r.dateReservation ASC, r.heureReservation ASC", Reservation.class)
                    .setParameter("id", idClient)
                    .setParameter("today", LocalDate.now())
                    .setMaxResults(limit).getResultList();
        } finally { em.close(); }
    }

    /**
     * Toutes les réservations d'un client, triées par date DESC.
     */
    public List<Reservation> findByClient(Long idClient) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT r FROM Reservation r LEFT JOIN FETCH r.table " +
                                    "WHERE r.client.idUser = :id " +
                                    "ORDER BY r.dateReservation DESC, r.heureReservation DESC", Reservation.class)
                    .setParameter("id", idClient).getResultList();
        } finally { em.close(); }
    }
}
