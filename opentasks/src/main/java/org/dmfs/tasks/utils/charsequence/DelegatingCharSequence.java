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

package org.dmfs.tasks.utils.charsequence;

import android.support.annotation.NonNull;


/**
 * Base class for {@link CharSequence}s that delegate to another {@link CharSequence} potentially composed in their constructor.
 *
 * @author Gabor Keszthelyi
 */
public abstract class DelegatingCharSequence implements CharSequence
{
    private final CharSequence mDelegate;


    public DelegatingCharSequence(@NonNull CharSequence delegate)
    {
        mDelegate = delegate;
    }


    @Override
    public final int length()
    {
        return mDelegate.length();
    }


    @Override
    public final char charAt(int index)
    {
        return mDelegate.charAt(index);
    }


    @Override
    public final CharSequence subSequence(int start, int end)
    {
        return mDelegate.subSequence(start, end);
    }


    @Override
    public final String toString()
    {
        return mDelegate.toString();
    }
}
