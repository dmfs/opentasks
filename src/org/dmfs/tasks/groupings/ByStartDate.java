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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.dmfs.provider.tasks.TaskContract.Instances;
import org.dmfs.tasks.R;
import org.dmfs.tasks.groupings.cursorloaders.TimeRangeCursorFactory;
import org.dmfs.tasks.groupings.cursorloaders.TimeRangeStartCursorFactory;
import org.dmfs.tasks.groupings.cursorloaders.TimeRangeStartCursorLoaderFactory;
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
import android.text.format.Time;
import android.view.View;
import android.widget.BaseExpandableListAdapter;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * Definition of the by-start date grouping.
 * 
 * 
 * 
 * @author Tobias Reinsch <tobias@dmfs.org>
 */
public class ByStartDate extends AbstractGroupingFactory
{

	/**
	 * A {@link ViewDescriptor} that knows how to present the tasks in the task list.
	 */
	public final ViewDescriptor TASK_VIEW_DESCRIPTOR = new ViewDescriptor()
	{
		/**
		 * We use this to get the current time.
		 */
		private Time mNow;

		/**
		 * The formatter we use for due dates other than today.
		 */
		private final DateFormat mDateFormatter = DateFormat.getDateInstance(SimpleDateFormat.MEDIUM);

		/**
		 * The formatter we use for tasks that are due today.
		 */
		private final DateFormat mTimeFormatter = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT);

		private int mFlingContentViewId = R.id.flingContentView;
		private int mFlingRevealLeftViewId = R.id.fling_reveal_left;
		private int mFlingRevealRightViewId = R.id.fling_reveal_right;


		@TargetApi(Build.VERSION_CODES.HONEYCOMB)
		@Override
		public void populateView(View view, Cursor cursor, BaseExpandableListAdapter adapter, int flags)
		{
			TextView title = (TextView) view.findViewById(android.R.id.title);
			boolean isClosed = cursor.getInt(13) > 0;

			// get the view inside that was flinged if the view has an integrated fling content view
			View flingContentView = (View) view.findViewById(mFlingContentViewId);
			if (flingContentView == null)
			{
				flingContentView = view;
			}

			if (android.os.Build.VERSION.SDK_INT >= 14)
			{
				flingContentView.setTranslationX(0);
				flingContentView.setAlpha(1);
			}
			else
			{
				LayoutParams layoutParams = (LayoutParams) flingContentView.getLayoutParams();
				layoutParams.setMargins(0, layoutParams.topMargin, 0, layoutParams.bottomMargin);
				flingContentView.setLayoutParams(layoutParams);
			}
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
			TextView startDateField = (TextView) view.findViewById(R.id.task_start_date);
			if (startDateField != null)
			{
				Time startDate = INSTANCE_START_ADAPTER.get(cursor);

				if (startDate != null)
				{
					if (mNow == null)
					{
						mNow = new Time();
					}
					mNow.clear(TimeZone.getDefault().getID());
					mNow.setToNow();

					startDateField.setVisibility(View.VISIBLE);
					startDateField.setText(makeDateString(startDate));

					// format time
					startDateField.setTextAppearance(view.getContext(), R.style.task_list_due_text);

					ImageView icon = (ImageView) view.findViewById(R.id.task_start_image);
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

			TextView dueDateField = (TextView) view.findViewById(R.id.task_due_date);
			ImageView dueIcon = (ImageView) view.findViewById(R.id.task_due_image);
			if (dueDateField != null)
			{
				Time dueTime = INSTANCE_DUE_ADAPTER.get(cursor);

				if (dueTime != null)
				{
					if (mNow == null)
					{
						mNow = new Time();
					}
					mNow.clear(TimeZone.getDefault().getID());
					mNow.setToNow();
					dueDateField.setVisibility(View.VISIBLE);
					dueDateField.setText(makeDateString(dueTime));
					if (dueIcon != null)
					{
						dueIcon.setVisibility(View.VISIBLE);
					}

					// highlight overdue dates & times
					if (dueTime.before(mNow) && !isClosed)
					{
						dueDateField.setTextAppearance(view.getContext(), R.style.task_list_overdue_text);
					}
					else
					{
						dueDateField.setTextAppearance(view.getContext(), R.style.task_list_due_text);
					}
				}
				else
				{
					dueDateField.setVisibility(View.GONE);
					dueIcon.setVisibility(View.GONE);

				}
			}

			View colorbar = view.findViewById(R.id.colorbar);
			if (colorbar != null)
			{
				colorbar.setBackgroundColor(cursor.getInt(6));
			}

			View divider = view.findViewById(R.id.divider);
			if (divider != null)
			{
				divider.setVisibility((flags & FLAG_IS_LAST_CHILD) != 0 ? View.GONE : View.VISIBLE);
			}

			// display priority
			int priority = TaskFieldAdapters.PRIORITY.get(cursor);
			View priorityView = view.findViewById(R.id.task_priority_view_medium);
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
				View background = view.findViewById(R.id.percentage_background_view);
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
		}


		@Override
		public int getView()
		{
			return R.layout.task_list_element;
		}


		/**
		 * Get the date to show. It returns just a time for dates that are today and a date otherwise.
		 * 
		 * @param date
		 *            The date to format.
		 * @return A String with the formatted date.
		 */
		private String makeDateString(Time date)
		{
			if (!date.allDay)
			{
				date.switchTimezone(TimeZone.getDefault().getID());
			}

			if (date.year == mNow.year && date.yearDay == mNow.yearDay)
			{
				return mTimeFormatter.format(new Date(date.toMillis(false)));
			}
			else
			{
				return mDateFormatter.format(new Date(date.toMillis(false)));
			}
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
		 * Return the title of a date group.
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
		return new ExpandableChildDescriptor(Instances.getContentUri(authority), INSTANCE_PROJECTION, Instances.VISIBLE + "=1 and ((("
			+ Instances.INSTANCE_START + ">=?) and (" + Instances.INSTANCE_START + "<?)) or ((" + Instances.INSTANCE_START + ">=? or "
			+ Instances.INSTANCE_START + " is ?) and ? is null))", Instances.DEFAULT_SORT_ORDER, 0, 1, 0, 1, 1).setViewDescriptor(TASK_VIEW_DESCRIPTOR);
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
