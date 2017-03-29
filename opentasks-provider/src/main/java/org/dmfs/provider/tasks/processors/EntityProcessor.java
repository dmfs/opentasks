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

package org.dmfs.provider.tasks.processors;

import org.dmfs.provider.tasks.model.EntityAdapter;

import android.database.sqlite.SQLiteDatabase;


/**
 * EntityProcessors are called before and after any operation on an entity. They can be used to perform additional operations for each entity.
 * 
 * @param <T>
 *            The type of the entity adapter.
 * @author Marten Gajda <marten@dmfs.org>
 */
public interface EntityProcessor<T extends EntityAdapter<?>>
{
	/**
	 * Called before an entity is inserted.
	 * 
	 * @param db
	 *            A writable database.
	 * @param entityAdapter
	 *            The {@link EntityAdapter} that's about to be inserted. You can modify the entity at this stage. {@link EntityAdapter#id()} will return an
	 *            invalid value.
	 * @param isSyncAdapter
	 */
	public void beforeInsert(SQLiteDatabase db, T entityAdapter, boolean isSyncAdapter);


	/**
	 * Called after an entity has been inserted.
	 * 
	 * @param db
	 *            A writable database.
	 * @param entityAdapter
	 *            The {@link EntityAdapter} that's has been inserted. Modifying the entity has no effect.
	 * @param isSyncAdapter
	 */
	public void afterInsert(SQLiteDatabase db, T entityAdapter, boolean isSyncAdapter);


	/**
	 * Called before an entity is updated.
	 * 
	 * @param db
	 *            A writable database.
	 * @param entityAdapter
	 *            The {@link EntityAdapter} that's about to be updated. You can modify the entity at this stage.
	 * @param isSyncAdapter
	 */
	public void beforeUpdate(SQLiteDatabase db, T entityAdapter, boolean isSyncAdapter);


	/**
	 * Called after an entity has been updated.
	 * 
	 * @param db
	 *            A writable database.
	 * @param entityAdapter
	 *            The {@link EntityAdapter} that's has been updated. Modifying the entity has no effect.
	 * @param isSyncAdapter
	 */
	public void afterUpdate(SQLiteDatabase db, T entityAdapter, boolean isSyncAdapter);


	/**
	 * Called before an entity is deleted.
	 * <p>
	 * Note that may be called twice for each entity. Once when the entity is marked deleted by the UI and once when it's actually removed by the sync adapter.
	 * Both cases can be distinguished by the isSyncAdapter parameter. If an entity is removed because it was deleted on the server, this will be called only
	 * once with <code>isSyncAdapter == true</code>.
	 * </p>
	 * <p>
	 * Also note that no processor is called when an entity is removed automatically by a database trigger (e.g. when an entire task list is removed).
	 * </p>
	 * 
	 * @param db
	 *            A writable database.
	 * @param entityAdapter
	 *            The {@link EntityAdapter} that's about to be deleted. Modifying the entity has no effect.
	 * @param isSyncAdapter
	 */
	public void beforeDelete(SQLiteDatabase db, T entityAdapter, boolean isSyncAdapter);


	/**
	 * Called after an entity is deleted.
	 * <p>
	 * Note that may be called twice for each entity. Once when the entity is marked deleted by the UI and once when it's actually removed by the sync adapter.
	 * Both cases can be distinguished by the isSyncAdapter parameter. If an entity is removed because it was deleted on the server, this will be called only
	 * once with <code>isSyncAdapter == true</code>.
	 * </p>
	 * <p>
	 * Also note that no processor is called when an entity is removed automatically by a database trigger (e.g. when an entire task list is removed).
	 * </p>
	 * 
	 * @param db
	 *            A writable database.
	 * @param entityAdapter
	 *            The {@link EntityAdapter} that was deleted. The value of {@link EntityAdapter#id()} contains the id of the deleted entity. Modifying the
	 *            entity has no effect.
	 * @param isSyncAdapter
	 */
	public void afterDelete(SQLiteDatabase db, T entityAdapter, boolean isSyncAdapter);
}
