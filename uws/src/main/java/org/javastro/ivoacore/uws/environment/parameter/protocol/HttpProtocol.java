
package org.javastro.ivoacore.uws.environment.parameter.protocol;

import org.javastro.ivoacore.common.security.SecurityGuard;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

/** Protocol implementation for http:/
 * @todo replace with more robust implementation based on commons HttpClient.
 * @author Noel Winstanley nw@jb.man.ac.uk 16-Jun-2004
 *
 */

public class HttpProtocol implements Protocol{
    /** Construct a new HttpProtocol
     * 
     */
    public HttpProtocol() {
        super();
    }
    /**
     * @see org.javastro.ivoacore.uws.parameter.protocol.Protocol#getProtocolName()
     */
    public String getProtocolName() {
        return "http";
    }
    /**
     * @see org.javastro.ivoacore.uws.parameter.protocol.Protocol#createIndirectValue(URI, SecurityGuard)
     */
    public ExternalValue createIndirectValue(final URI reference, SecurityGuard secGuard) throws InaccessibleExternalValueException {
        return new ExternalValue() {

            public InputStream read() throws InaccessibleExternalValueException {
                try {
                return reference.toURL().openStream();
                } catch (IOException e) {
                    throw new InaccessibleExternalValueException(reference.toString(),e );
                }
            }

            public OutputStream write() throws InaccessibleExternalValueException {              
              try {
                return reference.toURL().openConnection().getOutputStream();
              } catch (IOException e) {
                    throw new InaccessibleExternalValueException(reference.toString(),e );
                }
            }
        };
    }

}


