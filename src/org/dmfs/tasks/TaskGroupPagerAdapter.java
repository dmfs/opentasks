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

package org.dmfs.tasks;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.dmfs.tasks.groupings.AbstractGroupingFactory;
import org.dmfs.tasks.groupings.TabConfig;
import org.dmfs.xmlobjects.pull.XmlObjectPullParserException;
import org.xmlpull.v1.XmlPullParserException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;


/**
 * An adapter to populate the different views of grouped tasks for a ViewPager.
 * 
 * @author Tobias Reinsch <tobias@dmfs.org>
 * @author Marten Gajda <marten@dmfs.org>
 */

public class TaskGroupPagerAdapter extends FragmentStatePagerAdapter
{

	@SuppressWarnings("unused")
	private static final String TAG = "TaskGroupPager";
	private final Map<Integer, AbstractGroupingFactory> mGroupingFactories = new HashMap<Integer, AbstractGroupingFactory>(16);
	private boolean mTwoPaneLayout;
	private final TabConfig mTabConfig;


	/**
	 * Create a new {@link TaskGroupPagerAdapter}.
	 * 
	 * @param fm
	 *            A {@link FragmentManager}
	 * @param groupingFactories
	 *            An array of {@link AbstractGroupingFactory}.
	 * @param context
	 *            A context to access resources
	 * @param tabRes
	 *            The resource id of an XML resource that describes the items of the pager
	 * @throws XmlObjectPullParserException
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	@SuppressLint("NewApi")
	public TaskGroupPagerAdapter(FragmentManager fm, AbstractGroupingFactory[] groupingFactories, Context context, int tabRes) throws XmlPullParserException,
		IOException, XmlObjectPullParserException
	{
		super(fm);

		mTabConfig = TabConfig.load(context, tabRes);

		for (AbstractGroupingFactory factory : groupingFactories)
		{
			mGroupingFactories.put(factory.getId(), factory);
		}
	}


	@Override
	public CharSequence getPageTitle(int position)
	{
		// we don't want to show any title
		return null;
	}


	@Override
	public Fragment getItem(int position)
	{
		int pageId = mTabConfig.getVisibleItem(position).getId();
		AbstractGroupingFactory factory = getGroupingFactoryForId(pageId);

		TaskListFragment fragment = TaskListFragment.newInstance(position, mTwoPaneLayout);
		fragment.setExpandableGroupDescriptor(factory.getExpandableGroupDescriptor());
		fragment.setPageId(pageId);
		return fragment;

	}


	/**
	 * Get the id of a specific page.
	 * 
	 * @param position
	 *            The position of the page.
	 * @return The id of the page.
	 */
	public int getPageId(int position)
	{
		return mTabConfig.getVisibleItem(position).getId();
	}


	/**
	 * Returns the position of the page with the given id.
	 * 
	 * @param id
	 *            The id of the page.
	 * @return The position of the page or <code>-1</code> if the page doesn't exist or is not visible.
	 */
	public int getPagePosition(int id)
	{
		TabConfig groupings = mTabConfig;
		for (int i = 0, count = groupings.visibleSize(); i < count; ++i)
		{
			if (groupings.getVisibleItem(i).getId() == id)
			{
				return i;
			}
		}
		return -1;
	}


	/**
	 * Get an {@link AbstractGroupingFactory} for the page with the given id.
	 * 
	 * @param id
	 *            The is of the page.
	 * @return The {@link AbstractGroupingFactory} that belongs to the id, if any, <code>null</code> otherwise.
	 */
	public AbstractGroupingFactory getGroupingFactoryForId(int id)
	{
		return mGroupingFactories.get(id);
	}


	@Override
	public int getCount()
	{
		return mTabConfig.visibleSize();
	}


	public void setTwoPaneLayout(boolean twoPane)
	{
		mTwoPaneLayout = twoPane;
	}


	public int getTabIcon(int position)
	{
		return mTabConfig.getVisibleItem(position).getIcon();
	}

}
