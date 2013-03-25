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

import org.dmfs.provider.tasks.TaskContract;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.Button;


/**
 * This extends the {@link FragmentActivity} for displaying the list of synced or visible task-providers. It displays the visible providers when it is created.
 * 
 * @author Arjun Naik<arjun@arjunnaik.in>
 * 
 */
public class SyncSettingsActivity extends FragmentActivity
{
	FragmentManager manager;
	SettingsListFragment currentFrag;


	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		// Show the Up button in the action bar.
		setupActionBar();

		manager = getSupportFragmentManager();
		showVisibleListsFragment();

	}


	/**
	 * This function displays the list of providers which can be visible or hidden in {@link TaskListFragment}.
	 */
	public void showVisibleListsFragment()
	{
		SettingsListFragment syncedListFragment = new SettingsListFragment();
		Bundle args = new Bundle();
		args.putStringArray(SettingsListFragment.LIST_STRING_PARAMS, new String[] { "1" });
		args.putInt(SettingsListFragment.LIST_FRAGMENT_LAYOUT, R.layout.fragment_visiblelist);
		args.putString(SettingsListFragment.LIST_SELECTION_ARGS, TaskContract.TaskLists.SYNC_ENABLED + "=?");
		args.putString(SettingsListFragment.COMPARE_COLUMN_NAME, TaskContract.TaskLists.VISIBLE);
		args.putBoolean(SettingsListFragment.LIST_ONDETACH_SAVE, true);
		syncedListFragment.setArguments(args);
		manager.beginTransaction().replace(R.id.visible_task_list_fragment, syncedListFragment).commit();
		currentFrag = syncedListFragment;
	}


	/**
	 * This function displays the list of providers which can synced.
	 */
	public void showSyncedListsFragment()
	{
		SettingsListFragment syncedListFragment = new SettingsListFragment();
		Bundle args = new Bundle();
		args.putStringArray(SettingsListFragment.LIST_STRING_PARAMS, null);
		args.putInt(SettingsListFragment.LIST_FRAGMENT_LAYOUT, R.layout.fragment_synced_task_list);
		args.putString(SettingsListFragment.LIST_SELECTION_ARGS, null);
		args.putString(SettingsListFragment.COMPARE_COLUMN_NAME, TaskContract.TaskLists.SYNC_ENABLED);
		args.putBoolean(SettingsListFragment.LIST_ONDETACH_SAVE, false);
		syncedListFragment.setArguments(args);
		manager.beginTransaction().replace(R.id.visible_task_list_fragment, syncedListFragment).commit();
		currentFrag = syncedListFragment;
	}


	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar()
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}


	/**
	 * This function is a handler for the {@link Button} which is present in the layout loaded by {@link SettingsListFragment}. When this button is clicked the
	 * {@link SyncSettingsActivity} instructs the {@link SettingsListFragment} to save the current modification and then loads a {@link SettingsListFragment}
	 * which shows the list the syncable task-providers.
	 * 
	 * @param v
	 *            Reference to the {@link Button} which was clicked is passed as a {@link View} object.
	 */
	public void showSyncedList(View v)
	{
		currentFrag.saveListState();
		// Call a function to indicate to the fragment that the state change to the list have been saved(clear the hashmap).
		currentFrag.doneSaveListState();
		showSyncedListsFragment();
	}


	/**
	 * This function is a handler for the {@link Button} which is present in the layout loaded by {@link SettingsListFragment}. When this button is clicked the
	 * {@link SyncSettingsActivity} instructs the {@link SettingsListFragment} to save the current modification and then loads a {@link SettingsListFragment}
	 * which shows the list the displayable task-providers.
	 * 
	 * @param v
	 *            Reference to the {@link Button} which was clicked is passed as a {@link View} object.
	 */
	public void onSaveUpdated(View v)
	{
		currentFrag.saveListState();
		showVisibleListsFragment();

	}


	/**
	 * This function is a handler for the {@link Button} which is present in the layout loaded by {@link SettingsListFragment}. When this button is clicked the
	 * list showing the syncable task-providers is displayed.
	 * 
	 * @param v
	 *            Reference to the {@link Button} which was clicked is passed as a {@link View} object.
	 */
	public void onCancelUpdated(View v)
	{
		showVisibleListsFragment();
	}

}
