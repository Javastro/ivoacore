package org.javastro.ivoacore.common.testing;


import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;

public abstract class AbstractBaseDSTest {

   /**
    * Create a DataSource for an in-memory H2 database and set up the JPA/Hibernate configuration to use it.
    * @return
    */
  public static DataSource createDataSource() {
      JdbcDataSource dataSource = new JdbcDataSource();
      dataSource.setURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL");
      dataSource.setUser("sa");
      dataSource.setPassword("");

      // Configure JPA/Hibernate properties
//       Map<String, Object> properties = new HashMap<>();
//       properties.put("jakarta.persistence.nonJtaDataSource", dataSource);
//       properties.put("hibernate.hbm2ddl.auto", "create-drop"); // Auto-create tables
//       properties.put("hibernate.show_sql", "true");


//       EntityManagerFactory emf = new HibernatePersistenceProvider()
//             .createEntityManagerFactory("my-test-unit", properties);
//
//       // Create the EntityManager
//       EntityManager em = emf.createEntityManager();

      return dataSource;

  }
}
