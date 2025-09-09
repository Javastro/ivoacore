
package org.javastro.ivoacore.uws.environment.parameter;

import org.javastro.ivoacore.uws.UWSException;

/** Abstraction around reading and writing  parameter values.
 * @see org.javastro.ivoacore.uws.AbstractApplication#instantiateAdapter()}
 * @author Noel Winstanley nw@jb.man.ac.uk 04-Jun-2004
 * @author Paul Harrison (paul.harrison@manchester.ac.uk) 13 Jul 2009 -  changed to be more concrete.
 *
 */
public interface ParameterAdapter {
    
    /** do what it takes to get the actual value for this parameter (used for input parameters)
     * 
     * @return the actual value for this parameter ( or some symbolic representation of it)
     * @throws UWSException
     */
    MutableInternalValue getInternalValue() throws UWSException;
    /**
     * write out this parameter (used for output parameters).
     * 
     * @throws UWSException
     */
    void writeBack() throws UWSException;
    
    /** returns the parameter object this adapter is wrapping. 
     * @TODO eliminate this and add the uses made of the raw parameterValue to this interface.
     * @return the parameter value this adapter wraps.
     * */
    org.javastro.ivoacore.uws.environment.execution.ParameterValue getWrappedParameter();
}


