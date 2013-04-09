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

package org.dmfs.tasks;

import org.dmfs.provider.tasks.TaskContract.Tasks;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ExpandableListView;
import android.widget.ListView;


/**
 * An activity representing a list of Tasks. This activity has different presentations for handset and tablet-size devices. On handsets, the activity presents a
 * list of items, which when touched, lead to a {@link ViewTaskActivity} representing item details. On tablets, the activity presents the list of items and item
 * details side-by-side using two vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a {@link TaskListFragment} and the item details (if present) is a {@link ViewTaskFragment}.
 * <p>
 * This activity also implements the required {@link TaskListFragment.Callbacks} interface to listen for item selections.
 */
public class TaskListActivity extends FragmentActivity implements TaskListFragment.Callbacks, ViewTaskFragment.Callback
{

	private static final String TAG = "TaskListActivity";
	private static final String OPEN_CHILD_PREFERENCE_NAME = "open_child";
	private static final String OPEN_GROUP_PREFERENCE_NAME = "open_group";
	private static final String EXPANDED_GROUPS_PREFERENCE_NAME = "expanded_groups";

	private final static int REQUEST_CODE_NEW_TASK = 2924;

	/**
	 * Whether or not the activity is in two-pane mode, i.e. running on a tablet device.
	 */
	private boolean mTwoPane;
	private ViewTaskFragment mTaskDetailFrag;
	private TaskListFragment mTaskListFrag;
	private SharedPreferences mOpenTaskPrefs;


	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		Log.d(TAG, "onCreate called again");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_task_list);

		if (findViewById(R.id.task_detail_container) != null)
		{
			// The detail container view will be present only in the
			// large-screen layouts (res/values-large and
			// res/values-sw600dp). If this view is present, then the
			// activity should be in two-pane mode.
			mTwoPane = true;

			// In two-pane mode, list items should be given the
			// 'activated' state when touched.

			mTaskListFrag = (TaskListFragment) getSupportFragmentManager().findFragmentById(R.id.task_list);

			mTaskListFrag.setActivateOnItemClick(true);
			mTaskListFrag.setListViewScrollbarPositionLeft(true);

			mOpenTaskPrefs = getPreferences(MODE_PRIVATE);
			int openChildPosition = mOpenTaskPrefs.getInt(OPEN_CHILD_PREFERENCE_NAME, ExpandableListView.INVALID_POSITION);
			int openGroupPosition = mOpenTaskPrefs.getInt(OPEN_GROUP_PREFERENCE_NAME, ExpandableListView.INVALID_POSITION);

			Log.d(TAG, "Open Child Position : " + openChildPosition);
			Log.d(TAG, "Open Group Position : " + openGroupPosition);

			if (openChildPosition != ExpandableListView.INVALID_POSITION && openGroupPosition != ExpandableListView.INVALID_POSITION)
			{
				mTaskListFrag.setOpenChildPosition(openChildPosition);
				mTaskListFrag.setOpenGroupPosition(openGroupPosition);
			}

			String openGroupsString = mOpenTaskPrefs.getString(EXPANDED_GROUPS_PREFERENCE_NAME, "");

			String[] openGroupsArray = TextUtils.split(openGroupsString, "-");
			long[] ids = new long[openGroupsArray.length];
			for (int i = 0; i < openGroupsArray.length; i++)
			{
				ids[i] = Long.parseLong(openGroupsArray[i]);
			}

			mTaskListFrag.setExpandedGroupsIds(ids);

			/*
			 * Create a detail fragment, but don't load any URL yet, we do that later when the fragment gets attached
			 */
			mTaskDetailFrag = new ViewTaskFragment();
			getSupportFragmentManager().beginTransaction().replace(R.id.task_detail_container, mTaskDetailFrag).commit();
		}
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
						onItemSelected(newTaskUri, false);
					}
			}

		}
	}


	@Override
	public void onPause()
	{
		super.onPause();
		if (mTaskListFrag != null)
		{
			int openChildPosition = mTaskListFrag.getOpenChildPosition();
			int openGroupPosition = mTaskListFrag.getOpenGroupPosition();
			SharedPreferences.Editor openPositsEditor = mOpenTaskPrefs.edit();

			if (openChildPosition != ListView.INVALID_POSITION && openGroupPosition != ExpandableListView.INVALID_POSITION)
			{

				openPositsEditor.putInt(OPEN_CHILD_PREFERENCE_NAME, openChildPosition);
				openPositsEditor.putInt(OPEN_GROUP_PREFERENCE_NAME, openGroupPosition);

				Log.d(TAG, "Saved Child Pos : " + openChildPosition);
				Log.d(TAG, "Saved Group Pos : " + openGroupPosition);
			}
			else
			{
				Log.d(TAG, "Nothing Selected. Nothing Saved");
			}

			long[] ids = mTaskListFrag.getExpandedGroups();

			if (ids.length > 0)
			{
				StringBuilder openGroupBuilder = new StringBuilder();

				for (long id : ids)
				{
					openGroupBuilder.append(Long.toString(id));
					openGroupBuilder.append("-");
				}

				openPositsEditor.putString(EXPANDED_GROUPS_PREFERENCE_NAME, openGroupBuilder.substring(0, openGroupBuilder.length() - 1));
			}
			else
			{
				openPositsEditor.remove(EXPANDED_GROUPS_PREFERENCE_NAME);
			}
			openPositsEditor.commit();
			Log.d(TAG, "Finished Saving the open positions");
		}
		else
		{
			Log.d(TAG, "taskListFrag is NULL!!");
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
		else
		{
			return super.onOptionsItemSelected(item);
		}
	}
}
