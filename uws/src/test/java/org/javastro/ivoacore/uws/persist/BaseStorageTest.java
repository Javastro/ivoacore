package org.javastro.ivoacore.uws.persist;


import org.javastro.ivoa.entities.uws.ExecutionPhase;
import org.javastro.ivoacore.uws.BaseUWSJob;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class BaseStorageTest {
   static JobStore jobStore;
   private static ZonedDateTime EARLY_DATE = ZonedDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC"));
   private static ZonedDateTime MID_DATE = ZonedDateTime.of(2024, 4, 1, 0, 0, 0, 0, ZoneId.of("UTC"));
   private static ZonedDateTime LATER_DATE = ZonedDateTime.of(2025, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC"));

   static MockJob job1 = new MockJob("job1", "run1", EARLY_DATE,ExecutionPhase.COMPLETED);
   static MockJob job2 = new MockJob("job2", "run2", LATER_DATE,ExecutionPhase.COMPLETED);
   static MockJob job3 = new MockJob("job3", "run3", EARLY_DATE,ExecutionPhase.PENDING);
   static MockJob job4 = new MockJob("job4", "run4", LATER_DATE,ExecutionPhase.PENDING);

   abstract void startTransaction();
   abstract void commitTransaction();
   @Test
   @Order(1)
   void store() {
       startTransaction();
       jobStore.store(job1);
       jobStore.store(job2);
       jobStore.store(job3);
       jobStore.store(job4);
       commitTransaction();

       assertEquals(4, jobStore.getAllIds().size());
       job4.setExecutionPhase(ExecutionPhase.ERROR);
       startTransaction();
       jobStore.store(job4);// this should not be a problem - it should update
       commitTransaction();
       assertEquals(4, jobStore.getAllIds().size());


   }

   @Test
   @Order(2)
   void retrieve() {
       BaseUWSJob ret = jobStore.retrieve("job1");
       assertNotNull(ret);
       assertEquals(ExecutionPhase.COMPLETED, ret.getExecutionPhase());
   }

   @Test
   @Order(3)
   void delete() {
       startTransaction();
       jobStore.delete("job1");
       commitTransaction();
       BaseUWSJob ret = jobStore.retrieve("job1");
       assertNull(ret);
   }

   @Test
   @Order(4)
   void getAllIds() {
       Set<String> ids = jobStore.getAllIds();
       assertTrue(ids.contains("job2"));
       assertEquals(3, ids.size());
   }

   @Test
   @Order(5)
   void getJobs() {
       List<BaseUWSJob> jobs = jobStore.getJobs(null, null, null);
       assertTrue(isJobInResult(jobs,job2.getID()));
       assertEquals(3, jobs.size());
       jobs = jobStore.getJobs(ExecutionPhase.COMPLETED, null, null);
       assertTrue(isJobInResult(jobs,job2.getID()));
       assertEquals(1, jobs.size());
       jobs = jobStore.getJobs(ExecutionPhase.COMPLETED, MID_DATE, null);
       assertTrue(isJobInResult(jobs,job2.getID()));
       assertEquals(1, jobs.size());
       jobs = jobStore.getJobs(null, MID_DATE, null);
       assertTrue(isJobInResult(jobs,job2.getID()));
       assertEquals(2, jobs.size());
       jobs = jobStore.getJobs(null, MID_DATE, 1);
       assertEquals(1, jobs.size());
       jobs = jobStore.getJobs(ExecutionPhase.PENDING, MID_DATE, null);
       assertEquals(0, jobs.size());

   }
   
   boolean isJobInResult(List<BaseUWSJob> jobs, String jobId) {
      return jobs.stream().anyMatch(job -> job.getID().equals(jobId));
   }
}
