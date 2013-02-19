package org.dmfs.tasks.utils;

import java.util.TimeZone;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.Loader;
import android.text.format.Time;


/**
 * A very simple Loader that returns just a TimeRangeCursorBuilder
 * 
 * @author marten
 * 
 */
public class TimeRangeCursorLoader extends Loader<Cursor> implements TimeUpdateListener
{
	private Cursor mCursor;
	private final String[] mProjection;
	private final Time mToday = new Time(TimeZone.getDefault().getID());


	public TimeRangeCursorLoader(Context context, String[] projection)
	{
		super(context);
		mProjection = projection;

		mToday.setToNow();
		mToday.set(mToday.monthDay, mToday.month, mToday.year);
		++mToday.monthDay;
		mToday.normalize(true);

		// set trigger at midnight
		new TimeChangeManager(context, this).setNextAlarm(mToday.toMillis(false));
	}


	@Override
	public void deliverResult(Cursor cursor)
	{
		if (isReset())
		{
			// An async query came in while the loader is stopped
			if (cursor != null)
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
		if (mCursor == null)
		{
			mCursor = new TimeRangeCursorBuilder(mProjection).getCursor();
		}
		deliverResult(mCursor);
	}


	@Override
	protected void onForceLoad()
	{
		Cursor oldCursor = mCursor;

		mCursor = new TimeRangeCursorBuilder(mProjection).getCursor();
		deliverResult(mCursor);

		if (oldCursor != null && !oldCursor.isClosed())
		{
			oldCursor.close();
		}
	}


	@Override
	protected void onReset()
	{
		super.onReset();

		onStopLoading();

		if (mCursor != null && !mCursor.isClosed())
		{
			mCursor.close();
		}

		mCursor = null;
	}


	@Override
	public void onTimeUpdate(TimeChangeManager manager)
	{
		// set trigger at midnight
		mToday.clear(TimeZone.getDefault().getID());
		mToday.setToNow();
		mToday.set(mToday.monthDay, mToday.month, mToday.year);
		++mToday.monthDay;
		mToday.normalize(true);
		manager.setNextAlarm(mToday.toMillis(false));

		onContentChanged();
	}
}
