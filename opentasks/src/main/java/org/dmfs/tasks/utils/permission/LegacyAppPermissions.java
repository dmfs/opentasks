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

package org.dmfs.tasks.utils.permission;

import android.app.Activity;
import android.content.Context;
import androidx.core.content.ContextCompat;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;


/**
 * {@link AppPermissions} on Android API level <23.
 * <p>
 * This is not intended for public use. Use {@link BasicAppPermissions} instead.
 *
 * @author Marten Gajda
 */
final class LegacyAppPermissions implements AppPermissions
{
    private final Context mAppContext;


    LegacyAppPermissions(Context appContext)
    {
        mAppContext = appContext.getApplicationContext();
    }


    @Override
    public Permission forName(String permissionName)
    {
        return new LegacyPermission(permissionName, ContextCompat.checkSelfPermission(mAppContext, permissionName) == PERMISSION_GRANTED);
    }


    /**
     * A {@link Permission} on API levels < 23.
     */
    private static final class LegacyPermission implements Permission
    {
        private final String mName;
        private final boolean mIsGranted;


        LegacyPermission(String name, boolean isGranted)
        {
            mName = name;
            mIsGranted = isGranted;
        }


        @Override
        public String name()
        {
            return mName;
        }


        @Override
        public boolean isGranted()
        {
            return mIsGranted;
        }


        @Override
        public boolean isRequestable(Activity activity)
        {
            return isGranted();
        }


        @Override
        public boolean isGrantable()
        {
            return false;
        }


        @Override
        public PermissionRequest request()
        {
            return new NoOpPermissionRequest();
        }
    }
}
