

package org.javastro.ivoacore.uws.environment.parameter;

import org.javastro.ivoacore.uws.environment.execution.BinaryEncodings;

import java.io.*;
import java.net.URL;

/**
 * An internalValue that is really stored as a string. All methods make the assumption that the values are to be interpreted as strings.
 * @author Paul Harrison (paul.harrison@manchester.ac.uk) 13 Jul 2009
 * @version $Name:  $
 * @since AIDA Stage 1
 */
public class DefaultInternalValue implements MutableInternalValue {
    
    private String value = null;

    public DefaultInternalValue(String value2) {
       this.value = value2;
    }

    public DefaultInternalValue() {
       
    }

    public String asString() {
        return value;
    }

   public URL locationOf() {
        return null;
    }

    public long size() {
       return value.length();
    }

    public BinaryEncodings getStringEncoding() {
        return BinaryEncodings.NONE;
    }

    public void setValue(InputStreamReader ir) throws ParameterStorageException {
        StringWriter sw = null;
        try {
            sw = new StringWriter();
            ir.transferTo(sw);
            value=sw.toString();
        } catch (IOException e) {
           logger.error("problem setting value", e);
           throw new ParameterStorageException("problem setting value", e);
        }
        finally{
            try {
                ir.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
           if(sw!=null) {
               try {
                sw.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
           }
        }
    }

    /** logger for this class */
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
            .getLogger(DefaultInternalValue.class);

    public void writeToStream(OutputStream os) {
       PrintWriter pw = new PrintWriter(os);
       pw.print(value);
       pw.flush();
    }

    public void setValue(String string) {
       value = string;
    }

    public InputStream getStreamFrom() {
       return new ByteArrayInputStream(value.getBytes());
    }

    public void setValue(byte[] resultData) throws ParameterStorageException {
       this.setValue(new InputStreamReader(new ByteArrayInputStream(resultData)));
    }

    public void setValue(InputStream is) throws ParameterStorageException {
        this.setValue(new InputStreamReader(is));
    }

     
}


