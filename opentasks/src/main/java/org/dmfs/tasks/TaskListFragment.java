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

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import org.dmfs.android.bolts.color.Color;
import org.dmfs.android.bolts.color.elementary.ValueColor;
import org.dmfs.android.retentionmagic.SupportFragment;
import org.dmfs.android.retentionmagic.annotations.Parameter;
import org.dmfs.android.retentionmagic.annotations.Retain;
import org.dmfs.provider.tasks.AuthorityUtil;
import org.dmfs.tasks.contract.TaskContract;
import org.dmfs.tasks.contract.TaskContract.Instances;
import org.dmfs.tasks.contract.TaskContract.Tasks;
import org.dmfs.tasks.groupings.filters.AbstractFilter;
import org.dmfs.tasks.groupings.filters.ConstantFilter;
import org.dmfs.tasks.model.Model;
import org.dmfs.tasks.model.Sources;
import org.dmfs.tasks.model.TaskFieldAdapters;
import org.dmfs.tasks.utils.ExpandableGroupDescriptor;
import org.dmfs.tasks.utils.ExpandableGroupDescriptorAdapter;
import org.dmfs.tasks.utils.FlingDetector;
import org.dmfs.tasks.utils.FlingDetector.OnFlingListener;
import org.dmfs.tasks.utils.OnChildLoadedListener;
import org.dmfs.tasks.utils.OnModelLoadedListener;
import org.dmfs.tasks.utils.RetainExpandableListView;
import org.dmfs.tasks.utils.SafeFragmentUiRunnable;
import org.dmfs.tasks.utils.SearchHistoryDatabaseHelper.SearchHistoryColumns;

import androidx.annotation.NonNull;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;


/**
 * A list fragment representing a list of Tasks. This fragment also supports tablet devices by allowing list items to be given an 'activated' state upon
 * selection. This helps indicate which item is currently being viewed in a {@link ViewTaskFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks} interface
 *
 * @author Tobias Reinsch <tobias@dmfs.org>
 */
public class TaskListFragment extends SupportFragment
        implements LoaderManager.LoaderCallbacks<Cursor>, OnChildLoadedListener, OnModelLoadedListener, OnFlingListener
{

    @SuppressWarnings("unused")
    private static final String TAG = "org.dmfs.tasks.TaskListFragment";

    private final static String ARG_INSTANCE_ID = "instance_id";

    private static final long INTERVAL_LISTVIEW_REDRAW = 60000;

    /**
     * A filter to hide completed tasks.
     */
    private final static AbstractFilter COMPLETED_FILTER = new ConstantFilter(Tasks.IS_CLOSED + "=0");

    /**
     * The group descriptor to use.
     */
    private ExpandableGroupDescriptor mGroupDescriptor;

    /**
     * The fragment's current callback object, which is notified of list item clicks.
     */
    private Callbacks mCallbacks;

    @Retain(permanent = true, instanceNSField = "mInstancePosition")
    private int mActivatedPositionGroup = ExpandableListView.INVALID_POSITION;
    @Retain(permanent = true, instanceNSField = "mInstancePosition")
    private int mActivatedPositionChild = ExpandableListView.INVALID_POSITION;

    private RetainExpandableListView mExpandableListView;
    private Context mAppContext;
    private ExpandableGroupDescriptorAdapter mAdapter;
    private Handler mHandler;
    @Retain(permanent = true, instanceNSField = "mInstancePosition")
    private long[] mSavedExpandedGroups = null;
    @Retain(permanent = true, instanceNSField = "mInstancePosition")
    private boolean mSavedCompletedFilter;

    @Parameter(key = ARG_INSTANCE_ID)
    private int mInstancePosition;

    private Loader<Cursor> mCursorLoader;
    private String mAuthority;

    private Uri mSelectedTaskUri;

    private boolean mTwoPaneLayout;

    /**
     * The child position to open when the fragment is displayed.
     **/
    private ListPosition mSelectedChildPosition;

    @Retain
    private int mPageId = -1;

    private final OnChildClickListener mTaskItemClickListener = new OnChildClickListener()
    {

        @Override
        public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id)
        {
            selectChildView(parent, groupPosition, childPosition, true);

            mActivatedPositionGroup = groupPosition;
            mActivatedPositionChild = childPosition;
            /*
             * In contrast to a ListView an ExpandableListView does not set the activated item on it's own. So we have to do that here.
             */
            setActivatedItem(groupPosition, childPosition);
            return true;
        }

    };

    private final OnGroupCollapseListener mTaskListCollapseListener = new OnGroupCollapseListener()
    {

        @Override
        public void onGroupCollapse(int groupPosition)
        {
            if (groupPosition == mActivatedPositionGroup)
            {
                mActivatedPositionChild = ExpandableListView.INVALID_POSITION;
                mActivatedPositionGroup = ExpandableListView.INVALID_POSITION;
            }

        }
    };


    /**
     * A callback interface that all activities containing this fragment must implement. This mechanism allows activities to be notified of item selections.
     */
    public interface Callbacks
    {
        /**
         * Callback for when an item has been selected.
         *
         * @param taskUri
         *         The {@link Uri} of the selected task.
         * @param taskListColor
         *         the color of the task list (used for toolbars)
         * @param forceReload
         *         Whether to reload the task or not.
         */
        void onItemSelected(@NonNull Uri taskUri, @NonNull Color taskListColor, boolean forceReload, int pagePosition);

        /**
         * Called when a task has been removed from the list.
         * <p>
         * TODO It's only called when task is deleted by the swipe out, and not when it is completed.
         * It should probably be called that time, too. See https://github.com/dmfs/opentasks/issues/641.
         *
         * @param taskUri
         *         the content uri of the task that has been removed
         */
        void onItemRemoved(@NonNull Uri taskUri);

        void onAddNewTask();

        ExpandableGroupDescriptor getGroupDescriptor(int position);
    }


    /**
     * A runnable that periodically updates the list. We need that to update relative dates & times. TODO: we probably should move that to the adapter to update
     * only the date & times fields, not the entire list.
     */
    private Runnable mListRedrawRunnable = new SafeFragmentUiRunnable(this, new Runnable()
    {

        @Override
        public void run()
        {
            mExpandableListView.invalidateViews();
            mHandler.postDelayed(mListRedrawRunnable, INTERVAL_LISTVIEW_REDRAW);
        }
    });


    public static TaskListFragment newInstance(int instancePosition)
    {
        TaskListFragment result = new TaskListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_INSTANCE_ID, instancePosition);
        result.setArguments(args);
        return result;
    }


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the fragment (e.g. upon screen orientation changes).
     */
    public TaskListFragment()
    {
    }


    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);

        mTwoPaneLayout = activity.getResources().getBoolean(R.bool.has_two_panes);

        mAuthority = AuthorityUtil.taskAuthority(activity);

        mAppContext = activity.getBaseContext();

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks))
        {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;

        // load accounts early
        Sources.loadModelAsync(activity, TaskContract.LOCAL_ACCOUNT_TYPE, this);
    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mHandler = new Handler();
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_expandable_task_list, container, false);
        mExpandableListView = (RetainExpandableListView) rootView.findViewById(android.R.id.list);

        if (mGroupDescriptor == null)
        {
            loadGroupDescriptor();
        }

        // setup the views
        this.prepareReload();

        // expand lists
        if (mSavedExpandedGroups != null)
        {
            mExpandableListView.expandGroups(mSavedExpandedGroups);
        }

        FlingDetector swiper = new FlingDetector(mExpandableListView, mGroupDescriptor.getElementViewDescriptor().getFlingContentViewId());
        swiper.setOnFlingListener(this);

        return rootView;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
    }


    @Override
    public void onStart()
    {
        reloadCursor();
        super.onStart();
    }


    @Override
    public void onResume()
    {
        super.onResume();
        mExpandableListView.invalidateViews();
        startAutomaticRedraw();
        openSelectedChild();

        if (mTwoPaneLayout)
        {
            setListViewScrollbarPositionLeft(true);
            setActivateOnItemClick(true);
        }
    }


    @Override
    public void onPause()
    {
        // we can't rely on save instance state being called before onPause, so we get the expanded groups here again
        if (!((TaskListActivity) getActivity()).isInTransientState())
        {
            mSavedExpandedGroups = mExpandableListView.getExpandedGroups();
        }
        stopAutomaticRedraw();
        super.onPause();
    }


    @Override
    public void onDetach()
    {
        super.onDetach();

    }


    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        if (!((TaskListActivity) getActivity()).isInTransientState())
        {
            mSavedExpandedGroups = mExpandableListView.getExpandedGroups();
        }
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        // create menu
        inflater.inflate(R.menu.task_list_fragment_menu, menu);

        // restore menu state
        MenuItem item = menu.findItem(R.id.menu_show_completed);
        if (item != null)
        {
            item.setChecked(mSavedCompletedFilter);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_show_completed)
        {

            mSavedCompletedFilter = !mSavedCompletedFilter;
            item.setChecked(mSavedCompletedFilter);
            mAdapter.setChildCursorFilter(mSavedCompletedFilter ? null : COMPLETED_FILTER);

            // reload the child cursors only
            for (int i = 0; i < mAdapter.getGroupCount(); ++i)
            {
                mAdapter.reloadGroup(i);
            }
            return true;
        }
        else if (itemId == R.id.menu_sync_now)
        {
            doSyncNow();
            return true;
        }
        else
        {
            return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1)
    {

        if (mGroupDescriptor != null)
        {
            mCursorLoader = mGroupDescriptor.getGroupCursorLoader(mAppContext);
        }
        return mCursorLoader;

    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)
    {

        if (mSavedExpandedGroups == null)
        {
            mSavedExpandedGroups = mExpandableListView.getExpandedGroups();
        }

        mAdapter.setGroupCursor(cursor);

        if (mSavedExpandedGroups != null)
        {
            mExpandableListView.expandGroups(mSavedExpandedGroups);
            if (!((TaskListActivity) getActivity()).isInTransientState())
            {
                mSavedExpandedGroups = null;
            }
        }

        mHandler.post(new SafeFragmentUiRunnable(this, () -> mAdapter.reloadLoadedGroups()));
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {
        mAdapter.changeCursor(new MatrixCursor(new String[] { "_id" }));
    }


    @Override
    public void onChildLoaded(final int pos, Cursor childCursor)
    {
        if (mActivatedPositionChild != ExpandableListView.INVALID_POSITION)
        {
            if (pos == mActivatedPositionGroup && mActivatedPositionChild != ExpandableListView.INVALID_POSITION)
            {
                mHandler.post(setOpenHandler);
            }
        }
        // check for child to select
        if (mTwoPaneLayout)
        {
            selectChild(pos, childCursor);
        }
    }


    @Override
    public void onModelLoaded(Model model)
    {
        // nothing to do, we've just loaded the default model to speed up loading the detail view and the editor view.
    }


    private void selectChildView(ExpandableListView expandLV, int groupPosition, int childPosition, boolean force)
    {
        if (groupPosition < mAdapter.getGroupCount() && childPosition < mAdapter.getChildrenCount(groupPosition))
        {
            // a task instance element has been clicked, get it's instance id and notify the activity
            ExpandableListAdapter listAdapter = expandLV.getExpandableListAdapter();
            Cursor cursor = (Cursor) listAdapter.getChild(groupPosition, childPosition);

            if (cursor == null)
            {
                return;
            }

            Uri taskUri = ContentUris.withAppendedId(Instances.getContentUri(mAuthority), (long) TaskFieldAdapters.TASK_ID.get(cursor));
            Color taskListColor = new ValueColor(TaskFieldAdapters.LIST_COLOR.get(cursor));
            mCallbacks.onItemSelected(taskUri, taskListColor, force, mInstancePosition);
        }
    }


    /**
     * prepares the update of the view after the group descriptor was changed
     */
    public void prepareReload()
    {
        mAdapter = new ExpandableGroupDescriptorAdapter(new MatrixCursor(new String[] { "_id" }), getActivity(), getLoaderManager(), mGroupDescriptor);
        mExpandableListView.setAdapter(mAdapter);
        mExpandableListView.setOnChildClickListener(mTaskItemClickListener);
        mExpandableListView.setOnGroupCollapseListener(mTaskListCollapseListener);
        mAdapter.setOnChildLoadedListener(this);
        mAdapter.setChildCursorFilter(COMPLETED_FILTER);
        restoreFilterState();

    }


    private void reloadCursor()
    {
        getLoaderManager().restartLoader(-1, null, this);
    }


    public void restoreFilterState()
    {
        if (mSavedCompletedFilter)
        {
            mAdapter.setChildCursorFilter(mSavedCompletedFilter ? null : COMPLETED_FILTER);
            // reload the child cursors only
            for (int i = 0; i < mAdapter.getGroupCount(); ++i)
            {
                mAdapter.reloadGroup(i);
            }
        }

    }


    /**
     * Trigger a synchronization for all accounts.
     */
    private void doSyncNow()
    {
        AccountManager accountManager = AccountManager.get(mAppContext);
        Account[] accounts = accountManager.getAccounts();
        for (Account account : accounts)
        {
            // TODO: do we need a new bundle for each account or can we reuse it?
            Bundle extras = new Bundle();
            extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
            ContentResolver.requestSync(account, mAuthority, extras);
        }
    }


    /**
     * Remove the task with the given {@link Uri} and title, asking for confirmation first.
     *
     * @param taskUri
     *         The {@link Uri} of the atsk to remove.
     * @param taskTitle
     *         the title of the task to remove.
     *
     * @return
     */
    private void removeTask(final Uri taskUri, final String taskTitle)
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
                mAppContext.getContentResolver().delete(taskUri, null, null);
                Snackbar.make(mExpandableListView, getString(R.string.toast_task_deleted, taskTitle), Snackbar.LENGTH_SHORT).show();
                mCallbacks.onItemRemoved(taskUri);
            }
        }).setMessage(getString(R.string.confirm_delete_message_with_title, taskTitle)).create().show();
    }


    /**
     * Opens the task editor for the selected Task.
     *
     * @param taskUri
     *         The {@link Uri} of the task.
     */
    private void openTaskEditor(final Uri taskUri, final String accountType)
    {
        Intent editTaskIntent = new Intent(Intent.ACTION_EDIT);
        editTaskIntent.setData(taskUri);
        editTaskIntent.putExtra(EditTaskActivity.EXTRA_DATA_ACCOUNT_TYPE, accountType);
        startActivity(editTaskIntent);
    }


    @Override
    public int canFling(ListView v, int pos)
    {
        long packedPos = mExpandableListView.getExpandableListPosition(pos);
        if (packedPos != ExpandableListView.PACKED_POSITION_VALUE_NULL
                && ExpandableListView.getPackedPositionType(packedPos) == ExpandableListView.PACKED_POSITION_TYPE_CHILD)
        {
            return FlingDetector.RIGHT_FLING | FlingDetector.LEFT_FLING;
        }
        else
        {
            return 0;
        }
    }


    @Override
    public void onFlingStart(ListView listView, View listElement, int position, int direction)
    {

        // control the visibility of the views that reveal behind a flinging element regarding the fling direction
        int rightFlingViewId = mGroupDescriptor.getElementViewDescriptor().getFlingRevealRightViewId();
        int leftFlingViewId = mGroupDescriptor.getElementViewDescriptor().getFlingRevealLeftViewId();
        TextView rightFlingView = null;
        TextView leftFlingView = null;

        if (rightFlingViewId != -1)
        {
            rightFlingView = (TextView) listElement.findViewById(rightFlingViewId);
        }
        if (leftFlingViewId != -1)
        {
            leftFlingView = (TextView) listElement.findViewById(leftFlingViewId);
        }

        Resources resources = getActivity().getResources();

        // change title and icon regarding the task status
        long packedPos = mExpandableListView.getExpandableListPosition(position);
        if (ExpandableListView.getPackedPositionType(packedPos) == ExpandableListView.PACKED_POSITION_TYPE_CHILD)
        {
            ExpandableListAdapter listAdapter = mExpandableListView.getExpandableListAdapter();
            Cursor cursor = (Cursor) listAdapter.getChild(ExpandableListView.getPackedPositionGroup(packedPos),
                    ExpandableListView.getPackedPositionChild(packedPos));

            if (cursor != null)
            {
                int taskStatus = cursor.getInt(cursor.getColumnIndex(Instances.STATUS));
                if (leftFlingView != null && rightFlingView != null)
                {
                    if (taskStatus == Instances.STATUS_COMPLETED)
                    {
                        leftFlingView.setText(R.string.fling_task_delete);
                        leftFlingView.setCompoundDrawablesWithIntrinsicBounds(resources.getDrawable(R.drawable.content_discard), null, null, null);
                        rightFlingView.setText(R.string.fling_task_uncomplete);
                        rightFlingView.setCompoundDrawablesWithIntrinsicBounds(null, null, resources.getDrawable(R.drawable.content_remove_light), null);
                    }
                    else
                    {
                        leftFlingView.setText(R.string.fling_task_complete);
                        leftFlingView.setCompoundDrawablesWithIntrinsicBounds(resources.getDrawable(R.drawable.ic_action_complete), null, null, null);
                        rightFlingView.setText(R.string.fling_task_edit);
                        rightFlingView.setCompoundDrawablesWithIntrinsicBounds(null, null, resources.getDrawable(R.drawable.content_edit), null);
                    }
                }
            }
        }

        if (rightFlingView != null)
        {
            rightFlingView.setVisibility(direction != FlingDetector.LEFT_FLING ? View.GONE : View.VISIBLE);
        }
        if (leftFlingView != null)
        {
            leftFlingView.setVisibility(direction != FlingDetector.RIGHT_FLING ? View.GONE : View.VISIBLE);
        }

    }


    @Override
    public boolean onFlingEnd(ListView v, View listElement, int pos, int direction)
    {
        long packedPos = mExpandableListView.getExpandableListPosition(pos);
        if (ExpandableListView.getPackedPositionType(packedPos) == ExpandableListView.PACKED_POSITION_TYPE_CHILD)
        {
            ExpandableListAdapter listAdapter = mExpandableListView.getExpandableListAdapter();
            Cursor cursor = (Cursor) listAdapter.getChild(ExpandableListView.getPackedPositionGroup(packedPos),
                    ExpandableListView.getPackedPositionChild(packedPos));

            if (cursor != null)
            {
                long instanceId = cursor.getLong(cursor.getColumnIndex(Instances._ID));

                boolean closed = cursor.getLong(cursor.getColumnIndex(Instances.IS_CLOSED)) > 0;
                String title = cursor.getString(cursor.getColumnIndex(Instances.TITLE));
                // TODO: use the instance URI once we support recurrence
                Uri taskUri = ContentUris.withAppendedId(Instances.getContentUri(mAuthority), instanceId);

                if (direction == FlingDetector.RIGHT_FLING)
                {
                    if (closed)
                    {
                        removeTask(taskUri, title);
                        // we do not know for sure if the task has been removed since the user is asked for confirmation first, so return false

                        return false;

                    }
                    else
                    {
                        return setCompleteTask(taskUri, title, true);
                    }
                }
                else if (direction == FlingDetector.LEFT_FLING)
                {
                    if (closed)
                    {
                        return setCompleteTask(taskUri, title, false);
                    }
                    else
                    {
                        openTaskEditor(taskUri, cursor.getString(cursor.getColumnIndex(Instances.ACCOUNT_TYPE)));
                        return false;
                    }
                }
            }
        }

        return false;
    }


    @Override
    public void onFlingCancel(int direction)
    {
        // TODO Auto-generated method stub

    }


    public void loadGroupDescriptor()
    {
        if (getActivity() != null)
        {
            TaskListActivity activity = (TaskListActivity) getActivity();
            if (activity != null)
            {
                mGroupDescriptor = activity.getGroupDescriptor(mPageId);
            }
        }
    }


    /**
     * Starts the automatic list view redraw (e.g. to display changing time values) on the next minute.
     */
    public void startAutomaticRedraw()
    {
        long now = System.currentTimeMillis();
        long millisToInterval = INTERVAL_LISTVIEW_REDRAW - (now % INTERVAL_LISTVIEW_REDRAW);

        mHandler.postDelayed(mListRedrawRunnable, millisToInterval);
    }


    /**
     * Stops the automatic list view redraw.
     */
    public void stopAutomaticRedraw()
    {
        mHandler.removeCallbacks(mListRedrawRunnable);
    }


    public int getOpenChildPosition()
    {
        return mActivatedPositionChild;
    }


    public int getOpenGroupPosition()
    {
        return mActivatedPositionGroup;
    }


    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be given the 'activated' state when touched.
     * <p>
     * Note: this does not work 100% with {@link ExpandableListView}, it doesn't check touched items automatically.
     * </p>
     *
     * @param activateOnItemClick
     *         Whether to enable single choice mode or not.
     */
    public void setActivateOnItemClick(boolean activateOnItemClick)
    {
        mExpandableListView.setChoiceMode(activateOnItemClick ? ListView.CHOICE_MODE_SINGLE : ListView.CHOICE_MODE_NONE);
    }


    public void setListViewScrollbarPositionLeft(boolean left)
    {
        if (left)
        {
            mExpandableListView.setVerticalScrollbarPosition(View.SCROLLBAR_POSITION_LEFT);
            // expandLV.setScrollBarStyle(style);
        }
        else
        {
            mExpandableListView.setVerticalScrollbarPosition(View.SCROLLBAR_POSITION_RIGHT);
        }
    }


    public void setExpandableGroupDescriptor(ExpandableGroupDescriptor groupDescriptor)
    {
        mGroupDescriptor = groupDescriptor;
    }


    /**
     * Mark the given task as completed.
     *
     * @param taskUri
     *         The {@link Uri} of the task.
     * @param taskTitle
     *         The name/title of the task.
     * @param completedValue
     *         The value to be set for the completed status.
     *
     * @return <code>true</code> if the operation was successful, <code>false</code> otherwise.
     */
    private boolean setCompleteTask(Uri taskUri, String taskTitle, boolean completedValue)
    {
        ContentValues values = new ContentValues();
        values.put(Tasks.STATUS, completedValue ? Tasks.STATUS_COMPLETED : Tasks.STATUS_IN_PROCESS);
        if (!completedValue)
        {
            values.put(Tasks.PERCENT_COMPLETE, 50);
        }

        boolean completed = mAppContext.getContentResolver().update(taskUri, values, null, null) != 0;
        if (completed)
        {
            if (completedValue)
            {
                Snackbar.make(mExpandableListView, getString(R.string.toast_task_completed, taskTitle), Snackbar.LENGTH_SHORT).show();
            }
            else
            {
                Snackbar.make(mExpandableListView, getString(R.string.toast_task_uncompleted, taskTitle), Snackbar.LENGTH_SHORT).show();
            }
        }
        return completed;
    }


    public void setOpenChildPosition(int openChildPosition)
    {
        mActivatedPositionChild = openChildPosition;

    }


    public void setOpenGroupPosition(int openGroupPosition)
    {
        mActivatedPositionGroup = openGroupPosition;

    }


    public void notifyDataSetChanged(boolean expandFirst)
    {
        getLoaderManager().restartLoader(-1, null, this);
    }


    private Runnable setOpenHandler = new SafeFragmentUiRunnable(this, new Runnable()
    {
        @Override
        public void run()
        {
            selectChildView(mExpandableListView, mActivatedPositionGroup, mActivatedPositionChild, false);
            mExpandableListView.expandGroups(mSavedExpandedGroups);
            setActivatedItem(mActivatedPositionGroup, mActivatedPositionChild);
        }
    });


    public void setActivatedItem(int groupPosition, int childPosition)
    {
        if (groupPosition != ExpandableListView.INVALID_POSITION && groupPosition < mAdapter.getGroupCount()
                && childPosition != ExpandableListView.INVALID_POSITION && childPosition < mAdapter.getChildrenCount(groupPosition))
        {
            try
            {
                mExpandableListView
                        .setItemChecked(mExpandableListView.getFlatListPosition(ExpandableListView.getPackedPositionForChild(groupPosition, childPosition)),
                                true);
            }
            catch (NullPointerException e)
            {
                // for now we just catch the NPE until we've found the reason
                // just catching it won't hurt, it's just that the list selection won't be updated properly

                // FIXME: find the actual cause and fix it
            }
        }
    }


    public void expandCurrentSearchGroup()
    {
        if (mPageId == R.id.task_group_search && mAdapter.getGroupCount() > 0)
        {
            Cursor c = mAdapter.getGroup(0);
            if (c != null && c.getInt(c.getColumnIndex(SearchHistoryColumns.HISTORIC)) < 1)
            {
                mExpandableListView.expandGroup(0);
            }
        }
    }


    public void setPageId(int pageId)
    {
        mPageId = pageId;
    }


    private void selectChild(final int groupPosition, Cursor childCursor)
    {
        mSelectedTaskUri = ((TaskListActivity) getActivity()).getSelectedTaskUri();
        if (mSelectedTaskUri != null)
        {
            new AsyncSelectChildTask().execute(new SelectChildTaskParams(groupPosition, childCursor, mSelectedTaskUri));
        }
    }


    public void openSelectedChild()
    {
        if (mSelectedChildPosition != null)
        {
            // post delayed to allow the list view to finish creation
            mExpandableListView.postDelayed(new SafeFragmentUiRunnable(this, () ->
            {
                mExpandableListView.expandGroup(mSelectedChildPosition.groupPosition);
                mSelectedChildPosition.flatListPosition = mExpandableListView.getFlatListPosition(
                        RetainExpandableListView.getPackedPositionForChild(mSelectedChildPosition.groupPosition, mSelectedChildPosition.childPosition));

                setActivatedItem(mSelectedChildPosition.groupPosition, mSelectedChildPosition.childPosition);
                selectChildView(mExpandableListView, mSelectedChildPosition.groupPosition, mSelectedChildPosition.childPosition, true);
                mExpandableListView.smoothScrollToPosition(mSelectedChildPosition.flatListPosition);
            }), 0);
        }
    }


    /**
     * Returns the position of the task in the cursor. Returns -1 if the task is not in the cursor
     **/
    private int getSelectedChildPostion(Uri taskUri, Cursor listCursor)
    {
        if (taskUri != null && listCursor != null && listCursor.moveToFirst())
        {
            Long taskIdToSelect = Long.valueOf(taskUri.getLastPathSegment());
            do
            {
                Long taskId = listCursor.getLong(listCursor.getColumnIndex(Tasks._ID));
                if (taskId.equals(taskIdToSelect))
                {
                    return listCursor.getPosition();
                }
            } while (listCursor.moveToNext());
        }
        return -1;
    }


    private static class SelectChildTaskParams
    {
        int groupPosition;
        Uri taskUriToSelect;
        Cursor childCursor;


        SelectChildTaskParams(int groupPosition, Cursor childCursor, Uri taskUriToSelect)
        {
            this.groupPosition = groupPosition;
            this.childCursor = childCursor;
            this.taskUriToSelect = taskUriToSelect;
        }
    }


    private static class ListPosition
    {
        int groupPosition;
        int childPosition;
        int flatListPosition;


        ListPosition(int groupPosition, int childPosition)
        {
            this.groupPosition = groupPosition;
            this.childPosition = childPosition;
        }
    }


    private class AsyncSelectChildTask extends AsyncTask<SelectChildTaskParams, Void, Void>
    {

        @Override
        protected Void doInBackground(SelectChildTaskParams... params)
        {
            int count = params.length;
            for (int i = 0; i < count; i++)
            {
                final SelectChildTaskParams param = params[i];

                final int childPosition = getSelectedChildPostion(param.taskUriToSelect, param.childCursor);
                if (childPosition > -1)
                {
                    mSelectedChildPosition = new ListPosition(param.groupPosition, childPosition);
                    openSelectedChild();
                }
            }
            return null;
        }

    }
}
