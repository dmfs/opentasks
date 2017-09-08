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
 * Decorator for {@link NotificationSignal} that toggles a flag value. Flag must be one of
 * {@link Notification#DEFAULT_LIGHTS}, {@link Notification#DEFAULT_SOUND}, {@link Notification#DEFAULT_VIBRATE}.
 * <p>
 * Intended for internal use in the package only since it exposes the bit flag mechanism.
 *
 * @author Gabor Keszthelyi
 */
final class Toggled implements NotificationSignal
{
    private final int mFlag;
    private final boolean mEnable;
    private final NotificationSignal mOriginal;


    /**
     * @param flag
     *         must be one of {@link Notification#DEFAULT_LIGHTS}, {@link Notification#DEFAULT_SOUND}, {@link Notification#DEFAULT_VIBRATE}
     */
    Toggled(int flag, boolean enable, NotificationSignal original)
    {
        mFlag = flag;
        mEnable = enable;
        mOriginal = original;
    }


    @Override
    public int value()
    {
        if (mFlag != Notification.DEFAULT_VIBRATE && mFlag != Notification.DEFAULT_SOUND && mFlag != Notification.DEFAULT_LIGHTS)
        {
            throw new IllegalArgumentException("Notification signal flag is not valid: " + mFlag);
        }
        return mEnable ? addFlag(mOriginal.value(), mFlag) : removeFlag(mOriginal.value(), mFlag);
    }


    private int addFlag(int flagSet, int flag)
    {
        return flagSet | flag;
    }


    private int removeFlag(int flagSet, int flag)
    {
        return flagSet & (~flag);
    }
}
