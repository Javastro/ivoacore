

package org.javastro.ivoacore.uws.environment;

/**
 * Defines aspects of how an {@link ExecutionController} behaves.
 * @author Paul Harrison (paul.harrison@manchester.ac.uk) 23 Apr 2008
 * @version $Name:  $
 * @since VOTech Stage 7
 */
public interface ExecutionPolicy {
    
    /**
     * Returns the maximum run time for applications in seconds. A value of 0 implies unlimited run times;
     * @return
     */
    int getMaxRunTime();

    
    /**
     * Returns the period of the checks for overruning applications.
     * @return the period in seconds
     */
    int getKillPeriod();
    
    
    /**
     * Returns the default lifetime for a job record in seconds.
     * @return
     */
    int getDefaultLifetime();
    
    /**
     * Returns the period of the checks for jobs that should be destroyed.
     * @return the period in seconds
     */
    int getDestroyPeriod();


    /**
     * The maximum number of jobs that can run concurrently
     * @return
     */
    int getMaxConcurrent();
}


