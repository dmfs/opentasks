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

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.dmfs.iterables.decorators.Sieved;
import org.dmfs.iterables.elementary.Seq;
import org.dmfs.optional.First;
import org.dmfs.optional.NullSafe;
import org.dmfs.optional.Optional;
import org.dmfs.provider.tasks.TaskDatabaseHelper;
import org.dmfs.provider.tasks.model.InstanceAdapter;
import org.dmfs.provider.tasks.model.TaskAdapter;
import org.dmfs.provider.tasks.model.adapters.FieldAdapter;
import org.dmfs.provider.tasks.processors.EntityProcessor;
import org.dmfs.tasks.contract.TaskContract;

import java.util.Locale;


/**
 * An {@link EntityProcessor} which validates the instance data.
 *
 * @author Marten Gajda
 */
public final class Validating implements EntityProcessor<InstanceAdapter>
{
    private final static Iterable<FieldAdapter<?, InstanceAdapter>> INSTANCE_FIELD_ADAPTERS = new Seq<>(
            InstanceAdapter._ID,
            InstanceAdapter.INSTANCE_START,
            InstanceAdapter.INSTANCE_START_SORTING,
            InstanceAdapter.INSTANCE_DUE,
            InstanceAdapter.INSTANCE_DUE_SORTING,
            InstanceAdapter.INSTANCE_ORIGINAL_TIME,
            InstanceAdapter.DISTANCE_FROM_CURRENT,
            InstanceAdapter.TASK_ID);

    private final static Iterable<FieldAdapter<?, TaskAdapter>> RECURRENCE_FIELD_ADAPTERS = new Seq<>(
            TaskAdapter.RRULE,
            TaskAdapter.RDATE,
            TaskAdapter.EXDATE);

    private final EntityProcessor<InstanceAdapter> mDelegate;


    public Validating(EntityProcessor<InstanceAdapter> delegate)
    {
        mDelegate = delegate;
    }


    @Override
    public InstanceAdapter insert(SQLiteDatabase db, InstanceAdapter entityAdapter, boolean isSyncAdapter)
    {
        validate(entityAdapter, isSyncAdapter);

        Optional<Long> instanceId = new NullSafe<>(entityAdapter.taskAdapter().valueOf(TaskAdapter.ORIGINAL_INSTANCE_ID));
        if (instanceId.isPresent())
        {
            String timeStampString = Long.toString(entityAdapter.taskAdapter().valueOf(TaskAdapter.ORIGINAL_INSTANCE_TIME).getTimestamp());
            // there better be no instance for this yet, otherwise this is going to fail
            try (Cursor c = db.query(
                    TaskDatabaseHelper.Tables.INSTANCE_VIEW,
                    new String[] { TaskContract.Instances._ID },
                    // find any instance which refers to the given original ID and has the same instance time
                    // for recurring tasks this matches the INSTANCE_ORIGINAL_TIME, for non-recurring tasks this matches start or due (whichever is present).
                    String.format("(%1$s == ? or %2$s == ?) and (%3$s == ? or %3$s is null and %4$s == ? or %3$s is null and %4$s is null and %5$s == ?) ",
                            TaskContract.Instances.TASK_ID,
                            TaskContract.Instances.ORIGINAL_INSTANCE_ID,
                            TaskContract.Instances.INSTANCE_ORIGINAL_TIME,
                            TaskContract.Instances.INSTANCE_START,
                            TaskContract.Instances.INSTANCE_DUE),
                    new String[] {
                            instanceId.value().toString(),
                            instanceId.value().toString(),
                            timeStampString,
                            timeStampString,
                            timeStampString },
                    null,
                    null,
                    null))
            {
                if (c.getCount() > 0)
                {
                    throw new UnsupportedOperationException(String.format(Locale.ENGLISH, "Instance %s of task %d already exists",
                            entityAdapter.taskAdapter().valueOf(TaskAdapter.ORIGINAL_INSTANCE_TIME).toString(), instanceId.value()));
                }
            }
        }
        return mDelegate.insert(db, entityAdapter, false);
    }


    @Override
    public InstanceAdapter update(SQLiteDatabase db, InstanceAdapter entityAdapter, boolean isSyncAdapter)
    {
        validate(entityAdapter, isSyncAdapter);
        return mDelegate.update(db, entityAdapter, false);
    }


    @Override
    public void delete(SQLiteDatabase db, InstanceAdapter entityAdapter, boolean isSyncAdapter)
    {
        if (isSyncAdapter)
        {
            throw new UnsupportedOperationException("Sync adapters are not expected to write to the instances table.");
        }
        mDelegate.delete(db, entityAdapter, false);
    }


    private void validate(InstanceAdapter instanceAdapter, boolean isSyncAdapter)
    {
        if (isSyncAdapter)
        {
            throw new UnsupportedOperationException("Sync adapters are not expected to write to the instances table.");
        }

        // actually, no instance value can be changed, the instance table only allows for updating task values
        if (new First<>(new Sieved<>(instanceAdapter::isUpdated, INSTANCE_FIELD_ADAPTERS)).isPresent())
        {
            throw new IllegalArgumentException("Instance columns are read-only.");
        }

        TaskAdapter taskAdapter = instanceAdapter.taskAdapter();
        // By definition, single instances don't have a recurrence set on their own, hence changes to the recurrence fields are not allowed.
        if (new First<>(new Sieved<>(taskAdapter::isUpdated, RECURRENCE_FIELD_ADAPTERS)).isPresent())
        {
            throw new IllegalArgumentException("Recurrence values can not be modified through the instances table.");
        }
    }
}
