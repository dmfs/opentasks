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

import org.dmfs.provider.tasks.FTSDatabaseHelper;
import org.dmfs.provider.tasks.model.TaskAdapter;
import org.dmfs.provider.tasks.processors.AbstractEntityProcessor;

import android.database.sqlite.SQLiteDatabase;


/**
 * A {@link TaskProcessor} to update the fast text search table when inserting or updating a task.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public class FtsProcessor extends AbstractEntityProcessor<TaskAdapter>
{

	@Override
	public void afterInsert(SQLiteDatabase db, TaskAdapter task, boolean isSyncAdapter)
	{
		FTSDatabaseHelper.updateTaskFTSEntries(db, task);
	}


	@Override
	public void afterUpdate(SQLiteDatabase db, TaskAdapter task, boolean isSyncAdapter)
	{
		FTSDatabaseHelper.updateTaskFTSEntries(db, task);
	}
}
