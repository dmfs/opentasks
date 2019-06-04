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

package org.dmfs.tasks.notification.signals;

import androidx.core.app.NotificationCompat;


/**
 * Represents a notification signal setting (signal meaning sound, vibration, lights) that can be set for a {@link NotificationCompat}
 * with {@link NotificationCompat.Builder#setDefaults(int)}.
 *
 * @author Gabor Keszthelyi
 */
public interface NotificationSignal
{
    /**
     * Returns the value that can be used for {@link NotificationCompat.Builder#setDefaults(int)}.
     * <p>
     * (<code>0</code> means no signal.)
     */
    int value();
}
