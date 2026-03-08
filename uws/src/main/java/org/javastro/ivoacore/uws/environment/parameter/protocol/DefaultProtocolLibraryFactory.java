
package org.javastro.ivoacore.uws.environment.parameter.protocol;

/** Factory object that will create a pre-configured DefaultProtocolLibrary, in which all known protocols are registered.
 * <b>NB: when implementing a new standard protocol, add it to this factory too </b>
 * @author Noel Winstanley nw@jb.man.ac.uk 22-Nov-2004
 *
 */
public class DefaultProtocolLibraryFactory {

    /** Construct a new DefaultProtocolLibraryFactory
     * 
     */
    public DefaultProtocolLibraryFactory() {
        super();
    }
    
    /**
     * Creates a {@link DefaultProtocolLibrary} pre-configured with all standard protocol implementations
     * (file, FTP, HTTP).
     * @return a new {@link DefaultProtocolLibrary} with standard protocols registered.
     */
    public DefaultProtocolLibrary createLibrary() {
        DefaultProtocolLibrary lib = new DefaultProtocolLibrary(new Protocol[]{
        new FileProtocol(),new FtpProtocol(),new HttpProtocol()});
        return lib;
    }

}


