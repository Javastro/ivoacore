
package org.javastro.ivoacore.uws.environment.parameter.protocol;

import org.javastro.ivoacore.common.security.SecurityGuard;

import java.io.*;
import java.net.URI;

/** Protocol implementation for file:/
 * @author Noel Winstanley nw@jb.man.ac.uk 16-Jun-2004
 *
 */

public class FileProtocol implements Protocol {

    /**
     * @see org.javastro.ivoacore.uws.environment.parameter.protocol.Protocol#getProtocolName()
     */
    public String getProtocolName() {
        return "file";
    }

    /**
     * @see org.javastro.ivoacore.uws.environment.parameter.protocol.Protocol#createIndirectValue(URI, SecurityGuard)
     */
    public ExternalValue createIndirectValue(final URI reference, SecurityGuard secGuard) throws InaccessibleExternalValueException {
        final File f = new File(reference);
       return new ExternalValue() {

           public InputStream read() throws InaccessibleExternalValueException {
               try {
               return new FileInputStream(f);
               } catch (IOException e) {
                   throw new InaccessibleExternalValueException(reference.toString(),e );
               }
           }

           public OutputStream write() throws InaccessibleExternalValueException {              
             try {
               return new FileOutputStream(f);
             } catch (IOException e) {
                   throw new InaccessibleExternalValueException(reference.toString(),e );
               }
           }
       };
   }


}


