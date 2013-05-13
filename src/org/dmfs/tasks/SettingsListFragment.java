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

import java.util.ArrayList;
import java.util.HashMap;

import org.dmfs.provider.tasks.TaskContract;

import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.TextView;


/**
 * This fragment is used to display a list of task-providers. It show the task-providers which are visible in main {@link TaskListFragment} and also the
 * task-providers which are synced. The selection between the two lists is made by passing arguments to the fragment in a {@link Bundle} when it created in the
 * {@link SyncSettingsActivity}.
 * 
 * @author Arjun Naik<arjun@arjunnaik.in>
 */

public class SettingsListFragment extends ListFragment implements AbsListView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor>
{
	private static final String TAG = "SettingsListFragment";
	public static final String LIST_SELECTION_ARGS = "list_selection_args";
	public static final String LIST_STRING_PARAMS = "list_string_params";
	public static final String LIST_FRAGMENT_LAYOUT = "list_fragment_layout";
	public static final String COMPARE_COLUMN_NAME = "column_name";
	public static final String LIST_ONDETACH_SAVE = "list_ondetach_save";
	private Context mContext;
	private VisibleListAdapter mAdapter;

	private String mListSelectionArguments;
	private String[] mListSelectionParam;
	private String mListCompareColumnName;
	private boolean saveOnDetach;
	private int fragmentLayout;


	public SettingsListFragment()
	{

	}


	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}


	/**
	 * The SQL selection condition used to select synced or visible list, the parameters for the select condition, the layout to be used and the column which is
	 * used for current selection is passed through a {@link Bundle}. The fragment layout is inflated and returned.
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		Bundle args = getArguments();
		mListSelectionArguments = args.getString(LIST_SELECTION_ARGS);
		mListSelectionParam = args.getStringArray(LIST_STRING_PARAMS);
		fragmentLayout = args.getInt(LIST_FRAGMENT_LAYOUT);
		saveOnDetach = args.getBoolean(LIST_ONDETACH_SAVE);
		mListCompareColumnName = args.getString(COMPARE_COLUMN_NAME);
		View view = inflater.inflate(fragmentLayout, container, false);
		return view;
	}


	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		getLoaderManager().restartLoader(-2, null, this);
		mAdapter = new VisibleListAdapter(mContext, null, 0);
		setListAdapter(mAdapter);
		getListView().setOnItemClickListener(this);
	}


	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		mContext = activity.getBaseContext();
	}


	@Override
	public void onDetach()
	{
		if (saveOnDetach)
		{
			saveListState();
		}
		super.onDetach();
	}


	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long rowId)
	{
		Log.d(TAG, "Item Clicked");
		VisibleListAdapter adapter = (VisibleListAdapter) adapterView.getAdapter();
		VisibleListAdapter.CheckableItem item = (VisibleListAdapter.CheckableItem) view.getTag();
		boolean checked = item.cb.isChecked();
		item.cb.setChecked(!checked);
		adapter.addToState(rowId, !checked);
	}


	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1)
	{
		return new CursorLoader(mContext, TaskContract.TaskLists.CONTENT_URI, new String[] { TaskContract.TaskLists._ID, TaskContract.TaskLists.LIST_NAME,
			TaskContract.TaskLists.LIST_COLOR, TaskContract.TaskLists.SYNC_ENABLED, TaskContract.TaskLists.VISIBLE, TaskContract.TaskLists.ACCOUNT_NAME },
			mListSelectionArguments, mListSelectionParam, TaskContract.TaskLists.ACCOUNT_NAME + " COLLATE NOCASE ASC");
	}


	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1)
	{
		mAdapter.swapCursor(arg1);

	}


	@Override
	public void onLoaderReset(Loader<Cursor> arg0)
	{
		mAdapter.changeCursor(null);

	}

	/**
	 * This extends the {@link CursorAdapter}. The column index for the list name, list color and the current selection state is computed when the
	 * {@link Cursor} is swapped. It also maintains the changes made to the current selection state through a {@link HashMap} of ids and selection state. If the
	 * selection state is modified and then modified again then it is removed from the HashMap because it has reverted to the original state.
	 * 
	 * @author Arjun Naik<arjun@arjunnaik.in>
	 * 
	 */
	private class VisibleListAdapter extends CursorAdapter
	{
		LayoutInflater inflater;
		private int listNameColumn, listColorColumn, compareColumn, accountNameColumn;
		private HashMap<Long, Boolean> savedPositions = new HashMap<Long, Boolean>();


		@Override
		public Cursor swapCursor(Cursor c)
		{
			if (c != null)
			{
				listNameColumn = c.getColumnIndex(TaskContract.TaskLists.LIST_NAME);
				listColorColumn = c.getColumnIndex(TaskContract.TaskLists.LIST_COLOR);
				compareColumn = c.getColumnIndex(mListCompareColumnName);
				accountNameColumn = c.getColumnIndex(TaskContract.TaskLists.ACCOUNT_NAME);
			}
			else
			{
				listNameColumn = -1;
				listColorColumn = -1;
				compareColumn = -1;
				accountNameColumn = -1;
			}
			return super.swapCursor(c);

		}


		public VisibleListAdapter(Context context, Cursor c, int flags)
		{
			super(context, c, flags);
			inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		}


		@Override
		public void bindView(View v, Context c, final Cursor cur)
		{
			String listName = cur.getString(listNameColumn);
			CheckableItem item = (CheckableItem) v.getTag();
			item.text1.setText(listName);
			item.text2.setText(cur.getString(accountNameColumn));
			int listColor = cur.getInt(listColorColumn);
			item.bgColor.setBackgroundColor(listColor);

			if (!cur.isNull(compareColumn))
			{
				long id = cur.getLong(0);
				boolean checkValue;
				if (savedPositions.containsKey(id))
				{
					checkValue = savedPositions.get(id);
				}
				else
				{
					checkValue = cur.getInt(compareColumn) == 1;
				}
				item.cb.setChecked(checkValue);

			}
		}

		public class CheckableItem
		{
			TextView text1;
			TextView text2;
			View bgColor;
			CheckBox cb;
		}


		@Override
		public View newView(Context c, Cursor cur, ViewGroup vg)
		{
			View newInflatedView = inflater.inflate(R.layout.visible_task_list_item, null);
			CheckableItem item = new CheckableItem();
			item.text1 = (TextView) newInflatedView.findViewById(android.R.id.text1);
			item.text2 = (TextView) newInflatedView.findViewById(android.R.id.text2);
			item.bgColor = newInflatedView.findViewById(R.id.visible_task_list_color);
			item.cb = (CheckBox) newInflatedView.findViewById(R.id.visible_task_list_checked);
			newInflatedView.setTag(item);
			return newInflatedView;
		}


		private boolean addToState(long id, boolean val)
		{
			if (savedPositions.containsKey(Long.valueOf(id)))
			{
				savedPositions.remove(id);
				return false;
			}
			else
			{
				savedPositions.put(id, val);
				return true;
			}
		}


		public void clearHashMap()
		{
			savedPositions.clear();
		}


		public HashMap<Long, Boolean> getState()
		{
			return savedPositions;
		}

	}


	/**
	 * This function is called to save the any modifications made to the displayed list. It retrieves the {@link HashMap} from the adapter of the list and uses
	 * it makes the changes persistent. For this it uses a batch operation provided by {@link ContentResolver}. The operations to be performed in the batch
	 * operation are stored in an {@link ArrayList} of {@link ContentProviderOperation}.
	 * 
	 * @return <code>true</code> if the save operation was successful, <code>false</code> otherwise.
	 */
	public boolean saveListState()
	{
		HashMap<Long, Boolean> savedPositions = ((VisibleListAdapter) getListAdapter()).getState();
		Log.d(TAG, "Length of Changes: " + savedPositions.size());
		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
		for (Long posInt : savedPositions.keySet())
		{
			boolean val = savedPositions.get(posInt);
			ContentProviderOperation op = ContentProviderOperation.newUpdate(TaskContract.TaskLists.CONTENT_URI)
				.withSelection(TaskContract.TaskLists._ID + "=?", new String[] { posInt.toString() }).withValue(mListCompareColumnName, val ? "1" : "0")
				.build();
			ops.add(op);
		}
		try
		{
			mContext.getContentResolver().applyBatch(TaskContract.AUTHORITY, ops);
		}
		catch (RemoteException e)
		{
			Log.e(TAG, "Remote Exception :" + e.getMessage());
			e.printStackTrace();
			return false;
		}
		catch (OperationApplicationException e)
		{
			Log.e(TAG, "OperationApplicationException : " + e.getMessage());
			e.printStackTrace();
			return false;
		}
		return true;
	}


	public void doneSaveListState()
	{
		((VisibleListAdapter) getListAdapter()).clearHashMap();
	}

}
