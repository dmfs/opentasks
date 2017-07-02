/*
 * Copyright (C) 2015 Marten Gajda <marten@dmfs.org>
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
 * 
 */

package org.dmfs.provider.tasks.processors.tasks;

import org.dmfs.provider.tasks.TaskContract.Property.Relation;
import org.dmfs.provider.tasks.TaskContract.Tasks;
import org.dmfs.provider.tasks.TaskDatabaseHelper;
import org.dmfs.provider.tasks.model.TaskAdapter;
import org.dmfs.provider.tasks.processors.AbstractEntityProcessor;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;


/**
 * A processor that updates relations for new tasks.
 * <p>
 * In general there is no guarantee that a related task is already in the database when a task is inserted. In such a case we can not set the
 * {@link Relation#RELATED_ID} value. This processor updates the {@link Relation#RELATED_ID} when a task is inserted.
 * </p>
 * <p>
 * It also updates {@link Relation#RELATED_UID} when a tasks is synced the first time and a UID has been set.
 * </p>
 * TODO: update {@link Tasks#PARENT_ID} of related tasks.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public class RelationProcessor extends AbstractEntityProcessor<TaskAdapter>
{

	@Override
	public void afterInsert(SQLiteDatabase db, TaskAdapter task, boolean isSyncAdapter)
	{
		// A new task has been inserted by the sync adapter. Update all relations that point to this task.

		if (!isSyncAdapter)
		{
			// the task was created on the device, so it doesn't have a UID
			return;
		}

		String uid = task.valueOf(TaskAdapter._UID);

		if (uid != null)
		{
			ContentValues v = new ContentValues(1);
			v.put(Relation.RELATED_ID, task.id());

			db.update(TaskDatabaseHelper.Tables.PROPERTIES, v, Relation.MIMETYPE + "= ? AND " + Relation.RELATED_UID + "=?", new String[] {
				Relation.CONTENT_ITEM_TYPE, uid });
		}
	}


	@Override
	public void afterUpdate(SQLiteDatabase db, TaskAdapter task, boolean isSyncAdapter)
	{
		// A task has been updated and may have received a UID by the sync adapter. Update all by-id references to this task.

		if (!isSyncAdapter)
		{
			// only sync adapters may assign a UID
			return;
		}

		String uid = task.valueOf(TaskAdapter._UID);

		if (uid != null)
		{
			ContentValues v = new ContentValues(1);
			v.put(Relation.RELATED_UID, uid);

			db.update(TaskDatabaseHelper.Tables.PROPERTIES, v, Relation.MIMETYPE + "= ? AND " + Relation.RELATED_ID + "=?", new String[] {
				Relation.CONTENT_ITEM_TYPE, Long.toString(task.id()) });
		}
	}


	@Override
	public void afterDelete(SQLiteDatabase db, TaskAdapter task, boolean isSyncAdapter)
	{
		if (!isSyncAdapter)
		{
			// remove once the deletion is final, which is when the sync adapter removes it
			return;
		}

		db.delete(TaskDatabaseHelper.Tables.PROPERTIES, Relation.MIMETYPE + "= ? AND " + Relation.RELATED_ID + "=?", new String[] { Relation.CONTENT_ITEM_TYPE,
			Long.toString(task.id()) });
	}
}
