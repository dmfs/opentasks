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

package org.dmfs.provider.tasks.model;

import org.dmfs.provider.tasks.TaskContract;
import org.dmfs.provider.tasks.TaskDatabaseHelper;
import org.dmfs.provider.tasks.model.adapters.FieldAdapter;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


/**
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public class CursorContentValuesListAdapter extends AbstractListAdapter
{
	private final long mId;
	private final Cursor mCursor;
	private final ContentValues mValues;


	public CursorContentValuesListAdapter(long id, Cursor cursor, ContentValues values)
	{
		mId = id;
		mCursor = cursor;
		mValues = values;
	}


	@Override
	public long id()
	{
		return mId;
	}


	@Override
	public <T> T valueOf(FieldAdapter<T, ListAdapter> fieldAdapter)
	{
		return fieldAdapter.getFrom(mCursor, mValues);
	}


	@Override
	public <T> T oldValueOf(FieldAdapter<T, ListAdapter> fieldAdapter)
	{
		return fieldAdapter.getFrom(mCursor);
	}


	@Override
	public <T> boolean isUpdated(FieldAdapter<T, ListAdapter> fieldAdapter)
	{
		return mValues != null && fieldAdapter.isSetIn(mValues);
	}


	@Override
	public boolean isWriteable()
	{
		return true;
	}


	@Override
	public boolean hasUpdates()
	{
		return mValues != null && mValues.size() > 0;
	}


	@Override
	public <T> void set(FieldAdapter<T, ListAdapter> fieldAdapter, T value) throws IllegalStateException
	{
		fieldAdapter.setIn(mValues, value);
	}


	@Override
	public <T> void unset(FieldAdapter<T, ListAdapter> fieldAdapter) throws IllegalStateException
	{
		fieldAdapter.removeFrom(mValues);
	}


	@Override
	public int commit(SQLiteDatabase db)
	{
		if (mValues.size() == 0)
		{
			return 0;
		}

		return db.update(TaskDatabaseHelper.Tables.LISTS, mValues, TaskContract.TaskListColumns._ID + "=" + mId, null);
	}


	@Override
	public ListAdapter duplicate()
	{
		ContentValues newValues = new ContentValues(mValues);

		// copy all columns (except _ID) that are not in the values yet
		for (int i = 0, count = mCursor.getColumnCount(); i < count; ++i)
		{
			String column = mCursor.getColumnName(i);
			if (!newValues.containsKey(column) && !TaskContract.Tasks._ID.equals(column))
			{
				newValues.put(column, mCursor.getString(i));
			}
		}

		return new ContentValuesListAdapter(newValues);
	}
}
