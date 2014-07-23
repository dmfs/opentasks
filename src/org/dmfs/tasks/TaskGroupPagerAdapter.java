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

import java.util.HashMap;
import java.util.Map;

import org.dmfs.tasks.groupings.AbstractGroupingFactory;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.PopupMenu;

import com.astuetz.PagerSlidingTabStrip.IconTabProvider;


/**
 * An adapter to populate the different views of grouped tasks for a ViewPager.
 * 
 * @author Tobias Reinsch <tobias@dmfs.org>
 * @author Marten Gajda <marten@dmfs.org>
 */

public class TaskGroupPagerAdapter extends FragmentStatePagerAdapter implements IconTabProvider
{

	private static final String TAG = "TaskGroupPager";
	private final Map<Integer, AbstractGroupingFactory> mGroupingFactories = new HashMap<Integer, AbstractGroupingFactory>(16);
	private final Context mContext;
	private boolean mTwoPaneLayout;
	private final Menu mMenu;


	/**
	 * Create a new {@link TaskGroupPagerAdapter}.
	 * 
	 * @param fm
	 *            A {@link FragmentManager}
	 * @param groupingFactories
	 *            An array of {@link AbstractGroupingFactory}.
	 * @param context
	 *            A context to access resources
	 * @param menuRes
	 *            The resource id of a menu resource that describes the items of the pager
	 */
	@SuppressLint("NewApi")
	public TaskGroupPagerAdapter(FragmentManager fm, AbstractGroupingFactory[] groupingFactories, Context context, int menuRes)
	{
		super(fm);
		mContext = context;

		// TODO: add support for Android<SDK 11
		// this is a hack to get a Menu
		mMenu = new PopupMenu(context, null).getMenu();
		((Activity) context).getMenuInflater().inflate(menuRes, mMenu);

		// remove invisible menu items
		int itemCount = mMenu.size();
		int i = 0;
		while (i < itemCount)
		{
			MenuItem item = mMenu.getItem(i);
			if (!item.isVisible())
			{
				mMenu.removeItem(item.getItemId());
				--itemCount;
			}
			else
			{
				++i;
			}
		}
		for (AbstractGroupingFactory factory : groupingFactories)
		{
			mGroupingFactories.put(factory.getId(), factory);
		}
	}


	@Override
	public CharSequence getPageTitle(int position)
	{
		return mMenu.getItem(position).getTitle();
	}


	@Override
	public Fragment getItem(int position)
	{
		AbstractGroupingFactory factory = getGroupingFactoryForId(mMenu.getItem(position).getItemId());

		TaskListFragment fragment = TaskListFragment.newInstance(position, mTwoPaneLayout);
		fragment.setExpandableGroupDescriptor(factory.getExpandableGroupDescriptor());
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
		return mMenu.getItem(position).getItemId();
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
		Menu menu = mMenu;
		for (int i = 0, count = menu.size(); i < count; ++i)
		{
			if (menu.getItem(i).getItemId() == id)
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
		return mMenu.size();
	}


	public void setTwoPaneLayout(boolean twoPane)
	{
		mTwoPaneLayout = twoPane;
	}


	@Override
	public int getPageIconResId(int position)
	{
		return -1;
	}


	@Override
	public Drawable getPageIconDrawable(int position)
	{
		return mMenu.getItem(position).getIcon();
	}
}
