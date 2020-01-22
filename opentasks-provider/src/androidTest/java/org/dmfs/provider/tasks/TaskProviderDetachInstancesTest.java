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

package org.dmfs.provider.tasks;

import android.accounts.Account;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.OperationApplicationException;
import android.os.Build;
import android.os.RemoteException;

import org.dmfs.android.contentpal.Operation;
import org.dmfs.android.contentpal.OperationsQueue;
import org.dmfs.android.contentpal.RowSnapshot;
import org.dmfs.android.contentpal.Table;
import org.dmfs.android.contentpal.operations.Assert;
import org.dmfs.android.contentpal.operations.BulkAssert;
import org.dmfs.android.contentpal.operations.BulkDelete;
import org.dmfs.android.contentpal.operations.BulkUpdate;
import org.dmfs.android.contentpal.operations.Counted;
import org.dmfs.android.contentpal.operations.Put;
import org.dmfs.android.contentpal.predicates.AllOf;
import org.dmfs.android.contentpal.predicates.AnyOf;
import org.dmfs.android.contentpal.predicates.EqArg;
import org.dmfs.android.contentpal.predicates.Not;
import org.dmfs.android.contentpal.predicates.ReferringTo;
import org.dmfs.android.contentpal.queues.BasicOperationsQueue;
import org.dmfs.android.contentpal.rowdata.CharSequenceRowData;
import org.dmfs.android.contentpal.rowdata.Composite;
import org.dmfs.android.contentpal.rowdata.EmptyRowData;
import org.dmfs.android.contentpal.rowsnapshots.VirtualRowSnapshot;
import org.dmfs.android.contentpal.tables.Synced;
import org.dmfs.android.contenttestpal.operations.AssertEmptyTable;
import org.dmfs.android.contenttestpal.operations.AssertRelated;
import org.dmfs.iterables.SingletonIterable;
import org.dmfs.jems.iterable.elementary.Seq;
import org.dmfs.jems.optional.elementary.Present;
import org.dmfs.opentaskspal.tables.InstanceTable;
import org.dmfs.opentaskspal.tables.LocalTaskListsTable;
import org.dmfs.opentaskspal.tables.TaskListScoped;
import org.dmfs.opentaskspal.tables.TaskListsTable;
import org.dmfs.opentaskspal.tables.TasksTable;
import org.dmfs.opentaskspal.tasks.ExDatesTaskData;
import org.dmfs.opentaskspal.tasks.RDatesTaskData;
import org.dmfs.opentaskspal.tasks.RRuleTaskData;
import org.dmfs.opentaskspal.tasks.StatusData;
import org.dmfs.opentaskspal.tasks.TimeData;
import org.dmfs.opentaskspal.tasks.TitleData;
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
import static org.dmfs.jems.optional.elementary.Absent.absent;
import static org.junit.Assert.assertThat;


/**
 * Test {@link TaskProvider} for correctly detaching completed instances.
 *
 * @author Marten Gajda
 */
@RunWith(AndroidJUnit4.class)
public class TaskProviderDetachInstancesTest
{
    private String mAuthority;
    private ContentProviderClient mClient;
    private Account mTestAccount = new Account("testname", "testtype");


    @Before
    public void setUp() throws Exception
    {
        Context context = InstrumentationRegistry.getTargetContext();
        mAuthority = AuthorityUtil.taskAuthority(context);
        mClient = context.getContentResolver().acquireContentProviderClient(mAuthority);

        // Assert that tables are empty:
        OperationsQueue queue = new BasicOperationsQueue(mClient);
        queue.flush();
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
     * Test if the first instance of a task with a DTSTART, DUE and an RRULE is correctly detached when completed.
     */
    @Test
    public void testRRule() throws InvalidRecurrenceRuleException, RemoteException, OperationApplicationException
    {
        RowSnapshot<TaskLists> taskList = new VirtualRowSnapshot<>(new Synced<>(mTestAccount, new TaskListsTable(mAuthority)));
        Table<Instances> instancesTable = new InstanceTable(mAuthority);
        RowSnapshot<Tasks> task = new VirtualRowSnapshot<>(new TaskListScoped(taskList, new TasksTable(mAuthority)));

        Duration hour = new Duration(1, 0, 3600 /* 1 hour */);
        DateTime start = DateTime.parse("20180104T123456Z");
        DateTime due = start.addDuration(hour);
        DateTime localStart = start.shiftTimeZone(TimeZone.getDefault());

        Duration day = new Duration(1, 1, 0);

        DateTime second = localStart.addDuration(day);
        DateTime third = second.addDuration(day);
        DateTime fourth = third.addDuration(day);
        DateTime fifth = fourth.addDuration(day);

        DateTime localDue = due.shiftTimeZone(TimeZone.getDefault());

        OperationsQueue queue = new BasicOperationsQueue(mClient);
        queue.enqueue(new Seq<>(
                new Put<>(taskList, new EmptyRowData<>()),
                new Put<>(task,
                        new Composite<>(
                                new TimeData<>(start, due),
                                new RRuleTaskData(new RecurrenceRule("FREQ=DAILY;COUNT=5", RecurrenceRule.RfcMode.RFC2445_LAX))))
        ));
        queue.flush();

        assertThat(new Seq<>(
                        // update the first non-closed instance
                        new BulkUpdate<>(instancesTable, new StatusData<>(Tasks.STATUS_COMPLETED),
                                new AllOf<>(new ReferringTo<>(Instances.TASK_ID, task),
                                        new EqArg<>(Instances.DISTANCE_FROM_CURRENT, 0)))
                ),
                resultsIn(queue,
                        /*
                         * We expect three tasks:
                         * - the original master with updated RRULE, DTSTART and DUE
                         * - a deleted instance
                         * - a detached task
                         */

                        // the original master
                        new Assert<>(task,
                                new Composite<>(
                                        new TimeData<>(start.addDuration(day), due.addDuration(day)),
                                        new CharSequenceRowData<>(Tasks.RRULE, "FREQ=DAILY;COUNT=4"))),
                        // there is one instance referring to the master (the old second instance, now first)
                        new Counted<>(1, new AssertRelated<>(instancesTable, Instances.TASK_ID, task)),
                        // the detached task instance:
                        new Counted<>(1, new BulkAssert<>(new Synced<>(mTestAccount, instancesTable),
                                new Composite<>(
                                        new InstanceTestData(localStart, localDue, absent(), -1),
                                        new CharSequenceRowData<>(Tasks.STATUS, String.valueOf(Tasks.STATUS_COMPLETED))),
                                new Not<>(new ReferringTo<>(Instances.TASK_ID, task)))),
                        // the deleted task (doesn't have an instance)
                        new Counted<>(1, new BulkAssert<>(new Synced<>(mTestAccount, new TasksTable(mAuthority)),
                                new Composite<>(new TimeData<>(start, due)),
                                new AllOf<>(
                                        new ReferringTo<>(Tasks.ORIGINAL_INSTANCE_ID, task),
                                        new EqArg<>(Tasks._DELETED, 1)))),
                        // the former 2nd instance (now first)
                        new AssertRelated<>(new Synced<>(mTestAccount, instancesTable), Instances.TASK_ID, task,
                                new InstanceTestData(second, second.addDuration(hour), new Present<>(second), 0),
                                new EqArg<>(Instances.INSTANCE_ORIGINAL_TIME, second.getTimestamp()))));
    }


    /**
     * Test if two instances of a task with a DTSTART, DUE and an RRULE are detached correctly.
     */
    @Test
    public void testRRuleCompleteAll() throws InvalidRecurrenceRuleException, RemoteException, OperationApplicationException
    {
        RowSnapshot<TaskLists> taskList = new VirtualRowSnapshot<>(new Synced<>(mTestAccount, new TaskListsTable(mAuthority)));
        Table<Instances> instancesTable = new InstanceTable(mAuthority);
        RowSnapshot<Tasks> task = new VirtualRowSnapshot<>(new TaskListScoped(taskList, new TasksTable(mAuthority)));

        Duration hour = new Duration(1, 0, 3600 /* 1 hour */);
        DateTime start = DateTime.parse("20180104T123456Z");
        DateTime due = start.addDuration(hour);
        DateTime localStart = start.shiftTimeZone(TimeZone.getDefault());

        Duration day = new Duration(1, 1, 0);

        DateTime second = localStart.addDuration(day);
        DateTime third = second.addDuration(day);
        DateTime fourth = third.addDuration(day);
        DateTime fifth = fourth.addDuration(day);

        DateTime localDue = due.shiftTimeZone(TimeZone.getDefault());

        OperationsQueue queue = new BasicOperationsQueue(mClient);
        queue.enqueue(new Seq<>(
                new Put<>(taskList, new EmptyRowData<>()),
                new Put<>(task,
                        new Composite<>(
                                new TitleData("Test-Task"),
                                new TimeData<>(start, due),
                                new RRuleTaskData(new RecurrenceRule("FREQ=DAILY;COUNT=2", RecurrenceRule.RfcMode.RFC2445_LAX)))),
                // complete the first non-closed instance
                new BulkUpdate<>(instancesTable, new StatusData<>(Tasks.STATUS_COMPLETED),
                        new AllOf<>(new ReferringTo<>(Instances.TASK_ID, task),
                                new EqArg<>(Instances.DISTANCE_FROM_CURRENT, 0)))
        ));
        queue.flush();

        Synced<Tasks> tasksTable = new Synced<>(mTestAccount, new TasksTable(mAuthority));
        Synced<Instances> syncedInstances = new Synced<>(mTestAccount, instancesTable);
        assertThat(new Seq<>(
                        // update the second instance
                        new BulkUpdate<>(instancesTable, new StatusData<>(Tasks.STATUS_COMPLETED),
                                new AllOf<>(new ReferringTo<>(Instances.TASK_ID, task),
                                        new EqArg<>(Instances.DISTANCE_FROM_CURRENT, 0)))
                ),
                resultsIn(queue,
                        /*
                         * We expect five tasks:
                         * - the original master with updated RRULE, DTSTART and DUE, deleted
                         * - a completed and deleted overrides for the first and second instance
                         * - a detached first and second instance
                         */

                        // the original master
                        new Assert<>(task,
                                new Composite<>(
                                        // points to former second instance before being deleted
                                        new TimeData<>(start.addDuration(day), due.addDuration(day)),
                                        new CharSequenceRowData<>(Tasks.RRULE, "FREQ=DAILY;COUNT=1"),
                                        new CharSequenceRowData<>(Tasks._DELETED, "1"))),
                        // there is no instance referring to the master because it has been fully completed (and deleted)
                        new Counted<>(0, new AssertRelated<>(instancesTable, Instances.TASK_ID, task)),
                        // the first detached task instance:
                        new Counted<>(1, new BulkAssert<>(syncedInstances,
                                new Composite<>(
                                        new InstanceTestData(localStart, localDue, absent(), -1),
                                        new CharSequenceRowData<>(Tasks.STATUS, String.valueOf(Tasks.STATUS_COMPLETED))),
                                new AllOf<>(
                                        new EqArg<>(Instances.INSTANCE_START, start.getTimestamp()),
                                        new Not<>(new ReferringTo<>(Instances.TASK_ID, task))))),
                        // the second detached task instance:
                        new Counted<>(1, new BulkAssert<>(syncedInstances,
                                new Composite<>(
                                        new InstanceTestData(second, second.addDuration(new Duration(1, 0, 3600)), absent(), -1),
                                        new CharSequenceRowData<>(Tasks.STATUS, String.valueOf(Tasks.STATUS_COMPLETED))),
                                new AllOf<>(
                                        new EqArg<>(Instances.INSTANCE_START, second.getTimestamp()),
                                        new Not<>(new ReferringTo<>(Instances.TASK_ID, task))))),
                        // two instances total, both completed
                        new Counted<>(2,
                                new BulkAssert<>(
                                        syncedInstances,
                                        new CharSequenceRowData<>(Tasks.STATUS, String.valueOf(Tasks.STATUS_COMPLETED)),
                                        new AnyOf<>())),
                        // five tasks in total
                        new Counted<>(5,
                                new BulkAssert<>(
                                        tasksTable,
                                        new TitleData("Test-Task"),
                                        new AnyOf<>())),
                        // three deleted tasks in total
                        new Counted<>(3,
                                new BulkAssert<>(
                                        tasksTable,
                                        new TitleData("Test-Task"),
                                        new EqArg<>(Tasks._DELETED, 1)))));
    }


    /**
     * Test if two instances of a task with a DTSTART, DUE, RRULE and RDATE are detached correctly.
     */
    @Test
    public void testRRuleRDateCompleteFirstTwo() throws InvalidRecurrenceRuleException, RemoteException, OperationApplicationException
    {
        RowSnapshot<TaskLists> taskList = new VirtualRowSnapshot<>(new Synced<>(mTestAccount, new TaskListsTable(mAuthority)));
        Table<Instances> instancesTable = new InstanceTable(mAuthority);
        RowSnapshot<Tasks> task = new VirtualRowSnapshot<>(new TaskListScoped(taskList, new TasksTable(mAuthority)));

        Duration hour = new Duration(1, 0, 3600 /* 1 hour */);
        DateTime start = DateTime.parse("20180104T123456Z");
        DateTime due = start.addDuration(hour);
        DateTime localStart = start.shiftTimeZone(TimeZone.getDefault());

        Duration day = new Duration(1, 1, 0);

        DateTime second = localStart.addDuration(day);
        DateTime third = second.addDuration(day);
        DateTime fourth = third.addDuration(day);
        DateTime fifth = fourth.addDuration(day);

        DateTime localDue = due.shiftTimeZone(TimeZone.getDefault());

        OperationsQueue queue = new BasicOperationsQueue(mClient);
        queue.enqueue(new Seq<>(
                new Put<>(taskList, new EmptyRowData<>()),
                new Put<>(task,
                        new Composite<>(
                                new TitleData("Test-Task"),
                                new TimeData<>(start, due),
                                new RRuleTaskData(new RecurrenceRule("FREQ=DAILY;INTERVAL=2;COUNT=2", RecurrenceRule.RfcMode.RFC2445_LAX)),
                                new RDatesTaskData(
                                        new Seq<>(
                                                DateTime.parse("20180103T123456Z"),
                                                DateTime.parse("20180105T123456Z"),
                                                DateTime.parse("20180107T123456Z"))))),
                // update the first non-closed instance
                new BulkUpdate<>(instancesTable, new StatusData<>(Tasks.STATUS_COMPLETED),
                        new AllOf<>(new ReferringTo<>(Instances.TASK_ID, task),
                                new EqArg<>(Instances.DISTANCE_FROM_CURRENT, 0)))
        ));
        queue.flush();

        Synced<Tasks> tasksTable = new Synced<>(mTestAccount, new TasksTable(mAuthority));
        Synced<Instances> syncedInstances = new Synced<>(mTestAccount, instancesTable);
        assertThat(new Seq<>(
                        // update the second instance
                        new BulkUpdate<>(instancesTable, new StatusData<>(Tasks.STATUS_COMPLETED),
                                new AllOf<>(new ReferringTo<>(Instances.TASK_ID, task),
                                        new EqArg<>(Instances.DISTANCE_FROM_CURRENT, 0)))
                ),
                resultsIn(queue,
                        /*
                         * We expect five tasks:
                         * - the original master with updated RRULE, RDATES, DTSTART and DUE, deleted
                         * - completed and deleted overrides for the first and second instance
                         * - a detached first and second instance
                         */

                        // the first detached task instance:
                        new Counted<>(1, new BulkAssert<>(syncedInstances,
                                new Composite<>(
                                        new InstanceTestData(DateTime.parse("20180103T123456Z"), DateTime.parse("20180103T133456Z"), absent(), -1),
                                        new CharSequenceRowData<>(Tasks.STATUS, String.valueOf(Tasks.STATUS_COMPLETED))),
                                new AllOf<>(
                                        new EqArg<>(Instances.INSTANCE_START, DateTime.parse("20180103T123456Z").getTimestamp()),
                                        new Not<>(new ReferringTo<>(Instances.TASK_ID, task))))),
                        // the original master has been updated
                        new Assert<>(task,
                                new Composite<>(
                                        // points to former third instance before being deleted
                                        new TimeData<>(start.addDuration(day).addDuration(day), due.addDuration(day).addDuration(day)),
                                        new CharSequenceRowData<>(Tasks.RRULE, "FREQ=DAILY;INTERVAL=2;COUNT=1"),
                                        new CharSequenceRowData<>(Tasks._DELETED, "0"),
                                        new RDatesTaskData(
                                                new Seq<>(
                                                        DateTime.parse("20180105T123456Z"),
                                                        DateTime.parse("20180107T123456Z"))))),
                        // there is one instance referring to the master
                        new Counted<>(1, new AssertRelated<>(instancesTable, Instances.TASK_ID, task,
                                new CharSequenceRowData<>(Instances.INSTANCE_ORIGINAL_TIME,
                                        String.valueOf(DateTime.parse("20180105T123456Z").getTimestamp())))),
                        // the second detached task instance:
                        new Counted<>(1, new BulkAssert<>(syncedInstances,
                                new Composite<>(
                                        new InstanceTestData(start, due, absent(), -1),
                                        new CharSequenceRowData<>(Tasks.STATUS, String.valueOf(Tasks.STATUS_COMPLETED))),
                                new AllOf<>(
                                        new EqArg<>(Instances.INSTANCE_START, start.getTimestamp()),
                                        new Not<>(new ReferringTo<>(Instances.TASK_ID, task))))),
                        // two completed instances, neither of them referring to the master
                        new Counted<>(2,
                                new BulkAssert<>(
                                        syncedInstances,
                                        new CharSequenceRowData<>(Tasks.STATUS, String.valueOf(Tasks.STATUS_COMPLETED)),
                                        new AllOf<>(
                                                new EqArg<>(Instances.DISTANCE_FROM_CURRENT, -1),
                                                new Not<>(new ReferringTo<>(Instances.TASK_ID, task))))),
                        // one incomplete instance , the first instance of the new master
                        new Counted<>(1,
                                new BulkAssert<>(
                                        syncedInstances,
                                        new CharSequenceRowData<>(Tasks.STATUS, String.valueOf(Tasks.STATUS_NEEDS_ACTION)),
                                        new AllOf<>(
                                                new EqArg<>(Instances.DISTANCE_FROM_CURRENT, 0),
                                                new ReferringTo<>(Instances.TASK_ID, task)))),
                        // five tasks in total (two deleted overrides, two detached ones and the new master)
                        new Counted<>(5,
                                new BulkAssert<>(
                                        tasksTable,
                                        new TitleData("Test-Task"),
                                        new AnyOf<>())),
                        // two deleted tasks in total (the old overrides)
                        new Counted<>(2,
                                new BulkAssert<>(
                                        tasksTable,
                                        new TitleData("Test-Task"),
                                        new EqArg<>(Tasks._DELETED, 1)))));
    }


    /**
     * Test if two instances of a task with a DTSTART, DUE, RRULE, RDATE and EXDATE are detached correctly.
     */
    @Test
    public void testRRuleRDateCompleteWithExdates() throws InvalidRecurrenceRuleException, RemoteException, OperationApplicationException
    {
        RowSnapshot<TaskLists> taskList = new VirtualRowSnapshot<>(new Synced<>(mTestAccount, new TaskListsTable(mAuthority)));
        Table<Instances> instancesTable = new InstanceTable(mAuthority);
        RowSnapshot<Tasks> task = new VirtualRowSnapshot<>(new TaskListScoped(taskList, new TasksTable(mAuthority)));

        Duration hour = new Duration(1, 0, 3600 /* 1 hour */);
        DateTime start = DateTime.parse("20180104T123456Z");
        DateTime due = start.addDuration(hour);
        DateTime localStart = start.shiftTimeZone(TimeZone.getDefault());

        Duration day = new Duration(1, 1, 0);

        DateTime second = localStart.addDuration(day);
        DateTime third = second.addDuration(day);
        DateTime fourth = third.addDuration(day);
        DateTime fifth = fourth.addDuration(day);

        DateTime localDue = due.shiftTimeZone(TimeZone.getDefault());

        OperationsQueue queue = new BasicOperationsQueue(mClient);
        queue.enqueue(new Seq<>(
                new Put<>(taskList, new EmptyRowData<>()),
                new Put<>(task,
                        new Composite<>(
                                new TitleData("Test-Task"),
                                new TimeData<>(start, due),
                                new RRuleTaskData(new RecurrenceRule("FREQ=DAILY;INTERVAL=2;COUNT=2", RecurrenceRule.RfcMode.RFC2445_LAX)),
                                new RDatesTaskData(
                                        new Seq<>(
                                                DateTime.parse("20180105T123456Z"),
                                                DateTime.parse("20180107T123456Z"))),
                                new ExDatesTaskData(
                                        new Seq<>(
                                                DateTime.parse("20180104T123456Z"),
                                                DateTime.parse("20180105T123456Z"))))),
                // update the first non-closed instance
                new BulkUpdate<>(instancesTable, new StatusData<>(Tasks.STATUS_COMPLETED),
                        new AllOf<>(new ReferringTo<>(Instances.TASK_ID, task),
                                new EqArg<>(Instances.DISTANCE_FROM_CURRENT, 0)))
        ));
        queue.flush();

        Synced<Tasks> tasksTable = new Synced<>(mTestAccount, new TasksTable(mAuthority));
        Synced<Instances> syncedInstances = new Synced<>(mTestAccount, instancesTable);
        assertThat(new Seq<>(
                        // update the second instance
                        new BulkUpdate<>(instancesTable, new StatusData<>(Tasks.STATUS_COMPLETED),
                                new AllOf<>(new ReferringTo<>(Instances.TASK_ID, task),
                                        new EqArg<>(Instances.DISTANCE_FROM_CURRENT, 0)))
                ),
                resultsIn(queue,
                        /*
                         * We expect five tasks:
                         * - the original master deleted
                         * - completed and deleted overrides for the first and second instance
                         * - detached first and second instances
                         */

                        // the first detached task instance:
                        new Counted<>(1, new BulkAssert<>(syncedInstances,
                                new Composite<>(
                                        new InstanceTestData(DateTime.parse("20180106T123456Z"), DateTime.parse("20180106T133456Z"), absent(), -1),
                                        new CharSequenceRowData<>(Tasks.STATUS, String.valueOf(Tasks.STATUS_COMPLETED))),
                                new AllOf<>(
                                        new EqArg<>(Instances.INSTANCE_START, DateTime.parse("20180106T123456Z").getTimestamp()),
                                        new Not<>(new ReferringTo<>(Instances.TASK_ID, task))))),
                        // the original master has been deleted
                        new Counted<>(0, new Assert<>(task, new Composite<>(new EmptyRowData<>()))),
                        // there is no instance referring to the master
                        new Counted<>(0, new AssertRelated<>(instancesTable, Instances.TASK_ID, task)),
                        // the second detached task instance:
                        new Counted<>(1, new BulkAssert<>(syncedInstances,
                                new Composite<>(
                                        new InstanceTestData(DateTime.parse("20180107T123456Z"), DateTime.parse("20180107T133456Z"), absent(), -1),
                                        new CharSequenceRowData<>(Tasks.STATUS, String.valueOf(Tasks.STATUS_COMPLETED))),
                                new AllOf<>(
                                        new EqArg<>(Instances.INSTANCE_START, DateTime.parse("20180107T123456Z").getTimestamp()),
                                        new Not<>(new ReferringTo<>(Instances.TASK_ID, task))))),
                        // two completed instances, neither of them referring to the master
                        new Counted<>(2,
                                new BulkAssert<>(
                                        syncedInstances,
                                        new CharSequenceRowData<>(Tasks.STATUS, String.valueOf(Tasks.STATUS_COMPLETED)),
                                        new AllOf<>(
                                                new EqArg<>(Instances.DISTANCE_FROM_CURRENT, -1),
                                                new Not<>(new ReferringTo<>(Instances.TASK_ID, task))))),
                        // five tasks in total (two deleted overrides, two detached ones and the old master)
                        new Counted<>(5,
                                new BulkAssert<>(
                                        tasksTable,
                                        new TitleData("Test-Task"),
                                        new AnyOf<>())),
                        // three deleted tasks in total (the old overrides and the old master)
                        new Counted<>(3,
                                new BulkAssert<>(
                                        tasksTable,
                                        new TitleData("Test-Task"),
                                        new EqArg<>(Tasks._DELETED, 1)))));
    }


    /**
     * Test if two instances of a task with a DTSTART, DUE, RRULE, RDATE and EXDATE are detached correctly.
     */
    @Test
    public void testRRuleRDateCompleteOnlyRRuleInstances() throws InvalidRecurrenceRuleException, RemoteException, OperationApplicationException
    {
        RowSnapshot<TaskLists> taskList = new VirtualRowSnapshot<>(new Synced<>(mTestAccount, new TaskListsTable(mAuthority)));
        Table<Instances> instancesTable = new InstanceTable(mAuthority);
        RowSnapshot<Tasks> task = new VirtualRowSnapshot<>(new TaskListScoped(taskList, new TasksTable(mAuthority)));

        Duration hour = new Duration(1, 0, 3600 /* 1 hour */);
        DateTime start = DateTime.parse("20180104T123456Z");
        DateTime due = start.addDuration(hour);
        DateTime localStart = start.shiftTimeZone(TimeZone.getDefault());

        Duration day = new Duration(1, 1, 0);

        DateTime second = localStart.addDuration(day);
        DateTime third = second.addDuration(day);
        DateTime fourth = third.addDuration(day);
        DateTime fifth = fourth.addDuration(day);

        DateTime localDue = due.shiftTimeZone(TimeZone.getDefault());

        OperationsQueue queue = new BasicOperationsQueue(mClient);
        queue.enqueue(new Seq<>(
                new Put<>(taskList, new EmptyRowData<>()),
                new Put<>(task,
                        new Composite<>(
                                new TitleData("Test-Task"),
                                new TimeData<>(start, due),
                                new RRuleTaskData(new RecurrenceRule("FREQ=DAILY;INTERVAL=2;COUNT=2", RecurrenceRule.RfcMode.RFC2445_LAX)),
                                new RDatesTaskData(
                                        new Seq<>(
                                                DateTime.parse("20180105T123456Z"),
                                                DateTime.parse("20180107T123456Z"))),
                                new ExDatesTaskData(
                                        new Seq<>(
                                                DateTime.parse("20180104T123456Z")))))
/*                // update the first non-closed instance
                new BulkUpdate<>(instancesTable, new StatusData<>(Tasks.STATUS_COMPLETED),
                        new AllOf<>(new ReferringTo<>(Instances.TASK_ID, task),
                                new EqArg<>(Instances.DISTANCE_FROM_CURRENT, 0)))*/
        ));
        queue.flush();

        Synced<Tasks> tasksTable = new Synced<>(mTestAccount, new TasksTable(mAuthority));
        Synced<Instances> syncedInstances = new Synced<>(mTestAccount, instancesTable);
        assertThat(new Seq<>(
                        // update the second instance
                        new BulkUpdate<>(instancesTable, new StatusData<>(Tasks.STATUS_COMPLETED),
                                new AllOf<>(new ReferringTo<>(Instances.TASK_ID, task),
                                        new EqArg<>(Instances.DISTANCE_FROM_CURRENT, 0)))
                ),
                resultsIn(queue,
                        /*
                         * We expect five tasks:
                         * - the original master deleted
                         * - completed and deleted overrides for the first and second instance
                         * - detached first and second instances
                         */

                        // the first detached task instance:
                        new Counted<>(1, new BulkAssert<>(syncedInstances,
                                new Composite<>(
                                        new InstanceTestData(DateTime.parse("20180105T123456Z"), DateTime.parse("20180105T133456Z"), absent(), -1),
                                        new CharSequenceRowData<>(Tasks.STATUS, String.valueOf(Tasks.STATUS_COMPLETED))),
                                new AllOf<>(
                                        new EqArg<>(Instances.INSTANCE_START, DateTime.parse("20180105T123456Z").getTimestamp()),
                                        new Not<>(new ReferringTo<>(Instances.TASK_ID, task))))),
                        // the original master has been updated
                        new Assert<>(task,
                                new Composite<>(
                                        new TimeData<>(DateTime.parse("20180106T123456Z"), DateTime.parse("20180106T133456Z")),
                                        new CharSequenceRowData<>(Tasks.RRULE, "FREQ=DAILY;INTERVAL=2;COUNT=1"),
                                        new CharSequenceRowData<>(Tasks._DELETED, "0"),
                                        new RDatesTaskData(
                                                new Seq<>(
                                                        DateTime.parse("20180107T123456Z"))))),
                        // the second detached task instance:
                      /*  new Counted<>(1, new BulkAssert<>(syncedInstances,
                                new Composite<>(
                                        new InstanceTestData(DateTime.parse("20180106T123456Z"), DateTime.parse("20180106T133456Z"), absent(), -1),
                                        new CharSequenceRowData<>(Tasks.STATUS, String.valueOf(Tasks.STATUS_COMPLETED))),
                                new AllOf<>(
                                        new EqArg<>(Instances.INSTANCE_START, DateTime.parse("20180106T123456Z").getTimestamp()),
                                        new Not<>(new ReferringTo<>(Instances.TASK_ID, task))))),*/
                        // one completed instance, not referring to the master
                        new Counted<>(1,
                                new BulkAssert<>(
                                        syncedInstances,
                                        new CharSequenceRowData<>(Tasks.STATUS, String.valueOf(Tasks.STATUS_COMPLETED)),
                                        new AllOf<>(
                                                new EqArg<>(Instances.DISTANCE_FROM_CURRENT, -1),
                                                new Not<>(new ReferringTo<>(Instances.TASK_ID, task))))),
                        // three tasks in total (one deleted override, one detached one and the master)
                        new Counted<>(3,
                                new BulkAssert<>(
                                        tasksTable,
                                        new TitleData("Test-Task"),
                                        new AnyOf<>())),
                        // three deleted tasks in total (the old overrides and the old master)
                        new Counted<>(1,
                                new BulkAssert<>(
                                        tasksTable,
                                        new TitleData("Test-Task"),
                                        new EqArg<>(Tasks._DELETED, 1)))));
    }


    /**
     * Test if two all-day instances of a task with a DTSTART, DUE, RRULE, RDATE and EXDATE are detached correctly.
     */
    @Test
    public void testRRuleRDateCompleteWithExdatesAllDay() throws InvalidRecurrenceRuleException, RemoteException, OperationApplicationException
    {
        RowSnapshot<TaskLists> taskList = new VirtualRowSnapshot<>(new Synced<>(mTestAccount, new TaskListsTable(mAuthority)));
        Table<Instances> instancesTable = new InstanceTable(mAuthority);
        RowSnapshot<Tasks> task = new VirtualRowSnapshot<>(new TaskListScoped(taskList, new TasksTable(mAuthority)));

        Duration hour = new Duration(1, 1, 0);
        DateTime start = DateTime.parse("20180104");
        DateTime due = start.addDuration(hour);

        Duration day = new Duration(1, 1, 0);

        OperationsQueue queue = new BasicOperationsQueue(mClient);
        queue.enqueue(new Seq<>(
                new Put<>(taskList, new EmptyRowData<>()),
                new Put<>(task,
                        new Composite<>(
                                new TitleData("Test-Task"),
                                new TimeData<>(start, due),
                                new RRuleTaskData(new RecurrenceRule("FREQ=DAILY;INTERVAL=2;COUNT=2", RecurrenceRule.RfcMode.RFC2445_LAX)),
                                new RDatesTaskData(
                                        new Seq<>(
                                                DateTime.parse("20180105"),
                                                DateTime.parse("20180107"))),
                                new ExDatesTaskData(
                                        new Seq<>(
                                                DateTime.parse("20180104"),
                                                DateTime.parse("20180105"))))),
                // update the first non-closed instance
                new BulkUpdate<>(instancesTable, new StatusData<>(Tasks.STATUS_COMPLETED),
                        new AllOf<>(new ReferringTo<>(Instances.TASK_ID, task),
                                new EqArg<>(Instances.DISTANCE_FROM_CURRENT, 0)))
        ));
        queue.flush();

        Synced<Tasks> tasksTable = new Synced<>(mTestAccount, new TasksTable(mAuthority));
        Synced<Instances> syncedInstances = new Synced<>(mTestAccount, instancesTable);
        assertThat(new Seq<>(
                        // update the second instance
                        new BulkUpdate<>(instancesTable, new StatusData<>(Tasks.STATUS_COMPLETED),
                                new AllOf<>(new ReferringTo<>(Instances.TASK_ID, task),
                                        new EqArg<>(Instances.DISTANCE_FROM_CURRENT, 0)))
                ),
                resultsIn(queue,
                        /*
                         * We expect five tasks:
                         * - the original master deleted
                         * - completed and deleted overrides for the first and second instance
                         * - detached first and second instances
                         */

                        // the first detached task instance:
                        new Counted<>(1, new BulkAssert<>(syncedInstances,
                                new Composite<>(
                                        new InstanceTestData(DateTime.parse("20180106"), DateTime.parse("20180107"), absent(), -1),
                                        new CharSequenceRowData<>(Tasks.STATUS, String.valueOf(Tasks.STATUS_COMPLETED))),
                                new AllOf<>(
                                        new EqArg<>(Instances.INSTANCE_START, DateTime.parse("20180106").getTimestamp()),
                                        new Not<>(new ReferringTo<>(Instances.TASK_ID, task))))),
                        // the original master has been deleted
                        new Counted<>(0, new Assert<>(task, new Composite<>(new EmptyRowData<>()))),
                        // there is no instance referring to the master
                        new Counted<>(0, new AssertRelated<>(instancesTable, Instances.TASK_ID, task)),
                        // the second detached task instance:
                        new Counted<>(1, new BulkAssert<>(syncedInstances,
                                new Composite<>(
                                        new InstanceTestData(DateTime.parse("20180107"), DateTime.parse("20180108"), absent(), -1),
                                        new CharSequenceRowData<>(Tasks.STATUS, String.valueOf(Tasks.STATUS_COMPLETED))),
                                new AllOf<>(
                                        new EqArg<>(Instances.INSTANCE_START, DateTime.parse("20180107").getTimestamp()),
                                        new Not<>(new ReferringTo<>(Instances.TASK_ID, task))))),
                        // two completed instances, neither of them referring to the master
                        new Counted<>(2,
                                new BulkAssert<>(
                                        syncedInstances,
                                        new CharSequenceRowData<>(Tasks.STATUS, String.valueOf(Tasks.STATUS_COMPLETED)),
                                        new AllOf<>(
                                                new EqArg<>(Instances.DISTANCE_FROM_CURRENT, -1),
                                                new Not<>(new ReferringTo<>(Instances.TASK_ID, task))))),
                        // five tasks in total (two deleted overrides, two detached ones and the old master)
                        new Counted<>(5,
                                new BulkAssert<>(
                                        tasksTable,
                                        new TitleData("Test-Task"),
                                        new AnyOf<>())),
                        // three deleted tasks in total (the old overrides and the old master)
                        new Counted<>(3,
                                new BulkAssert<>(
                                        tasksTable,
                                        new TitleData("Test-Task"),
                                        new EqArg<>(Tasks._DELETED, 1)))));
    }

}
