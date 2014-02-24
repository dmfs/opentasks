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
 * A factory that builds shiny new {@link Cursor}s with progress information.
 * 
 * @author Tobias Reinsch <tobias@dmfs.org>
 */
public final class ProgressCursorFactory extends AbstractCustomCursorFactory
{

	public final static String PROGRESS_ID = "_id";
	public final static String PROGRESS_MIN_STATUS = "min_status";
	public final static String PROGRESS_MAX_STATUS = "max_status";
	public final static String PROGRESS_TYPE = "type";
	public final static String PROGRESS_TITLE_RES_ID = "res_id";

	public final static int PROGRESS_TYPE_0 = 0;
	public final static int PROGRESS_TYPE_40 = 1;
	public final static int PROGRESS_TYPE_60 = 2;
	public final static int PROGRESS_TYPE_80 = 3;
	public final static int PROGRESS_TYPE_100 = 4;

	public static final String[] DEFAULT_PROJECTION = new String[] { PROGRESS_ID, PROGRESS_MIN_STATUS, PROGRESS_MAX_STATUS, PROGRESS_TYPE,
		PROGRESS_TITLE_RES_ID };
	private static final Integer[] ROW_PROGRESS_TYPE_0 = new Integer[] { 1, null, 0, PROGRESS_TYPE_0, R.string.task_group_progress_0 };
	private static final Integer[] ROW_PROGRESS_TYPE_40 = new Integer[] { 2, 1, 40, PROGRESS_TYPE_40, R.string.task_group_progress_40 };
	private static final Integer[] ROW_PROGRESS_TYPE_60 = new Integer[] { 3, 41, 60, PROGRESS_TYPE_60, R.string.task_group_progress_60 };
	private static final Integer[] ROW_PROGRESS_TYPE_80 = new Integer[] { 4, 61, 99, PROGRESS_TYPE_80, R.string.task_group_progress_80 };
	private static final Integer[] ROW_PROGRESS_TYPE_100 = new Integer[] { 5, 100, 100, PROGRESS_TYPE_100, R.string.task_group_progress_100 };


	/**
	 * Initialize the factory with the given projection.
	 * 
	 * @param projection
	 *            An array of column names.
	 */
	public ProgressCursorFactory(String[] projection)
	{
		super(projection);
	}


	@Override
	public Cursor getCursor()
	{
		MatrixCursor result = new MatrixCursor(DEFAULT_PROJECTION);
		result.addRow(ROW_PROGRESS_TYPE_80);
		result.addRow(ROW_PROGRESS_TYPE_60);
		result.addRow(ROW_PROGRESS_TYPE_40);
		result.addRow(ROW_PROGRESS_TYPE_0);
		result.addRow(ROW_PROGRESS_TYPE_100);
		return result;
	}
}
