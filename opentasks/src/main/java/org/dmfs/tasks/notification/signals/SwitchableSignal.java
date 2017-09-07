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

import android.content.Context;


/**
 * {@link NotificationSignal} that evaluates to either {@link NoSignal} or {@link SettingsSignal} based on the received boolean.
 *
 * @author Gabor Keszthelyi
 */
public final class SwitchableSignal implements NotificationSignal
{
    private final Context mContext;
    private final boolean mWithoutSignal;


    public SwitchableSignal(Context context, boolean withoutSignal)
    {
        mContext = context;
        mWithoutSignal = withoutSignal;
    }


    @Override
    public int value()
    {
        return (mWithoutSignal ? new NoSignal() : new SettingsSignal(mContext)).value();
    }
}
