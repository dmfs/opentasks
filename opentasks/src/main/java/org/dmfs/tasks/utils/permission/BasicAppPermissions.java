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

import android.content.Context;
import android.os.Build;


/**
 * Basic {@link AppPermissions} implementation. It automatically handles different permission support on different Android versions.
 *
 * @author Marten Gajda
 */
public final class BasicAppPermissions implements AppPermissions
{
    private final AppPermissions mDelegate;


    public BasicAppPermissions(Context context)
    {
        mDelegate = Build.VERSION.SDK_INT < 23 ? new LegacyAppPermissions(context) : new MarshmallowPermissions(context);
    }


    @Override
    public Permission forName(String permissionName)
    {
        return mDelegate.forName(permissionName);
    }
}
