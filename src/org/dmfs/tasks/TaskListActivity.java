package org.dmfs.tasks;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

/**
 * An activity representing a list of Tasks. This activity has different
 * presentations for handset and tablet-size devices. On handsets, the activity
 * presents a list of items, which when touched, lead to a
 * {@link TaskDetailActivity} representing item details. On tablets, the
 * activity presents the list of items and item details side-by-side using two
 * vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link TaskListFragment} and the item details (if present) is a
 * {@link TaskViewDetailFragment}.
 * <p>
 * This activity also implements the required {@link TaskListFragment.Callbacks}
 * interface to listen for item selections.
 */
public class TaskListActivity extends FragmentActivity implements
		TaskListFragment.Callbacks, TaskViewDetailFragment.Callback {

	private static final String TAG = "TaskListActivity";
	/**
	 * Whether or not the activity is in two-pane mode, i.e. running on a tablet
	 * device.
	 */
	private boolean mTwoPane;
	Context appContext;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_task_list);
		appContext = getApplicationContext();
		
		if (findViewById(R.id.task_detail_container) != null) {
			// The detail container view will be present only in the
			// large-screen layouts (res/values-large and
			// res/values-sw600dp). If this view is present, then the
			// activity should be in two-pane mode.
			mTwoPane = true;

			// In two-pane mode, list items should be given the
			// 'activated' state when touched.
			((TaskListFragment) getSupportFragmentManager().findFragmentById(
					R.id.task_list)).setActivateOnItemClick(true);
		}

		// TODO: If exposing deep links into your app, handle intents here.
	}

	/**
	 * Callback method from {@link TaskListFragment.Callbacks} indicating that
	 * the item with the given ID was selected.
	 */
	@Override
	public void onItemSelected(Uri uri) {
		if (mTwoPane) {
			// In two-pane mode, show the detail view in this activity by
			// adding or replacing the detail fragment using a
			// fragment transaction.
			Bundle arguments = new Bundle();
			Log.d(TAG, "Added Fragment Intent");
			arguments.putParcelable(TaskViewDetailFragment.ARG_ITEM_ID, uri);
			TaskViewDetailFragment fragment = new TaskViewDetailFragment();
			fragment.setArguments(arguments);
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.task_detail_container, fragment).commit();

		} else {
			// In single-pane mode, simply start the detail activity
			// for the selected item ID.
			Intent detailIntent = new Intent(this, TaskDetailActivity.class);
			detailIntent.putExtra(TaskViewDetailFragment.ARG_ITEM_ID, uri);
			startActivity(detailIntent);
		}
	}

	@Override
	public void displayEditTask(Uri taskUri)
	{
		if (mTwoPane) {
			Toast.makeText(appContext, "Edit Task : " + taskUri, Toast.LENGTH_SHORT).show();
			Log.d(TAG,"Display Edit Task");
			
			Bundle arguments = new Bundle();
			arguments.putParcelable(TaskEditDetailFragment.ARG_ITEM_ID, taskUri);
			arguments.putString(TaskEditDetailFragment.FRAGMENT_INTENT, TaskEditDetailFragment.EDIT_TASK);
			TaskEditDetailFragment fragment = new TaskEditDetailFragment();
			fragment.setArguments(arguments);
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.task_detail_container, fragment).commit();

		}
		
	}

	@Override
	public void onAddNewTask()
	{
		if (mTwoPane) {
			Bundle arguments = new Bundle();
			arguments.putString(TaskEditDetailFragment.FRAGMENT_INTENT, TaskEditDetailFragment.NEW_TASK);
			TaskEditDetailFragment fragment = new TaskEditDetailFragment();
			fragment.setArguments(arguments);
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.task_detail_container, fragment).commit();

		} else {
			Intent addTaskIntent = new Intent(this, AddTaskActivity.class);
			startActivity(addTaskIntent);
		}
		
	}
}
