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

import android.content.Context;

import org.dmfs.iterators.elementary.Seq;

import java.util.Iterator;


/**
 * An {@link Iterable} of a string array resource.
 *
 * @author Marten Gajda
 */
public final class ResourceArray implements Iterable<String>
{
    private final Context mContext;
    private final int mResource;


    public ResourceArray(Context context, int resource)
    {
        mContext = context;
        mResource = resource;
    }


    @Override
    public Iterator<String> iterator()
    {
        return new Seq<>(mContext.getResources().getStringArray(mResource));
    }
}
