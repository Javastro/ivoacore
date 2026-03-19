package org.javastro.ivoacore.tap;
/*
 * Created on 03/05/2023 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */


import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.SharedCacheMode;
import jakarta.persistence.ValidationMode;
import jakarta.persistence.spi.ClassTransformer;
import jakarta.persistence.spi.PersistenceUnitInfo;
import jakarta.persistence.spi.PersistenceUnitTransactionType;
import org.h2.jdbcx.JdbcDataSource;
import org.hibernate.Session;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.hibernate.jpa.boot.internal.EntityManagerFactoryBuilderImpl;
import org.hibernate.jpa.boot.internal.PersistenceUnitInfoDescriptor;

import javax.sql.DataSource;
import java.net.URL;
import java.sql.PreparedStatement;
import java.util.*;

/**
 * Base Class for doing database tests.
 */
public  class AbstractBaseDBTest {


    /**
     * Create an Entity manager for a memory-based test database;
     * @param puname the persistence unit name of the JPA DB.
     * @param classNames the list of classes managed by the persistence unit.
     * @return the EntityManager for the database.
     */
    protected static EntityManager setupH2Db(String puname, List<String> classNames) {


        PersistenceUnitInfo persistenceUnitInfo = new HibernatePersistenceUnitInfo(puname, classNames);
        Map<String, Object> configuration = new HashMap<>();
        return new EntityManagerFactoryBuilderImpl(
              new PersistenceUnitInfoDescriptor(persistenceUnitInfo), configuration)
              .build().createEntityManager();
    }


    /**
     * Write the contents of the database to a file.
     * @param em the entity manager for the database.
     * @param filename The name of the file to write the DDL to.
     */
    protected void dumpDbData(EntityManager em, String filename) {
        //IMPL hibernate specific way of getting connection... generally dirty, see  https://stackoverflow.com/questions/3493495/getting-database-connection-in-pure-jpa-setup
            Session sess = em.unwrap(Session.class);
            sess.doWork(conn -> {
                PreparedStatement ps = conn.prepareStatement("SCRIPT TO ?"); //IMPL this is H2db specific
                ps.setString(1, filename);
                ps.execute();
            });
    }

    /**
     * set the name of the file to which the dbDump is written. The default is null so that no file is written.
     * @return the filename.
     */
    protected String setDbDumpFile() {
        return  null;
    }


    private static class HibernatePersistenceUnitInfo implements PersistenceUnitInfo {

        public static String JPA_VERSION = "3.1";
        private String persistenceUnitName;
        private PersistenceUnitTransactionType transactionType
              = PersistenceUnitTransactionType.RESOURCE_LOCAL;
        private List<String> managedClassNames;
        private List<String> mappingFileNames = new ArrayList<>();
        private Properties properties;
        private DataSource jtaDataSource;
        private DataSource nonjtaDataSource;
        private List<ClassTransformer> transformers = new ArrayList<>();

        public HibernatePersistenceUnitInfo(
              String persistenceUnitName, List<String> managedClassNames) {
            this.persistenceUnitName = persistenceUnitName;
            this.managedClassNames = managedClassNames;
            this.properties = new Properties();
            //derby
            //      properties.put("jakarta.persistence.jdbc.url", "jdbc:derby:memory:"+puname+";create=true");//IMPL differenrt DB for each PU to stop interactions
            //        properties.put(PersistenceUnitProperties.JDBC_URL, "jdbc:derby:emerlindb;create=true;traceFile=derbytrace.out;traceLevel=-1;traceDirectory=/tmp");
            //      properties.put("jakarta.persistence.jdbc.driver", "org.apache.derby.jdbc.EmbeddedDriver");
            // properties.put(PersistenceUnitProperties.TARGET_DATABASE, "org.eclipse.persistence.platform.database.DerbyPlatform");

            //        //h2
            properties.put("jakarta.persistence.jdbc.url", "jdbc:h2:mem:"+persistenceUnitName+";DB_CLOSE_DELAY=-1");//IMPL differenrt DB for each PU to stop interactions
            properties.put("jakarta.persistence.jdbc.driver", "org.h2.Driver");
            properties.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
            //        properties.put(PersistenceUnitProperties.TARGET_DATABASE, "org.eclipse.persistence.platform.database.H2Platform");
            //
            //        //hsqldb
            //        properties.put(PersistenceUnitProperties.JDBC_URL, "jdbc:hsqldb:mem:"+puname+";");//IMPL differenrt DB for each PU to stop interactions
            //        properties.put(PersistenceUnitProperties.JDBC_DRIVER, "org.hsqldb.jdbcDriver");
            //        properties.put(PersistenceUnitProperties.TARGET_DATABASE, "org.eclipse.persistence.platform.database.HSQLPlatform");


            // properties.put(PersistenceUnitProperties.DDL_GENERATION_MODE, PersistenceUnitProperties.DDL_BOTH_GENERATION);
            properties.put("jakarta.persistence.schema-generation.scripts.create-target", "test.sql");
            properties.put("jakarta.persistence.schema-generation.scripts.drop-target", "test-drop.sql");
            properties.put("hibernate.hbm2ddl.schema-generation.script.append", "false");
            properties.put("jakarta.persistence.create-database-schemas", "true");

            properties.put("jakarta.persistence.schema-generation.create-source", "metadata");
            properties.put("jakarta.persistence.schema-generation.database.action", "drop-and-create");
            properties.put("jakarta.persistence.schema-generation.scripts.action", "drop-and-create");
            properties.put("jakarta.persistence.jdbc.user", "");
            //        properties.put(PersistenceUnitProperties.CACHE_SHARED_, "false");

        }

        /**
         * {@inheritDoc}
         * overrides @see jakarta.persistence.spi.PersistenceUnitInfo#getPersistenceUnitName()
         */
        @Override
        public String getPersistenceUnitName() {
            return persistenceUnitName;
        }

        /**
         * {@inheritDoc}
         * overrides @see jakarta.persistence.spi.PersistenceUnitInfo#getPersistenceProviderClassName()
         */
        @Override
        public String getPersistenceProviderClassName() {
            return "org.hibernate.jpa.HibernatePersistenceProvider";
        }

        @Override
        public String getScopeAnnotationName() {
            return "";
        }

        @Override
        public List<String> getQualifierAnnotationNames() {
            return Collections.emptyList();
        }

        /**
         * {@inheritDoc}
         * overrides @see jakarta.persistence.spi.PersistenceUnitInfo#getTransactionType()
         */
        @Override
        public PersistenceUnitTransactionType getTransactionType() {
            return  transactionType;     
        }

        /**
         * {@inheritDoc}
         * overrides @see jakarta.persistence.spi.PersistenceUnitInfo#getJtaDataSource()
         */
        @Override
        public DataSource getJtaDataSource() {
           return null;
            
        }

        /**
         * {@inheritDoc}
         * overrides @see jakarta.persistence.spi.PersistenceUnitInfo#getNonJtaDataSource()
         */
        @Override
        public DataSource getNonJtaDataSource() {
            return null;
        }

        /**
         * {@inheritDoc}
         * overrides @see jakarta.persistence.spi.PersistenceUnitInfo#getMappingFileNames()
         */
        @Override
        public List<String> getMappingFileNames() {
            return mappingFileNames;
        }

        /**
         * {@inheritDoc}
         * overrides @see jakarta.persistence.spi.PersistenceUnitInfo#getJarFileUrls()
         */
        @Override
        public List<URL> getJarFileUrls() {
           return Collections.emptyList();
            
        }

        /**
         * {@inheritDoc}
         * overrides @see jakarta.persistence.spi.PersistenceUnitInfo#getPersistenceUnitRootUrl()
         */
        @Override
        public URL getPersistenceUnitRootUrl() {
           return null;
        }

        /**
         * {@inheritDoc}
         * overrides @see jakarta.persistence.spi.PersistenceUnitInfo#getManagedClassNames()
         */
        @Override
        public List<String> getManagedClassNames() {
            return managedClassNames;            
        }

        /**
         * {@inheritDoc}
         * overrides @see jakarta.persistence.spi.PersistenceUnitInfo#excludeUnlistedClasses()
         */
        @Override
        public boolean excludeUnlistedClasses() {
            return true;           
        }

        /**
         * {@inheritDoc}
         * overrides @see jakarta.persistence.spi.PersistenceUnitInfo#getSharedCacheMode()
         */
        @Override
        public SharedCacheMode getSharedCacheMode() {
            return SharedCacheMode.ALL;//IMPL is this good?
            
        }
        /**
         * {@inheritDoc}
         * overrides @see jakarta.persistence.spi.PersistenceUnitInfo#getValidationMode()
         */
        @Override
        public ValidationMode getValidationMode() {
            return ValidationMode.AUTO;
                
        }

        /**
         * {@inheritDoc}
         * overrides @see jakarta.persistence.spi.PersistenceUnitInfo#getProperties()
         */
        @Override
        public Properties getProperties() {
            return properties;
            
        }

        /**
         * {@inheritDoc}
         * overrides @see jakarta.persistence.spi.PersistenceUnitInfo#getPersistenceXMLSchemaVersion()
         */
        @Override
        public String getPersistenceXMLSchemaVersion() {
            return JPA_VERSION;
        }

        /**
         * {@inheritDoc}
         * overrides @see jakarta.persistence.spi.PersistenceUnitInfo#getClassLoader()
         */
        @Override
        public ClassLoader getClassLoader() {
          return Thread.currentThread().getContextClassLoader(); //IMPL ??
        }

        /**
         * {@inheritDoc}
         * overrides @see jakarta.persistence.spi.PersistenceUnitInfo#addTransformer(jakarta.persistence.spi.ClassTransformer)
         */
        @Override
        public void addTransformer(ClassTransformer transformer) {
            // TODO Auto-generated method stub
            throw new  UnsupportedOperationException("PersistenceUnitInfo.addTransformer() not implemented");
            
        }

        /**
         * {@inheritDoc}
         * overrides @see jakarta.persistence.spi.PersistenceUnitInfo#getNewTempClassLoader()
         */
        @Override
        public ClassLoader getNewTempClassLoader() {
            return null;// return Thread.currentThread().getContextClassLoader(); //IMPL or null

        }


   }

    /**
     * Create a DataSource for an in-memory H2 database and set up the JPA/Hibernate configuration to use it.
     * @return
     */
   public static DataSource createDataSource() {
       JdbcDataSource dataSource = new JdbcDataSource();
       dataSource.setURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
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
