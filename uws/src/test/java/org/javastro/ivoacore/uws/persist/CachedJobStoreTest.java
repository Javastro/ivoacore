package org.javastro.ivoacore.uws.persist;


import org.junit.jupiter.api.BeforeAll;

public class CachedJobStoreTest extends BaseStorageTest {


   @BeforeAll
   static void setJobStore(){

      jobStore = new CachedJobStore(new MemoryBasedJobStore());
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
