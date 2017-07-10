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
import android.util.AttributeSet;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.List;


/**
 * An {@link ExpandableListView} that is able to retain the expanded groups after the dataset changed.
 *
 * @author Tobias Reinsch <tobias@dmfs.org>
 */
public class RetainExpandableListView extends ExpandableListView
{
    long[] mExpandedGroups;
    boolean mExpandFirst = false;
    boolean mExpandLast = false;


    public RetainExpandableListView(Context context)
    {
        super(context);
    }


    public RetainExpandableListView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }


    public RetainExpandableListView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }


    public void retainExpadedGroups(boolean expandFirst, boolean expandLast)
    {
        mExpandedGroups = getExpandedGroups();
        mExpandFirst = expandFirst;
        mExpandLast = expandLast;

    }


    @Override
    public void requestLayout()
    {
        expandGroups(mExpandedGroups);
        super.requestLayout();
    }


    public long[] getExpandedGroups()
    {
        ExpandableListAdapter adapter = this.getExpandableListAdapter();
        int count = adapter.getGroupCount();
        ArrayList<Long> expandedIds = new ArrayList<Long>();
        for (int i = 0; i < count; i++)
        {
            if (this.isGroupExpanded(i))
            {
                expandedIds.add(adapter.getGroupId(i));
            }
        }
        return toLongArray(expandedIds);
    }


    private static long[] toLongArray(List<Long> list)
    {
        long[] ret = new long[list.size()];
        int i = 0;
        for (Long e : list)
        {
            ret[i++] = e.longValue();
        }
        return ret;
    }


    public void expandGroups(long[] groupsToExpand)
    {
        // this.expandedIds = expandedIds;
        if (groupsToExpand != null && groupsToExpand.length > 0)
        {
            ExpandableListAdapter adapter = getExpandableListAdapter();
            if (adapter != null)
            {
                for (int i = 0; i < adapter.getGroupCount(); i++)
                {
                    long id = adapter.getGroupId(i);
                    if (inArray(groupsToExpand, id))
                    {
                        expandGroup(i);
                    }
                }
            }
        }
    }


    private static boolean inArray(long[] array, long element)
    {
        for (long l : array)
        {
            if (l == element)
            {
                return true;
            }
        }
        return false;
    }

}
