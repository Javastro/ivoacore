package org.javastro.ivoacore.uws.persist;


import com.fasterxml.jackson.databind.jsontype.NamedType;
import org.junit.jupiter.api.*;

public class DataBaseJobStoreTest extends BaseStorageTest {

   private static JpaTestSupport jpa;

   @BeforeAll
   static void setJobStore(){
      jpa = new JpaTestSupport();
      jobStore = new DatabaseJobStore(jpa.entityManager(),new NamedType(MockJob.Specification.class, "mock"));
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
