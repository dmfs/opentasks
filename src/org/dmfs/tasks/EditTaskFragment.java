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
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.dmfs.android.retentionmagic.SupportFragment;
import org.dmfs.android.retentionmagic.annotations.Parameter;
import org.dmfs.android.retentionmagic.annotations.Retain;
import org.dmfs.provider.tasks.TaskContract;
import org.dmfs.provider.tasks.TaskContract.TaskLists;
import org.dmfs.provider.tasks.TaskContract.Tasks;
import org.dmfs.tasks.model.CheckListItem;
import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.Model;
import org.dmfs.tasks.model.OnContentChangeListener;
import org.dmfs.tasks.model.Sources;
import org.dmfs.tasks.model.TaskFieldAdapters;
import org.dmfs.tasks.utils.ContentValueMapper;
import org.dmfs.tasks.utils.OnModelLoadedListener;
import org.dmfs.tasks.utils.TasksListCursorSpinnerAdapter;
import org.dmfs.tasks.widget.ListenableScrollView;
import org.dmfs.tasks.widget.ListenableScrollView.OnScrollListener;
import org.dmfs.tasks.widget.TaskEdit;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;


/**
 * Fragment to edit task details.
 * 
 * @author Arjun Naik <arjun@arjunnaik.in>
 * @author Marten Gajda <marten@dmfs.org>
 * @author Tobias Reinsch <tobias@dmfs.org>
 */

public class EditTaskFragment extends SupportFragment implements LoaderManager.LoaderCallbacks<Cursor>, OnModelLoadedListener, OnContentChangeListener,
	OnItemSelectedListener
{
	private static final String TAG = "TaskEditDetailFragment";

	public static final String PARAM_TASK_URI = "task_uri";
	public static final String PARAM_CONTENT_SET = "task_content_set";
	public static final String PARAM_ACCOUNT_TYPE = "task_account_type";

	public static final String LIST_LOADER_URI = "uri";
	public static final String LIST_LOADER_FILTER = "filter";

	public static final String LIST_LOADER_VISIBLE_LISTS_FILTER = TaskLists.SYNC_ENABLED + "=1";

	public static final String PREFERENCE_LAST_LIST = "pref_last_list_used_for_new_event";
	public static final String PREFERENCE_LAST_ACCOUNT_TYPE = "pref_last_account_type_used_for_new_event";

	/**
	 * A set of values that may affect the recurrence set of a task. If one of these values changes we have to submit all of them.
	 */
	private final static Set<String> RECURRENCE_VALUES = new HashSet<String>(Arrays.asList(new String[] { Tasks.DUE, Tasks.DTSTART, Tasks.TZ, Tasks.IS_ALLDAY,
		Tasks.RRULE, Tasks.RDATE, Tasks.EXDATE }));

	/**
	 * Projection into the task list.
	 */
	private final static String[] TASK_LIST_PROJECTION = new String[] { TaskContract.TaskListColumns._ID, TaskContract.TaskListColumns.LIST_NAME,
		TaskContract.TaskListSyncColumns.ACCOUNT_TYPE, TaskContract.TaskListSyncColumns.ACCOUNT_NAME, TaskContract.TaskListColumns.LIST_COLOR };

	/**
	 * This interface provides a convenient way to get column indices of {@link #TASK_LIST_PROJECTION} without any overhead.
	 */
	private interface TASK_LIST_PROJECTION_VALUES
	{
		public final static int id = 0;
		@SuppressWarnings("unused")
		public final static int list_name = 1;
		public final static int account_type = 2;
		@SuppressWarnings("unused")
		public final static int account_name = 3;
		@SuppressWarnings("unused")
		public final static int list_color = 4;
	}

	private static final String KEY_VALUES = "key_values";

	static final ContentValueMapper CONTENT_VALUE_MAPPER = new ContentValueMapper()
		.addString(Tasks.ACCOUNT_TYPE, Tasks.ACCOUNT_NAME, Tasks.TITLE, Tasks.LOCATION, Tasks.DESCRIPTION, Tasks.GEO, Tasks.URL, Tasks.TZ, Tasks.DURATION,
			Tasks.LIST_NAME)
		.addInteger(Tasks.PRIORITY, Tasks.LIST_COLOR, Tasks.TASK_COLOR, Tasks.STATUS, Tasks.CLASSIFICATION, Tasks.PERCENT_COMPLETE, Tasks.IS_ALLDAY,
			Tasks.IS_CLOSED, Tasks.PINNED).addLong(Tasks.LIST_ID, Tasks.DTSTART, Tasks.DUE, Tasks.COMPLETED, Tasks._ID);

	private boolean mAppForEdit = true;
	private TasksListCursorSpinnerAdapter mTaskListAdapter;

	private Uri mTaskUri;

	private ContentSet mValues;
	private ViewGroup mContent;
	private ViewGroup mHeader;
	private Model mModel;
	private Context mAppContext;
	private TaskEdit mEditor;
	private LinearLayout mTaskListBar;
	private Spinner mListSpinner;
	private String mAuthority;
	private View mColorBar;

	private boolean mRestored;

	private int mListColor = -1;
	private ListenableScrollView mRootView;

	@Parameter(key = PARAM_ACCOUNT_TYPE)
	private String mAccountType;

	/**
	 * The id of the list that was selected when we created the last task.
	 */
	@Retain(key = PREFERENCE_LAST_LIST, classNS = "", permanent = true)
	private long mSelectedList = -1;

	/**
	 * The last account type we added a task to.
	 */
	@Retain(key = PREFERENCE_LAST_ACCOUNT_TYPE, classNS = "", permanent = true)
	private String mLastAccountType = null;

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
		mAuthority = TaskContract.taskAuthority(activity);
		Bundle bundle = getArguments();

		// check for supplied task information from intent
		if (bundle.containsKey(PARAM_CONTENT_SET))
		{
			mValues = bundle.getParcelable(PARAM_CONTENT_SET);
			if (!mValues.isInsert())
			{
				mTaskUri = mValues.getUri();
			}
		}
		else
		{
			mTaskUri = bundle.getParcelable(PARAM_TASK_URI);
		}
		mAppContext = activity.getApplicationContext();

	}


	@SuppressWarnings("deprecation")
	@TargetApi(16)
	@Override
	public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		ListenableScrollView rootView = mRootView = (ListenableScrollView) inflater.inflate(R.layout.fragment_task_edit_detail, container, false);
		mContent = (ViewGroup) rootView.findViewById(R.id.content);
		mHeader = (ViewGroup) rootView.findViewById(R.id.header);
		mColorBar = rootView.findViewById(R.id.headercolorbar);

		mRestored = savedInstanceState != null;

		if (mColorBar != null)
		{
			mRootView.setOnScrollListener(new OnScrollListener()
			{

				@Override
				public void onScroll(int oldScrollY, int newScrollY)
				{
					int headerHeight = mTaskListBar.getMeasuredHeight();
					if (newScrollY <= headerHeight || oldScrollY <= headerHeight)
					{
						updateColor((float) newScrollY / headerHeight);
					}
				}
			});
		}
		mAppForEdit = !Tasks.getContentUri(mAuthority).equals(mTaskUri) && mTaskUri != null;

		mTaskListBar = (LinearLayout) inflater.inflate(R.layout.task_list_provider_bar, mHeader);
		mListSpinner = (Spinner) mTaskListBar.findViewById(R.id.task_list_spinner);

		mTaskListAdapter = new TasksListCursorSpinnerAdapter(mAppContext);
		mListSpinner.setAdapter(mTaskListAdapter);

		mListSpinner.setOnItemSelectedListener(this);

		if (android.os.Build.VERSION.SDK_INT < 11)
		{
			mListSpinner.setBackgroundDrawable(null);
		}

		if (mAppForEdit)
		{
			if (mTaskUri != null)
			{
				if (savedInstanceState == null && mValues == null)
				{
					mValues = new ContentSet(mTaskUri);
					mValues.addOnChangeListener(this, null, false);

					mValues.update(mAppContext, CONTENT_VALUE_MAPPER);
				}
				else
				{
					if (savedInstanceState != null)
					{
						mValues = savedInstanceState.getParcelable(KEY_VALUES);
						Sources.loadModelAsync(mAppContext, mValues.getAsString(Tasks.ACCOUNT_TYPE), this);
					}
					else
					{
						Sources.loadModelAsync(mAppContext, mValues.getAsString(Tasks.ACCOUNT_TYPE), this);
						// ensure we're using the latest values
						mValues.update(mAppContext, CONTENT_VALUE_MAPPER);
					}
					mListColor = TaskFieldAdapters.LIST_COLOR.get(mValues);
					// update the color of the action bar as soon as possible
					updateColor(0);
					setListUri(TaskLists.getContentUri(mAuthority), LIST_LOADER_VISIBLE_LISTS_FILTER);
				}
			}
		}
		else
		{
			if (savedInstanceState == null)
			{
				// create empty ContentSet if there was no ContentSet supplied
				if (mValues == null)
				{
					mValues = new ContentSet(Tasks.getContentUri(mAuthority));
					// ensure we start with the current time zone
					TaskFieldAdapters.TIMEZONE.set(mValues, TimeZone.getDefault());
				}
				else
				{
					// check id the provided content set contains a list and update the selected list if so
					Long listId = mValues.getAsLong(Tasks.LIST_ID);
					if (listId != null)
					{
						mSelectedList = listId;
					}
				}

				if (mLastAccountType != null)
				{
					Sources.loadModelAsync(mAppContext, mLastAccountType, this);
				}
			}
			else
			{
				mValues = savedInstanceState.getParcelable(KEY_VALUES);
				Sources.loadModelAsync(mAppContext, mLastAccountType, this);
			}
			setListUri(TaskLists.getContentUri(mAuthority), LIST_LOADER_VISIBLE_LISTS_FILTER);
		}

		return rootView;
	}


	@Override
	public void onPause()
	{
		// save values on rotation
		if (mEditor != null)
		{
			mEditor.updateValues();
		}

		super.onPause();
	};


	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		if (mEditor != null)
		{
			// remove values, to ensure all listeners get released
			mEditor.setValues(null);
		}
		if (mContent != null)
		{
			mContent.removeAllViews();
		}

		final Spinner listSpinner = (Spinner) mTaskListBar.findViewById(R.id.task_list_spinner);
		listSpinner.setOnItemSelectedListener(null);
		if (mValues != null)
		{
			mValues.removeOnChangeListener(this, null);
		}
	}


	private void updateView()
	{
		/*
		 * If the model loads very slowly then this function may be called after onDetach. In this case check if Activity is <code>null</code> and return if
		 * <code>true</code>. Also return if we don't have values or the values are still loading.
		 */
		Activity activity = getActivity();
		if (activity == null || mValues == null || mValues.isLoading())
		{
			return;
		}

		final LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		if (mEditor != null)
		{
			// remove values, to ensure all listeners get released
			mEditor.setValues(null);
		}
		mContent.removeAllViews();

		mEditor = (TaskEdit) inflater.inflate(R.layout.task_edit, mContent, false);
		mEditor.setModel(mModel);
		mEditor.setValues(mValues);
		mContent.addView(mEditor);

		// update focus to title
		String title = mValues.getAsString(Tasks.TITLE);

		// set focus to first element of the editor
		if (mEditor != null)
		{
			mEditor.requestFocus();
			if (title == null || title.length() == 0)
			{
				// open soft input as there is no title
				InputMethodManager imm = (InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				if (imm != null)
				{
					imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
				}
			}
		}

		updateColor((float) mRootView.getScrollY() / mTaskListBar.getMeasuredHeight());
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
			Toast.makeText(getActivity(), "Could not load Model", Toast.LENGTH_LONG).show();
			return;
		}
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
	public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putParcelable(KEY_VALUES, mValues);
	}


	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle)
	{
		return new CursorLoader(mAppContext, (Uri) bundle.getParcelable(LIST_LOADER_URI), TASK_LIST_PROJECTION, bundle.getString(LIST_LOADER_FILTER), null,
			null);
	}


	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)
	{
		mTaskListAdapter.changeCursor(cursor);
		if (cursor != null)
		{
			if (mAppForEdit)
			{
				mSelectedList = mValues.getAsLong(Tasks.LIST_ID);
			}
			// set the list that was used the last time the user created an event
			if (mSelectedList != -1)
			{
				// iterate over all lists and select the one that matches the given id
				cursor.moveToFirst();
				while (!cursor.isAfterLast())
				{
					Long listId = cursor.getLong(TASK_LIST_PROJECTION_VALUES.id);
					if (listId != null && listId == mSelectedList)
					{
						mListSpinner.setSelection(cursor.getPosition());
						break;
					}
					cursor.moveToNext();
				}
			}
		}
	}


	@Override
	public void onLoaderReset(Loader<Cursor> loader)
	{
		mTaskListAdapter.changeCursor(null);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		final int menuId = item.getItemId();
		Activity activity = getActivity();
		if (menuId == R.id.editor_action_save)
		{
			saveAndExit();
			return true;
		}
		else if (menuId == R.id.editor_action_cancel)
		{
			activity.setResult(Activity.RESULT_CANCELED);
			activity.finish();
			return true;
		}
		return false;
	}


	@Override
	public void onContentLoaded(ContentSet contentSet)
	{
		if (contentSet.containsKey(Tasks.ACCOUNT_TYPE))
		{
			mListColor = TaskFieldAdapters.LIST_COLOR.get(contentSet);
			updateColor((float) mRootView.getScrollY() / mTaskListBar.getMeasuredHeight());

			if (mAppForEdit)
			{
				Sources.loadModelAsync(mAppContext, contentSet.getAsString(Tasks.ACCOUNT_TYPE), EditTaskFragment.this);
			}

			/*
			 * Don't start the model loader here, let onItemSelected do that.
			 */
			setListUri(TaskLists.getContentUri(mAuthority), LIST_LOADER_VISIBLE_LISTS_FILTER);
		}

	}


	private void setListUri(Uri uri, String filter)
	{
		if (this.isAdded())
		{
			Bundle bundle = new Bundle();
			bundle.putParcelable(LIST_LOADER_URI, uri);
			bundle.putString(LIST_LOADER_FILTER, filter);

			getLoaderManager().restartLoader(-2, bundle, this);
		}
	}


	@Override
	public void onContentChanged(ContentSet contentSet)
	{
		// nothing to do
	}


	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int pos, long itemId)
	{
		Cursor c = (Cursor) arg0.getItemAtPosition(pos);

		String accountType = c.getString(TASK_LIST_PROJECTION_VALUES.account_type);
		mListColor = TaskFieldAdapters.LIST_COLOR.get(c);
		updateColor((float) mRootView.getScrollY() / mTaskListBar.getMeasuredHeight());

		if (mEditor != null)
		{
			mEditor.updateValues();
		}

		long listId = c.getLong(TASK_LIST_PROJECTION_VALUES.id);
		mValues.put(Tasks.LIST_ID, listId);
		mSelectedList = itemId;
		mLastAccountType = c.getString(TASK_LIST_PROJECTION_VALUES.account_type);

		if (mModel == null || !mModel.getAccountType().equals(accountType))
		{
			// the model changed, load the new model
			Sources.loadModelAsync(mAppContext, accountType, this);
		}
		else
		{
			postUpdateView();
		}
	}


	private static int darkenColor(int color)
	{
		float[] hsv = new float[3];
		Color.colorToHSV(color, hsv);
		hsv[2] = hsv[2] * 0.75f;
		color = Color.HSVToColor(hsv);
		return color;
	}


	public int mixColors(int col1, int col2)
	{
		int r1, g1, b1, r2, g2, b2;

		int a1 = Color.alpha(col1);

		r1 = Color.red(col1);
		g1 = Color.green(col1);
		b1 = Color.blue(col1);

		r2 = Color.red(col2);
		g2 = Color.green(col2);
		b2 = Color.blue(col2);

		int r3 = (r1 * a1 + r2 * (255 - a1)) / 255;
		int g3 = (g1 * a1 + g2 * (255 - a1)) / 255;
		int b3 = (b1 * a1 + b2 * (255 - a1)) / 255;

		return Color.rgb(r3, g3, b3);
	}


	private int getBlendColor(int baseColor, int targetColor, float alpha)
	{
		int r1, g1, b1, r3, g3, b3;

		if (alpha <= 0)
		{
			return targetColor;
		}
		else if (alpha > 254)
		{
			return targetColor;
		}

		r1 = Color.red(baseColor);
		g1 = Color.green(baseColor);
		b1 = Color.blue(baseColor);

		r3 = Color.red(targetColor);
		g3 = Color.green(targetColor);
		b3 = Color.blue(targetColor);

		int r2 = (int) Math.ceil((Math.max(0, r3 * 255 - r1 * (255 - alpha))) / alpha);
		int g2 = (int) Math.ceil((Math.max(0, g3 * 255 - g1 * (255 - alpha))) / alpha);
		int b2 = (int) Math.ceil((Math.max(0, b3 * 255 - b1 * (255 - alpha))) / alpha);

		return Color.argb((int) alpha, r2, g2, b2);
	}


	@SuppressLint("NewApi")
	private void updateColor(float percentage)
	{
		if (VERSION.SDK_INT >= 11)
		{
			if (mColorBar == null)
			{
				percentage = 1;
			}
			else
			{
				percentage = Math.max(0, Math.min(Float.isNaN(percentage) ? 0 : percentage, 1));
			}

			int newColor = getBlendColor(mListColor, darkenColor(mListColor), (int) ((0.5 + 0.5 * percentage) * 255));
			ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
			actionBar.setBackgroundDrawable(new ColorDrawable(newColor));

			// this is a workaround to ensure the new color is applied on all devices, some devices show a transparent ActionBar if we don't do that.
			actionBar.setDisplayShowTitleEnabled(false);
			actionBar.setDisplayShowTitleEnabled(true);

			if (VERSION.SDK_INT >= 21)
			{
				Window window = getActivity().getWindow();
				window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
				window.setStatusBarColor(mixColors(newColor, mListColor));
				// window.setNavigationBarColor(mixColors(newColor, mListColor));
			}
		}
		mTaskListBar.setBackgroundColor(mListColor);
		if (mColorBar != null)
		{
			mColorBar.setBackgroundColor(mListColor);
		}
	}


	@Override
	public void onNothingSelected(AdapterView<?> arg0)
	{
		// nothing to do here
	}


	/**
	 * Persist the current task (if anything has been edited) and close the editor.
	 */
	public void saveAndExit()
	{
		// TODO: put that in a background task
		Activity activity = getActivity();

		int resultCode = Activity.RESULT_CANCELED;
		Intent result = null;
		int toastId = -1;

		if (mEditor != null)
		{
			mEditor.updateValues();
		}

		if (mValues.isInsert() || mValues.isUpdate())
		{
			if (TextUtils.isEmpty(TaskFieldAdapters.TITLE.get(mValues)))
			{
				// there is no title, try to set one from the description or check list

				String description = TaskFieldAdapters.DESCRIPTION.get(mValues);
				if (description != null)
				{
					// remove spaces and empty lines
					description = description.trim();
				}

				if (!TextUtils.isEmpty(description))
				{
					// we have a description, use it to make up a title
					int eol = description.indexOf('\n');
					TaskFieldAdapters.TITLE.set(mValues, description.substring(0, eol > 0 ? eol : Math.min(description.length(), 100)));
				}
				else
				{
					// no description, try to find a non-empty checklist item
					List<CheckListItem> checklist = TaskFieldAdapters.CHECKLIST.get(mValues);
					if (checklist != null && checklist.size() > 0)
					{
						for (CheckListItem item : checklist)
						{
							String trimmedItem = item.text.trim();
							if (!TextUtils.isEmpty(trimmedItem))
							{
								TaskFieldAdapters.TITLE.set(mValues, trimmedItem);
								break;
							}
						}
					}
				}
			}

			if (!TextUtils.isEmpty(TaskFieldAdapters.TITLE.get(mValues)) || mValues.isUpdate())
			{

				if (mValues.updatesAnyKey(RECURRENCE_VALUES))
				{
					mValues.ensureUpdates(RECURRENCE_VALUES);
				}

				mTaskUri = mValues.persist(activity);

				// return proper result
				result = new Intent();
				result.setData(mTaskUri);
				resultCode = Activity.RESULT_OK;
				toastId = R.string.activity_edit_task_task_saved;
			}
			else
			{
				toastId = R.string.activity_edit_task_empty_task_not_saved;
			}
		}
		else
		{
			Log.i(TAG, "nothing to save");
		}

		if (toastId != -1)
		{
			Toast.makeText(activity, toastId, Toast.LENGTH_SHORT).show();
		}

		if (result != null)
		{
			activity.setResult(resultCode, result);
		}
		else
		{
			activity.setResult(resultCode);
		}

		activity.finish();
	}
}
