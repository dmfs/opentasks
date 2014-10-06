/*
 * Copyright (C) 2014 Marten Gajda <marten@dmfs.org>
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

package org.dmfs.tasks.homescreen;

import java.util.Collection;

import org.dmfs.tasks.R;
import org.dmfs.tasks.homescreen.TaskListSelectionFragment.onSelectionListener;
import org.dmfs.tasks.model.TaskList;
import org.dmfs.tasks.utils.WidgetConfigurationDatabaseHelper;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;


/**
 * Allows to configure the task list widget prior to adding to the home screen
 * 
 * @author Tobias Reinsch <tobias@dmfs.org>
 * 
 */
public class TaskListWidgetSettingsActivity extends FragmentActivity implements onSelectionListener
{
	private int mAppWidgetId;


	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_widget_configuration);

		Intent intent = getIntent();
		if (intent.hasExtra(AppWidgetManager.EXTRA_APPWIDGET_ID))
		{
			mAppWidgetId = intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
		}

		TaskListSelectionFragment fragment = new TaskListSelectionFragment(this);
		getSupportFragmentManager().beginTransaction().add(R.id.task_list_selection_container, fragment).commit();

	}


	@Override
	public void onSelection(Collection<TaskList> selectedLists)
	{
		persistSelectedTaskLists(selectedLists);
		finishWithResult(true);

	}


	@Override
	public void onSelectionCancel()
	{
		finishWithResult(false);

	}


	private void persistSelectedTaskLists(Collection<TaskList> lists)
	{
		WidgetConfigurationDatabaseHelper dbHelper = new WidgetConfigurationDatabaseHelper(this);
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		// delete old configuration
		WidgetConfigurationDatabaseHelper.deleteConfiguration(db, mAppWidgetId);

		// add new configuration
		for (TaskList taskList : lists)
		{
			WidgetConfigurationDatabaseHelper.insertTaskList(db, mAppWidgetId, taskList.accountName, taskList.listName);
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
