package org.dmfs.tasks.utils;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.Loader;


public abstract class AbstractCursorLoaderFactory
{
	public abstract Loader<Cursor> getLoader(Context context);
}
