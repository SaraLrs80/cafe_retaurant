package com.cafe.caferestaurant.dao;

import com.cafe.caferestaurant.entities.Utilisateur;
import com.cafe.caferestaurant.enums.Role;
import com.cafe.caferestaurant.utils.HibernateUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class UtilisateurDAO {

    public Utilisateur findByEmail(String email) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<Utilisateur> query = em.createQuery(
                "SELECT u FROM Utilisateur u WHERE LOWER(u.email) = LOWER(:email)",
                Utilisateur.class);
            query.setParameter("email", email);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    public List<Utilisateur> findAll() {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                "SELECT u FROM Utilisateur u ORDER BY u.nom, u.prenom",
                Utilisateur.class).getResultList();
        } finally {
            em.close();
        }
    }

    public List<Utilisateur> findByRole(Role role) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                "SELECT u FROM Utilisateur u WHERE u.role = :role ORDER BY u.nom",
                Utilisateur.class)
                .setParameter("role", role)
                .getResultList();
        } finally {
            em.close();
        }
    }

    public void save(Utilisateur utilisateur) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(utilisateur);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException("Erreur save utilisateur", e);
        } finally {
            em.close();
        }
    }

    public void update(Utilisateur utilisateur) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(utilisateur);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException("Erreur update utilisateur", e);
        } finally {
            em.close();
        }
    }

    public Utilisateur findById(Long id) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.find(Utilisateur.class, id);
        } finally {
            em.close();
        }
    }
}
