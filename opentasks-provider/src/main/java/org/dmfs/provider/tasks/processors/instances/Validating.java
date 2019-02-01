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
import org.dmfs.jems.optional.Optional;
import org.dmfs.jems.optional.adapters.First;
import org.dmfs.jems.optional.elementary.NullSafe;
import org.dmfs.provider.tasks.TaskDatabaseHelper;
import org.dmfs.provider.tasks.model.InstanceAdapter;
import org.dmfs.provider.tasks.model.TaskAdapter;
import org.dmfs.provider.tasks.model.adapters.FieldAdapter;
import org.dmfs.provider.tasks.processors.EntityProcessor;
import org.dmfs.rfc5545.DateTime;
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

    private static final Iterable<FieldAdapter<?, TaskAdapter>> ORIGINAL_INSTANCE_FIELD_ADAPTERS = new Seq<>(
            TaskAdapter.ORIGINAL_INSTANCE_ID,
            TaskAdapter.ORIGINAL_INSTANCE_TIME,
            TaskAdapter.ORIGINAL_INSTANCE_ALLDAY,
            TaskAdapter.ORIGINAL_INSTANCE_SYNC_ID);

    private final EntityProcessor<InstanceAdapter> mDelegate;


    public Validating(EntityProcessor<InstanceAdapter> delegate)
    {
        mDelegate = delegate;
    }


    @Override
    public InstanceAdapter insert(SQLiteDatabase db, InstanceAdapter entityAdapter, boolean isSyncAdapter)
    {
        validateIsSyncAdapter(isSyncAdapter);
        validateValues(entityAdapter);
        validateInstanceIsNew(db, entityAdapter);

        return mDelegate.insert(db, entityAdapter, false);
    }


    @Override
    public InstanceAdapter update(SQLiteDatabase db, InstanceAdapter entityAdapter, boolean isSyncAdapter)
    {
        validateIsSyncAdapter(isSyncAdapter);
        validateValues(entityAdapter);
        validateOriginalInstanceValues(entityAdapter);
        return mDelegate.update(db, entityAdapter, false);
    }


    @Override
    public void delete(SQLiteDatabase db, InstanceAdapter entityAdapter, boolean isSyncAdapter)
    {
        validateIsSyncAdapter(isSyncAdapter);
        mDelegate.delete(db, entityAdapter, false);
    }


    private void validateIsSyncAdapter(boolean isSyncAdapter)
    {
        if (isSyncAdapter)
        {
            throw new UnsupportedOperationException("Sync adapters are not expected to write to the instances table.");
        }
    }


    private void validateInstanceIsNew(SQLiteDatabase db, InstanceAdapter entityAdapter)
    {
        Optional<Long> instanceId = new NullSafe<>(entityAdapter.taskAdapter().valueOf(TaskAdapter.ORIGINAL_INSTANCE_ID));
        Optional<DateTime> instanceTime = new NullSafe<>(entityAdapter.taskAdapter().valueOf(TaskAdapter.ORIGINAL_INSTANCE_TIME));

        // check if ORIGINAL_INSTANCE_ID and ORIGINAL_INSTANCE_TIME are both present/absent at the same time
        if (instanceId.isPresent() != instanceTime.isPresent())
        {
            throw new IllegalArgumentException(String.format("%s and %s must either be both absent or both present",
                    TaskContract.Tasks.ORIGINAL_INSTANCE_ID, TaskContract.Tasks.ORIGINAL_INSTANCE_TIME));
        }

        if (instanceId.isPresent())
        {
            String timeStampString = Long.toString(instanceTime.value().getTimestamp());
            // Make sure there is no instance at the given time already
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
                    throw new IllegalArgumentException(String.format(Locale.ENGLISH, "Instance %s of task %d already exists",
                            entityAdapter.taskAdapter().valueOf(TaskAdapter.ORIGINAL_INSTANCE_TIME).toString(), instanceId.value()));
                }
            }
        }
    }


    private void validateValues(InstanceAdapter instanceAdapter)
    {
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


    private void validateOriginalInstanceValues(InstanceAdapter instanceAdapter)
    {
        TaskAdapter taskAdapter = instanceAdapter.taskAdapter();
        // Updates of ORIGINAL_INSTANCE_* fields are not allowed
        if (new First<>(new Sieved<>(taskAdapter::isUpdated, ORIGINAL_INSTANCE_FIELD_ADAPTERS)).isPresent())
        {
            throw new IllegalArgumentException("ORIGINAL_INSTANCE_* fields can not be updated through the instances table.");
        }
    }
}
