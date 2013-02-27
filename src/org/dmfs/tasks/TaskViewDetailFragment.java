/*
 * TaskDetailFragment.java
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

import org.dmfs.provider.tasks.TaskContract.Tasks;
import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.Model;
import org.dmfs.tasks.model.OnContentChangeListener;
import org.dmfs.tasks.utils.AsyncModelLoader;
import org.dmfs.tasks.utils.ContentValueMapper;
import org.dmfs.tasks.utils.OnModelLoadedListener;
import org.dmfs.tasks.widget.TaskView;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


/**
 * A fragment representing a single Task detail screen. This fragment is either contained in a {@link TaskListActivity} in two-pane mode (on tablets) or a
 * {@link TaskDetailActivity} on handsets.
 * 
 * @author Arjun Naik <arjun@arjunnaik.in>
 * @author Marten Gajda <marten@dmfs.org>
 */

public class TaskViewDetailFragment extends Fragment implements OnModelLoadedListener, OnContentChangeListener
{
	/**
	 * The fragment argument representing the item ID that this fragment represents.
	 */
	public static final String PARAM_TASK_URI = "task_uri";

	private static final String TAG = "TaskViewDetailFragment";

	private static final String KEY_VALUES = "key_values";

	private static final ContentValueMapper CONTENT_VALUE_MAPPER = new ContentValueMapper()
		.addString(Tasks.ACCOUNT_TYPE, Tasks.ACCOUNT_NAME, Tasks.TITLE, Tasks.LOCATION, Tasks.DESCRIPTION, Tasks.GEO, Tasks.URL, Tasks.TZ, Tasks.DURATION,
			Tasks.LIST_NAME).addInteger(Tasks.PRIORITY, Tasks.LIST_COLOR, Tasks.TASK_COLOR, Tasks.STATUS, Tasks.CLASSIFICATION, Tasks.PERCENT_COMPLETE)
		.addLong(Tasks.LIST_ID, Tasks.DTSTART, Tasks.DUE, Tasks.COMPLETED, Tasks._ID);

	/**
	 * The dummy content this fragment is presenting.
	 */
	private Uri mTaskUri;
	private Context mActivity;

	ContentSet mValues;
	ViewGroup mContent;
	Model mModel;

	private Callback callback;


	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the fragment (e.g. upon screen orientation changes).
	 */
	public TaskViewDetailFragment()
	{
	}


	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		/*
		 * Get the URI of the task to show. For now this is always a task URI.
		 * 
		 * TODO: properly accept and handle instance URIs
		 */
		mTaskUri = getArguments().getParcelable(PARAM_TASK_URI);
		setHasOptionsMenu(true);
	}


	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);

		if (!(activity instanceof Callback))
		{
			throw new IllegalStateException("Activity must implement TaskViewDetailFragment callback.");
		}

		mActivity = activity;
		if (mTaskUri == null)
		{
			mTaskUri = activity.getIntent().getData();
		}
		callback = (Callback) activity;
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View rootView = inflater.inflate(R.layout.fragment_task_view_detail, container, false);

		if (mTaskUri != null)
		{
			Log.d(TAG, "taskUri is not null");
			mContent = (ViewGroup) rootView.findViewById(R.id.content);

			if (savedInstanceState == null)
			{
				mValues = new ContentSet(mActivity, mTaskUri, CONTENT_VALUE_MAPPER);
				mValues.addOnChangeListener(this, null, true);
			}
			else
			{
				// mValues = savedInstanceState.getParcelableArrayList(KEY_VALUES);
				new AsyncModelLoader(mActivity, this).execute();
			}
		}
		else
		{
			Log.w(TAG, "task_uri is null!");
		}

		return rootView;
	}


	private void updateView()
	{
		final LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		mContent.removeAllViews();
		TaskView editor = (TaskView) inflater.inflate(R.layout.task_view, mContent, false);
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
			Toast.makeText(mActivity, "Could not load Model", Toast.LENGTH_LONG).show();
			return;
		}

		mModel = model;

		updateView();

	}


	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		// outState.putParcelableArrayList(KEY_VALUES, mValues);
	}


	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		inflater.inflate(R.menu.task_detail_menu, menu);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.edit_task:
				callback.displayEditTask(mTaskUri);
				return true;
			case R.id.delete_task:
				Log.v(TAG, "removing task");
				mValues.delete();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	public interface Callback
	{
		public void displayEditTask(Uri taskUri);
	}


	@Override
	public void onContentChanged(ContentSet contentSet, String key)
	{
		Log.v(TAG, "modelloader called");
		if (key == null && contentSet.containsKey(Tasks.ACCOUNT_TYPE))
		{
			Log.v(TAG, "modelloader called");
			new AsyncModelLoader(mActivity, this).execute(contentSet.getAsString(Tasks.ACCOUNT_TYPE));
		}
	}
}
