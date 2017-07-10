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

import org.dmfs.tasks.utils.factory.Factory;
import org.dmfs.tasks.utils.factory.SimpleLazy;


/**
 * An abstract {@link CharSequence} that uses the provided factory to create the value which will be cached.
 *
 * @author Gabor Keszthelyi
 */
// TODO Use it from dmfs java tools library when available
public abstract class AbstractCharSequence implements CharSequence
{

    private final SimpleLazy<CharSequence> mDelegate;


    public AbstractCharSequence(Factory<CharSequence> charSequenceFactory)
    {
        mDelegate = new SimpleLazy<>(charSequenceFactory);
    }


    @Override
    public final int length()
    {
        return mDelegate.get().length();
    }


    @Override
    public final char charAt(int index)
    {
        return mDelegate.get().charAt(index);
    }


    @Override
    public final CharSequence subSequence(int start, int end)
    {
        return mDelegate.get().subSequence(start, end);
    }


    @NonNull
    @Override
    public final String toString()
    {
        return mDelegate.get().toString();
    }
}