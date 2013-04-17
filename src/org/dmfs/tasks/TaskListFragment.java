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

import java.util.Arrays;

import org.dmfs.provider.tasks.TaskContract;
import org.dmfs.provider.tasks.TaskContract.Instances;
import org.dmfs.provider.tasks.TaskContract.Tasks;
import org.dmfs.tasks.groupings.ByCompleted;
import org.dmfs.tasks.groupings.ByDueDate;
import org.dmfs.tasks.groupings.ByList;
import org.dmfs.tasks.groupings.filters.AbstractFilter;
import org.dmfs.tasks.groupings.filters.ConstantFilter;
import org.dmfs.tasks.model.Model;
import org.dmfs.tasks.utils.AsyncModelLoader;
import org.dmfs.tasks.utils.ExpandableGroupDescriptor;
import org.dmfs.tasks.utils.ExpandableGroupDescriptorAdapter;
import org.dmfs.tasks.utils.OnChildLoadedListener;
import org.dmfs.tasks.utils.OnModelLoadedListener;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ListView;


/**
 * A list fragment representing a list of Tasks. This fragment also supports tablet devices by allowing list items to be given an 'activated' state upon
 * selection. This helps indicate which item is currently being viewed in a {@link ViewTaskFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks} interface.
 */
@SuppressLint("NewApi")
public class TaskListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, OnChildLoadedListener, OnModelLoadedListener
{

	private static final String TAG = "org.dmfs.tasks.TaskListFragment";

	private static final String STATE_EXPANDED_GROUPS = "expanded_groups";

	private static final String STATE_ACTIVATED_POSITION_GROUP = "activated_group_position";
	private static final String STATE_ACTIVATED_POSITION_CHILD = "activated_child_position";

	/**
	 * A filter to hide completed tasks.
	 */
	private final static AbstractFilter COMPLETED_FILTER = new ConstantFilter(Tasks.IS_CLOSED + "=0");

	/**
	 * The group descriptor to use. At present this can be either {@link ByDueDate#GROUP_DESCRIPTOR}, {@link ByCompleted#GROUP_DESCRIPTOR} or
	 * {@link ByList#GROUP_DESCRIPTOR}.
	 */
	private final static ExpandableGroupDescriptor CURRENT_GROUP_DESCRIPTOR = ByList.GROUP_DESCRIPTOR;

	/**
	 * The fragment's current callback object, which is notified of list item clicks.
	 */
	private Callbacks mCallbacks;
	private int mActivatedPositionGroup = ExpandableListView.INVALID_POSITION;
	private int mActivatedPositionChild = ExpandableListView.INVALID_POSITION;
	long[] expandedIds = new long[0];
	private ExpandableListView expandLV;
	private Context appContext;
	private ExpandableGroupDescriptorAdapter mAdapter;
	private Handler mHandler;

	private long[] mSavedExpandedGroups = null;

	/**
	 * A callback interface that all activities containing this fragment must implement. This mechanism allows activities to be notified of item selections.
	 */
	public interface Callbacks
	{
		/**
		 * Callback for when an item has been selected.
		 */
		public void onItemSelected(Uri taskUri, boolean forceReload);


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
		mHandler = new Handler();
		setHasOptionsMenu(true);
	}


	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		/*
		 * // Restore the previously serialized activated item position. if (savedInstanceState != null &&
		 * savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) { setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION)); }
		 */
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View rootView = inflater.inflate(R.layout.fragment_expandable_task_list, container, false);
		expandLV = (ExpandableListView) rootView.findViewById(android.R.id.list);
		mAdapter = new ExpandableGroupDescriptorAdapter(getActivity(), getLoaderManager(), CURRENT_GROUP_DESCRIPTOR);
		expandLV.setAdapter(mAdapter);
		expandLV.setOnChildClickListener((android.widget.ExpandableListView.OnChildClickListener) mTaskItemClickListener);
		expandLV.setOnGroupCollapseListener((android.widget.ExpandableListView.OnGroupCollapseListener) mTaskListCollapseListener);
		mAdapter.setOnChildLoadedListener(this);
		mAdapter.setChildCursorFilter(COMPLETED_FILTER);
		expandLV.setOnScrollListener(new OnScrollListener()
		{

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState)
			{

			}


			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
			{
				// TODO Auto-generated method stub

			}
		});

		getLoaderManager().restartLoader(-1, null, this);

		if (savedInstanceState != null)
		{
			Log.d(TAG, "savedInstance state is not null");
			// store expanded groups array for later, when the groups have been loaded
			mSavedExpandedGroups = savedInstanceState.getLongArray(STATE_EXPANDED_GROUPS);
			mActivatedPositionGroup = savedInstanceState.getInt(STATE_ACTIVATED_POSITION_GROUP);
			mActivatedPositionChild = savedInstanceState.getInt(STATE_ACTIVATED_POSITION_CHILD);
		}
		else
		{
			Log.d(TAG, "savedInstancestate is null!!");
		}
		return rootView;
	}


	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);

		appContext = activity.getBaseContext();

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
	public void onDetach()
	{
		super.onDetach();

	}

	View selectedView = null;

	private final OnChildClickListener mTaskItemClickListener = new OnChildClickListener()
	{

		@Override
		public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id)
		{
			selectChildView(parent, groupPosition, childPosition, true);
			if (expandLV.getChoiceMode() == ExpandableListView.CHOICE_MODE_SINGLE)
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


	private void selectChildView(ExpandableListView expandLV, int groupPosition, int childPosition, boolean force)
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
			Uri taskUri = ContentUris.withAppendedId(Tasks.CONTENT_URI, selectTaskId);

			mCallbacks.onItemSelected(taskUri, force);
		}
	}


	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putLongArray(STATE_EXPANDED_GROUPS, getExpandedGroups());
		outState.putInt(STATE_ACTIVATED_POSITION_GROUP, mActivatedPositionGroup);
		outState.putInt(STATE_ACTIVATED_POSITION_CHILD, mActivatedPositionChild);
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
		expandLV.setChoiceMode(activateOnItemClick ? ListView.CHOICE_MODE_SINGLE : ListView.CHOICE_MODE_NONE);

	}


	public void setListViewScrollbarPositionLeft(boolean left)
	{
		if (android.os.Build.VERSION.SDK_INT >= 11)
		{
			if (left)
			{
				expandLV.setVerticalScrollbarPosition(View.SCROLLBAR_POSITION_LEFT);
				// expandLV.setScrollBarStyle(style);
			}
			else
			{
				expandLV.setVerticalScrollbarPosition(View.SCROLLBAR_POSITION_RIGHT);
			}
		}
	}


	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		inflater.inflate(R.menu.task_list_fragment_menu, menu);
		// TODO: set menu_show_completed
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
			item.setChecked(!item.isChecked());
			mAdapter.setChildCursorFilter(item.isChecked() ? null : COMPLETED_FILTER);
			// reload the child cursors only

			// TODO: reload only groups that are visible
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
		return CURRENT_GROUP_DESCRIPTOR.getGroupCursorLoader(appContext);
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
			mSavedExpandedGroups = getExpandedGroups();
		}

		mAdapter.changeCursor(cursor);
		/*
		 * expandLV.setSelectionFromTop(scrollx, 0); int scrollx2 = expandLV.getFirstVisiblePosition(); View itemView2 = expandLV.getChildAt(0); int scrolly2 =
		 * itemView == null ? 0 : itemView2.getTop(); Log.v(TAG, "scrollY " + scrollx2 + "  " + scrolly2);
		 */
		if (mSavedExpandedGroups != null)
		{
			expandedIds = mSavedExpandedGroups;
			setExpandedGroups();
			mSavedExpandedGroups = null;
		}
	}


	@Override
	public void onLoaderReset(Loader<Cursor> loader)
	{
		mAdapter.changeCursor(null);
	}


	public long[] getExpandedGroups()
	{
		ExpandableListAdapter adapter = expandLV.getExpandableListAdapter();
		int count = adapter.getGroupCount();

		long[] result = new long[count];

		int idx = 0;
		for (int i = 0; i < count; ++i)
		{
			if (expandLV.isGroupExpanded(i))
			{
				result[idx] = adapter.getGroupId(i);
				++idx;
			}
		}

		// Arrays.copyOf not available in API level 8 and below.
		/*
		 * if (android.os.Build.VERSION.SDK_INT > 8) { return Arrays.copyOf(result, idx); } else
		 */
		{
			long[] returnArray = new long[idx];
			System.arraycopy(result, 0, returnArray, 0, idx);
			return returnArray;
		}
	}


	public void setExpandedGroups()
	{
		ExpandableListAdapter adapter = expandLV.getExpandableListAdapter();
		Arrays.sort(expandedIds);
		Log.d(TAG, "NOW EXPANDING : " + adapter.getGroupCount());
		int count = adapter.getGroupCount();
		for (int i = 0; i < count; ++i)
		{
			if (Arrays.binarySearch(expandedIds, adapter.getGroupId(i)) >= 0)
			{
				Log.d(TAG, "NOW EXPANDING GROUPS: " + i);
				expandLV.expandGroup(i);
			}
		}
	}


	@Override
	public void onChildLoaded(int pos)
	{
		if (mActivatedPositionGroup != ExpandableListView.INVALID_POSITION)
		{
			if (pos == mActivatedPositionGroup && mActivatedPositionChild != ExpandableListView.INVALID_POSITION)
			{
				Log.d(TAG, "Restoring Child Postion : " + mActivatedPositionChild);
				Log.d(TAG, "Restoring Group Position : " + mActivatedPositionGroup);
				mHandler.post(setOpenHandler);

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


	public void setOpenChildPosition(int openChildPosition)
	{
		mActivatedPositionChild = openChildPosition;

	}


	public void setOpenGroupPosition(int openGroupPosition)
	{
		mActivatedPositionGroup = openGroupPosition;

	}

	Runnable setOpenHandler = new Runnable()
	{
		@Override
		public void run()
		{
			selectChildView(expandLV, mActivatedPositionGroup, mActivatedPositionChild, false);
			setExpandedGroups();
			setActivatedItem(mActivatedPositionGroup, mActivatedPositionChild);
		}
	};


	public void setExpandedGroupsIds(long[] ids)
	{
		Log.d(TAG, "SET EXPAND :" + ids);
		expandedIds = ids;

	}


	public void setActivatedItem(int groupPosition, int childPosition)
	{
		if (groupPosition != ExpandableListView.INVALID_POSITION && groupPosition < mAdapter.getGroupCount()
			&& childPosition != ExpandableListView.INVALID_POSITION && childPosition < mAdapter.getChildrenCount(groupPosition))
		{
			expandLV.setItemChecked(expandLV.getFlatListPosition(ExpandableListView.getPackedPositionForChild(groupPosition, childPosition)), true);
		}
	}


	/**
	 * Trigger a synchronization for all accounts.
	 */
	private void doSyncNow()
	{
		AccountManager accountManager = AccountManager.get(appContext);
		Account[] accounts = accountManager.getAccounts();
		for (Account account : accounts)
		{
			// TODO: do we need a new bundle for each account or can we reuse it?
			Bundle extras = new Bundle();
			extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
			ContentResolver.requestSync(account, TaskContract.AUTHORITY, extras);
		}
	}


	@Override
	public void onModelLoaded(Model model)
	{
		// nothing to do, we've just loaded the default model to speed up loading the detail view and the editor view.
	}


	/**
	 * Mark a task as completed and show a nice animation.
	 * 
	 * @param taskUri
	 *            The {@link Uri} of the task to mark as completed.
	 * @param v
	 *            The {@link View} to animate.
	 */
	private void animateCompleteTask(final Uri taskUri, final View v)
	{
		if (android.os.Build.VERSION.SDK_INT >= 12)
		{
			// Use animations for SDK level 12+ only
			v.animate().translationX(((View) v.getParent()).getWidth()).setListener(new AnimatorListener()
			{

				@Override
				public void onAnimationStart(Animator animation)
				{
				}


				@Override
				public void onAnimationRepeat(Animator animation)
				{
				}


				@Override
				public void onAnimationEnd(Animator animation)
				{
					completeTask(taskUri);
				}


				@Override
				public void onAnimationCancel(Animator animation)
				{
					v.setTranslationX(0);
					completeTask(taskUri);
				}
			}).start();
		}
		else
		{
			completeTask(taskUri);
		}
	}


	/**
	 * Mark the give task as completed.
	 * 
	 * @param taskUri
	 *            The {@link Uri} of the task.
	 */
	private void completeTask(Uri taskUri)
	{
		ContentValues values = new ContentValues();
		values.put(Tasks.STATUS, Tasks.STATUS_COMPLETED);
		appContext.getContentResolver().update(taskUri, values, null, null);

	}
}
