package org.dmfs.tasks.model.contraints;

import org.dmfs.tasks.model.ContentSet;


public abstract class AbstractConstraint<T>
{
	/**
	 * Checks that <code>object</code> does not violate the constraint within the context of <code>values</code>. Enforces the constraint if possible or throws
	 * an exception if not.
	 * 
	 * @param values
	 *            The {@link ContentSet} to validate.
	 * @param object
	 *            The value to value.
	 */
	public abstract void apply(ContentSet values, T object);
}
