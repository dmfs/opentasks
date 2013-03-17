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

	private static final String STATE_VALUES = "values";
	private static final String STATE_TASK_URI = "task_uri";

	private static final ContentValueMapper CONTENT_VALUE_MAPPER = new ContentValueMapper()
		.addString(Tasks.ACCOUNT_TYPE, Tasks.ACCOUNT_NAME, Tasks.TITLE, Tasks.LOCATION, Tasks.DESCRIPTION, Tasks.GEO, Tasks.URL, Tasks.TZ, Tasks.DURATION,
			Tasks.LIST_NAME)
		.addInteger(Tasks.PRIORITY, Tasks.LIST_COLOR, Tasks.TASK_COLOR, Tasks.STATUS, Tasks.CLASSIFICATION, Tasks.PERCENT_COMPLETE, Tasks.IS_ALLDAY)
		.addLong(Tasks.LIST_ID, Tasks.DTSTART, Tasks.DUE, Tasks.COMPLETED, Tasks._ID);

	private Uri mTaskUri;

	private ContentSet mContentSet;
	private ViewGroup mContent;
	private Model mModel;
	private Context mAppContext;

	private Callback mCallback;

	public interface Callback
	{
		/**
		 * This is called to instruct the Activity to call the editor for a specific task.
		 * 
		 * @param taskUri
		 *            The {@link Uri} of the task to edit.
		 */
		public void onEditTask(Uri taskUri);


		/**
		 * This is called to inform the Activity that a task has been deleted.
		 * 
		 * @param taskUri
		 *            The {@link Uri} of the deleted task. Note that the Uri is likely to have invalid at the time of calling this method.
		 */
		public void onDelete(Uri taskUri);
	}


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

		mCallback = (Callback) activity;
		mAppContext = activity.getApplicationContext();
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
			// We have an incoming state, so load the ContentSet and the task Uri from the saved state.
			mContentSet = savedInstanceState.getParcelable(STATE_VALUES);
			mTaskUri = savedInstanceState.getParcelable(STATE_TASK_URI);

			if (mContent != null)
			{
				// register listener and observer
				mContentSet.addOnChangeListener(this, null, true);
				if (mTaskUri != null)
				{
					mAppContext.getContentResolver().registerContentObserver(mTaskUri, false, mObserver);
				}

				if (mContentSet.getAsString(Tasks.ACCOUNT_TYPE) != null)
				{
					// the content set contains a valid task, so load the model
					new AsyncModelLoader(mAppContext, this).execute(mContentSet.getAsString(Tasks.ACCOUNT_TYPE));
				}
			}
		}

		return rootView;
	}


	/**
	 * Load the task with the given {@link Uri} in the detail view.
	 * <p>
	 * At present only Task Uris are supported.
	 * </p>
	 * TODO: add support for instance Uris.
	 * 
	 * @param uri
	 *            The {@link Uri} of the task to show.
	 */
	public void loadUri(Uri uri)
	{
		if (mTaskUri != null)
		{
			/*
			 * Unregister the observer for any previously shown task first.
			 */
			mAppContext.getContentResolver().unregisterContentObserver(mObserver);
		}

		mTaskUri = uri;
		if (uri != null)
		{
			/*
			 * Create a new ContentSet and load the values for the given Uri. Also register listener and observer for changes in the ContentSet and the Uri.
			 */
			mContentSet = new ContentSet(uri);
			mContentSet.addOnChangeListener(this, null, true);
			mAppContext.getContentResolver().registerContentObserver(uri, false, mObserver);
			mContentSet.update(mAppContext, CONTENT_VALUE_MAPPER);
		}
		else
		{
			/*
			 * Immediately update the view with the empty task uri, i.e. clear the view.
			 */
			mContentSet = null;
			if (mContent != null)
			{
				mContent.removeAllViews();
			}
		}

		getActivity().invalidateOptionsMenu();
	}


	/**
	 * Update the detail view with the current ContentSet. This removes any previous detail view and creates a new one if {@link #mContentSet} is not
	 * <code>null</code>.
	 */
	private void updateView()
	{
		if (mContent != null)
		{
			final LayoutInflater inflater = (LayoutInflater) mAppContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			mContent.removeAllViews();
			if (mContentSet != null)
			{
				TaskView detailView = (TaskView) inflater.inflate(R.layout.task_view, mContent, false);
				detailView.setModel(mModel);
				detailView.setValues(mContentSet);
				mContent.addView(detailView);
			}
		}
	}


	@Override
	public void onModelLoaded(Model model)
	{
		if (model == null)
		{
			return;
		}

		// the model has been loaded, now update the view
		mModel = model;
		updateView();

	}


	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		if (mTaskUri != null)
		{
			/*
			 * Unregister the observer for any previously shown task first.
			 */
			mAppContext.getContentResolver().unregisterContentObserver(mObserver);
		}
		outState.putParcelable(STATE_VALUES, mContentSet);
		outState.putParcelable(STATE_TASK_URI, mTaskUri);
	}


	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		/*
		 * Don't show any options if we don't have a task to show.
		 */
		if (mTaskUri != null)
		{
			inflater.inflate(R.menu.view_task_fragment_menu, menu);
		}
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.edit_task:
				// open editor for this task
				mCallback.onEditTask(mTaskUri);
				return true;
			case R.id.delete_task:
				Log.v(TAG, "removing task");
				// TODO: remove the task in a background task
				mContentSet.delete(mAppContext);
				mCallback.onDelete(mTaskUri);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}


	@Override
	public void onContentChanged(ContentSet contentSet, String key)
	{
		if (key == null && contentSet.containsKey(Tasks.ACCOUNT_TYPE))
		{
			// the ContentSet has been (re-)loaded, load the model of this task
			new AsyncModelLoader(mAppContext, this).execute(contentSet.getAsString(Tasks.ACCOUNT_TYPE));
		}
	}

	/**
	 * An observer for the tasks URI. It updates the task view whenever the URI changes.
	 */
	private final ContentObserver mObserver = new ContentObserver(null)
	{
		@Override
		public void onChange(boolean selfChange)
		{
			if (mContentSet != null)
			{
				// reload the task
				mContentSet.update(mAppContext, CONTENT_VALUE_MAPPER);
			}
		}
	};

}
