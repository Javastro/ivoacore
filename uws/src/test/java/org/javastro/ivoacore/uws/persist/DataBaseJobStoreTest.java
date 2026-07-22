package org.javastro.ivoacore.uws.persist;


import com.fasterxml.jackson.databind.jsontype.NamedType;
import org.junit.jupiter.api.*;

import java.util.List;

public class DataBaseJobStoreTest extends BaseStorageTest {

   private static JpaTestSupport jpa;

   @BeforeAll
   static void setJobStore(){
      jpa = new JpaTestSupport();
      jobStore = new DatabaseJobStore(jpa.entityManager(), List.of(new NamedType(MockJob.Specification.class, MockJob.JOB_TYPE)));
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
