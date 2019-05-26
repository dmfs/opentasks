/*
 * Copyright 2018 dmfs GmbH
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

package org.dmfs.tasks.utils;

import androidx.fragment.app.Fragment;


/**
 * {@link Runnable} control proxy / decorator that only runs the delegate {@link Runnable}
 * if the provided {@link Fragment} is still added to its activity when {@link #run()} is called.
 * <p>
 * Use this to safely execute a delayed UI update in a {@link Fragment}.
 * <p>
 * Do not use this when execution has (side)effects which have to be guaranteed.
 *
 * @author Gabor Keszthelyi
 */
public final class SafeFragmentUiRunnable implements Runnable
{
    private final Fragment mFragment;
    private final Runnable mDelegate;


    public SafeFragmentUiRunnable(Fragment fragment, Runnable delegate)
    {
        mFragment = fragment;
        mDelegate = delegate;
    }


    @Override
    public void run()
    {
        if (mFragment.isAdded() && mFragment.getActivity() != null)
        {
            mDelegate.run();
        }
    }
}
