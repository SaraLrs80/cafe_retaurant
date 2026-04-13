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
                    "SELECT COUNT(t) FROM TableRestaurant t WHERE t.statut = 'DISPONIBLE' AND t.active = true",
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
                    "SELECT t FROM TableRestaurant t WHERE t.active = true ORDER BY t.numeroTable ASC",
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
                    "SELECT t FROM TableRestaurant t WHERE t.statut = 'DISPONIBLE' AND t.active = true ORDER BY t.numeroTable ASC",
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

            if (table == null) return;

            // On refuse si la table n'est pas disponible
            if (!table.getStatut().name().equals("DISPONIBLE")) {
                throw new IllegalStateException("TABLE_NOT_AVAILABLE");
            }

            // Désactivation logique au lieu de suppression physique
            table.setActive(false);
            em.merge(table);

            em.getTransaction().commit();

        } catch (IllegalStateException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Erreur delete Table", e);
        } finally {
            em.close();
        }
    }

    public Integer getNextNumeroTable() {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            Integer maxNumero = em.createQuery(
                    "SELECT MAX(t.numeroTable) FROM TableRestaurant t",
                    Integer.class
            ).getSingleResult();

            return (maxNumero == null) ? 1 : maxNumero + 1;
        } finally {
            em.close();
        }
    }
}