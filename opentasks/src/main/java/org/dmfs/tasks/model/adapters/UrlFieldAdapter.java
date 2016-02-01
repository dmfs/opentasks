/*
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

import java.net.MalformedURLException;
import java.net.URL;

import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.OnContentChangeListener;

import android.content.ContentValues;
import android.database.Cursor;


/**
 * Knows how to load and store {@link URL} values in a {@link ContentSet}.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class UrlFieldAdapter extends FieldAdapter<URL>
{

	private final String mFieldName;

	private final URL mDefaultValue;


	/**
	 * Constructor for a new urlFieldAdapter without default value.
	 * 
	 * @param urlField
	 *            The field name that holds the URL.
	 */
	public UrlFieldAdapter(String urlField)
	{
		if (urlField == null)
		{
			throw new IllegalArgumentException("urlField must not be null");
		}
		mFieldName = urlField;
		mDefaultValue = null;
	}


	/**
	 * Constructor for a new UrlFieldAdapter with default value.
	 * 
	 * @param fieldName
	 *            The name of the field to use when loading or storing the value.
	 * @param defaultValue
	 *            The defaultValue.
	 */
	public UrlFieldAdapter(String urlField, URL defaultValue)
	{
		if (urlField == null)
		{
			throw new IllegalArgumentException("urlField must not be null");
		}
		mFieldName = urlField;
		mDefaultValue = defaultValue;
	}


	@Override
	public URL get(ContentSet values)
	{
		try
		{
			return new URL(values.getAsString(mFieldName));
		}
		catch (MalformedURLException e)
		{
			return null;
		}
	}


	@Override
	public URL get(Cursor cursor)
	{
		int columnIdx = cursor.getColumnIndex(mFieldName);
		if (columnIdx < 0)
		{
			throw new IllegalArgumentException("The urlField column missing in cursor.");
		}
		try
		{
			return new URL(cursor.getString(columnIdx));
		}
		catch (MalformedURLException e)
		{
			return null;
		}
	}


	@Override
	public URL getDefault(ContentSet values)
	{
		return mDefaultValue;
	}


	@Override
	public void set(ContentSet values, URL value)
	{
		if (value == null)
		{
			values.put(mFieldName, (String) null);
		}
		else
		{
			values.put(mFieldName, value.toString());
		}
	}


	@Override
	public void set(ContentValues values, URL value)
	{
		if (value == null)
		{
			values.putNull(mFieldName);
		}
		else
		{
			values.put(mFieldName, value.toString());
		}
	}


	@Override
	public void registerListener(ContentSet values, OnContentChangeListener listener, boolean initalNotification)
	{
		values.addOnChangeListener(listener, mFieldName, initalNotification);
	}


	@Override
	public void unregisterListener(ContentSet values, OnContentChangeListener listener)
	{
		values.removeOnChangeListener(listener, mFieldName);
	}
}
