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

import org.json.JSONObject;

import androidx.annotation.NonNull;


/**
 * The {@link StateInfo} stored in a {@link JSONObject}.
 *
 * @author Marten Gajda
 */
final class JsonStateInfo implements StateInfo
{

    private final JSONObject mObject;


    JsonStateInfo(@NonNull JSONObject object)
    {
        mObject = object;
    }


    @Override
    public boolean pinned()
    {
        return mObject.optBoolean("ongoing", false);
    }


    @Override
    public boolean due()
    {
        return mObject.optBoolean("due", false);
    }


    @Override
    public boolean started()
    {
        return mObject.optBoolean("started", false);
    }


    @Override
    public boolean done()
    {
        return mObject.optBoolean("done", false);
    }
}
