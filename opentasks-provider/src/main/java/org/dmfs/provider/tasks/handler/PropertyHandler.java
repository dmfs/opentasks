/*
 * Copyright (C) 2014 Marten Gajda <marten@dmfs.org>
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

package org.dmfs.provider.tasks.handler;

import org.dmfs.provider.tasks.FTSDatabaseHelper;
import org.dmfs.provider.tasks.TaskContract.Properties;
import org.dmfs.provider.tasks.TaskDatabaseHelper.Tables;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


/**
 * Abstract class that is used as template for specific property handlers.
 * 
 * @author Tobias Reinsch <tobias@dmfs.org>
 * 
 */
public abstract class PropertyHandler
{

	/**
	 * Validates the content of the property prior to insert and update transactions.
	 * 
	 * @param db
	 *            The {@link SQLiteDatabase}.
	 * @param taskId
	 *            The id of the task this property belongs to.
	 * @param propertyId
	 *            The id of the property if <code>isNew</code> is <code>false</code>. If <code>isNew</code> is <code>true</code> this value is ignored.
	 * @param isNew
	 *            Indicates that the content is new and not an update.
	 * @param values
	 *            The {@link ContentValues} to validate.
	 * @param isSyncAdapter
	 *            Indicates that the transaction was triggered from a SyncAdapter.
	 * 
	 * @return The valid {@link ContentValues}.
	 * 
	 * @throws IllegalArgumentException
	 *             if the {@link ContentValues} are invalid.
	 */
	public abstract ContentValues validateValues(SQLiteDatabase db, long taskId, long propertyId, boolean isNew, ContentValues values, boolean isSyncAdapter);


	/**
	 * Inserts the property {@link ContentValues} into the database.
	 * 
	 * @param db
	 *            The {@link SQLiteDatabase}.
	 * @param taskId
	 *            The id of the task the new property belongs to.
	 * @param values
	 *            The {@link ContentValues} to insert.
	 * @param isSyncAdapter
	 *            Indicates that the transaction was triggered from a SyncAdapter.
	 * 
	 * @return The row id of the new property as <code>long</code>
	 */
	public long insert(SQLiteDatabase db, long taskId, ContentValues values, boolean isSyncAdapter)
	{
		return db.insert(Tables.PROPERTIES, "", values);
	}


	/**
	 * Updates the property {@link ContentValues} in the database.
	 * 
	 * @param db
	 *            The {@link SQLiteDatabase}.
	 * @param taskId
	 *            The id of the task this property belongs to.
	 * @param propertyId
	 *            The id of the property.
	 * @param values
	 *            The {@link ContentValues} to update.
	 * @param oldValues
	 *            A {@link Cursor} pointing to the old values in the database.
	 * @param isSyncAdapter
	 *            Indicates that the transaction was triggered from a SyncAdapter.
	 * 
	 * @return The number of rows affected.
	 */
	public int update(SQLiteDatabase db, long taskId, long propertyId, ContentValues values, Cursor oldValues, boolean isSyncAdapter)
	{
		return db.update(Tables.PROPERTIES, values, Properties.PROPERTY_ID + "=" + propertyId, null);
	}


	/**
	 * Deletes the property in the database.
	 * 
	 * @param db
	 *            The belonging database.
	 * @param taskId
	 *            The id of the task this property belongs to.
	 * @param propertyId
	 *            The id of the property.
	 * @param oldValues
	 *            A {@link Cursor} pointing to the old values in the database.
	 * @param isSyncAdapter
	 *            Indicates that the transaction was triggered from a SyncAdapter.
	 * @return
	 */
	public int delete(SQLiteDatabase db, long taskId, long propertyId, Cursor oldValues, boolean isSyncAdapter)
	{
		return db.delete(Tables.PROPERTIES, Properties.PROPERTY_ID + "=" + propertyId, null);

	}


	/**
	 * Method hook to insert FTS entries on database migration.
	 * 
	 * @param db
	 *            The {@link SQLiteDatabase}.
	 * @param taskId
	 *            the row id of the task this property belongs to
	 * @param propertyId
	 *            the id of the property
	 * @param text
	 *            the searchable text of the property. If the property has multiple text snippets to search in, concat them separated by a space.
	 */
	protected void updateFTSEntry(SQLiteDatabase db, long taskId, long propertyId, String text)
	{
		FTSDatabaseHelper.updatePropertyFTSEntry(db, taskId, propertyId, text);

	}
}
