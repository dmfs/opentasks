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

package org.dmfs.provider.tasks.processors.tasks;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.dmfs.jems.iterable.composite.Diff;
import org.dmfs.jems.iterable.decorators.Mapped;
import org.dmfs.jems.optional.Optional;
import org.dmfs.jems.optional.elementary.NullSafe;
import org.dmfs.jems.pair.Pair;
import org.dmfs.jems.pair.elementary.RightSidedPair;
import org.dmfs.jems.single.Single;
import org.dmfs.jems.single.combined.Backed;
import org.dmfs.provider.tasks.TaskDatabaseHelper;
import org.dmfs.provider.tasks.model.CursorContentValuesTaskAdapter;
import org.dmfs.provider.tasks.model.TaskAdapter;
import org.dmfs.provider.tasks.model.adapters.BooleanFieldAdapter;
import org.dmfs.provider.tasks.processors.EntityProcessor;
import org.dmfs.provider.tasks.processors.tasks.instancedata.TaskRelated;
import org.dmfs.provider.tasks.utils.InstanceValuesIterable;
import org.dmfs.provider.tasks.utils.Limited;
import org.dmfs.provider.tasks.utils.Range;
import org.dmfs.tasks.contract.TaskContract;

import java.util.Locale;


/**
 * A processor that creates or updates the instance values of a task.
 *
 * @author Marten Gajda
 */
public final class Instantiating implements EntityProcessor<TaskAdapter>
{

    /**
     * This is a field adapter for a pseudo column to indicate that the instances may need an update, even if no relevant value has changed. This is useful to
     * force an update of the sorting values when the local timezone has been changed.
     * <p>
     * TODO: get rid of it
     */
    private final static BooleanFieldAdapter<TaskAdapter> UPDATE_REQUESTED = new BooleanFieldAdapter<TaskAdapter>(
            "org.dmfs.tasks.TaskInstanceProcessor.UPDATE_REQUESTED");

    // for now we only expand the next upcoming instance
    private final static int UPCOMING_INSTANCE_COUNT_LIMIT = 1;


    /**
     * Add a pseudo column to the given {@link ContentValues} to request an instances update, even if no time value has changed.
     *
     * @param values
     *         The {@link ContentValues} to add the pseudo column to.
     */
    public static void addUpdateRequest(ContentValues values)
    {
        UPDATE_REQUESTED.setIn(values, true);
    }


    private final EntityProcessor<TaskAdapter> mDelegate;


    public Instantiating(EntityProcessor<TaskAdapter> delegate)
    {
        mDelegate = delegate;
    }


    @Override
    public TaskAdapter insert(SQLiteDatabase db, TaskAdapter task, boolean isSyncAdapter)
    {
        TaskAdapter result = mDelegate.insert(db, task, isSyncAdapter);
        if (task.valueOf(TaskAdapter.ORIGINAL_INSTANCE_ID) != null)
        {
            // an override was created, insert a single task
            updateOverrideInstance(db, result, result.id());
        }
        else
        {
            // update the recurring instances, there may already be overrides, so we use the update method
            updateMasterInstances(db, result, result.id());
        }
        return result;
    }


    @Override
    public TaskAdapter update(SQLiteDatabase db, TaskAdapter task, boolean isSyncAdapter)
    {
        // TODO: get rid if this mechanism
        boolean updateRequested = task.isUpdated(UPDATE_REQUESTED) ? task.valueOf(UPDATE_REQUESTED) : false;
        task.unset(UPDATE_REQUESTED);

        TaskAdapter result = mDelegate.update(db, task, isSyncAdapter);

        if (!result.isUpdated(TaskAdapter.DTSTART) && !result.isUpdated(TaskAdapter.DUE) && !result.isUpdated(TaskAdapter.DURATION)
                && !result.isUpdated(TaskAdapter.STATUS) && !result.isUpdated(TaskAdapter.RDATE) && !result.isUpdated(TaskAdapter.RRULE) && !result.isUpdated(
                TaskAdapter.EXDATE) && !result.isUpdated(TaskAdapter.IS_CLOSED) && !updateRequested)
        {
            // date values didn't change and update not requested -> no need to update the instances table
            return result;
        }
        if (task.valueOf(TaskAdapter.ORIGINAL_INSTANCE_ID) == null)
        {
            updateMasterInstances(db, result, result.id());
        }
        else
        {
            updateOverrideInstance(db, result, result.id());
        }
        return result;
    }


    @Override
    public void delete(SQLiteDatabase db, TaskAdapter entityAdapter, boolean isSyncAdapter)
    {
        // Note: there is a database trigger which cleans the instances table automatically when a task is deleted
        mDelegate.delete(db, entityAdapter, isSyncAdapter);
    }


    /**
     * Update the instance of an override.
     * <p>
     * TODO: take instance overrides into account
     *
     * @param db
     *         an {@link SQLiteDatabase}.
     * @param taskAdapter
     *         the {@link TaskAdapter} of the task to insert.
     * @param id
     *         the row id of the new task.
     */
    private void updateOverrideInstance(SQLiteDatabase db, TaskAdapter taskAdapter, long id)
    {
        long origId = taskAdapter.valueOf(TaskAdapter.ORIGINAL_INSTANCE_ID);
        int count = 0;
        for (Single<ContentValues> values : new InstanceValuesIterable(taskAdapter))
        {
            if (count++ > 1)
            {
                throw new RuntimeException("more than one instance returned for task which was supposed to have exactly one");
            }
            try (Cursor c = db.query(TaskDatabaseHelper.Tables.INSTANCE_VIEW, new String[] { TaskContract.Instances._ID },
                    String.format(Locale.ENGLISH, "(%s = %d or %s = %d) and (%s = %d) ",
                            TaskContract.Instances.TASK_ID,
                            origId,
                            TaskContract.Instances.ORIGINAL_INSTANCE_ID,
                            origId,
                            TaskContract.Instances.INSTANCE_ORIGINAL_TIME,
                            taskAdapter.valueOf(TaskAdapter.ORIGINAL_INSTANCE_TIME).getTimestamp()),
                    null, null, null, null))
            {
                if (c.moveToFirst())
                {
                    db.update(TaskDatabaseHelper.Tables.INSTANCES, new TaskRelated(id, values).value(), String.format(Locale.ENGLISH, "%s = %d",
                            TaskContract.Instances._ID, c.getLong(0)), null);
                }
                else
                {
                    db.insert(TaskDatabaseHelper.Tables.INSTANCES, "", new TaskRelated(id, values).value());
                }
            }
        }
        if (count == 0)
        {
            throw new RuntimeException("no instance returned for task which was supposed to have exactly one");
        }

        // ensure the distance from current is set properly for all sibling instances
        try (Cursor c = db.query(TaskDatabaseHelper.Tables.TASKS, null,
                String.format(Locale.ENGLISH, "(%s = %d)", TaskContract.Tasks._ID, origId), null, null, null, null))
        {
            if (c.moveToFirst())
            {
                TaskAdapter ta = new CursorContentValuesTaskAdapter(c, new ContentValues());
                updateMasterInstances(db, ta, ta.id());
            }
        }
    }


    /**
     * Updates the instances of an existing task
     * <p>
     * TODO: take instance overrides into account
     *
     * @param db
     *         An {@link SQLiteDatabase}.
     * @param taskAdapter
     *         the {@link TaskAdapter} of the task to update
     * @param id
     *         the row id of the new task
     */
    private void updateMasterInstances(SQLiteDatabase db, TaskAdapter taskAdapter, long id)
    {
        final Cursor existingInstances = db.query(
                TaskDatabaseHelper.Tables.INSTANCE_VIEW,
                new String[] {
                        TaskContract.Instances._ID, TaskContract.Instances.INSTANCE_ORIGINAL_TIME, TaskContract.Instances.TASK_ID,
                        TaskContract.Instances.IS_CLOSED, TaskContract.Instances.DISTANCE_FROM_CURRENT },
                String.format(Locale.ENGLISH, "%s = %d or %s = %d", TaskContract.Instances.TASK_ID, id, TaskContract.Instances.ORIGINAL_INSTANCE_ID, id),
                null,
                null,
                null,
                TaskContract.Instances.INSTANCE_ORIGINAL_TIME);

        /*
         * The goal of the code below is to update existing instances in place (as opposed to delete and recreate all instances). We do this for two reasons:
         * 1) efficiency, in most cases existing instances don't change, deleting and recreating them would be overly expensive
         * 2) stable row ids, deleting and recreating instances would change their id and void any existing URIs to them
         */
        try
        {
            final int idIdx = existingInstances.getColumnIndex(TaskContract.Instances._ID);
            final int startIdx = existingInstances.getColumnIndex(TaskContract.Instances.INSTANCE_ORIGINAL_TIME);
            final int taskIdIdx = existingInstances.getColumnIndex(TaskContract.Instances.TASK_ID);
            final int isClosedIdx = existingInstances.getColumnIndex(TaskContract.Instances.IS_CLOSED);
            final int distanceIdx = existingInstances.getColumnIndex(TaskContract.Instances.DISTANCE_FROM_CURRENT);

            // get an Iterator of all expected instances
            // for very long or even infinite series we need to stop iterating at some point.

            Iterable<Pair<Optional<ContentValues>, Optional<Integer>>> diff = new Diff<>(
                    new Mapped<>(Single::value,
                            new Limited<>(10000 /* hard limit for infinite rules*/, new InstanceValuesIterable(taskAdapter))),
                    new Range(existingInstances.getCount()),
                    (newInstanceValues, cursorRow) ->
                    {
                        existingInstances.moveToPosition(cursorRow);
                        return (int) (new Backed<>(new NullSafe<>(newInstanceValues.getAsLong(TaskContract.Instances.INSTANCE_ORIGINAL_TIME)), 0L).value()
                                - existingInstances.getLong(startIdx));
                    });

            int distance = -1;
            // sync the instances table with the new instances
            for (Pair<Optional<ContentValues>, Optional<Integer>> next : diff)
            {
                if (distance >= UPCOMING_INSTANCE_COUNT_LIMIT - 1)
                {
                    // if we already expanded enough instances, we pretend no other instance exists
                    if (!next.right().isPresent())
                    {
                        // actually no instance exists, no need to do anything
                        continue;
                    }
                    next = new RightSidedPair<>(next.right());
                }

                if (!next.left().isPresent())
                {
                    // there is no new instance for this old one, remove it
                    existingInstances.moveToPosition(next.right().value());
                    db.delete(TaskDatabaseHelper.Tables.INSTANCES,
                            String.format(Locale.ENGLISH, "%s = %d", TaskContract.Instances._ID, existingInstances.getLong(idIdx)), null);
                }
                else if (!next.right().isPresent())
                {
                    // there is no old instance for this new one, add it
                    ContentValues values = next.left().value();
                    values.put(TaskContract.Instances.TASK_ID, taskAdapter.id());
                    if (distance >= 0 || !taskAdapter.valueOf(TaskAdapter.IS_CLOSED))
                    {
                        distance += 1;
                    }
                    values.put(TaskContract.Instances.DISTANCE_FROM_CURRENT, distance);
                    db.insert(TaskDatabaseHelper.Tables.INSTANCES, "", values);
                }
                else // both sides are present
                {
                    // update this instance
                    existingInstances.moveToPosition(next.right().value());
                    // only update if the instance belongs to this task
                    if (existingInstances.getLong(taskIdIdx) == id)
                    {
                        ContentValues values = next.left().value();
                        if (distance >= 0 ||
                                taskAdapter.isUpdated(TaskAdapter.IS_CLOSED) && !taskAdapter.valueOf(TaskAdapter.IS_CLOSED) ||
                                !taskAdapter.isUpdated(TaskAdapter.IS_CLOSED) && existingInstances.getInt(isClosedIdx) == 0)
                        {
                            // the distance needs to be updated
                            distance += 1;
                            values.put(TaskContract.Instances.DISTANCE_FROM_CURRENT, distance);
                        }

                        // TODO: only update if something actually changed
                        db.update(TaskDatabaseHelper.Tables.INSTANCES, values,
                                String.format(Locale.ENGLISH, "%s = %d", TaskContract.Instances._ID, existingInstances.getLong(idIdx)), null);
                    }
                    else if (distance >= 0 || existingInstances.getInt(isClosedIdx) == 0)
                    {
                        // this is an override and we need to check the distance value
                        distance += 1;
                        if (distance != existingInstances.getInt(distanceIdx))
                        {
                            ContentValues contentValues = new ContentValues(1);
                            contentValues.put(TaskContract.Instances.DISTANCE_FROM_CURRENT, distance);
                            db.update(TaskDatabaseHelper.Tables.INSTANCES, contentValues,
                                    String.format(Locale.ENGLISH, "%s = %d", TaskContract.Instances._ID, existingInstances.getLong(idIdx)), null);
                        }
                    }
                }
            }
        }
        finally
        {
            existingInstances.close();
        }
    }

}
