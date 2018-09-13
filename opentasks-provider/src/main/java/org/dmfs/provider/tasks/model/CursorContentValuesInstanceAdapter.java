/*
 * Copyright 2017 dmfs GmbH
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
 */

package org.dmfs.provider.tasks.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.dmfs.provider.tasks.TaskDatabaseHelper;
import org.dmfs.provider.tasks.model.adapters.FieldAdapter;
import org.dmfs.tasks.contract.TaskContract;


/**
 * An {@link InstanceAdapter} that adapts a {@link Cursor} and a {@link ContentValues} instance. All changes are written to the {@link ContentValues} and can be
 * stored in the database with {@link #commit(SQLiteDatabase)}.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public class CursorContentValuesInstanceAdapter extends AbstractInstanceAdapter
{
    private final long mId;
    private final Cursor mCursor;
    private final ContentValues mValues;


    public CursorContentValuesInstanceAdapter(Cursor cursor, ContentValues values)
    {
        if (cursor == null && !_ID.existsIn(values))
        {
            mId = -1L;
        }
        else
        {
            mId = _ID.getFrom(cursor);
        }
        mCursor = cursor;
        mValues = values;
    }


    public CursorContentValuesInstanceAdapter(long id, Cursor cursor, ContentValues values)
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
    public <T> T valueOf(FieldAdapter<T, InstanceAdapter> fieldAdapter)
    {
        if (mValues == null)
        {
            return fieldAdapter.getFrom(mCursor);
        }
        return fieldAdapter.getFrom(mCursor, mValues);
    }


    @Override
    public <T> T oldValueOf(FieldAdapter<T, InstanceAdapter> fieldAdapter)
    {
        return fieldAdapter.getFrom(mCursor);
    }


    @Override
    public boolean isUpdated(FieldAdapter<?, InstanceAdapter> fieldAdapter)
    {
        return mValues != null && fieldAdapter.isSetIn(mValues);
    }


    @Override
    public boolean isWriteable()
    {
        return mValues != null;
    }


    @Override
    public boolean hasUpdates()
    {
        return mValues != null && mValues.size() > 0;
    }


    @Override
    public <T> void set(FieldAdapter<T, InstanceAdapter> fieldAdapter, T value) throws IllegalStateException
    {
        fieldAdapter.setIn(mValues, value);
    }


    @Override
    public void unset(FieldAdapter<?, InstanceAdapter> fieldAdapter) throws IllegalStateException
    {
        fieldAdapter.removeFrom(mValues);
    }


    @Override
    public void commit(SQLiteDatabase db)
    {
        if (mValues.size() == 0)
        {
            return;
        }

        db.update(TaskDatabaseHelper.Tables.TASKS, mValues, TaskContract.TaskColumns._ID + "=" + mId, null);
    }


    @Override
    public <T> T getState(FieldAdapter<T, InstanceAdapter> stateFieldAdater)
    {
        return null;
    }


    @Override
    public <T> void setState(FieldAdapter<T, InstanceAdapter> stateFieldAdater, T value)
    {

    }


    @Override
    public InstanceAdapter duplicate()
    {
        ContentValues newValues = new ContentValues(mValues);

        // copy all columns (except _ID) that are not in the values yet
        for (int i = 0, count = mCursor.getColumnCount(); i < count; ++i)
        {
            String column = mCursor.getColumnName(i);
            if (!newValues.containsKey(column) && !TaskContract.Instances._ID.equals(column))
            {
                newValues.put(column, mCursor.getString(i));
            }
        }

        return new ContentValuesInstanceAdapter(newValues);
    }


    @Override
    public TaskAdapter taskAdapter()
    {
        // make sure we remove any instance fields
        ContentValues values = new ContentValues(mValues);
        values.remove(TaskContract.Instances.INSTANCE_START);
        values.remove(TaskContract.Instances.INSTANCE_START_SORTING);
        values.remove(TaskContract.Instances.INSTANCE_DUE);
        values.remove(TaskContract.Instances.INSTANCE_DUE_SORTING);
        values.remove(TaskContract.Instances.INSTANCE_DURATION);
        values.remove(TaskContract.Instances.INSTANCE_ORIGINAL_TIME);
        values.remove(TaskContract.Instances.TASK_ID);
        values.remove(TaskContract.Instances.DISTANCE_FROM_CURRENT);
        values.remove("_id:1");

        return new CursorContentValuesTaskAdapter(valueOf(InstanceAdapter.TASK_ID), mCursor, values);
    }
}
