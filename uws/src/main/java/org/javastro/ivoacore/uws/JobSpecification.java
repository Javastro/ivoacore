package org.javastro.ivoacore.uws;


/*
 * Created on 02/09/2025 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

import org.javastro.ivoacore.uws.environment.execution.ParameterValue;

import java.util.List;

/**
 * defines the specification of the Job.
 */
public interface JobSpecification {

   /**
    * Returns the identifier of the job type.
    * @return the job type identifier string.
    */
   String jobTypeIdentifier();

   /**
    * Returns the Job Description Language (JDL) string for this job, if applicable.
    * @return the JDL string, or {@code null} if not applicable.
    */
   String getJDL();

   /**
    * Returns the list of parameter values for this job.
    * @return the list of {@link ParameterValue} objects.
    */
   List<ParameterValue> getParameters();

   /**
    * Returns the run ID associated with this job specification.
    * @return the run identifier string.
    */
   String getRunId();

}
