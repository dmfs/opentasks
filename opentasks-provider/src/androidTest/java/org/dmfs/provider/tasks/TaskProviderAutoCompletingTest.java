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
import android.content.ContentResolver;
import android.content.Context;
import android.os.Build;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.dmfs.android.contentpal.Operation;
import org.dmfs.android.contentpal.OperationsQueue;
import org.dmfs.android.contentpal.RowSnapshot;
import org.dmfs.android.contentpal.operations.Assert;
import org.dmfs.android.contentpal.operations.BulkDelete;
import org.dmfs.android.contentpal.operations.Put;
import org.dmfs.android.contentpal.queues.BasicOperationsQueue;
import org.dmfs.android.contentpal.rowdata.CharSequenceRowData;
import org.dmfs.android.contentpal.rowsnapshots.VirtualRowSnapshot;
import org.dmfs.android.contenttestpal.operations.AssertEmptyTable;
import org.dmfs.iterables.SingletonIterable;
import org.dmfs.iterables.elementary.Seq;
import org.dmfs.opentaskspal.tables.InstanceTable;
import org.dmfs.opentaskspal.tables.LocalTaskListsTable;
import org.dmfs.opentaskspal.tables.TaskListScoped;
import org.dmfs.opentaskspal.tables.TaskListsTable;
import org.dmfs.opentaskspal.tables.TasksTable;
import org.dmfs.opentaskspal.tasklists.NameData;
import org.dmfs.provider.tasks.processors.tasks.AutoCompleting;
import org.dmfs.tasks.contract.TaskContract.TaskLists;
import org.dmfs.tasks.contract.TaskContract.Tasks;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.dmfs.android.contenttestpal.ContentMatcher.resultsIn;
import static org.junit.Assert.assertThat;


/**
 * Test cases for {@link TaskProvider} that check data auto-corrections/auto-completions done by provider for task insert/update.
 * <p>
 * Implementation is in {@link AutoCompleting}.
 *
 * @author Gabor Keszthelyi
 */
@RunWith(AndroidJUnit4.class)
public class TaskProviderAutoCompletingTest
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
     * Test that timezone for all-day tasks are set to null.
     */
    @Test
    public void testInsertTaskWithAllDayAndTimeZone()
    {
        RowSnapshot<TaskLists> taskList = new VirtualRowSnapshot<>(new LocalTaskListsTable(mAuthority));
        RowSnapshot<Tasks> allDayTask = new VirtualRowSnapshot<>(new TaskListScoped(taskList, new TasksTable(mAuthority)));
        RowSnapshot<Tasks> notAllDayTask = new VirtualRowSnapshot<>(new TaskListScoped(taskList, new TasksTable(mAuthority)));

        long start = System.currentTimeMillis();

        assertThat(new Seq<>(
                new Put<>(taskList, new NameData("list1")),
                new Put<>(allDayTask, (transactionContext, builder) -> builder
                        .withValue(Tasks.DTSTART, start)
                        .withValue(Tasks.TZ, "Europe/Berlin")
                        .withValue(Tasks.IS_ALLDAY, 1)
                ),
                new Put<>(notAllDayTask, (transactionContext, builder) -> builder
                        .withValue(Tasks.DTSTART, start)
                        .withValue(Tasks.TZ, "Europe/Paris")
                        .withValue(Tasks.IS_ALLDAY, 0)
                )

        ), resultsIn(mClient,
                new Assert<>(taskList, new NameData("list1")),
                new Assert<>(allDayTask, new CharSequenceRowData<>(Tasks.TZ, null)),
                new Assert<>(notAllDayTask, new CharSequenceRowData<>(Tasks.TZ, "Europe/Paris"))
        ));
    }

}
