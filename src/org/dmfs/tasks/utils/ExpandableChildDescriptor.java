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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.dmfs.tasks.groups.AbstractFilter;

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
	 * @param filter
	 *            An additional {@link AbstractFilter} to apply to the selection of the cursor.
	 * @return A new {@link CursorLoader} instance.
	 */
	public CursorLoader getCursorLoader(Context context, Cursor cursor, AbstractFilter filter)
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
			selectionBuilder.append("(");

			// temporary array list for the selection arguments
			List<String> selectionArgList = new ArrayList<String>();
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
					selectionArgList.add(arg);
				}

				pos = newPos;
			}

			if (filter != null)
			{
				selectionArgList.addAll(Arrays.asList(filter.getSelectionArgs()));
			}

			selectionArgs = selectionArgList.toArray(new String[selectionArgList.size()]);

			if (pos > 0)
			{
				// if we had any ?, get the new string
				selectionBuilder.append(mSelection.substring(pos));
			}
			if (filter != null)
			{
				selectionBuilder.append(") and (" + filter.getSelection() + ")");
			}
			else
			{
				selectionBuilder.append(")");
			}
			selection = selectionBuilder.toString();
		}

		Log.v("", selection.toString() + " " + TextUtils.join(",", selectionArgs));
		return new CursorLoader(context, mUri, mProjection, selection.toString(), selectionArgs, mSortOrder);
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
		return getCursorLoader(context, cursor, null);
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
