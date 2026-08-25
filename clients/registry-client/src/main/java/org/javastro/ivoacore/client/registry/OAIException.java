package org.javastro.ivoacore.client.registry;


/*
 * Created on 21/08/2025 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */

/**
 * An exception class for handling errors related to OAI-PMH operations.
 * This class extends the standard Java Exception class and provides constructors
 * for creating exceptions with a message and an optional cause.
 *
 * @author Paul Harrison (paul.harrison@manchester.ac.uk) */
public class OAIException extends Exception {
   /**
    * Constructs a new OAIException with the specified detail message.
    *
    * @param message the detail message
    */
   public OAIException(String message) {
      super(message);
   }
   /**
    * Constructs a new OAIException with the specified detail message and cause.
    *
    * @param message the detail message
    * @param cause   the cause of the exception
    */
   public OAIException(String message, Throwable cause) {
      super(message, cause);
   }
}
