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

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.dmfs.jems.optional.decorators.Mapped;
import org.dmfs.optional.NullSafe;
import org.dmfs.provider.tasks.TaskDatabaseHelper;
import org.dmfs.provider.tasks.model.ContentValuesInstanceAdapter;
import org.dmfs.provider.tasks.model.CursorContentValuesTaskAdapter;
import org.dmfs.provider.tasks.model.InstanceAdapter;
import org.dmfs.provider.tasks.model.TaskAdapter;
import org.dmfs.provider.tasks.processors.EntityProcessor;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.tasks.contract.TaskContract;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * An instance {@link EntityProcessor} which delegates to the appropriate task {@link EntityProcessor}.
 *
 * @author Marten Gajda
 */
public final class TaskValueDelegate implements EntityProcessor<InstanceAdapter>
{

    private final EntityProcessor<TaskAdapter> mDelegate;


    public TaskValueDelegate(EntityProcessor<TaskAdapter> delegate)
    {
        mDelegate = delegate;
    }


    @Override
    public InstanceAdapter insert(SQLiteDatabase db, InstanceAdapter entityAdapter, boolean isSyncAdapter)
    {
        // for now inserts are just forwarded, but we have to make sure we return an InstanceAdapter which contains the current instance row _ID.
        // TODO: in case ORIGINAL_INSTANCE_ID refers to a master tasks and ORIGINAL_INSTANCE_TIME is valid we should add an RDATE to it and remove any EXDATE for this
        // TODO: handle when inserting an instance that already exists, either update the existing override or fail
        TaskAdapter taskResult = mDelegate.insert(db, entityAdapter.taskAdapter(), false);
        try (Cursor c = db.query(TaskDatabaseHelper.Tables.INSTANCES, new String[] { TaskContract.Instances._ID },
                TaskContract.Instances.TASK_ID + "=" + taskResult.id(), null, null, null, null))
        {
            // the cursor should contain exactly one row after this operation
            c.moveToFirst();
            return new ContentValuesInstanceAdapter(c.getLong(0), new ContentValues());
        }
    }


    @Override
    public InstanceAdapter update(SQLiteDatabase db, InstanceAdapter entityAdapter, boolean isSyncAdapter)
    {
        // if this is the master of a recurring task, we create a new instance or update an existing one for this override, otherwise we just delegate
        TaskAdapter taskAdapter = entityAdapter.taskAdapter();
        if (taskAdapter.isRecurring())
        {
            // clone the task to create an unsynced override
            InstanceAdapter newInstanceAdapter = entityAdapter.duplicate();
            TaskAdapter override = newInstanceAdapter.taskAdapter();
            override.set(TaskAdapter.ORIGINAL_INSTANCE_ID, entityAdapter.valueOf(InstanceAdapter.TASK_ID));
            override.set(TaskAdapter.ORIGINAL_INSTANCE_TIME, entityAdapter.valueOf(InstanceAdapter.INSTANCE_ORIGINAL_TIME));
            override.unset(TaskAdapter.SYNC1);
            override.unset(TaskAdapter.SYNC2);
            override.unset(TaskAdapter.SYNC3);
            override.unset(TaskAdapter.SYNC4);
            override.unset(TaskAdapter.SYNC5);
            override.unset(TaskAdapter.SYNC6);
            override.unset(TaskAdapter.SYNC7);
            override.unset(TaskAdapter.SYNC8);
            override.unset(TaskAdapter.SYNC_ID);
            override.unset(TaskAdapter.SYNC_VERSION);
            // unset any list and read-only fields
            override.unset(TaskAdapter.ACCOUNT_NAME);
            override.unset(TaskAdapter.ACCOUNT_TYPE);
            override.unset(TaskAdapter.LIST_VISIBLE);
            override.unset(TaskAdapter.LIST_COLOR);
            override.unset(TaskAdapter.LIST_NAME);
            override.unset(TaskAdapter.LIST_ACCESS_LEVEL);
            override.unset(TaskAdapter.LIST_OWNER);
            override.unset(TaskAdapter._DELETED);
            override.unset(TaskAdapter._DIRTY);
            override.unset(TaskAdapter.IS_NEW);
            override.unset(TaskAdapter.IS_CLOSED);
            override.unset(TaskAdapter.HAS_PROPERTIES);
            override.unset(TaskAdapter.HAS_ALARMS);
            override.unset(TaskAdapter.ORIGINAL_INSTANCE_SYNC_ID); /* this will be resolved automatically */
            // also unset any recurrence fields
            override.unset(TaskAdapter.RRULE);
            override.unset(TaskAdapter.RDATE);
            override.unset(TaskAdapter.EXDATE);
            // finally make sure we update DTSTART and DUE to match the instance values (unless they are set explicitly)
            if (!taskAdapter.isUpdated(TaskAdapter.DTSTART))
            {
                // set DTSTART to the instance start
                override.set(TaskAdapter.DTSTART, newInstanceAdapter.valueOf(InstanceAdapter.INSTANCE_START));
            }
            if (!taskAdapter.isUpdated(TaskAdapter.DUE) && !taskAdapter.isUpdated(TaskAdapter.DURATION))
            {
                // set DUE to the effective instance DUE and wipe any duration
                override.set(TaskAdapter.DUE, newInstanceAdapter.valueOf(InstanceAdapter.INSTANCE_DUE));
                override.set(TaskAdapter.DURATION, null);
            }
            // set the correct original instance allday flag
            override.set(TaskAdapter.ORIGINAL_INSTANCE_ALLDAY, taskAdapter.valueOf(TaskAdapter.IS_ALLDAY));

            // TODO: if this is the first instance (and maybe no other overrides exist), don't create an override but split the series into two tasks
            mDelegate.insert(db, override, true /* for now insert as a sync adapter to retain the UID */);
        }
        else
        {
            // this is a non-recurring task or it's already an override, just delegate the update
            mDelegate.update(db, taskAdapter, false);
        }
        return entityAdapter;
    }


    @Override
    public void delete(SQLiteDatabase db, InstanceAdapter entityAdapter, boolean isSyncAdapter)
    {
        // deleted instances are converted to deleted tasks (for non-recurring tasks) or exdates (for recurring tasks).
        TaskAdapter taskAdapter = entityAdapter.taskAdapter();

        if (taskAdapter.valueOf(TaskAdapter.ORIGINAL_INSTANCE_ID) != null)
        {
            /* this is an override - we have to:
             * - mark it deleted
             * - add an exclusion to the master task
             *
             * TODO: if this instance was added by an RDATE, just remove the RDATE
             * TODO: if this is the first instance, consider moving the recurrence start instead of adding an exdate
             * TODO: if this is the last instance of a finite task, consider just setting a new recurrence end
             */
            long masterTaskId = taskAdapter.valueOf(TaskAdapter.ORIGINAL_INSTANCE_ID);
            DateTime originalTime = entityAdapter.valueOf(InstanceAdapter.INSTANCE_ORIGINAL_TIME);

            // delete the override
            mDelegate.delete(db, taskAdapter, false);

            // get the master and add an exdate
            try (Cursor c = db.query(TaskDatabaseHelper.Tables.TASKS, null /* all */, TaskContract.Tasks._ID + "=" + masterTaskId, null, null, null, null))
            {
                if (c.moveToFirst())
                {
                    TaskAdapter masterTaskAdapter = new CursorContentValuesTaskAdapter(masterTaskId, c, new ContentValues());
                    addExDate(masterTaskAdapter, originalTime);
                    mDelegate.update(db, masterTaskAdapter, false);
                }
            }
        }
        else if (taskAdapter.isRecurring())
        {
            // TODO: if this is the first instance, consider moving the recurrence start instead of adding an exdate
            // TODO: if this is the last instance of a finite task, consider just setting a new recurrence end
            addExDate(taskAdapter, entityAdapter.valueOf(InstanceAdapter.INSTANCE_ORIGINAL_TIME));
            mDelegate.update(db, taskAdapter, false);
        }
        else
        {
            // task is non-recurring, delete it as a non-sync-adapter (effectively setting the _deleted flag)
            mDelegate.delete(db, taskAdapter, false);
        }
    }


    private void addExDate(TaskAdapter taskAdapter, DateTime exdate)
    {
        List<DateTime> exdates = new Mapped<>(Arrays::asList, new NullSafe<>(taskAdapter.valueOf(TaskAdapter.EXDATE))).value(new ArrayList<>(1));
        exdates.add(exdate);
        taskAdapter.set(TaskAdapter.EXDATE, exdates.toArray(new DateTime[exdates.size()]));
    }
}
