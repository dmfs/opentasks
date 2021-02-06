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
import android.content.ActivityNotFoundException;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.RemoteException;
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

import org.dmfs.android.bolts.color.colors.AttributeColor;
import org.dmfs.android.widgets.ColoredShapeCheckBox;
import org.dmfs.provider.tasks.AuthorityUtil;
import org.dmfs.tasks.contract.TaskContract;
import org.dmfs.tasks.model.Model;
import org.dmfs.tasks.model.Sources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.cursoradapter.widget.CursorAdapter;
import androidx.fragment.app.ListFragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;


/**
 * This fragment is used to display a list of task-providers. It show the task-providers which are visible in main {@link TaskListFragment} and also the
 * task-providers which are synced. The selection between the two lists is made by passing arguments to the fragment in a {@link Bundle} when it created in the
 * {@link SyncSettingsActivity}.
 * <p/>
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

    private Context mContext;
    private VisibleListAdapter mAdapter;

    private String mListSelectionArguments;
    private String[] mListSelectionParam;
    private String mListCompareColumnName;
    private boolean mSaveOnPause;
    private int mFragmentLayout;
    private String mAuthority;

    /**
     * A dialog, that shows a list of accounts, which support the insert intent.
     */
    private AlertDialog mChooseAccountToAddListDialog;
    /**
     * An adapter, that holds the accounts, which support the insert intent.
     */
    private AccountAdapter mAccountAdapter;

    private Sources mSources;


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
        mSaveOnPause = args.getBoolean(LIST_ONDETACH_SAVE);
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
        List<Account> accounts = mSources.getExistingAccounts();
        if (mContext.getResources().getBoolean(R.bool.opentasks_support_local_lists))
        {
            accounts.add(new Account(TaskContract.LOCAL_ACCOUNT_NAME, TaskContract.LOCAL_ACCOUNT_TYPE));
        }
        mAccountAdapter = new AccountAdapter(accounts);
        setListAdapter(mAdapter);
        getListView().setOnItemClickListener(this);
    }


    @Override
    public void onResume()
    {
        super.onResume();
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

        Model model = mSources.getModel(selectedAccount.type);
        if (model.hasInsertActivity())
        {
            try
            {
                model.startInsertIntent(getActivity(), selectedAccount);
            }
            catch (ActivityNotFoundException e)
            {
                Toast.makeText(getActivity(), "No activity found to edit list", Toast.LENGTH_SHORT).show();
            }
        }
    }


    /*
     * Adds an action to the ActionBar to create local lists.
     */
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.list_settings_menu, menu);
        // for now we tint all icons manually
        for (int i = 0; i < menu.size(); ++i)
        {
            MenuItem item = menu.getItem(0);
            Drawable drawable = DrawableCompat.wrap(item.getIcon());
            drawable.setTint(0x80000000);
            item.setIcon(drawable);
        }
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
            if (mAccountAdapter.getCount() == 1)
            {
                Account account = mAccountAdapter.getItem(0);
                Model model = mSources.getModel(account.type);
                model.startInsertIntent(getActivity(), account);
            }
            else
            {
                mChooseAccountToAddListDialog.show();
            }
        }
        catch (ActivityNotFoundException e)
        {
            Toast.makeText(getActivity(), "No activity found to edit list", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        mSources = Sources.getInstance(activity);
        mContext = activity.getBaseContext();
        mAuthority = AuthorityUtil.taskAuthority(activity);
    }


    @Override
    public void onPause()
    {
        super.onPause();
        if (mSaveOnPause)
        {
            saveListState();
            doneSaveListState();
        }
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
        return new CursorLoader(mContext, TaskContract.TaskLists.getContentUri(mAuthority), new String[] {
                TaskContract.TaskLists._ID,
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
            String accountName = cur.getString(accountNameColumn);
            String accountType = cur.getString(accountTypeColumn);
            Model model = mSources.getModel(accountType);
            if (model.hasEditActivity())
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

            item.text1.setText(listName);
            item.text2.setText(accountName);

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
            View newInflatedView = inflater.inflate(R.layout.visible_task_list_item, vg, false);
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
            Integer position = (Integer) v.getTag();
            Cursor cursor = (Cursor) getItem(position);
            if (cursor != null)
            {
                onEditListClick(new Account(cursor.getString(accountNameColumn), cursor.getString(accountTypeColumn)), getItemId(position),
                        cursor.getString(listNameColumn), cursor.getInt(listColorColumn));
            }
        }

    }


    /**
     * Is called, when the user click on the settings icon of a list item. This calls the assigned component to edit the list.
     *
     * @param account
     *         The account of the list.
     * @param listId
     *         The id of the list.
     * @param name
     *         The name of the list.
     * @param color
     *         The color of the list.
     */
    private void onEditListClick(Account account, long listId, String name, Integer color)
    {
        Model model = mSources.getModel(account.type);

        if (!model.hasEditActivity())
        {
            return;
        }

        try
        {
            model.startEditIntent(getActivity(), account, listId, name, color);
        }
        catch (ActivityNotFoundException e)
        {
            Toast.makeText(getActivity(), "No activity found to edit the list" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
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
     * This class is used to display a list of accounts. The dialog is
     * supposed to display accounts, which support the insert intent to create new task list. The selection must be done before. The adapter will show all
     * accounts, which are added.
     *
     * @author Tristan Heinig <tristan@dmfs.org>
     */
    private class AccountAdapter extends BaseAdapter
    {

        private List<Account> mAccountList;


        public AccountAdapter(List<Account> accountList)
        {
            mAccountList = accountList;
            Iterator<Account> accountIterator = accountList.iterator();
            while (accountIterator.hasNext())
            {
                Account account = accountIterator.next();
                if (!mSources.getModel(account.type).hasInsertActivity())
                {
                    accountIterator.remove();
                }
            }
        }


        @Override
        public int getCount()
        {
            return mAccountList.size();
        }


        @Override
        public Account getItem(int position)
        {
            return mAccountList.get(position);
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
            Model model = mSources.getModel(account.type);
            ((TextView) convertView.findViewById(android.R.id.text1)).setText(account.name);
            ((TextView) convertView.findViewById(android.R.id.text2)).setText(model.getAccountLabel());
            return convertView;
        }

    }

}
