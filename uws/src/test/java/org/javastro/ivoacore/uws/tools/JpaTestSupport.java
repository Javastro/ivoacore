package org.javastro.ivoacore.uws.tools;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JpaTestSupport {

    private EntityManagerFactory emf;
    private EntityManager em;

    public void start() {
        emf = Persistence.createEntityManagerFactory("my-pu");
        em = emf.createEntityManager();
    }

    public EntityManager em() {
        return em;
    }

    public void stop() {
        if (em != null) em.close();
        if (emf != null) emf.close();
    }
}