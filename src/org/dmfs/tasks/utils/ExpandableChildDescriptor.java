package org.dmfs.tasks.utils;

import java.util.Arrays;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.CursorLoader;
import android.text.TextUtils;
import android.util.Log;


public class ExpandableChildDescriptor
{

	private final Uri mUri;
	private final String[] mProjection;
	private final String mSelection;
	private final int[] mSelectionColumns;
	private final String mSortOrder;

	private ViewDescriptor mViewDescriptor;


	public ExpandableChildDescriptor(Uri uri, String[] projection, String selection, String sortOrder, int... selectionColumns)
	{
		mUri = uri;
		mProjection = projection;
		mSelection = selection;
		mSelectionColumns = selectionColumns;
		mSortOrder = sortOrder;
	}


	/**
	 * Get a new {@link CursorLoader} and update it's selection arguments with the values in {@code cursor} as definded by {@code selectionColumns} in
	 * {@link #ExpandableChildDescriptor(Uri, String[], String, String, int...)}
	 * 
	 * @param context
	 *            A {@link Context}.
	 * @param cursor
	 *            The {@link Cursor} containing the selection.
	 * @return A new {@link CursorLoader} instance.
	 */
	public CursorLoader getCursorLoader(Context context, Cursor cursor)
	{
		String[] selectionArgs = null;
		String selection = mSelection;

		if (mSelectionColumns.length > 0)
		{
			/*
			 * The columns in cursor may be null, but the selection arguments for the CursorLoader must be non-null.
			 * 
			 * To fix that we scan the selection string for question marks and replace them by "null" if the corresponding selection argument is null.
			 */

			int pos = 0;
			int newPos;

			// a StringBuilder to build the new selection string
			StringBuilder selectionBuilder = new StringBuilder(mSelection.length() + 20);
			// temporary array for the selection arguments
			selectionArgs = new String[mSelectionColumns.length];
			// the 
			int argPos = 0;
			
			// for every selection argument
			for (int i = 0; i < mSelectionColumns.length; ++i)
			{
				// find next ?
				newPos = mSelection.indexOf('?', pos == 0 ? pos : pos + 1);
				selectionBuilder.append(mSelection.substring(pos, newPos));
				
				// get the argument
				String arg = cursor.getString(mSelectionColumns[i]);
				if (arg == null)
				{
					// insert null
					selectionBuilder.append("null");
					// skip the ?
					newPos++;
				}
				else
				{
					// add argument to argument list
					selectionArgs[argPos++] = arg;
				}

				pos = newPos;
			}
			
			if (argPos != selectionArgs.length)
			{
				// we had null values, so we have to shrink the array
				selectionArgs = Arrays.copyOf(selectionArgs, argPos);
			}

			if (pos > 0)
			{
				// if we had any ?, get the new string
				selectionBuilder.append(mSelection.substring(pos));
				selection = selectionBuilder.toString();
			}
		}

		Log.v("", selection.toString() + " " + TextUtils.join(",", selectionArgs));
		return new CursorLoader(context, mUri, mProjection, selection.toString(), selectionArgs, mSortOrder);
	}


	public ExpandableChildDescriptor setViewDescriptor(ViewDescriptor descriptor)
	{
		mViewDescriptor = descriptor;
		return this;
	}


	public ViewDescriptor getViewDescriptor()
	{
		return mViewDescriptor;
	}
}
