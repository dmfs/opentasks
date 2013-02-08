package org.dmfs.tasks;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import org.dmfs.provider.tasks.TaskContract;
import org.dmfs.provider.tasks.TaskContract.Tasks;
import org.dmfs.tasks.helpers.TaskItem;
import org.dmfs.tasks.helpers.TaskItemGroup;
import org.dmfs.tasks.helpers.TaskListBucketer;
import org.dmfs.tasks.model.adapters.TimeFieldAdapter;
import org.dmfs.tasks.utils.AsyncContentLoader;
import org.dmfs.tasks.utils.AsyncModelLoader;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
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
public class TaskListFragment extends Fragment
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

	private ExpandableListView expandLV;
	private Context appContext;
	private static final TimeFieldAdapter TFADAPTER = new TimeFieldAdapter(TaskContract.Tasks.DUE, TaskContract.Tasks.TZ, TaskContract.Tasks.IS_ALLDAY);
	
	private TaskItemGroup[] itemGroupArray;
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
		Time todayTime = new Time();
		todayTime.setToNow();
		TaskListBucketer bucketer = new TaskListBucketer(todayTime);
		// CursorLoader taskCursorLoader = new CursorLoader(appContext,
		// tasksURI,
		// new String[] { "_id", "title" }, null, null, null);
		Cursor tasksCursor = appContext.getContentResolver().query(
			TaskContract.Tasks.CONTENT_URI,
			new String[] { TaskContract.Tasks._ID, TaskContract.Tasks.TITLE, TaskContract.Tasks.LIST_COLOR, TaskContract.Tasks.DUE, TaskContract.Tasks.TZ,
				TaskContract.Tasks.IS_ALLDAY, TaskContract.Tasks.LIST_ID, TaskContract.Tasks.LIST_NAME }, null, null, null);
		Log.d(TAG, "No of tasks are :" + tasksCursor.getCount());

		int titleCol, idCol, colorCol, listIdCol, listNameCol;
		titleCol = tasksCursor.getColumnIndex(TaskContract.Tasks.TITLE);
		idCol = tasksCursor.getColumnIndex(TaskContract.Tasks._ID);
		colorCol = tasksCursor.getColumnIndex(TaskContract.Tasks.LIST_COLOR);
		listIdCol = tasksCursor.getColumnIndex(TaskContract.Tasks.LIST_ID);
		listNameCol = tasksCursor.getColumnIndex(TaskContract.Tasks.LIST_NAME);

		tasksCursor.moveToFirst();
		while (!tasksCursor.isAfterLast())
		{

			String taskTitle = tasksCursor.getString(titleCol);
			int taskId = tasksCursor.getInt(idCol);
			int taskColor = tasksCursor.getInt(colorCol);
			Time dueTime = TFADAPTER.get(tasksCursor);
			int listId = tasksCursor.getInt(listIdCol);
			String listName = tasksCursor.getString(listNameCol);
			bucketer.put(new TaskItem(taskId, taskColor, taskTitle, dueTime));

			tasksCursor.moveToNext();
		}
		
		tasksCursor.close();

		itemGroupArray = bucketer.getArray();
		

		setHasOptionsMenu(true);
	}

	private class TaskExpandListAdapter implements ExpandableListAdapter
	{
		TaskItemGroup[] taskGroupArray;
		private LayoutInflater mViewInflater;


		public TaskExpandListAdapter(Context context, TaskItemGroup[] g)
		{
			taskGroupArray = g;
			mViewInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}


		@Override
		public boolean areAllItemsEnabled()
		{
			return true;
		}


		@Override
		public Object getChild(int groupPosition, int childPosition)
		{
			return taskGroupArray[groupPosition].getChild(childPosition);

		}


		@Override
		public long getChildId(int groupPosition, int childPosition)
		{
			return childPosition;
		}


		@Override
		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
		{
			TaskItem t = taskGroupArray[groupPosition].getChild(childPosition);
			if (convertView == null)
			{
				convertView = mViewInflater.inflate(R.layout.task_list_element, null);
			}

			TextView tv = (TextView) convertView.findViewById(R.id.task_title);
			View cv = convertView.findViewById(R.id.colorbar);
			Log.d(TAG, "Task Title : " + t.getTaskTitle());
			tv.setText(t.getTaskTitle());
			cv.setBackgroundColor(t.getTaskColor());

			return convertView;
		}


		@Override
		public int getChildrenCount(int groupPosition)
		{
			return taskGroupArray[groupPosition].getSize();

		}


		@Override
		public long getCombinedChildId(long groupId, long childId)
		{
			return (groupId * 1000) + childId;
		}


		@Override
		public long getCombinedGroupId(long groupId)
		{
			return groupId;
		}


		@Override
		public Object getGroup(int groupPosition)
		{
			return taskGroupArray[groupPosition];
		}


		@Override
		public int getGroupCount()
		{
			return taskGroupArray.length;
		}


		@Override
		public long getGroupId(int groupPosition)
		{
			return groupPosition;
		}


		@Override
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
		{
			if (convertView == null)
			{
				convertView = mViewInflater.inflate(R.layout.task_list_element, null);
			}

			TextView tv = (TextView) convertView.findViewById(R.id.task_title);
			View cv = convertView.findViewById(R.id.colorbar);

			tv.setText(taskGroupArray[groupPosition].getName());
			cv.setBackgroundColor(taskGroupArray[groupPosition].getColor());
			return convertView;
		}


		@Override
		public boolean hasStableIds()
		{
			return true;
		}


		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition)
		{
			return true;
		}


		@Override
		public boolean isEmpty()
		{
			return false;
		}


		@Override
		public void onGroupCollapsed(int groupPosition)
		{

		}


		@Override
		public void onGroupExpanded(int groupPosition)
		{

		}


		@Override
		public void registerDataSetObserver(DataSetObserver observer)
		{

		}


		@Override
		public void unregisterDataSetObserver(DataSetObserver observer)
		{

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
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View rootView = inflater.inflate(R.layout.fragment_expandable_task_list, container, false);
		expandLV = (ExpandableListView) rootView.findViewById(R.id.expandable_tasks_list);
		expandLV.setAdapter(new TaskExpandListAdapter(appContext, itemGroupArray));
		expandLV.setOnChildClickListener(new TaskItemClickListener());
		return rootView;
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

	private class TaskItemClickListener implements OnChildClickListener
	{

		@Override
		public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id)
		{
			ExpandableListAdapter la = parent.getExpandableListAdapter();
			TaskItem selectedItem = (TaskItem) la.getChild(groupPosition, childPosition);
			int selectTaskId = selectedItem.getTaskId();
			Toast.makeText(appContext, "Selected ID is : " + selectTaskId, Toast.LENGTH_SHORT).show();
			// Notify the active callbacks interface (the activity, if the
			// fragment is attached to one) that an item has been selected.
			Uri taskUri = ContentUris.withAppendedId(Tasks.CONTENT_URI, selectTaskId);
			mCallbacks.onItemSelected(taskUri);
			return true;
		}

	};


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
		expandLV.setChoiceMode(activateOnItemClick ? ListView.CHOICE_MODE_SINGLE : ListView.CHOICE_MODE_NONE);
	}


	private void setActivatedPosition(int position)
	{
		if (position == ListView.INVALID_POSITION)
		{
			expandLV.setItemChecked(mActivatedPosition, false);
		}
		else
		{
			expandLV.setItemChecked(position, true);
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
			}
			else
			{
				tv.setText(dateFormatter.format(new Date(dueDate.toMillis(false))));
			}

			if (dueDate.before(today))
			{
				Log.d(TAG, "Before Today");
				tv.setTextColor(Color.RED);
			}
		}
	}
}
