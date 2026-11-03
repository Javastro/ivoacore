package org.javastro.ivoacore.tap;


import org.javastro.ivoacore.uws.environment.ExecutionPolicy;

/**
 * Extra parts of the execution policy for TAP
 *
 * @author Paul Harrison (paul.harrison@manchester.ac.uk) */
public interface TapExecutionPolicy extends ExecutionPolicy {
   /**
    * Returns the maximum number of records that can be returned in a query.
    * @return the maximum number of records.
    */
   long maxRec();
}
