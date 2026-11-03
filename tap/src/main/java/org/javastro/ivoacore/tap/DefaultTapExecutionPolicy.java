package org.javastro.ivoacore.tap;


import org.javastro.ivoacore.uws.environment.DefaultExecutionPolicy;

/**
 * A default TAP execution policy that provides a maximum record limit for queries.
 * This class extends the DefaultExecutionPolicy and implements the TapExecutionPolicy interface, allowing it to be used in TAP job processing.
 *
 * @author Paul Harrison (paul.harrison@manchester.ac.uk) */
public class DefaultTapExecutionPolicy extends DefaultExecutionPolicy implements TapExecutionPolicy {

   private final long maxRec;

   /**
    * constructor that allows specification of the maximum record limit for queries.
    * @param maxRec the maximum number of records that can be returned by a query.
    */
   public DefaultTapExecutionPolicy(long maxRec) {

      this.maxRec = maxRec;
   }

   /**
    * constructor that sets a default maximum record limit of 10,000 for queries.
    */
   public DefaultTapExecutionPolicy() {
     this(10000L);
   }

   @Override
   public long maxRec() {
      return maxRec;
   }
}
