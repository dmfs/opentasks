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

package org.dmfs.provider.tasks;

import java.util.List;

import org.dmfs.provider.tasks.model.EntityAdapter;
import org.dmfs.provider.tasks.processors.EntityProcessor;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


/**
 * Provides handlers for INSERT, UPDATE and DELETE operations for {@link EntityAdapter}s.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public enum ProviderOperation
{

	/**
	 * Handles insert operations.
	 */
	INSERT {
		@Override
		<T extends EntityAdapter<?>> void executeBeforeProcessor(SQLiteDatabase db, EntityProcessor<T> processor, T entityAdapter, boolean isSyncAdapter)
		{
			processor.beforeInsert(db, entityAdapter, isSyncAdapter);
		}


		@Override
		<T extends EntityAdapter<?>> void executeAfterProcessor(SQLiteDatabase db, EntityProcessor<T> processor, T entityAdapter, boolean isSyncAdapter)
		{
			processor.afterInsert(db, entityAdapter, isSyncAdapter);
		}
	},

	/**
	 * Handles update operations.
	 */
	UPDATE {
		@Override
		<T extends EntityAdapter<?>> void executeBeforeProcessor(SQLiteDatabase db, EntityProcessor<T> processor, T entityAdapter, boolean isSyncAdapter)
		{
			processor.beforeUpdate(db, entityAdapter, isSyncAdapter);
		}


		@Override
		<T extends EntityAdapter<?>> void executeAfterProcessor(SQLiteDatabase db, EntityProcessor<T> processor, T entityAdapter, boolean isSyncAdapter)
		{
			processor.afterUpdate(db, entityAdapter, isSyncAdapter);
		}
	},

	/**
	 * Handles delete operations.
	 */
	DELETE {
		@Override
		<T extends EntityAdapter<?>> void executeBeforeProcessor(SQLiteDatabase db, EntityProcessor<T> processor, T entityAdapter, boolean isSyncAdapter)
		{
			processor.beforeDelete(db, entityAdapter, isSyncAdapter);
		}


		@Override
		<T extends EntityAdapter<?>> void executeAfterProcessor(SQLiteDatabase db, EntityProcessor<T> processor, T entityAdapter, boolean isSyncAdapter)
		{
			processor.afterDelete(db, entityAdapter, isSyncAdapter);
		}
	};

	private final static String TAG = "OpenTasks.Operation";


	abstract <T extends EntityAdapter<?>> void executeBeforeProcessor(SQLiteDatabase db, EntityProcessor<T> processor, T entityAdapter, boolean isSyncAdapter);


	abstract <T extends EntityAdapter<?>> void executeAfterProcessor(SQLiteDatabase db, EntityProcessor<T> processor, T entityAdapter, boolean isSyncAdapter);


	/**
	 * Executes this operation by running the respective methods of the given {@link EntityProcessor}s.
	 * 
	 * @param db
	 *            An {@link SQLiteDatabase}.
	 * @param processors
	 *            The {@link EntityProcessor} chain.
	 * @param entityAdapter
	 *            The {@link EntityAdapter} to operate on.
	 * @param isSyncAdapter
	 *            <code>true</code> if this operation is triggered by a sync adapter, false otherwise.
	 * @param log
	 *            An {@link ProviderOperationsLog} to log this operation.
	 * @param authority
	 *            The authority of this provider.
	 */
	public <T extends EntityAdapter<?>> void execute(SQLiteDatabase db, List<EntityProcessor<T>> processors, T entityAdapter, boolean isSyncAdapter,
		ProviderOperationsLog log, String authority)
	{
		long start = System.currentTimeMillis();

		for (EntityProcessor<T> processor : processors)
		{
			executeBeforeProcessor(db, processor, entityAdapter, isSyncAdapter);
		}

		for (EntityProcessor<T> processor : processors)
		{
			executeAfterProcessor(db, processor, entityAdapter, isSyncAdapter);
		}

		if (this != UPDATE || entityAdapter.hasUpdates()) // don't log empty operations
		{
			log.log(this, entityAdapter.uri(authority));
		}
	}
}
