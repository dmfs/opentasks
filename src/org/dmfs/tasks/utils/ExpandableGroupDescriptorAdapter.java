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

import java.util.HashSet;
import java.util.Set;

import org.dmfs.tasks.groupings.cursorloaders.EmptyCursorLoaderFactory;
import org.dmfs.tasks.groupings.filters.AbstractFilter;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
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
 * TODO: manage loader ids to avoid clashes with other instances using the {@link LoaderManager}.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public class ExpandableGroupDescriptorAdapter extends CursorTreeAdapter implements LoaderManager.LoaderCallbacks<Cursor>
{
	private final Context mContext;
	private final LayoutInflater mLayoutInflater;
	private final LoaderManager mLoaderManager;
	private final Set<Integer> mLoadedGroups = new HashSet<Integer>();

	private ExpandableGroupDescriptor mDescriptor;
	private OnChildLoadedListener mOnChildLoadedListener;
	private AbstractFilter mChildCursorFilter;
	private Handler mHandler = new Handler();


	public ExpandableGroupDescriptorAdapter(Context context, LoaderManager loaderManager, ExpandableGroupDescriptor descriptor)
	{
		this(null, context, loaderManager, descriptor);
	}


	public ExpandableGroupDescriptorAdapter(Cursor cursor, Context context, LoaderManager loaderManager, ExpandableGroupDescriptor descriptor)
	{
		super(cursor, context, false);
		mContext = context;
		mDescriptor = descriptor;
		mLoaderManager = loaderManager;
		mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}


	public void setOnChildLoadedListener(OnChildLoadedListener listener)
	{
		mOnChildLoadedListener = listener;
	}


	public void setChildCursorFilter(AbstractFilter filter)
	{
		mChildCursorFilter = filter;
	}


	public boolean childCursorLoaded(int position)
	{
		return mLoadedGroups.contains(position);
	}


	@Override
	public Loader<Cursor> onCreateLoader(int pos, Bundle arguments)
	{
		// the child cursor is no longer valid
		mLoadedGroups.remove(pos);

		Cursor cursor = getGroup(pos);
		if (cursor != null)
		{
			return mDescriptor.getChildCursorLoader(mContext, cursor, mChildCursorFilter);
		}

		// we can't return a valid loader for the child cursor if cursor is null, so return an empty cursor without any rows.
		return new EmptyCursorLoaderFactory(mContext, new String[] { "_id" });
	}


	@Override
	public boolean hasStableIds()
	{
		return true;
	}


	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)
	{
		int pos = loader.getId();

		if (pos < getGroupCount())
		{
			// the child cursor has been loaded
			mLoadedGroups.add(pos);
			setChildrenCursor(pos, cursor);

			if (mOnChildLoadedListener != null)
			{
				mOnChildLoadedListener.onChildLoaded(pos, cursor);
			}
		}
	}


	@Override
	public void onLoaderReset(Loader<Cursor> loader)
	{
		// FIXME: what are we supposed to do here?
	}


	@Override
	protected void bindChildView(View view, Context context, Cursor cursor, boolean isLastChild)
	{
		ViewDescriptor viewDescriptor = mDescriptor.getElementViewDescriptor();

		viewDescriptor.populateView(view, cursor, this, isLastChild ? ViewDescriptor.FLAG_IS_LAST_CHILD : 0);
	}


	@Override
	protected void bindGroupView(View view, Context context, Cursor cursor, boolean isExpanded)
	{
		ViewDescriptor viewDescriptor = mDescriptor.getGroupViewDescriptor();

		viewDescriptor.populateView(view, cursor, this, isExpanded ? ViewDescriptor.FLAG_IS_EXPANDED : 0);
	}


	@Override
	protected Cursor getChildrenCursor(Cursor groupCursor)
	{
		reloadGroup(groupCursor.getPosition());
		return null;
	}


	public void reloadGroup(final int position)
	{
		// the child cursor is no longer valid
		mLoadedGroups.remove(position);
		if (position < getGroupCount())
		{
			mHandler.post(new Runnable()
			{

				@Override
				public void run()
				{
					if (position < getGroupCount()) // ensure this is still true
					{
						mLoaderManager.restartLoader(position, null, ExpandableGroupDescriptorAdapter.this);
					}
				}
			});
		}
	}


	public void reloadLoadedGroups()
	{
		// we operate on a copy of the set to avoid concurrent modification when a group is loaded before we're done here
		for (Integer i : new HashSet<Integer>(mLoadedGroups))
		{
			int getGroupCount = getGroupCount();
			if (i < getGroupCount)
			{
				mLoadedGroups.remove(i);
				mLoaderManager.restartLoader(i, null, ExpandableGroupDescriptorAdapter.this);
			}
		}
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
