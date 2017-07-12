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

package org.dmfs.tasks.utils;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;

import org.dmfs.tasks.utils.lazy.Lazy;


/**
 * Lazy access to {@link FragmentActivity#getSupportFragmentManager()}.
 * The Context must belong to an Activity.
 *
 * @author Gabor Keszthelyi
 */
// TODO use it from dmfs android tools library when available
public final class ContextFragmentManager implements Lazy<FragmentManager>
{
    private final Context mContext;


    public ContextFragmentManager(Context context)
    {
        mContext = context;
    }


    public ContextFragmentManager(View view)
    {
        this(view.getContext());
    }


    @Override
    public FragmentManager get()
    {
        return new ContextActivity(mContext).get().getSupportFragmentManager();
    }
}

