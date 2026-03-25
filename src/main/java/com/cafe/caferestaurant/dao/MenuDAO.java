package com.cafe.caferestaurant.dao;

import com.cafe.caferestaurant.entities.Menu;
import com.cafe.caferestaurant.enums.CategorieMenu;
import com.cafe.caferestaurant.utils.HibernateUtil;
import jakarta.persistence.EntityManager;

import java.util.List;

/**
 * DAO Menu — méthodes admin + client.
 * Chemin : src/main/java/com/cafe/caferestaurant/dao/MenuDAO.java
 */
public class MenuDAO {

    // ══════════════════════════════════════════════════════════════════════
    // MÉTHODES ADMIN (existantes)
    // ══════════════════════════════════════════════════════════════════════

    public List<Menu> findAll() {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT m FROM Menu m ORDER BY m.categorie, m.nom", Menu.class)
                    .getResultList();
        } finally { em.close(); }
    }

    public List<Menu> findByCategorie(String categorie) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT m FROM Menu m WHERE m.categorie = :cat ORDER BY m.nom", Menu.class)
                    .setParameter("cat", CategorieMenu.valueOf(categorie))
                    .getResultList();
        } finally { em.close(); }
    }

    public Menu findById(Long id) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.find(Menu.class, id);
        } finally { em.close(); }
    }

    public void save(Menu m) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(m);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException("Erreur save Menu", e);
        } finally { em.close(); }
    }

    public void update(Menu m) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(m);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException("Erreur update Menu", e);
        } finally { em.close(); }
    }

    public void delete(Long id) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            Menu m = em.find(Menu.class, id);
            if (m != null) em.remove(m);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException("Erreur delete Menu", e);
        } finally { em.close(); }
    }

    // ══════════════════════════════════════════════════════════════════════
    // MÉTHODES CLIENT
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Plats disponibles uniquement (pour le client).
     */
    public List<Menu> findDisponibles() {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT m FROM Menu m WHERE m.disponibilite = true " +
                                    "ORDER BY m.categorie, m.nom", Menu.class)
                    .getResultList();
        } finally { em.close(); }
    }

    /**
     * Plats disponibles filtrés par catégorie (pour le client).
     */
    public List<Menu> findDisponiblesByCategorie(String categorie) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT m FROM Menu m WHERE m.disponibilite = true " +
                                    "AND m.categorie = :cat ORDER BY m.nom", Menu.class)
                    .setParameter("cat", CategorieMenu.valueOf(categorie))
                    .getResultList();
        } finally { em.close(); }
    }
}
