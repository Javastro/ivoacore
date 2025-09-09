
package org.javastro.ivoacore.uws.environment.parameter;

import org.javastro.ivoacore.uws.UWSException;

/** Some generic fault concerning parameter adapters
 * @author Noel Winstanley nw@jb.man.ac.uk 16-Jun-2004
 *
 */
public class ParameterAdapterException extends UWSException {
    /** Construct a new ParameterAdapterException
     * @param message
     */
    public ParameterAdapterException(String message) {
        super(message);
    }
    /** Construct a new ParameterAdapterException
     * @param message
     * @param cause
     */
    public ParameterAdapterException(String message, Throwable cause) {
        super(message, cause);
    }
}


