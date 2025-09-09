
/*
 * Created on 05/09/2025 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */
/**
 * TODO rework/modernise, as parameter handling has been carried over from astrogrid code without much modification.
 *
 * 	Managing and Manpulating parameter values<.
 * 		This class defines the abstraction of a {@link ParameterAdapter} - which privides a uniform way of handling parameters as
 * 		they are marshalled in and out of the application.
 *
 * 		<p />
 * 		This package provides a default implementation that provides all parameters as in-memory strings, and
 * 		utility code for working with indirect parameters - i.e. those passed by reference to an external resource, which has to be
 * 		retreived to gain the parameter value itself. Handlers for accessing external resources by a variety of different protocols are defined in the
 * 		      {@link org.javastro.ivoacore.uws.parameter.protocol} package.
 */
package org.javastro.ivoacore.uws.environment.parameter;