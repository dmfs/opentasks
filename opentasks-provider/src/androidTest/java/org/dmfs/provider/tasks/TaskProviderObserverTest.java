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
import org.dmfs.android.contentpal.operations.BulkDelete;
import org.dmfs.android.contentpal.operations.Put;
import org.dmfs.android.contentpal.queues.BasicOperationsQueue;
import org.dmfs.android.contentpal.rowsnapshots.VirtualRowSnapshot;
import org.dmfs.android.contentpal.tables.Synced;
import org.dmfs.android.contenttestpal.operations.AssertEmptyTable;
import org.dmfs.iterables.elementary.Seq;
import org.dmfs.opentaskspal.tables.InstanceTable;
import org.dmfs.opentaskspal.tables.LocalTaskListsTable;
import org.dmfs.opentaskspal.tables.TaskListScoped;
import org.dmfs.opentaskspal.tables.TaskListsTable;
import org.dmfs.opentaskspal.tables.TasksTable;
import org.dmfs.opentaskspal.tasklists.NameData;
import org.dmfs.opentaskspal.tasks.TitleData;
import org.dmfs.tasks.contract.TaskContract;
import org.dmfs.tasks.contract.TaskContract.TaskLists;
import org.dmfs.tasks.contract.TaskContract.Tasks;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import static org.dmfs.android.contentpal.testing.android.uri.UriMatcher.hasParam;
import static org.dmfs.provider.tasks.matchers.NotifiesMatcher.notifies;
import static org.dmfs.provider.tasks.matchers.UriMatcher.authority;
import static org.dmfs.provider.tasks.matchers.UriMatcher.path;
import static org.dmfs.provider.tasks.matchers.UriMatcher.scheme;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;


/**
 * Tests for {@link TaskProvider}.
 *
 * @author Marten Gajda
 */
@RunWith(AndroidJUnit4.class)
public class TaskProviderObserverTest
{
    private String mAuthority;
    private Context mContext;
    private ContentProviderClient mClient;
    private final Account testAccount = new Account("foo", "bar");


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
     * Test notifications for creating one task list and task.
     */
    @Test
    public void testSingleInsert() throws RemoteException, OperationApplicationException
    {
        RowSnapshot<TaskLists> taskList = new VirtualRowSnapshot<>(new LocalTaskListsTable(mAuthority));
        RowSnapshot<Tasks> task = new VirtualRowSnapshot<>(new TaskListScoped(taskList, new TasksTable(mAuthority)));
        OperationsQueue queue = new BasicOperationsQueue(mClient);

        assertThat(new Seq<>(
                        new Put<>(taskList, new NameData("list1")),
                        new Put<>(task, new TitleData("task1"))),
                notifies(
                        TaskContract.getContentUri(mAuthority),
                        queue,
                        containsInAnyOrder(
                                allOf(
                                        scheme("content"),
                                        authority(mAuthority),
                                        path(is("/tasks"))
                                ),
                                allOf(
                                        scheme("content"),
                                        authority(mAuthority),
                                        path(startsWith("/tasks/"))
                                ),
                                allOf(
                                        scheme("content"),
                                        authority(mAuthority),
                                        path(startsWith("/instances"))
                                ),
                                allOf(
                                        scheme("content"),
                                        authority(mAuthority),
                                        path(startsWith("/tasklists/"))
                                ),
                                allOf(
                                        scheme("content"),
                                        authority(mAuthority),
                                        path(is("/tasklists")),
                                        hasParam(TaskContract.CALLER_IS_SYNCADAPTER, "true"),
                                        hasParam(TaskContract.ACCOUNT_NAME, TaskContract.LOCAL_ACCOUNT_NAME),
                                        hasParam(TaskContract.ACCOUNT_TYPE, TaskContract.LOCAL_ACCOUNT_TYPE)
                                ))));
    }


    /**
     * Update a task and check the notifications.
     */
    @Test
    public void testSingleUpdate() throws RemoteException, OperationApplicationException
    {
        RowSnapshot<TaskLists> taskList = new VirtualRowSnapshot<>(new LocalTaskListsTable(mAuthority));
        RowSnapshot<Tasks> task = new VirtualRowSnapshot<>(new TaskListScoped(taskList, new TasksTable(mAuthority)));
        OperationsQueue queue = new BasicOperationsQueue(mClient);

        queue.enqueue(
                new Seq<>(
                        new Put<>(taskList, new NameData("list1")),
                        new Put<>(task, new TitleData("task1"))));
        queue.flush();

        assertThat(new Seq<>(
                        new Put<>(task, new TitleData("task1b"))),
                notifies(
                        TaskContract.getContentUri(mAuthority),
                        queue,
                        // taskprovider should notity the tasks URI iself, the task diretory and the instances directory
                        containsInAnyOrder(
                                allOf(
                                        scheme("content"),
                                        authority(mAuthority),
                                        path(is("/tasks"))
                                ),
                                allOf(
                                        scheme("content"),
                                        authority(mAuthority),
                                        path(startsWith("/tasks/"))
                                ),
                                allOf(
                                        scheme("content"),
                                        authority(mAuthority),
                                        path(is("/instances"))
                                ))));
    }


    /**
     * Test that an update that doesn't change anything doesn't trigger a notification.
     */
    @Test
    public void testNoOpUpdate() throws RemoteException, OperationApplicationException
    {
        RowSnapshot<TaskLists> taskList = new VirtualRowSnapshot<>(new LocalTaskListsTable(mAuthority));
        RowSnapshot<Tasks> task = new VirtualRowSnapshot<>(new TaskListScoped(taskList, new TasksTable(mAuthority)));
        OperationsQueue queue = new BasicOperationsQueue(mClient);

        queue.enqueue(
                new Seq<>(
                        new Put<>(taskList, new NameData("list1")),
                        new Put<>(task, new TitleData("task1"))));
        queue.flush();

        assertThat(new Seq<>(
                        new Put<>(task, new TitleData("task1"))),
                notifies(
                        TaskContract.getContentUri(mAuthority),
                        queue,
                        // there should no notification
                        emptyIterable()));
    }

}
