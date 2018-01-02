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

package org.dmfs.provider.tasks;

import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.os.Build;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.dmfs.android.contentpal.Operation;
import org.dmfs.android.contentpal.OperationsQueue;
import org.dmfs.android.contentpal.RowSnapshot;
import org.dmfs.android.contentpal.Table;
import org.dmfs.android.contentpal.operations.Assert;
import org.dmfs.android.contentpal.operations.BulkDelete;
import org.dmfs.android.contentpal.operations.Delete;
import org.dmfs.android.contentpal.operations.Put;
import org.dmfs.android.contentpal.operations.Referring;
import org.dmfs.android.contentpal.predicates.AllOf;
import org.dmfs.android.contentpal.predicates.EqArg;
import org.dmfs.android.contentpal.predicates.IsNull;
import org.dmfs.android.contentpal.queues.BasicOperationsQueue;
import org.dmfs.android.contentpal.rowdata.Composite;
import org.dmfs.android.contentpal.rowdata.EmptyRowData;
import org.dmfs.android.contentpal.rowsnapshots.VirtualRowSnapshot;
import org.dmfs.android.contenttestpal.operations.AssertEmptyTable;
import org.dmfs.android.contenttestpal.operations.AssertRelated;
import org.dmfs.iterables.SingletonIterable;
import org.dmfs.iterables.elementary.Seq;
import org.dmfs.opentaskspal.tables.InstanceTable;
import org.dmfs.opentaskspal.tables.LocalTaskListsTable;
import org.dmfs.opentaskspal.tables.TaskListScoped;
import org.dmfs.opentaskspal.tables.TaskListsTable;
import org.dmfs.opentaskspal.tables.TasksTable;
import org.dmfs.opentaskspal.tasklists.NameData;
import org.dmfs.opentaskspal.tasks.OriginalInstanceSyncIdData;
import org.dmfs.opentaskspal.tasks.StatusData;
import org.dmfs.opentaskspal.tasks.SyncIdData;
import org.dmfs.opentaskspal.tasks.TimeData;
import org.dmfs.opentaskspal.tasks.TitleData;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.Duration;
import org.dmfs.tasks.contract.TaskContract.Instances;
import org.dmfs.tasks.contract.TaskContract.TaskLists;
import org.dmfs.tasks.contract.TaskContract.Tasks;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.TimeZone;

import static org.dmfs.android.contenttestpal.ContentMatcher.resultsIn;
import static org.junit.Assert.assertThat;


/**
 * Tests for {@link TaskProvider}.
 *
 * @author Yannic Ahrens
 * @author Gabor Keszthelyi
 */
@RunWith(AndroidJUnit4.class)
public class TaskProviderTest
{
    private ContentResolver mResolver;
    private String mAuthority;
    private Context mContext;
    private ContentProviderClient mClient;


    @Before
    public void setUp() throws Exception
    {
        mContext = InstrumentationRegistry.getTargetContext();
        mResolver = mContext.getContentResolver();
        mAuthority = AuthorityUtil.taskAuthority(mContext);
        mClient = mContext.getContentResolver().acquireContentProviderClient(mAuthority);

        // Assert that tables are empty:
        OperationsQueue queue = new BasicOperationsQueue(mClient);
        queue.enqueue(new Seq<Operation<?>>(
                new AssertEmptyTable<>(new TasksTable(mAuthority)),
                new AssertEmptyTable<>(new TaskListsTable(mAuthority)),
                new AssertEmptyTable<>(new InstanceTable(mAuthority))));
        queue.flush();
    }


    @After
    public void tearDown() throws Exception
    {
        /*
        TODO When Test Orchestration is available, there will be no need for clean up here and check in setUp(), every test method will run in separate instrumentation
        https://android-developers.googleblog.com/2017/07/android-testing-support-library-10-is.html
        https://developer.android.com/training/testing/junit-runner.html#using-android-test-orchestrator
        */

        // Clear the DB:
        BasicOperationsQueue queue = new BasicOperationsQueue(mClient);
        queue.enqueue(new SingletonIterable<Operation<?>>(new BulkDelete<>(new LocalTaskListsTable(mAuthority))));
        queue.flush();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        {
            mClient.close();
        }
        else
        {
            mClient.release();
        }
    }


    /**
     * Create 1 local task list and 1 task, check values in TaskLists, TaskList, Instances tables.
     */
    @Test
    public void testSingleInsert()
    {
        RowSnapshot<TaskLists> taskList = new VirtualRowSnapshot<>(new LocalTaskListsTable(mAuthority));
        RowSnapshot<Tasks> task = new VirtualRowSnapshot<>(new TaskListScoped(taskList, new TasksTable(mAuthority)));

        assertThat(new Seq<>(
                new Put<>(taskList, new NameData("list1")),
                new Put<>(task, new TitleData("task1"))

        ), resultsIn(mClient,
                new Assert<>(taskList, new NameData("list1")),
                new Assert<>(task, new TitleData("task1")),
                new AssertRelated<>(new InstanceTable(mAuthority), Instances.TASK_ID, task, new AllOf(
                        new IsNull(Instances.INSTANCE_START),
                        new EqArg(Instances.INSTANCE_ORIGINAL_TIME, 0),
                        new IsNull(Instances.INSTANCE_DUE),
                        new IsNull(Instances.INSTANCE_START_SORTING),
                        new IsNull(Instances.INSTANCE_DUE_SORTING),
                        new IsNull(Instances.INSTANCE_DURATION),
                        new IsNull(Tasks.TZ),
                        new EqArg(Instances.DISTANCE_FROM_CURRENT, 0)
                ))
        ));
    }


    /**
     * Create 2 task list and 3 tasks, check values.
     */
    @Test
    public void testMultipleInserts()
    {
        Table<TaskLists> taskListsTable = new LocalTaskListsTable(mAuthority);
        RowSnapshot<TaskLists> taskList1 = new VirtualRowSnapshot<>(taskListsTable);
        RowSnapshot<TaskLists> taskList2 = new VirtualRowSnapshot<>(taskListsTable);
        RowSnapshot<Tasks> task1 = new VirtualRowSnapshot<>(new TaskListScoped(taskList1, new TasksTable(mAuthority)));
        RowSnapshot<Tasks> task2 = new VirtualRowSnapshot<>(new TaskListScoped(taskList1, new TasksTable(mAuthority)));
        RowSnapshot<Tasks> task3 = new VirtualRowSnapshot<>(new TaskListScoped(taskList2, new TasksTable(mAuthority)));

        assertThat(new Seq<>(
                new Put<>(taskList1, new NameData("list1")),
                new Put<>(taskList2, new NameData("list2")),
                new Put<>(task1, new TitleData("task1")),
                new Put<>(task2, new TitleData("task2")),
                new Put<>(task3, new TitleData("task3"))

        ), resultsIn(mClient,
                new Assert<>(taskList1, new NameData("list1")),
                new Assert<>(taskList2, new NameData("list2")),
                new Assert<>(task1, new TitleData("task1")),
                new Assert<>(task2, new TitleData("task2")),
                new Assert<>(task3, new TitleData("task3")),
                new AssertRelated<>(new InstanceTable(mAuthority), Instances.TASK_ID, task1, new AllOf(
                        new IsNull(Instances.INSTANCE_START),
                        new EqArg(Instances.INSTANCE_ORIGINAL_TIME, 0),
                        new IsNull(Instances.INSTANCE_DUE),
                        new IsNull(Instances.INSTANCE_START_SORTING),
                        new IsNull(Instances.INSTANCE_DUE_SORTING),
                        new IsNull(Instances.INSTANCE_DURATION),
                        new IsNull(Tasks.TZ),
                        new EqArg(Instances.DISTANCE_FROM_CURRENT, 0)
                )),
                new AssertRelated<>(new InstanceTable(mAuthority), Instances.TASK_ID, task2, new AllOf(
                        new IsNull(Instances.INSTANCE_START),
                        new EqArg(Instances.INSTANCE_ORIGINAL_TIME, 0),
                        new IsNull(Instances.INSTANCE_DUE),
                        new IsNull(Instances.INSTANCE_START_SORTING),
                        new IsNull(Instances.INSTANCE_DUE_SORTING),
                        new IsNull(Instances.INSTANCE_DURATION),
                        new IsNull(Tasks.TZ),
                        new EqArg(Instances.DISTANCE_FROM_CURRENT, 0)
                )),
                new AssertRelated<>(new InstanceTable(mAuthority), Instances.TASK_ID, task3, new AllOf(
                        new IsNull(Instances.INSTANCE_START),
                        new EqArg(Instances.INSTANCE_ORIGINAL_TIME, 0),
                        new IsNull(Instances.INSTANCE_DUE),
                        new IsNull(Instances.INSTANCE_START_SORTING),
                        new IsNull(Instances.INSTANCE_DUE_SORTING),
                        new IsNull(Instances.INSTANCE_DURATION),
                        new IsNull(Tasks.TZ),
                        new EqArg(Instances.DISTANCE_FROM_CURRENT, 0)
                ))
        ));
    }


    /**
     * Create task with start and due, check datetime values including generated duration.
     */
    @Test
    public void testInsertTaskWithStartAndDue()
    {
        RowSnapshot<TaskLists> taskList = new VirtualRowSnapshot<>(new LocalTaskListsTable(mAuthority));
        RowSnapshot<Tasks> task = new VirtualRowSnapshot<>(new TaskListScoped(taskList, new TasksTable(mAuthority)));

        DateTime start = DateTime.now();
        DateTime due = start.addDuration(new Duration(1, 1, 0));

        assertThat(new Seq<>(
                new Put<>(taskList, new EmptyRowData<TaskLists>()),
                new Put<>(task, new TimeData(start, due))

        ), resultsIn(mClient,
                new Assert<>(task, new TimeData(start, due)),
                new AssertRelated<>(new InstanceTable(mAuthority), Instances.TASK_ID, task, new AllOf(
                        new EqArg(Instances.INSTANCE_START, start.getTimestamp()),
                        new EqArg(Instances.INSTANCE_ORIGINAL_TIME, 0),
                        new EqArg(Instances.INSTANCE_DUE, due.getTimestamp()),
                        new EqArg(Instances.INSTANCE_START_SORTING, start.shiftTimeZone(TimeZone.getDefault()).getInstance()),
                        new EqArg(Instances.INSTANCE_DUE_SORTING, due.shiftTimeZone(TimeZone.getDefault()).getInstance()),
                        new EqArg(Instances.INSTANCE_DURATION, due.getTimestamp() - start.getTimestamp()),
                        new EqArg(Tasks.TZ, start.isAllDay() ? "UTC" : start.getTimeZone().getID()),
                        new EqArg(Instances.DISTANCE_FROM_CURRENT, 0)
                ))
        ));
    }


    /**
     * Create task with start and due, check datetime and INSTANCE_STATUS values after updating the status.
     */
    @Test
    public void testInsertTaskWithStartAndDueUpdateStatus()
    {
        RowSnapshot<TaskLists> taskList = new VirtualRowSnapshot<>(new LocalTaskListsTable(mAuthority));
        RowSnapshot<Tasks> task = new VirtualRowSnapshot<>(new TaskListScoped(taskList, new TasksTable(mAuthority)));

        DateTime start = DateTime.now();
        DateTime due = start.addDuration(new Duration(1, 1, 0));

        assertThat(new Seq<>(
                new Put<>(taskList, new EmptyRowData<>()),
                new Put<>(task, new TimeData(start, due)),
                // update the status of the new task
                new Put<>(task, new StatusData(Tasks.STATUS_COMPLETED))
        ), resultsIn(mClient,
                new Assert<>(task, new TimeData(start, due)),
                new AssertRelated<>(new InstanceTable(mAuthority), Instances.TASK_ID, task, new AllOf(
                        new EqArg(Instances.INSTANCE_START, start.getTimestamp()),
                        new EqArg(Instances.INSTANCE_ORIGINAL_TIME, 0),
                        new EqArg(Instances.INSTANCE_DUE, due.getTimestamp()),
                        new EqArg(Instances.INSTANCE_START_SORTING, start.shiftTimeZone(TimeZone.getDefault()).getInstance()),
                        new EqArg(Instances.INSTANCE_DUE_SORTING, due.shiftTimeZone(TimeZone.getDefault()).getInstance()),
                        new EqArg(Instances.INSTANCE_DURATION, due.getTimestamp() - start.getTimestamp()),
                        new EqArg(Tasks.TZ, start.isAllDay() ? "UTC" : start.getTimeZone().getID()),
                        new EqArg(Instances.DISTANCE_FROM_CURRENT, -1)
                ))
        ));
    }


    /**
     * Create task with start and due and update it with new values, check datetime values including generated duration.
     */
    @Test
    public void testInsertTaskWithStartAndDueMovedForward()
    {
        RowSnapshot<TaskLists> taskList = new VirtualRowSnapshot<>(new LocalTaskListsTable(mAuthority));
        RowSnapshot<Tasks> task = new VirtualRowSnapshot<>(new TaskListScoped(taskList, new TasksTable(mAuthority)));

        DateTime start = DateTime.now();
        DateTime due = start.addDuration(new Duration(1, 1, 0));
        Duration duration = new Duration(1, 2, 0);

        DateTime startNew = start.addDuration(duration);
        DateTime dueNew = due.addDuration(duration);

        assertThat(new Seq<>(
                new Put<>(taskList, new EmptyRowData<TaskLists>()),
                new Put<>(task, new TimeData(start, due)),
                new Put<>(task, new TimeData(startNew, dueNew))
        ), resultsIn(mClient,
                new Assert<>(task, new TimeData(startNew, dueNew)),
                new AssertRelated<>(new InstanceTable(mAuthority), Instances.TASK_ID, task, new AllOf(
                        new EqArg(Instances.INSTANCE_START, startNew.getTimestamp()),
                        new EqArg(Instances.INSTANCE_ORIGINAL_TIME, 0),
                        new EqArg(Instances.INSTANCE_DUE, dueNew.getTimestamp()),
                        new EqArg(Instances.INSTANCE_START_SORTING, startNew.shiftTimeZone(TimeZone.getDefault()).getInstance()),
                        new EqArg(Instances.INSTANCE_DUE_SORTING, dueNew.shiftTimeZone(TimeZone.getDefault()).getInstance()),
                        new EqArg(Instances.INSTANCE_DURATION, dueNew.getTimestamp() - startNew.getTimestamp()),
                        new EqArg(Tasks.TZ, start.isAllDay() ? "UTC" : start.getTimeZone().getID()),
                        new EqArg(Instances.DISTANCE_FROM_CURRENT, 0)
                ))
        ));
    }


    /**
     * Create task with start and due and update it with new values, check datetime values including generated duration.
     */
    @Test
    public void testInsertTaskWithStartAndDueMovedBackwards()
    {
        RowSnapshot<TaskLists> taskList = new VirtualRowSnapshot<>(new LocalTaskListsTable(mAuthority));
        RowSnapshot<Tasks> task = new VirtualRowSnapshot<>(new TaskListScoped(taskList, new TasksTable(mAuthority)));

        DateTime start = DateTime.now();
        DateTime due = start.addDuration(new Duration(1, 1, 0));
        Duration duration = new Duration(-1, 2, 0);

        DateTime startNew = start.addDuration(duration);
        DateTime dueNew = due.addDuration(duration);

        assertThat(new Seq<>(
                new Put<>(taskList, new EmptyRowData<TaskLists>()),
                new Put<>(task, new TimeData(start, due)),
                new Put<>(task, new TimeData(startNew, dueNew))
        ), resultsIn(mClient,
                new Assert<>(task, new TimeData(startNew, dueNew)),
                new AssertRelated<>(new InstanceTable(mAuthority), Instances.TASK_ID, task, new AllOf(
                        new EqArg(Instances.INSTANCE_START, startNew.getTimestamp()),
                        new EqArg(Instances.INSTANCE_ORIGINAL_TIME, 0),
                        new EqArg(Instances.INSTANCE_DUE, dueNew.getTimestamp()),
                        new EqArg(Instances.INSTANCE_START_SORTING, startNew.shiftTimeZone(TimeZone.getDefault()).getInstance()),
                        new EqArg(Instances.INSTANCE_DUE_SORTING, dueNew.shiftTimeZone(TimeZone.getDefault()).getInstance()),
                        new EqArg(Instances.INSTANCE_DURATION, dueNew.getTimestamp() - startNew.getTimestamp()),
                        new EqArg(Tasks.TZ, start.isAllDay() ? "UTC" : start.getTimeZone().getID()),
                        new EqArg(Instances.DISTANCE_FROM_CURRENT, 0)
                ))
        ));
    }


    /**
     * Create task without dates and set start and due afterwards, check datetime values including generated duration.
     */
    @Test
    public void testInsertTaskWithStartAndDueAddedAfterwards()
    {
        RowSnapshot<TaskLists> taskList = new VirtualRowSnapshot<>(new LocalTaskListsTable(mAuthority));
        RowSnapshot<Tasks> task = new VirtualRowSnapshot<>(new TaskListScoped(taskList, new TasksTable(mAuthority)));

        DateTime start = DateTime.now();
        DateTime due = start.addDuration(new Duration(1, 1, 0));

        assertThat(new Seq<>(
                new Put<>(taskList, new EmptyRowData<TaskLists>()),
                new Put<>(task, new TitleData("Test")),
                new Put<>(task, new TimeData(start, due))
        ), resultsIn(mClient,
                new Assert<>(task, new TimeData(start, due)),
                new AssertRelated<>(new InstanceTable(mAuthority), Instances.TASK_ID, task, new AllOf(
                        new EqArg(Instances.INSTANCE_START, start.getTimestamp()),
                        new EqArg(Instances.INSTANCE_ORIGINAL_TIME, 0),
                        new EqArg(Instances.INSTANCE_DUE, due.getTimestamp()),
                        new EqArg(Instances.INSTANCE_START_SORTING, start.shiftTimeZone(TimeZone.getDefault()).getInstance()),
                        new EqArg(Instances.INSTANCE_DUE_SORTING, due.shiftTimeZone(TimeZone.getDefault()).getInstance()),
                        new EqArg(Instances.INSTANCE_DURATION, due.getTimestamp() - start.getTimestamp()),
                        new EqArg(Tasks.TZ, start.isAllDay() ? "UTC" : start.getTimeZone().getID()),
                        new EqArg(Instances.DISTANCE_FROM_CURRENT, 0)
                ))
        ));
    }


    /**
     * Create task with start and duration, check datetime values including generated due.
     */
    @Test
    public void testInsertWithStartAndDuration()
    {
        RowSnapshot<TaskLists> taskList = new VirtualRowSnapshot<>(new LocalTaskListsTable(mAuthority));
        RowSnapshot<Tasks> task = new VirtualRowSnapshot<>(new TaskListScoped(taskList, new TasksTable(mAuthority)));

        DateTime start = DateTime.now();
        Duration duration = Duration.parse("PT1H");
        long durationMillis = duration.toMillis();

        assertThat(new Seq<>(
                new Put<>(taskList, new EmptyRowData<TaskLists>()),
                new Put<>(task, new TimeData(start, duration))

        ), resultsIn(mClient,
                new Assert<>(task, new TimeData(start, duration)),
                new AssertRelated<>(new InstanceTable(mAuthority), Instances.TASK_ID, task, new AllOf(
                        new EqArg(Instances.INSTANCE_START, start.getTimestamp()),
                        new EqArg(Instances.INSTANCE_DUE, start.addDuration(duration).getTimestamp()),
                        new EqArg(Instances.INSTANCE_DURATION, durationMillis),
                        new EqArg(Instances.INSTANCE_START_SORTING, start.shiftTimeZone(TimeZone.getDefault()).getInstance()),
                        new EqArg(Instances.INSTANCE_DUE_SORTING, start.addDuration(duration).shiftTimeZone(TimeZone.getDefault()).getInstance()),
                        new EqArg(Instances.INSTANCE_ORIGINAL_TIME, 0),
                        new EqArg(Tasks.TZ, "UTC"),
                        new EqArg(Instances.DISTANCE_FROM_CURRENT, 0)
                ))
        ));
    }


    /**
     * Create task with start and duration, check datetime values including generated due.
     */
    @Test
    public void testInsertWithStartAndDurationChangeTimeZone()
    {
        RowSnapshot<TaskLists> taskList = new VirtualRowSnapshot<>(new LocalTaskListsTable(mAuthority));
        RowSnapshot<Tasks> task = new VirtualRowSnapshot<>(new TaskListScoped(taskList, new TasksTable(mAuthority)));

        DateTime start = DateTime.now();
        Duration duration = Duration.parse("PT1H");
        long durationMillis = duration.toMillis();
        DateTime startNew = start.shiftTimeZone(TimeZone.getTimeZone("America/New_York"));

        assertThat(new Seq<>(
                new Put<>(taskList, new EmptyRowData<TaskLists>()),
                new Put<>(task, new TimeData(start, duration)),
                // update the task with a the same start in a different time zone
                new Put<>(task, new TimeData(startNew, duration))

        ), resultsIn(mClient,
                new Assert<>(task, new TimeData(startNew, duration)),
                // note that, apart from the time zone, all values stay the same
                new AssertRelated<>(new InstanceTable(mAuthority), Instances.TASK_ID, task, new AllOf(
                        new EqArg(Instances.INSTANCE_START, start.getTimestamp()),
                        new EqArg(Instances.INSTANCE_DUE, start.addDuration(duration).getTimestamp()),
                        new EqArg(Instances.INSTANCE_DURATION, durationMillis),
                        new EqArg(Instances.INSTANCE_START_SORTING, start.shiftTimeZone(TimeZone.getDefault()).getInstance()),
                        new EqArg(Instances.INSTANCE_DUE_SORTING, start.addDuration(duration).shiftTimeZone(TimeZone.getDefault()).getInstance()),
                        new EqArg(Instances.INSTANCE_ORIGINAL_TIME, 0),
                        new EqArg(Tasks.TZ, "America/New_York"),
                        new EqArg(Instances.DISTANCE_FROM_CURRENT, 0)
                ))
        ));
    }


    /**
     * Having a task with start and due.
     * Update it with different due, check datetime values correct in Tasks and Instances.
     */
    @Test
    public void testUpdateDue() throws Exception
    {
        RowSnapshot<TaskLists> taskList = new VirtualRowSnapshot<>(new LocalTaskListsTable(mAuthority));
        RowSnapshot<Tasks> task = new VirtualRowSnapshot<>(new TaskListScoped(taskList, new TasksTable(mAuthority)));
        OperationsQueue queue = new BasicOperationsQueue(mClient);

        DateTime start = DateTime.now();
        DateTime due = start.addDuration(new Duration(1, 0, 1));

        queue.enqueue(new Seq<>(
                new Put<>(taskList, new NameData("list1")),
                new Put<>(task, new TimeData(start, due))
        ));
        queue.flush();

        DateTime due2 = due.addDuration(new Duration(1, 0, 2));

        assertThat(new SingletonIterable<>(
                new Put<>(task, new TimeData(start, due2))

        ), resultsIn(queue,
                new Assert<>(task, new TimeData(start, due2)),
                new AssertRelated<>(new InstanceTable(mAuthority), Instances.TASK_ID, task, new AllOf(
                        new EqArg(Instances.INSTANCE_START, start.getTimestamp()),
                        new EqArg(Instances.INSTANCE_DUE, due2.getTimestamp()),
                        new EqArg(Instances.INSTANCE_DURATION, due2.getTimestamp() - start.getTimestamp()),
                        new EqArg(Instances.INSTANCE_START_SORTING, start.shiftTimeZone(TimeZone.getDefault()).getInstance()),
                        new EqArg(Instances.INSTANCE_DUE_SORTING, due2.shiftTimeZone(TimeZone.getDefault()).getInstance()),
                        new EqArg(Instances.INSTANCE_ORIGINAL_TIME, 0),
                        new EqArg(Tasks.TZ, "UTC"),
                        new EqArg(Instances.DISTANCE_FROM_CURRENT, 0)
                ))
        ));
    }


    /**
     * Having a single task.
     * Delete task, check that it is removed from Tasks and Instances tables.
     */
    @Test
    public void testInstanceDelete() throws Exception
    {
        RowSnapshot<TaskLists> taskList = new VirtualRowSnapshot<>(new LocalTaskListsTable(mAuthority));
        Table<Tasks> taskTable = new TaskListScoped(taskList, new TasksTable(mAuthority));
        RowSnapshot<Tasks> task = new VirtualRowSnapshot<>(taskTable);
        OperationsQueue queue = new BasicOperationsQueue(mClient);

        queue.enqueue(new Seq<>(
                new Put<>(taskList, new NameData("list1")),
                new Put<>(task, new TitleData("task1"))
        ));
        queue.flush();

        assertThat(new SingletonIterable<>(
                new Delete<>(task)

        ), resultsIn(queue,
                new AssertEmptyTable<>(new TasksTable(mAuthority)),
                new AssertEmptyTable<>(new InstanceTable(mAuthority))
        ));
    }


    /**
     * Contract: LIST_ID is required on task creation.
     * <p>
     * Create task without LIST_ID, check for IllegalArgumentException.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInsertWithOutListId() throws Exception
    {
        RowSnapshot<Tasks> task = new VirtualRowSnapshot<>(new TasksTable(mAuthority));
        OperationsQueue queue = new BasicOperationsQueue(mClient);
        queue.enqueue(new SingletonIterable<Operation<?>>(new Put<>(task, new TitleData("task1"))));
        queue.flush();
    }


    /**
     * Contract: LIST_ID has to refer to existing TaskList.
     * <p>
     * Create task with a non-exsiting LIST_ID, check for IllegalArgumentException.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInsertWithInvalidTaskListId()
    {
        ContentValues values = new ContentValues();
        values.put(Tasks.LIST_ID, 5);
        mResolver.insert(Tasks.getContentUri(mAuthority), values);
    }


    /**
     * Contract: Setting ORIGINAL_INSTANCE_SYNC_ID for an exception task,
     * provider must fill ORIGINAL_INSTANCE_ID with corresponding original task's _ID.
     */
    @Test
    public void testExceptionalInstance_settingSyncId_shouldUpdateRegularId() throws Exception
    {
        RowSnapshot<TaskLists> taskList = new VirtualRowSnapshot<>(new LocalTaskListsTable(mAuthority));
        Table<Tasks> taskTable = new TaskListScoped(taskList, new TasksTable(mAuthority));
        RowSnapshot<Tasks> task = new VirtualRowSnapshot<>(taskTable);
        RowSnapshot<Tasks> exceptionTask = new VirtualRowSnapshot<>(taskTable);

        OperationsQueue queue = new BasicOperationsQueue(mClient);

        queue.enqueue(new Seq<Operation<?>>(
                new Put<>(taskList, new NameData("list1")),
                new Put<>(task, new Composite<>(
                        new TitleData("task1"),
                        new SyncIdData("syncId1"))
                )
        ));
        queue.flush();

        assertThat(new SingletonIterable<>(
                new Put<>(exceptionTask, new Composite<>(
                        new TitleData("task1exception"),
                        new OriginalInstanceSyncIdData("syncId1"))
                )

        ), resultsIn(queue,
                new AssertRelated<>(new TasksTable(mAuthority), Tasks.ORIGINAL_INSTANCE_ID, task, new TitleData("task1exception"))
        ));
    }


    /**
     * Contract: Setting ORIGINAL_INSTANCE_ID for an exception task,
     * provider must fill ORIGINAL_INSTANCE_SYNC_ID with corresponding original task's _SYNC_ID.
     */
    @Test
    public void testExceptionalInstance_settingRegularId_shouldUpdateSyncId() throws Exception
    {
        RowSnapshot<TaskLists> taskList = new VirtualRowSnapshot<>(new LocalTaskListsTable(mAuthority));
        Table<Tasks> taskTable = new TaskListScoped(taskList, new TasksTable(mAuthority));
        RowSnapshot<Tasks> task = new VirtualRowSnapshot<>(taskTable);
        RowSnapshot<Tasks> exceptionTask = new VirtualRowSnapshot<>(taskTable);

        OperationsQueue queue = new BasicOperationsQueue(mClient);

        queue.enqueue(new Seq<Operation<?>>(
                new Put<>(taskList, new NameData("list1")),
                new Put<>(task, new Composite<>(
                        new TitleData("task1"),
                        new SyncIdData("syncId1"))
                )
        ));
        queue.flush();

        assertThat(new SingletonIterable<>(
                new Referring<>(task, Tasks.ORIGINAL_INSTANCE_ID,
                        new Put<>(exceptionTask, new TitleData("task1exception")))

        ), resultsIn(queue,
                new AssertRelated<>(new TasksTable(mAuthority), Tasks.ORIGINAL_INSTANCE_ID, task,
                        new Composite<>(
                                new TitleData("task1exception"),
                                new OriginalInstanceSyncIdData("syncId1")
                        ))
        ));
    }

}
