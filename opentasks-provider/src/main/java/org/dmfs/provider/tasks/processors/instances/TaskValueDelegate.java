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
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

import org.dmfs.iterables.decorators.Filtered;
import org.dmfs.iterables.elementary.Seq;
import org.dmfs.iterators.filters.NoneOf;
import org.dmfs.jems.iterable.composite.Joined;
import org.dmfs.jems.optional.adapters.FirstPresent;
import org.dmfs.jems.optional.elementary.NullSafe;
import org.dmfs.jems.single.combined.Backed;
import org.dmfs.provider.tasks.TaskDatabaseHelper;
import org.dmfs.provider.tasks.handler.PropertyHandler;
import org.dmfs.provider.tasks.handler.PropertyHandlerFactory;
import org.dmfs.provider.tasks.model.ContentValuesInstanceAdapter;
import org.dmfs.provider.tasks.model.CursorContentValuesTaskAdapter;
import org.dmfs.provider.tasks.model.InstanceAdapter;
import org.dmfs.provider.tasks.model.TaskAdapter;
import org.dmfs.provider.tasks.model.adapters.FieldAdapter;
import org.dmfs.provider.tasks.processors.EntityProcessor;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.tasks.contract.TaskContract;

import java.util.Locale;


/**
 * An instance {@link EntityProcessor} which delegates to the appropriate task {@link EntityProcessor}.
 *
 * @author Marten Gajda
 */
public final class TaskValueDelegate implements EntityProcessor<InstanceAdapter>
{
    private final static Iterable<FieldAdapter<?, TaskAdapter>> SPECIAL_FIELD_ADAPTERS = new Seq<>(
            TaskAdapter.SYNC1,
            TaskAdapter.SYNC2,
            TaskAdapter.SYNC3,
            TaskAdapter.SYNC4,
            TaskAdapter.SYNC5,
            TaskAdapter.SYNC6,
            TaskAdapter.SYNC7,
            TaskAdapter.SYNC8,
            TaskAdapter.SYNC_ID,
            TaskAdapter.SYNC_VERSION,
            // unset any list and read-only fields
            TaskAdapter.VERSION,
            TaskAdapter.ACCOUNT_NAME,
            TaskAdapter.ACCOUNT_TYPE,
            TaskAdapter.LIST_VISIBLE,
            TaskAdapter.LIST_COLOR,
            TaskAdapter.LIST_NAME,
            TaskAdapter.LIST_ACCESS_LEVEL,
            TaskAdapter.LIST_OWNER,
            TaskAdapter._DELETED,
            TaskAdapter._DIRTY,
            TaskAdapter.IS_NEW,
            TaskAdapter.IS_CLOSED,
            TaskAdapter.HAS_PROPERTIES,
            TaskAdapter.HAS_ALARMS,
            TaskAdapter.ORIGINAL_INSTANCE_SYNC_ID, /* this will be resolved automatically */
            // also unset any recurrence fields
            TaskAdapter.RRULE,
            TaskAdapter.RDATE,
            TaskAdapter.EXDATE,
            TaskAdapter.CREATED,
            TaskAdapter.LAST_MODIFIED
    );

    private final EntityProcessor<TaskAdapter> mDelegate;


    public TaskValueDelegate(EntityProcessor<TaskAdapter> delegate)
    {
        mDelegate = delegate;
    }


    @Override
    public InstanceAdapter insert(SQLiteDatabase db, InstanceAdapter entityAdapter, boolean isSyncAdapter)
    {
        TaskAdapter taskAdapter = entityAdapter.taskAdapter();
        Long masterTaskId = null;
        if (taskAdapter.valueOf(TaskAdapter.ORIGINAL_INSTANCE_ID) != null)
        {
            // this is going to be an override to an existing task - make sure we add an RDATE first
            masterTaskId = taskAdapter.valueOf(TaskAdapter.ORIGINAL_INSTANCE_ID);
            DateTime originalTime = taskAdapter.valueOf(TaskAdapter.ORIGINAL_INSTANCE_TIME);
            // get the master and add an rdate
            try (Cursor c = db.query(TaskDatabaseHelper.Tables.TASKS, null /* all */, TaskContract.Tasks._ID + "=" + masterTaskId, null, null, null, null))
            {
                if (c.moveToFirst())
                {
                    TaskAdapter masterTaskAdapter = new CursorContentValuesTaskAdapter(masterTaskId, c, new ContentValues());
                    if (masterTaskAdapter.valueOf(TaskAdapter.ORIGINAL_INSTANCE_ID) != null)
                    {
                        throw new IllegalArgumentException("Can't add an instance to an override instance");
                    }
                    DateTime masterDate = new Backed<DateTime>(new FirstPresent<>(new Seq<>(
                            new NullSafe<>(masterTaskAdapter.valueOf(TaskAdapter.DTSTART)),
                            new NullSafe<>(masterTaskAdapter.valueOf(TaskAdapter.DUE)))), () -> null).value();
                    if (!masterTaskAdapter.isRecurring() && masterDate != null)
                    {
                        // master is not recurring yet, also add its start as an RDATE
                        appendDate(masterTaskAdapter, TaskAdapter.RDATE, TaskAdapter.EXDATE, masterDate);
                    }
                    // TODO: should we throw if the new master has no DTSTART?
                    appendDate(masterTaskAdapter, TaskAdapter.RDATE, TaskAdapter.EXDATE, originalTime);
                    mDelegate.update(db, masterTaskAdapter, false);

                }
                else
                {
                    throw new IllegalArgumentException(String.format(Locale.ENGLISH, "No task with _ID %d found", masterTaskId));
                }
            }
        }

        // move on with inserting the instance
        TaskAdapter taskResult = mDelegate.insert(db, entityAdapter.taskAdapter(), false);

        if (masterTaskId != null)
        {
            // we just cloned the master task into a new instance, we need to copy the properties as well
            copyProperties(db, masterTaskId, taskResult.id());
        }

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
            // unset all fields which have special meaning
            for (FieldAdapter<?, TaskAdapter> specialFieldAdapter : SPECIAL_FIELD_ADAPTERS)
            {
                override.unset(specialFieldAdapter);
            }

            // make sure we update DTSTART and DUE to match the instance values (unless they are set explicitly)
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
            // copy original instance allday flag
            override.set(TaskAdapter.ORIGINAL_INSTANCE_ALLDAY, taskAdapter.valueOf(TaskAdapter.IS_ALLDAY));

            TaskAdapter newTask = mDelegate.insert(db, override, false);

            copyProperties(db, taskAdapter.id(), newTask.id());
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
                    appendDate(masterTaskAdapter, TaskAdapter.EXDATE, TaskAdapter.RDATE, originalTime);
                    mDelegate.update(db, masterTaskAdapter, false);
                }
            }
        }
        else if (taskAdapter.isRecurring())
        {
            // TODO: if this is the first instance, consider moving the recurrence start instead of adding an exdate
            // TODO: if this is the last instance of a finite task, consider just setting a new recurrence end
            appendDate(taskAdapter, TaskAdapter.EXDATE, TaskAdapter.RDATE, entityAdapter.valueOf(InstanceAdapter.INSTANCE_ORIGINAL_TIME));
            mDelegate.update(db, taskAdapter, false);
        }
        else
        {
            // task is non-recurring, delete it as a non-sync-adapter (effectively setting the _deleted flag)
            mDelegate.delete(db, taskAdapter, false);
        }
    }


    private void appendDate(TaskAdapter taskAdapter, FieldAdapter<Iterable<DateTime>, TaskAdapter> addfieldAdapter, FieldAdapter<Iterable<DateTime>, TaskAdapter> removefieldAdapter, DateTime dateTime)
    {
        taskAdapter.set(addfieldAdapter, new Joined<>(new Filtered<>(taskAdapter.valueOf(addfieldAdapter), new NoneOf<>(dateTime)), new Seq<>(dateTime)));
        taskAdapter.set(removefieldAdapter, new Filtered<>(taskAdapter.valueOf(removefieldAdapter), new NoneOf<>(dateTime)));
    }


    /**
     * Copy the properties from the give original task to the new task.
     *
     * @param db
     *         The {@link SQLiteDatabase}
     * @param originalId
     *         The ID of the task of which to copy the properties
     * @param newId
     *         The ID of the task to copy the properties to.
     */
    private void copyProperties(SQLiteDatabase db, long originalId, long newId)
    {
        // for each property of the original task
        try (Cursor c = db.query(TaskDatabaseHelper.Tables.PROPERTIES, null /* all */,
                String.format(Locale.ENGLISH, "%s = %d", TaskContract.Properties.TASK_ID, originalId), null, null, null, null))
        {
            // load the property and insert it for the new task
            ContentValues values = new ContentValues(c.getColumnCount());
            while (c.moveToNext())
            {
                values.clear();
                DatabaseUtils.cursorRowToContentValues(c, values);
                PropertyHandler ph = PropertyHandlerFactory.get(values.getAsString(TaskContract.Properties.MIMETYPE));
                ph.insert(db, newId, ph.cloneForNewTask(newId, values), false);
            }
        }
    }
}
