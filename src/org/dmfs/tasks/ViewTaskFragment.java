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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.dmfs.android.retentionmagic.SupportFragment;
import org.dmfs.android.retentionmagic.annotations.Parameter;
import org.dmfs.android.retentionmagic.annotations.Retain;
import org.dmfs.provider.tasks.TaskContract.Tasks;
import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.Model;
import org.dmfs.tasks.model.OnContentChangeListener;
import org.dmfs.tasks.model.Sources;
import org.dmfs.tasks.model.TaskFieldAdapters;
import org.dmfs.tasks.utils.ContentValueMapper;
import org.dmfs.tasks.utils.OnModelLoadedListener;
import org.dmfs.tasks.widget.ListenableScrollView;
import org.dmfs.tasks.widget.ListenableScrollView.OnScrollListener;
import org.dmfs.tasks.widget.TaskView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.ContentObserver;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;


/**
 * A fragment representing a single Task detail screen. This fragment is either contained in a {@link TaskListActivity} in two-pane mode (on tablets) or in a
 * {@link ViewTaskActivity} on handsets.
 * 
 * @author Arjun Naik <arjun@arjunnaik.in>
 * @author Marten Gajda <marten@dmfs.org>
 */
public class ViewTaskFragment extends SupportFragment implements OnModelLoadedListener, OnContentChangeListener
{
	private final static String ARG_URI = "uri";

	/**
	 * Edit action assigned to the floating action button.
	 */
	private final static int ACTION_EDIT = 1;

	/**
	 * Complete action assigned to the floating action button.
	 */
	private final static int ACTION_COMPLETE = 2;

	/**
	 * A set of values that may affect the recurrence set of a task. If one of these values changes we have to submit all of them.
	 */
	private final static Set<String> RECURRENCE_VALUES = new HashSet<String>(Arrays.asList(new String[] { Tasks.DUE, Tasks.DTSTART, Tasks.TZ, Tasks.IS_ALLDAY,
		Tasks.RRULE, Tasks.RDATE, Tasks.EXDATE }));

	/**
	 * The {@link ContentValueMapper} that knows how to map the values in a cursor to {@link ContentValues}.
	 */
	private static final ContentValueMapper CONTENT_VALUE_MAPPER = EditTaskFragment.CONTENT_VALUE_MAPPER;

	/**
	 * The {@link Uri} of the current task in the view.
	 */
	@Parameter(key = ARG_URI)
	@Retain
	private Uri mTaskUri;

	/**
	 * The values of the current task.
	 */
	@Retain
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

	private View mColorBar;
	private int mListColor;
	private View mActionButton;
	private ListenableScrollView mRootView;
	private int mOldStatus = -1;
	private boolean mRestored;

	/**
	 * The current action that's assigned to the floating action button.
	 */
	private int mActionButtonAction = ACTION_COMPLETE;

	/**
	 * A {@link Callback} to the activity.
	 */
	private Callback mCallback;

	/**
	 * A Runnable that updates the view.
	 */
	private Runnable mUpdateViewRunnable = new Runnable()
	{
		@Override
		public void run()
		{
			updateView();
		}
	};

	public interface Callback
	{
		/**
		 * This is called to instruct the Activity to call the editor for a specific task.
		 * 
		 * @param taskUri
		 *            The {@link Uri} of the task to edit.
		 * @param data
		 *            The task data that belongs to the {@link Uri}. This is purely an optimization and may be <code>null</code>.
		 */
		public void onEditTask(Uri taskUri, ContentSet data);


		/**
		 * This is called to inform the Activity that a task has been deleted.
		 * 
		 * @param taskUri
		 *            The {@link Uri} of the deleted task. Note that the Uri is likely to be invalid at the time of calling this method.
		 */
		public void onDelete(Uri taskUri);
	}


	public static ViewTaskFragment newInstance(Uri uri)
	{
		ViewTaskFragment result = new ViewTaskFragment();
		if (uri != null)
		{
			Bundle args = new Bundle();
			args.putParcelable(ARG_URI, uri);
			result.setArguments(args);
		}
		return result;
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
		// remove listener
		if (mContentSet != null)
		{
			mContentSet.removeOnChangeListener(this, null);
		}

		if (mTaskUri != null)
		{
			mAppContext.getContentResolver().unregisterContentObserver(mObserver);
		}

		if (mContent != null)
		{
			mContent.removeAllViews();
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
		ListenableScrollView rootView = mRootView = (ListenableScrollView) inflater.inflate(R.layout.fragment_task_view_detail, container, false);
		mContent = (ViewGroup) rootView.findViewById(R.id.content);
		mColorBar = rootView.findViewById(R.id.headercolorbar);

		mRestored = savedInstanceState != null;

		if (savedInstanceState != null)
		{
			if (mContent != null && mContentSet != null)
			{
				// register for content updates
				mContentSet.addOnChangeListener(this, null, true);

				// register observer
				if (mTaskUri != null)
				{
					mAppContext.getContentResolver().registerContentObserver(mTaskUri, false, mObserver);
				}
			}
		}
		else if (mTaskUri != null)
		{
			Uri uri = mTaskUri;
			// pretend we didn't load anything yet
			mTaskUri = null;
			loadUri(uri);
		}

		if (VERSION.SDK_INT >= 11 && mColorBar != null)
		{
			updateColor(0);
			mRootView.setOnScrollListener(new OnScrollListener()
			{

				@SuppressLint("NewApi")
				@Override
				public void onScroll(int oldScrollY, int newScrollY)
				{
					int headerHeight = ((ActionBarActivity) getActivity()).getSupportActionBar().getHeight();
					if (newScrollY <= headerHeight || oldScrollY <= headerHeight)
					{
						updateColor((float) newScrollY / headerHeight);
					}
				}
			});
		}

		return rootView;
	}


	@Override
	public void onPause()
	{
		super.onPause();
		persistTask();
	}


	private void persistTask()
	{
		Context activity = getActivity();
		if (mContentSet != null && mContentSet.isUpdate() && activity != null)
		{
			if (mContentSet.updatesAnyKey(RECURRENCE_VALUES))
			{
				mContentSet.ensureUpdates(RECURRENCE_VALUES);
			}
			mContentSet.persist(activity);
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
			persistTask();
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

			mActionButton = mDetailView.findViewById(R.id.action_button);

			if (mActionButton != null)
			{
				mActionButton.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						switch (mActionButtonAction)
						{
							case ACTION_COMPLETE:
							{
								completeTask();
								break;
							}
							case ACTION_EDIT:
							{
								mCallback.onEditTask(mTaskUri, mContentSet);
								break;
							}
						}
					}
				});
				if (TaskFieldAdapters.IS_CLOSED.get(mContentSet))
				{
					((ImageView) mActionButton.findViewById(android.R.id.icon)).setImageResource(R.drawable.content_edit);
					mActionButtonAction = ACTION_EDIT;
				}
				else
				{
					mActionButtonAction = ACTION_COMPLETE;
				}
			}

			if (mColorBar != null)
			{
				updateColor((float) mRootView.getScrollY() / mColorBar.getMeasuredHeight());
			}
		}
	}


	/**
	 * Update the view. This doesn't call {@link #updateView()} right away, instead it posts it.
	 */
	private void postUpdateView()
	{
		if (mContent != null)
		{
			mContent.post(mUpdateViewRunnable);
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
		if (mModel == null || !mModel.equals(model))
		{
			mModel = model;
			if (mRestored)
			{
				// The fragment has been restored from a saved state
				// We need to wait until all views are ready, otherwise the new data might get lost and all widgets show their default state (and no data).
				postUpdateView();
			}
			else
			{
				// This is the initial update. Just go ahead and update the view right away to ensure the activity comes up with a filled form.
				updateView();
			}
		}
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

			if (mContentSet != null)
			{
				Integer status = TaskFieldAdapters.STATUS.get(mContentSet);
				if (status != null)
				{
					mOldStatus = status;
				}
				if (TaskFieldAdapters.IS_CLOSED.get(mContentSet) || status != null && status == Tasks.STATUS_COMPLETED)
				{
					// we disable the edit option, because the task is completed and the action button shows the edit option.
					MenuItem item = menu.findItem(R.id.edit_task);
					item.setEnabled(false);
					item.setVisible(false);
				}
			}
		}
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		int itemId = item.getItemId();
		if (itemId == R.id.edit_task)
		{
			// open editor for this task
			mCallback.onEditTask(mTaskUri, mContentSet);
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
		else if (itemId == R.id.complete_task)
		{
			completeTask();
			return true;
		}
		else
		{
			return super.onOptionsItemSelected(item);
		}
	}


	/**
	 * Completes the current task.
	 */
	private void completeTask()
	{
		TaskFieldAdapters.STATUS.set(mContentSet, Tasks.STATUS_COMPLETED);
		persistTask();
		Toast.makeText(mAppContext, getString(R.string.toast_task_completed, TaskFieldAdapters.TITLE.get(mContentSet)), Toast.LENGTH_SHORT).show();
		// at present we just handle it like deletion, i.e. close the task in phone mode, do nothing in tablet mode
		mCallback.onDelete(mTaskUri);
	}


	public static int darkenColor(int color)
	{
		float[] hsv = new float[3];
		Color.colorToHSV(color, hsv);
		if (hsv[2] > 0.8)
		{
			hsv[2] = 0.8f + (hsv[2] - 0.8f) * 0.5f;
			color = Color.HSVToColor(hsv);
		}
		return color;
	}


	public static int darkenColor2(int color)
	{
		float[] hsv = new float[3];
		Color.colorToHSV(color, hsv);
		hsv[2] = hsv[2] * 0.75f;
		color = Color.HSVToColor(hsv);
		return color;
	}


	/**
	 * Calculates the resulting color when you put col1 over col2.
	 */
	private int blendColors(int col1, int col2)
	{
		int a1 = Color.alpha(col1);

		int r1 = Color.red(col1);
		int g1 = Color.green(col1);
		int b1 = Color.blue(col1);

		int r2 = Color.red(col2);
		int g2 = Color.green(col2);
		int b2 = Color.blue(col2);

		int r3 = (r1 * a1 + r2 * (255 - a1)) / 255;
		int g3 = (g1 * a1 + g2 * (255 - a1)) / 255;
		int b3 = (b1 * a1 + b2 * (255 - a1)) / 255;

		return Color.rgb(r3, g3, b3);
	}


	@SuppressLint("NewApi")
	private void updateColor(float percentage)
	{
		float[] hsv = new float[3];
		Color.colorToHSV(mListColor, hsv);

		if (VERSION.SDK_INT >= 11 && mColorBar != null)
		{
			percentage = Math.max(0, Math.min(Float.isNaN(percentage) ? 0 : percentage, 1));
			percentage = (float) Math.pow(percentage, 1.5);

			int newColor = darkenColor2(mListColor);

			hsv[2] *= (0.5 + 0.25 * percentage);

			// int newColor = Color.HSVToColor((int) ((0.5 + 0.5 * percentage) * 255), hsv);
			ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
			actionBar.setBackgroundDrawable(new ColorDrawable((newColor & 0x00ffffff) | ((int) (percentage * 255) << 24)));

			// this is a workaround to ensure the new color is applied on all devices, some devices show a transparent ActionBar if we don't do that.
			actionBar.setDisplayShowTitleEnabled(true);
			actionBar.setDisplayShowTitleEnabled(false);

			mColorBar.setBackgroundColor(mListColor);

			if (VERSION.SDK_INT >= 21)
			{
				Window window = getActivity().getWindow();
				window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
				window.setStatusBarColor(blendColors((newColor & 0x00ffffff) | ((int) (percentage * 255) << 24), mListColor));
				window.setNavigationBarColor(newColor);
			}
		}

		if (mActionButton != null)
		{
			// adjust color of action button
			if (hsv[0] > 70 && hsv[0] < 170 && hsv[2] < 0.62)
			{
				mActionButton.setBackgroundResource(R.drawable.bg_actionbutton_light_green);
			}
			else
			{
				mActionButton.setBackgroundResource(R.drawable.bg_actionbutton);
			}
		}
	}


	@SuppressLint("NewApi")
	@Override
	public void onContentLoaded(ContentSet contentSet)
	{
		if (contentSet.containsKey(Tasks.ACCOUNT_TYPE))
		{
			mListColor = TaskFieldAdapters.LIST_COLOR.get(contentSet);

			if (VERSION.SDK_INT >= 11)
			{
				updateColor((float) mRootView.getScrollY() / ((ActionBarActivity) getActivity()).getSupportActionBar().getHeight());
			}

			Activity activity = getActivity();
			int newStatus = TaskFieldAdapters.STATUS.get(contentSet);
			if (VERSION.SDK_INT >= 11 && activity != null
				&& (mOldStatus != -1 && mOldStatus != newStatus || mOldStatus == -1 && TaskFieldAdapters.IS_CLOSED.get(mContentSet)))
			{
				// new need to update the options menu, because the status of the task has changed
				activity.invalidateOptionsMenu();

			}

			mOldStatus = newStatus;

			if (mModel == null || !TextUtils.equals(mModel.getAccountType(), contentSet.getAsString(Tasks.ACCOUNT_TYPE)))
			{
				Sources.loadModelAsync(mAppContext, contentSet.getAsString(Tasks.ACCOUNT_TYPE), this);
			}
			else
			{
				// the model didn't change, just update the view
				postUpdateView();
			}
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
	}
}
