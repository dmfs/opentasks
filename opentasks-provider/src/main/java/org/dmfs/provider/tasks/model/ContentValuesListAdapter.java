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
 * @author Marten Gajda <marten@dmfs.org>
 */
public class ContentValuesListAdapter extends AbstractListAdapter
{
    private long mId;
    private final ContentValues mValues;


    public ContentValuesListAdapter(ContentValues values)
    {
        this(-1L, values);
    }


    public ContentValuesListAdapter(long id, ContentValues values)
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
    public <T> T valueOf(FieldAdapter<T, ListAdapter> fieldAdapter)
    {
        return fieldAdapter.getFrom(mValues);
    }


    @Override
    public <T> T oldValueOf(FieldAdapter<T, ListAdapter> fieldAdapter)
    {
        return null;
    }


    @Override
    public boolean isUpdated(FieldAdapter<?, ListAdapter> fieldAdapter)
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
    public <T> void set(FieldAdapter<T, ListAdapter> fieldAdapter, T value) throws IllegalStateException
    {
        fieldAdapter.setIn(mValues, value);
    }


    @Override
    public void unset(FieldAdapter<?, ListAdapter> fieldAdapter) throws IllegalStateException
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
            mId = db.insert(TaskDatabaseHelper.Tables.LISTS, null, mValues);
            return mId > 0 ? 1 : 0;
        }
        else
        {
            return db.update(TaskDatabaseHelper.Tables.LISTS, mValues, TaskContract.TaskListColumns._ID + "=" + mId, null);
        }
    }


    @Override
    public ListAdapter duplicate()
    {
        return new ContentValuesListAdapter(new ContentValues(mValues));
    }
}
