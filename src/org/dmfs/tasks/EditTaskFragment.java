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

import org.dmfs.provider.tasks.TaskContract;
import org.dmfs.provider.tasks.TaskContract.TaskLists;
import org.dmfs.provider.tasks.TaskContract.Tasks;
import org.dmfs.provider.tasks.TaskContract.WriteableTaskLists;
import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.Model;
import org.dmfs.tasks.model.OnContentChangeListener;
import org.dmfs.tasks.utils.AsyncModelLoader;
import org.dmfs.tasks.utils.ContentValueMapper;
import org.dmfs.tasks.utils.OnModelLoadedListener;
import org.dmfs.tasks.utils.TasksListCursorAdapter;
import org.dmfs.tasks.widget.TaskEdit;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;


/**
 * 
 * Fragment for editing task details.
 * 
 * @author Arjun Naik <arjun@arjunnaik.in>
 * @author Marten Gajda <marten@dmfs.org>
 * 
 */

public class EditTaskFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, OnModelLoadedListener, OnContentChangeListener
{
	public static final String PARAM_TASK_URI = "task_uri";

	public static final String LIST_LOADER_URI = "uri";

	private static final String TAG = "TaskEditDetailFragment";

	/**
	 * Projection into the task list.
	 */
	private final static String[] TASK_LIST_PROJECTION = new String[] { TaskContract.TaskListColumns._ID, TaskContract.TaskListColumns.LIST_NAME,
		TaskContract.TaskListSyncColumns.ACCOUNT_TYPE, TaskContract.TaskListSyncColumns.ACCOUNT_NAME, TaskContract.TaskListColumns.LIST_COLOR };

	/**
	 * This interface provides a convenient way to get column indices of {@link EditTaskFragment#TASK_LIST_PROJECTION} without any overhead.
	 */
	@SuppressWarnings("unused")
	private interface TASK_LIST_PROJECTION_VALUES
	{
		public final static int id = 0;
		public final static int list_name = 1;
		public final static int account_type = 2;
		public final static int account_name = 3;
		public final static int list_color = 4;
	}

	private static final String KEY_VALUES = "key_values";

	private static final ContentValueMapper CONTENT_VALUE_MAPPER = new ContentValueMapper()
		.addString(Tasks.ACCOUNT_TYPE, Tasks.ACCOUNT_NAME, Tasks.TITLE, Tasks.LOCATION, Tasks.DESCRIPTION, Tasks.GEO, Tasks.URL, Tasks.TZ, Tasks.DURATION,
			Tasks.LIST_NAME).addInteger(Tasks.PRIORITY, Tasks.LIST_COLOR, Tasks.TASK_COLOR, Tasks.STATUS, Tasks.CLASSIFICATION, Tasks.PERCENT_COMPLETE, Tasks.IS_ALLDAY)
		.addLong(Tasks.LIST_ID, Tasks.DTSTART, Tasks.DUE, Tasks.COMPLETED, Tasks._ID);

	private static final String[] INSTANCE_VALUES = new String[] { Tasks.DTSTART, Tasks.DUE, Tasks.RDATE, Tasks.RRULE };

	private boolean appForEdit = true;
	private TasksListCursorAdapter taskListAdapter;
	/**
	 * The dummy content this fragment is presenting.
	 */
	private Uri mTaskUri;

	ContentSet mValues;
	ViewGroup mContent;
	ViewGroup mHeader;
	Model mModel;
	Context mAppContext;


	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the fragment (e.g. upon screen orientation changes).
	 */
	public EditTaskFragment()
	{
	}


	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}


	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		mTaskUri = getArguments().getParcelable(PARAM_TASK_URI);
		mAppContext = activity.getApplicationContext();
	}


	@Override
	public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		Log.v(TAG, "On create view");
		View rootView = inflater.inflate(R.layout.fragment_task_edit_detail, container, false);
		mContent = (ViewGroup) rootView.findViewById(R.id.content);
		mHeader = (ViewGroup) rootView.findViewById(R.id.header);

		appForEdit = !Tasks.CONTENT_URI.equals(mTaskUri);

		if (!appForEdit)
		{
			setListUri(WriteableTaskLists.CONTENT_URI);
		}

		final LinearLayout taskListBar = (LinearLayout) inflater.inflate(R.layout.task_list_provider_bar, mHeader);
		final Spinner listSpinner = (Spinner) taskListBar.findViewById(R.id.task_list_spinner);

		taskListAdapter = new TasksListCursorAdapter(mAppContext);
		listSpinner.setAdapter(taskListAdapter);

		listSpinner.setOnItemSelectedListener(new OnItemSelectedListener()
		{
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3)
			{
				Cursor c = (Cursor) arg0.getItemAtPosition(arg2);

				String accountType = c.getString(TASK_LIST_PROJECTION_VALUES.account_type);
				int listColor = c.getInt(TASK_LIST_PROJECTION_VALUES.list_color);
				taskListBar.setBackgroundColor(listColor);
				if (!appForEdit)
				{
					mValues.put(Tasks.LIST_ID, c.getLong(TASK_LIST_PROJECTION_VALUES.id));
				}
				new AsyncModelLoader(mAppContext, EditTaskFragment.this).execute(accountType);
			}


			@Override
			public void onNothingSelected(AdapterView<?> arg0)
			{
				// nothing to do here
			}

		});

		if (android.os.Build.VERSION.SDK_INT < 11)
		{
			listSpinner.setBackgroundDrawable(null);

		}

		if (appForEdit)
		{
			if (mTaskUri != null)
			{

				if (savedInstanceState == null)
				{
					mValues = new ContentSet(mTaskUri);
					mValues.update(mAppContext, CONTENT_VALUE_MAPPER);
					mValues.addOnChangeListener(this, null, true);
				}
				else
				{
					mValues = savedInstanceState.getParcelable(KEY_VALUES);
					new AsyncModelLoader(mAppContext, this).execute(mValues.getAsString(Tasks.ACCOUNT_TYPE));
					setListUri(ContentUris.withAppendedId(TaskLists.CONTENT_URI, mValues.getAsLong(Tasks.LIST_ID)));
				}
				// disable spinner
				listSpinner.setEnabled(false);
				// hide spinner background
				if (android.os.Build.VERSION.SDK_INT >= 16)
				{
					listSpinner.setBackground(null);
				}
			}
		}
		else
		{
			if (savedInstanceState == null)
			{
				mValues = new ContentSet(Tasks.CONTENT_URI);
			}
			else
			{
				mValues = savedInstanceState.getParcelable(KEY_VALUES);
				new AsyncModelLoader(mAppContext, this).execute(mValues.getAsString(Tasks.ACCOUNT_TYPE));
				setListUri(WriteableTaskLists.CONTENT_URI);
			}
		}

		return rootView;
	}


	private void updateView()
	{
		final LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		mContent.removeAllViews();

		TaskEdit editor = (TaskEdit) inflater.inflate(R.layout.task_edit, mContent, false);
		editor.setModel(mModel);
		editor.setValues(mValues);
		mContent.addView(editor);

		Log.d(TAG, "At the end of updateView");
	}


	@Override
	public void onModelLoaded(Model model)
	{
		if (model == null)
		{
			Toast.makeText(getActivity(), "Could not load Model", Toast.LENGTH_LONG).show();
			return;
		}
		mModel = model;

		updateView();
	}


	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putParcelable(KEY_VALUES, mValues);
	}


	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle)
	{
		return new CursorLoader(mAppContext, (Uri) bundle.getParcelable(LIST_LOADER_URI), TASK_LIST_PROJECTION, null, null, null);
	}


	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)
	{
		taskListAdapter.changeCursor(cursor);
	}


	@Override
	public void onLoaderReset(Loader<Cursor> loader)
	{
		taskListAdapter.changeCursor(null);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		final int menuId = item.getItemId();
		Activity activity = getActivity();
		if (menuId == R.id.editor_action_save)
		{
			Log.v(TAG, "persisting task");
			/*
			 * if (mValues.containsAnyKey(INSTANCE_VALUES)) { mValues.ensureValues(INSTANCE_VALUES); }
			 */
			mTaskUri = mValues.persist(activity);
			// return proper result
			Intent result = new Intent();
			result.setData(mTaskUri);
			activity.setResult(Activity.RESULT_OK, result);
			activity.finish();
			return true;
		}
		else if (menuId == R.id.editor_action_cancel)
		{
			Log.v(TAG, "cancelled");
			activity.setResult(Activity.RESULT_CANCELED);
			activity.finish();
			return true;
		}
		return false;
	}


	@Override
	public void onContentChanged(ContentSet contentSet, String key)
	{
		if (key == null && contentSet.containsKey(Tasks.ACCOUNT_TYPE))
		{
			new AsyncModelLoader(mAppContext, this).execute(contentSet.getAsString(Tasks.ACCOUNT_TYPE));
			setListUri(appForEdit ? ContentUris.withAppendedId(TaskLists.CONTENT_URI, contentSet.getAsLong(Tasks.LIST_ID)) : WriteableTaskLists.CONTENT_URI);
		}
	}


	private void setListUri(Uri uri)
	{
		if (this.isAdded())
		{
			Bundle bundle = new Bundle();
			bundle.putParcelable(LIST_LOADER_URI, uri);

			getLoaderManager().restartLoader(0, bundle, this);
		}
	}
}
