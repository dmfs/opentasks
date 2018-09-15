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

import android.text.TextUtils;


public final class CheckListItem
{
    public boolean checked;
    public String text;


    public CheckListItem(boolean checked, String text)
    {
        this.checked = checked;
        this.text = text;
    }


    @Override
    public int hashCode()
    {
        return text != null ? (text.hashCode() << 1) + (checked ? 1 : 0) : (checked ? 1 : 0);
    }


    public boolean equals(Object o)
    {
        if (!(o instanceof CheckListItem))
        {
            return false;
        }
        CheckListItem other = (CheckListItem) o;
        return TextUtils.equals(text, other.text) && checked == other.checked;
    }

}
