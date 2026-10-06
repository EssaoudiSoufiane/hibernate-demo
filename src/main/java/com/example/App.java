package com.example;

import com.example.model.Categorie;
import com.example.model.Produit;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class App {
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hibernate-demo");

        insererProduits(emf);
        lireProduits(emf);

        // 1. Mise à jour du prix
        mettreAJourPrix(emf, 2L, new BigDecimal("449.99"));

        // 4. Recherche par plage de prix
        rechercherParPlagePrix(emf, new BigDecimal("300.00"), new BigDecimal("500.00"));

        // 2. Suppression
        supprimerProduit(emf, 3L);

        lireProduits(emf);

        emf.close();
    }

    private static void insererProduits(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            Categorie informatique = new Categorie("Informatique");
            Categorie mobilite = new Categorie("Mobilité");
            em.persist(informatique);
            em.persist(mobilite);

            Produit p1 = new Produit("Laptop", "Ordinateur portable 15 pouces",
                    new BigDecimal("999.99"), 10, LocalDate.now(), informatique);
            Produit p2 = new Produit("Smartphone", "Téléphone Android 128 Go",
                    new BigDecimal("499.99"), 25, LocalDate.now(), mobilite);
            Produit p3 = new Produit("Tablette", "Tablette 10 pouces avec stylet",
                    new BigDecimal("299.99"), 15, LocalDate.now(), mobilite);

            em.persist(p1);
            em.persist(p2);
            em.persist(p3);

            em.getTransaction().commit();
            System.out.println("Produits insérés avec succès !");
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    private static void lireProduits(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();
        try {
            // JOIN FETCH pour charger la catégorie avant la fermeture de l'EntityManager
            List<Produit> produits = em.createQuery(
                            "SELECT p FROM Produit p LEFT JOIN FETCH p.categorie", Produit.class)
                    .getResultList();

            System.out.println("\nListe des produits :");
            for (Produit produit : produits) {
                System.out.println(produit);
            }
        } finally {
            em.close();
        }
    }

    // 1. Mettre à jour le prix d'un produit
    private static void mettreAJourPrix(EntityManagerFactory emf, Long id, BigDecimal nouveauPrix) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            Produit produit = em.find(Produit.class, id);
            if (produit != null) {
                produit.setPrix(nouveauPrix); // entité gérée : UPDATE automatique au commit
                System.out.println("\nPrix du produit " + id + " mis à jour : " + nouveauPrix);
            } else {
                System.out.println("\nProduit " + id + " introuvable");
            }

            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    // 2. Supprimer un produit
    private static void supprimerProduit(EntityManagerFactory emf, Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            Produit produit = em.find(Produit.class, id);
            if (produit != null) {
                em.remove(produit);
                System.out.println("\nProduit " + id + " supprimé");
            } else {
                System.out.println("\nProduit " + id + " introuvable");
            }

            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    // 4. Recherche par plage de prix
    private static void rechercherParPlagePrix(EntityManagerFactory emf, BigDecimal min, BigDecimal max) {
        EntityManager em = emf.createEntityManager();
        try {
            List<Produit> resultats = em.createQuery(
                            "SELECT p FROM Produit p LEFT JOIN FETCH p.categorie " +
                                    "WHERE p.prix BETWEEN :min AND :max ORDER BY p.prix", Produit.class)
                    .setParameter("min", min)
                    .setParameter("max", max)
                    .getResultList();

            System.out.println("\nProduits entre " + min + " et " + max + " :");
            if (resultats.isEmpty()) {
                System.out.println("Aucun produit trouvé");
            }
            for (Produit p : resultats) {
                System.out.println(p);
            }
        } finally {
            em.close();
        }
    }
}