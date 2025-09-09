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

   String jobTypeIdentifier();

   String getJDL();

   List<ParameterValue> getParameters();

   String getRunId();

}
