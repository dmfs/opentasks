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

package org.dmfs.tasks.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;


/**
 * A subclass of <code>android.wiget.ExpandableListView</code> that properly maintains the activated state.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public class ExpandableListView extends android.widget.ExpandableListView
{

	public ExpandableListView(Context context)
	{
		this(context, null);
	}


	public ExpandableListView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}


	public ExpandableListView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}


	/**
	 * Return if the position points to a header or footer item.
	 * 
	 * Taken from android.widget.ExpandableListView#isHeaderOrFooterPosition(int).
	 * 
	 * @param position
	 *            An absolute (including header and footer) flat list position.
	 * @return true if the position corresponds to a header or a footer item.
	 */
	private boolean isHeaderOrFooterPosition(int position)
	{
		final int footerViewsStart = getAdapter().getCount() - getFooterViewsCount();
		return (position < getHeaderViewsCount() || position >= footerViewsStart);
	}


	@Override
	public boolean performItemClick(View v, int position, long id)
	{
		/*
		 * If this is not a header or footer item check the item at position. The original ExpandableListView doesn't call super.performItemClick(View, int,
		 * long) (Except for header and footer items) which causes the activated state not being properly set. setItemChecked performs the same, that's why we
		 * call it here.
		 */
		if (!isHeaderOrFooterPosition(position))
		{
			// TODO: check if we have to remove any header count from position
			setItemChecked(position, true);
		}
		return super.performItemClick(v, position, id);
	}

}
