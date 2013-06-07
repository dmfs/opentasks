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

package org.dmfs.tasks.model;

import android.database.Cursor;
import android.graphics.drawable.Drawable;


/**
 * An {@link IChoicesAdapter} implementation that loads all values from a cursor.
 * <p>
 * TODO: This is merely a stub. It doesn't do anything useful yet.
 * </p>
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public class CursorChoicesAdapter implements IChoicesAdapter
{

	@SuppressWarnings("unused")
	private static final String TAG = "CursorChoicesAdapter";
	private final Cursor mCursor;
	@SuppressWarnings("unused")
	private String mTitleColumn;
	private String mKeyColumn;


	public CursorChoicesAdapter(Cursor cursor)
	{
		mCursor = cursor;
	}


	@Override
	public String getTitle(Object object)
	{
		// return mCursor.getString(mCursor.getColumnIndex(mTitleColumn));
		return null;
	}


	@Override
	public Drawable getDrawable(Object object)
	{
		return null;

	}


	public String getKeyColumn()
	{
		return mKeyColumn;
	}


	public CursorChoicesAdapter setKeyColumn(String keyColumn)
	{
		mKeyColumn = keyColumn;
		return this;
	}


	public CursorChoicesAdapter setTitleColumn(String column)
	{
		mTitleColumn = column;
		return this;
	}


	public Cursor getChoices()
	{
		return mCursor;
	}


	@Override
	public int getIndex(Object id)
	{
		return 0;
	}


	@Override
	public int getCount()
	{
		return mCursor.getCount();
	}


	@Override
	public Object getItem(int position)
	{
		return null;
	}

}
