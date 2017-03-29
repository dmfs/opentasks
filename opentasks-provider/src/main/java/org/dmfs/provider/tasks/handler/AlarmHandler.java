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

import org.dmfs.provider.tasks.TaskContract.Property;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


/**
 * This class is used to handle alarm property values during database transactions.
 * 
 * @author Tobias Reinsch <tobias@dmfs.org>
 * 
 */
public class AlarmHandler extends PropertyHandler
{

	// private static final String[] ALARM_ID_PROJECTION = { Alarms.ALARM_ID };
	// private static final String ALARM_SELECTION = Alarms.ALARM_ID + " =?";

	/**
	 * Validates the content of the alarm prior to insert and update transactions.
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
	@Override
	public ContentValues validateValues(SQLiteDatabase db, long taskId, long propertyId, boolean isNew, ContentValues values, boolean isSyncAdapter)
	{
		// row id can not be changed or set manually
		if (values.containsKey(Property.Alarm.PROPERTY_ID))
		{
			throw new IllegalArgumentException("_ID can not be set manually");
		}

		if (!values.containsKey(Property.Alarm.MINUTES_BEFORE))
		{
			throw new IllegalArgumentException("alarm property requires a time offset");
		}

		if (!values.containsKey(Property.Alarm.REFERENCE) || values.getAsInteger(Property.Alarm.REFERENCE) < 0)
		{
			throw new IllegalArgumentException("alarm property requires a valid reference date ");
		}

		if (!values.containsKey(Property.Alarm.ALARM_TYPE))
		{
			throw new IllegalArgumentException("alarm property requires an alarm type");
		}

		return values;
	}


	/**
	 * Inserts the alarm into the database.
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
	 * @return The row id of the new alarm as <code>long</code>
	 */
	@Override
	public long insert(SQLiteDatabase db, long taskId, ContentValues values, boolean isSyncAdapter)
	{
		values = validateValues(db, taskId, -1, true, values, isSyncAdapter);
		return super.insert(db, taskId, values, isSyncAdapter);
	}


	/**
	 * Updates the alarm in the database.
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
	@Override
	public int update(SQLiteDatabase db, long taskId, long propertyId, ContentValues values, Cursor oldValues, boolean isSyncAdapter)
	{
		values = validateValues(db, taskId, propertyId, false, values, isSyncAdapter);
		return super.update(db, taskId, propertyId, values, oldValues, isSyncAdapter);
	}
}
