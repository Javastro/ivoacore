

package org.javastro.ivoacore.uws.environment.parameter;

import org.javastro.ivoacore.uws.UWSException;
import org.javastro.ivoacore.uws.description.ParameterDescription;
import org.javastro.ivoacore.uws.description.ParameterDirection;
import org.javastro.ivoacore.uws.environment.ExecutionEnvironment;
import org.javastro.ivoacore.uws.environment.execution.ParameterValue;
import org.javastro.ivoacore.uws.environment.parameter.protocol.DefaultProtocolLibraryFactory;
import org.javastro.ivoacore.uws.environment.parameter.protocol.ProtocolLibrary;

/**
 * Base class for {@link org.javastro.ivoacore.uws.parameter.ParameterAdapter}. It contains the necessary fileds that the adapter wraps. 
 * @author Paul Harrison (pah@jb.man.ac.uk) 27-Oct-2004
 * @version $Name:  $
 * @since iteration6
 */
public abstract class AbstractParameterAdapter implements ParameterAdapter {

/** Construct a new AbstractParameterAdapter
    * @param val the parameter value to adapt.
    * @param description the description associated with this value.
    * @param env wrapper around the external location that contains the true value of the parameter (in case of direct parameters, is null) 
    */
   public AbstractParameterAdapter(ParameterValue val, ParameterDescription description, ParameterDirection direction, ExecutionEnvironment env) {
       this.val = val;
       this.description = description;
       this.env = env;
       this.direction = direction;
   }

   /** the parameter value */
   protected final ParameterValue val;
   /** the parameter descritpion */
   protected final ParameterDescription description;
   /** indirection to the external location containing the true value of an indirect parameter */
   protected final ExecutionEnvironment env;
   /** the parameter direction - whether input or output */
protected ParameterDirection direction;
  
   protected MutableInternalValue internalVal = null;
   
   public ParameterValue getWrappedParameter() {
        return val;
    }

   protected ProtocolLibrary getProtocolLib() {
       return new DefaultProtocolLibraryFactory().createLibrary();
   }

public abstract MutableInternalValue getInternalValue() throws UWSException;

public abstract void writeBack() throws UWSException;


}
