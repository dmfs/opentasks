/*
 * Copyright (C) 2014 Marten Gajda <marten@dmfs.org>
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

package org.dmfs.tasks.homescreen;

import java.util.ArrayList;

import org.dmfs.provider.tasks.TaskContract;
import org.dmfs.provider.tasks.TaskContract.TaskLists;
import org.dmfs.tasks.R;
import org.dmfs.tasks.utils.TasksListCursorAdapter;
import org.dmfs.tasks.utils.TasksListCursorAdapter.SelectionEnabledListener;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;


/**
 * Provides the selection of task list.
 * 
 * @author Tobias Reinsch <tobias@dmfs.org>
 * 
 */
public class TaskListSelectionFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>
{

	public static final String LIST_LOADER_URI = "uri";
	public static final String LIST_LOADER_FILTER = "filter";

	public static final String LIST_LOADER_VISIBLE_LISTS_FILTER = TaskLists.SYNC_ENABLED + "=1";

	/**
	 * Projection into the task list.
	 */
	private final static String[] TASK_LIST_PROJECTION = new String[] { TaskContract.TaskListColumns._ID, TaskContract.TaskListColumns.LIST_NAME,
		TaskContract.TaskListSyncColumns.ACCOUNT_TYPE, TaskContract.TaskListSyncColumns.ACCOUNT_NAME, TaskContract.TaskListColumns.LIST_COLOR };

	private TasksListCursorAdapter mTaskListAdapter;
	private Activity mActivity;
	private ListView mTaskList;
	private String mAuthority;
	private onSelectionListener mListener;
	private TextView mButtonOk;
	private TextView mButtonCancel;


	public TaskListSelectionFragment(onSelectionListener listener)
	{
		mListener = listener;
	}


	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		mActivity = activity;
		mAuthority = TaskContract.taskAuthority(activity);
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View rootView = inflater.inflate(R.layout.fragment_task_list_selection, container, false);
		mButtonOk = (TextView) rootView.findViewById(android.R.id.button1);
		mButtonCancel = (TextView) rootView.findViewById(android.R.id.button2);

		mButtonOk.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (mListener != null)
				{
					mListener.onSelection(mTaskListAdapter.getSelectedLists());
				}

			}
		});
		mButtonCancel.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (mListener != null)
				{
					mListener.onSelectionCancel();
				}

			}
		});

		return rootView;
	}


	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		mTaskList = getListView();
		mTaskListAdapter = new TasksListCursorAdapter(mActivity);
		mTaskList.setAdapter(mTaskListAdapter);

		mTaskListAdapter.setSelectionEnabledListener(new SelectionEnabledListener()
		{
			@Override
			public void onSelectionEnabled()
			{
				mButtonOk.setEnabled(true);
			}


			@Override
			public void onSelectionDisabled()
			{
				mButtonOk.setEnabled(false);

			}
		});

		Bundle bundle = new Bundle();
		bundle.putParcelable(LIST_LOADER_URI, TaskLists.getContentUri(mAuthority));
		bundle.putString(LIST_LOADER_FILTER, LIST_LOADER_VISIBLE_LISTS_FILTER);
		getLoaderManager().restartLoader(-2, bundle, this);
	}


	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle)
	{
		return new CursorLoader(mActivity, (Uri) bundle.getParcelable(LIST_LOADER_URI), TASK_LIST_PROJECTION, bundle.getString(LIST_LOADER_FILTER), null, null);
	}


	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)
	{
		mTaskListAdapter.changeCursor(cursor);
	}


	@Override
	public void onLoaderReset(Loader<Cursor> loader)
	{
		mTaskListAdapter.changeCursor(null);
	}

	public interface onSelectionListener
	{
		public void onSelection(ArrayList<Long> selectedLists);


		public void onSelectionCancel();
	}

}
