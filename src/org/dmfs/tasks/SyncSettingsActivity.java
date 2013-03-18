package org.dmfs.tasks;

import org.dmfs.provider.tasks.TaskContract;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class SyncSettingsActivity extends FragmentActivity implements SettingsListFragment.OnFragmentInteractionListener
{
	FragmentManager manager;


	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		// Show the Up button in the action bar.
		setupActionBar();

		manager = getSupportFragmentManager();

		SettingsListFragment syncedListFragment = new SettingsListFragment(TaskContract.TaskLists.SYNC_ENABLED + "=?", new String[] { "1" },
			R.layout.fragment_visiblelist, TaskContract.TaskLists.VISIBLE);
		manager.beginTransaction().replace(R.id.visible_task_list_fragment, syncedListFragment).commit();

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


	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case android.R.id.home:
				// This ID represents the Home or Up button. In the case of this
				// activity, the Up button is shown. Use NavUtils to allow users
				// to navigate up one level in the application structure. For
				// more details, see the Navigation pattern on Android Design:
				//
				// http://developer.android.com/design/patterns/navigation.html#up-vs-back
				//
				NavUtils.navigateUpFromSameTask(this);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	public void viewSyncedLists()
	{

	}


	@Override
	public void savedUpdatedSyncedLists()
	{

	}


	@Override
	public void cancelFromSyncedLists()
	{

	}


	public void showSyncedList(View v)
	{
		SettingsListFragment syncedListFragment = new SettingsListFragment(null, null, R.layout.fragment_synced_task_list, TaskContract.TaskLists.SYNC_ENABLED);
		manager.beginTransaction().replace(R.id.visible_task_list_fragment, syncedListFragment).commit();
	}


	public void onSaveUpdated(View v)
	{
		SettingsListFragment syncedListFragment = new SettingsListFragment(TaskContract.TaskLists.SYNC_ENABLED + "=?", new String[] { "1" },
			R.layout.fragment_visiblelist, TaskContract.TaskLists.VISIBLE);
		manager.beginTransaction().replace(R.id.visible_task_list_fragment, syncedListFragment).commit();
	}


	public void onCancelUpdated(View v)
	{
		SettingsListFragment syncedListFragment = new SettingsListFragment(TaskContract.TaskLists.SYNC_ENABLED + "=?", new String[] { "1" },
			R.layout.fragment_visiblelist, TaskContract.TaskLists.VISIBLE);
		manager.beginTransaction().replace(R.id.visible_task_list_fragment, syncedListFragment).commit();
	}

}
