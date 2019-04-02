/*
 * Copyright 2019 dmfs GmbH
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

package org.dmfs.tasks.actions.conditions;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import org.dmfs.android.contentpal.RowDataSnapshot;
import org.dmfs.jems.function.BiFunction;
import org.dmfs.tasks.R;
import org.dmfs.tasks.contract.TaskContract;


/**
 * @author Marten Gajda
 */
public final class NotificationEnabled implements BiFunction<Context, RowDataSnapshot<TaskContract.Tasks>, Boolean>
{
    @Override
    public Boolean value(Context context, RowDataSnapshot<TaskContract.Tasks> snapshot)
    {
        if (Build.VERSION.SDK_INT >= 26)
        {
            // on Android SDK Level 26+ we leave this decision to Android and always attempt to show the notification
            return true;
        }
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        return settings.getBoolean(context.getString(R.string.opentasks_pref_notification_enabled), true);

    }
}
