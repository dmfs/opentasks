/*
 * FieldAdapter.java
 *
 * Copyright (C) 2012 Marten Gajda <marten@dmfs.org>
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

package org.dmfs.tasks.model.adapters;

import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.OnContentChangeListener;

import android.content.ContentValues;
import android.database.Cursor;


/**
 * A FieldAdapter knows how to store a certain field in {@link ContentValues}.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 * 
 * @param <Type>
 *            The type of the value this adapter stores.
 */
public abstract class FieldAdapter<Type>
{

	/**
	 * Get the value form the given {@link ContentValues}
	 * 
	 * @param values
	 *            The {link ContentValues} that contain the value to return.
	 * @return The value.
	 */
	public abstract Type get(ContentSet values);


	/**
	 * Get the value form the given {@link Cursor}
	 * 
	 * @param values
	 *            The {link Cursor} that contain the value to return.
	 * @return The value.
	 */
	public abstract Type get(Cursor cursor);


	/**
	 * Get a default value for this Adapter.
	 * 
	 * @param values
	 *            The {link ContentValues}.
	 * 
	 * @return A default Value
	 */
	public abstract Type getDefault(ContentSet values);


	/**
	 * Set a value in the given {@link ContentValues}.
	 * 
	 * @param values
	 *            The {@link ContentValues} where to store the new value.
	 * @param value
	 *            The new value to store.
	 */
	public abstract void set(ContentSet values, Type value);


	/**
	 * Register a listener for the values that this adapter manages.
	 * 
	 * @param values
	 *            The {@link ContentSet}.
	 * @param listener
	 *            The {link {@link OnContentChangeListener} to register.
	 */
	public abstract void registerListener(ContentSet values, OnContentChangeListener listener, boolean initialNotification);


	/**
	 * Unregister a listener for the values that this adapter manages.
	 * 
	 * @param values
	 *            The {@link ContentSet}.
	 * @param listener
	 *            The {link {@link OnContentChangeListener} to unregister.
	 */
	public abstract void unregisterListener(ContentSet values, OnContentChangeListener listener);
}
