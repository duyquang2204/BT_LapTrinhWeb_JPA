package vn.iotstar.configs;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public final class JPAConfig {

    private static final String PERSISTENCE_UNIT =
            "jpa-hibernate-mysql";

    private static EntityManagerFactory factory;

    private JPAConfig() {
    }

    public static synchronized EntityManager getEntityManager() {
        if (factory == null || !factory.isOpen()) {
            factory = Persistence.createEntityManagerFactory(
                    PERSISTENCE_UNIT);
        }

        return factory.createEntityManager();
    }

    public static synchronized void shutdown() {
        if (factory != null && factory.isOpen()) {
            factory.close();
        }

        factory = null;
    }
}