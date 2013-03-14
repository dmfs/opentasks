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
import android.database.ContentObserver;
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


/**
 * A fragment representing a single Task detail screen. This fragment is either contained in a {@link TaskListActivity} in two-pane mode (on tablets) or a
 * {@link ViewTaskActivity} on handsets.
 * 
 * @author Arjun Naik <arjun@arjunnaik.in>
 * @author Marten Gajda <marten@dmfs.org>
 */

public class ViewTaskFragment extends Fragment implements OnModelLoadedListener, OnContentChangeListener
{
	private static final String TAG = "TaskViewDetailFragment";

	private static final String KEY_VALUES = "key_values";

	private static final ContentValueMapper CONTENT_VALUE_MAPPER = new ContentValueMapper()
		.addString(Tasks.ACCOUNT_TYPE, Tasks.ACCOUNT_NAME, Tasks.TITLE, Tasks.LOCATION, Tasks.DESCRIPTION, Tasks.GEO, Tasks.URL, Tasks.TZ, Tasks.DURATION,
			Tasks.LIST_NAME).addInteger(Tasks.PRIORITY, Tasks.LIST_COLOR, Tasks.TASK_COLOR, Tasks.STATUS, Tasks.CLASSIFICATION, Tasks.PERCENT_COMPLETE, Tasks.IS_ALLDAY)
		.addLong(Tasks.LIST_ID, Tasks.DTSTART, Tasks.DUE, Tasks.COMPLETED, Tasks._ID);

	/**
	 * The dummy content this fragment is presenting.
	 */
	private Uri mTaskUri;

	ContentSet mContentSet;
	ViewGroup mContent;
	Model mModel;
	Context mAppContext;

	private Callback callback;


	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the fragment (e.g. upon screen orientation changes).
	 */
	public ViewTaskFragment()
	{
	}


	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		/*
		 * Get the URI of the task to show. For now this is always a TASK_URI.
		 * 
		 * TODO: properly accept and handle instance URIs
		 */

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

		callback = (Callback) activity;
		mAppContext = activity.getApplicationContext();
		Log.v(TAG, "mTaskUri " + mTaskUri);

	}


	@Override
	public void onDetach()
	{
		super.onDetach();
		mAppContext.getContentResolver().unregisterContentObserver(mObserver);
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View rootView = inflater.inflate(R.layout.fragment_task_view_detail, container, false);
		mContent = (ViewGroup) rootView.findViewById(R.id.content);

		if (savedInstanceState != null)
		{
			mContentSet = savedInstanceState.getParcelable(KEY_VALUES);
			new AsyncModelLoader(mAppContext, this).execute(mContentSet.getAsString(Tasks.ACCOUNT_TYPE));
		}

		return rootView;
	}


	public void loadUri(Uri uri)
	{
		if (mTaskUri != null)
		{
			mAppContext.getContentResolver().unregisterContentObserver(mObserver);
		}

		mTaskUri = uri;
		mContentSet = new ContentSet(uri);
		mContentSet.update(mAppContext, CONTENT_VALUE_MAPPER);
		mContentSet.addOnChangeListener(this, null, true);
		mAppContext.getContentResolver().registerContentObserver(uri, false, mObserver);
	}


	private void updateView()
	{
		if (mContent != null)
		{
			final LayoutInflater inflater = (LayoutInflater) mAppContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			mContent.removeAllViews();
			TaskView editor = (TaskView) inflater.inflate(R.layout.task_view, null);
			editor.setModel(mModel);
			editor.setValues(mContentSet);
			mContent.addView(editor);
			Log.d(TAG, "At the end of updateView");
		}
	}


	@Override
	public void onModelLoaded(Model model)
	{
		if (model == null)
		{
			return;
		}

		mModel = model;

		updateView();

	}


	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putParcelable(KEY_VALUES, mContentSet);
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
				mContentSet.delete(mAppContext);
				callback.onDelete(mTaskUri);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	public interface Callback
	{
		public void displayEditTask(Uri taskUri);
		public void onDelete(Uri taskUri);
	}


	@Override
	public void onContentChanged(ContentSet contentSet, String key)
	{
		Log.v(TAG, "modelloader called");
		if (key == null && contentSet.containsKey(Tasks.ACCOUNT_TYPE))
		{
			Log.v(TAG, "modelloader called");
			new AsyncModelLoader(mAppContext, this).execute(contentSet.getAsString(Tasks.ACCOUNT_TYPE));
		}
	}

	private final ContentObserver mObserver = new ContentObserver(null)
	{
		@Override
		public void onChange(boolean selfChange)
		{
			if (mContentSet != null)
			{
				mContentSet.update(mAppContext, CONTENT_VALUE_MAPPER);
			}
		}
	};

}
