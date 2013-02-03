/*
 * UrlFieldAdapter.java
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

import java.net.MalformedURLException;
import java.net.URL;

import android.content.ContentValues;


/**
 * A UrlFieldAdapter stores {@link URL} values in {@link ContentValues}.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class UrlFieldAdapter extends FieldAdapter<URL>
{

	private final String mField;

	private final URL mDefaultValue;


	/**
	 * Constructor for a new urlFieldAdapter without default value.
	 * 
	 * @param urlField
	 *            The field name that holds the URL.
	 */
	public UrlFieldAdapter(String urlField)
	{
		mField = urlField;
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
	public UrlFieldAdapter(String fieldName, URL defaultValue)
	{
		mField = fieldName;
		mDefaultValue = defaultValue;
	}


	@Override
	public URL get(ContentValues values)
	{
		try
		{
			return new URL(values.getAsString(mField));
		}
		catch (MalformedURLException e)
		{
			return null;
		}
	}


	@Override
	public URL getDefault(ContentValues values)
	{
		return mDefaultValue;
	}


	@Override
	public void set(ContentValues values, URL value)
	{
		values.put(mField, value.toString());
	}

}
