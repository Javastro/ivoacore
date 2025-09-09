
package org.javastro.ivoacore.uws.environment.parameter.protocol;

import org.javastro.ivoacore.common.security.SecurityGuard;

import java.net.URI;

/** Factory interface for creating {@link org.javastro.ivoacore.uws.parameter.protocol.ExternalValue} instances.
 * @see org.javastro.ivoacore.uws.parameter.protocol.DefaultProtocolLibrary
 * @author Noel Winstanley nw@jb.man.ac.uk 16-Jun-2004
 *
 */
public interface Protocol {
    /** access the name of the protocol this object provides support for 
     * @return name of the protocl this factory can build instances for.*/
    String getProtocolName();
    /** create a new indirectParameterValue for the passed in URI 
     * @param reference the uri to build an instance for.
     * @param secGuard TODO
     * @return a handler for this uri.
     * @throws InaccessibleExternalValueException*/
    ExternalValue createIndirectValue(final URI reference, final SecurityGuard secGuard) throws InaccessibleExternalValueException;
}


