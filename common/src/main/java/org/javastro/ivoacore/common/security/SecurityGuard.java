package org.javastro.ivoacore.common.security;


/*
 * Created on 14/08/2025 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

/**
 * A general interface for obtaining authentication and authorization.
 */
public interface SecurityGuard {
   boolean isSignedOn();

   String getX500Principal();
}
