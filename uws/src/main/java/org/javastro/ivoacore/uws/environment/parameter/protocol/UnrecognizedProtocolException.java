
package org.javastro.ivoacore.uws.environment.parameter.protocol;


import org.javastro.ivoacore.uws.environment.parameter.ParameterAdapterException;

/** Exception thrown when an indirect parameter is encountered whose resource is not a known protocol.
 * @author Noel Winstanley nw@jb.man.ac.uk 16-Jun-2004
 *
 */
public class UnrecognizedProtocolException extends ParameterAdapterException {
    /** Construct a new UnrecognizedIndirectParameterProtocolException
     * @param message
     */
    public UnrecognizedProtocolException(String message) {
        super(message);
    }
    /** Construct a new UnrecognizedIndirectParameterProtocolException
     * @param message
     * @param cause
     */
    public UnrecognizedProtocolException(String message, Throwable cause) {
        super(message, cause);
    }
}


