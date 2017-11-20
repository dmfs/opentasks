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


/**
 * Represents a single Android permission.
 *
 * @author Marten Gajda
 */
public interface Permission
{
    /**
     * The name of this permission.
     *
     * @return A String containing the permission name.
     */
    String name();

    /**
     * Return whether this permission is granted or not.
     *
     * @return {@code true} if this permission has been granted to the app, {@code false} otherwise.
     */
    boolean isGranted();

    /**
     * Returns whether this permission can be requested at runtime. If this returns true you may use {@link #request()} to create a {@link PermissionRequest}
     * and send it.
     * <p>
     * Note that this also returns {@code true} if the permission has already been granted.
     *
     * @param activity
     *         The current {@link Activity}.
     *
     * @return {@code true} if this permission can be requested at runtime, {@code false} otherwise.
     */
    boolean isRequestable(Activity activity);

    /**
     * Returns whether this permission can be granted at runtime, either by a runtime permission request or in the settings of the device.
     * <p>
     * If this returns {@code true} but {@link #isRequestable(Activity)} returns {@code false} the user needs to go to the system settings in order to grant a
     * permission.
     *
     * @return {@code true} if and only if the permission can be granted at runtime.
     */
    boolean isGrantable();

    /**
     * Creates a {@link PermissionRequest} to request this {@link Permission}.
     *
     * @return A new {@link PermissionRequest} for this single {@link Permission}.
     */
    PermissionRequest request();
}
