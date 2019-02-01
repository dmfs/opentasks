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

package org.dmfs.provider.tasks.utils;

import android.util.Log;

import org.dmfs.jems.fragile.Fragile;
import org.dmfs.jems.single.Single;

import java.util.Locale;


/**
 * A simple class to measure the execution time of a given piece of code.
 *
 * @author Marten Gajda
 */
public final class Profiled
{
    private final String mSubject;


    public Profiled(String subject)
    {
        mSubject = subject;
    }


    public void run(Runnable runnable)
    {
        long start = System.currentTimeMillis();
        runnable.run();
        Log.d("Profiled", String.format(Locale.ENGLISH, "Time spent in %s: %d milliseconds", mSubject, System.currentTimeMillis() - start));
    }


    public <V> V run(Single<V> runnable)
    {

        long start = System.currentTimeMillis();
        try
        {
            return runnable.value();
        }
        finally
        {
            Log.d("Profiled", String.format(Locale.ENGLISH, "Time spent in %s: %d milliseconds", mSubject, System.currentTimeMillis() - start));
        }
    }


    public <V, E extends Exception> V run(Fragile<V, E> runnable) throws E
    {

        long start = System.currentTimeMillis();
        try
        {
            return runnable.value();
        }
        finally
        {
            Log.d("Profiled", String.format(Locale.ENGLISH, "Time spent in %s: %d milliseconds", mSubject, System.currentTimeMillis() - start));
        }
    }

}
