/*
 * Copyright 2018 dmfs GmbH
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
import android.content.Context;
import android.content.OperationApplicationException;
import android.os.Build;
import android.os.RemoteException;
import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import org.dmfs.android.contentpal.Operation;
import org.dmfs.android.contentpal.OperationsQueue;
import org.dmfs.android.contentpal.RowSnapshot;
import org.dmfs.android.contentpal.operations.Assert;
import org.dmfs.android.contentpal.operations.BulkAssert;
import org.dmfs.android.contentpal.operations.BulkDelete;
import org.dmfs.android.contentpal.operations.BulkUpdate;
import org.dmfs.android.contentpal.operations.Counted;
import org.dmfs.android.contentpal.operations.Put;
import org.dmfs.android.contentpal.predicates.ReferringTo;
import org.dmfs.android.contentpal.queues.BasicOperationsQueue;
import org.dmfs.android.contentpal.rowdata.CharSequenceRowData;
import org.dmfs.android.contentpal.rowdata.Composite;
import org.dmfs.android.contentpal.rowdata.Referring;
import org.dmfs.android.contentpal.rowsnapshots.VirtualRowSnapshot;
import org.dmfs.android.contentpal.transactions.BaseTransaction;
import org.dmfs.android.contenttestpal.operations.AssertEmptyTable;
import org.dmfs.android.contenttestpal.operations.AssertRelated;
import org.dmfs.iterables.SingletonIterable;
import org.dmfs.iterables.elementary.Seq;
import org.dmfs.opentaskspal.tables.InstanceTable;
import org.dmfs.opentaskspal.tables.LocalTaskListsTable;
import org.dmfs.opentaskspal.tables.TaskListsTable;
import org.dmfs.opentaskspal.tables.TasksTable;
import org.dmfs.opentaskspal.tasklists.NameData;
import org.dmfs.opentaskspal.tasks.TimeData;
import org.dmfs.opentaskspal.tasks.TitleData;
import org.dmfs.opentaskstestpal.InstanceTestData;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.tasks.contract.TaskContract.Instances;
import org.dmfs.tasks.contract.TaskContract.TaskLists;
import org.dmfs.tasks.contract.TaskContract.Tasks;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.dmfs.android.contenttestpal.ContentMatcher.resultsIn;
import static org.junit.Assert.assertThat;


/**
 * Tests for {@link TaskProvider}. These tests check various operations on the instances table.
 *
 * @author Gabor Keszthelyi
 * @author Marten Gajda
 */
@RunWith(AndroidJUnit4.class)
public class TaskProviderInstancesTest
{
    private String mAuthority;
    private Context mContext;
    private ContentProviderClient mClient;


    @Before
    public void setUp() throws Exception
    {
        mContext = InstrumentationRegistry.getTargetContext();
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
     * Create a single instance.
     */
    @Ignore("Inserting instances is currently unsupported.")
    @Test
    public void testInsertSingleInstance()
    {
        RowSnapshot<TaskLists> taskList = new VirtualRowSnapshot<>(new LocalTaskListsTable(mAuthority));
        RowSnapshot<Instances> instance = new VirtualRowSnapshot<>(new InstanceTable(mAuthority));

        assertThat(new Seq<>(
                // create a local list
                new Put<>(taskList, new NameData("list1")),
                // insert a new task straight into the instances table
                new Put<>(instance, new Referring<>(Tasks.LIST_ID, taskList, new CharSequenceRowData<>(Tasks.TITLE, "task1")))

        ), resultsIn(mClient,
                new Assert<>(taskList, new NameData("list1")),
                // the task list contains exactly one task with the title "task1"
                new Counted<>(1, new BulkAssert<>(new TasksTable(mAuthority))),
                new AssertRelated<>(new TasksTable(mAuthority), Tasks.LIST_ID, taskList,
                        new Composite<>(
                                new CharSequenceRowData<>(Tasks.TITLE, "task1"))),
                // the instances table contains one instance
                new Counted<>(1, new BulkAssert<>(new InstanceTable(mAuthority))),
                // the instances table contains the given instance
                new Assert<>(instance, new Composite<>(
                        new InstanceTestData(0),
                        new CharSequenceRowData<>(Tasks.TITLE, "task1")))));
    }


    /**
     * Create a single instance and update it.
     */
    @Test
    public void testUpdateSingleInstance()
    {
        RowSnapshot<TaskLists> taskList = new VirtualRowSnapshot<>(new LocalTaskListsTable(mAuthority));
        RowSnapshot<Tasks> task = new VirtualRowSnapshot<>(new TasksTable(mAuthority));

        assertThat(new Seq<>(
                // create a local list
                new Put<>(taskList, new NameData("list1")),
                // insert a new task straight into the instances table
                new Put<>(task, new Referring<>(Tasks.LIST_ID, taskList, new CharSequenceRowData<>(Tasks.TITLE, "task1"))),
                // update the instance
                new BulkUpdate<>(
                        new InstanceTable(mAuthority),
                        new CharSequenceRowData<>(Tasks.TITLE, "Updated"),
                        new ReferringTo<>(Instances.TASK_ID, task))
        ), resultsIn(mClient,
                new Assert<>(taskList, new NameData("list1")),
                // the task list contains exactly one task with the title "Updated"
                new Counted<>(1, new BulkAssert<>(new TasksTable(mAuthority))),
                new AssertRelated<>(new TasksTable(mAuthority), Tasks.LIST_ID, taskList,
                        new Composite<>(
                                new CharSequenceRowData<>(Tasks.TITLE, "Updated"))),
                // the instances table contains one instance
                new Counted<>(1, new BulkAssert<>(new InstanceTable(mAuthority))),
                // the instances table contains the given instance
                new Counted<>(1, new BulkAssert<>(
                        new InstanceTable(mAuthority),
                        new Composite<>(
                                new InstanceTestData(0),
                                new CharSequenceRowData<>(Tasks.TITLE, "Updated")),
                        new ReferringTo<>(Instances.TASK_ID, task)))));
    }


    /**
     * Create a single instance and complete it.
     */
    @Test
    public void testCompleteSingleInstance()
    {
        RowSnapshot<TaskLists> taskList = new VirtualRowSnapshot<>(new LocalTaskListsTable(mAuthority));
        RowSnapshot<Tasks> task = new VirtualRowSnapshot<>(new TasksTable(mAuthority));

        assertThat(new Seq<>(
                // create a local list
                new Put<>(taskList, new NameData("list1")),
                // insert a new task straight into the instances table
                new Put<>(task, new Referring<>(Tasks.LIST_ID, taskList, new CharSequenceRowData<>(Tasks.TITLE, "task1"))),
                // update the instance status
                new BulkUpdate<>(
                        new InstanceTable(mAuthority),
                        (transactionContext, builder) -> builder.withValue(Tasks.STATUS, Tasks.STATUS_COMPLETED),
                        new ReferringTo<>(Instances.TASK_ID, task))
        ), resultsIn(mClient,
                new Assert<>(taskList, new NameData("list1")),
                // the task list contains exactly one task with the title "Updated"
                new Counted<>(1, new BulkAssert<>(new TasksTable(mAuthority))),
                new AssertRelated<>(new TasksTable(mAuthority), Tasks.LIST_ID, taskList,
                        new Composite<>(
                                new CharSequenceRowData<>(Tasks.TITLE, "task1"))),
                // the instances table contains one instance
                new Counted<>(1, new BulkAssert<>(new InstanceTable(mAuthority))),
                // the instances table contains the given instance
                new Counted<>(1, new BulkAssert<>(
                        new InstanceTable(mAuthority),
                        new Composite<>(
                                new InstanceTestData(-1),
                                new CharSequenceRowData<>(Tasks.TITLE, "task1")),
                        new ReferringTo<>(Instances.TASK_ID, task)))));
    }


    /**
     * Create a single instance and delete it.
     */
    @Test
    public void testDeleteSingleInstance()
    {
        RowSnapshot<TaskLists> taskList = new VirtualRowSnapshot<>(new LocalTaskListsTable(mAuthority));
        RowSnapshot<Tasks> task = new VirtualRowSnapshot<>(new TasksTable(mAuthority));

        assertThat(new Seq<>(
                // create a local list
                new Put<>(taskList, new NameData("list1")),
                // insert a new task
                new Put<>(task, new Referring<>(Tasks.LIST_ID, taskList, new CharSequenceRowData<>(Tasks.TITLE, "task1"))),
                // delete the instance
                new BulkDelete<>(new InstanceTable(mAuthority), new ReferringTo<>(Instances.TASK_ID, task))
        ), resultsIn(mClient,
                new Assert<>(taskList, new NameData("list1")),
                // the list does not contain a single task
                new AssertEmptyTable<>(new TasksTable(mAuthority)),
                new AssertEmptyTable<>(new InstanceTable(mAuthority))));

    }


    /**
     * Create a single instance and insert an override for exactly the same instance.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInsertSingleInstanceTwice() throws RemoteException, OperationApplicationException
    {
        RowSnapshot<TaskLists> taskList = new VirtualRowSnapshot<>(new LocalTaskListsTable(mAuthority));
        RowSnapshot<Tasks> task = new VirtualRowSnapshot<>(new TasksTable(mAuthority));
        RowSnapshot<Instances> instance = new VirtualRowSnapshot<>(new InstanceTable(mAuthority));

        DateTime dateTime = DateTime.parse("20180110T224500Z");
        String dtstart = Long.toString(dateTime.getTimestamp());

        new BaseTransaction().with(new Seq<>(
                // create a local list
                new Put<>(taskList, new NameData("list1")),
                // insert a new task into the tasks table (we insert a task to get a RowReference to the new row)
                new Put<>(task,
                        new Composite<>(
                                new Referring<>(Tasks.LIST_ID, taskList),
                                new TimeData<>(dateTime),
                                new TitleData("task1"))),
                new Put<>(instance,
                        new Composite<>(
                                new Referring<>(Tasks.LIST_ID, taskList),
                                new CharSequenceRowData<>(Tasks.DTSTART, "1234"),
                                // insert an instance which would override the original instance, which already exists
                                new Referring<>(Tasks.ORIGINAL_INSTANCE_ID, task),
                                new CharSequenceRowData<>(Tasks.ORIGINAL_INSTANCE_TIME, dtstart),
                                new CharSequenceRowData<>(Tasks.TZ, "UTC"),
                                new CharSequenceRowData<>(Tasks.TITLE, "task1")))

        )).commit(mClient);
    }


    /**
     * Create a single instance and insert an override for a new instance, turning the event into a recurring event.
     */
    @Ignore("Inserting instances is currently not supported.")
    @Test
    public void testInsertSingleInstanceAddAnother()
    {
        RowSnapshot<TaskLists> taskList = new VirtualRowSnapshot<>(new LocalTaskListsTable(mAuthority));
        RowSnapshot<Tasks> task = new VirtualRowSnapshot<>(new TasksTable(mAuthority));
        RowSnapshot<Instances> instance = new VirtualRowSnapshot<>(new InstanceTable(mAuthority));

        DateTime dateTimeOriginal = DateTime.parse("20180110T224500Z");
        // override is one day later
        DateTime dateTimeOverride = DateTime.parse("20180111T224500Z");
        String startOverride = Long.toString(dateTimeOverride.getTimestamp());

        assertThat(new Seq<>(
                // create a local list
                new Put<>(taskList, new NameData("list1")),
                // insert a new task into the tasks table (we insert a task to get a RowReference to the new row)
                new Put<>(task,
                        new Composite<Tasks>(
                                new Referring<>(Tasks.LIST_ID, taskList),
                                new TimeData(dateTimeOriginal),
                                new TitleData("task1"))),
                new Put<>(instance,
                        new Composite<Instances>(
                                new Referring<>(Tasks.LIST_ID, taskList),
                                new CharSequenceRowData<>(Tasks.DTSTART, "1234"),
                                new CharSequenceRowData<>(Tasks.IS_ALLDAY, "0"),
                                // insert an override instance
                                new Referring<>(Tasks.ORIGINAL_INSTANCE_ID, task),
                                new CharSequenceRowData<>(Tasks.ORIGINAL_INSTANCE_TIME, startOverride),
                                new CharSequenceRowData<>(Tasks.TZ, "UTC"),
                                new CharSequenceRowData<>(Tasks.TITLE, "task override")))

        ), resultsIn(mClient,
                new Assert<>(taskList, new NameData("list1")),
                // the task list contains exactly two tasks
                new Counted<>(2, new BulkAssert<>(new TasksTable(mAuthority))),
                // check that the original task has RDATES now, one for the original start and one for the new override
                new Assert<>(task,
                        new Composite<Tasks>(
                                new Referring<>(Tasks.LIST_ID, taskList),
                                new TimeData(dateTimeOriginal),
                                new CharSequenceRowData<>(Tasks.RDATE, "20180110T224500Z,20180111T224500Z"),
                                new TitleData("task1"))),
                // and check there is a task for the override
                new AssertRelated<>(new TasksTable(mAuthority), Tasks.ORIGINAL_INSTANCE_ID, task,
                        new Composite<Tasks>(
                                new Referring<>(Tasks.LIST_ID, taskList),
                                new CharSequenceRowData<>(Tasks.ORIGINAL_INSTANCE_TIME, startOverride),
                                new TimeData(new DateTime(1234)),
                                new TitleData("task override")))
                // TODO: enable tests below once recurrence has been implemented
//                // the instances table contains two instances as well
//                new Counted<>(2, new BulkAssert<>(new InstanceTable(mAuthority))),
//                // one instance is related to the task
//               new AssertRelated<>(new InstanceTable(mAuthority), Instances.TASK_ID, task,
//                        new Composite<>(
//                                new InstanceTestData(new Present<>(dateTimeOriginal), new Absent<>(), new Present<>(dateTimeOriginal), 0),
//                                new CharSequenceRowData<>(Tasks.TITLE, "task1"))),
//                // the other instance is for the override
//                new Assert<>(instance, new Composite<>(
//                        new InstanceTestData(new Present<>(new DateTime(1234)), new Absent<>(), new Present<>(dateTimeOverride), 0),
//                        new CharSequenceRowData<>(Tasks.TITLE, "task override")))
        ));
    }
}
