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

import android.app.Notification;


/**
 * {@link NotificationSignal} for representing, adding, or toggling {@link Notification#DEFAULT_SOUND}
 * value.
 *
 * @author Gabor Keszthelyi
 */
public final class Sound extends DelegatingNotificationSignal
{
    public Sound(boolean enable, NotificationSignal original)
    {
        super(new Toggled(Notification.DEFAULT_SOUND, enable, original));
    }


    public Sound(NotificationSignal original)
    {
        super(new Toggled(Notification.DEFAULT_SOUND, true, original));
    }


    public Sound()
    {
        super(new Toggled(Notification.DEFAULT_SOUND, true, new NoSignal()));
    }

}
