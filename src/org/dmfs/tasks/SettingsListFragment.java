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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.dmfs.android.widgets.ColoredShapeCheckBox;
import org.dmfs.provider.tasks.TaskContract;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;


/**
 * This fragment is used to display a list of task-providers. It show the task-providers which are visible in main {@link TaskListFragment} and also the
 * task-providers which are synced. The selection between the two lists is made by passing arguments to the fragment in a {@link Bundle} when it created in the
 * {@link SyncSettingsActivity}.
 * 
 * @author Arjun Naik<arjun@arjunnaik.in>
 */
public class SettingsListFragment extends ListFragment implements AbsListView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor>,
    android.content.DialogInterface.OnClickListener
{
    public static final String LIST_SELECTION_ARGS = "list_selection_args";
    public static final String LIST_STRING_PARAMS = "list_string_params";
    public static final String LIST_FRAGMENT_LAYOUT = "list_fragment_layout";
    public static final String LIST_ONDETACH_SAVE = "list_ondetach_save";
    public static final String COMPARE_COLUMN_NAME = "column_name";

    /**
     * Color picker action.
     */

    public final static int REQUEST_CODE_MANAGE_LIST = 3345;

    private Context mContext;
    private VisibleListAdapter mAdapter;

    private String mListSelectionArguments;
    private String[] mListSelectionParam;
    private String mListCompareColumnName;
    private boolean mSaveOnDetach;
    private int mFragmentLayout;
    private String mAuthority;

    /**
     * A cache, that holds a list of account types, which support an intent to insert task lists.
     */
    private HashMap<String, Intent> cachedInsertIntents = new HashMap<String, Intent>();
    /**
     * A cache, that holds a list of account types, which support an intent to edit task lists.
     */
    private HashMap<String, Intent> cachedEditIntents = new HashMap<String, Intent>();
    /**
     * A dialog, that shows a list of accounts, which support the insert intent.
     */
    private AlertDialog mChooseAccountToAddListDialog;
    /**
     * An adapter, that holds the accounts, which support the insert intent.
     */
    private AccountAdapter mAccountAdapter;


    public SettingsListFragment()
    {

    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    /**
     * The SQL selection condition used to select synced or visible list, the parameters for the select condition, the layout to be used and the column which is
     * used for current selection is passed through a {@link Bundle}. The fragment layout is inflated and returned.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Bundle args = getArguments();
        mListSelectionArguments = args.getString(LIST_SELECTION_ARGS);
        mListSelectionParam = args.getStringArray(LIST_STRING_PARAMS);
        mFragmentLayout = args.getInt(LIST_FRAGMENT_LAYOUT);
        mSaveOnDetach = args.getBoolean(LIST_ONDETACH_SAVE);
        mListCompareColumnName = args.getString(COMPARE_COLUMN_NAME);
        View view = inflater.inflate(mFragmentLayout, container, false);
        return view;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().restartLoader(-2, null, this);
        mAdapter = new VisibleListAdapter(mContext, null, 0);
        mAccountAdapter = new AccountAdapter();
        setListAdapter(mAdapter);
        getListView().setOnItemClickListener(this);
    }


    @Override
    public void onResume()
    {
        super.onResume();
        
        // this happens in onResume, because if the user changed its account settings, onResume is called after he returns.
        AccountManager am = AccountManager.get(getActivity());
        Account[] accounts = am.getAccounts();
        // we need a modifiable list
        ArrayList<Account> accountList = new ArrayList<Account>(Arrays.asList(accounts));

        mAccountAdapter.clear();

        // Adds the default local account
        accountList.add(new Account("Local", TaskContract.LOCAL_ACCOUNT));

        for (Account account : accountList)
        {
            // insert action
            Intent insertIntent = new Intent();
            insertIntent.setAction(Intent.ACTION_INSERT);
            insertIntent.setDataAndType(TaskContract.TaskLists.getContentUri(mAuthority), ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + mAuthority
                + ".tasklists");
            insertIntent.addCategory(buildCategoryFrom(account.type));
            insertIntent.putExtra(ManageListActivity.EXTRA_ACCOUNT, account);
            ComponentName insertComponent = insertIntent.resolveActivity(getActivity().getPackageManager());
            if (insertComponent != null)
            {
                cachedInsertIntents.put(account.type, insertIntent);
                mAccountAdapter.addAccount(account);
            }
            // edit action
            Intent editIntent = new Intent();
            editIntent.setAction(Intent.ACTION_EDIT);
            editIntent.setDataAndType(TaskContract.TaskLists.getContentUri(mAuthority), ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + mAuthority + ".tasklists");
            editIntent.addCategory(buildCategoryFrom(account.type));
            editIntent.putExtra(ManageListActivity.EXTRA_ACCOUNT, account);
            ComponentName editComponent = editIntent.resolveActivity(getActivity().getPackageManager());
            if (editComponent != null)
            {
                cachedEditIntents.put(account.type, editIntent);
            }
        }
        // create a new dialog, that shows accounts for inserting task lists
        mChooseAccountToAddListDialog = new AlertDialog.Builder(getActivity()).setTitle(R.string.task_list_settings_dialog_account_title)
            .setAdapter(mAccountAdapter, this).create();
    }


    /*
     * Is called, when the user clicks on an account of the 'mChooseAccountToAddListDialog' dialog
     */
    @Override
    public void onClick(DialogInterface dialog, int which)
    {
        Account selectedAccount = mAccountAdapter.getItem(which);
        if (selectedAccount == null)
        {
            return;
        }
        // Is there any component, that supports task list creation for this account?
        Intent intent = cachedInsertIntents.get(selectedAccount.type);
        if (intent == null)
        {
            return;
        }
        // start the component
        startActivityForResult(intent, REQUEST_CODE_MANAGE_LIST);
    }


    /*
     * Adds an action to the ActionBar to create local lists.
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.list_settings_menu, menu);
    }


    /*
     * Called, when the user clicks on an ActionBar item
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (R.id.action_add_local_list == item.getItemId())
        {
            onAddListClick();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Is called, when the user clicks the action to create a local list. It will start the component, that handles the list creation. If there are more than
     * one, it will show a list of assigned accounts before.
     */
    private void onAddListClick()
    {
        try
        {
            if (cachedInsertIntents.size() == 1)
            {
                Intent intent = cachedInsertIntents.values().iterator().next();
                startActivityForResult(intent, REQUEST_CODE_MANAGE_LIST);
                return;
            }
            mChooseAccountToAddListDialog.show();

        }
        catch (ActivityNotFoundException e)
        {
            Toast.makeText(getActivity(), "No activity found to edit list", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Builds an intent filter category from a given accountType. This category is used to identify components which supports the creation and editing of task
     * lists.
     * 
     * @param accountType
     *            Account type of an {@link Account}.
     * @return The category as String.
     */
    private String buildCategoryFrom(String accountType)
    {
        return "org.dmfs.intent.category." + accountType.trim();
    }


    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        mContext = activity.getBaseContext();
        mAuthority = getActivity().getString(R.string.org_dmfs_tasks_authority);
    }


    @Override
    public void onDetach()
    {
        if (mSaveOnDetach)
        {
            saveListState();
        }
        super.onDetach();
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long rowId)
    {
        VisibleListAdapter adapter = (VisibleListAdapter) adapterView.getAdapter();
        VisibleListAdapter.CheckableItem item = (VisibleListAdapter.CheckableItem) view.getTag();
        boolean checked = item.coloredCheckBox.isChecked();
        item.coloredCheckBox.setChecked(!checked);
        adapter.addToState(rowId, !checked);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1)
    {
        return new CursorLoader(mContext, TaskContract.TaskLists.getContentUri(mAuthority), new String[] { TaskContract.TaskLists._ID,
            TaskContract.TaskLists.LIST_NAME, TaskContract.TaskLists.LIST_COLOR, TaskContract.TaskLists.SYNC_ENABLED, TaskContract.TaskLists.VISIBLE,
            TaskContract.TaskLists.ACCOUNT_NAME, TaskContract.TaskLists.ACCOUNT_TYPE }, mListSelectionArguments, mListSelectionParam,
            TaskContract.TaskLists.ACCOUNT_NAME + " COLLATE NOCASE ASC");
    }


    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor)
    {
        mAdapter.swapCursor(cursor);
    }


    @Override
    public void onLoaderReset(Loader<Cursor> arg0)
    {
        mAdapter.changeCursor(null);

    }

    /**
     * This extends the {@link CursorAdapter}. The column index for the list name, list color and the current selection state is computed when the
     * {@link Cursor} is swapped. It also maintains the changes made to the current selection state through a {@link HashMap} of ids and selection state. If the
     * selection state is modified and then modified again then it is removed from the HashMap because it has reverted to the original state.
     * 
     * @author Arjun Naik<arjun@arjunnaik.in>
     * 
     */
    private class VisibleListAdapter extends CursorAdapter implements OnClickListener
    {
        LayoutInflater inflater;
        private int listNameColumn, listColorColumn, compareColumn, accountNameColumn, accountTypeColumn;
        private HashMap<Long, Boolean> savedPositions = new HashMap<Long, Boolean>();


        @Override
        public Cursor swapCursor(Cursor c)
        {
            if (c != null)
            {
                listNameColumn = c.getColumnIndex(TaskContract.TaskLists.LIST_NAME);
                listColorColumn = c.getColumnIndex(TaskContract.TaskLists.LIST_COLOR);
                compareColumn = c.getColumnIndex(mListCompareColumnName);
                accountNameColumn = c.getColumnIndex(TaskContract.TaskLists.ACCOUNT_NAME);
                accountTypeColumn = c.getColumnIndex(TaskContract.TaskLists.ACCOUNT_TYPE);
            }
            else
            {
                listNameColumn = -1;
                listColorColumn = -1;
                compareColumn = -1;
                accountNameColumn = -1;
                accountTypeColumn = -1;
            }
            return super.swapCursor(c);

        }


        public VisibleListAdapter(Context context, Cursor c, int flags)
        {
            super(context, c, flags);
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }


        @Override
        public void bindView(View v, Context c, final Cursor cur)
        {
            String listName = cur.getString(listNameColumn);
            CheckableItem item = (CheckableItem) v.getTag();
            item.text1.setText(listName);
            item.text2.setText(cur.getString(accountNameColumn));
            // store the cursor position inside the settings button.
            String accountType = cur.getString(accountTypeColumn);
            // show settings button only, when there is cached resolve result.
            if (cachedEditIntents.containsKey(accountType) && cachedEditIntents.get(accountType) != null)
            {
                item.btnSettings.setVisibility(View.VISIBLE);
                item.btnSettings.setTag(cur.getPosition());
                item.btnSettings.setOnClickListener(this);
            }
            else
            {
                item.btnSettings.setVisibility(View.GONE);
                item.btnSettings.setOnClickListener(null);
            }

            int listColor = cur.getInt(listColorColumn);
            item.coloredCheckBox.setColor(listColor);

            if (!cur.isNull(compareColumn))
            {
                long id = cur.getLong(0);
                boolean checkValue;
                if (savedPositions.containsKey(id))
                {
                    checkValue = savedPositions.get(id);
                }
                else
                {
                    checkValue = cur.getInt(compareColumn) == 1;
                }
                item.coloredCheckBox.setChecked(checkValue);

            }
        }


        @Override
        public View newView(Context c, Cursor cur, ViewGroup vg)
        {
            View newInflatedView = inflater.inflate(R.layout.visible_task_list_item, null);
            CheckableItem item = new CheckableItem();
            item.text1 = (TextView) newInflatedView.findViewById(android.R.id.text1);
            item.text2 = (TextView) newInflatedView.findViewById(android.R.id.text2);
            item.btnSettings = newInflatedView.findViewById(R.id.btn_settings);
            item.coloredCheckBox = (ColoredShapeCheckBox) newInflatedView.findViewById(R.id.visible_task_list_checked);
            newInflatedView.setTag(item);
            return newInflatedView;
        }

        public class CheckableItem
        {
            TextView text1;
            TextView text2;
            View btnSettings;
            ColoredShapeCheckBox coloredCheckBox;
        }


        private boolean addToState(long id, boolean val)
        {
            if (savedPositions.containsKey(Long.valueOf(id)))
            {
                savedPositions.remove(id);
                return false;
            }
            else
            {
                savedPositions.put(id, val);
                return true;
            }
        }


        public void clearHashMap()
        {
            savedPositions.clear();
        }


        public HashMap<Long, Boolean> getState()
        {
            return savedPositions;
        }


        @Override
        public void onClick(View v)
        {
            Cursor cursor = (Cursor) getItem((Integer) v.getTag());
            if (cursor != null)
            {
                onEditListClick(TaskContract.TaskLists.getContentUri(mAuthority).buildUpon().appendPath(String.valueOf(cursor.getLong(mRowIDColumn))).build(),
                    cursor.getString(accountTypeColumn));
            }
        }

    }


    /**
     * Is called, when the user click on the settings icon of a list item. This calls the assigned component to edit the list.
     * 
     * @param uri
     *            The uri of a certain task list. Ensure the database id of the task list is appended.
     * @param accountType
     *            The account type of the task list holder.
     */
    private void onEditListClick(Uri uri, String accountType)
    {
        if (uri == null || accountType == null)
        {
            return;
        }
        Intent intent = cachedEditIntents.get(accountType);
        if (intent == null)
        {
            return;
        }

        try
        {
            intent.setDataAndType(uri, ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + mAuthority + ".tasklists");
            startActivityForResult(intent, REQUEST_CODE_MANAGE_LIST);
        }
        catch (ActivityNotFoundException e)
        {
            Toast.makeText(getActivity(), "No activity found to edit the list", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * This function is called to save the any modifications made to the displayed list. It retrieves the {@link HashMap} from the adapter of the list and uses
     * it makes the changes persistent. For this it uses a batch operation provided by {@link ContentResolver}. The operations to be performed in the batch
     * operation are stored in an {@link ArrayList} of {@link ContentProviderOperation}.
     * 
     * @return <code>true</code> if the save operation was successful, <code>false</code> otherwise.
     */
    public boolean saveListState()
    {
        HashMap<Long, Boolean> savedPositions = ((VisibleListAdapter) getListAdapter()).getState();
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

        for (Long posInt : savedPositions.keySet())
        {
            boolean val = savedPositions.get(posInt);
            ContentProviderOperation op = ContentProviderOperation.newUpdate(TaskContract.TaskLists.getContentUri(mAuthority))
                .withSelection(TaskContract.TaskLists._ID + "=?", new String[] { posInt.toString() }).withValue(mListCompareColumnName, val ? "1" : "0")
                .build();
            ops.add(op);
        }

        try
        {
            mContext.getContentResolver().applyBatch(mAuthority, ops);
        }
        catch (RemoteException e)
        {
            e.printStackTrace();
            return false;
        }
        catch (OperationApplicationException e)
        {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    public void doneSaveListState()
    {
        ((VisibleListAdapter) getListAdapter()).clearHashMap();
    }

    /**
     * This class is used to display a list of accounts. The list can be modified by the {@link #addAccount(Account)} and
     * {@link #clear()} method. The dialog is supposed to display accounts, which support the insert intent to create new task list. The selection must be done
     * before. The adapter will show all accounts, which are added.
     * 
     * @author Tristan Heinig <tristan@dmfs.org>
     * 
     */
    private class AccountAdapter extends BaseAdapter
    {

        private ArrayList<Account> accountList = new ArrayList<Account>();


        public AccountAdapter(ArrayList<Account> accountList)
        {
            this.accountList = accountList;
        }


        public AccountAdapter()
        {
        }


        public void clear()
        {
            accountList.clear();
        }


        public void addAccount(Account account)
        {
            accountList.add(account);
        }


        @Override
        public int getCount()
        {
            return accountList.size();
        }


        @Override
        public Account getItem(int position)
        {
            return accountList.get(position);
        }


        @Override
        public long getItemId(int position)
        {
            return position;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            if (convertView == null)
            {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.account_list_item_dialog, parent, false);
            }
            Account account = getItem(position);
            ((TextView) convertView.findViewById(android.R.id.text1)).setText(account.name);
            ((TextView) convertView.findViewById(android.R.id.text2)).setText(account.type);
            return convertView;
        }

    }

}
