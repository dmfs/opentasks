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

package org.dmfs.provider.tasks.processors.tasks;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import org.dmfs.provider.tasks.TaskDatabaseHelper;
import org.dmfs.provider.tasks.model.TaskAdapter;
import org.dmfs.provider.tasks.processors.EntityProcessor;
import org.dmfs.tasks.contract.TaskContract;

import java.util.Locale;


/**
 * An {@link EntityProcessor} which updates the {@link TaskContract.Tasks#ORIGINAL_INSTANCE_ID} of any overrides when a master is inserted which has the
 * matching {@link TaskContract.Tasks#ORIGINAL_INSTANCE_SYNC_ID}.
 *
 * @author Marten Gajda
 */
public final class Originating implements EntityProcessor<TaskAdapter>
{
    private final EntityProcessor<TaskAdapter> mDelegate;


    public Originating(EntityProcessor<TaskAdapter> delegate)
    {
        mDelegate = delegate;
    }


    @Override
    public TaskAdapter insert(SQLiteDatabase db, TaskAdapter task, boolean isSyncAdapter)
    {
        TaskAdapter result = mDelegate.insert(db, task, isSyncAdapter);
        String syncId = result.valueOf(TaskAdapter.SYNC_ID);
        if (syncId != null)
        {
            // A master task with a syncId has been inserted.
            // Update original ID of any existing overrides.
            ContentValues values = new ContentValues(1);
            values.put(TaskContract.Tasks.ORIGINAL_INSTANCE_ID, result.id());
            db.update(TaskDatabaseHelper.Tables.TASKS, values, String.format(Locale.ENGLISH, "%s = ?", TaskContract.Tasks.ORIGINAL_INSTANCE_SYNC_ID),
                    new String[] { syncId });
        }
        return result;
    }


    @Override
    public TaskAdapter update(SQLiteDatabase db, TaskAdapter task, boolean isSyncAdapter)
    {
        return mDelegate.update(db, task, isSyncAdapter);
    }


    @Override
    public void delete(SQLiteDatabase db, TaskAdapter task, boolean isSyncAdapter)
    {
        mDelegate.delete(db, task, isSyncAdapter);
    }
}
