/*
 * Copyright (C) 2015 Marten Gajda <marten@dmfs.org>
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

import org.dmfs.android.colorpicker.ColorPickerActivity;
import org.dmfs.android.colorpicker.palettes.RandomPalette;
import org.dmfs.android.retentionmagic.annotations.Retain;
import org.dmfs.provider.tasks.TaskContract;
import org.dmfs.provider.tasks.TaskContract.TaskLists;
import org.dmfs.tasks.InputTextDialogFragment.InputTextListener;
import org.dmfs.tasks.utils.ActionBarActivity;

import android.accounts.Account;
import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;


/**
 * Activity to create and edit local task lists. This activity provides an interface to edit the name and the color of a local list.
 * 
 * @author Tristan Heinig <tristan@dmfs.org>
 * 
 */
public class ManageListActivity extends ActionBarActivity implements OnClickListener, InputTextListener, android.content.DialogInterface.OnClickListener
{
    /**
     * Action to call the ColorPicker activity.
     */
    public static final String ACTION_PICK_COLOR = "org.openintents.action.PICK_COLOR";
    /**
     * Account, that is assigned to the task list.
     */
    public static final String EXTRA_ACCOUNT = "dmfs_extra_account";
    /**
     * Intent filter category to handle local lists only.
     */
    public static final String CATEGORY_LOCAL = "org.dmfs.intent.category.LOCAL";
    private static final int REQUEST_CODE_COLOR_PICKER = 4465;
    private static final int NO_COLOR = -1;

    @Retain
    private int mListColor = NO_COLOR;
    private boolean mStateInsert;
    private String mAction;
    @Retain
    private String mListName;
    private Uri mTaskListUri;
    private Account mAccount;
    private TextView mNameView;
    private View mColorView;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        mTaskListUri = intent.getData();
        mAction = intent.getAction();
        mAccount = intent.getParcelableExtra(EXTRA_ACCOUNT);
        if (mTaskListUri == null || mAction == null || mAccount == null)
        {
            setResult(Activity.RESULT_CANCELED);
            finish();
            return;
        }
        setContentView(R.layout.activity_manage_task_list);

        LayoutParams params = getWindow().getAttributes();
        params.height = LayoutParams.WRAP_CONTENT;
        params.width = LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

        findViewById(R.id.color_setting).setOnClickListener(this);
        findViewById(R.id.name_setting).setOnClickListener(this);
        mNameView = (TextView) findViewById(R.id.list_name);
        mColorView = findViewById(R.id.list_color);

        if (Intent.ACTION_EDIT.equals(mAction))
        {
            initEditing(savedInstanceState);
            return;
        }

        if (Intent.ACTION_INSERT.equals(mAction))
        {
            initInsert(savedInstanceState);
        }

    }


    /**
     * Initializes the user interface for editing tasks.
     * 
     * @param savedInstanceState
     *            saved activity state from {@link #onCreate(Bundle)}
     */
    private void initEditing(Bundle savedInstanceState)
    {
        mStateInsert = false;
        findViewById(android.R.id.button2).setOnClickListener(this);
        findViewById(android.R.id.button3).setOnClickListener(this);
        setTitle(R.string.activity_edit_list_title);

        if (savedInstanceState == null)
        {
            Cursor cursor = getContentResolver().query(
                mTaskListUri,
                new String[] { TaskContract.TaskLists._ID, TaskContract.TaskLists.LIST_NAME, TaskContract.TaskLists.LIST_COLOR,
                    TaskContract.TaskLists.ACCOUNT_NAME }, null, null, null);
            if (cursor == null || cursor.getCount() < 1)
            {
                setResult(Activity.RESULT_CANCELED);
                finish();
                return;
            }
            cursor.moveToNext();
            if (mListName == null)
            {
                mListName = cursor.getString(cursor.getColumnIndex(TaskContract.TaskLists.LIST_NAME));
            }
            if (mListColor == NO_COLOR)
            {
                mListColor = cursor.getInt(cursor.getColumnIndex(TaskContract.TaskLists.LIST_COLOR));
            }
        }
        mNameView.setText(mListName);
        mColorView.setBackgroundColor(mListColor);
    }


    /**
     * Initializes the user interface for creating tasks.
     * 
     * @param savedInstanceState
     *            saved activity state from {@link #onCreate(Bundle)}
     */
    private void initInsert(Bundle savedInstanceState)
    {
        mStateInsert = true;
        findViewById(android.R.id.button2).setVisibility(View.GONE);
        findViewById(android.R.id.button3).setOnClickListener(this);
        setTitle(R.string.activity_add_list_title);

        if (savedInstanceState == null)
        {
            InputTextDialogFragment dialog = InputTextDialogFragment.newInstance(getString(R.string.task_list_name_dialog_title),
                getString(R.string.task_list_name_dialog_hint), null, getString(R.string.task_list_no_sync));
            dialog.show(getSupportFragmentManager(), null);
        }
        if (mListColor == NO_COLOR)
        {
            RandomPalette palette = new RandomPalette("generate list color", "random colors", 1);
            mListColor = palette.getColor(0);
        }
        mNameView.setText(mListName);
        mColorView.setBackgroundColor(mListColor);

    }


    @Override
    public void onClick(View v)
    {
        // click on save
        if (android.R.id.button3 == v.getId())
        {
            if (Intent.ACTION_INSERT.equals(mAction))
            {
                createList();
            }
            else if (Intent.ACTION_EDIT.equals(mAction))
            {
                updateList();
            }
            return;
        }
        // click on delete
        if (android.R.id.button2 == v.getId())
        {
            final AlertDialog dialog = new AlertDialog.Builder(this).setTitle(getString(R.string.task_list_delete_dialog_title, mListName))
                .setMessage(R.string.task_list_delete_dialog_message).setPositiveButton(R.string.activity_manage_list_btn_delete, this)
                .setNegativeButton(android.R.string.cancel, this).create();
            // changes the color of the delete list button to red
            dialog.setOnShowListener(new OnShowListener()
            {
                @Override
                public void onShow(DialogInterface arg0)
                {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.holo_red_light));
                }
            });
            dialog.show();
            return;
        }
        if (R.id.color_setting == v.getId())
        {
            Intent intent = new Intent(ACTION_PICK_COLOR);
            startActivityForResult(intent, REQUEST_CODE_COLOR_PICKER);
            return;
        }
        if (R.id.name_setting == v.getId())
        {
            InputTextDialogFragment dialog = InputTextDialogFragment.newInstance(getString(R.string.task_list_name_dialog_title),
                getString(R.string.task_list_name_dialog_hint), mNameView.getText().toString());
            dialog.show(getSupportFragmentManager(), null);
            return;
        }
    }


    /*
     * Belongs to the delete task confirm dialog. It is fired when the user clicks on the delete (positive) or cancel (negative) button.
     */
    @Override
    public void onClick(DialogInterface dialog, int which)
    {
        if (DialogInterface.BUTTON_POSITIVE == which)
        {
            deleteList();
        }
    }


    /**
     * Creates a list out of the given user input and insert it to the database. After that the activity will be closed with RESULT_OK.
     */
    private void createList()
    {
        ContentValues values = new ContentValues();
        values.put(TaskLists.LIST_NAME, mNameView.getText().toString());
        values.put(TaskLists.LIST_COLOR, mListColor | 0xff000000);
        values.put(TaskLists.VISIBLE, 1);
        values.put(TaskLists.SYNC_ENABLED, 1);
        values.put(TaskLists.OWNER, "");
        getContentResolver().insert(
            mTaskListUri.buildUpon().appendQueryParameter(TaskContract.CALLER_IS_SYNCADAPTER, "1")
                .appendQueryParameter(TaskContract.ACCOUNT_TYPE, mAccount.type).appendQueryParameter(TaskContract.ACCOUNT_NAME, mAccount.name).build(), values);
        setResult(Activity.RESULT_OK);
        finish();
    }


    /**
     * Takes the given user input and update a particular task list of the database. After that the activity will be closed with RESULT_OK, if the update was
     * successful, otherwise with RESULT_CANCELED.
     */
    private void updateList()
    {
        ContentValues values = new ContentValues();
        values.put(TaskLists.LIST_NAME, mNameView.getText().toString());
        values.put(TaskLists.LIST_COLOR, mListColor | 0xff000000);
        values.put(TaskLists.VISIBLE, 1);
        values.put(TaskLists.SYNC_ENABLED, 1);
        values.put(TaskLists.OWNER, "");
        int count = getContentResolver().update(
            mTaskListUri.buildUpon().appendQueryParameter(TaskContract.CALLER_IS_SYNCADAPTER, "1")
                .appendQueryParameter(TaskContract.ACCOUNT_TYPE, mAccount.type).appendQueryParameter(TaskContract.ACCOUNT_NAME, mAccount.name).build(), values,
            null, null);
        if (count > 0)
        {
            setResult(Activity.RESULT_OK);
            finish();
            return;
        }
        setResult(Activity.RESULT_CANCELED);
        finish();
    }


    /**
     * Removes a particular task list from the database. After that the activity will be closed with RESULT_OK, if the remove was successful, otherwise with
     * RESULT_CANCELED.
     */
    private void deleteList()
    {
        int count = getContentResolver().delete(
            mTaskListUri.buildUpon().appendQueryParameter(TaskContract.CALLER_IS_SYNCADAPTER, "1")
                .appendQueryParameter(TaskContract.ACCOUNT_TYPE, mAccount.type).appendQueryParameter(TaskContract.ACCOUNT_NAME, mAccount.name).build(), null,
            null);
        if (count > 0)
        {
            setResult(Activity.RESULT_OK);
            Toast.makeText(this, getString(R.string.task_list_delete_toast, mListName), Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        setResult(Activity.RESULT_CANCELED);
        finish();
    }


    /*
     * If we called the ColerPicker before, we will receive the chosen color here.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (REQUEST_CODE_COLOR_PICKER == requestCode)
        {
            if (Activity.RESULT_OK == resultCode)
            {
                if (data != null && data.hasExtra(ColorPickerActivity.EXTRA_COLOR))
                {
                    mListColor = data.getIntExtra(ColorPickerActivity.EXTRA_COLOR, -1);
                    if (mNameView != null)
                    {
                        mColorView.setBackgroundColor(mListColor);
                    }
                }
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onInputTextChanged(String inputText)
    {
        mStateInsert = false;
        mNameView.setText(inputText);
        mListName = inputText;
    }


    /*
     * If the activity is started to create a new task, the user see the InputDialog for editing task list names first. If he cancels the dialog, we finish the
     * activity also.
     */
    @Override
    public void onCancelInputDialog()
    {
        if (mStateInsert == true)
        {
            setResult(Activity.RESULT_CANCELED);
            finish();
        }
    }

}
