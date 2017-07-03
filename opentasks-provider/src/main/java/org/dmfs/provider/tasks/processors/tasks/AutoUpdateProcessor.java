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

import org.dmfs.provider.tasks.TaskContract;
import org.dmfs.provider.tasks.TaskContract.Tasks;
import org.dmfs.provider.tasks.TaskDatabaseHelper;
import org.dmfs.provider.tasks.TaskDatabaseHelper.Tables;
import org.dmfs.provider.tasks.model.TaskAdapter;
import org.dmfs.provider.tasks.processors.AbstractEntityProcessor;
import org.dmfs.rfc5545.DateTime;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


/**
 * A processor to adjust some task values automatically.
 * <p />
 * Other then recurrence exceptions no relations are handled by this code. Relation specific changes go to {@link RelationProcessor}.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public class AutoUpdateProcessor extends AbstractEntityProcessor<TaskAdapter>
{

	private static final String[] TASK_ID_PROJECTION = { Tasks._ID };
	private static final String[] TASK_SYNC_ID_PROJECTION = { Tasks._SYNC_ID };

	private static final String SYNC_ID_SELECTION = Tasks._SYNC_ID + "=?";
	private static final String TASK_ID_SELECTION = Tasks._ID + "=?";


	@Override
	public void beforeInsert(SQLiteDatabase db, TaskAdapter task, boolean isSyncAdapter)
	{
		updateFields(db, task, isSyncAdapter);

		if (!isSyncAdapter)
		{
			// set created date for tasks created on the device
			task.set(TaskAdapter.CREATED, new DateTime(System.currentTimeMillis()));
		}
	}


	@Override
	public void afterInsert(SQLiteDatabase db, TaskAdapter task, boolean isSyncAdapter)
	{
		if (isSyncAdapter && task.isRecurring())
		{
			// task is recurring, update ORIGINAL_INSTANCE_ID of all exceptions that may already exists
			ContentValues values = new ContentValues(1);
			TaskAdapter.ORIGINAL_INSTANCE_ID.setIn(values, task.id());
			db.update(TaskDatabaseHelper.Tables.TASKS, values, TaskContract.Tasks.ORIGINAL_INSTANCE_SYNC_ID + "=? and "
				+ TaskContract.Tasks.ORIGINAL_INSTANCE_ID + " is null", new String[] { task.valueOf(TaskAdapter.SYNC_ID) });
		}
	}


	@Override
	public void beforeUpdate(SQLiteDatabase db, TaskAdapter task, boolean isSyncAdapter)
	{
		updateFields(db, task, isSyncAdapter);
	}


	@Override
	public void afterUpdate(SQLiteDatabase db, TaskAdapter task, boolean isSyncAdapter)
	{
		if (isSyncAdapter && task.isRecurring() && task.isUpdated(TaskAdapter.SYNC_ID))
		{
			// task is recurring, update ORIGINAL_INSTANCE_SYNC_ID of all exceptions that may already exists
			ContentValues values = new ContentValues(1);
			TaskAdapter.ORIGINAL_INSTANCE_SYNC_ID.setIn(values, task.valueOf(TaskAdapter.SYNC_ID));
			db.update(TaskDatabaseHelper.Tables.TASKS, values, TaskContract.Tasks.ORIGINAL_INSTANCE_ID + "=" + task.id(), null);
		}
	}


	private void updateFields(SQLiteDatabase db, TaskAdapter task, boolean isSyncAdapter)
	{
		if (!isSyncAdapter)
		{
			task.set(TaskAdapter._DIRTY, true);
			task.set(TaskAdapter.LAST_MODIFIED, new DateTime(System.currentTimeMillis()));

			// set proper STATUS if task has been completed
			if (task.valueOf(TaskAdapter.COMPLETED) != null && !task.isUpdated(TaskAdapter.STATUS))
			{
				task.set(TaskAdapter.STATUS, Tasks.STATUS_COMPLETED);
			}
		}

		if (task.isUpdated(TaskAdapter.PRIORITY))
		{
			Integer priority = task.valueOf(TaskAdapter.PRIORITY);
			if (priority != null && priority == 0)
			{
				// replace priority 0 by null, it's the default and we need that for proper sorting
				task.set(TaskAdapter.PRIORITY, null);
			}
		}

		// Find corresponding ORIGINAL_INSTANCE_ID
		if (task.isUpdated(TaskAdapter.ORIGINAL_INSTANCE_SYNC_ID))
		{
			String[] syncId = { task.valueOf(TaskAdapter.ORIGINAL_INSTANCE_SYNC_ID) };
			Cursor cursor = db.query(Tables.TASKS, TASK_ID_PROJECTION, SYNC_ID_SELECTION, syncId, null, null, null);
			try
			{
				if (cursor.moveToNext())
				{
					Long originalId = cursor.getLong(0);
					task.set(TaskAdapter.ORIGINAL_INSTANCE_ID, originalId);
				}
			}
			finally
			{
				if (cursor != null)
				{
					cursor.close();
				}
			}
		}
		else if (task.isUpdated(TaskAdapter.ORIGINAL_INSTANCE_ID)) // Find corresponding ORIGINAL_INSTANCE_SYNC_ID
		{
			String[] id = { Long.toString(task.valueOf(TaskAdapter.ORIGINAL_INSTANCE_ID)) };
			Cursor cursor = db.query(Tables.TASKS, TASK_SYNC_ID_PROJECTION, TASK_ID_SELECTION, id, null, null, null);
			try
			{
				if (cursor.moveToNext())
				{
					String originalSyncId = cursor.getString(0);
					task.set(TaskAdapter.ORIGINAL_INSTANCE_SYNC_ID, originalSyncId);
				}
			}
			finally
			{
				if (cursor != null)
				{
					cursor.close();
				}
			}
		}

		// check that PERCENT_COMPLETE is an Integer between 0 and 100 if supplied also update status and completed accordingly
		if (task.isUpdated(TaskAdapter.PERCENT_COMPLETE))
		{
			Integer percent = task.valueOf(TaskAdapter.PERCENT_COMPLETE);

			if (!isSyncAdapter && percent != null && percent == 100)
			{
				if (!task.isUpdated(TaskAdapter.STATUS))
				{
					task.set(TaskAdapter.STATUS, Tasks.STATUS_COMPLETED);
				}

				if (!task.isUpdated(TaskAdapter.COMPLETED))
				{
					task.set(TaskAdapter.COMPLETED, new DateTime(System.currentTimeMillis()));
				}
			}
			else if (!isSyncAdapter && percent != null)
			{
				if (!task.isUpdated(TaskAdapter.COMPLETED))
				{
					task.set(TaskAdapter.COMPLETED, null);
				}
			}
		}

		// validate STATUS and set IS_NEW and IS_CLOSED accordingly
		if (task.isUpdated(TaskAdapter.STATUS) || task.id() < 0 /* this is true when the task is new */)
		{
			Integer status = task.valueOf(TaskAdapter.STATUS);
			if (status == null)
			{
				status = Tasks.STATUS_DEFAULT;
				task.set(TaskAdapter.STATUS, status);
			}

			task.set(TaskAdapter.IS_NEW, status == null || status == Tasks.STATUS_NEEDS_ACTION);
			task.set(TaskAdapter.IS_CLOSED, status != null && (status == Tasks.STATUS_COMPLETED || status == Tasks.STATUS_CANCELLED));

			/*
			 * Update PERCENT_COMPLETE and COMPLETED (if not given). Sync adapters should know what they're doing, so don't update anything if caller is a sync
			 * adapter.
			 */
			if (status == Tasks.STATUS_COMPLETED && !isSyncAdapter)
			{
				task.set(TaskAdapter.PERCENT_COMPLETE, 100);
				if (!task.isUpdated(TaskAdapter.COMPLETED))
				{
					task.set(TaskAdapter.COMPLETED, new DateTime(System.currentTimeMillis()));
				}
			}
			else if (!isSyncAdapter)
			{
				task.set(TaskAdapter.COMPLETED, null);
			}
		}
	}
}
