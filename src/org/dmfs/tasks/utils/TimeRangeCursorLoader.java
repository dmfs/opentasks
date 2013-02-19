/*
 * Copyright (C) 2012 Marten Gajda <marten@dmfs.org>
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

package org.dmfs.tasks.utils;

import java.util.TimeZone;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.Loader;
import android.text.format.Time;


/**
 * A very simple {@link Loader} that returns the {@link Cursor} from a {@link TimeRangeCursorFactory}. It also delivers a new Cursor each time the time or the
 * time zone changes and each day at midnight.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public class TimeRangeCursorLoader extends Loader<Cursor> implements TimeChangeListener
{
	/**
	 * The current Cursor.
	 */
	private Cursor mCursor;

	/**
	 * A helper to retrieve the timestamp for midnight.
	 */
	private final Time mMidnight = new Time();

	/**
	 * The factory that creates our time range Cursor.
	 */
	private final TimeRangeCursorFactory mCursorFactory;


	public TimeRangeCursorLoader(Context context, String[] projection)
	{
		super(context);

		// set trigger at midnight
		new TimeChangeObserver(context, this).setNextAlarm(getMidnightTimestamp());

		mCursorFactory = new TimeRangeCursorFactory(projection);
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


	@Override
	public void onTimeUpdate(TimeChangeObserver observer)
	{
		// reset next alarm
		observer.setNextAlarm(getMidnightTimestamp());

		// notify LoaderManager
		onContentChanged();
	}


	@Override
	public void onAlarm(TimeChangeObserver observer)
	{
		// set next alarm
		observer.setNextAlarm(getMidnightTimestamp());

		// notify LoaderManager
		onContentChanged();
	}


	private long getMidnightTimestamp()
	{
		mMidnight.clear(TimeZone.getDefault().getID());
		mMidnight.setToNow();
		mMidnight.set(mMidnight.monthDay, mMidnight.month, mMidnight.year);
		++mMidnight.monthDay;
		mMidnight.normalize(true);
		return mMidnight.toMillis(false);
	}

}
