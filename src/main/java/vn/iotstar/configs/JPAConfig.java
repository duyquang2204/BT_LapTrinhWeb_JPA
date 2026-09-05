package vn.iotstar.configs;

import jakarta.persistence.*;

@PersistenceContext
public class JPAConfig {
	public static EntityManager getEntityManager() {
		EntityManagerFactory factory = Persistence.createEntityManagerFactory("jpa-hibernate-mysql");
		return factory.createEntityManager();
	}
}
