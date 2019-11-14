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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import org.dmfs.android.bolts.color.Color;
import org.dmfs.android.bolts.color.colors.PrimaryColor;
import org.dmfs.android.bolts.color.elementary.ValueColor;
import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.utils.BaseActivity;
import org.dmfs.tasks.utils.colors.DarkenedForStatusBar;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;


/**
 * An activity representing a single Task detail screen. This activity is only used on handset devices. On tablet-size devices, item details are presented
 * side-by-side with a list of items in a {@link TaskListActivity}.
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing more than a {@link ViewTaskFragment}.
 * </p>
 */
public class ViewTaskActivity extends BaseActivity implements ViewTaskFragment.Callback
{

    /**
     * The {@link ColorInt} the toolbars should take while loading the task. Optional parameter.
     * {@link android.graphics.Color#TRANSPARENT} also means absent.
     */
    public static final String EXTRA_COLOR = "color";


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        // If should be in two-pane mode, finish to return to main activity
        if (getResources().getBoolean(R.bool.has_two_panes))
        {

            Intent taskListIntent = new Intent(this, TaskListActivity.class);
            taskListIntent.putExtra(TaskListActivity.EXTRA_FORCE_LIST_SELECTION,
                    getIntent().getBooleanExtra(TaskListActivity.EXTRA_FORCE_LIST_SELECTION, false));
            taskListIntent.putExtra(TaskListActivity.EXTRA_DISPLAY_TASK, true);
            taskListIntent.setData(getIntent().getData());
            startActivity(taskListIntent);
            finish();
            return;
        }

        if (savedInstanceState == null)
        {
            int color = getIntent().getIntExtra(EXTRA_COLOR, 0);
            ViewTaskFragment fragment = ViewTaskFragment.newInstance(
                    getIntent().getData(), color != 0 ? new ValueColor(color) : new PrimaryColor(this));
            getSupportFragmentManager().beginTransaction().add(R.id.task_detail_container, fragment).commit();
        }
    }


    @Override
    public void onAttachFragment(Fragment fragment)
    {
        if (fragment instanceof ViewTaskFragment)
        {
            final ViewTaskFragment detailFragment = (ViewTaskFragment) fragment;
            new Handler().post(new Runnable()
            {
                @Override
                public void run()
                {
                    detailFragment.setupToolbarAsActionbar(ViewTaskActivity.this);
                }
            });
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                Intent upIntent = new Intent(this, TaskListActivity.class);
                // provision the task uri, so the main activity will be opened with the right task selected
                upIntent.setData(getIntent().getData());
                upIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(upIntent);
                finish();
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
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
        // The task we're showing has been deleted, just finish.
        finish();
    }


    @Override
    public void onTaskCompleted(@NonNull Uri taskUri)
    {
        // The task we're showing has been completed, just finish.
        finish();
    }


    @Override
    public void onListColorLoaded(@NonNull Color color)
    {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(new DarkenedForStatusBar(color).argb());
    }

}
