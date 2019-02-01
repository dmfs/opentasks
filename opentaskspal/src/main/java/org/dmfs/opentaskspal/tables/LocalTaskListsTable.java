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

package org.dmfs.opentaskspal.tables;

import android.accounts.Account;
import android.support.annotation.NonNull;

import org.dmfs.android.contentpal.Table;
import org.dmfs.android.contentpal.tables.AccountScoped;
import org.dmfs.android.contentpal.tables.DelegatingTable;
import org.dmfs.android.contentpal.tables.Synced;
import org.dmfs.tasks.contract.TaskContract;


/**
 * {@link Table} for {@link TaskContract.TaskLists} scoped on local lists, i.e. lists with 'the local account'.
 *
 * @author Gabor Keszthelyi
 */
public final class LocalTaskListsTable extends DelegatingTable<TaskContract.TaskLists>
{
    public LocalTaskListsTable(@NonNull String authority)
    {
        // TODO When https://github.com/dmfs/opentasks/issues/416 is completed Synced can be removed from here:
        super(new Synced<>(
                new AccountScoped<>(new Account(TaskContract.LOCAL_ACCOUNT_NAME, TaskContract.LOCAL_ACCOUNT_TYPE),
                        new TaskListsTable(authority))));
    }
}
