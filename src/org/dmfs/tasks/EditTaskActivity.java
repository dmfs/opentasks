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

import org.dmfs.provider.tasks.TaskContract.Tasks;
import org.dmfs.tasks.model.ContentSet;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;


/**
 * Activity to edit a task.
 * 
 * @author Arjun Naik <arjun@arjunnaik.in>
 * @author Marten Gajda <marten@dmfs.org>
 * @author Tobias Reinsch <tobias@dmfs.org>
 */
public class EditTaskActivity extends FragmentActivity
{
	private EditTaskFragment mEditFragment;

	private String mAuthority;


	@TargetApi(11)
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_task_editor);

		mAuthority = getString(R.string.org_dmfs_tasks_authority);

		if (android.os.Build.VERSION.SDK_INT >= 11)
		{
			// hide up button in action bar
			ActionBar actionBar = getActionBar();
			actionBar.setDisplayShowHomeEnabled(false);
			actionBar.setDisplayHomeAsUpEnabled(false);
			// actionBar.setDisplayShowTitleEnabled(false);
		}

		if (savedInstanceState == null)
		{

			Bundle arguments = new Bundle();
			Intent intent = getIntent();

			if (Intent.ACTION_SEND.equals(intent.getAction()))
			{
				// load data from incoming share intent
				ContentSet sharedContentSet = new ContentSet(Tasks.getContentUri(mAuthority));
				if (intent.hasExtra(Intent.EXTRA_SUBJECT))
				{
					sharedContentSet.put(Tasks.TITLE, intent.getStringExtra(Intent.EXTRA_SUBJECT));
				}
				if (intent.hasExtra(Intent.EXTRA_TITLE))
				{
					sharedContentSet.put(Tasks.TITLE, intent.getStringExtra(Intent.EXTRA_TITLE));
				}
				if (intent.hasExtra(Intent.EXTRA_TEXT))
				{
					String extraText = intent.getStringExtra(Intent.EXTRA_TEXT);
					sharedContentSet.put(Tasks.DESCRIPTION, extraText);
					// check if supplied text is a URL
					if (extraText.startsWith("http://") && !extraText.contains(" "))
					{
						sharedContentSet.put(Tasks.URL, extraText);
					}

				}
				// hand over shared information to EditTaskFragment
				arguments.putParcelable(EditTaskFragment.PARAM_CONTENT_SET, sharedContentSet);
			}
			else
			{
				// hand over task URI for editing / creating empty task
				arguments.putParcelable(EditTaskFragment.PARAM_TASK_URI, getIntent().getData());
			}

			EditTaskFragment fragment = new EditTaskFragment();
			fragment.setArguments(arguments);
			getSupportFragmentManager().beginTransaction().add(R.id.add_task_container, fragment).commit();

		}

	}


	@Override
	public void onAttachFragment(Fragment fragment)
	{
		super.onAttachFragment(fragment);
		if (fragment instanceof EditTaskFragment)
		{
			mEditFragment = (EditTaskFragment) fragment;
		}
		else
		{
			throw new IllegalArgumentException("Invalid fragment attached: " + fragment.getClass().getCanonicalName());
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.edit_task_activity_menu, menu);
		return true;
	}


	@Override
	public void onBackPressed()
	{
		super.onBackPressed();

		if (mEditFragment != null)
		{
			mEditFragment.saveAndExit();
		}
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case android.R.id.home:
				NavUtils.navigateUpFromSameTask(this);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
