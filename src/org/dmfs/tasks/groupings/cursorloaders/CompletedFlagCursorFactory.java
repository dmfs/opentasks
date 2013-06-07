/*
 * Copyright (C) 2013 Marten Gajda <marten@dmfs.org>
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

package org.dmfs.tasks.groupings.cursorloaders;

import org.dmfs.provider.tasks.TaskContract.Tasks;

import android.database.Cursor;
import android.database.MatrixCursor;


/**
 * A factory that builds shiny new {@link Cursor}s with completed flags.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class CompletedFlagCursorFactory extends AbstractCustomCursorFactory
{

	public final static String STATUS_ID = "_id";
	public final static String STATUS_MIN_STATUS = "min_status";
	public final static String STATUS_MAX_STATUS = "max_status";
	public final static String STATUS_TYPE = "type";

	public final static int STATUS_TYPE_INCOMPLETE = 0;
	public final static int STATUS_TYPE_COMPLETED = 1;

	public static final String[] DEFAULT_PROJECTION = new String[] { STATUS_ID, STATUS_MIN_STATUS, STATUS_MAX_STATUS, STATUS_TYPE };
	private static final Integer[] ROW_COMPLETED = new Integer[] { 0, Tasks.STATUS_COMPLETED, Tasks.STATUS_CANCELLED, STATUS_TYPE_COMPLETED };
	private static final Integer[] ROW_INCOMPLETE = new Integer[] { 0, Tasks.STATUS_NEEDS_ACTION, Tasks.STATUS_IN_PROCESS, STATUS_TYPE_INCOMPLETE };


	/**
	 * Initialize the factory with the given projection.
	 * 
	 * @param projection
	 *            An array of column names.
	 */
	public CompletedFlagCursorFactory(String[] projection)
	{
		super(projection);
	}


	@Override
	public Cursor getCursor()
	{
		MatrixCursor result = new MatrixCursor(DEFAULT_PROJECTION);
		result.addRow(ROW_COMPLETED);
		result.addRow(ROW_INCOMPLETE);
		return result;
	}
}
