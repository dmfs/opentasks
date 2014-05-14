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

import org.dmfs.provider.tasks.TaskContract.Tasks;
import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.Model;
import org.dmfs.tasks.model.OnContentChangeListener;
import org.dmfs.tasks.utils.AsyncModelLoader;
import org.dmfs.tasks.utils.ContentValueMapper;
import org.dmfs.tasks.utils.OnModelLoadedListener;
import org.dmfs.tasks.widget.TaskView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


/**
 * A fragment representing a single Task detail screen. This fragment is either contained in a {@link TaskListActivity} in two-pane mode (on tablets) or in a
 * {@link ViewTaskActivity} on handsets.
 * 
 * @author Arjun Naik <arjun@arjunnaik.in>
 * @author Marten Gajda <marten@dmfs.org>
 */
public class ViewTaskFragment extends Fragment implements OnModelLoadedListener, OnContentChangeListener
{
	/**
	 * The key we use to store the {@link ContentSet} that holds the values we show.
	 */
	private static final String STATE_VALUES = "values";

	/**
	 * The key we use to store the {@link Uri} of the task we show.
	 */
	private static final String STATE_TASK_URI = "task_uri";

	/**
	 * The {@link ContentValueMapper} that knows how to map the values in a cursor to {@link ContentValues}.
	 */
	private static final ContentValueMapper CONTENT_VALUE_MAPPER = new ContentValueMapper()
		.addString(Tasks.ACCOUNT_TYPE, Tasks.ACCOUNT_NAME, Tasks.TITLE, Tasks.LOCATION, Tasks.DESCRIPTION, Tasks.GEO, Tasks.URL, Tasks.TZ, Tasks.DURATION,
			Tasks.LIST_NAME)
		.addInteger(Tasks.PRIORITY, Tasks.LIST_COLOR, Tasks.TASK_COLOR, Tasks.STATUS, Tasks.CLASSIFICATION, Tasks.PERCENT_COMPLETE, Tasks.IS_ALLDAY)
		.addLong(Tasks.LIST_ID, Tasks.DTSTART, Tasks.DUE, Tasks.COMPLETED, Tasks._ID);

	/**
	 * The {@link Uri} of the current task in the view.
	 */
	private Uri mTaskUri;

	/**
	 * The values of the current task.
	 */
	private ContentSet mContentSet;

	/**
	 * The view that contains the details.
	 */
	private ViewGroup mContent;

	/**
	 * The {@link Model} of the current task.
	 */
	private Model mModel;

	/**
	 * The application context.
	 */
	private Context mAppContext;

	/**
	 * The actual detail view. We store this direct reference to be able to clear it when the fragment gets detached.
	 */
	private TaskView mDetailView;

	/**
	 * A {@link Callback} to the activity.
	 */
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
		 *            The {@link Uri} of the deleted task. Note that the Uri is likely to be invalid at the time of calling this method.
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
	public void onDestroyView()
	{
		super.onDestroyView();
		if (mContent != null)
		{
			mContent.removeAllViews();
		}

		if (mTaskUri != null)
		{
			mAppContext.getContentResolver().unregisterContentObserver(mObserver);
		}

		if (mDetailView != null)
		{
			// remove values, to ensure all listeners get released
			mDetailView.setValues(null);
		}
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

			if (mContent != null && mContentSet != null)
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


	@Override
	public void onPause()
	{
		super.onPause();
		if (mContentSet.isUpdate())
		{
			Context activity = getActivity();
			mContentSet.persist(activity);
			Toast.makeText(activity, R.string.activity_edit_task_task_saved, Toast.LENGTH_SHORT).show();
		}
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

			if (mContentSet.isUpdate())
			{
				Context activity = getActivity();
				mContentSet.persist(activity);
				Toast.makeText(activity, R.string.activity_edit_task_task_saved, Toast.LENGTH_SHORT).show();
			}
		}

		Uri oldUri = mTaskUri;
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

		if ((oldUri == null) != (uri == null))
		{
			/*
			 * getActivity().invalidateOptionsMenu() doesn't work in Android 2.x so use the compat lib
			 */
			ActivityCompat.invalidateOptionsMenu(getActivity());
		}
	}


	/**
	 * Update the detail view with the current ContentSet. This removes any previous detail view and creates a new one if {@link #mContentSet} is not
	 * <code>null</code>.
	 */
	private void updateView()
	{
		Activity activity = getActivity();
		if (mContent != null && activity != null)
		{
			final LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			if (mDetailView != null)
			{
				// remove values, to ensure all listeners get released
				mDetailView.setValues(null);
			}

			mContent.removeAllViews();
			if (mContentSet != null)
			{
				mDetailView = (TaskView) inflater.inflate(R.layout.task_view, mContent, false);
				mDetailView.setModel(mModel);
				mDetailView.setValues(mContentSet);
				mContent.addView(mDetailView);
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
		int itemId = item.getItemId();
		if (itemId == R.id.edit_task)
		{
			// open editor for this task
			mCallback.onEditTask(mTaskUri);
			return true;
		}
		else if (itemId == R.id.delete_task)
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
						mContentSet.delete(mAppContext);
						mCallback.onDelete(mTaskUri);
					}
				}).setMessage(R.string.confirm_delete_message).create().show();
			return true;
		}
		else
		{
			return super.onOptionsItemSelected(item);
		}
	}


	@Override
	public void onContentLoaded(ContentSet contentSet)
	{
		if (contentSet.containsKey(Tasks.ACCOUNT_TYPE))
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


	@Override
	public void onContentChanged(ContentSet contentSet)
	{
		// nothing to do, the widgets will handle that themselves.
	}

}
