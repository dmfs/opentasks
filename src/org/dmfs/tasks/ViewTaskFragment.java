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
import org.dmfs.tasks.notification.TaskNotificationHandler;
import org.dmfs.tasks.utils.ContentValueMapper;
import org.dmfs.tasks.utils.OnModelLoadedListener;
import org.dmfs.tasks.widget.TaskView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.ColorStateList;
import android.database.ContentObserver;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.AppBarLayout.LayoutParams;
import android.support.design.widget.AppBarLayout.OnOffsetChangedListener;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.Toolbar.OnMenuItemClickListener;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;


/**
 * A fragment representing a single Task detail screen. This fragment is either contained in a {@link TaskListActivity} in two-pane mode (on tablets) or in a
 * {@link ViewTaskActivity} on handsets.
 * 
 * @author Arjun Naik <arjun@arjunnaik.in>
 * @author Marten Gajda <marten@dmfs.org>
 */
public class ViewTaskFragment extends SupportFragment implements OnModelLoadedListener, OnContentChangeListener, OnMenuItemClickListener,
	OnOffsetChangedListener, OnGlobalLayoutListener
{
	private final static String ARG_URI = "uri";

	/**
	 * A set of values that may affect the recurrence set of a task. If one of these values changes we have to submit all of them.
	 */
	private final static Set<String> RECURRENCE_VALUES = new HashSet<String>(Arrays.asList(new String[] { Tasks.DUE, Tasks.DTSTART, Tasks.TZ, Tasks.IS_ALLDAY,
		Tasks.RRULE, Tasks.RDATE, Tasks.EXDATE }));

	/**
	 * The {@link ContentValueMapper} that knows how to map the values in a cursor to {@link ContentValues}.
	 */
	private static final ContentValueMapper CONTENT_VALUE_MAPPER = EditTaskFragment.CONTENT_VALUE_MAPPER;

	private static final float PERCENTAGE_TO_HIDE_TITLE_DETAILS = 0.3f;
	private static final int ALPHA_ANIMATIONS_DURATION = 200;

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

	private int mListColor;
	private int mOldStatus = -1;
	private boolean mPinned = false;
	private boolean mRestored;
	private NestedScrollView mScrollView;
	private AppBarLayout mAppBar;
	private Toolbar mToolBar;
	private View mRootView;

	private int mAppBarOffset = 0;

	private FloatingActionButton mFloatingActionButton;

	/**
	 * A {@link Callback} to the activity.
	 */
	private Callback mCallback;

	private boolean mShowFloatingActionButton = false;

	private boolean mIsTheTitleContainerVisible = true;

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


		/**
		 * Notifies the listener about the list color of the current task.
		 * 
		 * @param color
		 *            The color.
		 */
		public void updateColor(int color);
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
		mShowFloatingActionButton = getResources().getBoolean(R.bool.opentasks_enabled_detail_view_fab);

		mRootView = inflater.inflate(R.layout.fragment_task_view_detail, container, false);
		mContent = (ViewGroup) mRootView.findViewById(R.id.content);
		mAppBar = (AppBarLayout) mRootView.findViewById(R.id.appbar);
		mToolBar = (Toolbar) mRootView.findViewById(R.id.toolbar);
		mToolBar.setOnMenuItemClickListener(this);
		mToolBar.setTitle("");
		mAppBar.addOnOffsetChangedListener(this);

		animate(mToolBar.findViewById(R.id.toolbar_title), 0, View.INVISIBLE);

		mFloatingActionButton = (FloatingActionButton) mRootView.findViewById(R.id.floating_action_button);
		showFloatingActionButton(false);
		mFloatingActionButton.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				completeTask();
			}
		});

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

		mScrollView = (NestedScrollView) mRootView.findViewById(R.id.scrollView);
		mScrollView.getViewTreeObserver().addOnGlobalLayoutListener(this);
		return mRootView;
	}


	@Override
	public void onPause()
	{
		super.onPause();
		persistTask();
	}


	private void persistTask()
	{
		Activity activity = getActivity();
		if (mContentSet != null && activity != null)
		{
			if (mDetailView != null)
			{
				mDetailView.updateValues();
			}

			if (mContentSet.isUpdate())
			{
				mContentSet.persist(activity);
				ActivityCompat.invalidateOptionsMenu(activity);
			}
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
		showFloatingActionButton(false);

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

		mAppBar.setExpanded(true, false);
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

				TaskView mToolbarInfo = (TaskView) mAppBar.findViewById(R.id.toolbar_content);
				if (mToolbarInfo != null)
				{
					Model minModel = Sources.getInstance(activity).getMinimalModel(TaskFieldAdapters.ACCOUNT_TYPE.get(mContentSet));
					mToolbarInfo.setModel(minModel);
					mToolbarInfo.setValues(null);
					mToolbarInfo.setValues(mContentSet);
				}
				((TextView) mToolBar.findViewById(R.id.toolbar_title)).setText(TaskFieldAdapters.TITLE.get(mContentSet));
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
			menu = mToolBar.getMenu();
			menu.clear();

			inflater.inflate(R.menu.view_task_fragment_menu, menu);

			if (mContentSet != null)
			{
				Integer status = TaskFieldAdapters.STATUS.get(mContentSet);
				if (status != null)
				{
					mOldStatus = status;
				}

				if (!mShowFloatingActionButton && !(TaskFieldAdapters.IS_CLOSED.get(mContentSet) || status != null && status == Tasks.STATUS_COMPLETED))
				{
					MenuItem item = menu.findItem(R.id.complete_task);
					item.setEnabled(true);
					item.setVisible(true);
				}

				// check pinned status
				if (TaskFieldAdapters.PINNED.get(mContentSet))
				{
					// we disable the edit option, because the task is completed and the action button shows the edit option.
					MenuItem item = menu.findItem(R.id.pin_task);
					item.setIcon(R.drawable.ic_pin_off_white_24dp);
				}
				else
				{
					MenuItem item = menu.findItem(R.id.pin_task);
					item.setIcon(R.drawable.ic_pin_white_24dp);
				}
			}
		}
	}


	@Override
	public boolean onMenuItemClick(MenuItem item)
	{
		return onOptionsItemSelected(item);
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
		else if (itemId == R.id.pin_task)
		{
			if (TaskFieldAdapters.PINNED.get(mContentSet))
			{
				item.setIcon(R.drawable.ic_pin_white_24dp);
				TaskNotificationHandler.unpinTask(mAppContext, mContentSet);
			}
			else
			{
				item.setIcon(R.drawable.ic_pin_off_white_24dp);
				TaskNotificationHandler.pinTask(mAppContext, mContentSet);
			}
			persistTask();
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
		TaskFieldAdapters.PINNED.set(mContentSet, false);
		persistTask();
		Snackbar.make(getActivity().getWindow().getDecorView(), getString(R.string.toast_task_completed, TaskFieldAdapters.TITLE.get(mContentSet)),
			Snackbar.LENGTH_SHORT).show();
		// at present we just handle it like deletion, i.e. close the task in phone mode, do nothing in tablet mode
		mCallback.onDelete(mTaskUri);
		if (mShowFloatingActionButton)
		{
			// hide fab in two pane mode
			mFloatingActionButton.hide();
		}
	}


	@SuppressLint("NewApi")
	private void updateColor()
	{
		mAppBar.setBackgroundColor(mListColor);

		if (mShowFloatingActionButton && mFloatingActionButton.getVisibility() == View.VISIBLE)
		{
			// the FAB gets a slightly lighter color to stand out a bit more. If it's too light, we darken it instead.
			float[] hsv = new float[3];
			Color.colorToHSV(mListColor, hsv);
			if (hsv[2] * (1 - hsv[1]) < 0.4)
			{
				hsv[2] *= 1.2;
			}
			else
			{
				hsv[2] /= 1.2;
			}
			mFloatingActionButton.setBackgroundTintList(new ColorStateList(new int[][] { new int[] { 0 } }, new int[] { Color.HSVToColor(hsv) }));
		}
	}


	@SuppressLint("NewApi")
	@Override
	public void onContentLoaded(ContentSet contentSet)
	{
		if (contentSet.containsKey(Tasks.ACCOUNT_TYPE))
		{
			mListColor = TaskFieldAdapters.LIST_COLOR.get(contentSet);
			((Callback) getActivity()).updateColor(mListColor);

			if (VERSION.SDK_INT >= 11)
			{
				updateColor();
			}

			Activity activity = getActivity();
			int newStatus = TaskFieldAdapters.STATUS.get(contentSet);
			boolean newPinned = TaskFieldAdapters.PINNED.get(contentSet);
			if (activity != null && (hasNewStatus(newStatus) || pinChanged(newPinned)))
			{
				// new need to update the options menu, because the status of the task has changed
				ActivityCompat.invalidateOptionsMenu(activity);
			}

			mPinned = newPinned;
			mOldStatus = newStatus;

			if (mShowFloatingActionButton)
			{
				if (!TaskFieldAdapters.IS_CLOSED.get(contentSet))
				{
					showFloatingActionButton(true);
					mFloatingActionButton.show();
				}
				else
				{
					if (mFloatingActionButton.getVisibility() == View.VISIBLE)
					{
						mFloatingActionButton.hide();
					}
				}
			}

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


	private boolean hasNewStatus(int newStatus)
	{
		return (mOldStatus != -1 && mOldStatus != newStatus || mOldStatus == -1 && TaskFieldAdapters.IS_CLOSED.get(mContentSet));
	}


	private boolean pinChanged(boolean newPinned)
	{
		return !(mPinned == newPinned);
	}


	@SuppressLint("NewApi")
	@Override
	public void onOffsetChanged(AppBarLayout appBarLayout, int offset)
	{
		mAppBarOffset = offset;
		int maxScroll = appBarLayout.getTotalScrollRange();
		float percentage = (float) Math.abs(offset) / (float) maxScroll;

		handleAlphaOnTitle(percentage);

		if (mIsTheTitleContainerVisible && Build.VERSION.SDK_INT >= 11)
		{
			mAppBar.findViewById(R.id.toolbar_content).setAlpha(1 - percentage);
		}
	}


	private void handleAlphaOnTitle(float percentage)
	{
		if (percentage >= PERCENTAGE_TO_HIDE_TITLE_DETAILS)
		{
			if (mIsTheTitleContainerVisible)
			{
				animate(mAppBar.findViewById(R.id.toolbar_content), ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
				animate(mToolBar.findViewById(R.id.toolbar_title), ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
				mIsTheTitleContainerVisible = false;
			}
		}
		else
		{
			if (!mIsTheTitleContainerVisible)
			{
				animate(mToolBar.findViewById(R.id.toolbar_title), ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
				animate(mAppBar.findViewById(R.id.toolbar_content), ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
				mIsTheTitleContainerVisible = true;
			}
		}
	}


	private void animate(View v, int duration, int visibility)
	{
		AlphaAnimation alphaAnimation = (visibility == View.VISIBLE) ? new AlphaAnimation(0f, 1f) : new AlphaAnimation(1f, 0f);
		alphaAnimation.setDuration(duration);
		alphaAnimation.setFillAfter(true);
		v.startAnimation(alphaAnimation);
	}


	/**
	 * Set the toolbar of this fragment (if any), as the ActionBar if the given Activity.
	 * 
	 * @param activty
	 *            an {@link AppCompatActivity}.
	 */
	public void setupToolbarAsActionbar(android.support.v7.app.AppCompatActivity activty)
	{
		if (mToolBar == null)
		{
			return;
		}

		activty.setSupportActionBar(mToolBar);
		if (android.os.Build.VERSION.SDK_INT >= 11)
		{
			activty.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}


	/**
	 * Shows or hides the floating action button.
	 * 
	 * @param show
	 *            <code>true</code> to show the FloatingActionButton, <code>false</code> to hide it.
	 */
	@SuppressLint("NewApi")
	private void showFloatingActionButton(final boolean show)
	{
		CoordinatorLayout.LayoutParams p = (CoordinatorLayout.LayoutParams) mFloatingActionButton.getLayoutParams();
		if (show)
		{
			p.setAnchorId(R.id.appbar);
			mFloatingActionButton.setLayoutParams(p);
			mFloatingActionButton.setVisibility(View.VISIBLE);
			// make sure the FAB has the right color
			updateColor();
		}
		else
		{
			p.setAnchorId(View.NO_ID);
			mFloatingActionButton.setLayoutParams(p);
			mFloatingActionButton.setVisibility(View.GONE);
		}
	}


	@Override
	public void onGlobalLayout()
	{
		if (Build.VERSION.SDK_INT < 21)
		{
			// disabling scroll in code seems to be broken on some Android 4.x devices. For now we just disable this function.
			return;
		}

		if (mScrollView.getHeight() == 0 || mContent.getHeight() == 0)
		{
			return;
		}

		// check if we actually need scrolling and disable toolbar collapsing if not
		CollapsingToolbarLayout toolbar = (CollapsingToolbarLayout) mAppBar.getChildAt(0);
		AppBarLayout.LayoutParams p = (LayoutParams) toolbar.getLayoutParams();

		int appBarHeight = mAppBar.getHeight();
		int rootHeight = mRootView.getHeight();

		// we need collapsing of the content is larger than the space under the app bar or if we're already collapsed to some degree
		boolean needScroll = rootHeight - appBarHeight <= mContent.getHeight() || mAppBarOffset != 0;
		boolean hasScroll = (p.getScrollFlags() & AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL) != 0;

		if (hasScroll != needScroll)
		{
			// update scroll flags accordingly
			p.setScrollFlags(needScroll ? AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED
				| AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP : 0);
			toolbar.setLayoutParams(p);
		}
	}
}
