/*
 * Copyright (C) 2013 Marten Gajda <marten@dmfs.org>
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

import org.dmfs.android.retentionmagic.SupportFragment;
import org.dmfs.android.retentionmagic.annotations.Parameter;
import org.dmfs.android.retentionmagic.annotations.Retain;
import org.dmfs.provider.tasks.TaskContract;
import org.dmfs.provider.tasks.TaskContract.Instances;
import org.dmfs.provider.tasks.TaskContract.Tasks;
import org.dmfs.tasks.groupings.ByDueDate;
import org.dmfs.tasks.groupings.ByList;
import org.dmfs.tasks.groupings.filters.AbstractFilter;
import org.dmfs.tasks.groupings.filters.ConstantFilter;
import org.dmfs.tasks.model.Model;
import org.dmfs.tasks.utils.AsyncModelLoader;
import org.dmfs.tasks.utils.ExpandableGroupDescriptor;
import org.dmfs.tasks.utils.ExpandableGroupDescriptorAdapter;
import org.dmfs.tasks.utils.FlingDetector;
import org.dmfs.tasks.utils.FlingDetector.OnFlingListener;
import org.dmfs.tasks.utils.OnChildLoadedListener;
import org.dmfs.tasks.utils.OnModelLoadedListener;
import org.dmfs.tasks.utils.RetainExpandableListView;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
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
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


/**
 * A list fragment representing a list of Tasks. This fragment also supports tablet devices by allowing list items to be given an 'activated' state upon
 * selection. This helps indicate which item is currently being viewed in a {@link ViewTaskFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks} interface
 * 
 * @author Tobias Reinsch <tobias@dmfs.org>
 */
public class TaskListFragment extends SupportFragment implements LoaderManager.LoaderCallbacks<Cursor>, OnChildLoadedListener, OnModelLoadedListener,
	OnFlingListener
{

	private static final String TAG = "org.dmfs.tasks.TaskListFragment";

	private final static String ARG_INSTANCE_ID = "instance_id";
	private final static String ARG_TWO_PANE_LAYOUT = "two_pane_layout";

	/**
	 * A filter to hide completed tasks.
	 */
	private final static AbstractFilter COMPLETED_FILTER = new ConstantFilter(Tasks.IS_CLOSED + "=0");

	/**
	 * The group descriptor to use. At present this can be either {@link ByDueDate#GROUP_DESCRIPTOR}, {@link ByCompleted#GROUP_DESCRIPTOR} or
	 * {@link ByList#GROUP_DESCRIPTOR}.
	 */
	private ExpandableGroupDescriptor mGroupDescriptor;

	/**
	 * The fragment's current callback object, which is notified of list item clicks.
	 */
	private Callbacks mCallbacks;

	@Retain(permanent = true, instanceNSField = "mInstancePosition")
	private int mActivatedPositionGroup = ExpandableListView.INVALID_POSITION;
	@Retain(permanent = true, instanceNSField = "mInstancePosition")
	private int mActivatedPositionChild = ExpandableListView.INVALID_POSITION;

	private RetainExpandableListView mExpandableListView;
	private Context mAppContext;
	private ExpandableGroupDescriptorAdapter mAdapter;
	private Handler mHandler;
	@Retain(permanent = true, instanceNSField = "mInstancePosition")
	private long[] mSavedExpandedGroups = null;
	@Retain(permanent = true, instanceNSField = "mInstancePosition")
	private boolean mSavedCompletedFilter;

	@Parameter(key = ARG_INSTANCE_ID)
	private int mInstancePosition;

	@Parameter(key = ARG_TWO_PANE_LAYOUT)
	private boolean mTwoPaneLayout;

	private Loader<Cursor> mCursorLoader;
	private String mAuthority;

	private final OnChildClickListener mTaskItemClickListener = new OnChildClickListener()
	{

		@Override
		public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id)
		{
			selectChildView(parent, groupPosition, childPosition, true);
			if (mExpandableListView.getChoiceMode() == ExpandableListView.CHOICE_MODE_SINGLE)
			{
				mActivatedPositionGroup = groupPosition;
				mActivatedPositionChild = childPosition;
			}
			/*
			 * In contrast to a ListView an ExpandableListView does not set the activated item on it's own. So we have to do that here.
			 */
			setActivatedItem(groupPosition, childPosition);
			return true;
		}

	};

	private final OnGroupCollapseListener mTaskListCollapseListener = new OnGroupCollapseListener()
	{

		@Override
		public void onGroupCollapse(int groupPosition)
		{
			if (groupPosition == mActivatedPositionGroup)
			{
				mActivatedPositionChild = ExpandableListView.INVALID_POSITION;
				mActivatedPositionGroup = ExpandableListView.INVALID_POSITION;
			}

		}
	};

	/**
	 * A callback interface that all activities containing this fragment must implement. This mechanism allows activities to be notified of item selections.
	 */
	public interface Callbacks
	{
		/**
		 * Callback for when an item has been selected.
		 */
		public void onItemSelected(Uri taskUri, boolean forceReload);


		public ExpandableGroupDescriptor getGroupDescriptor(int position);


		public void onAddNewTask();
	}


	public static TaskListFragment newInstance(int instancePosition, boolean twoPaneLayout)
	{
		TaskListFragment result = new TaskListFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_INSTANCE_ID, instancePosition);
		args.putBoolean(ARG_TWO_PANE_LAYOUT, twoPaneLayout);
		result.setArguments(args);
		return result;
	}


	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the fragment (e.g. upon screen orientation changes).
	 */
	public TaskListFragment()
	{
	}


	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		mAuthority = getString(R.string.org_dmfs_tasks_authority);

		mAppContext = activity.getBaseContext();

		// Activities containing this fragment must implement its callbacks.
		if (!(activity instanceof Callbacks))
		{
			throw new IllegalStateException("Activity must implement fragment's callbacks.");
		}

		mCallbacks = (Callbacks) activity;

		// load accounts early
		new AsyncModelLoader(activity, this).execute(TaskContract.LOCAL_ACCOUNT);
	}


	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		mHandler = new Handler();
		setHasOptionsMenu(true);
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View rootView = inflater.inflate(R.layout.fragment_expandable_task_list, container, false);
		mExpandableListView = (RetainExpandableListView) rootView.findViewById(android.R.id.list);

		if (mGroupDescriptor == null)
		{
			loadGroupDescriptor();
		}

		// setup the views
		this.updateView();

		// expand lists
		if (mSavedExpandedGroups != null)
		{
			mExpandableListView.expandGroups(mSavedExpandedGroups);
		}

		FlingDetector swiper = new FlingDetector(mExpandableListView, mGroupDescriptor.getElementViewDescriptor().getFlingContentViewId(), getActivity()
			.getApplicationContext());
		swiper.setOnFlingListener(this);

		if (mTwoPaneLayout)
		{
			setListViewScrollbarPositionLeft(true);
			setActivateOnItemClick(true);
		}

		return rootView;
	}


	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
	}


	@Override
	public void onPause()
	{
		mSavedExpandedGroups = mExpandableListView.getExpandedGroups();
		super.onPause();
	}


	@Override
	public void onDetach()
	{
		super.onDetach();

	}


	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		mSavedExpandedGroups = mExpandableListView.getExpandedGroups();
		super.onSaveInstanceState(outState);
	}


	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		// create menu
		inflater.inflate(R.menu.task_list_fragment_menu, menu);

		// restore menu state
		MenuItem item = menu.findItem(R.id.menu_show_completed);
		if (item != null)
		{
			item.setChecked(mSavedCompletedFilter);

			if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
			{
				if (mSavedCompletedFilter)
				{
					item.setTitle(R.string.menu_hide_completed);
				}
				else
				{
					item.setTitle(R.string.menu_show_completed);
				}
			}
		}
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		int itemId = item.getItemId();
		if (itemId == R.id.menu_add_task)
		{
			mCallbacks.onAddNewTask();
			return true;
		}
		else if (itemId == R.id.menu_show_completed)
		{

			mSavedCompletedFilter = !mSavedCompletedFilter;
			item.setChecked(mSavedCompletedFilter);
			mAdapter.setChildCursorFilter(mSavedCompletedFilter ? null : COMPLETED_FILTER);

			// reload the child cursors only
			for (int i = 0; i < mAdapter.getGroupCount(); ++i)
			{
				mAdapter.reloadGroup(i);
			}
			return true;
		}
		else if (itemId == R.id.menu_sync_now)
		{
			doSyncNow();
			return true;
		}
		else
		{
			return super.onOptionsItemSelected(item);
		}
	}


	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1)
	{

		if (mGroupDescriptor != null)
		{
			mCursorLoader = mGroupDescriptor.getGroupCursorLoader(mAppContext);

		}
		return mCursorLoader;

	}


	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)
	{
		/*
		 * int scrollx = expandLV.getFirstVisiblePosition(); View itemView = expandLV.getChildAt(0); int scrolly = itemView == null ? 0 : itemView.getTop();
		 * Log.v(TAG, "scrollY " + scrollx + "  " + scrolly);
		 */
		Log.v(TAG, "change cursor");
		if (mSavedExpandedGroups == null)
		{
			mSavedExpandedGroups = mExpandableListView.getExpandedGroups();
		}

		mAdapter.setGroupCursor(cursor);
		/*
		 * expandLV.setSelectionFromTop(scrollx, 0); int scrollx2 = expandLV.getFirstVisiblePosition(); View itemView2 = expandLV.getChildAt(0); int scrolly2 =
		 * itemView == null ? 0 : itemView2.getTop(); Log.v(TAG, "scrollY " + scrollx2 + "  " + scrolly2);
		 */
		if (mSavedExpandedGroups != null)
		{
			mExpandableListView.expandGroups(mSavedExpandedGroups);
			mSavedExpandedGroups = null;
		}

		mHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				mAdapter.reloadLoadedGroups();
			}
		});
	}


	@Override
	public void onLoaderReset(Loader<Cursor> loader)
	{
		mAdapter.changeCursor(null);
	}


	@Override
	public void onChildLoaded(int pos)
	{
		if (mActivatedPositionChild != ExpandableListView.INVALID_POSITION)
		{
			if (pos == mActivatedPositionGroup && mActivatedPositionChild != ExpandableListView.INVALID_POSITION)
			{
				Log.d(TAG, "Restoring Child Postion : " + mActivatedPositionChild);
				Log.d(TAG, "Restoring Group Position : " + mActivatedPositionGroup);
				mHandler.post(setOpenHandler);

			}
		}

	}


	@Override
	public void onModelLoaded(Model model)
	{
		// nothing to do, we've just loaded the default model to speed up loading the detail view and the editor view.
	}


	private void selectChildView(ExpandableListView expandLV, int groupPosition, int childPosition, boolean force)
	{
		if (groupPosition < mAdapter.getGroupCount() && childPosition < mAdapter.getChildrenCount(groupPosition))
		{
			// a task instance element has been clicked, get it's instance id and notify the activity
			ExpandableListAdapter listAdapter = expandLV.getExpandableListAdapter();
			Cursor cursor = (Cursor) listAdapter.getChild(groupPosition, childPosition);

			if (cursor == null)
			{
				return;
			}
			// TODO: for now we get the id of the task, not the instance, once we support recurrence we'll have to change that
			Long selectTaskId = cursor.getLong(cursor.getColumnIndex(Instances.TASK_ID));

			if (selectTaskId != null)
			{
				// Notify the active callbacks interface (the activity, if the fragment is attached to one) that an item has been selected.

				// TODO: use the instance URI one we support recurrence
				Uri taskUri = ContentUris.withAppendedId(Tasks.getContentUri(mAuthority), selectTaskId);

				mCallbacks.onItemSelected(taskUri, force);
			}
		}
	}


	/**
	 * Updates the view after the group descriptor was changed
	 * 
	 */
	public void updateView()
	{
		mAdapter = new ExpandableGroupDescriptorAdapter(getActivity(), getLoaderManager(), mGroupDescriptor);
		mExpandableListView.setAdapter(mAdapter);
		mExpandableListView.setOnChildClickListener((android.widget.ExpandableListView.OnChildClickListener) mTaskItemClickListener);
		mExpandableListView.setOnGroupCollapseListener((android.widget.ExpandableListView.OnGroupCollapseListener) mTaskListCollapseListener);
		mAdapter.setOnChildLoadedListener(this);
		mAdapter.setChildCursorFilter(COMPLETED_FILTER);
		restoreFilterState();
		getLoaderManager().restartLoader(-1, null, this);
	}


	public void restoreFilterState()
	{
		if (mSavedCompletedFilter)
		{
			mAdapter.setChildCursorFilter(mSavedCompletedFilter ? null : COMPLETED_FILTER);
			// reload the child cursors only
			for (int i = 0; i < mAdapter.getGroupCount(); ++i)
			{
				mAdapter.reloadGroup(i);
			}
		}

	}


	/**
	 * Trigger a synchronization for all accounts.
	 */
	private void doSyncNow()
	{
		AccountManager accountManager = AccountManager.get(mAppContext);
		Account[] accounts = accountManager.getAccounts();
		for (Account account : accounts)
		{
			// TODO: do we need a new bundle for each account or can we reuse it?
			Bundle extras = new Bundle();
			extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
			ContentResolver.requestSync(account, mAuthority, extras);
		}
	}


	/**
	 * Remove the task with the given {@link Uri} and title, asking for confirmation first.
	 * 
	 * @param taskUri
	 *            The {@link Uri} of the atsk to remove.
	 * @param taskTitle
	 *            the title of the task to remove.
	 * @return
	 */
	private void removeTask(final Uri taskUri, final String taskTitle)
	{
		new AlertDialog.Builder(getActivity()).setTitle(R.string.confirm_delete_title).setCancelable(true)
			.setNegativeButton(android.R.string.cancel, new OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					// nothing to do here
				}
			}).setPositiveButton(android.R.string.ok, new OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					// TODO: remove the task in a background task
					mAppContext.getContentResolver().delete(taskUri, null, null);
					Toast.makeText(mAppContext, getString(R.string.toast_task_deleted, taskTitle), Toast.LENGTH_SHORT).show();
				}
			}).setMessage(getString(R.string.confirm_delete_message_with_title, taskTitle)).create().show();
	}


	/**
	 * Opens the task editor for the selected Task.
	 * 
	 * @param taskUri
	 *            The {@link Uri} of the task.
	 * @param taskTitle
	 *            The name/title of the task.
	 */
	private void openTaskEditor(final Uri taskUri, final String accountType)
	{
		Intent editTaskIntent = new Intent(Intent.ACTION_EDIT);
		editTaskIntent.setData(taskUri);
		editTaskIntent.putExtra(EditTaskActivity.EXTRA_DATA_ACCOUNT_TYPE, accountType);
		startActivity(editTaskIntent);
	}


	@Override
	public int canFling(ListView v, int pos)
	{
		long packedPos = mExpandableListView.getExpandableListPosition(pos);
		if (packedPos != ExpandableListView.PACKED_POSITION_VALUE_NULL
			&& ExpandableListView.getPackedPositionType(packedPos) == ExpandableListView.PACKED_POSITION_TYPE_CHILD)
		{
			return FlingDetector.RIGHT_FLING | FlingDetector.LEFT_FLING;
		}
		else
		{
			return 0;
		}
	}


	@Override
	public void onFlingStart(ListView listView, View listElement, int position, int direction)
	{

		// control the visibility of the views that reveal behind a flinging element regarding the fling direction
		int rightFlingViewId = mGroupDescriptor.getElementViewDescriptor().getFlingRevealRightViewId();
		int leftFlingViewId = mGroupDescriptor.getElementViewDescriptor().getFlingRevealLeftViewId();
		TextView rightFlingView = null;
		TextView leftFlingView = null;

		if (rightFlingViewId != -1)
		{
			rightFlingView = (TextView) listElement.findViewById(rightFlingViewId);
		}
		if (leftFlingViewId != -1)
		{
			leftFlingView = (TextView) listElement.findViewById(leftFlingViewId);
		}

		Resources resources = getActivity().getResources();

		// change title and icon regarding the task status
		long packedPos = mExpandableListView.getExpandableListPosition(position);
		if (ExpandableListView.getPackedPositionType(packedPos) == ExpandableListView.PACKED_POSITION_TYPE_CHILD)
		{
			ExpandableListAdapter listAdapter = mExpandableListView.getExpandableListAdapter();
			Cursor cursor = (Cursor) listAdapter.getChild(ExpandableListView.getPackedPositionGroup(packedPos),
				ExpandableListView.getPackedPositionChild(packedPos));

			if (cursor != null)
			{
				int taskStatus = cursor.getInt(cursor.getColumnIndex(Instances.STATUS));
				if (leftFlingView != null && rightFlingView != null)
				{
					if (taskStatus == Instances.STATUS_COMPLETED)
					{
						leftFlingView.setText(R.string.fling_task_delete);
						leftFlingView.setCompoundDrawablesWithIntrinsicBounds(resources.getDrawable(R.drawable.content_discard), null, null, null);
						rightFlingView.setText(R.string.fling_task_uncomplete);
						rightFlingView.setCompoundDrawablesWithIntrinsicBounds(null, null, resources.getDrawable(R.drawable.content_remove), null);
					}
					else
					{
						leftFlingView.setText(R.string.fling_task_complete);
						leftFlingView.setCompoundDrawablesWithIntrinsicBounds(resources.getDrawable(R.drawable.ic_action_complete), null, null, null);
						rightFlingView.setText(R.string.fling_task_edit);
						rightFlingView.setCompoundDrawablesWithIntrinsicBounds(null, null, resources.getDrawable(R.drawable.content_edit), null);
					}
				}
			}
		}

		if (rightFlingView != null)
		{
			rightFlingView.setVisibility(direction != FlingDetector.LEFT_FLING ? View.GONE : View.VISIBLE);
		}
		if (leftFlingView != null)
		{
			leftFlingView.setVisibility(direction != FlingDetector.RIGHT_FLING ? View.GONE : View.VISIBLE);
		}

	}


	@Override
	public boolean onFlingEnd(ListView v, View listElement, int pos, int direction)
	{
		long packedPos = mExpandableListView.getExpandableListPosition(pos);
		if (ExpandableListView.getPackedPositionType(packedPos) == ExpandableListView.PACKED_POSITION_TYPE_CHILD)
		{
			ExpandableListAdapter listAdapter = mExpandableListView.getExpandableListAdapter();
			Cursor cursor = (Cursor) listAdapter.getChild(ExpandableListView.getPackedPositionGroup(packedPos),
				ExpandableListView.getPackedPositionChild(packedPos));

			if (cursor != null)
			{
				// TODO: for now we get the id of the task, not the instance, once we support recurrence we'll have to change that
				Long taskId = cursor.getLong(cursor.getColumnIndex(Instances.TASK_ID));

				if (taskId != null)
				{
					boolean closed = cursor.getLong(cursor.getColumnIndex(Instances.IS_CLOSED)) > 0;
					String title = cursor.getString(cursor.getColumnIndex(Instances.TITLE));
					// TODO: use the instance URI once we support recurrence
					Uri taskUri = ContentUris.withAppendedId(Tasks.getContentUri(mAuthority), taskId);

					if (direction == FlingDetector.RIGHT_FLING)
					{
						if (closed)
						{
							removeTask(taskUri, title);
							// we do not know for sure if the task has been removed since the user is asked for confirmation first, so return false

							return false;

						}
						else
						{
							return setCompleteTask(taskUri, title, true);
						}
					}
					else if (direction == FlingDetector.LEFT_FLING)
					{
						if (closed)
						{
							return setCompleteTask(taskUri, title, false);
						}
						else
						{
							openTaskEditor(taskUri, cursor.getString(cursor.getColumnIndex(Instances.ACCOUNT_TYPE)));
							return false;
						}
					}
				}
			}
		}

		return false;
	}


	@Override
	public void onFlingCancel(int direction)
	{
		// TODO Auto-generated method stub

	}


	public void loadGroupDescriptor()
	{
		if (getActivity() != null)
		{
			TaskListActivity activity = (TaskListActivity) getActivity();
			if (activity != null)
			{
				mGroupDescriptor = activity.getGroupDescriptor(mInstancePosition);
			}
		}
	}


	public int getOpenChildPosition()
	{
		return mActivatedPositionChild;
	}


	public int getOpenGroupPosition()
	{
		return mActivatedPositionGroup;
	}


	/**
	 * Turns on activate-on-click mode. When this mode is on, list items will be given the 'activated' state when touched.
	 * <p>
	 * Note: this does not work 100% with {@link ExpandableListView}, it doesn't check touched items automatically.
	 * </p>
	 * 
	 * @param activateOnItemClick
	 *            Whether to enable single choice mode or not.
	 */
	public void setActivateOnItemClick(boolean activateOnItemClick)
	{
		mExpandableListView.setChoiceMode(activateOnItemClick ? ListView.CHOICE_MODE_SINGLE : ListView.CHOICE_MODE_NONE);
	}


	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void setListViewScrollbarPositionLeft(boolean left)
	{
		if (android.os.Build.VERSION.SDK_INT >= 11)
		{
			if (left)
			{
				mExpandableListView.setVerticalScrollbarPosition(View.SCROLLBAR_POSITION_LEFT);
				// expandLV.setScrollBarStyle(style);
			}
			else
			{
				mExpandableListView.setVerticalScrollbarPosition(View.SCROLLBAR_POSITION_RIGHT);
			}
		}
	}


	public void setExpandableGroupDescriptor(ExpandableGroupDescriptor groupDescriptor)
	{
		mGroupDescriptor = groupDescriptor;
	}


	/**
	 * Mark the given task as completed.
	 * 
	 * @param taskUri
	 *            The {@link Uri} of the task.
	 * @param taskTitle
	 *            The name/title of the task.
	 * @param completedValue
	 *            The value to be set for the completed status.
	 * @return <code>true</code> if the operation was successful, <code>false</code> otherwise.
	 */
	private boolean setCompleteTask(Uri taskUri, String taskTitle, boolean completedValue)
	{
		ContentValues values = new ContentValues();
		values.put(Tasks.STATUS, completedValue ? Tasks.STATUS_COMPLETED : Tasks.STATUS_IN_PROCESS);
		if (!completedValue)
		{
			values.put(Tasks.PERCENT_COMPLETE, 50);
		}

		boolean completed = mAppContext.getContentResolver().update(taskUri, values, null, null) != 0;
		if (completed)
		{
			if (completedValue)
			{
				Toast.makeText(mAppContext, getString(R.string.toast_task_completed, taskTitle), Toast.LENGTH_SHORT).show();
			}
			else
			{
				Toast.makeText(mAppContext, getString(R.string.toast_task_uncompleted, taskTitle), Toast.LENGTH_SHORT).show();
			}
		}
		return completed;
	}


	public void setOpenChildPosition(int openChildPosition)
	{
		mActivatedPositionChild = openChildPosition;

	}


	public void setOpenGroupPosition(int openGroupPosition)
	{
		mActivatedPositionGroup = openGroupPosition;

	}


	public void notifyDataSetChanged(boolean expandFirst)
	{
		// mExpandableListView.retainExpadedGroups(true, expandFirst, false);
		// LoaderManager loaderManager = getLoaderManager();
		// if (loaderManager.getLoader(-1) != null)
		// {
		// if (!loaderManager.hasRunningLoaders())
		// {
		getLoaderManager().restartLoader(-1, null, this);
		// }
		// }
		// else
		// {
		// loaderManager.initLoader(-1, null, this);
		// }

		// if (expandFirst)
		// {
		// long firstId = mExpandableListView.getExpandableListAdapter().getGroupId(0);
		// if (!mExpandableListView.isGroupExpanded(0))
		// {
		// mSavedExpandedGroups = new long[] { firstId };
		// }

		// }

	}

	Runnable setOpenHandler = new Runnable()
	{
		@Override
		public void run()
		{
			selectChildView(mExpandableListView, mActivatedPositionGroup, mActivatedPositionChild, false);
			mExpandableListView.expandGroups(mSavedExpandedGroups);
			setActivatedItem(mActivatedPositionGroup, mActivatedPositionChild);
		}
	};


	public void setActivatedItem(int groupPosition, int childPosition)
	{
		if (groupPosition != ExpandableListView.INVALID_POSITION && groupPosition < mAdapter.getGroupCount()
			&& childPosition != ExpandableListView.INVALID_POSITION && childPosition < mAdapter.getChildrenCount(groupPosition))
		{
			try
			{
				mExpandableListView.setItemChecked(
					mExpandableListView.getFlatListPosition(ExpandableListView.getPackedPositionForChild(groupPosition, childPosition)), true);
			}
			catch (NullPointerException e)
			{
				// for now we just catch the NPE until we've found the reason
				// just catching it won't hurt, it's just that the list selection won't be updated properly

				// FIXME: find the actual cause and fix it
			}
		}
	}

}
