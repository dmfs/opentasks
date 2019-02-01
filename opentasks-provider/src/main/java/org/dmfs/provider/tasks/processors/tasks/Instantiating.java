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
import org.dmfs.jems.optional.elementary.NullSafe;
import org.dmfs.jems.pair.Pair;
import org.dmfs.jems.single.Single;
import org.dmfs.jems.single.combined.Backed;
import org.dmfs.optional.Optional;
import org.dmfs.provider.tasks.TaskDatabaseHelper;
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
 * <p>
 * TODO: At present this does not support recurrence.
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

    private final static int INSTANCE_COUNT_LIMIT = 1000;


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
        createInstances(db, result, task.id());
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
                && !result.isUpdated(TaskAdapter.STATUS) && !updateRequested)
        {
            // date values didn't change and update not requested -> no need to update the instances table
            return result;
        }
        updateInstances(db, result, task.id());
        return result;
    }


    @Override
    public void delete(SQLiteDatabase db, TaskAdapter entityAdapter, boolean isSyncAdapter)
    {
        // Note: there is a database trigger which cleans the instances table automatically when a task is deleted
        mDelegate.delete(db, entityAdapter, isSyncAdapter);
    }


    /**
     * Creates instances for a new task.
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
    private void createInstances(SQLiteDatabase db, TaskAdapter taskAdapter, long id)
    {
        // TODO: only limit future instances
        for (Single<ContentValues> values : new Limited<>(INSTANCE_COUNT_LIMIT, new InstanceValuesIterable(taskAdapter)))
        {
            db.insert(TaskDatabaseHelper.Tables.INSTANCES, "", new TaskRelated(id, values).value());
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
    private void updateInstances(SQLiteDatabase db, TaskAdapter taskAdapter, long id)
    {
        // get a cursor of all existing instances
        final Cursor existingInstances = db.query(
                TaskDatabaseHelper.Tables.INSTANCE_VIEW,
                new String[] { TaskContract.Instances._ID, TaskContract.Instances.INSTANCE_ORIGINAL_TIME },
                String.format(Locale.ENGLISH, "%s = %d", TaskContract.Instances.TASK_ID, id),
                null,
                null,
                null,
                TaskContract.Instances.INSTANCE_ORIGINAL_TIME);


        /*
         * The goal of the code below is to update existing instances in place (as opposed to delete and recreate all instances). We do this for two reasons:
         * 1) efficiency, in most cases existing instances don't change, deleting and recreating them would be very expensive
         * 2) stable row ids, deleting and recreating instances would change their id and void any existing URIs to them
         */
        try
        {
            int idIdx = existingInstances.getColumnIndex(TaskContract.Instances._ID);
            final int startIdx = existingInstances.getColumnIndex(TaskContract.Instances.INSTANCE_ORIGINAL_TIME);

            // get an Iterator of all expected instances
            // for very long or even infinite series we need to stop iterating at some point.
            // TODO: once we actually support recurrence we should only count future instances

            Iterable<Pair<Optional<ContentValues>, Optional<Integer>>> diff = new Diff<>(
                    new Mapped<>(Single::value, new Limited<>(INSTANCE_COUNT_LIMIT, new InstanceValuesIterable(taskAdapter))),
                    new Range(existingInstances.getCount()),
                    (newInstanceValues, cursorRow) ->
                    {
                        existingInstances.moveToPosition(cursorRow);
                        return (int) (existingInstances.getLong(startIdx) -
                                new Backed<>(
                                        new NullSafe<>(newInstanceValues.getAsLong(TaskContract.Instances.INSTANCE_ORIGINAL_TIME)),
                                        0L).value());
                    });

            // sync the instances table with the new instances
            for (Pair<Optional<ContentValues>, Optional<Integer>> next : diff)
            {
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
                    db.insert(TaskDatabaseHelper.Tables.INSTANCES, "", values);
                }
                else // both sides are present
                {
                    // update this instance
                    existingInstances.moveToPosition(next.right().value());
                    // TODO: only update if something has changed
                    db.update(TaskDatabaseHelper.Tables.INSTANCES, next.left().value(),
                            String.format(Locale.ENGLISH, "%s = %d", TaskContract.Instances._ID, existingInstances.getLong(idIdx)), null);
                }
            }
        }
        finally
        {
            existingInstances.close();
        }
    }

}
