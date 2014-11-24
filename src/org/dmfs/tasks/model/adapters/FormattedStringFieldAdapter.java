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

import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.OnContentChangeListener;

import android.content.ContentValues;
import android.database.Cursor;


/**
 * A {@link FieldAdapter} that can format values from multiple String fields into one value.
 * 
 * <p>
 * Note: This adapter doesn't support any set method. Attempts to modify a value will be ignored.
 * </p>
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public class FormattedStringFieldAdapter extends FieldAdapter<String>
{

	/**
	 * The format of the result, just like {@link String#format(String, Object...)} expects it.
	 */
	private final String mFormat;

	/**
	 * The adapters of the parameter fields.
	 */
	private final FieldAdapter<String>[] mParamFields;


	/**
	 * Constructor for a new FormattedStringFieldAdapter with the given format string and the given {@link StringFieldAdapter}s as parameters.
	 * 
	 * @param format
	 *            The format of the resulting strings. This uses the format of {@link String#format(String, Object...)}.
	 */
	public FormattedStringFieldAdapter(String format, StringFieldAdapter... paramFields)
	{
		if (format == null)
		{
			throw new IllegalArgumentException("format must not be null");
		}
		mFormat = format;
		mParamFields = paramFields;
	}


	@Override
	public String get(ContentSet values)
	{
		String[] params = new String[mParamFields.length];
		for (int i = 0, len = mParamFields.length; i < len; ++i)
		{
			params[i] = mParamFields[i].get(values);
		}
		return String.format(mFormat, (Object[]) params);
	}


	@Override
	public String get(Cursor cursor)
	{
		String[] params = new String[mParamFields.length];
		for (int i = 0, len = mParamFields.length; i < len; ++i)
		{
			params[i] = mParamFields[i].get(cursor);
		}
		return String.format(mFormat, (Object[]) params);
	}


	@Override
	public String getDefault(ContentSet values)
	{
		String[] params = new String[mParamFields.length];
		for (int i = 0, len = mParamFields.length; i < len; ++i)
		{
			params[i] = mParamFields[i].getDefault(values);
		}
		return String.format(mFormat, (Object[]) params);
	}


	@Override
	public void set(ContentSet values, String value)
	{
		// setting values is not supported
	}


	@Override
	public void set(ContentValues values, String value)
	{
		// setting values is not supported
	}


	@Override
	public void registerListener(ContentSet values, OnContentChangeListener listener, boolean initalNotification)
	{
		for (FieldAdapter<?> adapter : mParamFields)
		{
			adapter.registerListener(values, listener, initalNotification);
		}
	}


	@Override
	public void unregisterListener(ContentSet values, OnContentChangeListener listener)
	{
		for (FieldAdapter<?> adapter : mParamFields)
		{
			adapter.unregisterListener(values, listener);
		}
	}
}
