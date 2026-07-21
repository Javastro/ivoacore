package org.javastro.ivoacore.uws.persist;

import org.junit.jupiter.api.*;

class MemoryBasedJobStoreTest extends BaseStorageTest {

    @BeforeAll
  static void setJobStore(){
      jobStore = new MemoryBasedJobStore();
  }

   @Override
   void startTransaction() {
     //do nothing
   }

   @Override
   void commitTransaction() {
      //do nothing
   }
}