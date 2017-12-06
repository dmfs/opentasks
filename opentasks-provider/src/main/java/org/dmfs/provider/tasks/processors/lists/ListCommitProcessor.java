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

package org.dmfs.provider.tasks.processors.lists;

import android.database.sqlite.SQLiteDatabase;

import org.dmfs.provider.tasks.TaskDatabaseHelper;
import org.dmfs.provider.tasks.model.ListAdapter;
import org.dmfs.provider.tasks.processors.EntityProcessor;
import org.dmfs.tasks.contract.TaskContract;


/**
 * A processor that performs the actual operations on task lists.
 *
 * @author Marten Gajda
 */
public final class ListCommitProcessor implements EntityProcessor<ListAdapter>
{

    @Override
    public ListAdapter insert(SQLiteDatabase db, ListAdapter list, boolean isSyncAdapter)
    {
        list.commit(db);
        return list;
    }


    @Override
    public ListAdapter update(SQLiteDatabase db, ListAdapter list, boolean isSyncAdapter)
    {
        list.commit(db);
        return list;
    }


    @Override
    public void delete(SQLiteDatabase db, ListAdapter list, boolean isSyncAdapter)
    {
        db.delete(TaskDatabaseHelper.Tables.LISTS, TaskContract.TaskLists._ID + "=" + list.id(), null);
    }
}
