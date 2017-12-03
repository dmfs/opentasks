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

package org.dmfs.provider.tasks.processors.tasks;

import android.database.sqlite.SQLiteDatabase;

import org.dmfs.provider.tasks.TaskDatabaseHelper;
import org.dmfs.provider.tasks.model.TaskAdapter;
import org.dmfs.provider.tasks.processors.EntityProcessor;
import org.dmfs.tasks.contract.TaskContract;


/**
 * A processor that performs the actual operations on tasks.
 *
 * @author Marten Gajda
 */
public final class TaskCommitProcessor implements EntityProcessor<TaskAdapter>
{
    @Override
    public TaskAdapter insert(SQLiteDatabase db, TaskAdapter task, boolean isSyncAdapter)
    {
        task.commit(db);
        return task;
    }


    @Override
    public TaskAdapter update(SQLiteDatabase db, TaskAdapter task, boolean isSyncAdapter)
    {
        task.commit(db);
        return task;
    }


    @Override
    public void delete(SQLiteDatabase db, TaskAdapter task, boolean isSyncAdapter)
    {
        String accountType = task.valueOf(TaskAdapter.ACCOUNT_TYPE);

        if (isSyncAdapter || TaskContract.LOCAL_ACCOUNT_TYPE.equals(accountType))
        {
            // this is a local task or it's removed by a sync adapter, in either case we delete it right away
            db.delete(TaskDatabaseHelper.Tables.TASKS, TaskContract.TaskColumns._ID + "=" + task.id(), null);
        }
        else
        {
            // just set the deleted flag otherwise
            task.set(TaskAdapter._DELETED, true);
            task.commit(db);
        }
    }
}
