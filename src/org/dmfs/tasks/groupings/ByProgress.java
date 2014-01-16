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
import org.dmfs.tasks.groupings.cursorloaders.ProgressCursorFactory;
import org.dmfs.tasks.groupings.cursorloaders.ProgressCursorLoaderFactory;
import org.dmfs.tasks.utils.ExpandableChildDescriptor;
import org.dmfs.tasks.utils.ExpandableGroupDescriptor;
import org.dmfs.tasks.utils.ExpandableGroupDescriptorAdapter;
import org.dmfs.tasks.utils.ViewDescriptor;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Paint;
import android.text.format.Time;
import android.view.View;
import android.widget.BaseExpandableListAdapter;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;


/**
 * Definition of the by-progress grouping.
 * 
 * @author Tobias Reinsch <tobias@dmfs.org>
 */
@TargetApi(11)
public interface ByProgress
{
	/**
	 * A {@link ViewDescriptor} that knows how to present the tasks in the task list grouped by progress.
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

			TextView dueDateField = (TextView) view.findViewById(R.id.task_due_date);
			if (dueDateField != null)
			{
				Time dueDate = Common.DUE_ADAPTER.get(cursor);

				if (dueDate != null)
				{
					if (mNow == null)
					{
						mNow = new Time();
					}
					mNow.clear(TimeZone.getDefault().getID());
					mNow.setToNow();

					dueDateField.setText(makeDueDate(dueDate, view.getContext()));

					// highlight overdue dates & times
					if (dueDate.before(mNow) && !isClosed)
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
					dueDateField.setText("");
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
			int priority = cursor.getInt(cursor.getColumnIndex(Instances.PRIORITY));
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
		}


		@Override
		public int getView()
		{
			return R.layout.task_list_element;
		}


		/**
		 * Get the due date to show. It returns just a time for tasks that are due today and a date otherwise.
		 * 
		 * @param due
		 *            The due date to format.
		 * @return A String with the formatted date.
		 */
		private String makeDueDate(Time due, Context context)
		{
			if (!due.allDay)
			{
				due.switchTimezone(TimeZone.getDefault().getID());
			}

			// normalize time to ensure yearDay is set properly
			due.normalize(false);

			if (due.year == mNow.year && due.yearDay == mNow.yearDay)
			{
				if (due.allDay)
				{
					return context.getString(R.string.today);
				}
				else
				{
					return context.getString(R.string.today) + ", " + mTimeFormatter.format(new Date(due.toMillis(false)));
				}
			}
			else
			{
				return mDateFormatter.format(new Date(due.toMillis(false)));
			}
		}


		@Override
		public int getFlingContentViewId()
		{
			return mFlingContentViewId;
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

			View colorbar1 = view.findViewById(R.id.colorbar1);
			View colorbar2 = view.findViewById(R.id.colorbar2);

			if ((flags & FLAG_IS_EXPANDED) != 0)
			{
				if (colorbar1 != null)
				{
					colorbar1.setBackgroundColor(cursor.getInt(2));
					colorbar1.setVisibility(View.GONE);
				}
				if (colorbar2 != null)
				{
					colorbar2.setVisibility(View.GONE);
				}
			}
			else
			{
				if (colorbar1 != null)
				{
					colorbar1.setVisibility(View.GONE);
				}
				if (colorbar2 != null)
				{
					colorbar2.setBackgroundColor(cursor.getInt(2));
					colorbar2.setVisibility(View.GONE);
				}
			}
		}


		@Override
		public int getView()
		{
			return R.layout.task_list_group_single_line;
		}


		/**
		 * Return the title of the priority group.
		 * 
		 * @param cursor
		 *            A {@link Cursor} pointing to the current group.
		 * @return A {@link String} with the group name.
		 */
		private String getTitle(Cursor cursor, Context context)
		{
			return context.getString(cursor.getInt(cursor.getColumnIndex(ProgressCursorFactory.PROGRESS_TITLE_RES_ID)));
		}


		@Override
		public int getFlingContentViewId()
		{
			return -1;
		}

	};

	/**
	 * A descriptor that knows how to load elements in a list group ordered by due date.
	 */
	public final static ExpandableChildDescriptor CHILD_DESCRIPTOR = new ExpandableChildDescriptor(Instances.CONTENT_URI, Common.INSTANCE_PROJECTION,
		Instances.VISIBLE + "=1 and (" + Instances.PERCENT_COMPLETE + ">=? and " + Instances.PERCENT_COMPLETE + " <= ? or " + Instances.PERCENT_COMPLETE
			+ " is ?)", Instances.INSTANCE_DUE + " is null, " + Instances.INSTANCE_DUE + ", " + Instances.TITLE, 1, 2, 1)
		.setViewDescriptor(TASK_VIEW_DESCRIPTOR);

	/**
	 * A descriptor for the "grouped by priority" view.
	 */
	public final static ExpandableGroupDescriptor GROUP_DESCRIPTOR = new ExpandableGroupDescriptor(new ProgressCursorLoaderFactory(
		ProgressCursorFactory.DEFAULT_PROJECTION), CHILD_DESCRIPTOR).setViewDescriptor(GROUP_VIEW_DESCRIPTOR);

}
