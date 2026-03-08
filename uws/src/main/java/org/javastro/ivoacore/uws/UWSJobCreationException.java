package org.javastro.ivoacore.uws;




/**
 * Exception thrown when a UWS job cannot be created.
 */
public class UWSJobCreationException extends UWSException {
   /**
    * Constructs a UWSJobCreationException with the given message.
    * @param message the detail message.
    */
   public UWSJobCreationException(String message) {
      super(message);
   }
}
