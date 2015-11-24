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
import org.dmfs.provider.tasks.TaskContract;
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
import org.dmfs.tasks.utils.AppCompatActivity;
import org.dmfs.tasks.utils.ExpandableGroupDescriptor;
import org.dmfs.tasks.utils.SearchHistoryHelper;
import org.dmfs.xmlobjects.pull.XmlObjectPullParserException;
import org.xmlpull.v1.XmlPullParserException;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.MenuItemCompat.OnActionExpandListener;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;


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
public class TaskListActivity extends AppCompatActivity implements TaskListFragment.Callbacks, ViewTaskFragment.Callback
{

	/** Tells the activity to display the details of the task with the URI from the intent data. **/
	public static final String EXTRA_DISPLAY_TASK = "org.dmfs.tasks.DISPLAY_TASK";

	/** Tells the activity to select the task in the list with the URI from the intent data. **/
	public static final String EXTRA_FORCE_LIST_SELECTION = "org.dmfs.tasks.FORCE_LIST_SELECTION";

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

	/** The current pager position **/
	private int mCurrentPagePosition = 0;

	private int mPreviousPagePosition = -1;

	private String mAuthority;

	private MenuItem mSearchItem;

	private TabLayout mTabs;

	private final Handler mHandler = new Handler();

	private SearchHistoryHelper mSearchHistoryHelper;

	private boolean mAutoExpandSearchView = false;

	/** Indicates that the activity switched to detail view due to rotation. **/
	@Retain
	private boolean mSwitchedToDetail = false;

	/** The Uri of the task to display/highlight in the list view. **/
	@Retain
	private Uri mSelectedTaskUri;

	/** The Uri of the task to display/highlight in the list view coming from the widget. **/
	private Uri mSelectedTaskUriOnLaunch;

	/** Indicates to display the two pane layout with details **/
	@Retain
	private boolean mShouldShowDetails = false;

	/** Indicates to show ViewTaskActivity when rotating to single pane. **/
	@Retain
	private boolean mShouldSwitchToDetail = false;

	/** Indicates the TaskListFragments to select/highlight the mSelectedTaskUri item **/
	private boolean mShouldSelectTaskListItem = false;

	/** Indicates a transient state after rotation to redirect to the TaskViewActivtiy **/
	private boolean mTransientState = false;

	private CollapsingToolbarLayout mToolbarLayout;

	private AppBarLayout mAppBarLayout;

	private FloatingActionButton mFloatingActionButton;


	@SuppressLint("NewApi")
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		Log.d(TAG, "onCreate called again");
		super.onCreate(savedInstanceState);

		// check for single pane activity change
		mTwoPane = getResources().getBoolean(R.bool.has_two_panes);

		resolveIntentAction(getIntent());

		if (mSelectedTaskUri != null)
		{
			if (mShouldShowDetails && mShouldSwitchToDetail)
			{
				Intent viewTaskIntent = new Intent(Intent.ACTION_VIEW);
				viewTaskIntent.setData(mSelectedTaskUri);
				startActivity(viewTaskIntent);
				mSwitchedToDetail = true;
				mShouldSwitchToDetail = false;
				mTransientState = true;
			}
		}
		else
		{
			mShouldShowDetails = false;
		}

		setContentView(R.layout.activity_task_list);
		mAppBarLayout = (AppBarLayout) findViewById(R.id.appbar);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		mAuthority = TaskContract.taskAuthority(this);
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
			mTaskDetailFrag = ViewTaskFragment.newInstance(mSelectedTaskUri);
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

		// Setup ViewPager
		mPagerAdapter.setTwoPaneLayout(mTwoPane);
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mPagerAdapter);

		int currentPageIndex = mPagerAdapter.getPagePosition(mCurrentPageId);

		if (currentPageIndex >= 0)
		{
			mCurrentPagePosition = currentPageIndex;
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
		mTabs = (TabLayout) findViewById(R.id.tabs);
		mTabs.setupWithViewPager(mViewPager);

		// set up the tab icons
		for (int i = 0, count = mPagerAdapter.getCount(); i < count; ++i)
		{
			mTabs.getTabAt(i).setIcon(mPagerAdapter.getTabIcon(i));
		}

		mViewPager.addOnPageChangeListener(new OnPageChangeListener()
		{

			@Override
			public void onPageSelected(int position)
			{
				mSelectedTaskUri = null;
				mCurrentPagePosition = position;

				int newPageId = mPagerAdapter.getPageId(mCurrentPagePosition);

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
					mHandler.postDelayed(new Runnable()
					{
						@Override
						public void run()
						{
							MenuItemCompat.expandActionView(mSearchItem);
						}
					}, 50);
				}
			}

		});

		mFloatingActionButton = (FloatingActionButton) findViewById(R.id.floating_action_button);
		if (mFloatingActionButton != null)
		{
			mFloatingActionButton.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					onAddNewTask();
				}
			});
		}
	}


	@Override
	protected void onResume()
	{
		updateTitle(mCurrentPageId);
		super.onResume();
	}


	@Override
	protected void onNewIntent(Intent intent)
	{
		resolveIntentAction(intent);
		super.onNewIntent(intent);
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
	public void onItemSelected(Uri uri, boolean forceReload, int pagePosition)
	{
		// only accept selections from the current visible task fragment or the activity itself
		if (pagePosition == -1 || pagePosition == mCurrentPagePosition)
		{
			if (mTwoPane)
			{
				mShouldShowDetails = true;
				if (forceReload)
				{
					mSelectedTaskUri = null;
					mShouldSwitchToDetail = false;
					mTaskDetailFrag.loadUri(uri);
				}
				else
				{
					mTaskDetailFrag.loadUri(uri);
				}
			}
			else if (forceReload)
			{
				mSelectedTaskUri = uri;

				// In single-pane mode, simply start the detail activity
				// for the selected item ID.
				Intent detailIntent = new Intent(Intent.ACTION_VIEW);
				detailIntent.setData(uri);
				startActivity(detailIntent);
				mSwitchedToDetail = true;
				mShouldSwitchToDetail = false;
			}
		}
	}


	private void updateTitle(int pageId)
	{
		switch (pageId)
		{
			case R.id.task_group_by_list:
				getSupportActionBar().setTitle(R.string.task_group_title_list);
				break;
			case R.id.task_group_by_start:
				getSupportActionBar().setTitle(R.string.task_group_title_start);
				break;
			case R.id.task_group_by_due:
				getSupportActionBar().setTitle(R.string.task_group_title_due);
				break;
			case R.id.task_group_by_priority:
				getSupportActionBar().setTitle(R.string.task_group_title_priority);
				break;
			case R.id.task_group_by_progress:
				getSupportActionBar().setTitle(R.string.task_group_title_progress);
				break;

			default:
				getSupportActionBar().setTitle(R.string.task_group_title_default);
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


	private void resolveIntentAction(Intent intent)
	{
		// check which task should be selected
		if (intent.hasExtra(EXTRA_DISPLAY_TASK))

		{
			mShouldSwitchToDetail = true;
			mSelectedTaskUri = intent.getData();
		}

		if (intent != null && intent.hasExtra(EXTRA_DISPLAY_TASK) && intent.getBooleanExtra(EXTRA_FORCE_LIST_SELECTION, true) && mTwoPane)
		{
			mShouldSwitchToDetail = true;
			Uri newSelection = intent.getData();
			mSelectedTaskUriOnLaunch = newSelection;
			mShouldSelectTaskListItem = true;
			if (mPagerAdapter != null)
			{
				mPagerAdapter.notifyDataSetChanged();
			}
		}
		else
		{
			mSelectedTaskUriOnLaunch = null;
			mShouldSelectTaskListItem = false;
		}
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent)
	{
		if (resultCode == RESULT_OK)
		{
			if (intent != null)
			{
				Uri taskUri = intent.getData();
				if (taskUri != null)
				{
					// select the new task
					onItemSelected(taskUri, false, -1);
				}
			}
		}
	}


	@Override
	public void onDelete(Uri taskUri)
	{
		// nothing to do here, the loader will take care of reloading the list and the list view will take care of selecting the next element.

		// empty the detail fragment
		if (mTwoPane)
		{
			mTaskDetailFrag.loadUri(null);
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.task_list_activity_menu, menu);

		MenuItem addItem = menu.findItem(R.id.menu_add_task);
		if (addItem != null && mFloatingActionButton != null)
		{
			// hide menu option to add a task if we have a floating action button
			addItem.setVisible(false);
		}

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
		int id = item.getItemId();
		if (id == R.id.menu_add_task)
		{
			onAddNewTask();
			return true;
		}
		else if (item.getItemId() == R.id.menu_visible_list)
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
	public ExpandableGroupDescriptor getGroupDescriptor(int pageId)
	{
		for (AbstractGroupingFactory factory : mGroupingFactories)
		{
			if (factory.getId() == pageId)
			{
				return factory.getExpandableGroupDescriptor();
			}
		}
		return null;
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


	private int darkenColor(int color)
	{
		float[] hsv = new float[3];
		Color.colorToHSV(color, hsv);
		hsv[2] = hsv[2] * 0.75f;
		color = Color.HSVToColor(hsv);
		return color;
	}


	@SuppressLint("NewApi")
	@Override
	public void updateColor(int color)
	{
		if (mTwoPane)
		{
			getSupportActionBar().setBackgroundDrawable(new ColorDrawable(color));
			mTabs.setBackgroundColor(color);

			if (mAppBarLayout != null)
			{
				mAppBarLayout.setBackgroundColor(color);
			}

			if (VERSION.SDK_INT >= 21)
			{
				Window window = getWindow();
				window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
				window.setStatusBarColor(darkenColor(color));
			}
		}
	}


	public Uri getSelectedTaskUri()
	{
		if (mShouldSelectTaskListItem)
		{
			return mSelectedTaskUriOnLaunch;
		}
		return null;
	}


	public boolean isInTransientState()
	{
		return mTransientState;
	}
}
