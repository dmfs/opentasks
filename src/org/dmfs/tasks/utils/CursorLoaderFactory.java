package org.dmfs.tasks.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;


public class CursorLoaderFactory extends AbstractCursorLoaderFactory
{
	private final Uri mUri;
	private final String[] mProjection;
	private final String mSelection;
	private final String[] mSelectionArgs;
	private final String mSortOrder;


	public CursorLoaderFactory(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
	{
		mUri = uri;
		mProjection = projection;
		mSelection = selection;
		mSelectionArgs = selectionArgs;
		mSortOrder = sortOrder;
	}


	@Override
	public Loader<Cursor> getLoader(Context context)
	{
		return new CursorLoader(context, mUri, mProjection, mSelection, mSelectionArgs, mSortOrder);
	}

}
