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

package org.dmfs.tasks;

import java.io.IOException;

import org.dmfs.android.retentionmagic.annotations.Retain;
import org.dmfs.provider.tasks.TaskContract.Tasks;
import org.dmfs.tasks.groupings.AbstractGroupingFactory;
import org.dmfs.tasks.groupings.ByDueDate;
import org.dmfs.tasks.groupings.ByList;
import org.dmfs.tasks.groupings.ByPriority;
import org.dmfs.tasks.groupings.ByProgress;
import org.dmfs.tasks.groupings.BySearch;
import org.dmfs.tasks.groupings.ByStartDate;
import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.notification.AlarmBroadcastReceiver;
import org.dmfs.tasks.utils.ActionBarActivity;
import org.dmfs.tasks.utils.ExpandableGroupDescriptor;
import org.dmfs.tasks.utils.SearchHistoryHelper;
import org.dmfs.xmlobjects.pull.XmlObjectPullParserException;
import org.xmlpull.v1.XmlPullParserException;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.MenuItemCompat.OnActionExpandListener;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.astuetz.PagerSlidingTabStrip;


/**
 * An activity representing a list of Tasks. This activity has different presentations for handset and tablet-size devices. On handsets, the activity presents a
 * list of items, which when touched, lead to a {@link ViewTaskActivity} representing item details. On tablets, the activity presents the list of items and item
 * details side-by-side using two vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a {@link TaskListFragment} and the item details (if present) is a {@link ViewTaskFragment}.
 * <p>
 * This activity also implements the required {@link TaskListFragment.Callbacks} interface to listen for item selections.
 * 
 * <p>
 * TODO: move the code to persist the expanded groups into a the GroupingDescriptor class
 * </p>
 * 
 * @author Tobias Reinsch <tobias@dmfs.org>
 */
public class TaskListActivity extends ActionBarActivity implements TaskListFragment.Callbacks, ViewTaskFragment.Callback
{

	private static final String TAG = "TaskListActivity";

	private final static int REQUEST_CODE_NEW_TASK = 2924;

	/**
	 * The time to wait for a new key before updating the search view.
	 */
	private final static int SEARCH_UPDATE_DELAY = 400; // ms

	private final static String DETAIL_FRAGMENT_TAG = "taskListActivity.ViewTaskFragment";

	/**
	 * Array of {@link ExpandableGroupDescriptor}s.
	 */
	private AbstractGroupingFactory[] mGroupingFactories;

	/**
	 * Whether or not the activity is in two-pane mode, i.e. running on a tablet device.
	 */
	private boolean mTwoPane;
	private ViewTaskFragment mTaskDetailFrag;
	private ViewPager mViewPager;
	private TaskGroupPagerAdapter mPagerAdapter;

	@Retain(permanent = true)
	private int mCurrentPageId;

	private int mPreviousPagePosition = -1;

	private String mAuthority;

	private MenuItem mSearchItem;

	private PagerSlidingTabStrip mTabs;

	private final Handler mHandler = new Handler();

	private SearchHistoryHelper mSearchHistoryHelper;

	private boolean mAutoExpandSearchView = false;

	@Retain
	private Uri mSelectedTaskUri;


	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		Log.d(TAG, "onCreate called again");
		super.onCreate(savedInstanceState);

		// check for single pane activity change
		mTwoPane = getResources().getBoolean(R.bool.has_two_panes);

		if (mSelectedTaskUri != null && !mTwoPane)
		{
			Intent viewTaskIntent = new Intent(Intent.ACTION_VIEW);
			viewTaskIntent.setData(mSelectedTaskUri);
			// editTaskIntent.putExtra(EditTaskActivity.EXTRA_DATA_ACCOUNT_TYPE, accountType);
			startActivity(viewTaskIntent);
		}

		setContentView(R.layout.activity_task_list);

		mAuthority = getString(R.string.org_dmfs_tasks_authority);
		mSearchHistoryHelper = new SearchHistoryHelper(this);

		if (findViewById(R.id.task_detail_container) != null)
		{
			// In two-pane mode, list items should be given the
			// 'activated' state when touched.

			// get list fragment
			// mTaskListFrag = (TaskListFragment) getSupportFragmentManager().findFragmentById(R.id.task_list);
			// mTaskListFrag.setListViewScrollbarPositionLeft(true);

			// mTaskListFrag.setActivateOnItemClick(true);

			/*
			 * Create a detail fragment, but don't load any URL yet, we do that later when the fragment gets attached
			 */
			mTaskDetailFrag = new ViewTaskFragment();
			getSupportFragmentManager().beginTransaction().replace(R.id.task_detail_container, mTaskDetailFrag, DETAIL_FRAGMENT_TAG).commit();
		}
		else
		{
			FragmentManager fragmentManager = getSupportFragmentManager();
			Fragment detailFragment = fragmentManager.findFragmentByTag(DETAIL_FRAGMENT_TAG);
			if (detailFragment != null)
			{
				fragmentManager.beginTransaction().remove(detailFragment).commit();
			}
		}

		mGroupingFactories = new AbstractGroupingFactory[] { new ByList(mAuthority), new ByDueDate(mAuthority), new ByStartDate(mAuthority),
			new ByPriority(mAuthority), new ByProgress(mAuthority), new BySearch(mAuthority, mSearchHistoryHelper) };

		// set up pager adapter
		try
		{
			mPagerAdapter = new TaskGroupPagerAdapter(getSupportFragmentManager(), mGroupingFactories, this, R.xml.listview_tabs);
		}
		catch (XmlPullParserException e)
		{
			// TODO Automatisch generierter Erfassungsblock
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Automatisch generierter Erfassungsblock
			e.printStackTrace();
		}
		catch (XmlObjectPullParserException e)
		{
			// TODO Automatisch generierter Erfassungsblock
			e.printStackTrace();
		}
		mPagerAdapter.setTwoPaneLayout(mTwoPane);

		// Setup ViewPager
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mPagerAdapter);

		int currentPageIndex = mPagerAdapter.getPagePosition(mCurrentPageId);

		if (currentPageIndex >= 0)
		{
			mViewPager.setCurrentItem(currentPageIndex);
			if (VERSION.SDK_INT >= 14 && mCurrentPageId == R.id.task_group_search)
			{
				if (mSearchItem != null)
				{
					// that's actually quite impossible to happen
					MenuItemCompat.expandActionView(mSearchItem);
				}
				else
				{
					mAutoExpandSearchView = true;
				}
			}
		}

		updateTitle(currentPageIndex);

		// Bind the tabs to the ViewPager
		mTabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
		mTabs.setViewPager(mViewPager);

		mTabs.setOnPageChangeListener(new OnPageChangeListener()
		{

			@Override
			public void onPageSelected(int position)
			{
				int newPageId = mPagerAdapter.getPageId(position);

				if (newPageId == R.id.task_group_search)
				{
					int oldPageId = mCurrentPageId;
					mCurrentPageId = newPageId;

					// store the page position we're coming from
					mPreviousPagePosition = mPagerAdapter.getPagePosition(oldPageId);
				}
				else if (mCurrentPageId == R.id.task_group_search)
				{
					// we've been on the search page before, so commit the search and close the search view
					mSearchHistoryHelper.commitSearch();
					mHandler.post(mSearchUpdater);
					mCurrentPageId = newPageId;
					hideSearchActionView();
				}
				mCurrentPageId = newPageId;

				updateTitle(mCurrentPageId);
			}


			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
			{

			}


			@Override
			public void onPageScrollStateChanged(int state)
			{
				if (state == ViewPager.SCROLL_STATE_IDLE && mCurrentPageId == R.id.task_group_search)
				{
					// the search page is selected now, expand the search view
					mHandler.post(new Runnable()
					{
						@Override
						public void run()
						{
							MenuItemCompat.expandActionView(mSearchItem);
						}
					});
				}
			}
		});
	}


	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		mSearchHistoryHelper.close();
	}


	/**
	 * Callback method from {@link TaskListFragment.Callbacks} indicating that the item with the given ID was selected.
	 */
	@Override
	public void onItemSelected(Uri uri, boolean forceReload)
	{

		if (mTwoPane)
		{
			mSelectedTaskUri = null;
			mTaskDetailFrag.loadUri(uri);
		}
		else if (forceReload)
		{
			mSelectedTaskUri = uri;
			// In single-pane mode, simply start the detail activity
			// for the selected item ID.
			Intent detailIntent = new Intent(Intent.ACTION_VIEW);
			detailIntent.setData(uri);
			startActivity(detailIntent);
		}

	}


	private void updateTitle(int pageId)
	{
		switch (pageId)
		{
			case R.id.task_group_by_list:
				setTitle(R.string.task_group_title_list);
				break;
			case R.id.task_group_by_start:
				setTitle(R.string.task_group_title_start);
				break;
			case R.id.task_group_by_due:
				setTitle(R.string.task_group_title_due);
				break;
			case R.id.task_group_by_priority:
				setTitle(R.string.task_group_title_priority);
				break;
			case R.id.task_group_by_progress:
				setTitle(R.string.task_group_title_progress);
				break;

			default:
				setTitle(R.string.task_group_title_default);
				break;
		}
	}


	@Override
	public void onEditTask(Uri taskUri, ContentSet data)
	{
		Intent editTaskIntent = new Intent(Intent.ACTION_EDIT);
		editTaskIntent.setData(taskUri);
		if (data != null)
		{
			editTaskIntent.putExtra(EditTaskActivity.EXTRA_DATA_CONTENT_SET, data);
		}
		startActivity(editTaskIntent);
	}


	@Override
	public void onAddNewTask()
	{
		Intent editTaskIntent = new Intent(Intent.ACTION_INSERT);
		editTaskIntent.setData(Tasks.getContentUri(mAuthority));
		startActivityForResult(editTaskIntent, REQUEST_CODE_NEW_TASK);
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent)
	{
		if (resultCode == Activity.RESULT_OK)
		{
			switch (requestCode)
			{
				case REQUEST_CODE_NEW_TASK:
					Uri newTaskUri = intent.getData();
					if (newTaskUri != null)
					{
						// select the new task
						onItemSelected(newTaskUri, false);
					}
			}
		}
	}


	@Override
	public void onDelete(Uri taskUri)
	{
		// nothing to do here, the loader will take care of reloading the list and the list view will take care of selecting the next element.

		// TODO: there is one exception: when there is no other element to focus!
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.task_list_activity_menu, menu);

		// restore menu state
		MenuItem item = menu.findItem(R.id.menu_alarms);
		if (item != null)
		{
			item.setChecked(AlarmBroadcastReceiver.getAlarmPreference(this));
		}

		// search
		setupSearch(menu);

		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == R.id.menu_visible_list)
		{
			Intent settingsIntent = new Intent(getBaseContext(), SyncSettingsActivity.class);
			startActivity(settingsIntent);
			return true;
		}
		else if (item.getItemId() == R.id.menu_alarms)
		{
			// set and save state
			boolean activatedAlarms = !item.isChecked();
			item.setChecked(activatedAlarms);
			AlarmBroadcastReceiver.setAlarmPreference(this, activatedAlarms);
			return true;
		}
		else
		{
			return super.onOptionsItemSelected(item);
		}
	}


	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private void hideSearchActionView()
	{
		MenuItemCompat.collapseActionView(mSearchItem);
	}


	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void setupSearch(Menu menu)
	{
		// bail early on unsupported devices
		if (Build.VERSION.SDK_INT < 11)
		{
			return;
		}

		mSearchItem = menu.findItem(R.id.search);
		MenuItemCompat.setOnActionExpandListener(mSearchItem, new OnActionExpandListener()
		{

			@Override
			public boolean onMenuItemActionExpand(MenuItem item)
			{
				// always allow expansion of the search action view
				return mCurrentPageId == R.id.task_group_search;
			}


			@Override
			public boolean onMenuItemActionCollapse(MenuItem item)
			{
				// return to previous view
				if (mPreviousPagePosition >= 0 && mCurrentPageId == R.id.task_group_search)
				{
					mViewPager.setCurrentItem(mPreviousPagePosition);
					mCurrentPageId = mPagerAdapter.getPageId(mPreviousPagePosition);
				}
				return mPreviousPagePosition >= 0 || mCurrentPageId != R.id.task_group_search;
			}
		});
		SearchView searchView = (SearchView) MenuItemCompat.getActionView(mSearchItem);

		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		if (null != searchManager)
		{
			searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		}

		searchView.setQueryHint(getString(R.string.menu_search_hint));
		searchView.setIconified(true);
		searchView.setOnQueryTextListener(new OnQueryTextListener()
		{

			@Override
			public boolean onQueryTextSubmit(String query)
			{
				// persist current search
				mSearchHistoryHelper.commitSearch();
				mHandler.post(mSearchUpdater);
				return true;
			}


			@Override
			public boolean onQueryTextChange(String query)
			{
				if (mCurrentPageId != R.id.task_group_search)
				{
					return true;
				}

				mHandler.removeCallbacks(mSearchUpdater);
				if (query.length() > 0)
				{
					mSearchHistoryHelper.updateSearch(query);
					mHandler.postDelayed(mSearchUpdater, SEARCH_UPDATE_DELAY);
				}
				else
				{
					mSearchHistoryHelper.removeCurrentSearch();
					mHandler.post(mSearchUpdater);
				}
				return true;
			}
		});

		if (mAutoExpandSearchView)
		{
			mSearchItem.expandActionView();
		}

	}


	@Override
	public ExpandableGroupDescriptor getGroupDescriptor(int position)
	{
		return mGroupingFactories[position].getExpandableGroupDescriptor();
	}

	/**
	 * Notifies the search fragment of an update.
	 */
	private final Runnable mSearchUpdater = new Runnable()
	{

		@Override
		public void run()
		{
			TaskListFragment fragment = (TaskListFragment) mPagerAdapter.instantiateItem(mViewPager, mViewPager.getCurrentItem());
			fragment.notifyDataSetChanged(true);
			fragment.expandCurrentSearchGroup();
		}
	};

}
