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
public class ContentValuesInstanceAdapter extends AbstractInstanceAdapter
{
    private long mId;
    private final ContentValues mValues;


    public ContentValuesInstanceAdapter(ContentValues values)
    {
        this(-1L, values);
    }


    public ContentValuesInstanceAdapter(long id, ContentValues values)
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
    public <T> T valueOf(FieldAdapter<T, InstanceAdapter> fieldAdapter)
    {
        return fieldAdapter.getFrom(mValues);
    }


    @Override
    public <T> T oldValueOf(FieldAdapter<T, InstanceAdapter> fieldAdapter)
    {
        return null;
    }


    @Override
    public boolean isUpdated(FieldAdapter<?, InstanceAdapter> fieldAdapter)
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
        return new ContentValuesInstanceAdapter(new ContentValues(mValues));
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

        return new ContentValuesTaskAdapter(values);
    }
}
