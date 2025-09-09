package org.javastro.ivoacore.tap;

import org.javastro.ivoa.entities.uws.ExecutionPhase;
import org.javastro.ivoacore.uws.*;
import org.javastro.ivoacore.uws.environment.DefaultExecutionEnvironment;
import org.javastro.ivoacore.uws.environment.DefaultExecutionPolicy;
import org.javastro.ivoacore.uws.persist.MemoryBasedJobStore;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

/*
 * Created on 09/09/2025 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

class TAPJobTest {

   static JobManager jobManager;
   @BeforeAll
   static void setup() throws IOException {
      File tmpdir = Files.createTempDirectory("managerTest").toFile();
      JobFactoryAggregator agg = new JobFactoryAggregator();
      //FIXME agg.addFactory(new TAPJob.JobFactory(ds));
      MemoryBasedJobStore store = new MemoryBasedJobStore();
      DefaultExecutionPolicy policy = new DefaultExecutionPolicy();
      jobManager = new JobManager(new DefaultExecutionEnvironment(tmpdir), agg, store, policy
      );
   }

   @Test
   public void runJob() throws UWSException, InterruptedException {
      SimpleTAPJobSpecification spec = new SimpleTAPJobSpecification("select * from Table");
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
      job.getResults().forEach(r->System.out.println(r.getValue()));//TODO this needs to be an indirect result of course...

   }
}