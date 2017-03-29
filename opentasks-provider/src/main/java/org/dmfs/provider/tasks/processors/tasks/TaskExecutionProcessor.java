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
import org.dmfs.provider.tasks.TaskContract.TaskColumns;
import org.dmfs.provider.tasks.TaskDatabaseHelper.Tables;
import org.dmfs.provider.tasks.model.TaskAdapter;
import org.dmfs.provider.tasks.processors.AbstractEntityProcessor;

import android.database.sqlite.SQLiteDatabase;


/**
 * A processor that perfomrs the actual operations on tasks.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public class TaskExecutionProcessor extends AbstractEntityProcessor<TaskAdapter>
{

	@Override
	public void beforeInsert(SQLiteDatabase db, TaskAdapter task, boolean isSyncAdapter)
	{
		task.commit(db);
	}


	@Override
	public void beforeUpdate(SQLiteDatabase db, TaskAdapter task, boolean isSyncAdapter)
	{
		task.commit(db);
	}


	@Override
	public void beforeDelete(SQLiteDatabase db, TaskAdapter task, boolean isSyncAdapter)
	{
		String accountType = task.valueOf(TaskAdapter.ACCOUNT_TYPE);

		if (isSyncAdapter || TaskContract.LOCAL_ACCOUNT_TYPE.equals(accountType))
		{
			// this is a local task or it' removed by a sync adapter, in either case we delete it right away
			db.delete(Tables.TASKS, TaskColumns._ID + "=" + task.id(), null);
		}
		else
		{
			// just set the deleted flag otherwise
			task.set(TaskAdapter._DELETED, true);
			task.commit(db);
		}
	}
}
