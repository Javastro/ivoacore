
package org.javastro.ivoacore.uws.environment.parameter.protocol;

import java.io.InputStream;
import java.io.OutputStream;

/** Interface for working with a value that is 'external' - ie probably not in this JVM. May be in local storage, may be remote.
 * <p>
 * Because of this vagueness, the interface provides the bare minimum for working with the external value.
 * @TODO rename the methods in this interface so that the imply opening a stream rather than reading and writing...
 * @TODO add method to copyto local file - to take advantage of any any secialization that the protocol might have to do this...then the parameter adapters should be rewritten to use this. (also exportFrom and getURL)
 * @TODO would be nice to have a string returning the "location" value also
 * @author Noel Winstanley nw@jb.man.ac.uk 16-Jun-2004
 * @author Paul Harrison (pah@jb.man.ac.uk)
 *
 */
public interface ExternalValue {
    /** access a stream to read the contents of the external value from 
     * @return an input stream containing the content of the external value
     * @throws InaccessibleExternalValueException*/
    InputStream read() throws InaccessibleExternalValueException;
    /** access a stream to write the contents of the external value to 
     * @return an output stream.
     * @throws InaccessibleExternalValueException*/
    OutputStream write() throws InaccessibleExternalValueException;
    
    /**
     * Copy the contents of the external value to a file. This method can take advantage of any specializations that the protocol might have.
    * @param f The file to which the contents of the externalValue should be copied.
    * @return file that it has copied to - this might not be the file requested for efficiencies' sake 
    * @throws InaccessibleExternalValueException
   
   File importTo(File f) throws InaccessibleExternalValueException;
    */
}


