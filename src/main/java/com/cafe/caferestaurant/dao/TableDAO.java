package com.cafe.caferestaurant.dao;

import com.cafe.caferestaurant.entities.TableRestaurant;
import com.cafe.caferestaurant.utils.HibernateUtil;
import jakarta.persistence.EntityManager;

import java.util.List;

public class TableDAO {

    public long countTablesDisponibles() {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                    "SELECT COUNT(t) FROM TableRestaurant t WHERE t.statut = 'DISPONIBLE'",
                    Long.class
            ).getSingleResult();
        } catch (Exception e) {
            return 0;
        } finally {
            em.close();
        }
    }

    public List<TableRestaurant> findAll() {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                    "SELECT t FROM TableRestaurant t ORDER BY t.numeroTable ASC",
                    TableRestaurant.class
            ).getResultList();
        } finally {
            em.close();
        }
    }

    public List<TableRestaurant> findDisponibles() {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                    "SELECT t FROM TableRestaurant t WHERE t.statut = 'DISPONIBLE' ORDER BY t.numeroTable ASC",
                    TableRestaurant.class
            ).getResultList();
        } finally {
            em.close();
        }
    }

    public TableRestaurant findById(Long id) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.find(TableRestaurant.class, id);
        } finally {
            em.close();
        }
    }

    public void save(TableRestaurant table) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(table);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Erreur save Table", e);
        } finally {
            em.close();
        }
    }

    public void update(TableRestaurant table) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(table);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Erreur update Table", e);
        } finally {
            em.close();
        }
    }

    public void delete(Long id) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            TableRestaurant table = em.find(TableRestaurant.class, id);
            if (table != null) {
                em.remove(table);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Erreur delete Table", e);
        } finally {
            em.close();
        }
    }
}