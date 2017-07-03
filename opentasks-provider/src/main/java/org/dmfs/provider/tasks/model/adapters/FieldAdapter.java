/*
 * Copyright (C) 2015 Marten Gajda <marten@dmfs.org>
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

package org.dmfs.provider.tasks.model.adapters;

import android.content.ContentValues;
import android.database.Cursor;


/**
 * Knows how to load and store a specific field from or to {@link ContentValues} or from {@link Cursor}s.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 * 
 * @param <FieldType>
 *            The type of the value this adapter stores.
 * @param <EntitType>
 *            The type of the entity the field belongs to.
 */
public interface FieldAdapter<FieldType, EntitType>
{

	/**
	 * Check if a value is present and non-<code>null</code> in the given {@link ContentValues}.
	 * 
	 * @param values
	 *            The {@link ContentValues} to check.
	 * @return
	 */
	public boolean existsIn(ContentValues values);


	/**
	 * Check if a value is present (may be <code>null</code>) in the given {@link ContentValues}.
	 * 
	 * @param values
	 *            The {@link ContentValues} to check.
	 * @return
	 */
	public boolean isSetIn(ContentValues values);


	/**
	 * Get the value from the given {@link ContentValues}
	 * 
	 * @param values
	 *            The {@link ContentValues} that contain the value to return.
	 * @return The value.
	 */
	public FieldType getFrom(ContentValues values);


	/**
	 * Check if a value is present and non-<code>null</code> in the given {@link Cursor}.
	 * 
	 * @param cursor
	 *            The {@link Cursor} that contains the value to check.
	 * @return
	 */
	public boolean existsIn(Cursor cursor);


	/**
	 * Get the value from the given {@link Cursor}
	 * 
	 * @param cursor
	 *            The {@link Cursor} that contain the value to return.
	 * @return The value.
	 */
	public FieldType getFrom(Cursor cursor);


	/**
	 * Check if a value is present and non-<code>null</code> in the given {@link Cursor} or {@link ContentValues}.
	 * 
	 * @param cursor
	 *            The {@link Cursor} that contains the value to check.
	 * @param values
	 *            The {@link ContentValues} that contains the value to check.
	 * @return
	 */
	public boolean existsIn(Cursor cursor, ContentValues values);


	/**
	 * Get the value from the given {@link Cursor} or {@link ContentValues}, with the {@link ContentValues} taking precedence over the cursor values.
	 * 
	 * @param cursor
	 *            The {@link Cursor} that contains the value to return.
	 * @param values
	 *            The {@link ContentValues} that contains the value to return.
	 * @return The value.
	 */
	public FieldType getFrom(Cursor cursor, ContentValues values);


	/**
	 * Set a value in the given {@link ContentValues}.
	 * 
	 * @param values
	 *            The {@link ContentValues} to store the new value in.
	 * @param value
	 *            The new value to store.
	 */
	public void setIn(ContentValues values, FieldType value);


	/**
	 * Remove a value from the given {@link ContentValues}.
	 * 
	 * @param values
	 *            The {@link ContentValues} from which to remove the value.
	 */
	public void removeFrom(ContentValues values);


	/**
	 * Copy the value from a {@link Cursor} to the given {@link ContentValues}.
	 * 
	 * @param source
	 *            The {@link Cursor} that contains the value to copy.
	 * @param dest
	 *            The {@link ContentValues} to receive the value.
	 */
	public void copyValue(Cursor source, ContentValues dest);


	/**
	 * Copy the value from {@link ContentValues} to another {@link ContentValues} object.
	 * 
	 * @param source
	 *            The {@link ContentValues} that contains the value to copy.
	 * @param dest
	 *            The {@link ContentValues} to receive the value.
	 */
	public void copyValue(ContentValues source, ContentValues dest);

}
