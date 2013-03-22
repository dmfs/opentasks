/*
 * Copyright (C) 2012 Marten Gajda <marten@dmfs.org>
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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;


/**
 * An activity representing a single Task detail screen. This activity is only used on handset devices. On tablet-size devices, item details are presented
 * side-by-side with a list of items in a {@link TaskListActivity}.
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing more than a {@link ViewTaskFragment}.
 * </p>
 */
public class ViewTaskActivity extends FragmentActivity implements ViewTaskFragment.Callback
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_task_detail);

		// Show the Up button in the action bar.
		if (android.os.Build.VERSION.SDK_INT >= 11)
		{
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}

		if (savedInstanceState == null)
		{
			ViewTaskFragment fragment = new ViewTaskFragment();
			getSupportFragmentManager().beginTransaction().add(R.id.task_detail_container, fragment).commit();
		}
	}


	@Override
	public void onAttachFragment(Fragment fragment)
	{
		if (fragment instanceof ViewTaskFragment)
		{
			ViewTaskFragment detailFragment = (ViewTaskFragment) fragment;
			detailFragment.loadUri(getIntent().getData());
		}
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case android.R.id.home:
				NavUtils.navigateUpTo(this, new Intent(this, TaskListActivity.class));
				return true;
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	public void onEditTask(Uri taskUri)
	{

		Intent editTaskIntent = new Intent(Intent.ACTION_EDIT);
		editTaskIntent.setData(taskUri);
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

}
