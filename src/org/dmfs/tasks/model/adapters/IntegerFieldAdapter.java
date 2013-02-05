/*
 * IntegerFieldAdapter.java
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

import android.content.ContentValues;
import android.database.Cursor;


/**
 * An IntegerFieldAdapter stores {@link Integer} values in a certain field of {@link ContentValues}.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class IntegerFieldAdapter extends FieldAdapter<Integer>
{

	/**
	 * The field name this adapter uses to store the values.
	 */
	private final String mFieldName;

	/**
	 * The default value, if any.
	 */
	private final Integer mDefaultValue;


	/**
	 * Constructor for a new IntegerFieldAdapter without default value.
	 * 
	 * @param fieldName
	 *            The name of the field to use when loading or storing the value.
	 */
	public IntegerFieldAdapter(String fieldName)
	{
		mFieldName = fieldName;
		mDefaultValue = null;
	}


	/**
	 * Constructor for a new IntegerFieldAdapter with default value.
	 * 
	 * @param fieldName
	 *            The name of the field to use when loading or storing the value.
	 * @param defaultValue
	 *            The defaultValue.
	 */
	public IntegerFieldAdapter(String fieldName, Integer defaultValue)
	{
		mFieldName = fieldName;
		mDefaultValue = defaultValue;
	}


	@Override
	public Integer get(ContentValues values)
	{
		// return the value as Integer
		return values.getAsInteger(mFieldName);
	}


	@Override
	public Integer get(Cursor cursor)
	{
		int columnIdx = cursor.getColumnIndex(mFieldName);
		if (columnIdx < 0)
		{
			return null;
		}
		return cursor.getInt(columnIdx);
	}


	@Override
	public Integer getDefault(ContentValues values)
	{
		return mDefaultValue;
	}


	@Override
	public void set(ContentValues values, Integer value)
	{
		values.put(mFieldName, value);
	}
}
