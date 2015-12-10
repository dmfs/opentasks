/*
 * Copyright (C) 2013 Marten Gajda <marten@dmfs.org>
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
 * 
 */

package org.dmfs.tasks.groupings;

import java.text.DateFormatSymbols;

import org.dmfs.provider.tasks.TaskContract.Instances;
import org.dmfs.tasks.R;
import org.dmfs.tasks.groupings.cursorloaders.TimeRangeCursorFactory;
import org.dmfs.tasks.groupings.cursorloaders.TimeRangeCursorLoaderFactory;
import org.dmfs.tasks.groupings.cursorloaders.TimeRangeShortCursorFactory;
import org.dmfs.tasks.model.TaskFieldAdapters;
import org.dmfs.tasks.utils.ExpandableChildDescriptor;
import org.dmfs.tasks.utils.ExpandableGroupDescriptor;
import org.dmfs.tasks.utils.ExpandableGroupDescriptorAdapter;
import org.dmfs.tasks.utils.ViewDescriptor;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Paint;
import android.os.Build;
import android.os.Build.VERSION;
import android.view.View;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;


/**
 * Definition of the by-due grouping.
 * 
 * <p>
 * TODO: refactor!
 * </p>
 * <p>
 * TODO: refactor!
 * </p>
 * <p>
 * TODO: also, don't forget to refactor!
 * </p>
 * 
 * The plan is to provide some kind of GroupingDescriptior that provides the {@link ExpandableGroupDescriptorAdapter}, a name and a set of filters. Also it
 * should take care of persisting and restoring the open groups, selected filters ...
 * 
 * @author Marten Gajda <marten@dmfs.org>
 * @author Tobias Reinsch <tobias@dmfs.org>
 */
public class ByDueDate extends AbstractGroupingFactory
{

	/**
	 * A {@link ViewDescriptor} that knows how to present the tasks in the task list.
	 */
	public final ViewDescriptor TASK_VIEW_DESCRIPTOR = new BaseTaskViewDescriptor()
	{

		private int mFlingContentViewId = R.id.flingContentView;
		private int mFlingRevealLeftViewId = R.id.fling_reveal_left;
		private int mFlingRevealRightViewId = R.id.fling_reveal_right;


		@TargetApi(Build.VERSION_CODES.HONEYCOMB)
		@Override
		public void populateView(View view, Cursor cursor, BaseExpandableListAdapter adapter, int flags)
		{
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

			setDueDate((TextView) getView(view, R.id.task_due_date), null, INSTANCE_DUE_ADAPTER.get(cursor), isClosed);

			View divider = getView(view, R.id.divider);
			if (divider != null)
			{
				divider.setVisibility((flags & FLAG_IS_LAST_CHILD) != 0 ? View.GONE : View.VISIBLE);
			}

			// display priority
			int priority = TaskFieldAdapters.PRIORITY.get(cursor);
			View priorityView = getView(view, R.id.task_priority_view_medium);
			priorityView.setBackgroundResource(android.R.color.transparent);
			priorityView.setVisibility(View.VISIBLE);

			if (priority > 0 && priority < 5)
			{
				priorityView.setBackgroundResource(R.color.priority_red);
			}
			if (priority == 5)
			{
				priorityView.setBackgroundResource(R.color.priority_yellow);
			}
			if (priority > 5 && priority <= 9)
			{
				priorityView.setBackgroundResource(R.color.priority_green);
			}
			if (VERSION.SDK_INT >= 11)
			{
				// update percentage background
				View background = getView(view, R.id.percentage_background_view);
				background.setPivotX(0);
				Integer percentComplete = TaskFieldAdapters.PERCENT_COMPLETE.get(cursor);
				if (percentComplete < 100)
				{
					background.setScaleX(percentComplete == null ? 0 : percentComplete / 100f);
					background.setBackgroundResource(R.drawable.task_progress_background_shade);
				}
				else
				{
					background.setScaleX(1);
					background.setBackgroundResource(R.drawable.complete_task_background_overlay);
				}
			}

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
	 * A {@link ViewDescriptor} that knows how to present due date groups.
	 */
	public final ViewDescriptor GROUP_VIEW_DESCRIPTOR = new ViewDescriptor()
	{
		// DateFormatSymbols.getInstance() not used because it is not available before API level 9
		private final String[] mMonthNames = new DateFormatSymbols().getMonths();


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

			// show/hide divider
			View divider = view.findViewById(R.id.divider);
			if (divider != null)
			{
				divider.setVisibility((flags & FLAG_IS_EXPANDED) != 0 && childrenCount > 0 ? View.VISIBLE : View.GONE);
			}

			View colorbar = view.findViewById(R.id.colorbar1);
			if (colorbar != null)
			{
				colorbar.setVisibility(View.GONE);
			}
		}


		@Override
		public int getView()
		{
			return R.layout.task_list_group_single_line;
		}


		/**
		 * Return the title of a due date group.
		 * 
		 * @param cursor
		 *            A {@link Cursor} pointing to the current group.
		 * @return A {@link String} with the group name.
		 */
		private String getTitle(Cursor cursor, Context context)
		{
			int type = cursor.getInt(cursor.getColumnIndex(TimeRangeCursorFactory.RANGE_TYPE));
			if (type == 0)
			{
				return context.getString(R.string.task_group_no_due);
			}
			if ((type & TimeRangeCursorFactory.TYPE_END_OF_TODAY) == TimeRangeCursorFactory.TYPE_END_OF_TODAY)
			{
				return context.getString(R.string.task_group_due_today);
			}
			if ((type & TimeRangeCursorFactory.TYPE_END_OF_YESTERDAY) == TimeRangeCursorFactory.TYPE_END_OF_YESTERDAY)
			{
				return context.getString(R.string.task_group_overdue);
			}
			if ((type & TimeRangeCursorFactory.TYPE_END_OF_TOMORROW) == TimeRangeCursorFactory.TYPE_END_OF_TOMORROW)
			{
				return context.getString(R.string.task_group_due_tomorrow);
			}
			if ((type & TimeRangeCursorFactory.TYPE_END_IN_7_DAYS) == TimeRangeCursorFactory.TYPE_END_IN_7_DAYS)
			{
				return context.getString(R.string.task_group_due_within_7_days);
			}
			if ((type & TimeRangeCursorFactory.TYPE_END_OF_A_MONTH) != 0)
			{
				return context.getString(R.string.task_group_due_in_month,
					mMonthNames[cursor.getInt(cursor.getColumnIndex(TimeRangeCursorFactory.RANGE_MONTH))]);
			}
			if ((type & TimeRangeCursorFactory.TYPE_END_OF_A_YEAR) != 0)
			{
				return context.getString(R.string.task_group_due_in_year, cursor.getInt(cursor.getColumnIndex(TimeRangeCursorFactory.RANGE_YEAR)));
			}
			if ((type & TimeRangeCursorFactory.TYPE_NO_END) != 0)
			{
				return context.getString(R.string.task_group_due_in_future);
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
			// TODO Auto-generated method stub
			return 0;
		}


		@Override
		public int getFlingRevealRightViewId()
		{
			// TODO Auto-generated method stub
			return 0;
		}

	};


	public ByDueDate(String authority)
	{
		super(authority);
	}


	@Override
	ExpandableChildDescriptor makeExpandableChildDescriptor(String authority)
	{
		return new ExpandableChildDescriptor(Instances.getContentUri(authority), INSTANCE_PROJECTION, Instances.VISIBLE + "=1 and (" + Instances.IS_ALLDAY
			+ "=0 and (((" + Instances.INSTANCE_DUE + ">=?) and (" + Instances.INSTANCE_DUE + "<?)) or ((" + Instances.INSTANCE_DUE + ">=? or "
			+ Instances.INSTANCE_DUE + " is ?) and ? is null))" + "or " + Instances.IS_ALLDAY + "=1 and (((" + Instances.INSTANCE_DUE + ">=?+?) and ("
			+ Instances.INSTANCE_DUE + "<?+?)) or ((" + Instances.INSTANCE_DUE + ">=?+? or " + Instances.INSTANCE_DUE + " is ?) and ? is null)))",
			Instances.DEFAULT_SORT_ORDER, 0, 1, 0, 1, 1, 0, 9, 1, 10, 0, 9, 1, 1).setViewDescriptor(TASK_VIEW_DESCRIPTOR);
	}


	@Override
	ExpandableGroupDescriptor makeExpandableGroupDescriptor(String authority)
	{
		return new ExpandableGroupDescriptor(new TimeRangeCursorLoaderFactory(TimeRangeShortCursorFactory.DEFAULT_PROJECTION),
			makeExpandableChildDescriptor(authority)).setViewDescriptor(GROUP_VIEW_DESCRIPTOR);
	}


	@Override
	public int getId()
	{
		return R.id.task_group_by_due;
	}

}
