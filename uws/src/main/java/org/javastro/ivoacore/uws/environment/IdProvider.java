package org.javastro.ivoacore.uws.environment;


/*
 * Created on 04/09/2025 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

public interface IdProvider {
   /**
    * Generate an identifier for a particular job instance.
    * @return
    */
   String generateId();
}
