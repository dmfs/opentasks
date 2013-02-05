/*
 * TaskEditDetailFragment.java
 *
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

import org.dmfs.provider.tasks.TaskContract;
import org.dmfs.provider.tasks.TaskContract.Tasks;
import org.dmfs.tasks.model.Model;
import org.dmfs.tasks.utils.AsyncContentLoader;
import org.dmfs.tasks.utils.AsyncModelLoader;
import org.dmfs.tasks.utils.ContentValueMapper;
import org.dmfs.tasks.utils.OnContentLoadedListener;
import org.dmfs.tasks.utils.OnModelLoadedListener;
import org.dmfs.tasks.widget.TaskEdit;
import org.dmfs.tasks.widget.TaskListFieldView;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;


/**
 * 
 * Fragment for editing task details.
 * 
 * @author Arjun Naik <arjun@arjunnaik.in>
 * 
 */

public class TaskEditDetailFragment extends Fragment implements OnContentLoadedListener, OnModelLoadedListener
{

	public static final String ARG_ITEM_ID = "item_id";

	private static final String TAG = "TaskEditDetailFragment";

	private static final String KEY_VALUES = "key_values";

	private static final ContentValueMapper CONTENT_VALUE_MAPPER = new ContentValueMapper()
		.addString(Tasks.ACCOUNT_TYPE, Tasks.ACCOUNT_NAME, Tasks.TITLE, Tasks.LOCATION, Tasks.DESCRIPTION, Tasks.GEO, Tasks.URL, Tasks.TZ, Tasks.DURATION,
			Tasks.LIST_NAME).addInteger(Tasks.PRIORITY, Tasks.LIST_COLOR, Tasks.TASK_COLOR, Tasks.STATUS, Tasks.CLASSIFICATION, Tasks.PERCENT_COMPLETE)
		.addLong(Tasks.LIST_ID, Tasks.DTSTART, Tasks.DUE, Tasks.COMPLETED, Tasks._ID);

	public static final String FRAGMENT_INTENT = "fragment_intent";

	public static final String EDIT_TASK = "edit_task";

	public static final String NEW_TASK = "new_task";

	private boolean appForEdit = true;
	private ArrayList<TaskProvider> providerList;

	/**
	 * The dummy content this fragment is presenting.
	 */
	private Uri taskUri;
	private Context appContext;

	ArrayList<ContentValues> mValues;
	ViewGroup mContent;
	Model mModel;

	private Activity mActivity;
	String fragmentIntent;


	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the fragment (e.g. upon screen orientation changes).
	 */
	public TaskEditDetailFragment()
	{
	}


	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		fragmentIntent = getArguments().getString(TaskEditDetailFragment.FRAGMENT_INTENT);
		if (fragmentIntent.equals(TaskEditDetailFragment.EDIT_TASK))
		{
			taskUri = getArguments().getParcelable(TaskViewDetailFragment.ARG_ITEM_ID);
		}
		mValues = new ArrayList<ContentValues>();
	}


	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);

		mActivity = activity;
		appContext = activity.getApplicationContext();
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View rootView = inflater.inflate(R.layout.fragment_task_edit_detail, container, false);
		mContent = (ViewGroup) rootView.findViewById(R.id.content);

		appForEdit = fragmentIntent.equals(TaskEditDetailFragment.EDIT_TASK) ? true : false;

		if (appForEdit)
		{
			if (taskUri != null)
			{

				if (savedInstanceState == null)
				{
					new AsyncContentLoader(appContext, this, CONTENT_VALUE_MAPPER).execute(taskUri);
				}
				else
				{
					mValues = savedInstanceState.getParcelableArrayList(KEY_VALUES);
					new AsyncModelLoader(appContext, this).execute("");
				}
			}
		}
		else
		{
			mValues = new ArrayList<ContentValues>();
			ContentValues emptyCV = new ContentValues();
			mValues.add(emptyCV);
			new AsyncModelLoader(appContext, this).execute("");

			Cursor taskListCursor = appContext.getContentResolver().query(
				TaskContract.WriteableTaskLists.CONTENT_URI,
				new String[] { TaskContract.TaskListColumns.LIST_NAME, TaskContract.TaskListSyncColumns.ACCOUNT_NAME, TaskContract.TaskListColumns.LIST_COLOR,
					TaskContract.TaskListColumns._ID }, null, null, null);
			Log.d(TAG, "taskListCursor lenght : " + taskListCursor.getCount());
			int nameColumn = taskListCursor.getColumnIndex(TaskContract.TaskListColumns.LIST_NAME);
			int accountColumn = taskListCursor.getColumnIndex(TaskContract.TaskListSyncColumns.ACCOUNT_NAME);
			int idColumn = taskListCursor.getColumnIndex(TaskContract.TaskListColumns._ID);
			int colorColumn = taskListCursor.getColumnIndex(TaskContract.TaskListColumns.LIST_COLOR);
			taskListCursor.moveToFirst();
			providerList = new ArrayList<TaskEditDetailFragment.TaskProvider>();
			while (!taskListCursor.isAfterLast())
			{

				String listName = taskListCursor.getString(nameColumn);
				String accountName = taskListCursor.getString(accountColumn);
				int id = taskListCursor.getInt(idColumn);
				int color = taskListCursor.getInt(colorColumn);
				TaskProvider provider = new TaskProvider(id, listName, accountName, color);
				providerList.add(provider);
				Log.d(TAG, "List Name : " + listName);
				Log.d(TAG, "Account Name : " + accountName);
				Log.d(TAG, "Color : " + color);
				Log.d(TAG, "ID : " + id);
				taskListCursor.moveToNext();
			}
			taskListCursor.close();
		}

		return rootView;
	}

	private class TaskProvider
	{
		private int _id;
		private int color;
		private String provider;
		private String account;


		public TaskProvider(int id, String p, String a, int c)
		{
			_id = id;
			color = c;
			provider = p;
			account = a;
		}


		public String getAccount()
		{
			return account;
		}


		public String getProvider()
		{
			return provider;
		}


		public int getColor()
		{
			return color;
		}

	}


	private void updateView()
	{
		final LayoutInflater inflater = (LayoutInflater) appContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		if (appForEdit)
		{
			for (ContentValues values : mValues)
			{
				TaskEdit editor = (TaskEdit) inflater.inflate(R.layout.task_edit, mContent, false);
				editor.setModel(mModel);
				editor.setActivity(mActivity);
				Log.d(TAG, "Values : " + values.toString());
				editor.setValues(values);
				mContent.addView(editor);
			}
		}
		else
		{
			final LinearLayout taskListBar = (LinearLayout) inflater.inflate(R.layout.task_list_provider_bar, mContent);
			final Spinner listSpinner = (Spinner) taskListBar.findViewById(R.id.task_list_spinner);
			final TextView listAccountTV = (TextView) taskListBar.findViewById(R.id.task_list_account_name_select);
			listSpinner.setAdapter(new SpinnerAdapter()
			{

				@Override
				public void unregisterDataSetObserver(DataSetObserver observer)
				{

				}


				@Override
				public void registerDataSetObserver(DataSetObserver observer)
				{

				}


				@Override
				public boolean isEmpty()
				{
					return false;
				}


				@Override
				public boolean hasStableIds()
				{
					return true;
				}


				@Override
				public int getViewTypeCount()
				{
					return 0;
				}


				@Override
				public View getView(int position, View convertView, ViewGroup parent)
				{
					if (convertView == null)
					{
						convertView = inflater.inflate(R.layout.list_spinner_item_dropdown, null);

					}
					((TextView) convertView).setText(((TaskProvider) getItem(position)).getProvider());
					return convertView;
				}


				@Override
				public int getItemViewType(int position)
				{
					return 0;
				}


				@Override
				public long getItemId(int position)
				{
					return position;
				}


				@Override
				public Object getItem(int position)
				{
					return providerList.get(position);
				}


				@Override
				public int getCount()
				{
					return providerList.size();
				}


				@Override
				public View getDropDownView(int position, View convertView, ViewGroup parent)
				{
					if (convertView == null)
					{
						convertView = inflater.inflate(R.layout.list_spinner_item_selected, null);

					}
					((TextView) convertView).setText(((TaskProvider) getItem(position)).getProvider());
					return convertView;
				}
			});

			listSpinner.setOnItemSelectedListener(new OnItemSelectedListener()
			{

				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3)
				{
					TaskProvider provider = (TaskProvider) arg0.getItemAtPosition(arg2);
					listAccountTV.setText(provider.getAccount());
					taskListBar.setBackgroundColor(provider.getColor());
					int textColor = TaskListFieldView.getTextColorFromBackground(provider.getColor());
					listAccountTV.setTextColor(textColor);
				}


				@Override
				public void onNothingSelected(AdapterView<?> arg0)
				{

				}

			});

		}
		Log.d(TAG, "At the end of updateView");
	}


	@Override
	public void onContentLoaded(ContentValues values)
	{
		if (values == null)
		{
			Toast.makeText(appContext, "Could not load Task", Toast.LENGTH_LONG).show();
			return;
		}

		new AsyncModelLoader(appContext, this).execute(values.getAsString(Tasks.ACCOUNT_TYPE));

		mValues.add(values);
		// updateView();

	}


	@Override
	public void onModelLoaded(Model model)
	{
		if (model == null)
		{
			Toast.makeText(appContext, "Could not load Model", Toast.LENGTH_LONG).show();
			return;
		}

		mModel = model;

		updateView();

	}


	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putParcelableArrayList(KEY_VALUES, mValues);
	}

}
