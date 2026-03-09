

package org.javastro.ivoacore.uws.environment.parameter;

/**
 * Exception thrown when parameter adapter used incorrectly. Typically this is when an attempt is made to write back to an input parameter.
 * @author Paul Harrison (paul.harrison@manchester.ac.uk) 16 Jul 2009
 * @version $Name:  $
 * @since AIDA Stage 1
 */
public class IllegalParameterUse extends ParameterAdapterException {

    /**
     * Constructs an IllegalParameterUse with the given message.
     * @param message the detail message.
     */
    public IllegalParameterUse(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    /**
     * Constructs an IllegalParameterUse with the given message and cause.
     * @param message the detail message.
     * @param cause the cause of this exception.
     */
    public IllegalParameterUse(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

}



