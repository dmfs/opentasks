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

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.dmfs.provider.tasks.TaskDatabaseHelper;
import org.dmfs.provider.tasks.model.TaskAdapter;
import org.dmfs.provider.tasks.processors.EntityProcessor;
import org.dmfs.tasks.contract.TaskContract;


/**
 * A processor that updates relations for new tasks.
 * <p>
 * In general there is no guarantee that a related task is already in the database when a task is
 * inserted. In such a case we can not set the {@link TaskContract.Property.Relation#RELATED_ID} value. This processor updates the {@link
 * TaskContract.Property.Relation#RELATED_ID} when a task is inserted.
 * <p>
 * It also updates {@link TaskContract.Property.Relation#RELATED_UID} when a tasks
 * is synced the first time and a UID has been set.
 * <p>
 *
 * @author Marten Gajda
 */
public final class Relating implements EntityProcessor<TaskAdapter>
{
    private final EntityProcessor<TaskAdapter> mDelegate;


    public Relating(EntityProcessor<TaskAdapter> delegate)
    {
        mDelegate = delegate;
    }


    @Override
    public TaskAdapter insert(SQLiteDatabase db, TaskAdapter task, boolean isSyncAdapter)
    {
        TaskAdapter result = mDelegate.insert(db, task, isSyncAdapter);
        // A new task has been inserted by the sync adapter. Update all relations that point to this task.

        if (!isSyncAdapter)
        {
            // the task was created on the device, so it doesn't have a UID
            return result;
        }

        String uid = result.valueOf(TaskAdapter._UID);

        if (uid != null)
        {
            ContentValues v = new ContentValues(1);
            v.put(TaskContract.Property.Relation.RELATED_ID, result.id());

            int updates = db.update(TaskDatabaseHelper.Tables.PROPERTIES, v,
                    TaskContract.Property.Relation.MIMETYPE + "= ? AND " + TaskContract.Property.Relation.RELATED_UID + "=?", new String[] {
                            TaskContract.Property.Relation.CONTENT_ITEM_TYPE, uid });

            if (updates > 0)
            {
                // there were other relations pointing towards this task, update PARENT_IDs if necessary
                ContentValues parentIdValues = new ContentValues(1);
                parentIdValues.put(TaskContract.Tasks.PARENT_ID, result.id());
                // iterate over all tasks which refer to this as their parent and update their PARENT_ID
                try (Cursor c = db.query(
                        TaskDatabaseHelper.Tables.PROPERTIES, new String[] { TaskContract.Property.Relation.TASK_ID },
                        String.format("%s = ? and %s = ? and %s = ?",
                                TaskContract.Property.Relation.MIMETYPE,
                                TaskContract.Property.Relation.RELATED_ID,
                                TaskContract.Property.Relation.RELATED_TYPE),
                        new String[] {
                                TaskContract.Property.Relation.CONTENT_ITEM_TYPE,
                                String.valueOf(result.id()),
                                String.valueOf(TaskContract.Property.Relation.RELTYPE_PARENT) },
                        null, null, null))
                {
                    while (c.moveToNext())
                    {
                        db.update(TaskDatabaseHelper.Tables.TASKS, parentIdValues, TaskContract.Tasks._ID + " = ?", new String[] { c.getString(0) });
                    }
                }
                // TODO, way also may have to do this for all the siblings of these tasks.
            }
        }
        return result;
    }


    @Override
    public TaskAdapter update(SQLiteDatabase db, TaskAdapter task, boolean isSyncAdapter)
    {
        TaskAdapter result = mDelegate.update(db, task, isSyncAdapter);
        // A task has been updated and may have received a UID by the sync adapter. Update all by-id references to this task.
        // in this case we don't need to update any PARENT_ID because it should already be set.

        if (!isSyncAdapter)
        {
            // only sync adapters may assign a UID
            return result;
        }

        String uid = result.valueOf(TaskAdapter._UID);

        if (uid != null)
        {
            ContentValues v = new ContentValues(1);
            v.put(TaskContract.Property.Relation.RELATED_UID, uid);

            db.update(TaskDatabaseHelper.Tables.PROPERTIES, v,
                    TaskContract.Property.Relation.MIMETYPE + "= ? AND " + TaskContract.Property.Relation.RELATED_ID + "=?", new String[] {
                            TaskContract.Property.Relation.CONTENT_ITEM_TYPE, Long.toString(result.id()) });
        }
        return result;
    }


    @Override
    public void delete(SQLiteDatabase db, TaskAdapter task, boolean isSyncAdapter)
    {
        mDelegate.delete(db, task, isSyncAdapter);

        if (!isSyncAdapter)
        {
            // remove once the deletion is final, which is when the sync adapter removes it
            return;
        }

        db.delete(TaskDatabaseHelper.Tables.PROPERTIES, TaskContract.Property.Relation.MIMETYPE + "= ? AND " + TaskContract.Property.Relation.RELATED_ID + "=?",
                new String[] {
                        TaskContract.Property.Relation.CONTENT_ITEM_TYPE,
                        Long.toString(task.id()) });
    }
}
