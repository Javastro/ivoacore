

package org.javastro.ivoacore.uws.environment;

import org.javastro.ivoacore.uws.JobManager;

/**
 * Defines aspects of how an {@link JobManager} behaves.
 * @author Paul Harrison (paul.harrison@manchester.ac.uk) 23 Apr 2008
 * @version $Name:  $
 * @since VOTech Stage 7
 */
public interface ExecutionPolicy {
    
    /**
     * Returns the maximum run time for applications in seconds. A value of 0 implies unlimited run times;
     * @return the maximum run time in seconds, or 0 for unlimited.
     */
    int getMaxRunTime();

    
    /**
     * Returns the period of the checks for overrunning applications.
     * How often the system checks for jobs that have exceeded their runtime limit.
     * @return the period in seconds
     */
    int getKillPeriod();
    
    
    /**
     * Returns the default lifetime for a job record in seconds.
     * @return the default lifetime in seconds.
     */
    int getDefaultLifetime();
    
    /**
     * Returns the period of the checks for jobs that should be destroyed.
     * How often the system checks for jobs whose destruction time has passed and deletes them.
     * @return the period in seconds
     */
    int getDestroyPeriod();


    /**
     * The maximum number of jobs that can run concurrently
     * @return the maximum number of concurrently running jobs.
     */
    int getMaxConcurrent();
}


