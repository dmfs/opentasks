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
import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import org.dmfs.android.colorpicker.ColorPickerDialogFragment;
import org.dmfs.android.colorpicker.palettes.ArrayPalette;
import org.dmfs.android.colorpicker.palettes.ColorFactory;
import org.dmfs.android.colorpicker.palettes.ColorShadeFactory;
import org.dmfs.android.colorpicker.palettes.CombinedColorFactory;
import org.dmfs.android.colorpicker.palettes.FactoryPalette;
import org.dmfs.android.colorpicker.palettes.Palette;
import org.dmfs.android.colorpicker.palettes.RainbowColorFactory;
import org.dmfs.android.colorpicker.palettes.RandomPalette;
import org.dmfs.android.retentionmagic.annotations.Retain;
import org.dmfs.tasks.InputTextDialogFragment.InputTextListener;
import org.dmfs.tasks.contract.TaskContract;
import org.dmfs.tasks.contract.TaskContract.TaskLists;
import org.dmfs.tasks.utils.BaseActivity;


/**
 * Activity to create and edit local task lists. This activity provides an interface to edit the name and the color of a local list.
 *
 * @author Tristan Heinig <tristan@dmfs.org>
 */
public class ManageListActivity extends BaseActivity implements OnClickListener, InputTextListener, android.content.DialogInterface.OnClickListener, ColorPickerDialogFragment.ColorDialogResultListener
{
    /**
     * Account, that is assigned to the task list.
     */
    public static final String EXTRA_ACCOUNT = "dmfs_extra_account";
    /**
     * Intent filter category to handle local lists only.
     */
    public static final String CATEGORY_LOCAL = "org.dmfs.intent.category.LOCAL";
    private static final int NO_COLOR = -1;

    private static final int[] MATERIAL_COLORS_PRIMARY = new int[] {
            -1499549, -769226, -43230, -26624, -16121, -5317, -3285959, -7617718, -11751600, -16738680, -16728876, -16537100, -14575885, -12627531, -10011977,
            -6543440 };
    private static final int[] MATERIAL_COLORS_DARK = new int[] {
            -5434281, -3790808, -2604267, -1086464, -28928, -415707, -6382300, -11171025, -13730510, -16750244, -16743537, -16615491, -15374912, -14142061,
            -12245088, -9823334 };
    private static final Palette[] PALETTES = new Palette[] {
            new ArrayPalette("material_primary", "Material Colors", MATERIAL_COLORS_PRIMARY),
            new ArrayPalette("material_secondary", "Dark Material Colors", MATERIAL_COLORS_DARK),
            new FactoryPalette("red", "Red", new CombinedColorFactory(new ColorShadeFactory(340.0F), ColorFactory.RED), 16),
            new FactoryPalette("orange", "Orange", new CombinedColorFactory(new ColorShadeFactory(18.0F), ColorFactory.ORANGE), 16),
            new FactoryPalette("yellow", "Yellow", new CombinedColorFactory(new ColorShadeFactory(53.0F), ColorFactory.YELLOW), 16),
            new FactoryPalette("green", "Green", new CombinedColorFactory(new ColorShadeFactory(80.0F), ColorFactory.GREEN), 16),
            new FactoryPalette("cyan", "Cyan", new CombinedColorFactory(new ColorShadeFactory(150.0F), ColorFactory.CYAN), 16),
            new FactoryPalette("blue", "Blue", new CombinedColorFactory(new ColorShadeFactory(210.0F), ColorFactory.BLUE), 16),
            new FactoryPalette("purple", "Purple", new CombinedColorFactory(new ColorShadeFactory(265.0F), ColorFactory.PURPLE), 16),
            new FactoryPalette("pink", "Pink", new CombinedColorFactory(new ColorShadeFactory(300.0F), ColorFactory.PINK), 16),
            new FactoryPalette("grey", "Grey", ColorFactory.GREY, 16), new FactoryPalette("pastel", "Pastel", ColorFactory.PASTEL, 16),
            new FactoryPalette("rainbow", "Rainbow", ColorFactory.RAINBOW, 16),
            new FactoryPalette("dark_rainbow", "Dark Rainbow", new RainbowColorFactory(0.5F, 0.5F), 16) };

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

    @Retain(
            classNS = "ManageList",
            key = "palette",
            permanent = true
    )
    private String mPaletteId = null;


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
        getWindow().setAttributes(params);

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
     *         saved activity state from {@link #onCreate(Bundle)}
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
                    new String[] {
                            TaskContract.TaskLists._ID, TaskContract.TaskLists.LIST_NAME, TaskContract.TaskLists.LIST_COLOR,
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
     *         saved activity state from {@link #onCreate(Bundle)}
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
            mListColor = palette.colorAt(0);
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
        }
        // click on delete
        else if (android.R.id.button2 == v.getId())
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
        }
        else if (R.id.color_setting == v.getId())
        {
            ColorPickerDialogFragment d = new ColorPickerDialogFragment();
            d.setPalettes(PALETTES);
            d.setTitle(org.dmfs.android.colorpicker.R.string.org_dmfs_colorpicker_pick_a_color);
            d.selectPaletteId(this.mPaletteId);
            d.show(this.getSupportFragmentManager(), "");
        }
        else if (R.id.name_setting == v.getId())
        {
            InputTextDialogFragment dialog = InputTextDialogFragment.newInstance(getString(R.string.task_list_name_dialog_title),
                    getString(R.string.task_list_name_dialog_hint), mNameView.getText().toString());
            dialog.show(getSupportFragmentManager(), null);
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
                        .appendQueryParameter(TaskContract.ACCOUNT_TYPE, mAccount.type).appendQueryParameter(TaskContract.ACCOUNT_NAME, mAccount.name).build(),
                values);
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
                        .appendQueryParameter(TaskContract.ACCOUNT_TYPE, mAccount.type).appendQueryParameter(TaskContract.ACCOUNT_NAME, mAccount.name).build(),
                values,
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
                        .appendQueryParameter(TaskContract.ACCOUNT_TYPE, mAccount.type).appendQueryParameter(TaskContract.ACCOUNT_NAME, mAccount.name).build(),
                null,
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


    @Override
    public void onColorChanged(int i, String paletteId, String s1, String s2)
    {
        this.mPaletteId = paletteId;
        mListColor = i;
        mColorView.setBackgroundColor(mListColor);
    }


    @Override
    public void onColorDialogCancelled()
    {

    }
}
