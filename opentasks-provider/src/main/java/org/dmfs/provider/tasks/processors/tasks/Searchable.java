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

import org.dmfs.provider.tasks.FTSDatabaseHelper;
import org.dmfs.provider.tasks.model.TaskAdapter;
import org.dmfs.provider.tasks.processors.EntityProcessor;
import org.dmfs.provider.tasks.utils.Profiled;


/**
 * An {@link EntityProcessor} to update the fast text search table when inserting or updating a task.
 *
 * @author Marten Gajda
 */
public final class Searchable implements EntityProcessor<TaskAdapter>
{
    private final EntityProcessor<TaskAdapter> mDelegate;


    public Searchable(EntityProcessor<TaskAdapter> delegate)
    {
        mDelegate = delegate;
    }


    @Override
    public TaskAdapter insert(SQLiteDatabase db, TaskAdapter task, boolean isSyncAdapter)
    {
        TaskAdapter result = mDelegate.insert(db, task, isSyncAdapter);
        new Profiled("InsertFTS").run(() -> FTSDatabaseHelper.updateTaskFTSEntries(db, task));
        return result;
    }


    @Override
    public TaskAdapter update(SQLiteDatabase db, TaskAdapter task, boolean isSyncAdapter)
    {
        TaskAdapter result = mDelegate.update(db, task, isSyncAdapter);
        new Profiled("UpdateFTS").run(() -> FTSDatabaseHelper.updateTaskFTSEntries(db, task));
        return result;
    }


    @Override
    public void delete(SQLiteDatabase db, TaskAdapter entityAdapter, boolean isSyncAdapter)
    {
        new Profiled("DeleteFTS").run(() -> mDelegate.delete(db, entityAdapter, isSyncAdapter));
    }
}
