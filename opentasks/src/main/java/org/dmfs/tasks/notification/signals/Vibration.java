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
 * {@link NotificationSignal} for representing, adding, or toggling {@link Notification#DEFAULT_VIBRATE}
 * value.
 *
 * @author Gabor Keszthelyi
 */
public final class Vibration extends DelegatingNotificationSignal
{
    public Vibration(boolean enable, NotificationSignal original)
    {
        super(new Toggled(Notification.DEFAULT_VIBRATE, enable, original));
    }


    public Vibration(NotificationSignal original)
    {
        super(new Toggled(Notification.DEFAULT_VIBRATE, true, original));
    }


    public Vibration()
    {
        super(new Toggled(Notification.DEFAULT_VIBRATE, true, new NoSignal()));
    }

}
