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

package org.dmfs.tasks.utils;

import android.accounts.Account;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.graphics.Color;
import android.os.RemoteException;

import org.dmfs.android.bolts.color.elementary.ValueColor;
import org.dmfs.android.contentpal.operations.Insert;
import org.dmfs.android.contentpal.rowdata.Composite;
import org.dmfs.android.contentpal.tables.Synced;
import org.dmfs.android.contentpal.transactions.BaseTransaction;
import org.dmfs.iterables.SingletonIterable;
import org.dmfs.opentaskspal.tables.TaskListsTable;
import org.dmfs.opentaskspal.tasklists.ColorData;
import org.dmfs.opentaskspal.tasklists.NameData;
import org.dmfs.opentaskspal.tasklists.OwnerData;
import org.dmfs.opentaskspal.tasklists.SyncStatusData;
import org.dmfs.opentaskspal.tasklists.VisibilityData;
import org.dmfs.provider.tasks.AuthorityUtil;
import org.dmfs.tasks.R;
import org.dmfs.tasks.contract.TaskContract;


public class DatabaseInitializedReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (context.getResources().getBoolean(R.bool.opentasks_support_local_lists))
        {
            // The database was just created, insert a local task list
            try
            {
                new BaseTransaction().with(new SingletonIterable<>(
                        new Insert<>(
                                // the table to insert into
                                new Synced<>(new Account(TaskContract.LOCAL_ACCOUNT_NAME, TaskContract.LOCAL_ACCOUNT_TYPE),
                                        new TaskListsTable(AuthorityUtil.taskAuthority(context))),
                                // the data to insert
                                new Composite<>(
                                        new NameData(context.getString(R.string.initial_local_task_list_name)),
                                        new VisibilityData(true),
                                        new OwnerData(""),
                                        new SyncStatusData(true),
                                        new ColorData(new ValueColor(Color.rgb(30, 136, 229)))))))
                        .commit(context.getContentResolver().acquireContentProviderClient(AuthorityUtil.taskAuthority(context)));
            }
            catch (RemoteException | OperationApplicationException e)
            {
                throw new Error("Unable to create initial task list. Something seems to be broken badly.", e);
            }


            /*

            Table<TaskLists> tasklistTable = new Synced<>(new AccountScoped<>(new Account(TaskContract.LOCAL_ACCOUNT_NAME, TaskContract.LOCAL_ACCOUNT_TYPE),
                    new TaskListsTable(AuthorityUtil.taskAuthority(context))));

            RowSnapshot<TaskLists> taskList = new VirtualRowSnapshot<>(tasklistTable);

            Table<TaskContract.Tasks> taskTable = new Synced<>(new TaskListScoped(taskList, new TasksTable(AuthorityUtil.taskAuthority(context))));
            Table<TaskContract.Properties> taskProperties = new Synced<>(new PropertiesTable(AuthorityUtil.taskAuthority(context)));

            RowSnapshot<TaskContract.Tasks> task1 = new VirtualRowSnapshot<>(taskTable);
            RowSnapshot<TaskContract.Tasks> task2 = new VirtualRowSnapshot<>(taskTable);
            RowSnapshot<TaskContract.Tasks> task3 = new VirtualRowSnapshot<>(taskTable);
            RowSnapshot<TaskContract.Tasks> task4 = new VirtualRowSnapshot<>(taskTable);
            try
            {
                new BaseTransaction().with(new Seq<>(
                        new Put<>(taskList, new Composite<>(
                                new NameData(context.getString(R.string.initial_local_task_list_name)),
                                new VisibilityData(true),
                                new OwnerData(""),
                                new SyncStatusData(true),
                                new ColorData(new ValueColor(Color.rgb(30, 136, 229))))),
                        new Put<>(task1, new Composite<>(
                                new TitleData("Task1"))),
                        new Put<>(task2, new Composite<>(
                                new TitleData("Task2"))),
                        new Put<>(task3, new Composite<>(
                                new TitleData("Task3"))),
                        new Put<>(task4, new Composite<>(
                                new TitleData("Task4"))),
                        new Insert<>(taskProperties, new RelationData(task2, TaskContract.Property.Relation.RELTYPE_PARENT, task1)),
                        new Insert<>(taskProperties, new RelationData(task3, TaskContract.Property.Relation.RELTYPE_PARENT, task1)),
                        new Insert<>(taskProperties, new RelationData(task4, TaskContract.Property.Relation.RELTYPE_PARENT, task2))
                )).commit(context.getContentResolver().acquireContentProviderClient(AuthorityUtil.taskAuthority(context)));
            }
            catch (RemoteException e)
            {
                e.printStackTrace();
            }
            catch (OperationApplicationException e)
            {
                e.printStackTrace();
            }*/
        }
    }
}
