package org.javastro.ivoacore.uws.tools;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Utility class to simplify the setup and management of JPA EntityManager and EntityManagerFactory
 * for testing purposes. This class provides a convenient way to obtain an {@link EntityManager}
 * and ensures proper resource cleanup by implementing {@link AutoCloseable}.
 *
 * <p>The {@link #entityManager()} method can be used to access the managed {@link EntityManager}
 * instance, which is created using a persistence unit named "my-pu".
 *
 * <p>Upon closing this class using the {@link #close()} method, it ensures that both the
 * {@link EntityManager} and {@link EntityManagerFactory} are properly closed to release
 * any allocated resources.
 *
 * <p>This class is particularly useful in testing scenarios where a simple and reusable
 * JPA setup is required.
 */
public final class JpaTestSupport implements AutoCloseable {

    private final EntityManagerFactory emf;
    private final EntityManager em;

    public JpaTestSupport() {
        this.emf = Persistence.createEntityManagerFactory("my-pu");
        this.em = emf.createEntityManager();
    }

    public EntityManager entityManager() {
        return em;
    }

    @Override
    public void close() {
        if (em.isOpen()) em.close();
        if (emf.isOpen()) emf.close();
    }
}