

package org.javastro.ivoacore.uws.environment.parameter;

import org.javastro.ivoacore.uws.UWSException;
import org.javastro.ivoacore.uws.environment.execution.BinaryEncodings;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 * How to access the internal value of a parameter.
 * 
 * @author Paul Harrison (paul.harrison@manchester.ac.uk) 11 Jul 2009
 * @version $Name:  $
 * @since AIDA Stage 1
 */
public interface InternalValue {

    /**
     * Return the internal value as a string. If the internal value is actually
     * binary in nature then the value should be text encoded in some fashion.
     * 
     * @return the string representation of the internal value.
     * @throws ParameterAdapterException if the value cannot be retrieved as a string.
     */
    String asString() throws ParameterAdapterException;

    /**
     * Return the type of the binary encoding.
     * 
     * @return the {@link BinaryEncodings} type used for this value.
     */
    BinaryEncodings getStringEncoding();

    /**
     * Return the location where the internal value is stored.
     * 
     * @return null if the value is not stored somewhere, but is just in memory.
     */
    URL locationOf();

    /**
     * The size of the value in bytes.
     * 
     * @return the size in bytes.
     */
    long size();

    /**
     * Write the value to the output stream.
     * 
     * @param os
     *            the outputstream to write the value to. Note that the stream
     *            is not closed.
     * @throws IOException if the value cannot be written to the stream.
     */
    void writeToStream(OutputStream os) throws IOException;
    
    
    /**
     * Get a stream to read the internal value;
     * @TODO do we really want to support this in the interface - not possible for String based internal values.
     * @return an {@link InputStream} to read the internal value.
     * @throws UWSException if the stream cannot be opened.
     */
    InputStream getStreamFrom() throws UWSException;


}


