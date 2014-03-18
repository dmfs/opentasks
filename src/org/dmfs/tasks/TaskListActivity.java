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

import org.dmfs.android.retentionmagic.FragmentActivity;
import org.dmfs.android.retentionmagic.annotations.Retain;
import org.dmfs.provider.tasks.TaskContract.Tasks;
import org.dmfs.tasks.groupings.ByDueDate;
import org.dmfs.tasks.groupings.ByList;
import org.dmfs.tasks.groupings.ByPriority;
import org.dmfs.tasks.groupings.ByProgress;
import org.dmfs.tasks.groupings.ByStartDate;
import org.dmfs.tasks.utils.ExpandableGroupDescriptor;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
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
public class TaskListActivity extends FragmentActivity implements TaskListFragment.Callbacks, ViewTaskFragment.Callback
{

	private static final String TAG = "TaskListActivity";

	private final static int REQUEST_CODE_NEW_TASK = 2924;

	/**
	 * Array of {@link ExpandableGroupDescriptor}s.
	 */
	private final static ExpandableGroupDescriptor[] GROUP_DESCRIPTORS = new ExpandableGroupDescriptor[] { ByList.GROUP_DESCRIPTOR, ByDueDate.GROUP_DESCRIPTOR,
		ByStartDate.GROUP_DESCRIPTOR, ByPriority.GROUP_DESCRIPTOR, ByProgress.GROUP_DESCRIPTOR };

	/**
	 * Whether or not the activity is in two-pane mode, i.e. running on a tablet device.
	 */
	private boolean mTwoPane;
	private ViewTaskFragment mTaskDetailFrag;
	private ViewPager mViewPager;
	private TaskGroupPagerAdapter mPagerAdapter;

	@Retain(permanent = true)
	private int mCurrentPage;


	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		Log.d(TAG, "onCreate called again");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_task_list);

		if (findViewById(R.id.task_detail_container) != null)
		{
			mTwoPane = true;

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
			getSupportFragmentManager().beginTransaction().replace(R.id.task_detail_container, mTaskDetailFrag).commit();
		}

		// set up pager adapter
		mPagerAdapter = new TaskGroupPagerAdapter(getSupportFragmentManager(), GROUP_DESCRIPTORS, getApplicationContext());
		mPagerAdapter.setTwoPaneLayout(mTwoPane);

		// Setup ViewPager
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.setCurrentItem(mCurrentPage);

		// Bind the tabs to the ViewPager
		PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
		tabs.setViewPager(mViewPager);

	}


	/**
	 * Callback method from {@link TaskListFragment.Callbacks} indicating that the item with the given ID was selected.
	 */
	@Override
	public void onItemSelected(Uri uri, boolean forceReload)
	{
		if (mTwoPane)
		{
			mTaskDetailFrag.loadUri(uri);
		}
		else if (forceReload)
		{
			// In single-pane mode, simply start the detail activity
			// for the selected item ID.
			Intent detailIntent = new Intent(Intent.ACTION_VIEW);
			detailIntent.setData(uri);
			startActivity(detailIntent);
		}
	}


	@Override
	public void onEditTask(Uri taskUri)
	{
		Intent editTaskIntent = new Intent(Intent.ACTION_EDIT);
		editTaskIntent.setData(taskUri);
		startActivity(editTaskIntent);
	}


	@Override
	public void onAddNewTask()
	{
		Intent editTaskIntent = new Intent(Intent.ACTION_INSERT);
		editTaskIntent.setData(Tasks.CONTENT_URI);
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
	public void onPause()
	{
		// save pager state, in Android 2.x the state gets persited in onPause, so save the state before
		if (mViewPager != null)
		{
			mCurrentPage = mViewPager.getCurrentItem();
		}
		super.onPause();
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


	@Override
	public ExpandableGroupDescriptor getGroupDescriptor(int position)
	{
		return GROUP_DESCRIPTORS[position];
	}
}
