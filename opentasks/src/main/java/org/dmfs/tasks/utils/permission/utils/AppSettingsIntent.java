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

package org.dmfs.tasks.utils.permission.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

import org.dmfs.jems.single.Single;


/**
 * {@link Single} for an {@link Intent} that opens the app's settings screen in the device settings.
 * <p>
 * See: https://stackoverflow.com/a/32983128/4247460
 *
 * @author Gabor Keszthelyi
 */
public final class AppSettingsIntent implements Single<Intent>
{
    private final Context mContext;


    public AppSettingsIntent(Context context)
    {
        mContext = context.getApplicationContext();
    }


    @Override
    public Intent value()
    {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", mContext.getPackageName(), null);
        intent.setData(uri);
        return intent;
    }
}