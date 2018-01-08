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

import org.dmfs.jems.single.Single;
import org.dmfs.jems.single.adapters.Unchecked;
import org.dmfs.jems.single.elementary.Frozen;
import org.json.JSONObject;

import java.util.Map;

import androidx.annotation.NonNull;


/**
 * The {@link TaskNotificationState} or a task stored in the shared preferences (represented by a {@link Map.Entry}).
 *
 * @author Marten Gajda
 */
public final class PrefState implements TaskNotificationState
{
    private final Map.Entry<String, ?> mEntry;
    private final Single<JSONObject> mStateObject;


    public PrefState(@NonNull Map.Entry<String, ?> entry)
    {
        mEntry = entry;
        mStateObject = new Frozen<>(new Unchecked<>("Could not parse notification JSON object", () -> new JSONObject(entry.getValue().toString())));
    }


    @NonNull
    @Override
    public Uri instance()
    {
        return Uri.parse(mEntry.getKey());
    }


    @Override
    public int taskVersion()
    {
        return mStateObject.value().optInt("version", 0);
    }


    @NonNull
    @Override
    public StateInfo info()
    {
        return new JsonStateInfo(mStateObject.value());
    }
}
