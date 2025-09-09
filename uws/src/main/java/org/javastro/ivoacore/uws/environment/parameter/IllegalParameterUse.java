

package org.javastro.ivoacore.uws.environment.parameter;

/**
 * Exception thrown when parameter adapter used incorrectly. Typically this is when an attempt is made to write back to an input parameter.
 * @author Paul Harrison (paul.harrison@manchester.ac.uk) 16 Jul 2009
 * @version $Name:  $
 * @since AIDA Stage 1
 */
public class IllegalParameterUse extends ParameterAdapterException {

    public IllegalParameterUse(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    public IllegalParameterUse(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

}



