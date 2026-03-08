package org.javastro.ivoacore.uws;


/**
 * Base exception for UWS (Universal Worker Service) errors.
 */
public class UWSException extends Exception {
   /**
    * Constructs a UWSException with the given message.
    * @param message the detail message.
    */
   public UWSException(String message) {
      super(message);
   }

   /**
    * Constructs a UWSException with the given message and cause.
    * @param message the detail message.
    * @param cause the cause of this exception.
    */
   public UWSException(String message, Throwable cause) {
      super(message, cause);
   }
}
