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

package org.dmfs.tasks.utils;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

import org.dmfs.provider.tasks.AuthorityUtil;
import org.dmfs.tasks.R;
import org.dmfs.tasks.contract.TaskContract;
import org.dmfs.tasks.contract.TaskContract.TaskLists;


public class DatabaseInitializedReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (context.getResources().getBoolean(R.bool.opentasks_support_local_lists))
        {
            // The database was just created, insert a local task list
            ContentValues listValues = new ContentValues(5);
            listValues.put(TaskLists.LIST_NAME, context.getString(R.string.initial_local_task_list_name));
            listValues.put(TaskLists.LIST_COLOR, Color.rgb(30, 136, 229) /* material blue 600 */);
            listValues.put(TaskLists.VISIBLE, 1);
            listValues.put(TaskLists.SYNC_ENABLED, 1);
            listValues.put(TaskLists.OWNER, "");

            context.getContentResolver().insert(
                    TaskContract.TaskLists.getContentUri(AuthorityUtil.taskAuthority(context)).buildUpon()
                            .appendQueryParameter(TaskContract.CALLER_IS_SYNCADAPTER, "true")
                            .appendQueryParameter(TaskContract.ACCOUNT_NAME, TaskContract.LOCAL_ACCOUNT_NAME)
                            .appendQueryParameter(TaskContract.ACCOUNT_TYPE, TaskContract.LOCAL_ACCOUNT_TYPE).build(), listValues);
        }
    }
}
