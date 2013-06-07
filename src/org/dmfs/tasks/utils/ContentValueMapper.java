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

package org.dmfs.tasks.utils;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;


/**
 * Loads {@link ContentValues} from a {@link Cursor}.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public class ContentValueMapper
{
	private final List<String> StringColumns = new ArrayList<String>();
	private final List<String> IntegerColumns = new ArrayList<String>();
	private final List<String> LongColumns = new ArrayList<String>();


	/**
	 * Tells the {@link ContentValueMapper} to load the given columns as String values.
	 * 
	 * @param columnNames
	 *            The column names to load as strings.
	 * @return This instance.
	 */
	public ContentValueMapper addString(String... columnNames)
	{
		for (String column : columnNames)
		{
			StringColumns.add(column);
		}
		return this;
	}


	/**
	 * Tells the {@link ContentValueMapper} to load the given columns as Integer values.
	 * 
	 * @param columnNames
	 *            The column names to load as integers.
	 * @return This instance.
	 */
	public ContentValueMapper addInteger(String... columnNames)
	{
		for (String column : columnNames)
		{
			IntegerColumns.add(column);
		}
		return this;
	}


	/**
	 * Tells the {@link ContentValueMapper} to load the given columns as Long values.
	 * 
	 * @param columnNames
	 *            The column names to load as longs.
	 * @return This instance.
	 */
	public ContentValueMapper addLong(String... columnNames)
	{
		for (String column : columnNames)
		{
			LongColumns.add(column);
		}
		return this;
	}


	/**
	 * Get an array of all column names this {@link ContentValueMapper} loads.
	 * 
	 * @return An array of Strings, will never be <code>null</code>.
	 */
	public String[] getColumns()
	{
		String[] columns = new String[StringColumns.size() + IntegerColumns.size() + LongColumns.size()];

		int i = 0;
		for (String column : StringColumns)
		{
			columns[i] = column;
			++i;
		}
		for (String column : IntegerColumns)
		{
			columns[i] = column;
			++i;
		}
		for (String column : LongColumns)
		{
			columns[i] = column;
			++i;
		}

		return columns;
	}


	/**
	 * Loads the values in a {@link Cursor} into {@link ContentValues}.
	 * 
	 * @param cursor
	 *            The {@link Cursor} to load.
	 * @return The {@link ContentValues} or <code>null</code> if <code>cursor</code> is <code>null</code>.
	 */
	public ContentValues map(Cursor cursor)
	{
		if (cursor == null)
		{
			return null;
		}

		ContentValues values = new ContentValues();

		for (String column : StringColumns)
		{
			final int index = cursor.getColumnIndexOrThrow(column);
			if (!cursor.isNull(index))
			{
				values.put(column, cursor.getString(index));
			}
			else
			{
				values.putNull(column);
			}
		}

		for (String column : IntegerColumns)
		{
			final int index = cursor.getColumnIndexOrThrow(column);
			if (!cursor.isNull(index))
			{
				values.put(column, cursor.getInt(index));
			}
			else
			{
				values.putNull(column);
			}
		}

		for (String column : LongColumns)
		{
			final int index = cursor.getColumnIndexOrThrow(column);
			if (!cursor.isNull(index))
			{
				values.put(column, cursor.getLong(index));
			}
			else
			{
				values.putNull(column);
			}
		}

		return values;
	}
}
