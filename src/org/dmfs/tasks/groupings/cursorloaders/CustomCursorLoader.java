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

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.Loader;


/**
 * A very simple {@link Loader} that returns the {@link Cursor} from a {@link AbstractCustomCursorFactory}.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public class CustomCursorLoader extends Loader<Cursor>
{
	/**
	 * The current Cursor.
	 */
	private Cursor mCursor;

	/**
	 * The factory that creates our Cursor.
	 */
	private final AbstractCustomCursorFactory mCursorFactory;


	public CustomCursorLoader(Context context, AbstractCustomCursorFactory factory)
	{
		super(context);

		mCursorFactory = factory;
	}


	@Override
	public void deliverResult(Cursor cursor)
	{
		if (isReset())
		{
			// An async query came in while the loader is stopped
			if (cursor != null && !cursor.isClosed())
			{
				cursor.close();
			}
			return;
		}
		Cursor oldCursor = mCursor;
		mCursor = cursor;

		if (isStarted())
		{
			super.deliverResult(cursor);
		}

		if (oldCursor != null && oldCursor != cursor && !oldCursor.isClosed())
		{
			oldCursor.close();
		}
	}


	@Override
	protected void onStartLoading()
	{
		if (mCursor == null || takeContentChanged())
		{
			// deliver a new cursor, deliverResult will take care of the old one if any
			deliverResult(mCursorFactory.getCursor());
		}
		else
		{
			// just deliver the same cursor
			deliverResult(mCursor);
		}
	}


	@Override
	protected void onForceLoad()
	{
		// just create a new cursor, deliverResult will take care of storing the new cursor and closing the old one
		deliverResult(mCursorFactory.getCursor());
	}


	@Override
	protected void onReset()
	{
		super.onReset();

		onStopLoading();

		// ensure the cursor is closed before we release it
		if (mCursor != null && !mCursor.isClosed())
		{
			mCursor.close();
		}

		mCursor = null;
	}
}
