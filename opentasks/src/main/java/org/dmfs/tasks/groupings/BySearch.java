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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.format.DateUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.dmfs.tasks.R;
import org.dmfs.tasks.contract.TaskContract.Instances;
import org.dmfs.tasks.contract.TaskContract.Tasks;
import org.dmfs.tasks.groupings.cursorloaders.SearchHistoryCursorLoaderFactory;
import org.dmfs.tasks.model.TaskFieldAdapters;
import org.dmfs.tasks.utils.ExpandableChildDescriptor;
import org.dmfs.tasks.utils.ExpandableGroupDescriptor;
import org.dmfs.tasks.utils.ExpandableGroupDescriptorAdapter;
import org.dmfs.tasks.utils.SearchChildDescriptor;
import org.dmfs.tasks.utils.SearchHistoryDatabaseHelper;
import org.dmfs.tasks.utils.SearchHistoryDatabaseHelper.SearchHistoryColumns;
import org.dmfs.tasks.utils.SearchHistoryHelper;
import org.dmfs.tasks.utils.ViewDescriptor;

import androidx.preference.PreferenceManager;


/**
 * Definition of the search history grouping.
 *
 * @author Tobias Reinsch <tobias@dmfs.org>
 * @author Marten Gajda <marten@dmfs.org>
 */
public class BySearch extends AbstractGroupingFactory
{
    /**
     * A {@link ViewDescriptor} that knows how to present the tasks in the task list grouped by priority.
     */
    public final ViewDescriptor TASK_VIEW_DESCRIPTOR = new BaseTaskViewDescriptor()
    {

        private int mFlingContentViewId = R.id.flingContentView;
        private int mFlingRevealLeftViewId = R.id.fling_reveal_left;
        private int mFlingRevealRightViewId = R.id.fling_reveal_right;


        @SuppressLint("NewApi")
        @Override
        public void populateView(View view, Cursor cursor, BaseExpandableListAdapter adapter, int flags)
        {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(view.getContext());
            TextView title = getView(view, android.R.id.title);
            boolean isClosed = TaskFieldAdapters.IS_CLOSED.get(cursor);

            resetFlingView(view);

            if (title != null)
            {
                String text = TaskFieldAdapters.TITLE.get(cursor);
                // float score = TaskFieldAdapters.SCORE.get(cursor);
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
            long now = System.currentTimeMillis();
            int position = cursor.getPosition();

            // set list title
            String groupTitle = getTitle(cursor, view.getContext());
            TextView title = (TextView) view.findViewById(android.R.id.title);
            if (title != null)
            {
                title.setText(groupTitle);

            }
            // set search time
            TextView text1 = (TextView) view.findViewById(android.R.id.text1);
            if (text1 != null)
            {
                text1.setText(DateUtils.getRelativeTimeSpanString(
                        cursor.getLong(cursor.getColumnIndex(SearchHistoryDatabaseHelper.SearchHistoryColumns.TIMESTAMP)), now, DateUtils.MINUTE_IN_MILLIS));
            }

            // set list elements
            TextView text2 = (TextView) view.findViewById(android.R.id.text2);
            int childrenCount = adapter.getChildrenCount(position);
            if (text2 != null && ((ExpandableGroupDescriptorAdapter) adapter).childCursorLoaded(position))
            {
                Resources res = view.getContext().getResources();

                text2.setText(res.getQuantityString(R.plurals.number_of_tasks, childrenCount, childrenCount));
            }

            View removeSearch = view.findViewById(R.id.quick_add_task);
            if (removeSearch != null)
            {
                ((ImageView) removeSearch).setImageResource(R.drawable.content_remove);
                removeSearch.setOnClickListener(removeListener);
                GroupTag tag = (GroupTag) removeSearch.getTag();
                Long groupId = cursor.getLong(cursor.getColumnIndex(SearchHistoryColumns._ID));
                if (tag == null || tag.groupId != groupId)
                {
                    removeSearch.setTag(new GroupTag(groupTitle, groupId));
                }
            }

            if ((flags & FLAG_IS_EXPANDED) != 0)
            {
                if (removeSearch != null)
                {
                    removeSearch.setVisibility(View.VISIBLE);
                }
                if (text2 != null)
                {
                    text2.setVisibility(View.GONE);
                }
            }
            else
            {
                if (removeSearch != null)
                {
                    removeSearch.setVisibility(View.GONE);
                }
                if (text2 != null)
                {
                    text2.setVisibility(View.VISIBLE);
                }
            }

            // TODO: swap styles instead of modifying the font style
            boolean isHistoric = cursor.getInt(cursor.getColumnIndex(SearchHistoryColumns.HISTORIC)) > 0;
            Typeface oldtypeface = title.getTypeface();
            title.setTypeface(oldtypeface, swapStyle(isHistoric, oldtypeface));

            // set history icon
            ImageView icon = (ImageView) view.findViewById(android.R.id.icon);
            icon.setImageResource(R.drawable.ic_history);
            icon.setVisibility(isHistoric ? View.VISIBLE : View.INVISIBLE);
        }


        @SuppressLint("WrongConstant")
        private int swapStyle(boolean isHistoric, Typeface oldtypeface)
        {
            return isHistoric ? oldtypeface.getStyle() & ~Typeface.ITALIC : oldtypeface.getStyle() | Typeface.ITALIC;
        }


        private final OnClickListener removeListener = new OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                GroupTag tag = (GroupTag) v.getTag();
                if (tag != null)
                {
                    Context context = v.getContext();
                    mHelper.removeSearch(tag.groupId);
                    mSearchCursorFactory.forceUpdate();
                    Toast.makeText(context, context.getString(R.string.toast_x_removed, tag.groupName), Toast.LENGTH_SHORT).show();
                }
            }
        };


        /**
         * A tag that holds information about a search group.
         */
        final class GroupTag
        {
            final String groupName;
            final long groupId;


            GroupTag(String groupName, long groupId)
            {
                this.groupName = groupName;
                this.groupId = groupId;
            }
        }


        @Override
        public int getView()
        {
            return R.layout.task_list_group;
        }


        /**
         * Return the title of the priority group.
         *
         * @param cursor
         *         A {@link Cursor} pointing to the current group.
         *
         * @return A {@link String} with the group name.
         */
        private String getTitle(Cursor cursor, Context context)
        {
            return cursor.getString(cursor.getColumnIndex(SearchHistoryDatabaseHelper.SearchHistoryColumns.SEARCH_QUERY));
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

    private final SearchHistoryHelper mHelper;
    private final SearchHistoryCursorLoaderFactory mSearchCursorFactory;


    public BySearch(String authority, SearchHistoryHelper helper)
    {
        super(authority);
        mHelper = helper;
        mSearchCursorFactory = new SearchHistoryCursorLoaderFactory(mHelper);
    }


    @Override
    public ExpandableChildDescriptor makeExpandableChildDescriptor(String authority)
    {
        return new SearchChildDescriptor(authority, SearchHistoryDatabaseHelper.SearchHistoryColumns.SEARCH_QUERY, INSTANCE_PROJECTION, null, Tasks.SCORE
                + ", " + Instances.INSTANCE_DUE_SORTING + " is null, " + Instances.INSTANCE_DUE_SORTING + ", " + Instances.PRIORITY + ", " + Instances.TITLE
                + " COLLATE NOCASE ASC", null).setViewDescriptor(TASK_VIEW_DESCRIPTOR);

    }


    @Override
    public ExpandableGroupDescriptor makeExpandableGroupDescriptor(String authority)
    {
        return new ExpandableGroupDescriptor(mSearchCursorFactory, makeExpandableChildDescriptor(authority)).setViewDescriptor(GROUP_VIEW_DESCRIPTOR);
    }


    @Override
    public int getId()
    {
        return R.id.task_group_search;
    }
}
