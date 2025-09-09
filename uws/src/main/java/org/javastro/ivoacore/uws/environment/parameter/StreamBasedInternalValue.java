

package org.javastro.ivoacore.uws.environment.parameter;

import org.javastro.ivoacore.uws.UWSException;
import org.javastro.ivoacore.uws.environment.ExecutionEnvironment;
import org.javastro.ivoacore.uws.environment.execution.BinaryEncodings;
import org.javastro.ivoacore.uws.environment.execution.ParameterValue;
import org.javastro.ivoacore.uws.environment.parameter.protocol.ProtocolLibrary;
import org.javastro.ivoacore.uws.environment.parameter.protocol.UnrecognizedProtocolException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * A {@link MutableInternalValue} that works with {@link InputStream}s. It avoids reading values until absolutely necessary.
 * @author Paul Harrison (paul.harrison@manchester.ac.uk) 14 Jul 2009
 * @TODO this class is unfinished - perhaps not necessary either
 * @version $Name:  $
 * @since AIDA Stage 1
 */
public class StreamBasedInternalValue implements MutableInternalValue {

    private ParameterValue val;
    private ProtocolLibrary lib;
    private ExecutionEnvironment env;
    
    public StreamBasedInternalValue(ParameterValue val, ProtocolLibrary lib, ExecutionEnvironment env) {
        this.val = val;
        this.lib = lib;
        this.env = env;
    }
    public InputStream getStreamFrom() throws UWSException {
        if(val.isIndirect()){
            return lib.getExternalValue(val, env.getSecGuard()).read();
        }
        else {
            return new ByteArrayInputStream(val.getValue().getBytes());
        }
    }

    public void setValue(String val) throws ParameterStorageException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException(
                "StreamBasedInternalValue.setValue() not implemented");
    }

    public String asString() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException(
                "StreamBasedInternalValue.asString() not implemented");
    }

    public BinaryEncodings getStringEncoding() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException(
                "StreamBasedInternalValue.getStringEncoding() not implemented");
    }

    public URL locationOf() {
        if (val.isIndirect()) {
            try {
                return new URL(val.getValue());
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return null;
            }
        } else {
            
       throw new UnsupportedOperationException(
                "StreamBasedInternalValue.locationOf() not allowed for direct parameter");
        }
            }

    public long size() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException(
                "StreamBasedInternalValue.size() not implemented");
    }

    public void writeToStream(OutputStream os) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException(
                "StreamBasedInternalValue.writeToStream() not implemented");
    }
    public OutputStream getStreamTo() throws ParameterAdapterException, UnrecognizedProtocolException {
        if(val.isIndirect())
        {
            return lib.getExternalValue(val, env.getSecGuard()).write();
        }
        else {
        throw new  UnsupportedOperationException("MutableInternalValue.getStreamTo() not possible for direct values");
        }
    }
    public void setValue(byte[] resultData) throws ParameterStorageException {
        // TODO Auto-generated method stub
        throw new  UnsupportedOperationException("MutableInternalValue.setValue() not implemented");
    }
    public void setValue(InputStream is) throws ParameterStorageException {
        // TODO Auto-generated method stub
        throw new  UnsupportedOperationException("MutableInternalValue.setValue() not implemented");
    }

}



