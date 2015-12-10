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

import org.dmfs.tasks.R;

import android.database.Cursor;
import android.database.MatrixCursor;


/**
 * A factory that builds shiny new {@link Cursor}s with priority information.
 * 
 * @author Tobias Reinsch <tobias@dmfs.org>
 */
public final class PriorityCursorFactory extends AbstractCustomCursorFactory
{

	public final static String PRIORITY_ID = "_id";
	public final static String PRIORITY_MIN_STATUS = "min_status";
	public final static String PRIORITY_MAX_STATUS = "max_status";
	public final static String PRIORITY_TYPE = "type";
	public final static String PRIORITY_TITLE_RES_ID = "res_id";

	public final static int PRIORITY_TYPE_NONE = 0;
	public final static int PRIORITY_TYPE_LOW = 3;
	public final static int PRIORITY_TYPE_MEDIUM = 2;
	public final static int PRIORITY_TYPE_HIGH = 1;

	public static final String[] DEFAULT_PROJECTION = new String[] { PRIORITY_ID, PRIORITY_MIN_STATUS, PRIORITY_MAX_STATUS, PRIORITY_TYPE,
		PRIORITY_TITLE_RES_ID };
	private static final Integer[] ROW_PRIORITY_NONE = new Integer[] { 1, null, 0, PRIORITY_TYPE_NONE, R.string.task_group_priority_none };
	private static final Integer[] ROW_PRIORITY_HIGH = new Integer[] { 2, 1, 4, PRIORITY_TYPE_HIGH, R.string.task_group_priority_high };
	private static final Integer[] ROW_PRIORITY_MEDIUM = new Integer[] { 3, 5, 5, PRIORITY_TYPE_MEDIUM, R.string.task_group_priority_medium };
	private static final Integer[] ROW_PRIORITY_LOW = new Integer[] { 4, 6, 9, PRIORITY_TYPE_LOW, R.string.task_group_priority_low };


	/**
	 * Initialize the factory with the given projection.
	 * 
	 * @param projection
	 *            An array of column names.
	 */
	public PriorityCursorFactory(String[] projection)
	{
		super(projection);
	}


	@Override
	public Cursor getCursor()
	{
		MatrixCursor result = new MatrixCursor(DEFAULT_PROJECTION);
		result.addRow(ROW_PRIORITY_HIGH);
		result.addRow(ROW_PRIORITY_MEDIUM);
		result.addRow(ROW_PRIORITY_LOW);
		result.addRow(ROW_PRIORITY_NONE);
		return result;
	}
}
