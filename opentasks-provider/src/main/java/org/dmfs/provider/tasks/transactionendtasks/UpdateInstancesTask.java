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

package org.dmfs.provider.tasks.transactionendtasks;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.dmfs.provider.tasks.TaskDatabaseHelper;
import org.dmfs.provider.tasks.model.CursorContentValuesTaskAdapter;
import org.dmfs.provider.tasks.model.TaskAdapter;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.Duration;
import org.dmfs.tasks.contract.TaskContract;

import java.util.Locale;
import java.util.TimeZone;


/**
 * A {@link TransactionEndTask} which creates/updates the instances of all tasks which have been flagged as stale.
 *
 * TODO: handle recurrence
 *
 * @author Marten Gajda
 */
public final class UpdateInstancesTask implements TransactionEndTask
{
    @Override
    public void execute(SQLiteDatabase database)
    {
        // read all tasks with stale instances
        Cursor staleInstances = database.query(
                TaskDatabaseHelper.Tables.TASKS,
                null,
                String.format(Locale.ENGLISH, "%s = 1 ", TaskContract.Tasks.INSTANCES_STALE),
                null,
                null,
                null,
                null);

        try
        {
            ContentValues clearInstanceVoid = new ContentValues(1);
            clearInstanceVoid.put(TaskContract.Tasks.INSTANCES_STALE, 0);
            while (staleInstances.moveToNext())
            {
                TaskAdapter task = new CursorContentValuesTaskAdapter(staleInstances, new ContentValues());
                ContentValues instanceValues = generateInstanceValues(task);
                // try updating any existing instance
                if (database.update(TaskDatabaseHelper.Tables.INSTANCES, instanceValues,
                        String.format(Locale.ENGLISH, "%s = %d", TaskContract.Instances.TASK_ID, task.id()), null) == 0)
                {
                    // if no instances were updated, insert a new one
                    instanceValues.put(TaskContract.Instances.TASK_ID, task.id());
                    database.insert(TaskDatabaseHelper.Tables.INSTANCES, "", instanceValues);
                }

                // wipe the stale instances flag of the task
                database.update(TaskDatabaseHelper.Tables.TASKS, clearInstanceVoid, String.format(Locale.ENGLISH, "%s = %d", TaskContract.Tasks._ID, task.id()),
                        null);
            }
        }
        finally
        {
            staleInstances.close();
        }
    }


    /**
     * Create new {@link ContentValues} for insertion into the instances table.
     *
     * @param task
     *         The {@link TaskAdapter} of the task that's about to be inserted.
     *
     * @return {@link ContentValues} of the instance of this task.
     */
    private ContentValues generateInstanceValues(TaskAdapter task)
    {
        ContentValues instanceValues = new ContentValues();

        // get the relevant values from values
        DateTime dtstart = task.valueOf(TaskAdapter.DTSTART);
        DateTime due = task.valueOf(TaskAdapter.DUE);
        Duration duration = task.valueOf(TaskAdapter.DURATION);

        TimeZone localTz = TimeZone.getDefault();

        if (dtstart != null)
        {
            // copy dtstart as is
            instanceValues.put(TaskContract.Instances.INSTANCE_START, dtstart.getTimestamp());
            instanceValues.put(TaskContract.Instances.INSTANCE_START_SORTING,
                    dtstart.isAllDay() ? dtstart.getInstance() : dtstart.shiftTimeZone(localTz).getInstance());
        }
        else
        {
            instanceValues.putNull(TaskContract.Instances.INSTANCE_START);
            instanceValues.putNull(TaskContract.Instances.INSTANCE_START_SORTING);
        }

        if (due != null)
        {
            // copy due and calculate the actual duration, if any
            instanceValues.put(TaskContract.Instances.INSTANCE_DUE, due.getTimestamp());
            instanceValues.put(TaskContract.Instances.INSTANCE_DUE_SORTING, due.isAllDay() ? due.getInstance() : due.shiftTimeZone(localTz).getInstance());
            if (dtstart != null)
            {
                instanceValues.put(TaskContract.Instances.INSTANCE_DURATION, due.getTimestamp() - dtstart.getTimestamp());
            }
            else
            {
                instanceValues.putNull(TaskContract.Instances.INSTANCE_DURATION);
            }
        }
        else if (duration != null)
        {
            if (dtstart != null)
            {
                // calculate the actual due value from dtstart and the duration string
                due = dtstart.addDuration(duration);
                instanceValues.put(TaskContract.Instances.INSTANCE_DUE, due.getTimestamp());
                instanceValues.put(TaskContract.Instances.INSTANCE_DUE_SORTING, due.isAllDay() ? due.getInstance() : due.shiftTimeZone(localTz).getInstance());
                instanceValues.put(TaskContract.Instances.INSTANCE_DURATION, due.getTimestamp() - dtstart.getTimestamp());
            }
            else
            {
                // this case should be filtered by TaskValidatorProcessor, since setting a DURATION without DTSTART is invalid
                instanceValues.putNull(TaskContract.Instances.INSTANCE_DURATION);
                instanceValues.putNull(TaskContract.Instances.INSTANCE_DUE);
                instanceValues.putNull(TaskContract.Instances.INSTANCE_DUE_SORTING);
            }
        }
        else
        {
            instanceValues.putNull(TaskContract.Instances.INSTANCE_DURATION);
            instanceValues.putNull(TaskContract.Instances.INSTANCE_DUE);
            instanceValues.putNull(TaskContract.Instances.INSTANCE_DUE_SORTING);
        }
        return instanceValues;
    }
}
