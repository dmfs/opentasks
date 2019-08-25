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

import androidx.annotation.NonNull;


/**
 * The state of a task notification.
 * <p>
 * TODO: refactor! The name is not quite correct and we may be able to generalize the interface.
 *
 * @author Marten Gajda
 */
public interface TaskNotificationState
{
    Uri instance();

    int taskVersion();

    @NonNull
    StateInfo info();
}
