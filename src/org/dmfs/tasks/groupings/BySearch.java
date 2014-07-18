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

import org.dmfs.provider.tasks.TaskContract.Tasks;
import org.dmfs.tasks.R;
import org.dmfs.tasks.groupings.cursorloaders.SearchHistoryCursorFactory;
import org.dmfs.tasks.groupings.cursorloaders.SearchHistoryCursorLoaderFactory;
import org.dmfs.tasks.model.TaskFieldAdapters;
import org.dmfs.tasks.model.adapters.TimeFieldAdapter;
import org.dmfs.tasks.utils.ExpandableChildDescriptor;
import org.dmfs.tasks.utils.ExpandableGroupDescriptor;
import org.dmfs.tasks.utils.SearchChildDescriptor;
import org.dmfs.tasks.utils.ViewDescriptor;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build.VERSION;
import android.text.format.Time;
import android.view.View;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;


/**
 * Definition of the search history grouping.
 * 
 * @author Tobias Reinsch <tobias@dmfs.org>
 */
public class BySearch extends AbstractGroupingFactory
{

	/**
	 * The projection we use when we load instances. We don't need every detail of a task here. This is used by all groupings.
	 */
	public final static String[] TASK_PROJECTION = new String[] { Tasks.DTSTART, Tasks.DURATION, Tasks.DUE, Tasks.IS_ALLDAY, Tasks.TZ, Tasks.TITLE,
		Tasks.LIST_COLOR, Tasks.PRIORITY, Tasks.LIST_ID, Tasks._ID, Tasks.STATUS, Tasks.COMPLETED, Tasks.IS_CLOSED, Tasks.PERCENT_COMPLETE };

	/**
	 * An adapter to load the due date from the tasks projection.
	 */
	public final static TimeFieldAdapter TASK_DUE_ADAPTER = new TimeFieldAdapter(Tasks.DUE, Tasks.TZ, Tasks.IS_ALLDAY);

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

		private int mFlingContentViewId = -1;


		@SuppressLint("NewApi")
		@Override
		public void populateView(View view, Cursor cursor, BaseExpandableListAdapter adapter, int flags)
		{
			TextView title = (TextView) view.findViewById(android.R.id.title);
			if (title != null)
			{
				String text = cursor.getString(cursor.getColumnIndex(Tasks.TITLE));
				title.setText(text);
			}

			Integer status = cursor.getInt(cursor.getColumnIndex(Tasks.STATUS));

			TextView dueDateField = (TextView) view.findViewById(R.id.task_due_date);
			if (dueDateField != null)
			{
				Time dueDate = TASK_DUE_ADAPTER.get(cursor);

				if (dueDate != null)
				{
					if (mNow == null)
					{
						mNow = new Time();
					}
					mNow.clear(TimeZone.getDefault().getID());
					mNow.setToNow();

					if (status == null || status < Tasks.STATUS_COMPLETED)
					{

						dueDateField.setText(makeDueDate(dueDate));

						// highlight overdue dates & times
						if (dueDate.before(mNow))
						{
							dueDateField.setTextColor(Color.RED);
						}
						else
						{
							dueDateField.setTextColor(Color.argb(255, 0x80, 0x80, 0x80));
						}
					}
					else
					{
						Long completed = cursor.getLong(cursor.getColumnIndex(Tasks.COMPLETED));
						if (completed != null)
						{
							Time complTime = new Time(Time.TIMEZONE_UTC);
							complTime.set(completed);

							dueDateField.setText(makeDueDate(complTime));

							// highlight if the task has been completed after the due date
							if (dueDate.after(complTime))
							{
								dueDateField.setTextColor(Color.RED);
							}
							else
							{
								dueDateField.setTextColor(Color.argb(255, 0x80, 0x80, 0x80));
							}
						}
						else
						{
							// TODO: what do we show then there is no completed date?
						}
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
				colorbar.setBackgroundColor(cursor.getColumnIndex(Tasks.LIST_COLOR));
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
		 * Get the due date to show. It returns just a time for tasks that are due today and a date otherwise.
		 * 
		 * @param due
		 *            The due date to format.
		 * @return A String with the formatted date.
		 */
		private String makeDueDate(Time due)
		{
			if (!due.allDay)
			{
				due.switchTimezone(TimeZone.getDefault().getID());
			}

			if (due.year == mNow.year && due.yearDay == mNow.yearDay)
			{
				return mTimeFormatter.format(new Date(due.toMillis(false)));
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

	/**
	 * A {@link ViewDescriptor} that knows how to present due date groups.
	 */
	public final ViewDescriptor GROUP_VIEW_DESCRIPTOR = new ViewDescriptor()
	{

		@Override
		public void populateView(View view, Cursor cursor, BaseExpandableListAdapter adapter, int flags)
		{
			TextView title = (TextView) view.findViewById(android.R.id.title);
			if (title != null)
			{
				title.setText(cursor.getString(cursor.getColumnIndex(SearchHistoryCursorFactory.SEARCH_TEXT)));
			}
		}


		@Override
		public int getView()
		{
			return R.layout.task_list_group;
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


	public BySearch(String authority)
	{
		super(authority);
	}


	@Override
	public ExpandableChildDescriptor makeExpandableChildDescriptor(String authority)
	{
		return new SearchChildDescriptor(authority, SearchHistoryCursorFactory.SEARCH_TEXT, TASK_PROJECTION, null, Tasks.DEFAULT_SORT_ORDER, null)
			.setViewDescriptor(TASK_VIEW_DESCRIPTOR);

	}


	@Override
	public ExpandableGroupDescriptor makeExpandableGroupDescriptor(String authority)
	{
		return new ExpandableGroupDescriptor(new SearchHistoryCursorLoaderFactory(SearchHistoryCursorFactory.DEFAULT_PROJECTION),
			makeExpandableChildDescriptor(authority)).setViewDescriptor(GROUP_VIEW_DESCRIPTOR);
	}


	@Override
	public int getTitle()
	{
		return R.string.task_group_search_title;
	}
}
