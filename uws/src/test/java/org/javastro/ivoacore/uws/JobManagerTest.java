package org.javastro.ivoacore.uws;

import org.javastro.ivoa.entities.uws.ExecutionPhase;
import org.javastro.ivoacore.uws.environment.DefaultExecutionEnvironment;
import org.javastro.ivoacore.uws.environment.DefaultExecutionPolicy;
import org.javastro.ivoacore.uws.persist.MemoryBasedJobStore;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
            Thread.sleep(3000);
         } catch (InterruptedException e) {
            throw new RuntimeException(e); //TODO review exception handling
         }
         return "hello "+s;}));
      MemoryBasedJobStore store = new MemoryBasedJobStore();
      DefaultExecutionPolicy policy = new DefaultExecutionPolicy();
      jobManager = new JobManager(new DefaultExecutionEnvironment(tmpdir), agg, store, policy
      );
   }

   @Test
   public void runJob() throws UWSException, InterruptedException {
      SimpleLambdaJob.Specification spec = new SimpleLambdaJob.Specification("world", "myrefID");
      BaseUWSJob job = jobManager.createJob(spec);
      assertNotNull(job);
      String id = job.getID();
      assertNotNull(id);
      jobManager.runJob(id);
      //poll for completion - could do things with the
      while(jobManager.getPhase(id) != ExecutionPhase.COMPLETED) {
         System.out.println("Waiting for job "+id);
        Thread.sleep(1000);
      }
      assertFalse(job.getResults().isEmpty());
      job.getResults().forEach(r->System.out.println(r.getValue()));

   }

}