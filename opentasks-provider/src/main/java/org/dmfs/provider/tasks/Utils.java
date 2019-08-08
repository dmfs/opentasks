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
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.dmfs.iterables.SingletonIterable;
import org.dmfs.jems.iterable.composite.Joined;
import org.dmfs.jems.iterable.decorators.Mapped;
import org.dmfs.jems.procedure.composite.Batch;
import org.dmfs.provider.tasks.TaskDatabaseHelper.Tables;
import org.dmfs.provider.tasks.utils.ResourceArray;
import org.dmfs.provider.tasks.utils.With;
import org.dmfs.tasks.contract.TaskContract;
import org.dmfs.tasks.contract.TaskContract.Instances;
import org.dmfs.tasks.contract.TaskContract.SyncState;
import org.dmfs.tasks.contract.TaskContract.TaskListColumns;
import org.dmfs.tasks.contract.TaskContract.TaskListSyncColumns;
import org.dmfs.tasks.contract.TaskContract.TaskLists;
import org.dmfs.tasks.contract.TaskContract.Tasks;
import org.dmfs.tasks.provider.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * The Class Utils.
 *
 * @author Tobias Reinsch <tobias@dmfs.org>
 * @author Marten Gajda <marten@dmfs.org>
 */
public class Utils
{
    public static void sendActionProviderChangedBroadCast(Context context, String authority)
    {
        // TODO: Using the TaskContract content uri results in a "Unknown URI content" error message. Using the Tasks content uri instead will break the
        // broadcast receiver. We have to find away around this
        // TODO: coalesce fast consecutive broadcasts, a delay of up to 1 second should be acceptable

        new With<>(new Intent(Intent.ACTION_PROVIDER_CHANGED, TaskContract.getContentUri(authority)))
                .process(providerChangedIntent ->
                        new Batch<Intent>(context::sendBroadcast)
                                .process(new Mapped<>(
                                        packageName -> new Intent(providerChangedIntent).setPackage(packageName),
                                        // TODO: fow now we hard code 3rd party package names, this should be replaced by some sort or registry
                                        // see https://github.com/dmfs/opentasks/issues/824
                                        new Joined<>(
                                                new SingletonIterable<>(context.getPackageName()),
                                                new ResourceArray(context, R.array.opentasks_provider_changed_receivers)))));
    }


    public static void cleanUpLists(Context context, SQLiteDatabase db, Account[] accounts, String authority)
    {
        // make a list of the accounts array
        List<Account> accountList = Arrays.asList(accounts);

        db.beginTransaction();

        try
        {
            Cursor c = db.query(Tables.LISTS, new String[] { TaskListColumns._ID, TaskListSyncColumns.ACCOUNT_NAME, TaskListSyncColumns.ACCOUNT_TYPE }, null,
                    null, null, null, null);

            // build a list of all task list ids that no longer have an account
            List<Long> obsoleteLists = new ArrayList<Long>();
            try
            {
                while (c.moveToNext())
                {
                    String accountType = c.getString(2);
                    // mark list for removal if it is non-local and the account
                    // is not in accountList
                    if (!TaskContract.LOCAL_ACCOUNT_TYPE.equals(accountType))
                    {
                        Account account = new Account(c.getString(1), accountType);
                        if (!accountList.contains(account))
                        {
                            obsoleteLists.add(c.getLong(0));

                            // remove syncstate for this account right away
                            db.delete(Tables.SYNCSTATE, SyncState.ACCOUNT_NAME + "=? and " + SyncState.ACCOUNT_TYPE + "=?", new String[] {
                                    account.name,
                                    account.type });
                        }
                    }
                }
            }
            finally
            {
                c.close();
            }

            if (obsoleteLists.size() == 0)
            {
                // nothing to do here
                return;
            }

            // remove all accounts in the list
            for (Long id : obsoleteLists)
            {
                if (id != null)
                {
                    db.delete(Tables.LISTS, TaskListColumns._ID + "=" + id, null);
                }
            }
            db.setTransactionSuccessful();
        }
        finally
        {
            db.endTransaction();
        }
        // notify all observers

        ContentResolver cr = context.getContentResolver();
        cr.notifyChange(TaskLists.getContentUri(authority), null);
        cr.notifyChange(Tasks.getContentUri(authority), null);
        cr.notifyChange(Instances.getContentUri(authority), null);

        Utils.sendActionProviderChangedBroadCast(context, authority);
    }

}
