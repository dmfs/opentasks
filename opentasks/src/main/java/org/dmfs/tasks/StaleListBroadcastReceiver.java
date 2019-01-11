/*
 * Copyright 2018 dmfs GmbH
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
import android.accounts.AccountManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import org.dmfs.android.contentpal.RowDataSnapshot;
import org.dmfs.android.contentpal.RowSnapshot;
import org.dmfs.android.contentpal.predicates.AccountEq;
import org.dmfs.android.contentpal.predicates.EqArg;
import org.dmfs.android.contentpal.predicates.Not;
import org.dmfs.android.contentpal.projections.MultiProjection;
import org.dmfs.android.contentpal.rowsets.QueryRowSet;
import org.dmfs.iterables.elementary.Seq;
import org.dmfs.jems.iterable.composite.Joined;
import org.dmfs.jems.iterable.decorators.Mapped;
import org.dmfs.opentaskspal.predicates.AnyOf;
import org.dmfs.opentaskspal.views.TaskListsView;
import org.dmfs.provider.tasks.AuthorityUtil;
import org.dmfs.tasks.contract.TaskContract;

import java.util.ArrayList;

import static java.util.Collections.singletonList;


/**
 * @author Marten Gajda
 */
public final class StaleListBroadcastReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (Build.VERSION.SDK_INT < 26)
        {
            // this receiver is Android 8+ only
            return;
        }
        AccountManager accountManager = AccountManager.get(context);
        String authority = AuthorityUtil.taskAuthority(context);
        String description = String.format("Please give %s access to the following account", context.getString(R.string.app_name));
        // request access to each account we don't know yet individually
        for (Intent accountRequestIntent :
                new Mapped<>(
                        account -> AccountManager.newChooseAccountIntent(account, new ArrayList<Account>(singletonList(account)), null,
                                description, null,
                                null, null),
                        new Mapped<RowDataSnapshot<TaskContract.TaskLists>, Account>(
                                this::account,
                                new Mapped<>(RowSnapshot::values,
                                        new QueryRowSet<>(
                                                new TaskListsView(authority, context.getContentResolver().acquireContentProviderClient(authority)),
                                                new MultiProjection<>(TaskContract.TaskLists.ACCOUNT_NAME, TaskContract.TaskLists.ACCOUNT_TYPE),
                                                new Not(new AnyOf(
                                                        new Joined<>(new Seq<>(
                                                                new EqArg(TaskContract.TaskLists.ACCOUNT_TYPE, TaskContract.LOCAL_ACCOUNT_TYPE)),
                                                                new Mapped<>(AccountEq::new, new Seq<>(accountManager.getAccounts()))))))))))
        {
            context.startActivity(accountRequestIntent);
        }
    }


    private Account account(RowDataSnapshot<TaskContract.TaskLists> data)
    {
        return (new Account(
                data.data(TaskContract.TaskLists.ACCOUNT_NAME, s -> s).value(),
                data.data(TaskContract.TaskLists.ACCOUNT_TYPE, s -> s).value()));
    }
}
