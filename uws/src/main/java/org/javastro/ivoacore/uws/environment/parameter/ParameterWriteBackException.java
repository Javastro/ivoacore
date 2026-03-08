

package org.javastro.ivoacore.uws.environment.parameter;



/**
 * Indicates that there was a problem when trying to return the results of an application run.
 * @author Paul Harrison (pah@jb.man.ac.uk) 19-Apr-2004
 * @version $Name:  $
 * @since iteration5
 */
public class ParameterWriteBackException extends ParameterAdapterException {

   /**
    * Constructs a ParameterWriteBackException with the given message.
    * @param message the detail message.
    */
   public ParameterWriteBackException(String message) {
      super(message);
   }

   /**
    * Constructs a ParameterWriteBackException with the given message and cause.
    * @param message the detail message.
    * @param cause the cause of this exception.
    */
   public ParameterWriteBackException(String message, Throwable cause) {
      super(message, cause);
   }

}
