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
import android.text.format.Time;
import android.view.View;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.dmfs.tasks.R;
import org.dmfs.tasks.contract.TaskContract.Instances;
import org.dmfs.tasks.groupings.cursorloaders.TimeRangeCursorFactory;
import org.dmfs.tasks.groupings.cursorloaders.TimeRangeStartCursorFactory;
import org.dmfs.tasks.groupings.cursorloaders.TimeRangeStartCursorLoaderFactory;
import org.dmfs.tasks.utils.DateFormatter;
import org.dmfs.tasks.utils.DateFormatter.DateFormatContext;
import org.dmfs.tasks.utils.ExpandableChildDescriptor;
import org.dmfs.tasks.utils.ExpandableGroupDescriptor;
import org.dmfs.tasks.utils.ExpandableGroupDescriptorAdapter;
import org.dmfs.tasks.utils.ViewDescriptor;

import androidx.preference.PreferenceManager;


/**
 * Definition of the by-start date grouping.
 *
 * @author Tobias Reinsch <tobias@dmfs.org>
 */
public class ByStartDate extends AbstractGroupingFactory
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

            setDueDate(getView(view, R.id.task_due_date), getView(view, R.id.task_due_image), INSTANCE_DUE_ADAPTER.get(cursor),
                    isClosed);

            TextView startDateField = getView(view, R.id.task_start_date);
            if (startDateField != null)
            {
                Time startDate = INSTANCE_START_ADAPTER.get(cursor);

                if (startDate != null)
                {

                    startDateField.setVisibility(View.VISIBLE);
                    startDateField.setText(new DateFormatter(view.getContext()).format(startDate, DateFormatContext.LIST_VIEW));

                    // format time
                    startDateField.setTextAppearance(view.getContext(), R.style.task_list_due_text);

                    ImageView icon = getView(view, R.id.task_start_image);
                    if (icon != null)
                    {
                        icon.setVisibility(View.VISIBLE);
                    }
                }
                else
                {
                    startDateField.setText("");
                }
            }

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
     * A {@link ViewDescriptor} that knows how to present start date groups.
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

            // set list elements
            TextView text2 = (TextView) view.findViewById(android.R.id.text2);
            int childrenCount = adapter.getChildrenCount(position);
            if (text2 != null && ((ExpandableGroupDescriptorAdapter) adapter).childCursorLoaded(position))
            {
                Resources res = view.getContext().getResources();
                text2.setText(res.getQuantityString(R.plurals.number_of_tasks, childrenCount, childrenCount));

            }
        }


        @Override
        public int getView()
        {
            return R.layout.task_list_group_single_line;
        }


        /**
         * Return the title of a date group.
         *
         * @param cursor
         *         A {@link Cursor} pointing to the current group.
         *
         * @return A {@link String} with the group name.
         */
        private String getTitle(Cursor cursor, Context context)
        {
            int type = cursor.getInt(cursor.getColumnIndex(TimeRangeCursorFactory.RANGE_TYPE));
            if (type == 0)
            {
                return context.getString(R.string.task_group_start_started);
            }
            if ((type & TimeRangeCursorFactory.TYPE_OVERDUE) == TimeRangeCursorFactory.TYPE_OVERDUE)
            {
                return context.getString(R.string.task_group_start_started);
            }
            if ((type & TimeRangeCursorFactory.TYPE_END_OF_TODAY) == TimeRangeCursorFactory.TYPE_END_OF_TODAY)
            {
                return context.getString(R.string.task_group_start_today);
            }
            if ((type & TimeRangeCursorFactory.TYPE_END_OF_TOMORROW) == TimeRangeCursorFactory.TYPE_END_OF_TOMORROW)
            {
                return context.getString(R.string.task_group_start_tomorrow);
            }
            if ((type & TimeRangeCursorFactory.TYPE_END_IN_7_DAYS) == TimeRangeCursorFactory.TYPE_END_IN_7_DAYS)
            {
                return context.getString(R.string.task_group_start_within_7_days);
            }
            if ((type & TimeRangeCursorFactory.TYPE_NO_END) != 0)
            {
                return context.getString(R.string.task_group_start_in_future);
            }
            return "";
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


    public ByStartDate(String authority)
    {
        super(authority);
    }


    @Override
    ExpandableChildDescriptor makeExpandableChildDescriptor(String authority)
    {
        // Note that we're using INSTANCE_START_SORTING to get correct grouping of all-day tasks
        return new ExpandableChildDescriptor(Instances.getContentUri(authority), INSTANCE_PROJECTION, Instances.VISIBLE + "=1 and (" + Instances.IS_ALLDAY
                + "=0 and (((" + Instances.INSTANCE_START + ">=?) and (" + Instances.INSTANCE_START + "<?)) or ((" + Instances.INSTANCE_START + ">=? or "
                + Instances.INSTANCE_START + " is ?) and ? is null)) or " + Instances.IS_ALLDAY + "=1 and (((" + Instances.INSTANCE_START + ">=?+?) and ("
                + Instances.INSTANCE_START + "<?+?)) or ((" + Instances.INSTANCE_START + ">=?+? or " + Instances.INSTANCE_START + " is ?) and ? is null)))",
                Instances.INSTANCE_START, 0, 1, 0, 1, 1, 0, 9, 1, 10, 0, 9, 1, 1).setViewDescriptor(TASK_VIEW_DESCRIPTOR);
    }


    @Override
    ExpandableGroupDescriptor makeExpandableGroupDescriptor(String authority)
    {
        return new ExpandableGroupDescriptor(new TimeRangeStartCursorLoaderFactory(TimeRangeStartCursorFactory.DEFAULT_PROJECTION),
                makeExpandableChildDescriptor(authority)).setViewDescriptor(GROUP_VIEW_DESCRIPTOR);
    }


    @Override
    public int getId()
    {
        return R.id.task_group_by_start;
    }

}
