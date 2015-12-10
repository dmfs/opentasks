/*
 * Copyright (C) 2013 Marten Gajda <marten@dmfs.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package org.dmfs.tasks.model.contraints;

import org.dmfs.tasks.model.ContentSet;


/**
 * Defines a constraint and provides a method to check that a value complies with that constraint or enforces it if it doesn't.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 * 
 * @param <T>
 *            The type of the value to check.
 */
public abstract class AbstractConstraint<T>
{
	/**
	 * Checks that <code>newValue</code> does not violate the constraint within the context of <code>currentValues</code>. Enforces the constraint if possible
	 * or throws an exception if not.
	 * 
	 * @param currentValues
	 *            The {@link ContentSet} to validate.
	 * @param oldValue
	 *            The old value, can be <code>null</code>.
	 * @param newValue
	 *            The new value to validate, can be <code>null</code>.
	 */
	public abstract T apply(ContentSet currentValues, T oldValue, T newValue);
}
