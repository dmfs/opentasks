/*
 * Copyright (C) 2014 Marten Gajda <marten@dmfs.org>
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

package org.dmfs.tasks.widget;

import android.content.Context;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.widget.ScrollView;


/**
 * Just a {@link ScrollView} that can notify a listener when the user scrolls.
 * <p />
 * TODO: get rid of it once the editor is refactored.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public class ListenableScrollView extends NestedScrollView
{

	public interface OnScrollListener
	{
		/**
		 * Called when the user scrolls the view.
		 * 
		 * @param oldScrollY
		 *            The previous scroll position.
		 * @param newScrollY
		 *            The new scroll position.
		 */
		public void onScroll(int oldScrollY, int newScrollY);
	}


	public ListenableScrollView(Context context)
	{
		super(context);
	}


	public ListenableScrollView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}


	public ListenableScrollView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}

	private OnScrollListener mScrollListener;


	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt)
	{
		super.onScrollChanged(l, t, oldl, oldt);
		if (mScrollListener != null)
		{
			mScrollListener.onScroll(oldt, t);
		}
	}


	public void setOnScrollListener(OnScrollListener listener)
	{
		mScrollListener = listener;
	}
}
