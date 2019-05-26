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
import android.content.SharedPreferences;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.dmfs.iterators.decorators.Filtered;
import org.dmfs.iterators.filters.AnyOf;
import org.dmfs.tasks.utils.permission.utils.ManifestPermissionStrings;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;


/**
 * {@link AppPermissions} on Android 6 and newer.
 * <p>
 * This is not intended for public use. Use {@link BasicAppPermissions} instead.
 *
 * @author Marten Gajda
 */
final class MarshmallowPermissions implements AppPermissions
{
    private final Context mContext;
    private final SharedPreferences mSharedPreferences;


    MarshmallowPermissions(Context context)
    {
        mContext = context.getApplicationContext();
        mSharedPreferences = mContext.getSharedPreferences("opentasks.permissions", 0);
    }


    @Override
    public Permission forName(final String permissionName)
    {
        return new MarshmallowPermission(mContext, mSharedPreferences, permissionName);
    }


    /**
     * A {@link Permission} on API levels >= 23.
     * <p>
     * In contrast to {@link LegacyAppPermissions.LegacyPermission} this determines the state on access and may be grantable at runtime.
     */
    private static final class MarshmallowPermission implements Permission
    {
        private final Context mContext;
        private final SharedPreferences mSharedPreferences;
        private final String mName;


        MarshmallowPermission(Context context, SharedPreferences sharedPreferences, String name)
        {
            mContext = context.getApplicationContext();
            mSharedPreferences = sharedPreferences;
            mName = name;
        }


        @Override
        public String name()
        {
            return mName;
        }


        @Override
        public boolean isGranted()
        {
            return ContextCompat.checkSelfPermission(mContext, mName) == PERMISSION_GRANTED;
        }


        @Override
        public boolean isRequestable(Activity activity)
        {
            return isGranted() ||
                    isGrantable() && (!mSharedPreferences.contains(mName) || ActivityCompat.shouldShowRequestPermissionRationale(activity, mName));
        }


        @Override
        public boolean isGrantable()
        {
            return new Filtered<>(new ManifestPermissionStrings(mContext).iterator(), new AnyOf<>(mName)).hasNext();
        }


        @Override
        public PermissionRequest request()
        {
            return new MarshmallowPermissionRequest(mName, mSharedPreferences);
        }

    }


    /**
     * A simple {@link PermissionRequest}.
     */
    private final static class MarshmallowPermissionRequest implements PermissionRequest
    {
        private final String mName;
        private final SharedPreferences mSharedPreferences;


        private MarshmallowPermissionRequest(String mName, SharedPreferences mSharedPreferences)
        {
            this.mName = mName;
            this.mSharedPreferences = mSharedPreferences;
        }


        @Override
        public PermissionRequest withPermission(Permission... permissions)
        {
            throw new UnsupportedOperationException("Not implemented yet.");
        }


        @Override
        public void send(Activity activity)
        {
            // store the fact that we just requested the permission
            mSharedPreferences.edit().putBoolean(mName, true).apply();
            ActivityCompat.requestPermissions(activity, new String[] { mName }, 1);
        }
    }
}
