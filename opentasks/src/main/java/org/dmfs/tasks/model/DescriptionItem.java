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

package org.dmfs.tasks.model;

import androidx.annotation.Nullable;


/**
 * A bloody POJO o_O to store a description/check list item
 */
public final class DescriptionItem
{
    public boolean checkbox;
    public boolean checked;
    public String text;


    public DescriptionItem(boolean checkbox, boolean checked, String text)
    {
        this.checkbox = checkbox;
        this.checked = checked;
        this.text = text;
    }


    @Override
    public boolean equals(@Nullable Object obj)
    {
        return obj instanceof DescriptionItem
                && ((DescriptionItem) obj).checkbox == checkbox
                && ((DescriptionItem) obj).checked == checked
                && ((DescriptionItem) obj).text.equals(text);
    }


    @Override
    public int hashCode()
    {
        return text.hashCode() * 31 + (checkbox ? 1 : 0) + (checked ? 2 : 0);
    }
}
