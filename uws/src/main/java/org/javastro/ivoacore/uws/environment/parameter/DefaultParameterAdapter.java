
package org.javastro.ivoacore.uws.environment.parameter;

import org.javastro.ivoacore.common.security.SecurityGuard;
import org.javastro.ivoacore.uws.UWSException;
import org.javastro.ivoacore.uws.description.ParameterDescription;
import org.javastro.ivoacore.uws.description.ParameterDirection;
import org.javastro.ivoacore.uws.environment.ExecutionEnvironment;
import org.javastro.ivoacore.uws.environment.execution.ParameterValue;
import org.javastro.ivoacore.uws.environment.parameter.protocol.ExternalValue;
import org.javastro.ivoacore.uws.environment.parameter.protocol.InaccessibleExternalValueException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;

/**
 * The default implementation of
 * {@link org.javastro.ivoacore.uws.parameter.ParameterAdapter}
 * <p/>
 * Handles both direct and indirect parameters, returning them as an in-memory
 * string.
 * 
 * @see org.javastro.ivoacore.uws.parameter.protocol.ProtocolLibrary#getExternalValue(ParameterValue,
 *      SecurityGuard)
 * @author Noel Winstanley (nw@jb.man.ac.uk)
 * @author Paul Harrison (pah@jb.man.ac.uk)
 * @todo should really do different things according to what the type of the
 *       parameter is.
 * @FIXME - binary encode in some fashion
 */
public class DefaultParameterAdapter extends AbstractParameterAdapter {
    /**
     * Constructor.
     * 
     * @param val
     * @param description
     * @param dir 
     * @param externalVal
     */
    public DefaultParameterAdapter(ParameterValue val,
                                   ParameterDescription description, ParameterDirection dir, ExecutionEnvironment env ){
        super(val, description, dir, env);
    }

    /**
     * Commons Logger for this class
     */
    private static final Logger logger = LoggerFactory
            .getLogger(DefaultParameterAdapter.class);

    /**
     * retrieves the value for this parameter if the parameter is direct, just
     * return the value of the parameter value itself, if indirect, retrieve the
     * value from the {@link #externalVal}
     * 
     * @return always returns the string value of this parameter
     * */
    @Override
    public MutableInternalValue getInternalValue() throws UWSException {
        if (internalVal == null) {

            if (!val.isIndirect()) {
                internalVal = new DefaultInternalValue(val.getValue());

            } else {
                try {

                    FileBasedInternalValue ival = new FileBasedInternalValue(
                            env.getTempFile(val.getId()));
                    if(direction.equals(ParameterDirection.INPUT)){ // only read the value if it is an input
                    ExternalValue externalVal = getProtocolLib()
                            .getExternalValue(val, env.getSecGuard());
                    ival.setValue(externalVal.read());
                    }
                    internalVal = ival;
                } catch (InaccessibleExternalValueException e) {
                    throw new UWSException("Could not process parameter "
                            + val.getId(), e);
                }
            }
        }
        return internalVal;
    }

    protected InternalValue createInternalValue() {
        return new DefaultInternalValue();
    }

    /**
     * Writes the value of an output parameter back to the parameter storage.
     * That storage may be an internal buffer (a "direct" parameter) or an
     * external location (an "indirect" parameter).  In a direct parameter, the value is forced into a
     * String object and will be encoded in the process.
     */
    @Override
    public void writeBack() throws UWSException {
        if(direction.equals(ParameterDirection.OUTPUT)){
        if (val.isIndirect()) {
            ExternalValue evalue = getProtocolLib().getExternalValue(val,
                    env.getSecGuard());
            OutputStream os = evalue.write();
            try {
                internalVal.writeToStream(os);
            } catch (IOException e) {
                throw new ParameterWriteBackException(
                        "cannot write back parameter =" + val.getId(), e);
            } finally {
                try {
                    os.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } else {
           //FIXME this needs to be solved
         //   val.setValue(internalVal.asString());
         //   val.setEncoding(internalVal.getStringEncoding());
        }
        }
        else {
            throw new IllegalParameterUse("cannot write to input parameter " + val.getId());
        }
    }

}

