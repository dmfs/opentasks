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

import org.dmfs.tasks.utils.SearchHistoryHelper;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;


/**
 * A factory that builds shiny new {@link Cursor}s with the search history
 * 
 * @author Tobias Reinsch <tobias@dmfs.org>
 */
public final class SearchHistoryCursorFactory extends AbstractCustomCursorFactory
{

	public final static String SEARCH_ID = "_id";
	public final static String SEARCH_TEXT = "text";

	public static final String[] DEFAULT_PROJECTION = new String[] { SEARCH_ID, SEARCH_TEXT };

	private Context mContext;


	/**
	 * Initialize the factory with the given projection.
	 * 
	 * @param projection
	 *            An array of column names.
	 */
	public SearchHistoryCursorFactory(Context context, String[] projection)
	{
		super(projection);
		mContext = context;
	}


	@Override
	public Cursor getCursor()
	{
		MatrixCursor result = new MatrixCursor(DEFAULT_PROJECTION);
		String[] history = SearchHistoryHelper.loadSearchHistory(mContext);
		for (int i = 0; i < history.length; i++)
		{
			String string = history[i];
			result.addRow(new Object[] { i, string });

		}
		return result;
	}
}
