/*
 * ExpandableGroupDescriptorAdapter.java
 *
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

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorTreeAdapter;
import android.widget.ExpandableListView;


/**
 * An adapter that adapts an {@link ExpandableGroupDescriptor} to an {@link ExpandableListView}.
 * 
 * It supports asynchronous loading of the group children.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 * 
 */
public class ExpandableGroupDescriptorAdapter extends CursorTreeAdapter implements LoaderManager.LoaderCallbacks<Cursor>
{

	private final ExpandableGroupDescriptor mDescriptor;

	private final Context mContext;
	private final LayoutInflater mLayoutInflater;
	private final LoaderManager mLoaderManager;


	public ExpandableGroupDescriptorAdapter(Context context, LoaderManager loaderManager, ExpandableGroupDescriptor descriptor)
	{
		this(null, context, loaderManager, descriptor);
	}


	public ExpandableGroupDescriptorAdapter(Cursor cursor, Context context, LoaderManager loaderManager, ExpandableGroupDescriptor descriptor)
	{
		super(cursor, context);
		mContext = context;
		mDescriptor = descriptor;
		mLoaderManager = loaderManager;
		mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}


	@Override
	public Loader<Cursor> onCreateLoader(int pos, Bundle arguments)
	{
		Cursor cursor = getGroup(pos);
		if (cursor != null)
		{
			return mDescriptor.getChildCursorLoader(mContext, cursor);
		}
		return null;
	}


	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)
	{
		setChildrenCursor(loader.getId(), cursor);
	}


	@Override
	public void onLoaderReset(Loader<Cursor> loader)
	{
		// setChildrenCursor(loader.getId(), null);
		// FIXME: what are we supposed to do here?
	}


	@Override
	protected void bindChildView(View view, Context context, Cursor cursor, boolean isLastChild)
	{
		ViewDescriptor viewDescriptor = mDescriptor.getElementViewDescriptor();

		viewDescriptor.populateView(view, cursor);
	}


	@Override
	protected void bindGroupView(View view, Context context, Cursor cursor, boolean isExpanded)
	{
		ViewDescriptor viewDescriptor = mDescriptor.getGroupViewDescriptor();

		viewDescriptor.populateView(view, cursor);
	}


	@Override
	protected Cursor getChildrenCursor(Cursor groupCursor)
	{
		mLoaderManager.restartLoader(groupCursor.getPosition(), null, this);
		return null;
	}


	@Override
	protected View newChildView(Context context, Cursor cursor, boolean isLastChild, ViewGroup parent)
	{
		ViewDescriptor viewDescriptor = mDescriptor.getElementViewDescriptor();

		View view = mLayoutInflater.inflate(viewDescriptor.getView(), null);

		return view;
	}


	@Override
	protected View newGroupView(Context context, Cursor cursor, boolean isExpanded, ViewGroup parent)
	{
		ViewDescriptor viewDescriptor = mDescriptor.getGroupViewDescriptor();

		View view = mLayoutInflater.inflate(viewDescriptor.getView(), null);

		return view;
	}

}
