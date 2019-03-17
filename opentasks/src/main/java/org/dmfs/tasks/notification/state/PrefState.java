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

package org.dmfs.tasks.notification.state;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;


/**
 * The {@link TaskNotificationState} or a task stored in the shared preferences (represented by a {@link Map.Entry}).
 *
 * @author Marten Gajda
 */
public final class PrefState implements TaskNotificationState
{
    private final Map.Entry<String, ?> mEntry;


    public PrefState(Map.Entry<String, ?> entry)
    {
        mEntry = entry;
    }


    @Override
    public Uri task()
    {
        return Uri.parse(mEntry.getKey());
    }


    @Override
    public int taskVersion()
    {
        return jsonObject().optInt("version", 0);
    }


    @Override
    public boolean ongoing()
    {
        return jsonObject().optBoolean("ongoing", false);
    }


    private JSONObject jsonObject()
    {
        try
        {
            return new JSONObject(mEntry.getValue().toString());
        }
        catch (JSONException e)
        {
            throw new RuntimeException(String.format("Can't parse JSONObject %s", mEntry.getValue()), e);
        }
    }
}
