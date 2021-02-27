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

package org.dmfs.provider.tasks;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.OnAccountsUpdateListener;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Log;

import org.dmfs.iterables.EmptyIterable;
import org.dmfs.provider.tasks.TaskDatabaseHelper.OnDatabaseOperationListener;
import org.dmfs.provider.tasks.TaskDatabaseHelper.Tables;
import org.dmfs.provider.tasks.handler.PropertyHandler;
import org.dmfs.provider.tasks.handler.PropertyHandlerFactory;
import org.dmfs.provider.tasks.model.ContentValuesListAdapter;
import org.dmfs.provider.tasks.model.ContentValuesTaskAdapter;
import org.dmfs.provider.tasks.model.CursorContentValuesInstanceAdapter;
import org.dmfs.provider.tasks.model.CursorContentValuesListAdapter;
import org.dmfs.provider.tasks.model.CursorContentValuesTaskAdapter;
import org.dmfs.provider.tasks.model.InstanceAdapter;
import org.dmfs.provider.tasks.model.ListAdapter;
import org.dmfs.provider.tasks.model.TaskAdapter;
import org.dmfs.provider.tasks.processors.EntityProcessor;
import org.dmfs.provider.tasks.processors.instances.Detaching;
import org.dmfs.provider.tasks.processors.instances.TaskValueDelegate;
import org.dmfs.provider.tasks.processors.lists.ListCommitProcessor;
import org.dmfs.provider.tasks.processors.tasks.AutoCompleting;
import org.dmfs.provider.tasks.processors.tasks.Instantiating;
import org.dmfs.provider.tasks.processors.tasks.Moving;
import org.dmfs.provider.tasks.processors.tasks.Originating;
import org.dmfs.provider.tasks.processors.tasks.Relating;
import org.dmfs.provider.tasks.processors.tasks.Reparenting;
import org.dmfs.provider.tasks.processors.tasks.Searchable;
import org.dmfs.provider.tasks.processors.tasks.TaskCommitProcessor;
import org.dmfs.provider.tasks.processors.tasks.Validating;
import org.dmfs.tasks.contract.TaskContract;
import org.dmfs.tasks.contract.TaskContract.Alarms;
import org.dmfs.tasks.contract.TaskContract.Categories;
import org.dmfs.tasks.contract.TaskContract.CategoriesColumns;
import org.dmfs.tasks.contract.TaskContract.Instances;
import org.dmfs.tasks.contract.TaskContract.Properties;
import org.dmfs.tasks.contract.TaskContract.PropertyColumns;
import org.dmfs.tasks.contract.TaskContract.SyncState;
import org.dmfs.tasks.contract.TaskContract.TaskColumns;
import org.dmfs.tasks.contract.TaskContract.TaskListColumns;
import org.dmfs.tasks.contract.TaskContract.TaskListSyncColumns;
import org.dmfs.tasks.contract.TaskContract.TaskLists;
import org.dmfs.tasks.contract.TaskContract.Tasks;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;


/**
 * The provider for tasks.
 * <p>
 * TODO: add support for recurring tasks
 * <p>
 * TODO: add support for reminders
 * <p>
 * TODO: add support for attendees
 * <p>
 * TODO: refactor the selection stuff
 *
 * @author Marten Gajda <marten@dmfs.org>
 * @author Tobias Reinsch <tobias@dmfs.org>
 */
public final class TaskProvider extends SQLiteContentProvider implements OnAccountsUpdateListener, OnDatabaseOperationListener
{

    private static final int LISTS = 1;
    private static final int LIST_ID = 2;
    private static final int TASKS = 101;
    private static final int TASK_ID = 102;
    private static final int INSTANCES = 103;
    private static final int INSTANCE_ID = 104;
    private static final int CATEGORIES = 1001;
    private static final int CATEGORY_ID = 1002;
    private static final int PROPERTIES = 1003;
    private static final int PROPERTY_ID = 1004;
    private static final int ALARMS = 1005;
    private static final int ALARM_ID = 1006;
    private static final int SEARCH = 1007;
    private static final int SYNCSTATE = 1008;
    private static final int SYNCSTATE_ID = 1009;

    private static final int OPERATIONS = 100000;

    private final static Set<String> TASK_LIST_SYNC_COLUMNS = new HashSet<String>(Arrays.asList(TaskLists.SYNC_ADAPTER_COLUMNS));
    private static final String TAG = "TaskProvider";

    /**
     * A list of {@link EntityProcessor}s to execute when doing operations on the instances table.
     */
    private EntityProcessor<InstanceAdapter> mInstanceProcessorChain;

    /**
     * A list of {@link EntityProcessor}s to execute when doing operations on the tasks table.
     */
    private EntityProcessor<TaskAdapter> mTaskProcessorChain;

    /**
     * A list of {@link EntityProcessor}s to execute when doing operations on the task lists table.
     */
    private EntityProcessor<ListAdapter> mListProcessorChain;

    /**
     * Our authority.
     */
    String mAuthority;

    /**
     * The {@link UriMatcher} we use.
     */
    private UriMatcher mUriMatcher;

    /**
     * A handler to execute asynchronous jobs.
     */
    Handler mAsyncHandler;

    /**
     * Boolean to track if there are changes within a transaction.
     * <p>
     * This can be shared by multiple threads, hence the {@link AtomicBoolean}.
     */
    private AtomicBoolean mChanged = new AtomicBoolean(false);

    /**
     * This is a per transaction/thread flag which indicates whether new lists with an unknown account have been added.
     * If this holds true at the end of a transaction a window should be shown to ask the user for access to that account.
     */
    private ThreadLocal<Boolean> mStaleListCreated = new ThreadLocal<>();

    /**
     * The currently known accounts. This may be accessed from various threads, hence the AtomicReference.
     * By statring with an empty set, we can always guarantee a non-null reference.
     */
    private AtomicReference<Set<Account>> mAccountCache = new AtomicReference<>(Collections.emptySet());


    public TaskProvider()
    {
        // for now we don't have anything specific to execute before the transaction ends.
        super(EmptyIterable.instance());
    }


    @Override
    public boolean onCreate()
    {
        mAuthority = AuthorityUtil.taskAuthority(getContext());

        mTaskProcessorChain = new Validating(
                new AutoCompleting(new Relating(new Reparenting(new Instantiating(new Searchable(new Moving(new Originating(new TaskCommitProcessor()))))))));

        mListProcessorChain = new org.dmfs.provider.tasks.processors.lists.Validating(new ListCommitProcessor());

        mInstanceProcessorChain = new org.dmfs.provider.tasks.processors.instances.Validating(
                new Detaching(new TaskValueDelegate(mTaskProcessorChain), mTaskProcessorChain));

        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mUriMatcher.addURI(mAuthority, TaskContract.TaskLists.CONTENT_URI_PATH, LISTS);

        mUriMatcher.addURI(mAuthority, TaskContract.TaskLists.CONTENT_URI_PATH + "/#", LIST_ID);

        mUriMatcher.addURI(mAuthority, TaskContract.Tasks.CONTENT_URI_PATH, TASKS);
        mUriMatcher.addURI(mAuthority, TaskContract.Tasks.CONTENT_URI_PATH + "/#", TASK_ID);

        mUriMatcher.addURI(mAuthority, TaskContract.Instances.CONTENT_URI_PATH, INSTANCES);
        mUriMatcher.addURI(mAuthority, TaskContract.Instances.CONTENT_URI_PATH + "/#", INSTANCE_ID);

        mUriMatcher.addURI(mAuthority, TaskContract.Properties.CONTENT_URI_PATH, PROPERTIES);
        mUriMatcher.addURI(mAuthority, TaskContract.Properties.CONTENT_URI_PATH + "/#", PROPERTY_ID);

        mUriMatcher.addURI(mAuthority, TaskContract.Categories.CONTENT_URI_PATH, CATEGORIES);
        mUriMatcher.addURI(mAuthority, TaskContract.Categories.CONTENT_URI_PATH + "/#", CATEGORY_ID);

        mUriMatcher.addURI(mAuthority, TaskContract.Alarms.CONTENT_URI_PATH, ALARMS);
        mUriMatcher.addURI(mAuthority, TaskContract.Alarms.CONTENT_URI_PATH + "/#", ALARM_ID);

        mUriMatcher.addURI(mAuthority, TaskContract.Tasks.SEARCH_URI_PATH, SEARCH);

        mUriMatcher.addURI(mAuthority, TaskContract.SyncState.CONTENT_URI_PATH, SYNCSTATE);
        mUriMatcher.addURI(mAuthority, TaskContract.SyncState.CONTENT_URI_PATH + "/#", SYNCSTATE_ID);

        ContentOperation.register(mUriMatcher, mAuthority, OPERATIONS);

        boolean result = super.onCreate();

        // create a HandlerThread to perform async operations
        HandlerThread thread = new HandlerThread("backgroundHandler");
        thread.start();
        mAsyncHandler = new Handler(thread.getLooper());

        AccountManager accountManager = AccountManager.get(getContext());
        accountManager.addOnAccountsUpdatedListener(this, mAsyncHandler, true);

        updateNotifications();

        return result;
    }


    /**
     * Return true if the caller is a sync adapter (i.e. if the Uri contains the query parameter {@link TaskContract#CALLER_IS_SYNCADAPTER} and its value is
     * true).
     *
     * @param uri
     *         The {@link Uri} to check.
     *
     * @return <code>true</code> if the caller pretends to be a sync adapter, <code>false</code> otherwise.
     */
    @Override
    public boolean isCallerSyncAdapter(Uri uri)
    {
        String param = uri.getQueryParameter(TaskContract.CALLER_IS_SYNCADAPTER);
        return param != null && !"false".equals(param);
    }


    /**
     * Return true if the URI indicates to a load extended properties with {@link TaskContract#LOAD_PROPERTIES}.
     *
     * @param uri
     *         The {@link Uri} to check.
     *
     * @return <code>true</code> if the URI requests to load extended properties, <code>false</code> otherwise.
     */
    public boolean shouldLoadProperties(Uri uri)
    {
        String param = uri.getQueryParameter(TaskContract.LOAD_PROPERTIES);
        return param != null && !"false".equals(param);
    }


    /**
     * Get the account name from the given {@link Uri}.
     *
     * @param uri
     *         The Uri to check.
     *
     * @return The account name or null if no account name has been specified.
     */
    protected String getAccountName(Uri uri)
    {
        return uri.getQueryParameter(TaskContract.ACCOUNT_NAME);
    }


    /**
     * Get the account type from the given {@link Uri}.
     *
     * @param uri
     *         The Uri to check.
     *
     * @return The account type or null if no account type has been specified.
     */
    protected String getAccountType(Uri uri)
    {
        return uri.getQueryParameter(TaskContract.ACCOUNT_TYPE);
    }


    /**
     * Get any id from the given {@link Uri}.
     *
     * @param uri
     *         The Uri.
     *
     * @return The last path segment (which should contain the id).
     */
    private long getId(Uri uri)
    {
        return Long.parseLong(uri.getPathSegments().get(1));
    }


    /**
     * Build a selection string that selects the account specified in <code>uri</code>.
     *
     * @param uri
     *         A {@link Uri} that specifies an account.
     *
     * @return A {@link StringBuilder} with a selection string for the account.
     */
    protected StringBuilder selectAccount(Uri uri)
    {
        StringBuilder sb = new StringBuilder(256);
        return selectAccount(sb, uri);
    }


    /**
     * Append the selection of the account specified in <code>uri</code> to the {@link StringBuilder} <code>sb</code>.
     *
     * @param sb
     *         A {@link StringBuilder} that the selection is appended to.
     * @param uri
     *         A {@link Uri} that specifies an account.
     *
     * @return <code>sb</code>.
     */
    protected StringBuilder selectAccount(StringBuilder sb, Uri uri)
    {
        String accountName = getAccountName(uri);
        String accountType = getAccountType(uri);

        if (accountName != null || accountType != null)
        {

            if (accountName != null)
            {
                if (sb.length() > 0)
                {
                    sb.append(" AND ");
                }

                sb.append(TaskListSyncColumns.ACCOUNT_NAME);
                sb.append("=");
                DatabaseUtils.appendEscapedSQLString(sb, accountName);
            }
            if (accountType != null)
            {

                if (sb.length() > 0)
                {
                    sb.append(" AND ");
                }

                sb.append(TaskListSyncColumns.ACCOUNT_TYPE);
                sb.append("=");
                DatabaseUtils.appendEscapedSQLString(sb, accountType);
            }
        }
        return sb;
    }


    /**
     * Append the selection of the account specified in <code>uri</code> to the an {@link SQLiteQueryBuilder}.
     *
     * @param sqlBuilder
     *         A {@link SQLiteQueryBuilder} that the selection is appended to.
     * @param uri
     *         A {@link Uri} that specifies an account.
     */
    protected void selectAccount(SQLiteQueryBuilder sqlBuilder, Uri uri)
    {
        String accountName = getAccountName(uri);
        String accountType = getAccountType(uri);

        if (accountName != null)
        {
            sqlBuilder.appendWhere(" AND ");
            sqlBuilder.appendWhere(TaskListSyncColumns.ACCOUNT_NAME);
            sqlBuilder.appendWhere("=");
            sqlBuilder.appendWhereEscapeString(accountName);
        }
        if (accountType != null)
        {
            sqlBuilder.appendWhere(" AND ");
            sqlBuilder.appendWhere(TaskListSyncColumns.ACCOUNT_TYPE);
            sqlBuilder.appendWhere("=");
            sqlBuilder.appendWhereEscapeString(accountType);
        }
    }


    private StringBuilder _selectId(StringBuilder sb, long id, String key)
    {
        if (sb.length() > 0)
        {
            sb.append(" AND ");
        }
        sb.append(key);
        sb.append("=");
        sb.append(id);
        return sb;
    }


    protected StringBuilder selectId(Uri uri)
    {
        StringBuilder sb = new StringBuilder(128);
        return selectId(sb, uri);
    }


    protected StringBuilder selectId(StringBuilder sb, Uri uri)
    {
        return _selectId(sb, getId(uri), TaskListColumns._ID);
    }


    protected StringBuilder selectTaskId(Uri uri)
    {
        StringBuilder sb = new StringBuilder(128);
        return selectTaskId(sb, uri);
    }


    protected StringBuilder selectTaskId(long id)
    {
        StringBuilder sb = new StringBuilder(128);
        return selectTaskId(sb, id);
    }


    protected StringBuilder selectTaskId(StringBuilder sb, Uri uri)
    {
        return selectTaskId(sb, getId(uri));
    }


    protected StringBuilder selectTaskId(StringBuilder sb, long id)
    {
        return _selectId(sb, id, Instances.TASK_ID);

    }


    protected StringBuilder selectPropertyId(Uri uri)
    {
        StringBuilder sb = new StringBuilder(128);
        return selectPropertyId(sb, uri);
    }


    protected StringBuilder selectPropertyId(StringBuilder sb, Uri uri)
    {
        return selectPropertyId(sb, getId(uri));
    }


    protected StringBuilder selectPropertyId(long id)
    {
        StringBuilder sb = new StringBuilder(128);
        return selectPropertyId(sb, id);
    }


    protected StringBuilder selectPropertyId(StringBuilder sb, long id)
    {
        return _selectId(sb, id, PropertyColumns.PROPERTY_ID);
    }


    /**
     * Add a selection by ID to the given {@link SQLiteQueryBuilder}. The id is taken from the given Uri.
     *
     * @param sqlBuilder
     *         The {@link SQLiteQueryBuilder} to append the selection to.
     * @param idColumn
     *         The column that must match the id.
     * @param uri
     *         An {@link Uri} that contains the id.
     */
    protected void selectId(SQLiteQueryBuilder sqlBuilder, String idColumn, Uri uri)
    {
        sqlBuilder.appendWhere(" AND ");
        sqlBuilder.appendWhere(idColumn);
        sqlBuilder.appendWhere("=");
        sqlBuilder.appendWhere(String.valueOf(getId(uri)));
    }


    /**
     * Append any arbitrary selection string to the selection in <code>sb</code>
     *
     * @param sb
     *         A {@link StringBuilder} that already contains a selection string.
     * @param selection
     *         A valid SQL selection string.
     *
     * @return A string with the final selection.
     */
    protected String updateSelection(StringBuilder sb, String selection)
    {
        if (selection != null)
        {
            if (sb.length() > 0)
            {
                sb.append(" AND ( ").append(selection).append(" ) ");
            }
            else
            {
                sb.append(" ( ").append(selection).append(" ) ");
            }
        }
        return sb.toString();
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {
        final SQLiteDatabase db = getDatabaseHelper().getWritableDatabase();
        SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
        // initialize appendWhere, this allows us to append all other selections with a preceding "AND"
        sqlBuilder.appendWhere(" 1=1 ");
        boolean isSyncAdapter = isCallerSyncAdapter(uri);

        switch (mUriMatcher.match(uri))
        {
            case SYNCSTATE_ID:
                // the id is ignored, we only match by account type and name given in the Uri
            case SYNCSTATE:
            {
                if (TextUtils.isEmpty(getAccountName(uri)) || TextUtils.isEmpty(getAccountType(uri)))
                {
                    throw new IllegalArgumentException("uri must contain an account when accessing syncstate");
                }
                selectAccount(sqlBuilder, uri);
                sqlBuilder.setTables(Tables.SYNCSTATE);
                break;
            }
            case LISTS:
                // add account to selection if any
                selectAccount(sqlBuilder, uri);
                sqlBuilder.setTables(Tables.LISTS);
                if (sortOrder == null || sortOrder.length() == 0)
                {
                    sortOrder = TaskContract.TaskLists.DEFAULT_SORT_ORDER;
                }
                break;

            case LIST_ID:
                // add account to selection if any
                selectAccount(sqlBuilder, uri);
                sqlBuilder.setTables(Tables.LISTS);
                selectId(sqlBuilder, TaskListColumns._ID, uri);
                if (sortOrder == null || sortOrder.length() == 0)
                {
                    sortOrder = TaskContract.TaskLists.DEFAULT_SORT_ORDER;
                }
                break;

            case TASKS:
                if (shouldLoadProperties(uri))
                {
                    // extended properties were requested, therefore change to task view that includes these properties
                    sqlBuilder.setTables(Tables.TASKS_PROPERTY_VIEW);
                }
                else
                {
                    sqlBuilder.setTables(Tables.TASKS_VIEW);
                }
                if (!isSyncAdapter)
                {
                    // do not return deleted rows if caller is not a sync adapter
                    sqlBuilder.appendWhere(" AND ");
                    sqlBuilder.appendWhere(Tasks._DELETED);
                    sqlBuilder.appendWhere("=0");
                }
                if (sortOrder == null || sortOrder.length() == 0)
                {
                    sortOrder = TaskContract.Tasks.DEFAULT_SORT_ORDER;
                }
                break;

            case TASK_ID:
                if (shouldLoadProperties(uri))
                {
                    // extended properties were requested, therefore change to task view that includes these properties
                    sqlBuilder.setTables(Tables.TASKS_PROPERTY_VIEW);
                }
                else
                {
                    sqlBuilder.setTables(Tables.TASKS_VIEW);
                }
                selectId(sqlBuilder, TaskColumns._ID, uri);
                if (!isSyncAdapter)
                {
                    // do not return deleted rows if caller is not a sync adapter
                    sqlBuilder.appendWhere(" AND ");
                    sqlBuilder.appendWhere(Tasks._DELETED);
                    sqlBuilder.appendWhere("=0");
                }
                if (sortOrder == null || sortOrder.length() == 0)
                {
                    sortOrder = TaskContract.Tasks.DEFAULT_SORT_ORDER;
                }
                break;

            case INSTANCES:
                if (shouldLoadProperties(uri))
                {
                    // extended properties were requested, therefore change to instance view that includes these properties
                    sqlBuilder.setTables(Tables.INSTANCE_PROPERTY_VIEW);
                }
                else
                {
                    sqlBuilder.setTables(Tables.INSTANCE_CLIENT_VIEW);
                }
                if (!isSyncAdapter)
                {
                    // do not return deleted rows if caller is not a sync adapter
                    sqlBuilder.appendWhere(" AND ");
                    sqlBuilder.appendWhere(Tasks._DELETED);
                    sqlBuilder.appendWhere("=0");
                }
                if (sortOrder == null || sortOrder.length() == 0)
                {
                    sortOrder = TaskContract.Instances.DEFAULT_SORT_ORDER;
                }
                break;

            case INSTANCE_ID:
                if (shouldLoadProperties(uri))
                {
                    // extended properties were requested, therefore change to instance view that includes these properties
                    sqlBuilder.setTables(Tables.INSTANCE_PROPERTY_VIEW);
                }
                else
                {
                    sqlBuilder.setTables(Tables.INSTANCE_CLIENT_VIEW);
                }
                selectId(sqlBuilder, Instances._ID, uri);
                if (!isSyncAdapter)
                {
                    // do not return deleted rows if caller is not a sync adapter
                    sqlBuilder.appendWhere(" AND ");
                    sqlBuilder.appendWhere(Tasks._DELETED);
                    sqlBuilder.appendWhere("=0");
                }
                if (sortOrder == null || sortOrder.length() == 0)
                {
                    sortOrder = TaskContract.Instances.DEFAULT_SORT_ORDER;
                }
                break;

            case CATEGORIES:
                selectAccount(sqlBuilder, uri);
                sqlBuilder.setTables(Tables.CATEGORIES);
                if (sortOrder == null || sortOrder.length() == 0)
                {
                    sortOrder = TaskContract.Categories.DEFAULT_SORT_ORDER;
                }
                break;

            case CATEGORY_ID:
                selectAccount(sqlBuilder, uri);
                sqlBuilder.setTables(Tables.CATEGORIES);
                selectId(sqlBuilder, CategoriesColumns._ID, uri);
                if (sortOrder == null || sortOrder.length() == 0)
                {
                    sortOrder = TaskContract.Categories.DEFAULT_SORT_ORDER;
                }
                break;

            case PROPERTIES:
                sqlBuilder.setTables(Tables.PROPERTIES);
                break;

            case PROPERTY_ID:
                sqlBuilder.setTables(Tables.PROPERTIES);
                selectId(sqlBuilder, PropertyColumns.PROPERTY_ID, uri);
                break;

            case SEARCH:
                String searchString = uri.getQueryParameter(Tasks.SEARCH_QUERY_PARAMETER);
                searchString = Uri.decode(searchString);
                Cursor searchCursor = FTSDatabaseHelper.getTaskSearchCursor(db, searchString, projection, selection, selectionArgs, sortOrder);
                if (searchCursor != null)
                {
                    // attach tasks uri for notifications, that way the search results are updated when a task changes
                    searchCursor.setNotificationUri(getContext().getContentResolver(), Tasks.getContentUri(mAuthority));
                }
                return searchCursor;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        Cursor c = sqlBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);

        if (c != null)
        {
            c.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return c;
    }


    @Override
    public int deleteInTransaction(final SQLiteDatabase db, Uri uri, String selection, String[] selectionArgs, final boolean isSyncAdapter)
    {
        int count = 0;
        String accountName = getAccountName(uri);
        String accountType = getAccountType(uri);

        switch (mUriMatcher.match(uri))
        {
            case SYNCSTATE_ID:
                // the id is ignored, we only match by account type and name given in the Uri
            case SYNCSTATE:
            {
                if (!isSyncAdapter)
                {
                    throw new IllegalAccessError("only sync adapters may access syncstate");
                }
                if (TextUtils.isEmpty(getAccountName(uri)) || TextUtils.isEmpty(getAccountType(uri)))
                {
                    throw new IllegalArgumentException("uri must contain an account when accessing syncstate");
                }
                selection = updateSelection(selectAccount(uri), selection);
                count = db.delete(Tables.SYNCSTATE, selection, selectionArgs);
                break;
            }
            /*
             * Deleting task lists is only allowed to sync adapters. They must provide ACCOUNT_NAME and ACCOUNT_TYPE.
             */
            case LIST_ID:
                // add _id to selection and fall through
                selection = updateSelection(selectId(uri), selection);
            case LISTS:
            {
                if (isSyncAdapter)
                {
                    if (TextUtils.isEmpty(accountType) || TextUtils.isEmpty(accountName))
                    {
                        throw new IllegalArgumentException("Sync adapters must specify an account and account type: " + uri);
                    }
                }

                // iterate over all lists that match the selection
                final Cursor cursor = db.query(Tables.LISTS, null, selection, selectionArgs, null, null, null, null);

                try
                {
                    while (cursor.moveToNext())
                    {
                        final ListAdapter list = new CursorContentValuesListAdapter(ListAdapter._ID.getFrom(cursor), cursor, new ContentValues());

                        mListProcessorChain.delete(db, list, isSyncAdapter);
                        mChanged.set(true);
                        count++;
                    }
                }
                finally
                {
                    cursor.close();
                }

                break;

            }
            /*
             * Task won't be removed, just marked as deleted if the caller isn't a sync adapter. Sync adapters can remove tasks immediately.
             */
            case TASK_ID:
                // add id to selection and fall through
                selection = updateSelection(selectId(uri), selection);

            case TASKS:
            {
                // TODO: filter by account name and type if present in uri.

                if (isSyncAdapter)
                {
                    if (TextUtils.isEmpty(accountType) || TextUtils.isEmpty(accountName))
                    {
                        throw new IllegalArgumentException("Sync adapters must specify an account and account type: " + uri);
                    }
                }

                // iterate over all tasks that match the selection
                final Cursor cursor = db.query(Tables.TASKS_VIEW, null, selection, selectionArgs, null, null, null, null);

                try
                {
                    while (cursor.moveToNext())
                    {
                        final TaskAdapter task = new CursorContentValuesTaskAdapter(cursor, new ContentValues());

                        mTaskProcessorChain.delete(db, task, isSyncAdapter);

                        mChanged.set(true);
                        count++;
                    }
                }
                finally
                {
                    cursor.close();
                }

                break;
            }

            case INSTANCE_ID:
                // add id to selection and fall through
                selection = updateSelection(selectId(uri), selection);

            case INSTANCES:
            {
                // iterate over all instances that match the selection
                try (Cursor cursor = db.query(Tables.INSTANCE_VIEW, null, selection, selectionArgs, null, null, null, null))
                {
                    while (cursor.moveToNext())
                    {
                        mInstanceProcessorChain.delete(db, new CursorContentValuesInstanceAdapter(cursor, new ContentValues()), isSyncAdapter);
                        mChanged.set(true);
                        count++;
                    }
                }

                break;
            }

            case ALARM_ID:
                // add id to selection and fall through
                selection = updateSelection(selectId(uri), selection);

            case ALARMS:

                count = db.delete(Tables.ALARMS, selection, selectionArgs);
                break;

            case PROPERTY_ID:
                selection = updateSelection(selectPropertyId(uri), selection);

            case PROPERTIES:
                // fetch all properties that match the selection
                Cursor cursor = db.query(Tables.PROPERTIES, null, selection, selectionArgs, null, null, null);

                try
                {
                    int propIdCol = cursor.getColumnIndex(Properties.PROPERTY_ID);
                    int taskIdCol = cursor.getColumnIndex(Properties.TASK_ID);
                    int mimeTypeCol = cursor.getColumnIndex(Properties.MIMETYPE);
                    while (cursor.moveToNext())
                    {
                        long propertyId = cursor.getLong(propIdCol);
                        long taskId = cursor.getLong(taskIdCol);
                        String mimeType = cursor.getString(mimeTypeCol);
                        if (mimeType != null)
                        {
                            PropertyHandler handler = PropertyHandlerFactory.get(mimeType);
                            count += handler.delete(db, taskId, propertyId, cursor, isSyncAdapter);
                        }
                    }
                }
                finally
                {
                    cursor.close();
                }
                postNotifyUri(Properties.getContentUri(mAuthority));
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        if (count > 0)
        {
            postNotifyUri(uri);
            postNotifyUri(Instances.getContentUri(mAuthority));
            postNotifyUri(Tasks.getContentUri(mAuthority));
        }
        return count;
    }


    @Override
    public Uri insertInTransaction(final SQLiteDatabase db, Uri uri, final ContentValues values, final boolean isSyncAdapter)
    {
        long rowId;
        Uri result_uri;

        String accountName = getAccountName(uri);
        String accountType = getAccountType(uri);

        switch (mUriMatcher.match(uri))
        {
            case SYNCSTATE:
            {
                if (!isSyncAdapter)
                {
                    throw new IllegalAccessError("only sync adapters may access syncstate");
                }
                if (TextUtils.isEmpty(accountName) || TextUtils.isEmpty(accountType))
                {
                    throw new IllegalArgumentException("uri must contain an account when accessing syncstate");
                }
                values.put(SyncState.ACCOUNT_NAME, accountName);
                values.put(SyncState.ACCOUNT_TYPE, accountType);
                rowId = db.replace(Tables.SYNCSTATE, null, values);
                result_uri = TaskContract.SyncState.getContentUri(mAuthority);
                break;
            }
            case LISTS:
            {
                final ListAdapter list = new ContentValuesListAdapter(values);
                list.set(ListAdapter.ACCOUNT_NAME, accountName);
                list.set(ListAdapter.ACCOUNT_TYPE, accountType);

                mListProcessorChain.insert(db, list, isSyncAdapter);
                mChanged.set(true);

                rowId = list.id();
                result_uri = TaskContract.TaskLists.getContentUri(mAuthority);
                // if the account is unknown we need to ask the user
                if (Build.VERSION.SDK_INT >= 26 &&
                        !TaskContract.LOCAL_ACCOUNT_TYPE.equals(accountType) &&
                        !mAccountCache.get().contains(new Account(accountName, accountType)))
                {
                    // store the fact that we have an unknown account in this transaction
                    mStaleListCreated.set(true);
                    Log.d(TAG, String.format("List with unknown account %s inserted.", new Account(accountName, accountType)));
                }
                break;
            }
            case TASKS:
                final TaskAdapter task = new ContentValuesTaskAdapter(values);

                mTaskProcessorChain.insert(db, task, isSyncAdapter);

                mChanged.set(true);

                rowId = task.id();
                result_uri = TaskContract.Tasks.getContentUri(mAuthority);

                postNotifyUri(Instances.getContentUri(mAuthority));
                postNotifyUri(Tasks.getContentUri(mAuthority));

                break;

            // inserting instances is currently disabled because we only expand one instance,
            // so even though a new task (exception) would be created, no instance might show up
            // we need to resolve this discrepancy. Until then this feature remains disabled.
//            case INSTANCES:
//            {
//                InstanceAdapter instance = mInstanceProcessorChain.insert(db, new ContentValuesInstanceAdapter(values), isSyncAdapter);
//                rowId = instance.id();
//                result_uri = TaskContract.Instances.getContentUri(mAuthority);
//
//                postNotifyUri(Instances.getContentUri(mAuthority));
//                postNotifyUri(Tasks.getContentUri(mAuthority));
//
//                break;
//            }
            case PROPERTIES:
                String mimetype = values.getAsString(Properties.MIMETYPE);

                if (mimetype == null)
                {
                    throw new IllegalArgumentException("missing mimetype in property values");
                }

                Long taskId = values.getAsLong(Properties.TASK_ID);
                if (taskId == null)
                {
                    throw new IllegalArgumentException("missing task id in property values");
                }

                if (values.containsKey(Properties.PROPERTY_ID))
                {
                    throw new IllegalArgumentException("property id can not be written");
                }

                PropertyHandler handler = PropertyHandlerFactory.get(mimetype);
                rowId = handler.insert(db, taskId, values, isSyncAdapter);
                result_uri = TaskContract.Properties.getContentUri(mAuthority);
                if (rowId >= 0)
                {
                    postNotifyUri(Tasks.getContentUri(mAuthority));
                    postNotifyUri(Instances.getContentUri(mAuthority));
                }
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        if (rowId > 0 && result_uri != null)
        {
            result_uri = ContentUris.withAppendedId(result_uri, rowId);
            postNotifyUri(result_uri);
            postNotifyUri(uri);
            return result_uri;
        }
        throw new SQLException("Failed to insert row into " + uri);
    }


    @Override
    public int updateInTransaction(final SQLiteDatabase db, Uri uri, final ContentValues values, String selection, String[] selectionArgs,
                                   final boolean isSyncAdapter)
    {
        int count = 0;
        boolean dataChanged = false;
        switch (mUriMatcher.match(uri))
        {
            case SYNCSTATE_ID:
                // the id is ignored, we only match by account type and name given in the Uri
            case SYNCSTATE:
            {
                if (!isSyncAdapter)
                {
                    throw new IllegalAccessError("only sync adapters may access syncstate");
                }

                String accountName = getAccountName(uri);
                String accountType = getAccountType(uri);
                if (TextUtils.isEmpty(accountName) || TextUtils.isEmpty(accountType))
                {
                    throw new IllegalArgumentException("uri must contain an account when accessing syncstate");
                }

                if (values.size() == 0)
                {
                    // we're done
                    break;
                }

                values.put(SyncState.ACCOUNT_NAME, accountName);
                values.put(SyncState.ACCOUNT_TYPE, accountType);

                long id = db.replace(Tables.SYNCSTATE, null, values);
                if (id >= 0)
                {
                    count = 1;
                }
                break;
            }
            case LIST_ID:
                // update selection and fall through
                selection = updateSelection(selectId(uri), selection);

            case LISTS:
            {
                // iterate over all task lists that match the selection
                final Cursor cursor = db.query(Tables.LISTS, null, selection, selectionArgs, null, null, null, null);

                int idCol = cursor.getColumnIndex(TaskContract.TaskLists._ID);

                try
                {
                    while (cursor.moveToNext())
                    {
                        final long listId = cursor.getLong(idCol);

                        // clone list values if we have more than one list to update
                        // we need this, because the processors may change the values
                        final ListAdapter list = new CursorContentValuesListAdapter(listId, cursor, cursor.getCount() > 1 ? new ContentValues(values) : values);

                        if (list.hasUpdates())
                        {
                            mListProcessorChain.update(db, list, isSyncAdapter);
                            dataChanged |= !TASK_LIST_SYNC_COLUMNS.containsAll(values.keySet());
                        }
                        // note we still count the row even if no update was necessary
                        count++;
                    }
                }
                finally
                {
                    cursor.close();
                }
                break;
            }
            case TASK_ID:
                // update selection and fall through
                selection = updateSelection(selectId(uri), selection);

            case TASKS:
            {
                // iterate over all tasks that match the selection
                final Cursor cursor = db.query(Tables.TASKS_VIEW, null, selection, selectionArgs, null, null, null, null);

                try
                {
                    while (cursor.moveToNext())
                    {
                        // clone task values if we have more than one task to update
                        // we need this, because the processors may change the values
                        final TaskAdapter task = new CursorContentValuesTaskAdapter(cursor, cursor.getCount() > 1 ? new ContentValues(values) : values);

                        if (task.hasUpdates())
                        {
                            mTaskProcessorChain.update(db, task, isSyncAdapter);
                            dataChanged |= !TASK_LIST_SYNC_COLUMNS.containsAll(values.keySet());
                        }
                        // note we still count the row even if no update was necessary
                        count++;
                    }
                }
                finally
                {
                    cursor.close();
                }

                if (dataChanged)
                {
                    postNotifyUri(Instances.getContentUri(mAuthority));
                    postNotifyUri(Tasks.getContentUri(mAuthority));
                }
                break;
            }

            case INSTANCE_ID:
                // update selection and fall through
                selection = updateSelection(selectId(uri), selection);

            case INSTANCES:
            {
                // iterate over all instances that match the selection

                try (Cursor cursor = db.query(Tables.INSTANCE_VIEW, null, selection, selectionArgs, null, null, null, null))
                {
                    while (cursor.moveToNext())
                    {
                        // clone task values if we have more than one task to update
                        // we need this, because the processors may change the values
                        final InstanceAdapter instance = new CursorContentValuesInstanceAdapter(cursor,
                                cursor.getCount() > 1 ? new ContentValues(values) : values);

                        if (instance.hasUpdates())
                        {
                            mInstanceProcessorChain.update(db, instance, isSyncAdapter);
                            dataChanged = true;
                        }
                        // note we still count the row even if no update was necessary
                        count++;
                    }
                }

                if (dataChanged)
                {
                    postNotifyUri(Instances.getContentUri(mAuthority));
                    postNotifyUri(Tasks.getContentUri(mAuthority));
                }
                break;
            }
            case PROPERTY_ID:
                selection = updateSelection(selectPropertyId(uri), selection);

            case PROPERTIES:
                if (values.containsKey(Properties.MIMETYPE))
                {
                    throw new IllegalArgumentException("property mimetypes can not be modified");
                }

                if (values.containsKey(Properties.TASK_ID))
                {
                    throw new IllegalArgumentException("task id can not be changed");
                }

                if (values.containsKey(Properties.PROPERTY_ID))
                {
                    throw new IllegalArgumentException("property id can not be changed");
                }

                // fetch all properties that match the selection
                Cursor cursor = db.query(Tables.PROPERTIES, null, selection, selectionArgs, null, null, null);

                try
                {
                    int propIdCol = cursor.getColumnIndex(Properties.PROPERTY_ID);
                    int taskIdCol = cursor.getColumnIndex(Properties.TASK_ID);
                    int mimeTypeCol = cursor.getColumnIndex(Properties.MIMETYPE);
                    while (cursor.moveToNext())
                    {
                        long propertyId = cursor.getLong(propIdCol);
                        long taskId = cursor.getLong(taskIdCol);
                        String mimeType = cursor.getString(mimeTypeCol);
                        if (mimeType != null)
                        {
                            PropertyHandler handler = PropertyHandlerFactory.get(mimeType);
                            count += handler.update(db, taskId, propertyId, values, cursor, isSyncAdapter);
                        }
                    }
                }
                finally
                {
                    cursor.close();
                }
                postNotifyUri(Properties.getContentUri(mAuthority));
                break;

            case CATEGORY_ID:
                String newCategorySelection = updateSelection(selectId(uri), selection);
                validateCategoryValues(values, false, isSyncAdapter);
                count = db.update(Tables.CATEGORIES, values, newCategorySelection, selectionArgs);
                break;
            case ALARM_ID:
                String newAlarmSelection = updateSelection(selectId(uri), selection);
                validateAlarmValues(values, false, isSyncAdapter);
                count = db.update(Tables.ALARMS, values, newAlarmSelection, selectionArgs);
                break;
            default:
                ContentOperation operation = ContentOperation.get(mUriMatcher.match(uri), OPERATIONS);

                if (operation == null)
                {
                    throw new IllegalArgumentException("Unknown URI " + uri);
                }

                operation.run(getContext(), mAsyncHandler, uri, db, values);
        }

        if (dataChanged)
        {
            // send notifications, because non-sync columns have been updated
            postNotifyUri(uri);
            mChanged.set(true);
        }

        return count;
    }


    /**
     * Update task due and task start notifications.
     */
    private void updateNotifications()
    {
        mAsyncHandler.post(new Runnable()
        {

            @Override
            public void run()
            {
                ContentOperation.UPDATE_NOTIFICATION_ALARM.fire(getContext(), null);
            }
        });
    }


    /**
     * Validate the given category values.
     *
     * @param values
     *         The category properties to validate.
     *
     * @throws IllegalArgumentException
     *         if any of the values is invalid.
     */
    private void validateCategoryValues(ContentValues values, boolean isNew, boolean isSyncAdapter)
    {
        // row id can not be changed or set manually
        if (values.containsKey(Categories._ID))
        {
            throw new IllegalArgumentException("_ID can not be set manually");
        }

        if (isNew != values.containsKey(Categories.ACCOUNT_NAME) && (!isNew || values.get(Categories.ACCOUNT_NAME) != null))
        {
            throw new IllegalArgumentException("ACCOUNT_NAME is write-once and required on INSERT");
        }

        if (isNew != values.containsKey(Categories.ACCOUNT_TYPE) && (!isNew || values.get(Categories.ACCOUNT_TYPE) != null))
        {
            throw new IllegalArgumentException("ACCOUNT_TYPE is write-once and required on INSERT");
        }
    }


    /**
     * Validate the given alarm values.
     *
     * @param values
     *         The alarm values to validate
     *
     * @throws IllegalArgumentException
     *         if any of the values is invalid.
     */
    private void validateAlarmValues(ContentValues values, boolean isNew, boolean isSyncAdapter)
    {
        if (values.containsKey(Alarms.ALARM_ID))
        {
            throw new IllegalArgumentException("ALARM_ID can not be set manually");
        }
    }


    @Override
    public String getType(Uri uri)
    {
        switch (mUriMatcher.match(uri))
        {
            case LISTS:
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/org.dmfs.tasks." + TaskLists.CONTENT_URI_PATH;
            case LIST_ID:
                return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/org.dmfs.tasks." + TaskLists.CONTENT_URI_PATH;
            case TASKS:
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/org.dmfs.tasks." + Tasks.CONTENT_URI_PATH;
            case TASK_ID:
                return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/org.dmfs.tasks." + Tasks.CONTENT_URI_PATH;
            case INSTANCES:
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/org.dmfs.tasks." + Instances.CONTENT_URI_PATH;
            case INSTANCE_ID:
                return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/org.dmfs.tasks." + Instances.CONTENT_URI_PATH;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }


    @Override
    protected void onEndTransaction(boolean callerIsSyncAdapter)
    {
        super.onEndTransaction(callerIsSyncAdapter);
        if (mChanged.compareAndSet(true, false))
        {
            updateNotifications();
            Utils.sendActionProviderChangedBroadCast(getContext(), mAuthority);
        }

        if (Boolean.TRUE.equals(mStaleListCreated.get()))
        {
            // notify UI about the stale lists, it's up the UI to deal with this, either by showing a notification or an instant popup.
            Intent visbilityRequest = new Intent("org.dmfs.tasks.action.STALE_LIST_BROADCAST").setPackage(getContext().getPackageName());
            getContext().sendBroadcast(visbilityRequest);
        }
    }


    @Override
    public SQLiteOpenHelper getDatabaseHelper(Context context)
    {
        TaskDatabaseHelper helper = new TaskDatabaseHelper(context, this);

        return helper;
    }


    @Override
    public void onDatabaseCreated(SQLiteDatabase db)
    {
        // notify listeners that the database has been created
        Intent dbInitializedIntent = new Intent(TaskContract.ACTION_DATABASE_INITIALIZED);
        dbInitializedIntent.setDataAndType(TaskContract.getContentUri(mAuthority), TaskContract.MIMETYPE_AUTHORITY);
        // Android SDK 26 doesn't allow us to send implicit broadcasts, this particular brodcast is only for internal use, so just make it explicit by setting our package name
        dbInitializedIntent.setPackage(getContext().getPackageName());
        getContext().sendBroadcast(dbInitializedIntent);
    }


    @Override
    public void onDatabaseUpdate(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        if (oldVersion < 15)
        {
            mAsyncHandler.post(() -> ContentOperation.UPDATE_TIMEZONE.fire(getContext(), null));
        }
    }


    @Override
    protected boolean syncToNetwork(Uri uri)
    {
        return true;
    }


    @Override
    public void onAccountsUpdated(Account[] accounts)
    {
        // cache the known accounts so we can check whether we know accounts for which new lists are added
        mAccountCache.set(new HashSet<>(Arrays.asList(accounts)));
        // TODO: we probably can move the cleanup code here and get rid of the Utils class
        Utils.cleanUpLists(getContext(), getDatabaseHelper().getWritableDatabase(), accounts, mAuthority);
    }
}
