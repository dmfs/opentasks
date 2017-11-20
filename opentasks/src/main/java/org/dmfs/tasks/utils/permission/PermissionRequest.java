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
 * A request to grant a permission.
 *
 * @author Marten Gajda
 */
public interface PermissionRequest
{
    /**
     * Adds the given {@link Permission}s to this request.
     *
     * @param permissions
     *         An array of {@link Permission}s.
     *
     * @return A new {@link PermissionRequest} that also asks for the added {@link Permission}s.
     */
    PermissionRequest withPermission(Permission... permissions);

    /**
     * Send this {@link PermissionRequest} and ask the user to grant the {@link Permission}s.
     *
     * @param activity
     *         An {@link Activity}.
     */
    void send(Activity activity);
}
