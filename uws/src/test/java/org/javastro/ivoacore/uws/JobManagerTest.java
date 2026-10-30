package org.javastro.ivoacore.uws;

import org.javastro.ivoa.entities.uws.ExecutionPhase;
import org.javastro.ivoa.entities.uws.Jobs;
import org.javastro.ivoa.entities.uws.ShortJobDescription;
import org.javastro.ivoacore.uws.environment.DefaultEnvironmentFactory;
import org.javastro.ivoacore.uws.environment.DefaultExecutionPolicy;
import org.javastro.ivoacore.uws.environment.ExecutionPolicy;
import org.javastro.ivoacore.uws.persist.JobStore;
import org.javastro.ivoacore.uws.persist.RecordingJobStore;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
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
      agg.addFactory( new SimpleLambdaJob.JobFactory(s-> {
         try {
            Thread.sleep(2300);
         } catch (InterruptedException e) {
            throw new RuntimeException(e); //TODO review exception handling
         }
         return "hello "+s;},new DefaultEnvironmentFactory(tmpdir), new DefaultExecutionPolicy()));
      JobStore store = new RecordingJobStore();
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

   /**
    * Test that the execution policy defaults and job control changes are correctly exposed through the job detail.
    * This test creates a job, checks its initial destruction time and execution duration, then modifies these values
    * and verifies that the changes are reflected in the job detail.
    */
   @Test
   void executionPolicyDefaultsAndJobControlChangesAreExposed() throws UWSException {
      BaseUWSJob job = jobManager.createJob(new SimpleLambdaJob.Specification("control", "run-control"));
      org.javastro.ivoa.entities.uws.Job initial = jobManager.jobDetail(job.getID());
      ZonedDateTime expectedDestruction = job.getCreationTime().plusSeconds(job.getExecutionPolicy().getDefaultLifetime());

      assertEquals(expectedDestruction, initial.getDestruction());
      assertEquals(job.getExecutionPolicy().getMaxRunTime(), initial.getExecutionDuration());

      ZonedDateTime requestedDestruction = ZonedDateTime.now(ZoneId.of("UTC")).plusDays(3);
      assertEquals(requestedDestruction, jobManager.setDestruction(job.getID(), requestedDestruction));
      assertEquals(requestedDestruction, jobManager.jobDetail(job.getID()).getDestruction());

      assertEquals(120L, jobManager.setExecutionDuration(job.getID(), 120L));
      assertEquals(120, jobManager.jobDetail(job.getID()).getExecutionDuration());
   }

   /**
    * Test that the setExecutionDuration method correctly rejects invalid values, including negative durations,
    * durations exceeding the policy maximum, and attempts to set the duration for a non-existent job.
    */
   @Test
   void setExecutionDurationRejectsInvalidValues() throws UWSException {
      BaseUWSJob job = jobManager.createJob(new SimpleLambdaJob.Specification("control", "run-invalid-duration"));

      assertThrows(UWSException.class, () -> jobManager.setExecutionDuration(job.getID(), -1L));
      assertThrows(UWSException.class, () -> jobManager.setExecutionDuration(
            job.getID(), (long) job.getExecutionPolicy().getMaxRunTime() + 1));
      assertThrows(UWSException.class, () -> jobManager.setExecutionDuration("missing-job", 10L));
   }

   /**
    * Test that the execution monitor correctly interrupts and aborts jobs that exceed their allowed execution duration.
    * This test creates two jobs: one with a short execution duration and another with a policy-limited duration.
    * Both jobs are forced to sleep for a longer time than allowed, and we verify that they are interrupted and aborted.
    */

   @Test
   void executionMonitorInterruptsJobsPastTheirDuration() throws Exception {
      File tmpdir = Files.createTempDirectory("timeoutTest").toFile();

      ExecutionPolicy policy = new DefaultExecutionPolicy() {
         @Override
         public int getMaxRunTime() {
            return 3;
         }

         @Override
         public int getKillPeriod() {
            return 1;
         }
      };

      CountDownLatch started = new CountDownLatch(2);
      CountDownLatch interrupted = new CountDownLatch(2);
      AtomicInteger interruptCount = new AtomicInteger();

      JobFactoryAggregator agg = new JobFactoryAggregator();
      agg.addFactory(new SimpleLambdaJob.JobFactory(input -> {
         started.countDown();
         try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(30));
         } catch (InterruptedException e) {
            interruptCount.incrementAndGet();
            interrupted.countDown();
            Thread.currentThread().interrupt();
         }
         return input;
      }, new DefaultEnvironmentFactory(tmpdir), policy));

      try (JobManager manager = new JobManager(
              agg, new RecordingJobStore(), policy)) {

         BaseUWSJob shortJob = manager.createJob(
                 new SimpleLambdaJob.Specification(
                         "timeout", "short-timeout-test"));

         BaseUWSJob policyLimitedJob = manager.createJob(
                 new SimpleLambdaJob.Specification(
                         "timeout", "policy-timeout-test"));

         manager.setExecutionDuration(shortJob.getID(), 1L);
         manager.setExecutionDuration(policyLimitedJob.getID(), 0L);

         manager.runJob(shortJob.getID());
         manager.runJob(policyLimitedJob.getID());

         assertTrue(started.await(5, TimeUnit.SECONDS),
                 "Both jobs should start before the timeout is checked");

         assertTrue(interrupted.await(8, TimeUnit.SECONDS),
                 "Both jobs should be interrupted after exceeding their limits");

         assertEquals(2, interruptCount.get(),
                 "Each timed-out job should receive an interrupt");

         assertEquals(ExecutionPhase.ABORTED, shortJob.getExecutionPhase());
         assertEquals(ExecutionPhase.ABORTED,
                 policyLimitedJob.getExecutionPhase());
      }
   }


   @Test
   void executionDurationCannotExceedPolicyMaximum() throws Exception {
      File tmpdir = Files.createTempDirectory("durationPolicyTest").toFile();

      ExecutionPolicy policy = new DefaultExecutionPolicy() {
         @Override
         public int getMaxRunTime() {
            return 3;
         }

         @Override
         public int getKillPeriod() {
            return 1;
         }
      };

      JobFactoryAggregator agg = new JobFactoryAggregator();
      agg.addFactory(new SimpleLambdaJob.JobFactory(
              input -> input,
              new DefaultEnvironmentFactory(tmpdir),
              policy));

      try (JobManager manager = new JobManager(
              agg, new RecordingJobStore(), policy)) {

         BaseUWSJob job = manager.createJob(
                 new SimpleLambdaJob.Specification("duration", "policy-limit"));

         assertThrows(UWSException.class,
                 () -> manager.setExecutionDuration(job.getID(), 4L));

         assertEquals(3, job.getExecutionPolicy().getMaxRunTime());
      }
   }


   @Test
   void completedJobIsNotAbortedByExecutionMonitor() throws Exception {
      File tmpdir = Files.createTempDirectory("completedJobTest").toFile();

      ExecutionPolicy policy = new DefaultExecutionPolicy() {
         @Override
         public int getMaxRunTime() {
            return 30;
         }

         @Override
         public int getKillPeriod() {
            return 1;
         }
      };

      JobFactoryAggregator agg = new JobFactoryAggregator();
      agg.addFactory(new SimpleLambdaJob.JobFactory(
              input -> input,
              new DefaultEnvironmentFactory(tmpdir),
              policy));

      try (JobManager manager = new JobManager(
              agg, new RecordingJobStore(), policy)) {

         BaseUWSJob job = manager.createJob(
                 new SimpleLambdaJob.Specification("fast", "completed-job"));

         manager.runJob(job.getID());

         long deadline = System.nanoTime()
                 + TimeUnit.SECONDS.toNanos(5);

         while (job.getExecutionPhase() != ExecutionPhase.COMPLETED
                 && System.nanoTime() < deadline) {
            Thread.sleep(20);
         }

         assertEquals(ExecutionPhase.COMPLETED, job.getExecutionPhase());

         // Allow a subsequent monitor tick to occur.
         Thread.sleep(1100);

         assertEquals(ExecutionPhase.COMPLETED, job.getExecutionPhase());
      }
   }



   private static List<String> extractIds(Jobs jobs) {
       return  jobs.getJobreves().stream().map(ShortJobDescription::getId).collect(Collectors.toList());
   }



}