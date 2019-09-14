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

import android.accounts.Account;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.os.Build;

import org.dmfs.android.contentpal.Operation;
import org.dmfs.android.contentpal.OperationsQueue;
import org.dmfs.android.contentpal.RowSnapshot;
import org.dmfs.android.contentpal.Table;
import org.dmfs.android.contentpal.operations.Assert;
import org.dmfs.android.contentpal.operations.BulkDelete;
import org.dmfs.android.contentpal.operations.BulkUpdate;
import org.dmfs.android.contentpal.operations.Counted;
import org.dmfs.android.contentpal.operations.Delete;
import org.dmfs.android.contentpal.operations.Put;
import org.dmfs.android.contentpal.predicates.ReferringTo;
import org.dmfs.android.contentpal.queues.BasicOperationsQueue;
import org.dmfs.android.contentpal.rowdata.CharSequenceRowData;
import org.dmfs.android.contentpal.rowdata.Composite;
import org.dmfs.android.contentpal.rowdata.EmptyRowData;
import org.dmfs.android.contentpal.rowdata.Referring;
import org.dmfs.android.contentpal.rowsnapshots.VirtualRowSnapshot;
import org.dmfs.android.contentpal.tables.Synced;
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
import org.dmfs.opentaskspal.tasks.OriginalInstanceData;
import org.dmfs.opentaskspal.tasks.OriginalInstanceSyncIdData;
import org.dmfs.opentaskspal.tasks.RRuleTaskData;
import org.dmfs.opentaskspal.tasks.StatusData;
import org.dmfs.opentaskspal.tasks.SyncIdData;
import org.dmfs.opentaskspal.tasks.TimeData;
import org.dmfs.opentaskspal.tasks.TitleData;
import org.dmfs.opentaskspal.tasks.VersionData;
import org.dmfs.opentaskstestpal.InstanceTestData;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.Duration;
import org.dmfs.rfc5545.recur.InvalidRecurrenceRuleException;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import org.dmfs.tasks.contract.TaskContract.Instances;
import org.dmfs.tasks.contract.TaskContract.TaskLists;
import org.dmfs.tasks.contract.TaskContract.Tasks;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.TimeZone;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import static org.dmfs.android.contenttestpal.ContentMatcher.resultsIn;
import static org.dmfs.optional.Absent.absent;
import static org.junit.Assert.assertThat;


/**
 * Tests for {@link TaskProvider}.
 *
 * @author Yannic Ahrens
 * @author Gabor Keszthelyi
 * @author Marten Gajda
 */
@RunWith(AndroidJUnit4.class)
public class TaskProviderTest
{
    private ContentResolver mResolver;
    private String mAuthority;
    private Context mContext;
    private ContentProviderClient mClient;
    private final Account testAccount = new Account("foo", "bar");


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
        queue.enqueue(new Seq<Operation<?>>(
                new BulkDelete<>(new LocalTaskListsTable(mAuthority)),
                new BulkDelete<>(new Synced<>(testAccount, new TaskListsTable(mAuthority)))));
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
     * Create 1 local task list and 1 task, check values in TaskLists, Tasks, Instances tables.
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
                new Assert<>(task, new Composite<>(
                        new TitleData("task1"),
                        new VersionData(0))),
                new AssertRelated<>(new InstanceTable(mAuthority), Instances.TASK_ID, task,
                        new Composite<Instances>(
                                new InstanceTestData(0),
                                new CharSequenceRowData<>(Tasks.TZ, null))
                )));
    }


    /**
     * Create 1 local task list and 1 task, update task via instances table and check values in TaskLists, Tasks, Instances tables.
     */
    @Test
    public void testSingleInsertUpdateInstance()
    {
        RowSnapshot<TaskLists> taskList = new VirtualRowSnapshot<>(new LocalTaskListsTable(mAuthority));
        RowSnapshot<Tasks> task = new VirtualRowSnapshot<>(new TaskListScoped(taskList, new TasksTable(mAuthority)));
        Table<Instances> instancesTable = new InstanceTable(mAuthority);

        assertThat(new Seq<>(
                new Put<>(taskList, new NameData("list1")),
                new Put<>(task, new TitleData("task1")),
                new BulkUpdate<>(instancesTable, new CharSequenceRowData<>(Tasks.TITLE, "task updated"), new ReferringTo<>(Instances.TASK_ID, task))

        ), resultsIn(mClient,
                new Assert<>(taskList, new NameData("list1")),
                new Assert<>(task, new Composite<>(
                        new TitleData("task updated"),
                        new VersionData(1))),
                new AssertRelated<>(new InstanceTable(mAuthority), Instances.TASK_ID, task,
                        new Composite<Instances>(
                                new InstanceTestData(0),
                                new CharSequenceRowData<>(Tasks.TZ, null))
                )));
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
                new Assert<>(task1, new Composite<>(
                        new TitleData("task1"),
                        new VersionData(0))),
                new Assert<>(task2, new Composite<>(
                        new TitleData("task2"),
                        new VersionData(0))),
                new Assert<>(task3, new Composite<>(
                        new TitleData("task3"),
                        new VersionData(0))),
                new AssertRelated<>(
                        new InstanceTable(mAuthority), Instances.TASK_ID, task1,
                        new Composite<Instances>(
                                new InstanceTestData(0),
                                new CharSequenceRowData<>(Tasks.TZ, null))),
                new AssertRelated<>(
                        new InstanceTable(mAuthority), Instances.TASK_ID, task2,
                        new Composite<Instances>(
                                new InstanceTestData(0),
                                new CharSequenceRowData<>(Tasks.TZ, null))),
                new AssertRelated<>(
                        new InstanceTable(mAuthority), Instances.TASK_ID, task3,
                        new Composite<Instances>(
                                new InstanceTestData(0),
                                new CharSequenceRowData<>(Tasks.TZ, null))
                )));
    }


    /**
     * Create 2 task list and 3 tasks with updates, check values.
     */
    @Test
    public void testMultipleInsertsAndUpdates()
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
                new Put<>(task1, new TitleData("task1a")),
                new Put<>(task2, new TitleData("task2a")),
                new Put<>(task3, new TitleData("task3a")),
                // update task 1 and 2
                new Put<>(task1, new TitleData("task1b")),
                new Put<>(task2, new TitleData("task2b")),
                // update task 1 once more
                new Put<>(task1, new TitleData("task1c"))

        ), resultsIn(mClient,
                new Assert<>(taskList1, new NameData("list1")),
                new Assert<>(taskList2, new NameData("list2")),
                new Assert<>(task1, new Composite<>(
                        new TitleData("task1c"),
                        new VersionData(2))),
                new Assert<>(task2, new Composite<>(
                        new TitleData("task2b"),
                        new VersionData(1))),
                new Assert<>(task3, new Composite<>(
                        new TitleData("task3a"),
                        new VersionData(0))),
                new AssertRelated<>(
                        new InstanceTable(mAuthority), Instances.TASK_ID, task1,
                        new Composite<Instances>(
                                new InstanceTestData(0),
                                new CharSequenceRowData<>(Tasks.TZ, null))),
                new AssertRelated<>(
                        new InstanceTable(mAuthority), Instances.TASK_ID, task2,
                        new Composite<Instances>(
                                new InstanceTestData(0),
                                new CharSequenceRowData<>(Tasks.TZ, null))),
                new AssertRelated<>(
                        new InstanceTable(mAuthority), Instances.TASK_ID, task3,
                        new Composite<Instances>(
                                new InstanceTestData(0),
                                new CharSequenceRowData<>(Tasks.TZ, null))
                )));
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
                new Put<>(task, new TimeData<>(start, due))

        ), resultsIn(mClient,
                new Assert<>(task, new Composite<>(
                        new TimeData<>(start, due),
                        new VersionData(0))),
                new AssertRelated<>(
                        new InstanceTable(mAuthority), Instances.TASK_ID, task,
                        new Composite<Instances>(
                                new InstanceTestData(
                                        start.shiftTimeZone(TimeZone.getDefault()),
                                        due.shiftTimeZone(TimeZone.getDefault()),
                                        absent(),
                                        0),
                                new CharSequenceRowData<>(Tasks.TZ, "UTC"))
                )));
    }



    /**
     * Create task with start and due, check datetime values including generated duration.
     */
    @Test
    public void testInsertTaskWithAlldayStartAndDue()
    {
        RowSnapshot<TaskLists> taskList = new VirtualRowSnapshot<>(new LocalTaskListsTable(mAuthority));
        RowSnapshot<Tasks> task = new VirtualRowSnapshot<>(new TaskListScoped(taskList, new TasksTable(mAuthority)));

        DateTime start = DateTime.now().toAllDay();
        DateTime due = start.addDuration(new Duration(1, 2, 0));

        assertThat(new Seq<>(
                new Put<>(taskList, new EmptyRowData<TaskLists>()),
                new Put<>(task, new TimeData<>(start, due))

        ), resultsIn(mClient,
                new Assert<>(task, new Composite<>(
                        new TimeData<>(start, due),
                        new VersionData(0))),
                new AssertRelated<>(
                        new InstanceTable(mAuthority), Instances.TASK_ID, task,
                        new Composite<Instances>(
                                new InstanceTestData(
                                        start,
                                        due,
                                        absent(),
                                        0),
                                new CharSequenceRowData<>(Tasks.TZ, "UTC"))
                )));
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
                new Put<>(task, new TimeData<>(start, due)),
                // update the status of the new task
                new Put<>(task, new StatusData<>(Tasks.STATUS_COMPLETED))
        ), resultsIn(mClient,
                new Assert<>(task, new Composite<>(
                        new TimeData<>(start, due),
                        new VersionData(1))), // task has been updated once
                new AssertRelated<>(
                        new InstanceTable(mAuthority), Instances.TASK_ID, task,
                        new Composite<Instances>(
                                new InstanceTestData(
                                        start.shiftTimeZone(TimeZone.getDefault()),
                                        due.shiftTimeZone(TimeZone.getDefault()),
                                        absent(),
                                        -1),
                                new CharSequenceRowData<>(Tasks.TZ, "UTC"))
                )));
    }


    /**
     * Create task with start and due, check datetime and INSTANCE_STATUS values after updating the task twice.
     */
    @Test
    public void testInsertTaskWithStartAndDueUpdateTwice()
    {
        RowSnapshot<TaskLists> taskList = new VirtualRowSnapshot<>(new LocalTaskListsTable(mAuthority));
        RowSnapshot<Tasks> task = new VirtualRowSnapshot<>(new TaskListScoped(taskList, new TasksTable(mAuthority)));

        DateTime start = DateTime.now();
        DateTime due = start.addDuration(new Duration(1, 1, 0));

        assertThat(new Seq<>(
                new Put<>(taskList, new EmptyRowData<>()),
                new Put<>(task, new TimeData<>(start, due)),
                // update the status of the new task
                new Put<>(task, new StatusData<>(Tasks.STATUS_COMPLETED)),
                // update the title of the new task
                new Put<>(task, new TitleData("Task Title"))
        ), resultsIn(mClient,
                new Assert<>(task, new Composite<>(
                        new TimeData<>(start, due),
                        new TitleData("Task Title"),
                        new VersionData(2))), // task has been updated twice
                new AssertRelated<>(
                        new InstanceTable(mAuthority), Instances.TASK_ID, task,
                        new Composite<Instances>(
                                new InstanceTestData(
                                        start.shiftTimeZone(TimeZone.getDefault()),
                                        due.shiftTimeZone(TimeZone.getDefault()),
                                        absent(),
                                        -1),
                                new CharSequenceRowData<>(Tasks.TZ, "UTC"))
                )));
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
                new Put<>(task, new TimeData<>(start, due)),
                new Put<>(task, new TimeData<>(startNew, dueNew))
        ), resultsIn(mClient,
                new Assert<>(task, new Composite<>(
                        new TimeData<>(startNew, dueNew),
                        new VersionData(1))),
                new AssertRelated<>(
                        new InstanceTable(mAuthority), Instances.TASK_ID, task,
                        new Composite<Instances>(
                                new InstanceTestData(
                                        startNew.shiftTimeZone(TimeZone.getDefault()),
                                        dueNew.shiftTimeZone(TimeZone.getDefault()),
                                        absent(),
                                        0),
                                new CharSequenceRowData<>(Tasks.TZ, "UTC"))
                )));
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
                new Put<>(task, new TimeData<>(start, due)),
                new Put<>(task, new TimeData<>(startNew, dueNew))
        ), resultsIn(mClient,
                new Assert<>(task, new Composite<>(
                        new TimeData<>(startNew, dueNew),
                        new VersionData(1))),
                new AssertRelated<>(
                        new InstanceTable(mAuthority), Instances.TASK_ID, task,
                        new Composite<Instances>(
                                new InstanceTestData(
                                        startNew.shiftTimeZone(TimeZone.getDefault()),
                                        dueNew.shiftTimeZone(TimeZone.getDefault()),
                                        absent(),
                                        0),
                                new CharSequenceRowData<>(Tasks.TZ, "UTC"))
                )));
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
                new Put<>(task, new TimeData<>(start, due))
        ), resultsIn(mClient,
                new Assert<>(task, new Composite<>(
                        new TimeData<>(start, due),
                        new VersionData(1))),
                new AssertRelated<>(
                        new InstanceTable(mAuthority), Instances.TASK_ID, task,
                        new Composite<Instances>(
                                new InstanceTestData(
                                        start.shiftTimeZone(TimeZone.getDefault()),
                                        due.shiftTimeZone(TimeZone.getDefault()),
                                        absent(),
                                        0),
                                new CharSequenceRowData<>(Tasks.TZ, "UTC"))
                )));
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
                new Put<>(task, new TimeData<>(start, duration))

        ), resultsIn(mClient,
                new Assert<>(task, new Composite<>(
                        new TimeData<>(start, duration),
                        new VersionData(0))),
                new AssertRelated<>(
                        new InstanceTable(mAuthority), Instances.TASK_ID, task,
                        new Composite<Instances>(
                                new InstanceTestData(
                                        start.shiftTimeZone(TimeZone.getDefault()),
                                        start.shiftTimeZone(TimeZone.getDefault()).addDuration(duration),
                                        absent(),
                                        0),
                                new CharSequenceRowData<>(Tasks.TZ, "UTC"))
                )));
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
                new Put<>(task, new TimeData<>(start, duration)),
                // update the task with a the same start in a different time zone
                new Put<>(task, new TimeData<>(startNew, duration))

        ), resultsIn(mClient,
                new Assert<>(task, new Composite<>(
                        new TimeData<>(startNew, duration),
                        new VersionData(1))),
                // note that, apart from the time zone, all values stay the same
                new AssertRelated<>(
                        new InstanceTable(mAuthority), Instances.TASK_ID, task,
                        new Composite<Instances>(
                                new InstanceTestData(
                                        start.shiftTimeZone(TimeZone.getDefault()),
                                        start.shiftTimeZone(TimeZone.getDefault()).addDuration(duration),
                                        absent(),
                                        0),
                                new CharSequenceRowData<>(Tasks.TZ, "America/New_York"))
                )));
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
                new Put<>(task, new TimeData<>(start, due))
        ));
        queue.flush();

        DateTime due2 = due.addDuration(new Duration(1, 0, 2));

        assertThat(new SingletonIterable<>(
                new Put<>(task, new TimeData<>(start, due2))

        ), resultsIn(queue,
                new Assert<>(task, new Composite<>(
                        new TimeData<>(start, due2),
                        new VersionData(1))),
                new AssertRelated<>(
                        new InstanceTable(mAuthority), Instances.TASK_ID, task,
                        new Composite<Instances>(
                                new InstanceTestData(
                                        start.shiftTimeZone(TimeZone.getDefault()),
                                        due2.shiftTimeZone(TimeZone.getDefault()),
                                        absent(),
                                        0),
                                new CharSequenceRowData<>(Tasks.TZ, "UTC"))
                )));
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
     * Having a single task.
     * Delete the instance of that task, check that it is removed from Tasks and Instances tables.
     */
    @Test
    public void testDeleteInstance() throws Exception
    {
        RowSnapshot<TaskLists> taskList = new VirtualRowSnapshot<>(new LocalTaskListsTable(mAuthority));
        Table<Tasks> taskTable = new TaskListScoped(taskList, new TasksTable(mAuthority));
        Table<Instances> instancesTable = new InstanceTable(mAuthority);
        RowSnapshot<Tasks> task = new VirtualRowSnapshot<>(taskTable);
        OperationsQueue queue = new BasicOperationsQueue(mClient);

        queue.enqueue(new Seq<>(
                new Put<>(taskList, new NameData("list1")),
                new Put<>(task, new TitleData("task1"))
        ));
        queue.flush();

        // check that removing the instance removes task and instance
        assertThat(new SingletonIterable<>(
                new BulkDelete<>(instancesTable, new ReferringTo<>(Instances.TASK_ID, task))

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
                        new OriginalInstanceSyncIdData("syncId1", new DateTime(0)))
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
                        new SyncIdData("syncId1")))
        ));
        queue.flush();

        DateTime now = DateTime.now();

        assertThat(new SingletonIterable<>(
                new Put<>(exceptionTask,
                        new Composite<>(
                                new TitleData("task1exception"),
                                new OriginalInstanceData(task, now)))

        ), resultsIn(queue,
                new AssertRelated<>(new TasksTable(mAuthority), Tasks.ORIGINAL_INSTANCE_ID, task,
                        new Composite<>(
                                new TitleData("task1exception"),
                                new OriginalInstanceSyncIdData("syncId1", now)
                        ))
        ));
    }


    /**
     * Move a non-recurring task to another list.
     */
    @Test
    public void testMoveTaskInstance() throws Exception
    {
        RowSnapshot<TaskLists> taskListOld = new VirtualRowSnapshot<>(new LocalTaskListsTable(mAuthority));
        RowSnapshot<TaskLists> taskListNew = new VirtualRowSnapshot<>(new LocalTaskListsTable(mAuthority));
        RowSnapshot<Tasks> task = new VirtualRowSnapshot<>(new TaskListScoped(taskListOld, new TasksTable(mAuthority)));
        OperationsQueue queue = new BasicOperationsQueue(mClient);

        // create two lists and a single task in the first list
        queue.enqueue(new Seq<>(
                new Put<>(taskListOld, new NameData("list1")),
                new Put<>(taskListNew, new NameData("list2")),
                new Put<>(task, new TitleData("title"))
        ));
        queue.flush();

        assertThat(new SingletonIterable<>(
                // update the sole task instance to the new list
                new BulkUpdate<>(new InstanceTable(mAuthority), new Referring<>(Tasks.LIST_ID, taskListNew), new ReferringTo<>(Tasks.LIST_ID, taskListOld))
        ), resultsIn(queue,
                // assert the old list is empty
                new Counted<>(0, new AssertRelated<>(new InstanceTable(mAuthority), Tasks.LIST_ID, taskListOld)),
                new Counted<>(0, new AssertRelated<>(new TasksTable(mAuthority), Tasks.LIST_ID, taskListOld)),
                // assert the new list contains a single entry
                new Counted<>(1, new AssertRelated<>(new InstanceTable(mAuthority), Tasks.LIST_ID, taskListNew)),
                new Counted<>(1, new AssertRelated<>(new TasksTable(mAuthority), Tasks.LIST_ID, taskListNew, new TitleData("title")))
        ));
    }


    /**
     * Move a non-recurring task to another list.
     */
    @Test
    public void testMoveTaskInstanceAsSyncAdapter() throws Exception
    {
        Table<TaskLists> taskListsTable = new Synced<>(testAccount, new TaskListsTable(mAuthority));
        Table<Instances> instancesTable = new Synced<>(testAccount, new InstanceTable(mAuthority));
        Table<Tasks> tasksTable = new Synced<>(testAccount, new TasksTable(mAuthority));

        RowSnapshot<TaskLists> taskListOld = new VirtualRowSnapshot<>(taskListsTable);
        RowSnapshot<TaskLists> taskListNew = new VirtualRowSnapshot<>(taskListsTable);
        RowSnapshot<Tasks> task = new VirtualRowSnapshot<>(new TaskListScoped(taskListOld, tasksTable));
        OperationsQueue queue = new BasicOperationsQueue(mClient);

        // create two lists and a single task in the first list
        queue.enqueue(new Seq<>(
                new Put<>(taskListOld, new NameData("list1")),
                new Put<>(taskListNew, new NameData("list2")),
                new Put<>(task, new Composite<>(
                        new SyncIdData("syncid"), // give it a sync id, so it counts as synced
                        new TitleData("title")))));
        queue.flush();

        assertThat(new SingletonIterable<>(
                // update the sole task instance to the new list
                new BulkUpdate<>(new InstanceTable(mAuthority), new Referring<>(Tasks.LIST_ID, taskListNew), new ReferringTo<>(Tasks.LIST_ID, taskListOld))
        ), resultsIn(queue,
                // assert the old list contains a deleted entry for the task
                new Counted<>(0,
                        new AssertRelated<>(
                                instancesTable,
                                Tasks.LIST_ID,
                                taskListOld)),
                new Counted<>(1,
                        new AssertRelated<>(
                                tasksTable,
                                Tasks.LIST_ID,
                                taskListOld,
                                new Composite<>(
                                        new TitleData("title"),
                                        new CharSequenceRowData<>(Tasks._DELETED, "1")))),
                // assert the new list contains a single entry
                new Counted<>(1, new AssertRelated<>(instancesTable, Tasks.LIST_ID, taskListNew)),
                new Counted<>(1, new AssertRelated<>(tasksTable, Tasks.LIST_ID, taskListNew, new TitleData("title")))
        ));
    }


    /**
     * Create task with start and due, check datetime values including generated duration.
     */
    @Test
    public void testInsertTaskWithoutStartAndDueButRRULE() throws InvalidRecurrenceRuleException
    {
        RowSnapshot<TaskLists> taskList = new VirtualRowSnapshot<>(new LocalTaskListsTable(mAuthority));
        RowSnapshot<Tasks> task = new VirtualRowSnapshot<>(new TaskListScoped(taskList, new TasksTable(mAuthority)));

        DateTime start = DateTime.now();
        DateTime due = start.addDuration(new Duration(1, 1, 0));

        assertThat(new Seq<>(
                        new Put<>(taskList, new EmptyRowData<>()),
                        new Put<>(task, new Composite<>(
                                new TitleData("test"),
                                new RRuleTaskData(new RecurrenceRule("FREQ=DAILY;COUNT=5", RecurrenceRule.RfcMode.RFC2445_LAX))))),
                resultsIn(mClient,
                        new Assert<>(task, new Composite<>(
                                new TitleData("test"),
                                new VersionData(0))),
                        new AssertRelated<>(
                                new InstanceTable(mAuthority), Instances.TASK_ID, task,
                                new Composite<>(
                                        new CharSequenceRowData<>(Tasks.TITLE, "test"),
                                        new InstanceTestData(
                                                absent(),
                                                absent(),
                                                absent(),
                                                0),
                                        new CharSequenceRowData<>(Tasks.TZ, null))
                        )));
    }

}
