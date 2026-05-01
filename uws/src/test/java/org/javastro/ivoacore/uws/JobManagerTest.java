package org.javastro.ivoacore.uws;

import org.javastro.ivoa.entities.uws.ExecutionPhase;
import org.javastro.ivoa.entities.uws.Jobs;
import org.javastro.ivoacore.uws.environment.DefaultEnvironmentFactory;
import org.javastro.ivoacore.uws.environment.DefaultExecutionPolicy;
import org.javastro.ivoacore.uws.persist.MemoryBasedJobStore;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*
 * Created on 04/09/2025 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

class JobManagerTest {

   static JobManager jobManager;
   @BeforeAll
   static void setup() throws IOException {
      File tmpdir = Files.createTempDirectory("managerTest").toFile();
      JobFactoryAggregator agg = new JobFactoryAggregator();
      agg.addFactory(new SimpleLambdaJob.JobFactory(s-> {
         try {
            Thread.sleep(2300);
         } catch (InterruptedException e) {
            throw new RuntimeException(e); //TODO review exception handling
         }
         return "hello "+s;},new DefaultEnvironmentFactory(tmpdir)));
      MemoryBasedJobStore store = new MemoryBasedJobStore();
      DefaultExecutionPolicy policy = new DefaultExecutionPolicy();
      jobManager = new JobManager( agg, store, policy
      );
   }

   @Test
   public void performAction() throws UWSException, InterruptedException {
      SimpleLambdaJob.Specification spec = new SimpleLambdaJob.Specification("world", "myrefID");
      BaseUWSJob job = jobManager.createJob(spec);
      assertNotNull(job);
      String id = job.getID();
      assertNotNull(id);
      jobManager.runJob(id);
      //poll for completion - could do things with the
      while(jobManager.jobDetail(id).getPhase() != ExecutionPhase.COMPLETED) {
         System.out.println("Waiting for job "+id);
        Thread.sleep(500);
      }
      assertFalse(job.getResults().isEmpty());
      job.getResults().forEach(r->System.out.println(r.getValue()));

   }

   @Test
   void listJobsFiltersByPhase() throws UWSException, InterruptedException {
      BaseUWSJob completedJob = jobManager.createJob(new SimpleLambdaJob.Specification("phase-completed", "run-completed"));
      BaseUWSJob pendingJob = jobManager.createJob(new SimpleLambdaJob.Specification("phase-pending", "run-pending"));

      jobManager.runJob(completedJob.getID());
      while(jobManager.jobDetail(completedJob.getID()).getPhase() != ExecutionPhase.COMPLETED) {
         Thread.sleep(200);
      }

      Jobs completedOnly = jobManager.listJobs("COMPLETED", null, null);
      List<String> ids = extractIds(completedOnly);

      assertTrue(ids.contains(completedJob.getID()));
      assertFalse(ids.contains(pendingJob.getID()));
   }

   @Test
   void listJobsAppliesAfterAndLast() throws Exception {
      BaseUWSJob oldJob = jobManager.createJob(new SimpleLambdaJob.Specification("old", "run-old"));
      Thread.sleep(25);
      ZonedDateTime cutoff = ZonedDateTime.now(ZoneId.of("UTC"));
      Thread.sleep(25);
      BaseUWSJob middleJob = jobManager.createJob(new SimpleLambdaJob.Specification("middle", "run-middle"));
      Thread.sleep(25);
      BaseUWSJob newJob = jobManager.createJob(new SimpleLambdaJob.Specification("new", "run-new"));
      Thread.sleep(25);

      Jobs afterCutoff = jobManager.listJobs(null, cutoff, null);
      List<String> afterIds = extractIds(afterCutoff);
      assertFalse(afterIds.contains(oldJob.getID()));
      assertTrue(afterIds.contains(middleJob.getID()));
      assertTrue(afterIds.contains(newJob.getID()));

      Jobs lastTwoAfterCutoff = jobManager.listJobs(null, cutoff, 2);
      List<String> lastIds = extractIds(lastTwoAfterCutoff);
      assertEquals(List.of(middleJob.getID(), newJob.getID()), lastIds);
   }

   @Test
   void listJobsRejectsInvalidPhase() {
      assertThrows(UWSException.class, () -> jobManager.listJobs("NOT_A_PHASE", null, null));
   }

   private static List<String> extractIds(Jobs jobs) {

       return  jobs.getJobreves().stream().map(j->j.getId()).collect(Collectors.toList());
   }



}