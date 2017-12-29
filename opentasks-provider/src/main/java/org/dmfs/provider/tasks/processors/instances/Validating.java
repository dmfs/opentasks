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

package org.dmfs.provider.tasks.processors.instances;

import android.database.sqlite.SQLiteDatabase;

import org.dmfs.provider.tasks.model.InstanceAdapter;
import org.dmfs.provider.tasks.model.TaskAdapter;
import org.dmfs.provider.tasks.processors.EntityProcessor;


/**
 * An {@link EntityProcessor} which validates the instance data.
 *
 * @author Marten Gajda
 */
public final class Validating implements EntityProcessor<InstanceAdapter>
{
    private final EntityProcessor<InstanceAdapter> mDelegate;


    public Validating(EntityProcessor<InstanceAdapter> delegate)
    {
        mDelegate = delegate;
    }


    @Override
    public InstanceAdapter insert(SQLiteDatabase db, InstanceAdapter entityAdapter, boolean isSyncAdapter)
    {
        validate(entityAdapter, isSyncAdapter);
        return mDelegate.insert(db, entityAdapter, isSyncAdapter);
    }


    @Override
    public InstanceAdapter update(SQLiteDatabase db, InstanceAdapter entityAdapter, boolean isSyncAdapter)
    {
        validate(entityAdapter, isSyncAdapter);
        return mDelegate.update(db, entityAdapter, isSyncAdapter);
    }


    @Override
    public void delete(SQLiteDatabase db, InstanceAdapter entityAdapter, boolean isSyncAdapter)
    {
        mDelegate.delete(db, entityAdapter, isSyncAdapter);
    }


    private void validate(InstanceAdapter instanceAdapter, boolean isSyncAdapter)
    {
        if (isSyncAdapter)
        {
            throw new UnsupportedOperationException("At present, sync adapters are not expected to write to the instances table.");
        }

        // actually, no instance value can be changed, the instance table only allows for updating task values
        if (instanceAdapter.isUpdated(InstanceAdapter._ID) ||
                instanceAdapter.isUpdated(InstanceAdapter.INSTANCE_START) ||
                instanceAdapter.isUpdated(InstanceAdapter.INSTANCE_START_SORTING) ||
                instanceAdapter.isUpdated(InstanceAdapter.INSTANCE_DUE) ||
                instanceAdapter.isUpdated(InstanceAdapter.INSTANCE_DUE_SORTING) ||
                instanceAdapter.isUpdated(InstanceAdapter.INSTANCE_ORIGINAL_TIME) ||
                instanceAdapter.isUpdated(InstanceAdapter.DISTANCE_FROM_CURRENT) ||
                instanceAdapter.isUpdated(InstanceAdapter.TASK_ID))
        {
            throw new IllegalArgumentException("Instance columns are read-only.");
        }

        TaskAdapter taskAdapter = instanceAdapter.taskAdapter();
        // By definition, single instances don't have a recurrence set on their own, hence changes to the recurrence fields are not allowed.
        if (taskAdapter.isUpdated(TaskAdapter.RRULE) ||
                taskAdapter.isUpdated(TaskAdapter.RDATE) ||
                taskAdapter.isUpdated(TaskAdapter.EXDATE))
        {
            throw new IllegalArgumentException("Recurrence values can not be modified through the instances table.");
        }
    }
}
