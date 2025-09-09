
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
    /** add a protocol to the set supported by this library */
    public void addProtocol(Protocol p) {
        map.put(p.getProtocolName().toLowerCase(),p);
        
    }
    
    
    /** Construct a new DefaultIndirectionProtocolLibrary
     * n.b. this will not work with picocontainer now
     */
    public DefaultProtocolLibrary(Protocol[]protocols) {
        super();
        this.map = new HashMap<>();
       for (Protocol protocol : protocols) {
          addProtocol(protocol);
       }
    }
    protected final Map<String,Protocol>
    map;
    /**
     * @param secGuard 
     *
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
     * @param secGuard 
     *
     */
    public ExternalValue getExternalValue(URI reference, SecurityGuard secGuard) throws InaccessibleExternalValueException, UnrecognizedProtocolException {
        Protocol p = (Protocol) map.get(reference.getScheme());
        if (p == null) {
            throw new UnrecognizedProtocolException(reference.toString());
        }
        return p.createIndirectValue(reference, secGuard);
    }
    


    /**
     * @param secGuard
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


