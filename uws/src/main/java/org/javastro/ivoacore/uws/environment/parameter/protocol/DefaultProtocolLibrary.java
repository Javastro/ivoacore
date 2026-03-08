
package org.javastro.ivoacore.uws.environment.parameter.protocol;

import org.javastro.ivoacore.common.security.SecurityGuard;
import org.javastro.ivoacore.uws.environment.execution.ParameterValue;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/** Default implementation of {@link org.javastro.ivoacore.uws.environment.parameter.protocol.ProtocolLibrary}
 * @author Noel Winstanley nw@jb.man.ac.uk 16-Jun-2004
 * @author Paul Harrison (paul.harrison@manchester.ac.uk) 2 Apr 2008
 *
 */
public class DefaultProtocolLibrary implements ProtocolLibrary{
    /** add a protocol to the set supported by this library
     * @param p the protocol to add.
     */
    public void addProtocol(Protocol p) {
        map.put(p.getProtocolName().toLowerCase(),p);
        
    }
    
    
    /** Construct a new DefaultIndirectionProtocolLibrary
     * n.b. this will not work with picocontainer now
     * @param protocols the array of protocols to register in this library.
     */
    public DefaultProtocolLibrary(Protocol[]protocols) {
        super();
        this.map = new HashMap<>();
       for (Protocol protocol : protocols) {
          addProtocol(protocol);
       }
    }
    /** The map of protocol name to protocol handler. */
    protected final Map<String,Protocol>
    map;
    /**
     * Builds an external value for a particular indirect parameter value.
     * @param pval the parameter value containing the reference URI.
     * @param secGuard the security guard for access control.
     * @return an external value for this parameter.
     * @throws InaccessibleExternalValueException if the external value cannot be accessed.
     * @throws UnrecognizedProtocolException if the protocol scheme is not recognized.
     */
    public ExternalValue getExternalValue(ParameterValue pval, SecurityGuard secGuard)
        throws InaccessibleExternalValueException, UnrecognizedProtocolException{
        URI reference;
        try {
            reference = new URI(pval.getValue());
        }
        catch (URISyntaxException e) {
            throw new UnrecognizedProtocolException(pval.getValue(),e);
        } 
        return getExternalValue(reference, secGuard);
    }
    /**
     * Builds an external value from a URI.
     * @param reference the URI to resolve.
     * @param secGuard the security guard for access control.
     * @return an external value for this URI.
     * @throws InaccessibleExternalValueException if the external value cannot be accessed.
     * @throws UnrecognizedProtocolException if the URI protocol / scheme is not recognized.
     */
    public ExternalValue getExternalValue(URI reference, SecurityGuard secGuard) throws InaccessibleExternalValueException, UnrecognizedProtocolException {
        Protocol p = (Protocol) map.get(reference.getScheme());
        if (p == null) {
            throw new UnrecognizedProtocolException(reference.toString());
        }
        return p.createIndirectValue(reference, secGuard);
    }
    


    /**
     * Builds an external value from a URI string.
     * @param location the string representation of the URI.
     * @param secGuard the security guard for access control.
     * @return an external value for this location.
     * @throws InaccessibleExternalValueException if the external value cannot be accessed.
     * @throws UnrecognizedProtocolException if the URI protocol / scheme is not recognized.
     * @throws URISyntaxException if the location string is not a valid URI.
     */
    public ExternalValue getExternalValue(String location, SecurityGuard secGuard) throws InaccessibleExternalValueException, UnrecognizedProtocolException, URISyntaxException {
        return getExternalValue(new URI(location), secGuard);
    }    
    /**
     * @see org.javastro.ivoacore.uws.environment.parameter.protocol.ProtocolLibrary#listSupportedProtocols()
     */
    public String[] listSupportedProtocols() {
        return (String[])map.keySet().toArray(new String[]{});
    }
    /**
     * @see org.javastro.ivoacore.uws.environment.parameter.protocol.ProtocolLibrary#isProtocolSupported(String)
     */
    public boolean isProtocolSupported(String protocol) {
        return map.containsKey(protocol.toLowerCase());
    }




}


