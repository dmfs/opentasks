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

import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.utils.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;


/**
 * An activity representing a single Task detail screen. This activity is only used on handset devices. On tablet-size devices, item details are presented
 * side-by-side with a list of items in a {@link TaskListActivity}.
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing more than a {@link ViewTaskFragment}.
 * </p>
 */
public class ViewTaskActivity extends AppCompatActivity implements ViewTaskFragment.Callback
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_task_detail);

		// If should be in two-pane mode, finish to return to main activity
		if (getResources().getBoolean(R.bool.has_two_panes))
		{

			Intent taskListIntent = new Intent(this, TaskListActivity.class);
			taskListIntent.putExtra(TaskListActivity.EXTRA_FORCE_LIST_SELECTION, getIntent()
				.getBooleanExtra(TaskListActivity.EXTRA_FORCE_LIST_SELECTION, false));
			taskListIntent.putExtra(TaskListActivity.EXTRA_DISPLAY_TASK, true);
			taskListIntent.setData(getIntent().getData());
			startActivity(taskListIntent);
			finish();
			return;
		}

		if (savedInstanceState == null)
		{
			ViewTaskFragment fragment = ViewTaskFragment.newInstance(getIntent().getData());
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
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	public void onEditTask(Uri taskUri, ContentSet data)
	{
		Intent editTaskIntent = new Intent(Intent.ACTION_EDIT);
		editTaskIntent.setData(taskUri);
		if (data != null)
		{
			editTaskIntent.putExtra(EditTaskActivity.EXTRA_DATA_CONTENT_SET, data);
		}
		startActivity(editTaskIntent);
	}


	@Override
	public void onDelete(Uri taskUri)
	{
		/*
		 * The task we're showing has been deleted, just finish.
		 */
		finish();
	}


	private int darkenColor(int color)
	{
		float[] hsv = new float[3];
		Color.colorToHSV(color, hsv);
		hsv[2] = hsv[2] * 0.75f;
		color = Color.HSVToColor(hsv);
		return color;
	}


	@SuppressLint("NewApi")
	@Override
	public void updateColor(int color)
	{

		if (VERSION.SDK_INT >= 21)
		{
			Window window = getWindow();
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.setStatusBarColor(darkenColor(color));
		}
	}

}
