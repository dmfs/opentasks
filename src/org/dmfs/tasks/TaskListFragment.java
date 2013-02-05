package org.dmfs.tasks;

import java.text.DateFormat;
import java.util.Date;

import org.dmfs.provider.tasks.TaskContract;
import org.dmfs.provider.tasks.TaskContract.Tasks;
import org.dmfs.tasks.model.adapters.TimeFieldAdapter;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.CursorAdapter;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


/**
 * A list fragment representing a list of Tasks. This fragment also supports tablet devices by allowing list items to be given an 'activated' state upon
 * selection. This helps indicate which item is currently being viewed in a {@link TaskViewDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks} interface.
 */
public class TaskListFragment extends ListFragment
{

	/**
	 * The serialization (saved instance state) Bundle key representing the activated item position. Only used on tablets.
	 */
	private static final String STATE_ACTIVATED_POSITION = "activated_position";

	private static final String AUTHORITY = "ms.jung.android.caldavtodo.provider";

	private static final String CONTENT_URI_PATH = "tasks";

	private static final String TAG = "TaskListFragment";

	/**
	 * The fragment's current callback object, which is notified of list item clicks.
	 */
	private Callbacks mCallbacks;

	/**
	 * The current activated item position. Only used on tablets.
	 */
	private int mActivatedPosition = ListView.INVALID_POSITION;

	private Cursor taskCursor;
	private Context appContext;
	private static final TimeFieldAdapter TFADAPTER = new TimeFieldAdapter(TaskContract.Tasks.DUE, TaskContract.Tasks.TZ, TaskContract.Tasks.IS_ALLDAY);

	/**
	 * A callback interface that all activities containing this fragment must implement. This mechanism allows activities to be notified of item selections.
	 */
	public interface Callbacks
	{
		/**
		 * Callback for when an item has been selected.
		 */
		public void onItemSelected(Uri taskUri);


		public void onAddNewTask();
	}


	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the fragment (e.g. upon screen orientation changes).
	 */
	public TaskListFragment()
	{
	}


	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// CursorLoader taskCursorLoader = new CursorLoader(appContext,
		// tasksURI,
		// new String[] { "_id", "title" }, null, null, null);
		Cursor tasksCursor = appContext.getContentResolver().query(
			TaskContract.Tasks.CONTENT_URI,
			new String[] { TaskContract.Tasks._ID, TaskContract.Tasks.TITLE, TaskContract.Tasks.LIST_COLOR, TaskContract.Tasks.DUE, TaskContract.Tasks.TZ,
				TaskContract.Tasks.IS_ALLDAY }, null, null, null);

		// TODO: replace with a real list adapter.
		// setListAdapter(new
		// ArrayAdapter<DummyContent.DummyItem>(getActivity(),
		// android.R.layout.simple_list_item_activated_1,
		// android.R.id.text1, DummyContent.ITEMS));
		Log.d(TAG, "No of tasks are :" + tasksCursor.getCount());
		setListAdapter(new TaskCursorAdapter(appContext, tasksCursor));
		setHasOptionsMenu(true);
	}

	private class TaskCursorAdapter extends CursorAdapter
	{

		private LayoutInflater mViewInflater;
		private int mTitleColumnIndex;
		private int mListColorColumnIndex;


		public TaskCursorAdapter(Context context, Cursor cursor)
		{
			super(context, cursor, false);
			mViewInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mTitleColumnIndex = cursor.getColumnIndex(Tasks.TITLE);
			mListColorColumnIndex = cursor.getColumnIndex(Tasks.LIST_COLOR);
		}


		@Override
		public void bindView(View view, Context context, Cursor cursor)
		{
			TextView tv = (TextView) view.findViewById(R.id.task_title);
			View cv = view.findViewById(R.id.colorbar);
			String taskName = cursor.getString(mTitleColumnIndex);
			tv.setText(taskName);
			cv.setBackgroundColor(cursor.getInt(mListColorColumnIndex));
			TextView dueDateTV = (TextView) view.findViewById(R.id.task_due_date);
			Time dueTime = TFADAPTER.get(cursor);
			if (dueTime == null)
			{
				dueDateTV.setVisibility(View.GONE);
			}
			else
			{
				dueDateTV.setVisibility(View.VISIBLE);
				new DueDisplayer(dueTime).display(dueDateTV);
			}
		}


		@Override
		public View newView(Context context, Cursor cursor, ViewGroup viewGroup)
		{
			View inflatedView = mViewInflater.inflate(R.layout.task_list_element, null);
			TextView tv = (TextView) inflatedView.findViewById(R.id.task_title);
			View cv = inflatedView.findViewById(R.id.colorbar);
			String taskName = cursor.getString(mTitleColumnIndex);
			tv.setText(taskName);
			cv.setBackgroundColor(cursor.getInt(mListColorColumnIndex));
			return inflatedView;
		}

	};


	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

		// Restore the previously serialized activated item position.
		if (savedInstanceState != null && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION))
		{
			setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
		}
	}


	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);

		appContext = activity.getApplicationContext();

		// Activities containing this fragment must implement its callbacks.
		if (!(activity instanceof Callbacks))
		{
			throw new IllegalStateException("Activity must implement fragment's callbacks.");
		}

		mCallbacks = (Callbacks) activity;
	}


	@Override
	public void onDetach()
	{
		super.onDetach();

	}


	@Override
	public void onListItemClick(ListView listView, View view, int position, long id)
	{
		super.onListItemClick(listView, view, position, id);
		ListAdapter la = listView.getAdapter();
		Cursor selectedItem = (Cursor) la.getItem(position);
		int taskIdIndex = selectedItem.getColumnIndex(Tasks._ID);
		String selectedId = selectedItem.getString(taskIdIndex);
		Toast.makeText(appContext, "Selected ID is : " + selectedId, Toast.LENGTH_SHORT).show();
		// Notify the active callbacks interface (the activity, if the
		// fragment is attached to one) that an item has been selected.
		Uri taskUri = ContentUris.withAppendedId(Tasks.CONTENT_URI, Long.parseLong(selectedId));
		mCallbacks.onItemSelected(taskUri);

	}


	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		if (mActivatedPosition != ListView.INVALID_POSITION)
		{
			// Serialize and persist the activated item position.
			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
		}
	}


	/**
	 * Turns on activate-on-click mode. When this mode is on, list items will be given the 'activated' state when touched.
	 */
	public void setActivateOnItemClick(boolean activateOnItemClick)
	{
		// When setting CHOICE_MODE_SINGLE, ListView will automatically
		// give items the 'activated' state when touched.
		getListView().setChoiceMode(activateOnItemClick ? ListView.CHOICE_MODE_SINGLE : ListView.CHOICE_MODE_NONE);
	}


	private void setActivatedPosition(int position)
	{
		if (position == ListView.INVALID_POSITION)
		{
			getListView().setItemChecked(mActivatedPosition, false);
		}
		else
		{
			getListView().setItemChecked(position, true);
		}

		mActivatedPosition = position;
	}


	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		inflater.inflate(R.menu.task_list_fragment_menu, menu);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.add_task_item:
				mCallbacks.onAddNewTask();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private class DueDisplayer
	{
		Time today, dueDate;
		DateFormat dateFormatter;
		DateFormat timeFormatter;


		public DueDisplayer(Time d)
		{
			dueDate = d;
			today = new Time();
			today.setToNow();
			dateFormatter = android.text.format.DateFormat.getDateFormat(appContext);
			timeFormatter = android.text.format.DateFormat.getTimeFormat(appContext);
		}


		public void display(TextView tv)
		{
			if (dueDate.year == today.year && dueDate.month == today.month && dueDate.monthDay == today.monthDay)
			{
				tv.setText(timeFormatter.format(new Date(dueDate.toMillis(false))));
				if (dueDate.before(today))
				{
					Log.d(TAG, "Before Today");
					tv.setTextColor(Color.RED);
				}

			}
			else
			{
				tv.setText(dateFormatter.format(new Date(dueDate.toMillis(false))));
				if (dueDate.before(today))
				{
					Log.d(TAG, "Before Today");
					tv.setTextColor(Color.RED);
				}

			}
		}
	}
}
