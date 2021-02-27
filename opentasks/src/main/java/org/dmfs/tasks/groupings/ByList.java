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

package org.dmfs.tasks.groupings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Paint;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import org.dmfs.tasks.QuickAddDialogFragment;
import org.dmfs.tasks.R;
import org.dmfs.tasks.contract.TaskContract.Instances;
import org.dmfs.tasks.contract.TaskContract.TaskLists;
import org.dmfs.tasks.groupings.cursorloaders.CursorLoaderFactory;
import org.dmfs.tasks.utils.ExpandableChildDescriptor;
import org.dmfs.tasks.utils.ExpandableGroupDescriptor;
import org.dmfs.tasks.utils.ExpandableGroupDescriptorAdapter;
import org.dmfs.tasks.utils.ViewDescriptor;

import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceManager;


/**
 * Definition of the by-list grouping.
 * <p>
 * <p>
 * TODO: refactor!
 * </p>
 * <p>
 * TODO: refactor!
 * </p>
 * <p>
 * TODO: also, don't forget to refactor!
 * </p>
 * <p>
 * The plan is to provide some kind of GroupingDescriptior that provides the {@link ExpandableGroupDescriptorAdapter}, a name and a set of filters. Also it
 * should take care of persisting and restoring the open groups, selected filters ...
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public class ByList extends AbstractGroupingFactory
{
    /**
     * A {@link ViewDescriptor} that knows how to present the tasks in the task list.
     */
    public final ViewDescriptor TASK_VIEW_DESCRIPTOR = new BaseTaskViewDescriptor()
    {
        private int mFlingContentViewId = R.id.flingContentView;
        private int mFlingRevealLeftViewId = R.id.fling_reveal_left;
        private int mFlingRevealRightViewId = R.id.fling_reveal_right;


        @Override
        public void populateView(View view, Cursor cursor, BaseExpandableListAdapter adapter, int flags)
        {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(view.getContext());
            TextView title = getView(view, android.R.id.title);
            boolean isClosed = cursor.getInt(13) > 0;

            resetFlingView(view);

            if (title != null)
            {
                String text = cursor.getString(5);
                title.setText(text);
                if (isClosed)
                {
                    title.setPaintFlags(title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                }
                else
                {
                    title.setPaintFlags(title.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                }
            }

            setDueDate(getView(view, R.id.task_due_date), null, INSTANCE_DUE_ADAPTER.get(cursor), isClosed);

            setPrio(prefs, view, cursor);
            setColorBar(view, cursor);
            setDescription(view, cursor);
            setOverlay(view, cursor.getPosition(), cursor.getCount());
        }


        @Override
        public int getView()
        {
            return R.layout.task_list_element;
        }


        @Override
        public int getFlingContentViewId()
        {
            return mFlingContentViewId;
        }


        @Override
        public int getFlingRevealLeftViewId()
        {
            return mFlingRevealLeftViewId;
        }


        @Override
        public int getFlingRevealRightViewId()
        {
            return mFlingRevealRightViewId;
        }
    };

    /**
     * A {@link ViewDescriptor} that knows how to present list groups.
     */
    public final ViewDescriptor GROUP_VIEW_DESCRIPTOR = new ViewDescriptor()
    {

        @Override
        public void populateView(View view, Cursor cursor, BaseExpandableListAdapter adapter, int flags)
        {
            int position = cursor.getPosition();

            // set list title
            TextView title = (TextView) view.findViewById(android.R.id.title);
            if (title != null)
            {
                title.setText(getTitle(cursor, view.getContext()));
            }

            // set list account
            TextView text1 = (TextView) view.findViewById(android.R.id.text1);
            if (text1 != null)
            {
                text1.setText(cursor.getString(3));
            }

            // set list elements
            TextView text2 = (TextView) view.findViewById(android.R.id.text2);
            int childrenCount = adapter.getChildrenCount(position);
            if (text2 != null && ((ExpandableGroupDescriptorAdapter) adapter).childCursorLoaded(position))
            {
                Resources res = view.getContext().getResources();

                text2.setText(res.getQuantityString(R.plurals.number_of_tasks, childrenCount, childrenCount));
            }

            View quickAddTask = view.findViewById(R.id.quick_add_task);
            if (quickAddTask != null)
            {
                quickAddTask.setOnClickListener(quickAddClickListener);
                quickAddTask.setTag(cursor.getLong(cursor.getColumnIndex(TaskLists._ID)));
            }

            if ((flags & FLAG_IS_EXPANDED) != 0)
            {
                // show quick add and hide task count
                if (quickAddTask != null)
                {
                    quickAddTask.setVisibility(View.VISIBLE);
                }
                if (text2 != null)
                {
                    text2.setVisibility(View.GONE);
                }
            }
            else
            {
                // hide quick add and show task count
                if (quickAddTask != null)
                {
                    quickAddTask.setVisibility(View.GONE);
                }
                if (text2 != null)
                {
                    text2.setVisibility(View.VISIBLE);
                }
            }
        }


        private final OnClickListener quickAddClickListener = new OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                Long tag = (Long) v.getTag();
                if (tag != null)
                {
                    QuickAddDialogFragment.newInstance(tag)
                            .show(mActivity.getSupportFragmentManager(), null);
                }
            }
        };


        @Override
        public int getView()
        {
            return R.layout.task_list_group;
        }


        /**
         * Return the title of a list group.
         *
         * @param cursor
         *         A {@link Cursor} pointing to the current group.
         *
         * @return A {@link String} with the group name.
         */
        private String getTitle(Cursor cursor, Context context)
        {
            return cursor.getString(1);
        }


        @Override
        public int getFlingContentViewId()
        {
            return -1;
        }


        @Override
        public int getFlingRevealLeftViewId()
        {
            return -1;
        }


        @Override
        public int getFlingRevealRightViewId()
        {
            return -1;
        }

    };

    private final FragmentActivity mActivity;


    public ByList(String authority, FragmentActivity activity)
    {
        super(authority);
        mActivity = activity;
    }


    @Override
    public ExpandableChildDescriptor makeExpandableChildDescriptor(String authority)
    {
        return new ExpandableChildDescriptor(Instances.getContentUri(authority), INSTANCE_PROJECTION, Instances.VISIBLE + "=1 and " + Instances.LIST_ID + "=?",
                Instances.INSTANCE_DUE_SORTING + " is null, " + Instances.INSTANCE_DUE_SORTING + ", " + Instances.TITLE + " COLLATE NOCASE ASC", 0)
                .setViewDescriptor(TASK_VIEW_DESCRIPTOR);
    }


    @Override
    public ExpandableGroupDescriptor makeExpandableGroupDescriptor(String authority)
    {
        return new ExpandableGroupDescriptor(new CursorLoaderFactory(TaskLists.getContentUri(authority), new String[] {
                TaskLists._ID, TaskLists.LIST_NAME,
                TaskLists.LIST_COLOR, TaskLists.ACCOUNT_NAME }, TaskLists.VISIBLE + ">0 and " + TaskLists.SYNC_ENABLED + ">0", null,
                TaskLists.ACCOUNT_NAME + ", "
                        + TaskLists.LIST_NAME), makeExpandableChildDescriptor(authority)).setViewDescriptor(GROUP_VIEW_DESCRIPTOR);
    }


    @Override
    public int getId()
    {
        return R.id.task_group_by_list;
    }

}
