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

import org.dmfs.provider.tasks.model.TaskAdapter;
import org.dmfs.provider.tasks.processors.AbstractEntityProcessor;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


/**
 * A simple debugging processor. It just logs every operation.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public class TestProcessor extends AbstractEntityProcessor<TaskAdapter>
{

	@Override
	public void beforeInsert(SQLiteDatabase db, TaskAdapter task, boolean isSyncAdapter)
	{
		Log.d("TestProcessor", "before insert processor called");
	}


	@Override
	public void afterInsert(SQLiteDatabase db, TaskAdapter task, boolean isSyncAdapter)
	{
		Log.d("TestProcessor", "after insert processor called for " + task.id());
	}


	@Override
	public void beforeUpdate(SQLiteDatabase db, TaskAdapter task, boolean isSyncAdapter)
	{
		Log.d("TestProcessor", "before update processor called for " + task.id());
	}


	@Override
	public void afterUpdate(SQLiteDatabase db, TaskAdapter task, boolean isSyncAdapter)
	{
		Log.d("TestProcessor", "after update processor called for " + task.id());
	}


	@Override
	public void beforeDelete(SQLiteDatabase db, TaskAdapter task, boolean isSyncAdapter)
	{
		Log.d("TestProcessor", "before delete processor called for " + task.id());
	}


	@Override
	public void afterDelete(SQLiteDatabase db, TaskAdapter task, boolean isSyncAdapter)
	{
		Log.i("TestProcessor", "after delete processor called for " + task.id());
	}
}
