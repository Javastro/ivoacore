package org.javastro.ivoacore.uws.persist;


import com.fasterxml.jackson.databind.jsontype.NamedType;
import org.javastro.ivoa.entities.stc.v1.OrbitType;
import org.javastro.ivoacore.uws.JobFactoryAggregator;
import org.junit.jupiter.api.*;

import java.util.List;

public class DataBaseJobStoreTest extends BaseStorageTest {

   private static JpaTestSupport jpa;

   @BeforeAll
   static void setJobStore(){
      jpa = new JpaTestSupport();
      JobFactoryAggregator agg = new JobFactoryAggregator();
      agg.addFactory(new MockJob.JobFactory());
      jobStore = new DatabaseJobStore(jpa.entityManager(), agg);
   }

   @Override
   void startTransaction() {
      jpa.entityManager().getTransaction().begin();

   }

   @Override
   void commitTransaction() {
      jpa.entityManager().getTransaction().commit();
   }

   @AfterAll
   static void testFinal(){
      jpa.dumpDbData(jpa.entityManager(),"uws_dump.sql");
   }
}
