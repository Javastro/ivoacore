package org.javastro.ivoacore.uws;

import org.javastro.ivoa.entities.uws.ExecutionPhase;
import org.javastro.ivoa.entities.uws.Jobs;
import org.javastro.ivoa.entities.uws.ShortJobDescription;
import org.javastro.ivoacore.uws.environment.DefaultEnvironmentFactory;
import org.javastro.ivoacore.uws.environment.DefaultExecutionPolicy;
import org.javastro.ivoacore.uws.environment.ExecutionPolicy;
import org.javastro.ivoacore.uws.persist.MemoryBasedJobStore;
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
import static org.junit.jupiter.api.Assertions.assertNull;
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
      assertEquals(job.getExecutionPolicy().getDefaultRunTime(), initial.getExecutionDuration());

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
         manager.setExecutionDuration(policyLimitedJob.getID(), 3L);

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

   /**
    * Tests that the execution duration of a job cannot exceed the maximum runtime specified
    * by the configured execution policy.
    *
    * The test performs the following actions:
    * - Creates a temporary directory to manage job-related files during the test execution.
    * - Defines a custom {@code ExecutionPolicy} with a maximum runtime of 3 seconds and a
    *   kill period of 1 second.
    * - Creates a {@code JobManager} initialized with a {@code JobFactoryAggregator} that uses
    *   the custom execution policy.
    * - Adds a job specification via the {@code SimpleLambdaJob} factory to the job manager and
    *   creates a job.
    * - Attempts to set an execution duration beyond the policy maximum (4 seconds) and verifies
    *   that an exception is thrown.
    * - Asserts that the maximum runtime retrieved from the job's execution policy matches the
    *   value defined in the custom policy (3 seconds).
    *
    * Assertions:
    * - Attempting to set an execution duration exceeding the policy maximum throws
    *   {@code UWSException}.
    * - The job's maximum runtime is correctly retrieved from the configured execution policy.
    *
    * @throws Exception if an unexpected error occurs during test execution
    */
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

   /**
    * Verifies that a job which has completed is not erroneously aborted by the execution monitor.
    * <p>
    * The test performs the following steps:
    * - Creates a temporary directory for managing job-related files.
    * - Configures a custom {@link ExecutionPolicy} with a short monitoring period for detecting overrunning jobs.
    * - Initialises a {@link JobManager} with a job store and a job factory producing simple lambda jobs.
    * - Creates and runs a job, ensuring it reaches the {@code COMPLETED} execution phase within a timeout period.
    * - Waits for a subsequent monitor tick to ensure the monitor runs after job completion.
    * - Confirms through assertions that the job remains in the {@code COMPLETED} phase and is not erroneously moved
    *   to another phase or aborted by the execution monitor.
    * <p>
    * Assertions:
    * - The job reaches the {@code COMPLETED} execution phase within the specified timeframe.
    * - The execution monitor does not interfere with the job after it transitions to the {@code COMPLETED} phase.
    *
    * @throws Exception if an unexpected error occurs during test execution
    */
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

   /**
    * The job manager deletes tests that expired completed jobs periodically.
    * <p>
    * The test:
    * - Creates a temporary directory for managing job environment files during execution.
    * - Configures a custom {@link ExecutionPolicy} with short check intervals for killing and destroying jobs.
    * - Initialises a {@link JobManager} with a job store and a factory producing simple lambda jobs.
    * - Creates and runs a job, verifying that it reaches the completed phase within a specified timeframe.
    * - Sets the destruction time for the completed job to be in the past.
    * - Verifies that the completed job remains in the store until its destruction time and is eventually deleted
    *   by the periodic destruction check.
    * <p>
    * Assertions ensure that:
    * - Completed jobs remain available before their destruction time.
    * - Destroyed jobs are correctly removed from the job store after the destruction period elapses.
    *
    * @throws Exception if an unexpected error occurs during the test execution
    */
   @Test
   void expiredCompletedJobsAreDeletedPeriodically() throws Exception {
      File tmpdir = Files.createTempDirectory("jobDestructionTest").toFile();
      ExecutionPolicy policy = new DefaultExecutionPolicy() {
         @Override
         public int getKillPeriod() {
            return 1;
         }

         @Override
         public int getDestroyPeriod() {
            return 1;
         }
      };
      JobFactoryAggregator agg = new JobFactoryAggregator();
      agg.addFactory(new SimpleLambdaJob.JobFactory(
              input -> input, new DefaultEnvironmentFactory(tmpdir), policy));
      JobStore store = new MemoryBasedJobStore();

      try (JobManager manager = new JobManager(agg, store, policy)) {
         BaseUWSJob job = manager.createJob(
                 new SimpleLambdaJob.Specification("retained", "completed-retention"));
         manager.runJob(job.getID());

         long completionDeadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
         while (job.getExecutionPhase() != ExecutionPhase.COMPLETED
                 && System.nanoTime() < completionDeadline) {
            Thread.sleep(20);
         }
         assertEquals(ExecutionPhase.COMPLETED, job.getExecutionPhase());
         assertNotNull(store.retrieve(job.getID()),
                 "Completed jobs should remain available before destruction time");

         manager.setDestruction(job.getID(), ZonedDateTime.now(ZoneId.of("UTC")).minusSeconds(1));

         long deletionDeadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
         while (store.retrieve(job.getID()) != null && System.nanoTime() < deletionDeadline) {
            Thread.sleep(20);
         }
         assertNull(store.retrieve(job.getID()),
                 "The periodic destruction check should remove expired completed jobs");
      }
   }

   /**
    * Verifies that a failed worker's state is correctly stored in the job store after it terminates.
    *
    * <ul>
    * <li>A temporary directory is created for managing job environment files during the test.</li>
    * <li>A custom {@code ExecutionPolicy} is defined with short kill and destroy periods to accelerate the test.</li>
    * <li>A custom {@code MemoryBasedJobStore} is used to intercept and verify the storage of a job in the terminal error state.</li>
    * <li>A {@code JobManager} instance is configured with a {@code SimpleLambdaJob} that simulates failure.</li>
    * <li>After executing the job, the test ensures the failed job state is stored in the job store, its execution phase is set to {@code ERROR},
    *     and the error message is populated.</li>
    * <li>The test validates that this process completes within a predefined timeout period.</li>
    * </ul>
    *
    * @throws Exception if an unexpected error occurs during the test execution
    */
   @Test
   void failedWorkerStateIsStoredAfterItTerminates() throws Exception {
      File tmpdir = Files.createTempDirectory("failedJobPersistenceTest").toFile();
      ExecutionPolicy policy = new DefaultExecutionPolicy() {
         @Override
         public int getKillPeriod() {
            return 1;
         }

         @Override
         public int getDestroyPeriod() {
            return 1;
         }
      };
      CountDownLatch failureStored = new CountDownLatch(1);
      MemoryBasedJobStore store = new MemoryBasedJobStore() {
         @Override
         public void store(BaseUWSJob job) {
            super.store(job);
            if (job.getExecutionPhase() == ExecutionPhase.ERROR) {
               failureStored.countDown();
            }
         }
      };
      JobFactoryAggregator agg = new JobFactoryAggregator();
      agg.addFactory(new SimpleLambdaJob.JobFactory(
              input -> {
                 throw new IllegalStateException("expected test failure");
              }, new DefaultEnvironmentFactory(tmpdir), policy));

      try (JobManager manager = new JobManager(agg, store, policy)) {
         BaseUWSJob job = manager.createJob(
                 new SimpleLambdaJob.Specification("failure", "failed-persistence"));
         manager.runJob(job.getID());

         assertTrue(failureStored.await(5, TimeUnit.SECONDS),
                 "The terminal job state should be written after worker termination");
         assertEquals(ExecutionPhase.ERROR, store.retrieve(job.getID()).getExecutionPhase());
         assertNotNull(store.retrieve(job.getID()).getErrorMessage());
      }
   }

   /**
    * Extracts a list of job IDs from the provided {@code Jobs} object.
    *
    * @param jobs the {@code Jobs} object containing the job details
    * @return a list of job IDs as {@code String}s extracted from the job references
    */
   private static List<String> extractIds(Jobs jobs) {
       return  jobs.getJobreves().stream().map(ShortJobDescription::getId).collect(Collectors.toList());
   }



}