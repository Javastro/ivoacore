
package org.javastro.ivoacore.uws.environment.parameter.protocol;


import org.javastro.ivoacore.uws.environment.parameter.ParameterAdapterException;

/** thrown when the value of an indirect parameter cannot be accessed - e.g. because the resource cannot be accessed.
 * @author Noel Winstanley nw@jb.man.ac.uk 16-Jun-2004
 *
 */
public class InaccessibleExternalValueException extends ParameterAdapterException {
    /** Construct a new InaccessibleIndirectParameterException
     * @param message
     */
    public InaccessibleExternalValueException(String message) {
        super(message);
    }
    /** Construct a new InaccessibleIndirectParameterException
     * @param message
     * @param cause
     */
    public InaccessibleExternalValueException(String message, Throwable cause) {
        super(message, cause);
    }
}


