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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ExpandableListView;
import android.widget.ListView;


/**
 * An activity representing a list of Tasks. This activity has different presentations for handset and tablet-size devices. On handsets, the activity presents a
 * list of items, which when touched, lead to a {@link ViewTaskActivity} representing item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a {@link TaskListFragment} and the item details (if present) is a
 * {@link ViewTaskFragment}.
 * <p>
 * This activity also implements the required {@link TaskListFragment.Callbacks} interface to listen for item selections.
 */
public class TaskListActivity extends FragmentActivity implements TaskListFragment.Callbacks, ViewTaskFragment.Callback
{

	private static final String TAG = "TaskListActivity";
	private static final String OPEN_CHILD_PREFERENCE_NAME = "open_child";
	private static final String OPEN_GROUP_PREFERENCE_NAME = "open_group";
	private static final String EXPANDED_GROUPS_PREFERENCE_NAME = "expanded_groups";
	/**
	 * Whether or not the activity is in two-pane mode, i.e. running on a tablet device.
	 */
	private boolean mTwoPane;
	Context appContext;
	ViewTaskFragment taskDetailFrag;
	TaskListFragment taskListFrag;
	SharedPreferences openTaskPrefs;


	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		Log.d(TAG, "onCreate called again");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_task_list);
		appContext = getApplicationContext();

		if (findViewById(R.id.task_detail_container) != null)
		{
			// The detail container view will be present only in the
			// large-screen layouts (res/values-large and
			// res/values-sw600dp). If this view is present, then the
			// activity should be in two-pane mode.
			mTwoPane = true;

			// In two-pane mode, list items should be given the
			// 'activated' state when touched.

			taskListFrag = (TaskListFragment) getSupportFragmentManager().findFragmentById(R.id.task_list);
			taskListFrag.setActivateOnItemClick(true);

			openTaskPrefs = getPreferences(MODE_PRIVATE);
			int openChildPosition = openTaskPrefs.getInt(OPEN_CHILD_PREFERENCE_NAME, ExpandableListView.INVALID_POSITION);
			int openGroupPosition = openTaskPrefs.getInt(OPEN_GROUP_PREFERENCE_NAME, ExpandableListView.INVALID_POSITION);

			Log.d(TAG, "Open Child Position : " + openChildPosition);
			Log.d(TAG, "Open Group Position : " + openGroupPosition);

			if (openChildPosition != ExpandableListView.INVALID_POSITION && openGroupPosition != ExpandableListView.INVALID_POSITION)
			{
				taskListFrag.setOpenChildPosition(openChildPosition);
				taskListFrag.setOpenGroupPosition(openGroupPosition);
			}

			String openGroupsString = openTaskPrefs.getString(EXPANDED_GROUPS_PREFERENCE_NAME, "");

			String[] openGroupsArray = TextUtils.split(openGroupsString, "-");
			long[] ids = new long[openGroupsArray.length];
			for (int i = 0; i < openGroupsArray.length; i++)
			{
				ids[i] = Long.parseLong(openGroupsArray[i]);
			}

			taskListFrag.setExpandedGroupsIds(ids);

			/*
			 * Create a detail fragment, but don't load any URL yet, we do that later when the fragment gets attached
			 */
			taskDetailFrag = new ViewTaskFragment();
			getSupportFragmentManager().beginTransaction().replace(R.id.task_detail_container, taskDetailFrag).commit();
		}

		// TODO: If exposing deep links into your app, handle intents here.
	}


	/**
	 * Callback method from {@link TaskListFragment.Callbacks} indicating that the item with the given ID was selected.
	 */
	@Override
	public void onItemSelected(Uri uri, boolean forceReload)
	{
		if (mTwoPane)
		{
			taskDetailFrag.loadUri(uri);
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
	public void displayEditTask(Uri taskUri)
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
		startActivity(editTaskIntent);
	}


	@Override
	public void onPause()
	{
		super.onPause();
		if (taskListFrag != null)
		{
			int openChildPosition = taskListFrag.getOpenChildPosition();
			int openGroupPosition = taskListFrag.getOpenGroupPosition();
			SharedPreferences.Editor openPositsEditor = openTaskPrefs.edit();

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

			long[] ids = taskListFrag.getExpandedGroups();

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

}
