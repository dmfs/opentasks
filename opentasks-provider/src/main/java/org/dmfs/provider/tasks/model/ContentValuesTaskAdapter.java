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
import android.database.sqlite.SQLiteDatabase;

import org.dmfs.provider.tasks.TaskDatabaseHelper;
import org.dmfs.provider.tasks.model.adapters.FieldAdapter;
import org.dmfs.tasks.contract.TaskContract;


/**
 * A {@link TaskAdapter} for tasks that are stored in a {@link ContentValues}.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public class ContentValuesTaskAdapter extends AbstractTaskAdapter
{
    private long mId;
    private final ContentValues mValues;


    public ContentValuesTaskAdapter(ContentValues values)
    {
        this(-1L, values);
    }


    public ContentValuesTaskAdapter(long id, ContentValues values)
    {
        mId = id;
        mValues = values;
    }


    @Override
    public long id()
    {
        return mId;
    }


    @Override
    public <T> T valueOf(FieldAdapter<T, TaskAdapter> fieldAdapter)
    {
        return fieldAdapter.getFrom(mValues);
    }


    @Override
    public <T> T oldValueOf(FieldAdapter<T, TaskAdapter> fieldAdapter)
    {
        return null;
    }


    @Override
    public boolean isUpdated(FieldAdapter<?, TaskAdapter> fieldAdapter)
    {
        return fieldAdapter.isSetIn(mValues);
    }


    @Override
    public boolean isWriteable()
    {
        return true;
    }


    @Override
    public boolean hasUpdates()
    {
        return mValues.size() > 0;
    }


    @Override
    public <T> void set(FieldAdapter<T, TaskAdapter> fieldAdapter, T value) throws IllegalStateException
    {
        fieldAdapter.setIn(mValues, value);
    }


    @Override
    public void unset(FieldAdapter<?, TaskAdapter> fieldAdapter) throws IllegalStateException
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

        if (mId < 0)
        {
            mId = db.insert(TaskDatabaseHelper.Tables.TASKS, null, mValues);
            return mId > 0 ? 1 : 0;
        }
        else
        {
            return db.update(TaskDatabaseHelper.Tables.TASKS, mValues, TaskContract.TaskColumns._ID + "=" + mId, null);
        }
    }


    @Override
    public TaskAdapter duplicate()
    {
        return new ContentValuesTaskAdapter(new ContentValues(mValues));
    }
}
