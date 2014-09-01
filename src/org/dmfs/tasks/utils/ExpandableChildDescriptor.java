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

package org.dmfs.tasks.utils;

import java.util.ArrayList;
import java.util.List;

import org.dmfs.tasks.groupings.filters.AbstractFilter;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.CursorLoader;


/**
 * Describes how to load the children of an expandable group.
 * <p>
 * The descriptor takes all arguments you'd give to a {@link CursorLoader}, except for the selection arguments. The selection arguments are determined from the
 * values of certain columns of a cursor when you call {@link #getCursorLoader(Context, Cursor)} or {@link #getCursorLoader(Context, Cursor, AbstractFilter)}.
 * </p>
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public class ExpandableChildDescriptor
{
	protected Uri mUri;
	protected String[] mProjection;
	protected String mSelection;
	protected int[] mSelectionColumns;
	protected String mSortOrder;

	private ViewDescriptor mViewDescriptor;


	protected ExpandableChildDescriptor()
	{

	};


	/**
	 * Create a new {@link ExpandableChildDescriptor} using the given values.
	 * 
	 * @param uri
	 * @param projection
	 * @param selection
	 * @param sortOrder
	 * @param selectionColumns
	 */
	public ExpandableChildDescriptor(Uri uri, String[] projection, String selection, String sortOrder, int... selectionColumns)
	{
		mUri = uri;
		mProjection = projection;
		mSelection = selection;
		mSelectionColumns = selectionColumns;
		mSortOrder = sortOrder;
	}


	/**
	 * Get a new {@link CursorLoader} and update it's selection arguments with the values in {@code cursor} as defined by {@code selectionColumns} in
	 * {@link #ExpandableChildDescriptor(Uri, String[], String, String, int...)}. Also applies any selection defined by <code>filter</code>.
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

		if (mSelectionColumns != null && mSelectionColumns.length > 0)
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

			// for every selection argument
			for (int i = 0; i < mSelectionColumns.length; ++i)
			{
				// find next "?"
				newPos = mSelection.indexOf('?', pos == 0 ? pos : pos + 1);
				selectionBuilder.append(mSelection.substring(pos, newPos));

				// get the argument
				String arg = cursor.getString(mSelectionColumns[i]);
				if (arg == null)
				{
					// insert null
					selectionBuilder.append("null");
					// skip the "?"
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
				filter.getSelectionArgs(selectionArgList);
			}

			selectionArgs = selectionArgList.toArray(new String[selectionArgList.size()]);

			selectionBuilder.append(mSelection.substring(pos));

			if (filter != null)
			{
				selectionBuilder.append(") and (");
				filter.getSelection(selectionBuilder);
				selectionBuilder.append(")");
			}
			else
			{
				selectionBuilder.append(")");
			}
			selection = selectionBuilder.toString();
		}
		else
		{
			if (filter != null)
			{
				// temporary array list for the selection arguments
				List<String> selectionArgList = new ArrayList<String>();
				if (filter != null)
				{
					filter.getSelectionArgs(selectionArgList);
				}
				selectionArgs = selectionArgList.toArray(new String[selectionArgList.size()]);

				StringBuilder selectionBuilder = new StringBuilder(120);

				{
					selectionBuilder.append("(");
					filter.getSelection(selectionBuilder);
					selectionBuilder.append(")");
				}
				selection = selectionBuilder.toString();
			}
		}

		String selectionString = null;
		if (selection != null)
		{
			selectionString = selection.toString();
		}
		return new CursorLoader(context, mUri, mProjection, selectionString, selectionArgs, mSortOrder);
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


	/**
	 * Set a view descriptor to use to display the children.
	 * 
	 * @param descriptor
	 *            The {@link ViewDescriptor} to use.
	 * @return This instance.
	 */
	public ExpandableChildDescriptor setViewDescriptor(ViewDescriptor descriptor)
	{
		mViewDescriptor = descriptor;
		return this;
	}


	/**
	 * Get the {@link ViewDescriptor} to use when preparing the views to display the children.
	 * 
	 * @return The {@link ViewDescriptor}.
	 */
	public ViewDescriptor getViewDescriptor()
	{
		return mViewDescriptor;
	}
}
