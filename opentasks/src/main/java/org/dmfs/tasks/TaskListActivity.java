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
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import org.dmfs.android.bolts.color.Color;
import org.dmfs.android.bolts.color.colors.PrimaryColor;
import org.dmfs.android.bolts.color.elementary.ValueColor;
import org.dmfs.android.retentionmagic.annotations.Retain;
import org.dmfs.jems.single.adapters.Unchecked;
import org.dmfs.provider.tasks.AuthorityUtil;
import org.dmfs.tasks.contract.TaskContract.Tasks;
import org.dmfs.tasks.groupings.AbstractGroupingFactory;
import org.dmfs.tasks.groupings.ByDueDate;
import org.dmfs.tasks.groupings.ByList;
import org.dmfs.tasks.groupings.ByPriority;
import org.dmfs.tasks.groupings.ByProgress;
import org.dmfs.tasks.groupings.BySearch;
import org.dmfs.tasks.groupings.ByStartDate;
import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.utils.BaseActivity;
import org.dmfs.tasks.utils.ExpandableGroupDescriptor;
import org.dmfs.tasks.utils.SearchHistoryHelper;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.SearchView.OnQueryTextListener;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.core.view.MenuItemCompat.OnActionExpandListener;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.ViewPager.OnPageChangeListener;


/**
 * An activity representing a list of Tasks. This activity has different presentations for handset and tablet-size devices. On handsets, the activity presents a
 * list of items, which when touched, lead to a {@link ViewTaskActivity} representing item details. On tablets, the activity presents the list of items and item
 * details side-by-side using two vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a {@link TaskListFragment} and the item details (if present) is a {@link ViewTaskFragment}.
 * <p>
 * This activity also implements the required {@link TaskListFragment.Callbacks} interface to listen for item selections.
 * <p>
 * <p>
 * TODO: move the code to persist the expanded groups into a the GroupingDescriptor class
 * </p>
 *
 * @author Tobias Reinsch <tobias@dmfs.org>
 */
public class TaskListActivity extends BaseActivity implements TaskListFragment.Callbacks, ViewTaskFragment.Callback
{

    /**
     * Tells the activity to display the details of the task with the URI from the intent data.
     **/
    public static final String EXTRA_DISPLAY_TASK = "org.dmfs.tasks.DISPLAY_TASK";

    /**
     * Tells the activity to select the task in the list with the URI from the intent data.
     **/
    public static final String EXTRA_FORCE_LIST_SELECTION = "org.dmfs.tasks.FORCE_LIST_SELECTION";

    private final static int REQUEST_CODE_NEW_TASK = 2924;
    private final static int REQUEST_CODE_PREFS = 2925;

    /**
     * The time to wait for a new key before updating the search view.
     */
    private final static int SEARCH_UPDATE_DELAY = 400; // ms

    private final static String DETAILS_FRAGMENT_TAG = "details_fragment_tag";

    /**
     * Array of {@link ExpandableGroupDescriptor}s.
     */
    private AbstractGroupingFactory[] mGroupingFactories;

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet device.
     */
    private boolean mTwoPane;
    private ViewPager mViewPager;
    private TaskGroupPagerAdapter mPagerAdapter;

    @Retain(permanent = true)
    private int mCurrentPageId;

    /**
     * The last used color for the toolbars. {@link android.graphics.Color#TRANSPARENT} represents the absent value.
     * (Used upon start/rotation until the actually selected task with its color is loaded, to avoid flashing up primary color.)
     */
    @Retain(permanent = true)
    @ColorInt
    private int mLastUsedColor = android.graphics.Color.TRANSPARENT;

    /**
     * The current pager position
     **/
    private int mCurrentPagePosition = 0;

    private int mPreviousPagePosition = -1;

    private String mAuthority;

    private MenuItem mSearchItem;

    private TabLayout mTabs;

    private final Handler mHandler = new Handler();

    private SearchHistoryHelper mSearchHistoryHelper;

    private boolean mAutoExpandSearchView = false;

    /**
     * The Uri of the task to display/highlight in the list view.
     **/
    @Retain
    private Uri mSelectedTaskUri;

    /**
     * The Uri of the task to display/highlight in the list view coming from the widget.
     **/
    private Uri mSelectedTaskUriOnLaunch;

    /**
     * Indicates to show ViewTaskActivity when rotating to single pane.
     **/
    @Retain
    private boolean mShouldSwitchToDetail = false;

    /**
     * Indicates the TaskListFragments to select/highlight the mSelectedTaskUri item
     **/
    private boolean mShouldSelectTaskListItem = false;

    /**
     * Indicates a transient state after rotation to redirect to the TaskViewActivtiy
     **/
    private boolean mTransientState = false;

    private AppBarLayout mAppBarLayout;

    private FloatingActionButton mFloatingActionButton;
    private SharedPreferences mPrefs;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        updateTheme();
        super.onCreate(savedInstanceState);

        if (mLastUsedColor == android.graphics.Color.TRANSPARENT)
        {
            // no saved color, use the primary color
            mLastUsedColor = new PrimaryColor(this).argb();
        }

        // check for single pane activity change
        mTwoPane = getResources().getBoolean(R.bool.has_two_panes);

        resolveIntentAction(getIntent());

        if (mSelectedTaskUri != null)
        {
            if (!mTwoPane && mShouldSwitchToDetail)
            {
                Intent viewTaskIntent = new Intent(Intent.ACTION_VIEW);
                viewTaskIntent.setData(mSelectedTaskUri);
                viewTaskIntent.putExtra(ViewTaskActivity.EXTRA_COLOR, mLastUsedColor);
                startActivity(viewTaskIntent);
                mShouldSwitchToDetail = false;
                mTransientState = true;
            }
        }
        else
        {
            mShouldSwitchToDetail = false;
        }

        setContentView(R.layout.activity_task_list);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuthority = AuthorityUtil.taskAuthority(this);
        mSearchHistoryHelper = new SearchHistoryHelper(this);

        if (findViewById(R.id.task_detail_container) != null)
        {
            /* Note: 'savedInstanceState == null' is not used here as would be usual with fragments, because of the case of when rotation means
            switching from one-pane mode to two-pane mode on small tablets and the fragment has to added. To cover that case as well, the fragment is always replaced. */
            replaceTaskDetailsFragment(
                    mSelectedTaskUri == null ?
                            EmptyTaskFragment.newInstance(new ValueColor(mLastUsedColor))
                            : ViewTaskFragment.newInstance(mSelectedTaskUri, new ValueColor(mLastUsedColor)));
        }
        else
        {
            // When rotating the screen means switching from two-pane to single-pane mode (on small tablets), remove the obsolete fragment that gets recreated by FragmentManager:
            FragmentManager fragmentManager = getSupportFragmentManager();
            Fragment detailFragment = fragmentManager.findFragmentByTag(DETAILS_FRAGMENT_TAG);
            if (detailFragment != null)
            {
                fragmentManager.beginTransaction().remove(detailFragment).commit();
            }
        }

        mGroupingFactories = new AbstractGroupingFactory[] {
                new ByList(mAuthority, this), new ByDueDate(mAuthority), new ByStartDate(mAuthority),
                new ByPriority(mAuthority, this), new ByProgress(mAuthority), new BySearch(mAuthority, mSearchHistoryHelper) };

        mPagerAdapter = new Unchecked<>(() -> new TaskGroupPagerAdapter(getSupportFragmentManager(), mGroupingFactories, this, R.xml.listview_tabs)).value();

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mPagerAdapter);

        int currentPageIndex = mPagerAdapter.getPagePosition(mCurrentPageId);

        if (currentPageIndex >= 0)
        {
            mCurrentPagePosition = currentPageIndex;
            mViewPager.setCurrentItem(currentPageIndex);
            if (mCurrentPageId == R.id.task_group_search)
            {
                if (mSearchItem != null)
                {
                    // that's actually quite impossible to happen
                    MenuItemCompat.expandActionView(mSearchItem);
                }
                else
                {
                    mAutoExpandSearchView = true;
                }
            }
        }
        updateTitle(currentPageIndex);

        // Bind the tabs to the ViewPager
        mTabs = (TabLayout) findViewById(R.id.tabs);
        mTabs.setupWithViewPager(mViewPager);
        setupTabIcons();

        mViewPager.addOnPageChangeListener(new OnPageChangeListener()
        {

            @Override
            public void onPageSelected(int position)
            {
                mSelectedTaskUri = null;
                mCurrentPagePosition = position;

                int newPageId = mPagerAdapter.getPageId(mCurrentPagePosition);

                if (newPageId == R.id.task_group_search)
                {
                    int oldPageId = mCurrentPageId;
                    mCurrentPageId = newPageId;

                    // store the page position we're coming from
                    mPreviousPagePosition = mPagerAdapter.getPagePosition(oldPageId);
                }
                else if (mCurrentPageId == R.id.task_group_search)
                {
                    // we've been on the search page before, so commit the search and close the search view
                    mSearchHistoryHelper.commitSearch();
                    mHandler.post(mSearchUpdater);
                    mCurrentPageId = newPageId;
                    hideSearchActionView();
                }
                mCurrentPageId = newPageId;
                updateTitle(mCurrentPageId);
            }


            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
            {
            }


            @Override
            public void onPageScrollStateChanged(int state)
            {
                if (state == ViewPager.SCROLL_STATE_IDLE && mCurrentPageId == R.id.task_group_search)
                {
                    // the search page is selected now, expand the search view
                    mHandler.postDelayed(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            MenuItemCompat.expandActionView(mSearchItem);
                        }
                    }, 50);
                }
            }

        });

        mFloatingActionButton = (FloatingActionButton) findViewById(R.id.floating_action_button);
        if (mFloatingActionButton != null)
        {
            mFloatingActionButton.setOnClickListener(new OnClickListener()
            {

                @Override
                public void onClick(View v)
                {
                    onAddNewTask();
                }
            });
        }
    }


    private void updateTheme()
    {
        if (Build.VERSION.SDK_INT >= 29)
        {
            boolean sysTheme = mPrefs.getBoolean(
                    getString(R.string.opentasks_pref_appearance_system_theme),
                    getResources().getBoolean(R.bool.opentasks_system_theme_default));
            boolean darkTheme = mPrefs.getBoolean(
                    getString(R.string.opentasks_pref_appearance_dark_theme),
                    getResources().getBoolean(R.bool.opentasks_dark_theme_default));

            AppCompatDelegate.setDefaultNightMode(
                    sysTheme ?
                            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM :
                            darkTheme ?
                                    AppCompatDelegate.MODE_NIGHT_YES :
                                    AppCompatDelegate.MODE_NIGHT_NO);
        }
    }


    private void setupTabIcons()
    {
        for (int i = 0, count = mPagerAdapter.getCount(); i < count; ++i)
        {
            mTabs.getTabAt(i).setIcon(mPagerAdapter.getTabIcon(i));
        }
    }


    @Override
    protected void onResume()
    {
        updateTitle(mCurrentPageId);
        super.onResume();
    }


    @Override
    protected void onNewIntent(Intent intent)
    {
        resolveIntentAction(intent);
        super.onNewIntent(intent);
    }


    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        mSearchHistoryHelper.close();
    }


    /**
     * Callback method from {@link TaskListFragment.Callbacks} indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(@NonNull Uri uri, @NonNull Color taskListColor, boolean forceReload, int pagePosition)
    {
        // only accept selections from the current visible task fragment or the activity itself
        if (pagePosition == -1 || pagePosition == mCurrentPagePosition)
        {
            if (mTwoPane)
            {
                if (forceReload)
                {
                    mSelectedTaskUri = uri;
                    mShouldSwitchToDetail = false;
                }
                replaceTaskDetailsFragment(ViewTaskFragment.newInstance(uri, taskListColor));
            }
            else if (forceReload)
            {
                mSelectedTaskUri = uri;

                // In single-pane mode, simply start the detail activity
                // for the selected item ID.
                Intent detailIntent = new Intent(Intent.ACTION_VIEW);
                detailIntent.setData(uri);
                detailIntent.putExtra(ViewTaskActivity.EXTRA_COLOR, mLastUsedColor);
                startActivity(detailIntent);
                mShouldSwitchToDetail = false;
            }
        }
    }


    @Override
    public void onItemRemoved(@NonNull Uri taskUri)
    {
        if (taskUri.equals(mSelectedTaskUri))
        {
            mSelectedTaskUri = null;
            if (mTwoPane)
            {
                replaceTaskDetailsFragment(EmptyTaskFragment.newInstance(new ValueColor(mLastUsedColor)));
            }
        }
    }


    @Override
    public void onAddNewTask()
    {
        Intent editTaskIntent = new Intent(Intent.ACTION_INSERT);
        editTaskIntent.setData(Tasks.getContentUri(mAuthority));
        startActivityForResult(editTaskIntent, REQUEST_CODE_NEW_TASK);
    }


    @Override
    public ExpandableGroupDescriptor getGroupDescriptor(int pageId)
    {
        for (AbstractGroupingFactory factory : mGroupingFactories)
        {
            if (factory.getId() == pageId)
            {
                return factory.getExpandableGroupDescriptor();
            }
        }
        return null;
    }


    private void replaceTaskDetailsFragment(@NonNull Fragment fragment)
    {
        FragmentManager fragmentManager = getSupportFragmentManager();
        // only change state if the state has not been saved yet, otherwise just drop it
        if (!fragmentManager.isStateSaved())
        {
            fragmentManager.beginTransaction()
                    .setCustomAnimations(0, R.anim.openttasks_fade_exit, 0, 0)
                    .replace(R.id.task_detail_container, fragment, DETAILS_FRAGMENT_TAG).commit();
        }
    }


    private void updateTitle(int pageId)
    {
        switch (pageId)
        {
            case R.id.task_group_by_list:
                getSupportActionBar().setTitle(R.string.task_group_title_list);
                break;
            case R.id.task_group_by_start:
                getSupportActionBar().setTitle(R.string.task_group_title_start);
                break;
            case R.id.task_group_by_due:
                getSupportActionBar().setTitle(R.string.task_group_title_due);
                break;
            case R.id.task_group_by_priority:
                getSupportActionBar().setTitle(R.string.task_group_title_priority);
                break;
            case R.id.task_group_by_progress:
                getSupportActionBar().setTitle(R.string.task_group_title_progress);
                break;

            default:
                getSupportActionBar().setTitle(R.string.task_group_title_default);
                break;
        }
    }


    private void resolveIntentAction(Intent intent)
    {
        // check which task should be selected
        if (intent.getBooleanExtra(EXTRA_DISPLAY_TASK, false))

        {
            mShouldSwitchToDetail = true;
            mSelectedTaskUri = intent.getData();
        }

        if (intent.getBooleanExtra(EXTRA_DISPLAY_TASK, false) && intent.getBooleanExtra(EXTRA_FORCE_LIST_SELECTION, true) && mTwoPane)
        {
            mShouldSwitchToDetail = true;
            mSelectedTaskUriOnLaunch = intent.getData();
            mShouldSelectTaskListItem = true;
            if (mPagerAdapter != null)
            {
                mPagerAdapter.notifyDataSetChanged();
            }
        }
        else
        {
            mSelectedTaskUriOnLaunch = null;
            mShouldSelectTaskListItem = false;
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        if (requestCode == REQUEST_CODE_NEW_TASK && resultCode == RESULT_OK && intent != null && intent.getData() != null)
        {
            // Use the same flow to display the new task as if it was opened from the widget
            Intent displayIntent = new Intent(this, TaskListActivity.class);
            displayIntent.putExtra(TaskListActivity.EXTRA_DISPLAY_TASK, !intent.getBooleanExtra(EditTaskFragment.KEY_NEW_TASK, false));
            displayIntent.putExtra(TaskListActivity.EXTRA_FORCE_LIST_SELECTION, true);
            Uri newTaskUri = intent.getData();
            displayIntent.setData(newTaskUri);
            onNewIntent(displayIntent);

            /* Icons have to be refreshed here because of some bug in ViewPager-TabLayout which causes them to disappear.
            See https://github.com/dmfs/opentasks/issues/643
            and https://stackoverflow.com/questions/42209046/tablayout-icons-disappear-after-viewpager-refresh */
            setupTabIcons();
            return;
        }
        if (requestCode == REQUEST_CODE_PREFS)
        {
            updateTheme();
            if (Build.VERSION.SDK_INT < 29)
            {
                recreate();
            }
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }


    @Override
    public void onTaskEditRequested(@NonNull Uri taskUri, ContentSet data)
    {
        Intent editTaskIntent = new Intent(Intent.ACTION_EDIT);
        editTaskIntent.setData(taskUri);
        if (data != null)
        {
            Bundle extraBundle = new Bundle();
            extraBundle.putParcelable(EditTaskActivity.EXTRA_DATA_CONTENT_SET, data);
            editTaskIntent.putExtra(EditTaskActivity.EXTRA_DATA_BUNDLE, extraBundle);
        }
        startActivity(editTaskIntent);
    }


    @Override
    public void onTaskDeleted(@NonNull Uri taskUri)
    {
        if (taskUri.equals(mSelectedTaskUri)) // Only the selected task can be deleted on the UI, but just to be safe
        {
            mSelectedTaskUri = null;
            if (mTwoPane)
            {
                // empty the detail fragment
                replaceTaskDetailsFragment(EmptyTaskFragment.newInstance(new ValueColor(mLastUsedColor)));
            }
        }
        // The loader will take care of reloading the list and the list view will take care of selecting the next element.
    }


    @Override
    public void onTaskCompleted(@NonNull Uri taskUri)
    {
        /* TODO We delegate to onTaskDeleted() which was used previously for this event, too.
        This causes the removal of details view, but the task is selected again if completed tasks are shown. This causes a flash. */
        onTaskDeleted(taskUri);
    }


    @SuppressLint("NewApi")
    @Override
    public void onListColorLoaded(@NonNull Color color)
    {
        // nothing to do
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.task_list_activity_menu, menu);

        MenuItem addItem = menu.findItem(R.id.menu_add_task);
        if (addItem != null && mFloatingActionButton != null)
        {
            // hide menu option to add a task if we have a floating action button
            addItem.setVisible(false);
        }

        // search
        setupSearch(menu);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if (id == R.id.menu_add_task)
        {
            onAddNewTask();
            return true;
        }
        else if (item.getItemId() == R.id.menu_visible_list)
        {
            Intent settingsIntent = new Intent(getBaseContext(), SyncSettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        else if (item.getItemId() == R.id.opentasks_menu_app_settings)
        {
            startActivityForResult(new Intent(this, AppSettingsActivity.class), REQUEST_CODE_PREFS);
            return true;
        }
        else
        {
            return super.onOptionsItemSelected(item);
        }
    }


    private void hideSearchActionView()
    {
        MenuItemCompat.collapseActionView(mSearchItem);
    }


    public void setupSearch(Menu menu)
    {
        mSearchItem = menu.findItem(R.id.search);
        MenuItemCompat.setOnActionExpandListener(mSearchItem, new OnActionExpandListener()
        {

            @Override
            public boolean onMenuItemActionExpand(MenuItem item)
            {
                // always allow expansion of the search action view
                return mCurrentPageId == R.id.task_group_search;
            }


            @Override
            public boolean onMenuItemActionCollapse(MenuItem item)
            {
                // return to previous view
                if (mPreviousPagePosition >= 0 && mCurrentPageId == R.id.task_group_search)
                {
                    mViewPager.setCurrentItem(mPreviousPagePosition);
                    mCurrentPageId = mPagerAdapter.getPageId(mPreviousPagePosition);
                }
                return mPreviousPagePosition >= 0 || mCurrentPageId != R.id.task_group_search;
            }
        });
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(mSearchItem);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        if (null != searchManager)
        {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        }

        searchView.setQueryHint(getString(R.string.menu_search_hint));
        searchView.setIconified(true);
        searchView.setOnQueryTextListener(new OnQueryTextListener()
        {

            @Override
            public boolean onQueryTextSubmit(String query)
            {
                // persist current search
                mSearchHistoryHelper.commitSearch();
                mHandler.post(mSearchUpdater);
                return true;
            }


            @Override
            public boolean onQueryTextChange(String query)
            {
                if (mCurrentPageId != R.id.task_group_search)
                {
                    return true;
                }

                mHandler.removeCallbacks(mSearchUpdater);
                if (query.length() > 0)
                {
                    mSearchHistoryHelper.updateSearch(query);
                    mHandler.postDelayed(mSearchUpdater, SEARCH_UPDATE_DELAY);
                }
                else
                {
                    mSearchHistoryHelper.removeCurrentSearch();
                    mHandler.post(mSearchUpdater);
                }
                return true;
            }
        });

        if (mAutoExpandSearchView)
        {
            mSearchItem.expandActionView();
        }

    }


    /**
     * Notifies the search fragment of an update.
     */
    private final Runnable mSearchUpdater = new Runnable()
    {

        @Override
        public void run()
        {
            TaskListFragment fragment = (TaskListFragment) mPagerAdapter.instantiateItem(mViewPager, mViewPager.getCurrentItem());
            fragment.notifyDataSetChanged(true);
            fragment.expandCurrentSearchGroup();
        }
    };


    public Uri getSelectedTaskUri()
    {
        if (mShouldSelectTaskListItem)
        {
            return mSelectedTaskUriOnLaunch;
        }
        return null;
    }


    public boolean isInTransientState()
    {
        return mTransientState;
    }


    @Override
    public Resources.Theme getTheme()
    {
        Resources.Theme theme = super.getTheme();
        if (Build.VERSION.SDK_INT < 29)
        {
            theme.applyStyle(
                    mPrefs.getBoolean(
                            getString(R.string.opentasks_pref_appearance_dark_theme),
                            getResources().getBoolean(R.bool.opentasks_dark_theme_default)) ?
                            R.style.OpenTasks_Theme_Dark :
                            R.style.OpenTasks_Theme_Light,
                    true);
        }
        return theme;
    }
}
