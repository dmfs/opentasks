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

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;


/**
 * This class is used to handle properties with unknown / unsupported mime-types.
 * 
 * @author Tobias Reinsch <tobias@dmfs.org>
 * 
 */
public class DefaultPropertyHandler extends PropertyHandler
{

	/**
	 * Validates the content of the alarm prior to insert and update transactions.
	 * 
	 * @param db
	 *            The {@link SQLiteDatabase}.
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
		return values;
	}

}
