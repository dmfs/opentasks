/*
 * Copyright 2016 Marten Gajda <marten@dmfs.org>
 *
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

package org.dmfs.tasks.share;

/**
 * @author Gabor Keszthelyi
 */
public abstract class StringTaskText implements TaskText
{
    private final String mValue;


    public StringTaskText(String value)
    {
        if (value == null)
        {
            throw new IllegalArgumentException("value cannot be null");
        }
        mValue = value;
    }

    @Override
    public final int length()
    {
        return mValue.length();
    }


    @Override
    public final char charAt(int index)
    {
        return mValue.charAt(index);
    }


    @Override
    public final CharSequence subSequence(int start, int end)
    {
        return mValue.subSequence(start, end);
    }


    @Override
    public String toString()
    {
        return mValue;
    }
}
