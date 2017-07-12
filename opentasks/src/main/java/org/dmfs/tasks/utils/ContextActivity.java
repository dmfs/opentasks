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

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import org.dmfs.tasks.utils.lazy.Lazy;


/**
 * Access to the {@link Activity} reference from a {@link Context} that is supposed to be an {@link Activity}.
 * <p>
 * Note: simple casting may not always be safe, see for example: https://stackoverflow.com/q/21657045/4247460
 * <p>
 * This access works together with the {@link BaseActivity#getSystemService(String)} override.
 *
 * @author Gabor Keszthelyi
 */
// TODO use it from dmfs android tools library when available
public final class ContextActivity implements Lazy<FragmentActivity>
{
    private final Context mContext;


    public ContextActivity(Context context)
    {
        mContext = context;
    }


    public ContextActivity(View view)
    {
        this(view.getContext());
    }


    @Override
    public FragmentActivity get()
    {
        //noinspection WrongConstant
        Object activity = mContext.getSystemService(BaseActivity.SERVICE_ACTIVITY);
        if (activity == null)
        {
            throw new RuntimeException(
                    String.format("Context doesn't belong to an Activity which provides itself in getSystemService()." +
                            " Have you extended %s?", BaseActivity.class.getName()));
        }
        try
        {
            return (FragmentActivity) activity;
        }
        catch (ClassCastException e)
        {
            throw new RuntimeException("This Context doesn't belong to a FragmentActivity", e);
        }
    }
}
