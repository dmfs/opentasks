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

package org.dmfs.tasks.homescreen;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import org.dmfs.tasks.R;
import org.dmfs.tasks.homescreen.TaskListSelectionFragment.OnSelectionListener;
import org.dmfs.tasks.utils.BaseActivity;
import org.dmfs.tasks.utils.WidgetConfigurationDatabaseHelper;

import java.util.ArrayList;


/**
 * Allows to configure the task list widget prior to adding to the home screen
 *
 * @author Tobias Reinsch <tobias@dmfs.org>
 */
public class TaskListWidgetSettingsActivity extends BaseActivity implements OnSelectionListener
{
    private int mAppWidgetId;
    private Intent mResultIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widget_configuration);

        Intent intent = getIntent();
        if (intent.hasExtra(AppWidgetManager.EXTRA_APPWIDGET_ID))
        {
            mAppWidgetId = intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);

            // make the result intent and set the result to canceled
            mResultIntent = new Intent();
            mResultIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_CANCELED, mResultIntent);
        }

        getSupportFragmentManager().beginTransaction()
                .add(R.id.task_list_selection_container, new TaskListSelectionFragment())
                .commit();
    }


    @Override
    public void onSelection(ArrayList<Long> selectedLists)
    {
        persistSelectedTaskLists(selectedLists);
        finishWithResult(true);

    }


    @Override
    public void onSelectionCancel()
    {
        finishWithResult(false);

    }


    private void persistSelectedTaskLists(ArrayList<Long> lists)
    {
        WidgetConfigurationDatabaseHelper dbHelper = new WidgetConfigurationDatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // delete old configuration
        WidgetConfigurationDatabaseHelper.deleteConfiguration(db, mAppWidgetId);

        // add new configuration
        for (Long taskId : lists)
        {
            WidgetConfigurationDatabaseHelper.insertTaskList(db, mAppWidgetId, taskId);
        }
        db.close();
    }


    private void finishWithResult(boolean ok)
    {
        Bundle bundle = new Bundle();
        bundle.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        Intent intent = new Intent();
        intent.putExtras(bundle);

        if (ok)
        {
            setResult(RESULT_OK, intent);
        }
        else
        {
            setResult(RESULT_CANCELED, intent);
        }

        finish();
    }
}
