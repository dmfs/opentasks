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

import org.dmfs.android.contentpal.Table;
import org.dmfs.android.contentpal.tables.DelegatingTable;
import org.dmfs.android.contentpal.tables.Synced;
import org.dmfs.tasks.contract.TaskContract;

import androidx.annotation.NonNull;


/**
 * {@link Table} for {@link TaskContract.TaskLists} scoped on local lists, i.e. lists with 'the local account'.
 *
 * @author Gabor Keszthelyi
 */
public final class LocalTaskListsTable extends DelegatingTable<TaskContract.TaskLists>
{
    public LocalTaskListsTable(@NonNull String authority)
    {
        this(new Account(TaskContract.LOCAL_ACCOUNT_NAME, TaskContract.LOCAL_ACCOUNT_TYPE), authority);
    }


    private LocalTaskListsTable(@NonNull Account localAccount, @NonNull String authority)
    {
        super(new Synced<>(localAccount, new TaskListsTable(authority)));
    }
}
