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

import android.content.Context;
import android.content.pm.ApplicationInfo;

import org.dmfs.jems.single.Single;


/**
 * @author Gabor Keszthelyi
 */
// TODO Use it from bolts lib when available
public final class ManifestAppName implements Single<CharSequence>
{
    private final Context mAppContext;


    public ManifestAppName(Context context)
    {
        mAppContext = context.getApplicationContext();
    }


    @Override
    public CharSequence value()
    {
        ApplicationInfo applicationInfo = mAppContext.getApplicationInfo();

        if (applicationInfo.labelRes != 0)
        {
            return mAppContext.getString(applicationInfo.labelRes);
        }

        if (applicationInfo.nonLocalizedLabel != null)
        {
            return applicationInfo.nonLocalizedLabel;
        }

        throw new RuntimeException("Application name not found in the manifest");
    }
}
