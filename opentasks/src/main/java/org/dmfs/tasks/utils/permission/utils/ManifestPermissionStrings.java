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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import org.dmfs.iterators.elementary.Seq;

import java.util.Iterator;


/**
 * An {@link Iterable} iterating all permission strings in the Manifest of this app.
 *
 * @author Marten Gajda
 */
public final class ManifestPermissionStrings implements Iterable<String>
{
    private final Context mAppContext;


    public ManifestPermissionStrings(Context context)
    {
        mAppContext = context.getApplicationContext();
    }


    @Override
    public Iterator<String> iterator()
    {
        try
        {
            PackageInfo packageInfo = mAppContext.getPackageManager().getPackageInfo(mAppContext.getPackageName(), PackageManager.GET_PERMISSIONS);
            return new Seq<>(packageInfo.requestedPermissions);
        }
        catch (PackageManager.NameNotFoundException e)
        {
            throw new RuntimeException("WTF! Own package not found by PackageManager?!", e);
        }
    }
}
