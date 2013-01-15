package org.dmfs.tasks;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.dmfs.provider.tasks.TaskContract;
import org.dmfs.tasks.dummy.DummyContent;

/**
 * A fragment representing a single Task detail screen. This fragment is either
 * contained in a {@link TaskListActivity} in two-pane mode (on tablets) or a
 * {@link TaskDetailActivity} on handsets.
 */
public class TaskDetailFragment extends Fragment {
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_ITEM_ID = "item_id";

	private static final String TAG = "TaskDetailFragment";

	/**
	 * The dummy content this fragment is presenting.
	 */
	private String taskId;
	private Context appContext;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public TaskDetailFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		taskId = getArguments().getString(ARG_ITEM_ID);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		appContext = activity.getApplicationContext();

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_task_detail,
				container, false);

		// Show the dummy content as text in a TextView.
		if (taskId != null) {

			Cursor selectedTaskCursor = appContext.getContentResolver().query(
					TaskContract.Tasks.CONTENT_URI,
					new String[] { TaskContract.Tasks._ID,
							TaskContract.Tasks.TITLE,
							TaskContract.Tasks.PRIORITY, TaskContract.Tasks.DESCRIPTION },
					TaskContract.Tasks._ID + "=?", new String[] { taskId },
					null);

			Log.d(TAG, "Are there results :"
					+ (selectedTaskCursor.getCount() > 0 ? "Yes" : "No"));
			
			
			int titleColumn = selectedTaskCursor.getColumnIndex(TaskContract.Tasks.TITLE);
			int priorityColumn = selectedTaskCursor.getColumnIndex(TaskContract.Tasks.PRIORITY);
			int descriptionColumn = selectedTaskCursor.getColumnIndex(TaskContract.Tasks.DESCRIPTION);
			
			selectedTaskCursor.moveToFirst();
			String taskTitle = selectedTaskCursor.getString(titleColumn);
			int taskPriority = selectedTaskCursor.getInt(priorityColumn);
			String taskDescription = selectedTaskCursor.getString(descriptionColumn);
			
			Log.d(TAG, taskTitle);
			Log.d(TAG, "" + taskDescription);
			Log.d(TAG, "" + taskPriority);
			
			((TextView) rootView.findViewById(R.id.task_title))
					.setText(taskTitle);
		}

		return rootView;
	}
}
