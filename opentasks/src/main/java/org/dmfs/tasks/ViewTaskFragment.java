/*
 * Copyright 2017 dmfs GmbH
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
 */

package org.dmfs.tasks;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.res.ColorStateList;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.dmfs.android.bolts.color.Color;
import org.dmfs.android.bolts.color.elementary.ValueColor;
import org.dmfs.android.contentpal.Operation;
import org.dmfs.android.contentpal.operations.BulkDelete;
import org.dmfs.android.contentpal.predicates.AnyOf;
import org.dmfs.android.contentpal.predicates.EqArg;
import org.dmfs.android.contentpal.predicates.IdIn;
import org.dmfs.android.contentpal.transactions.BaseTransaction;
import org.dmfs.android.retentionmagic.SupportFragment;
import org.dmfs.android.retentionmagic.annotations.Parameter;
import org.dmfs.android.retentionmagic.annotations.Retain;
import org.dmfs.jems.iterable.adapters.PresentValues;
import org.dmfs.jems.optional.elementary.NullSafe;
import org.dmfs.jems.single.combined.Backed;
import org.dmfs.opentaskspal.tables.InstanceTable;
import org.dmfs.opentaskspal.tables.TasksTable;
import org.dmfs.tasks.contract.TaskContract;
import org.dmfs.tasks.contract.TaskContract.Tasks;
import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.Model;
import org.dmfs.tasks.model.OnContentChangeListener;
import org.dmfs.tasks.model.Sources;
import org.dmfs.tasks.model.TaskFieldAdapters;
import org.dmfs.tasks.notification.ActionService;
import org.dmfs.tasks.share.ShareIntentFactory;
import org.dmfs.tasks.utils.ContentValueMapper;
import org.dmfs.tasks.utils.OnModelLoadedListener;
import org.dmfs.tasks.utils.SafeFragmentUiRunnable;
import org.dmfs.tasks.utils.colors.AdjustedForFab;
import org.dmfs.tasks.widget.TaskView;

import java.util.concurrent.atomic.AtomicReference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.view.MenuItemCompat;


/**
 * A fragment representing a single Task detail screen. This fragment is either contained in a {@link TaskListActivity} in two-pane mode (on tablets) or in a
 * {@link ViewTaskActivity} on handsets.
 *
 * @author Arjun Naik <arjun@arjunnaik.in>
 * @author Marten Gajda <marten@dmfs.org>
 */
public class ViewTaskFragment extends SupportFragment
        implements OnModelLoadedListener, OnContentChangeListener, OnMenuItemClickListener, OnOffsetChangedListener
{
    private final static String ARG_URI = "uri";
    private static final String ARG_STARTING_COLOR = "starting_color";

    /**
     * The {@link ContentValueMapper} that knows how to map the values in a cursor to {@link ContentValues}.
     */

    private static final ContentValueMapper CONTENT_VALUE_MAPPER = new ContentValueMapper()
            .addString(Tasks.ACCOUNT_TYPE, Tasks.ACCOUNT_NAME, Tasks.TITLE, Tasks.LOCATION, Tasks.DESCRIPTION, Tasks.GEO, Tasks.URL, Tasks.TZ, Tasks.DURATION,
                    Tasks.LIST_NAME, Tasks.RRULE, Tasks.RDATE)
            .addInteger(Tasks.PRIORITY, Tasks.LIST_COLOR, Tasks.TASK_COLOR, Tasks.STATUS, Tasks.CLASSIFICATION, Tasks.PERCENT_COMPLETE, Tasks.IS_ALLDAY,
                    Tasks.IS_CLOSED, Tasks.PINNED, TaskContract.Instances.IS_RECURRING)
            .addLong(Tasks.LIST_ID, Tasks.DTSTART, Tasks.DUE, Tasks.COMPLETED, Tasks._ID, Tasks.ORIGINAL_INSTANCE_ID, TaskContract.Instances.TASK_ID);

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


    public interface Callback
    {
        /**
         * Called when user pressed 'edit' for the task.
         *
         * @param taskUri
         *         The {@link Uri} of the task to edit.
         * @param data
         *         The task data that belongs to the {@link Uri}. This is purely an optimization and may be <code>null</code>.
         */
        void onTaskEditRequested(@NonNull Uri taskUri, @Nullable ContentSet data);

        /**
         * Called when the task has been deleted by the user.
         */
        void onTaskDeleted(@NonNull Uri taskUri);

        /**
         * Called when the task has been marked completed by the user.
         */
        void onTaskCompleted(@NonNull Uri taskUri);

        /**
         * Notifies the listener about the list color of the current task.
         *
         * @param color
         *         The color.
         */
        void onListColorLoaded(@NonNull Color color);
    }


    /**
     * @param taskContentUri
     *         the content uri of the task to display
     * @param startingColor
     *         The color that is used for the toolbars until the actual task color is loaded. (If available provide the actual task list color, otherwise the
     *         primary color.)
     */
    public static ViewTaskFragment newInstance(@NonNull Uri taskContentUri, @NonNull Color startingColor)
    {
        ViewTaskFragment fragment = new ViewTaskFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_URI, taskContentUri);
        args.putInt(ARG_STARTING_COLOR, startingColor.argb());
        fragment.setArguments(args);
        return fragment;
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

        if (mDetailView != null)
        {
            // remove values, to ensure all listeners get released
            mDetailView.setValues(null);
        }

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        mShowFloatingActionButton = !getResources().getBoolean(R.bool.has_two_panes);

        mRootView = inflater.inflate(R.layout.fragment_task_view_detail, container, false);
        mContent = (ViewGroup) mRootView.findViewById(R.id.content);
        mDetailView = (TaskView) inflater.inflate(R.layout.task_view, mContent, false);
        mContent.addView(mDetailView);
        mAppBar = (AppBarLayout) mRootView.findViewById(R.id.appbar);
        mToolBar = (Toolbar) mRootView.findViewById(R.id.toolbar);
        mToolBar.setOnMenuItemClickListener(this);
        mToolBar.setTitle("");
        mAppBar.addOnOffsetChangedListener(this);

        animate(mToolBar.findViewById(R.id.toolbar_title), 0, View.INVISIBLE);

        mFloatingActionButton = (FloatingActionButton) mRootView.findViewById(R.id.floating_action_button);
        showFloatingActionButton(false);
        mFloatingActionButton.setOnClickListener(v -> completeTask());

        // Update the toolbar color until the actual is loaded for the task

        mListColor = new ValueColor(getArguments().getInt(ARG_STARTING_COLOR)).argb();
        updateColor();

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


    /*
       TODO Refactor, simplify ViewTaskFragment now that it is only for displaying a single task once.
       Ticket for this: https://github.com/dmfs/opentasks/issues/628

       Earlier this Fragment was responsible for displaying no task (empty content)
       and also updating itself to show a newly selected one, using this loadUri() method which was public at the time.
       After refactorings, the Fragment is now only responsible to load an existing task once, for the task uri that is received in the args.
       As a result this class can now be simplified, for example potentially removing all uri == null checks.
     */


    /**
     * Load the task with the given {@link Uri} in the detail view.
     * <p>
     * At present only Task Uris are supported.
     * </p>
     * TODO: add support for instance Uris.
     *
     * @param uri
     *         The {@link Uri} of the task to show.
     */
    private void loadUri(Uri uri)
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
            mContent.post(new SafeFragmentUiRunnable(this, this::updateView));
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
        mDetailView.updateValues();

        int itemId = item.getItemId();
        if (itemId == R.id.edit_task)
        {
            // open editor for this task
            mCallback.onTaskEditRequested(mTaskUri, mContentSet);
            return true;
        }
        else if (itemId == R.id.delete_task)
        {
            long originalInstanceId = new Backed<>(TaskFieldAdapters.ORIGINAL_INSTANCE_ID.get(mContentSet), () ->
                    Long.valueOf(TaskFieldAdapters.INSTANCE_TASK_ID.get(mContentSet))).value();
            boolean isRecurring = TaskFieldAdapters.IS_RECURRING_INSTANCE.get(mContentSet);
            AtomicReference<Operation<?>> operation = new AtomicReference<>(
                    new BulkDelete<>(
                            new InstanceTable(mTaskUri.getAuthority()),
                            new IdIn<>(mTaskUri.getLastPathSegment())));
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setCancelable(true)
                    .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                        // nothing to do here
                    })
                    .setTitle(isRecurring ? R.string.opentasks_task_details_delete_recurring_task : R.string.confirm_delete_title)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        if (mContentSet != null)
                        {
                            // TODO: remove the task in a background task
                            try
                            {
                                new BaseTransaction()
                                        .with(new PresentValues<>(new NullSafe<>(operation.get())))
                                        .commit(getContext().getContentResolver().acquireContentProviderClient(mTaskUri));
                            }
                            catch (RemoteException | OperationApplicationException e)
                            {
                                Log.e(ViewTaskFragment.class.getSimpleName(), "Unable to delete task ", e);
                            }

                            mCallback.onTaskDeleted(mTaskUri);
                            mTaskUri = null;
                        }
                    });
            if (isRecurring)
            {
                builder.setSingleChoiceItems(
                        new CharSequence[] {
                                getString(R.string.opentasks_task_details_delete_this_task),
                                getString(R.string.opentasks_task_details_delete_all_tasks)
                        },
                        0,
                        (dialog, which) -> {
                            switch (which)
                            {
                                case 0:
                                    operation.set(new BulkDelete<>(
                                            new InstanceTable(mTaskUri.getAuthority()),
                                            new IdIn<>(mTaskUri.getLastPathSegment())));
                                case 1:
                                    operation.set(new BulkDelete<>(
                                            new TasksTable(mTaskUri.getAuthority()),
                                            new AnyOf<>(
                                                    new IdIn<>(originalInstanceId),
                                                    new EqArg<>(Tasks.ORIGINAL_INSTANCE_ID, originalInstanceId))));

                            }
                        });
            }
            else
            {
                builder.setMessage(R.string.confirm_delete_message);
            }
            builder.create().show();

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
                ActionService.startAction(getActivity(), ActionService.ACTION_UNPIN, mTaskUri);
            }
            else
            {
                item.setIcon(R.drawable.ic_pin_off_white_24dp);
                ActionService.startAction(getActivity(), ActionService.ACTION_PIN_TASK, mTaskUri);
            }
            persistTask();
            return true;
        }
        else if (itemId == R.id.opentasks_send_task)

        {
            setSendMenuIntent();
            return false;
        }
        else

        {
            return super.onOptionsItemSelected(item);
        }

    }


    private void setSendMenuIntent()
    {
        if (mContentSet != null && mModel != null && mToolBar != null && mToolBar.getMenu() != null)
        {
            MenuItem shareItem = mToolBar.getMenu().findItem(R.id.opentasks_send_task);
            if (shareItem != null)
            {
                ShareActionProvider actionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
                Intent shareIntent = new ShareIntentFactory().create(mContentSet, mModel, mAppContext);
                actionProvider.setShareIntent(shareIntent);
            }
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
        mCallback.onTaskCompleted(mTaskUri);
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
            mFloatingActionButton.setBackgroundTintList(ColorStateList.valueOf(new AdjustedForFab(mListColor).argb()));
        }
    }


    @SuppressLint("NewApi")
    @Override
    public void onContentLoaded(ContentSet contentSet)
    {
        if (contentSet.containsKey(Tasks.ACCOUNT_TYPE))
        {
            mListColor = TaskFieldAdapters.LIST_COLOR.get(contentSet);
            ((Callback) getActivity()).onListColorLoaded(new ValueColor(mListColor));

            updateColor();

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
            if (mContentSet != null && mTaskUri != null)
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

        if (mIsTheTitleContainerVisible)
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
     *         an {@link AppCompatActivity}.
     */
    public void setupToolbarAsActionbar(androidx.appcompat.app.AppCompatActivity activty)
    {
        if (mToolBar == null)
        {
            return;
        }

        activty.setSupportActionBar(mToolBar);
        activty.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    /**
     * Shows or hides the floating action button.
     *
     * @param show
     *         <code>true</code> to show the FloatingActionButton, <code>false</code> to hide it.
     */
    @SuppressLint("NewApi")
    private void showFloatingActionButton(final boolean show)
    {
        CoordinatorLayout.LayoutParams p = (CoordinatorLayout.LayoutParams) mFloatingActionButton.getLayoutParams();
        if (show)
        {
            p.setAnchorId(R.id.appbar);
            mFloatingActionButton.setLayoutParams(p);
            mFloatingActionButton.show();
            // make sure the FAB has the right color
            updateColor();
        }
        else
        {
            p.setAnchorId(View.NO_ID);
            mFloatingActionButton.setLayoutParams(p);
            mFloatingActionButton.hide();
        }
    }
}
