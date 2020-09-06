/*
 * Copyright 2019 dmfs GmbH
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

import org.dmfs.iterables.SingletonIterable;
import org.dmfs.iterables.decorators.Sieved;
import org.dmfs.jems.iterable.composite.Joined;
import org.dmfs.jems.optional.adapters.FirstPresent;
import org.dmfs.jems.optional.elementary.NullSafe;
import org.dmfs.jems.predicate.composite.AnyOf;
import org.dmfs.jems.predicate.composite.Not;
import org.dmfs.provider.tasks.TaskDatabaseHelper;
import org.dmfs.provider.tasks.model.CursorContentValuesInstanceAdapter;
import org.dmfs.provider.tasks.model.CursorContentValuesTaskAdapter;
import org.dmfs.provider.tasks.model.InstanceAdapter;
import org.dmfs.provider.tasks.model.TaskAdapter;
import org.dmfs.provider.tasks.model.adapters.IntegerFieldAdapter;
import org.dmfs.provider.tasks.model.adapters.LongFieldAdapter;
import org.dmfs.provider.tasks.processors.EntityProcessor;
import org.dmfs.provider.tasks.utils.Timestamps;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.Duration;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import org.dmfs.rfc5545.recurrenceset.RecurrenceList;
import org.dmfs.rfc5545.recurrenceset.RecurrenceRuleAdapter;
import org.dmfs.rfc5545.recurrenceset.RecurrenceSet;
import org.dmfs.rfc5545.recurrenceset.RecurrenceSetIterator;
import org.dmfs.tasks.contract.TaskContract;

import java.util.HashSet;
import java.util.TimeZone;

import static java.util.Arrays.asList;


/**
 * An instance {@link EntityProcessor} detaches completed instances at the start of a recurring task.
 *
 * @author Marten Gajda
 */
public final class Detaching implements EntityProcessor<InstanceAdapter>
{

    private final EntityProcessor<InstanceAdapter> mDelegate;
    private final EntityProcessor<TaskAdapter> mTaskDelegate;


    public Detaching(EntityProcessor<InstanceAdapter> delegate, EntityProcessor<TaskAdapter> taskDelegate)
    {
        mDelegate = delegate;
        mTaskDelegate = taskDelegate;
    }


    @Override
    public InstanceAdapter insert(SQLiteDatabase db, InstanceAdapter entityAdapter, boolean isSyncAdapter)
    {
        // just delegate for now
        // if we ever support inserting instances, we'll have to make sure that inserting a completed instance results in a detached task
        return mDelegate.insert(db, entityAdapter, isSyncAdapter);
    }


    /**
     * Detach the given instance if all of the following conditions are met
     * <p>
     * - The instance is a recurrence instance (INSTANCE_ORIGINAL_TIME != null)
     * - and the task has been closed (IS_CLOSED != 0)
     * - and the instance is the first non-closed instance (DISTANCE_FROM_CURRENT==0).
     * <p>
     */
    @Override
    public InstanceAdapter update(SQLiteDatabase db, InstanceAdapter entityAdapter, boolean isSyncAdapter)
    {
        if (entityAdapter.valueOf(InstanceAdapter.DISTANCE_FROM_CURRENT) != 0 // not the first open task

                // not closed, note we can't use IS_CLOSED at this point because its not updated yet
                || (!new HashSet<>(asList(TaskContract.Tasks.STATUS_COMPLETED, TaskContract.Tasks.STATUS_CANCELLED)).contains(
                entityAdapter.valueOf(new IntegerFieldAdapter<>(TaskContract.Tasks.STATUS))))

                // not recurring
                || entityAdapter.valueOf(InstanceAdapter.INSTANCE_ORIGINAL_TIME) == null)
        {
            // not a detachable instance
            return mDelegate.update(db, entityAdapter, isSyncAdapter);
        }
        // update instance accordingly and detach it
        return detachAll(db, mDelegate.update(db, entityAdapter, isSyncAdapter));
    }


    @Override
    public void delete(SQLiteDatabase db, InstanceAdapter entityAdapter, boolean isSyncAdapter)
    {
        // just delegate
        mDelegate.delete(db, entityAdapter, isSyncAdapter);
    }


    /**
     * Detach all closed instances preceding the given one.
     * <p>
     * TODO: this method needs some refactoring
     */
    private InstanceAdapter detachAll(SQLiteDatabase db, InstanceAdapter entityAdapter)
    {
        // keep some values for later
        long masterId = new FirstPresent<>(
                new NullSafe<>(entityAdapter.valueOf(new LongFieldAdapter<>(TaskContract.Instances.ORIGINAL_INSTANCE_ID))),
                new NullSafe<>(entityAdapter.valueOf(new LongFieldAdapter<>(TaskContract.Instances.TASK_ID)))).value();
        DateTime instanceOriginalTime = entityAdapter.valueOf(InstanceAdapter.INSTANCE_ORIGINAL_TIME);

        // detach instances which are completed
        try (Cursor instances = db.query(TaskDatabaseHelper.Tables.INSTANCE_VIEW,
                null,
                String.format("%s < 0 and %s == ?", TaskContract.Instances.DISTANCE_FROM_CURRENT, TaskContract.Instances.ORIGINAL_INSTANCE_ID),
                new String[] { String.valueOf(masterId) },
                null,
                null,
                null))
        {
            while (instances.moveToNext())
            {
                detachSingle(db, new CursorContentValuesInstanceAdapter(instances, new ContentValues()));
            }
        }

        // move the master to the first incomplete task
        try (Cursor task = db.query(TaskDatabaseHelper.Tables.TASKS_VIEW,
                null,
                String.format("%s == ?", TaskContract.Tasks._ID),
                new String[] { String.valueOf(masterId) },
                null,
                null,
                null))
        {
            if (task.moveToFirst())
            {
                TaskAdapter masterTask = new CursorContentValuesTaskAdapter(task, new ContentValues());
                DateTime oldStart = new FirstPresent<>(
                        new NullSafe<>(masterTask.valueOf(TaskAdapter.DTSTART)),
                        new NullSafe<>(masterTask.valueOf(TaskAdapter.DUE))).value();

                // assume we have no instances left
                boolean noInstances = true;

                // update RRULE, if existent
                RecurrenceRule rule = masterTask.valueOf(TaskAdapter.RRULE);
                int count = 0;
                if (rule != null)
                {
                    RecurrenceSet ruleSet = new RecurrenceSet();
                    ruleSet.addInstances(new RecurrenceRuleAdapter(rule));
                    if (rule.getCount() == null)
                    {
                        // rule has no count limit, allowing us to exclude exdates
                        ruleSet.addExceptions(new RecurrenceList(new Timestamps(masterTask.valueOf(TaskAdapter.EXDATE)).value()));
                    }
                    RecurrenceSetIterator ruleIterator = ruleSet.iterator(
                            oldStart.getTimeZone(),
                            oldStart.getTimestamp());

                    // move DTSTART to next RRULE instance which is > instanceOriginalTime
                    // reduce COUNT by the number of skipped instances, if present
                    while (count < 1000 && ruleIterator.hasNext())
                    {
                        DateTime inst = new DateTime(oldStart.getTimeZone(), ruleIterator.next());
                        if (instanceOriginalTime.before(inst))
                        {
                            updateStart(masterTask, inst);
                            noInstances = false; // just found another instance
                            break;
                        }
                        count += 1;
                    }

                    if (noInstances)
                    {
                        // remove the RRULE but keep a mask for the old start
                        masterTask.set(TaskAdapter.EXDATE,
                                new Joined<>(new SingletonIterable<>(oldStart), new Sieved<>(new Not<>(oldStart::equals), masterTask.valueOf(TaskAdapter.EXDATE))));
                        masterTask.set(TaskAdapter.RRULE, null);
                    }
                    else
                    {
                        // adjust COUNT if present
                        if (rule.getCount() != null)
                        {
                            rule.setCount(rule.getCount() - count);
                            masterTask.set(TaskAdapter.RRULE, rule);
                        }
                    }
                }

                DateTime newStart = new FirstPresent<>(
                        new NullSafe<>(masterTask.valueOf(TaskAdapter.DTSTART)),
                        new NullSafe<>(masterTask.valueOf(TaskAdapter.DUE))).value();

                // update RDATE and EXDATE
                masterTask.set(TaskAdapter.RDATE, new Sieved<>(instanceOriginalTime::before, masterTask.valueOf(TaskAdapter.RDATE)));
                masterTask.set(TaskAdapter.EXDATE,
                        new Sieved<>(new AnyOf<>(instanceOriginalTime::before, newStart::equals), masterTask.valueOf(TaskAdapter.EXDATE)));

                // First check if we still have any RDATE instances left
                // TODO: 6 lines for something we should be able to express in one simple expression, we need to straighten lib-recur!!
                RecurrenceSet rdateSet = new RecurrenceSet();
                rdateSet.addInstances(new RecurrenceList(new Timestamps(masterTask.valueOf(TaskAdapter.RDATE)).value()));
                rdateSet.addExceptions(new RecurrenceList(new Timestamps(masterTask.valueOf(TaskAdapter.EXDATE)).value()));
                RecurrenceSetIterator iterator = rdateSet.iterator(DateTime.UTC, Long.MIN_VALUE);
                iterator.fastForward(Long.MIN_VALUE + 1); // skip bogus start
                noInstances &= !iterator.hasNext();

                if (noInstances)
                {
                    // no more instances left, remove the master
                    mTaskDelegate.delete(db, masterTask, false);
                }
                else
                {
                    if (masterTask.valueOf(TaskAdapter.RRULE) == null)
                    {
                        // we don't have any RRULE, allowing us to adjust DTSTART/DUE to the first RDATE
                        DateTime start = new DateTime(iterator.next());
                        if (masterTask.valueOf(TaskAdapter.IS_ALLDAY))
                        {
                            start = start.toAllDay();
                        }
                        else if (masterTask.valueOf(TaskAdapter.TIMEZONE_RAW) != null)
                        {
                            start = start.shiftTimeZone(TimeZone.getTimeZone(masterTask.valueOf(TaskAdapter.TIMEZONE_RAW)));
                        }
                        updateStart(masterTask, start);
                    }

                    // we still have instances, update the database
                    mTaskDelegate.update(db, masterTask, false);
                }
            }
        }

        return entityAdapter;
    }


    private void updateStart(TaskAdapter task, DateTime newStart)
    {
        // this new instance becomes the new start (or due if we don't have a start)
        if (task.valueOf(TaskAdapter.DTSTART) != null)
        {
            DateTime oldStart = task.valueOf(TaskAdapter.DTSTART);
            task.set(TaskAdapter.DTSTART, newStart);
            if (task.valueOf(TaskAdapter.DUE) != null)
            {
                long duration = task.valueOf(TaskAdapter.DUE).getTimestamp() - oldStart.getTimestamp();
                task.set(TaskAdapter.DUE,
                        newStart.addDuration(
                                new Duration(1, (int) (duration / (3600 * 24 * 1000)), (int) (duration % (3600 * 24 * 1000)) / 1000)));
            }
        }
        else
        {
            task.set(TaskAdapter.DUE, newStart);
        }

    }


    /**
     * Detach the given instance.
     * <p>
     * - clone the override into a new deleted task (set _DELETED == 1)
     * - detach the original override by removing the ORIGINAL_INSTANCE_ID, ORIGINAL_INSTANCE_SYNC_ID, ORIGINAL_INSTANCE_START and ORIGINAL_INSTANCE_ALLDAY
     * (i.e. all columns which relate this to the original)
     * - wipe _SYNC_ID, _UID and all sync columns (make this an unsynced task)
     */
    private void detachSingle(SQLiteDatabase db, InstanceAdapter entityAdapter)
    {
        TaskAdapter original = entityAdapter.taskAdapter();
        TaskAdapter cloneAdapter = original.duplicate();

        // first prepare the original to resemble the same instance but as a new, detached task
        original.set(TaskAdapter.SYNC_ID, null);
        original.set(TaskAdapter.SYNC_VERSION, null);
        original.set(TaskAdapter.SYNC1, null);
        original.set(TaskAdapter.SYNC2, null);
        original.set(TaskAdapter.SYNC3, null);
        original.set(TaskAdapter.SYNC4, null);
        original.set(TaskAdapter.SYNC5, null);
        original.set(TaskAdapter.SYNC6, null);
        original.set(TaskAdapter.SYNC7, null);
        original.set(TaskAdapter.SYNC8, null);
        original.set(TaskAdapter._UID, null);
        original.set(TaskAdapter._DIRTY, true);
        original.set(TaskAdapter.ORIGINAL_INSTANCE_ID, null);
        original.set(TaskAdapter.ORIGINAL_INSTANCE_SYNC_ID, null);
        original.set(TaskAdapter.ORIGINAL_INSTANCE_TIME, null);
        original.unset(TaskAdapter.COMPLETED);
        original.commit(db);

        // wipe INSTANCE_ORIGINAL_TIME from instances entry
        ContentValues noOriginalTime = new ContentValues();
        noOriginalTime.putNull(TaskContract.Instances.INSTANCE_ORIGINAL_TIME);
        db.update(TaskDatabaseHelper.Tables.INSTANCES, noOriginalTime, "_ID = ?", new String[] { String.valueOf(entityAdapter.id()) });

        // reset the clone to be a deleted instance
        cloneAdapter.set(TaskAdapter._DELETED, true);
        // remove joined field values
        cloneAdapter.unset(TaskAdapter.LIST_ACCESS_LEVEL);
        cloneAdapter.unset(TaskAdapter.LIST_COLOR);
        cloneAdapter.unset(TaskAdapter.LIST_NAME);
        cloneAdapter.unset(TaskAdapter.LIST_OWNER);
        cloneAdapter.unset(TaskAdapter.LIST_VISIBLE);
        cloneAdapter.unset(TaskAdapter.ACCOUNT_NAME);
        cloneAdapter.unset(TaskAdapter.ACCOUNT_TYPE);
        cloneAdapter.commit(db);

        // note, we don't have to create an instance for the clone because it's deleted
    }
}
