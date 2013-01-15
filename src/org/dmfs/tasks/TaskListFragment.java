package org.dmfs.tasks;

import org.dmfs.provider.tasks.TaskContract;
import org.dmfs.provider.tasks.TaskContract.Tasks;
import org.dmfs.tasks.dummy.DummyContent;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.content.CursorLoader;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A list fragment representing a list of Tasks. This fragment also supports
 * tablet devices by allowing list items to be given an 'activated' state upon
 * selection. This helps indicate which item is currently being viewed in a
 * {@link TaskDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class TaskListFragment extends ListFragment {

	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * activated item position. Only used on tablets.
	 */
	private static final String STATE_ACTIVATED_POSITION = "activated_position";

	private static final String AUTHORITY = "ms.jung.android.caldavtodo.provider";

	private static final String CONTENT_URI_PATH = "tasks";

	private static final String TAG = "TaskListFragment";

	/**
	 * The fragment's current callback object, which is notified of list item
	 * clicks.
	 */
	private Callbacks mCallbacks = sDummyCallbacks;

	/**
	 * The current activated item position. Only used on tablets.
	 */
	private int mActivatedPosition = ListView.INVALID_POSITION;

	private Cursor taskCursor;
	private Context appContext;

	/**
	 * A callback interface that all activities containing this fragment must
	 * implement. This mechanism allows activities to be notified of item
	 * selections.
	 */
	public interface Callbacks {
		/**
		 * Callback for when an item has been selected.
		 */
		public void onItemSelected(String id);
	}

	/**
	 * A dummy implementation of the {@link Callbacks} interface that does
	 * nothing. Used only when this fragment is not attached to an activity.
	 */
	private static Callbacks sDummyCallbacks = new Callbacks() {
		@Override
		public void onItemSelected(String id) {
		}
	};

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public TaskListFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		

		//CursorLoader taskCursorLoader = new CursorLoader(appContext, tasksURI,
		//		new String[] { "_id", "title" }, null, null, null);
		Cursor tasksCursor = appContext.getContentResolver().query(TaskContract.Tasks.CONTENT_URI,
				new String[] { TaskContract.Tasks._ID, TaskContract.Tasks.TITLE }, null, null, null);

		// TODO: replace with a real list adapter.
		//setListAdapter(new ArrayAdapter<DummyContent.DummyItem>(getActivity(),
		//		android.R.layout.simple_list_item_activated_1,
		//		android.R.id.text1, DummyContent.ITEMS));
		Log.d(TAG, "No of tasks are :" + tasksCursor.getCount());
		setListAdapter(new TaskCursorAdapter(appContext, tasksCursor));
	}
	
	
	private class TaskCursorAdapter extends CursorAdapter{
		
		LayoutInflater viewInflater;
		int columnIndex;
		public TaskCursorAdapter(Context context, Cursor cursor){
			super(context, cursor, false);
			viewInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			columnIndex = cursor.getColumnIndex("title");
		}
		
		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			TextView tv = (TextView) view.findViewById(R.id.task_title);
			String taskName = cursor.getString(columnIndex);
			tv.setText(taskName);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
			View inflatedView = viewInflater.inflate(R.layout.task_title_display, null);
			TextView tv = (TextView) inflatedView.findViewById(R.id.task_title);
			String taskName = cursor.getString(columnIndex);
			tv.setText(taskName);
			return inflatedView;
		}
		
	};

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// Restore the previously serialized activated item position.
		if (savedInstanceState != null
				&& savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
			setActivatedPosition(savedInstanceState
					.getInt(STATE_ACTIVATED_POSITION));
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		appContext = activity.getApplicationContext();

		// Activities containing this fragment must implement its callbacks.
		if (!(activity instanceof Callbacks)) {
			throw new IllegalStateException(
					"Activity must implement fragment's callbacks.");
		}

		mCallbacks = (Callbacks) activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();

		// Reset the active callbacks interface to the dummy implementation.
		mCallbacks = sDummyCallbacks;
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position,
			long id) {
		super.onListItemClick(listView, view, position, id);
		ListAdapter la = listView.getAdapter();
		Cursor selectedItem = (Cursor)la.getItem(position);
		int taskIdIndex = selectedItem.getColumnIndex(Tasks._ID);
		String selectedId = selectedItem.getString(taskIdIndex);
		Toast.makeText(appContext, "Selected ID is : " + selectedId, Toast.LENGTH_SHORT).show();
		// Notify the active callbacks interface (the activity, if the
		// fragment is attached to one) that an item has been selected.
		mCallbacks.onItemSelected(selectedId);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mActivatedPosition != ListView.INVALID_POSITION) {
			// Serialize and persist the activated item position.
			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
		}
	}

	/**
	 * Turns on activate-on-click mode. When this mode is on, list items will be
	 * given the 'activated' state when touched.
	 */
	public void setActivateOnItemClick(boolean activateOnItemClick) {
		// When setting CHOICE_MODE_SINGLE, ListView will automatically
		// give items the 'activated' state when touched.
		getListView().setChoiceMode(
				activateOnItemClick ? ListView.CHOICE_MODE_SINGLE
						: ListView.CHOICE_MODE_NONE);
	}

	private void setActivatedPosition(int position) {
		if (position == ListView.INVALID_POSITION) {
			getListView().setItemChecked(mActivatedPosition, false);
		} else {
			getListView().setItemChecked(position, true);
		}

		mActivatedPosition = position;
	}
}
