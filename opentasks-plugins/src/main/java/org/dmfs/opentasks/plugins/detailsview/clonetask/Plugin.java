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

package org.dmfs.opentasks.plugins.detailsview.clonetask;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.dmfs.tasks.contract.TaskContract;


/**
 * @author Marten Gajda
 */
public final class Plugin extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.v("TASKPLUGIN---------", "probe recevied " + System.currentTimeMillis() % 10000);
        Log.v("TASKPLUGIN---------", "probe recevied " + intent.toString());

        Bundle result = new Bundle();
        result.putString("title", "Clone Task");
        result.putString("title_short", "Clone");
        result.putParcelable("action", PendingIntent.getActivity(context, 1,
                new Intent(Intent.ACTION_INSERT).setData(TaskContract.Tasks.getContentUri(intent.getData().getAuthority()))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                PendingIntent.FLAG_CANCEL_CURRENT));
        Intent response = new Intent("org.dmfs.opentasks.action.PLUGIN_RESPONSE").addCategory("org.dmfs.opentasks.category.DETAILS_MENU").putExtras(result);
        // .setData(intent.getData());
        Log.v("TASKPLUGIN---------", "response " + response.toString());

        context.sendBroadcast(response);
    }
}
