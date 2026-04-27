package org.javastro.ivoacore.uws;

/**
 * A composite factory interface that extends {@link JobFactory} and {@link RestorableJobFactory},
 * combining the creation of new UWS jobs with the ability to restore previously persisted jobs.
 * <p>
 * Implementations of this interface are responsible for defining mechanisms to create and restore
 * specific types of jobs within a UWS system.
 * <p>
 * This interface does not define additional methods of its own; it serves as a unifying contract
 * for classes that need capabilities from both {@link JobFactory} and {@link RestorableJobFactory}.
 */
public interface CommonJobFactory  extends JobFactory, RestorableJobFactory  {
}
