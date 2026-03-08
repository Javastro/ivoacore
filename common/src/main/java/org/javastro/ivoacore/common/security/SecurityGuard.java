package org.javastro.ivoacore.common.security;


/*
 * Created on 14/08/2025 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

/**
 * A general interface for obtaining authentication and authorization.
 */
public interface SecurityGuard {
   /**
    * Returns whether the user is currently signed on.
    * @return {@code true} if the user is signed on, {@code false} otherwise.
    */
   boolean isSignedOn();

   /**
    * Returns the X.500 principal name of the authenticated user.
    * @return the X.500 distinguished name, or {@code null} if not authenticated.
    */
   String getX500Principal();
}
