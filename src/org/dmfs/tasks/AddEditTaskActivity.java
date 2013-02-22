package org.dmfs.tasks;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;


public class AddEditTaskActivity extends FragmentActivity
{
	public static final String NEW_TASK = "new_task";
	public static final String EDIT_TASK = "edit_task";
	Intent appIntent;


	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_task);
		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);

		appIntent = getIntent();
		if (appIntent.getAction().equals(AddEditTaskActivity.NEW_TASK))
		{
			if (savedInstanceState == null)
			{
				// Create the detail fragment and add it to the activity
				// using a fragment transaction.
				Bundle arguments = new Bundle();
				arguments.putString(TaskEditDetailFragment.FRAGMENT_INTENT, TaskEditDetailFragment.NEW_TASK);
				TaskEditDetailFragment fragment = new TaskEditDetailFragment();
				fragment.setArguments(arguments);
				getSupportFragmentManager().beginTransaction().add(R.id.add_task_container, fragment).commit();
			}
		}
		else if(appIntent.getAction().equals(AddEditTaskActivity.EDIT_TASK)){
			Bundle arguments = new Bundle();
			arguments.putString(TaskEditDetailFragment.FRAGMENT_INTENT, TaskEditDetailFragment.EDIT_TASK);
			arguments.putParcelable(TaskViewDetailFragment.ARG_ITEM_ID, appIntent.getParcelableExtra(TaskViewDetailFragment.ARG_ITEM_ID));
			TaskEditDetailFragment fragment = new TaskEditDetailFragment();
			fragment.setArguments(arguments);
			getSupportFragmentManager().beginTransaction().add(R.id.add_task_container, fragment).commit();
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.activity_edit_task, menu);
		return true;
	}


	/*@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case android.R.id.home:
				NavUtils.navigateUpFromSameTask(this);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}*/

}
