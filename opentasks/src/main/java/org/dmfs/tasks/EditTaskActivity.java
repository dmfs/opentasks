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
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.core.app.NavUtils;
import androidx.appcompat.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;

import org.dmfs.provider.tasks.AuthorityUtil;
import org.dmfs.tasks.contract.TaskContract.Tasks;
import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.utils.BaseActivity;

import java.util.TimeZone;


/**
 * Activity to edit a task.
 *
 * @author Arjun Naik <arjun@arjunnaik.in>
 * @author Marten Gajda <marten@dmfs.org>
 * @author Tobias Reinsch <tobias@dmfs.org>
 */
public class EditTaskActivity extends BaseActivity
{
    private static final String ACTION_NOTE_TO_SELF = "com.google.android.gm.action.AUTO_SEND";

    public final static String EXTRA_DATA_BUNDLE = "org.dmfs.extra.BUNDLE";

    public final static String EXTRA_DATA_CONTENT_SET = "org.dmfs.DATA";

    public final static String EXTRA_DATA_ACCOUNT_TYPE = "org.dmfs.ACCOUNT_TYPE";

    private EditTaskFragment mEditFragment;

    private String mAuthority;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_editor);

        mAuthority = AuthorityUtil.taskAuthority(this);

        // hide up button in action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.content_remove_light);
        // actionBar.setDisplayShowTitleEnabled(false);

        Intent intent = getIntent();
        String action = intent.getAction();

        setActivityTitle(action);

        if (savedInstanceState == null)
        {
            Bundle arguments = new Bundle();

            if (Intent.ACTION_SEND.equals(action))
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
            else if (ACTION_NOTE_TO_SELF.equals(action))
            {
                // process the note to self intent
                ContentSet sharedContentSet = new ContentSet(Tasks.getContentUri(mAuthority));

                if (intent.hasExtra(Intent.EXTRA_SUBJECT))
                {
                    sharedContentSet.put(Tasks.DESCRIPTION, intent.getStringExtra(Intent.EXTRA_SUBJECT));
                }

                if (intent.hasExtra(Intent.EXTRA_TEXT))
                {
                    String extraText = intent.getStringExtra(Intent.EXTRA_TEXT);
                    sharedContentSet.put(Tasks.TITLE, extraText);

                }

                // add start time stamp
                sharedContentSet.put(Tasks.DTSTART, System.currentTimeMillis());
                sharedContentSet.put(Tasks.TZ, TimeZone.getDefault().getID());

                // hand over shared information to EditTaskFragment
                arguments.putParcelable(EditTaskFragment.PARAM_CONTENT_SET, sharedContentSet);

            }
            else
            {
                // hand over task URI for editing / creating empty task
                arguments.putParcelable(EditTaskFragment.PARAM_TASK_URI, getIntent().getData());
                Bundle extraBundle = getIntent().getBundleExtra(EXTRA_DATA_BUNDLE);
                if (extraBundle != null)
                {
                    ContentSet data = extraBundle.getParcelable(EXTRA_DATA_CONTENT_SET);
                    if (data != null)
                    {
                        arguments.putParcelable(EditTaskFragment.PARAM_CONTENT_SET, data);
                    }
                }
                String accountType = getIntent().getStringExtra(EXTRA_DATA_ACCOUNT_TYPE);
                if (accountType != null)
                {
                    arguments.putString(EditTaskFragment.PARAM_ACCOUNT_TYPE, accountType);
                }
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
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void setActivityTitle(String action)
    {
        if (Intent.ACTION_EDIT.equals(action))
        {
            setTitle(R.string.activity_edit_task_title);
        }
        else
        {
            setTitle(R.string.activity_add_task_title);
        }
    }

}
