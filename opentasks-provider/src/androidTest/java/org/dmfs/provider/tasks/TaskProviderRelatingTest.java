/*
 * Copyright 2020 dmfs GmbH
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
import android.content.Context;
import android.os.Build;

import org.dmfs.android.contentpal.Operation;
import org.dmfs.android.contentpal.OperationsQueue;
import org.dmfs.android.contentpal.RowSnapshot;
import org.dmfs.android.contentpal.operations.Assert;
import org.dmfs.android.contentpal.operations.BulkAssert;
import org.dmfs.android.contentpal.operations.BulkDelete;
import org.dmfs.android.contentpal.operations.Counted;
import org.dmfs.android.contentpal.operations.Insert;
import org.dmfs.android.contentpal.operations.Put;
import org.dmfs.android.contentpal.predicates.AllOf;
import org.dmfs.android.contentpal.predicates.EqArg;
import org.dmfs.android.contentpal.predicates.ReferringTo;
import org.dmfs.android.contentpal.queues.BasicOperationsQueue;
import org.dmfs.android.contentpal.rowdata.CharSequenceRowData;
import org.dmfs.android.contentpal.rowdata.Composite;
import org.dmfs.android.contentpal.rowdata.Referring;
import org.dmfs.android.contentpal.rowsnapshots.VirtualRowSnapshot;
import org.dmfs.android.contentpal.tables.Synced;
import org.dmfs.android.contenttestpal.operations.AssertEmptyTable;
import org.dmfs.jems.iterable.elementary.Seq;
import org.dmfs.opentaskspal.tables.InstanceTable;
import org.dmfs.opentaskspal.tables.LocalTaskListsTable;
import org.dmfs.opentaskspal.tables.PropertiesTable;
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

import static org.dmfs.android.contenttestpal.ContentMatcher.resultsIn;
import static org.junit.Assert.assertThat;


/**
 * Tests for {@link TaskProvider} reparenting feature.
 *
 * @author Marten Gajda
 */
@RunWith(AndroidJUnit4.class)
public class TaskProviderRelatingTest
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
                new AssertEmptyTable<>(new PropertiesTable(mAuthority)),
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
                new BulkDelete<>(new PropertiesTable(mAuthority)),
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
     * Create 1 local task list, then create a child Task, related to a parent UID and finally the parent.
     */
    @Test
    public void testRelateTask()
    {
        RowSnapshot<TaskLists> taskList = new VirtualRowSnapshot<>(new Synced<>(testAccount, new TaskListsTable(mAuthority)));
        RowSnapshot<Tasks> taskChild = new VirtualRowSnapshot<>(new TaskListScoped(taskList, new Synced<>(testAccount, new TasksTable(mAuthority))));
        RowSnapshot<Tasks> taskParent = new VirtualRowSnapshot<>(new TaskListScoped(taskList, new Synced<>(testAccount, new TasksTable(mAuthority))));

        assertThat(new Seq<>(
                        new Put<>(taskList, new NameData("list1")),
                        new Put<>(taskChild, new Composite<>(
                                new TitleData("child"),
                                new CharSequenceRowData<>(Tasks._UID, "childUID"))),
                        new Insert<>(new PropertiesTable(mAuthority), new Composite<>(
                                new CharSequenceRowData<>(TaskContract.Property.Relation.MIMETYPE, TaskContract.Property.Relation.CONTENT_ITEM_TYPE),
                                new Referring<>(TaskContract.Property.Relation.TASK_ID, taskChild),
                                new CharSequenceRowData<>(TaskContract.Property.Relation.RELATED_UID, "parentUID"),
                                new CharSequenceRowData<>(TaskContract.Property.Relation.RELATED_TYPE, String.valueOf(TaskContract.Property.Relation.RELTYPE_PARENT))
                        )),
                        new Put<>(taskParent, new Composite<>(
                                new TitleData("parent"),
                                new CharSequenceRowData<>(Tasks._UID, "parentUID")))
                ),
                resultsIn(mClient,
                        new Assert<>(taskList, new NameData("list1")),
                        new Assert<>(taskChild, new Composite<>(
                                new TitleData("child"),
                                new CharSequenceRowData<>(Tasks._UID, "childUID"),
                                new Referring<>(Tasks.PARENT_ID, taskParent))),
                        new Assert<>(taskParent, new Composite<>(
                                new CharSequenceRowData<>(Tasks._UID, "parentUID"),
                                new TitleData("parent"))),
                        new Counted<>(1, new BulkAssert<>(
                                new PropertiesTable(mAuthority),
                                new Composite<>(
                                        new CharSequenceRowData<>(TaskContract.Property.Relation.RELATED_TYPE,
                                                String.valueOf(TaskContract.Property.Relation.RELTYPE_PARENT)),
                                        new CharSequenceRowData<>(TaskContract.Property.Relation.RELATED_UID, "parentUID"),
                                        new Referring<>(TaskContract.Property.Relation.RELATED_ID, taskParent)
                                ),
                                new AllOf<>(
                                        new EqArg<>(TaskContract.Properties.MIMETYPE, TaskContract.Property.Relation.CONTENT_ITEM_TYPE),
                                        new ReferringTo<>(TaskContract.Properties.TASK_ID, taskChild)
                                ))),
                        new Counted<>(0, new BulkAssert<>(
                                new PropertiesTable(mAuthority),
                                new AllOf<>(
                                        new EqArg<>(TaskContract.Properties.MIMETYPE, TaskContract.Property.Relation.CONTENT_ITEM_TYPE),
                                        new ReferringTo<>(TaskContract.Properties.TASK_ID, taskParent)
                                )))
                ));
    }
}
