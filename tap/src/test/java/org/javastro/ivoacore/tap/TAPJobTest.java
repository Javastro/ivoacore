package org.javastro.ivoacore.tap;

import org.javastro.ivoa.entities.uws.ExecutionPhase;
import org.javastro.ivoacore.uws.*;
import org.javastro.ivoacore.uws.environment.DefaultEnvironmentFactory;
import org.javastro.ivoacore.uws.environment.DefaultExecutionPolicy;
import org.javastro.ivoacore.uws.persist.MemoryBasedJobStore;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/*
 * Created on 09/09/2025 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

/**
 *
 */
class TAPJobTest extends AbstractBaseDBTest{

   static JobManager jobManager;
   @BeforeAll
   static void setup() throws IOException, SQLException {
      File tmpdir = Files.createTempDirectory("managerTest").toFile();
      JobFactoryAggregator agg = new JobFactoryAggregator();
      DataSource ds = createDataSource();
      TestSchemaProvider schemaProvider = new TestSchemaProvider(false);
      schemaProvider.writeDataBaseDDL(ds.getConnection());
      schemaProvider.populateDataBase(ds.getConnection());

      agg.addFactory(new TAPJob.JobFactory(ds,schemaProvider,new DefaultEnvironmentFactory(tmpdir)));
      MemoryBasedJobStore store = new MemoryBasedJobStore();
      DefaultExecutionPolicy policy = new DefaultExecutionPolicy();
      jobManager = new JobManager( agg, store, policy);
   }

   @Test
   public void performAction() throws UWSException, InterruptedException {
      TAPJobSpecification spec = new TAPJobSpecification("select * from test_table");
      BaseUWSJob job = jobManager.createJob(spec);
      assertNotNull(job);
      String id = job.getID();
      assertNotNull(id);
      jobManager.runJob(id);
      //poll for completion - could do things with the
      int i = 0;
      while(jobManager.jobDetail(id).getPhase() != ExecutionPhase.COMPLETED && i++ < 3) {
         System.out.println("Waiting for job "+id);
         Thread.sleep(500);
      }
      assertTrue(jobManager.jobDetail(id).getPhase() == ExecutionPhase.COMPLETED);
      assertFalse(job.getResults().isEmpty());
      job.getResults().forEach(r->System.out.println(r.getValue()));//TODO this needs to be an indirect result of course...
      String votable = job.getResults().stream().filter(r -> r.getId().equals("result")).findFirst().orElseThrow(() -> new RuntimeException("No result with id 'result'")).getValue();
      //print content of file pointed to by votable result
      System.out.println("VOTable content:");
      try {
         Files.lines(new File(votable).toPath()).forEach(System.out::println);
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }
}