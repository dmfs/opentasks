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

package org.dmfs.provider.tasks.processors.lists;

import org.dmfs.provider.tasks.TaskContract;
import org.dmfs.provider.tasks.TaskDatabaseHelper.Tables;
import org.dmfs.provider.tasks.model.ListAdapter;
import org.dmfs.provider.tasks.processors.AbstractEntityProcessor;

import android.database.sqlite.SQLiteDatabase;


/**
 * A processor that performs the actual operations on task lists.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public class ListExecutionProcessor extends AbstractEntityProcessor<ListAdapter>
{

	@Override
	public void beforeInsert(SQLiteDatabase db, ListAdapter list, boolean isSyncAdapter)
	{
		list.commit(db);
	}


	@Override
	public void beforeUpdate(SQLiteDatabase db, ListAdapter list, boolean isSyncAdapter)
	{
		list.commit(db);
	}


	@Override
	public void beforeDelete(SQLiteDatabase db, ListAdapter list, boolean isSyncAdapter)
	{
		db.delete(Tables.LISTS, TaskContract.TaskLists._ID + "=" + list.id(), null);
	}
}
