package org.dmfs.tasks.utils;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.Loader;


public class TimeRangeCursorLoaderFactory extends AbstractCursorLoaderFactory
{

	private final String[] mProjection;


	public TimeRangeCursorLoaderFactory(String[] projection)
	{
		mProjection = projection;
	}


	@Override
	public Loader<Cursor> getLoader(Context context)
	{
		return new TimeRangeCursorLoader(context, mProjection);
	}
}
