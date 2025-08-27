package org.javastro.ivoacore.client.registry;


/*
 * Created on 21/08/2025 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

public class OAIException extends Exception {
   public OAIException(String message) {
      super(message);
   }
   public OAIException(String message, Throwable cause) {
      super(message, cause);
   }
}
