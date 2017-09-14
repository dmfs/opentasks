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
 * {@link NotificationSignal} that conditionally delegates to either {@link NoSignal} or to the given {@link NotificationSignal}
 * based on the provided boolean.
 *
 * @author Gabor Keszthelyi
 */
public final class Conditional implements NotificationSignal
{
    private final boolean mUseSignal;
    private final NotificationSignal mDelegate;


    public Conditional(boolean useSignal, NotificationSignal delegate)
    {
        mUseSignal = useSignal;
        mDelegate = delegate;
    }


    public Conditional(boolean useSignal, Context context)
    {
        this(useSignal, new SettingsSignal(context));
    }


    @Override
    public int value()
    {
        return (mUseSignal ? mDelegate : new NoSignal()).value();
    }
}
