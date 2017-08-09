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

import org.dmfs.tasks.utils.BitFlagUtils;


/**
 * {@link NotificationSignal} which receives all the relevant signal booleans in the constructor.
 *
 * @author Gabor Keszthelyi
 */
public final class CustomizableSignal implements NotificationSignal
{
    private final boolean mSound;
    private final boolean mVibrate;
    private final boolean mLight;


    public CustomizableSignal(boolean sound, boolean vibrate, boolean light)
    {
        mSound = sound;
        mVibrate = vibrate;
        mLight = light;
    }


    @Override
    public int defaultsValue()
    {
        int result = 0;
        if (mSound)
        {
            result = BitFlagUtils.addFlag(result, Notification.DEFAULT_SOUND);
        }
        if (mVibrate)
        {
            result = BitFlagUtils.addFlag(result, Notification.DEFAULT_VIBRATE);
        }
        if (mLight)
        {
            result = BitFlagUtils.addFlag(result, Notification.DEFAULT_LIGHTS);
        }
        return result;
    }
}
