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

package org.dmfs.tasks.actions.utils;

import android.content.Context;
import android.content.SharedPreferences;

import org.dmfs.jems.generator.Generator;


/**
 * {@link Generator} of {@link SharedPreferences} instances which contain the active notifications.
 *
 * @author Marten Gajda
 */
public final class NotificationPrefs implements Generator<SharedPreferences>
{
    private final Context mContext;


    public NotificationPrefs(Context context)
    {
        mContext = context;
    }


    @Override
    public SharedPreferences next()
    {
        return mContext.getSharedPreferences("org.dmfs.tasks.notifications", Context.MODE_PRIVATE);
    }
}
