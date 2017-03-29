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

import org.dmfs.rfc5545.Duration;

import android.content.ContentValues;
import android.database.Cursor;


/**
 * Knows how to load and store {@link Duration} values from a {@link Cursor} or {@link ContentValues}.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 * 
 * @param <EntityType>
 *            The type of the entity the field belongs to.
 */
public final class DurationFieldAdapter<EntityType> extends SimpleFieldAdapter<Duration, EntityType>
{

	private final String mFieldName;


	/**
	 * Constructor for a new {@link DurationFieldAdapter}.
	 * 
	 * @param urlField
	 *            The field name that holds the {@link Duration}.
	 */
	public DurationFieldAdapter(String urlField)
	{
		if (urlField == null)
		{
			throw new IllegalArgumentException("urlField must not be null");
		}
		mFieldName = urlField;
	}


	@Override
	String fieldName()
	{
		return mFieldName;
	}


	@Override
	public Duration getFrom(ContentValues values)
	{
		String rawValue = values.getAsString(mFieldName);
		if (rawValue == null)
		{
			return null;
		}

		return Duration.parse(rawValue);
	}


	@Override
	public Duration getFrom(Cursor cursor)
	{
		int columnIdx = cursor.getColumnIndex(mFieldName);
		if (columnIdx < 0)
		{
			throw new IllegalArgumentException("The column '" + mFieldName + "' is missing in cursor.");
		}

		if (cursor.isNull(columnIdx))
		{
			return null;
		}

		return Duration.parse(cursor.getString(columnIdx));
	}


	@Override
	public void setIn(ContentValues values, Duration value)
	{
		if (value != null)
		{
			values.put(mFieldName, value.toString());
		}
		else
		{
			values.putNull(mFieldName);
		}
	}

}
