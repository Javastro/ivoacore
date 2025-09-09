package org.javastro.ivoacore.uws;


public class UWSException extends Exception {
   public UWSException(String message) {
      super(message);
   }

   public UWSException(String message, Throwable cause) {
      super(message, cause);
   }
}
