/*
 * Copyright 2017 dmfs GmbH
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
 */
package org.dmfs.tasks;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import android.view.View;
import android.widget.Button;

import com.google.android.material.appbar.AppBarLayout;

import org.dmfs.tasks.contract.TaskContract;
import org.dmfs.tasks.utils.BaseActivity;


/**
 * This extends the {@link FragmentActivity} for displaying the list of synced or visible task-providers. It displays the visible providers when it is created.
 *
 * @author Arjun Naik<arjun@arjunnaik.in>
 */
public class SyncSettingsActivity extends BaseActivity
{
    private FragmentManager mManager;
    private SettingsListFragment mCurrentFrag;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        // Show the Up button in the action bar.
        AppBarLayout mAppBarLayout = findViewById(R.id.appbar);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mManager = getSupportFragmentManager();
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
        mManager.beginTransaction().replace(R.id.visible_task_list_fragment, syncedListFragment).commit();
        mCurrentFrag = syncedListFragment;
        getSupportActionBar().setTitle(R.string.visible_task_lists);
    }


    /**
     * This function displays the list of providers which can be synced.
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
        mManager.beginTransaction().replace(R.id.visible_task_list_fragment, syncedListFragment).commit();
        mCurrentFrag = syncedListFragment;
        getSupportActionBar().setTitle(R.string.synced_task_lists);
    }


    /**
     * This function is a handler for the {@link Button} which is present in the layout loaded by {@link SettingsListFragment}. When this button is clicked the
     * {@link SyncSettingsActivity} instructs the {@link SettingsListFragment} to save the current modification and then loads a {@link SettingsListFragment}
     * which shows the list the syncable task-providers.
     *
     * @param v
     *         Reference to the {@link Button} which was clicked is passed as a {@link View} object.
     */
    public void showSyncedList(View v)
    {
        mCurrentFrag.saveListState();
        // Call a function to indicate to the fragment that the state change to the list have been saved(clear the hashmap).
        mCurrentFrag.doneSaveListState();
        showSyncedListsFragment();
    }


    /**
     * This function is a handler for the {@link Button} which is present in the layout loaded by {@link SettingsListFragment}. When this button is clicked the
     * {@link SyncSettingsActivity} instructs the {@link SettingsListFragment} to save the current modification and then loads a {@link SettingsListFragment}
     * which shows the list the displayable task-providers.
     *
     * @param v
     *         Reference to the {@link Button} which was clicked is passed as a {@link View} object.
     */
    public void onSaveUpdated(View v)
    {
        mCurrentFrag.saveListState();
        showVisibleListsFragment();

    }


    /**
     * This function is a handler for the {@link Button} which is present in the layout loaded by {@link SettingsListFragment}. When this button is clicked the
     * list showing the syncable task-providers is displayed.
     *
     * @param v
     *         Reference to the {@link Button} which was clicked is passed as a {@link View} object.
     */
    public void onCancelUpdated(View v)
    {
        showVisibleListsFragment();
    }

}
