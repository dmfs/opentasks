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
import java.util.TimeZone;

import org.dmfs.provider.tasks.TaskContract.Tasks;
import org.dmfs.tasks.R;
import org.dmfs.tasks.groupings.cursorloaders.SearchHistoryCursorLoaderFactory;
import org.dmfs.tasks.model.TaskFieldAdapters;
import org.dmfs.tasks.model.adapters.TimeFieldAdapter;
import org.dmfs.tasks.utils.DueDateFormatter;
import org.dmfs.tasks.utils.ExpandableChildDescriptor;
import org.dmfs.tasks.utils.ExpandableGroupDescriptor;
import org.dmfs.tasks.utils.ExpandableGroupDescriptorAdapter;
import org.dmfs.tasks.utils.SearchChildDescriptor;
import org.dmfs.tasks.utils.SearchHistoryDatabaseHelper;
import org.dmfs.tasks.utils.SearchHistoryDatabaseHelper.SearchHistoryColumns;
import org.dmfs.tasks.utils.SearchHistoryHelper;
import org.dmfs.tasks.utils.ViewDescriptor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build.VERSION;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.view.View;
import android.widget.BaseExpandableListAdapter;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
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
		Tasks.LIST_COLOR, Tasks.PRIORITY, Tasks.LIST_ID, Tasks._ID, Tasks.STATUS, Tasks.COMPLETED, Tasks.IS_CLOSED, Tasks.PERCENT_COMPLETE, Tasks.SCORE };

	/**
	 * An adapter to load the due date from the tasks projection.
	 */
	public final static TimeFieldAdapter TASK_DUE_ADAPTER = new TimeFieldAdapter(Tasks.DUE, Tasks.TZ, Tasks.IS_ALLDAY);

	/**
	 * A {@link ViewDescriptor} that knows how to present the tasks in the task list grouped by priority.
	 */
	public final ViewDescriptor TASK_VIEW_DESCRIPTOR = new ViewDescriptor()
	{
		/**
		 * We use this to get the current time.
		 */
		private Time mNow;

		private int mFlingContentViewId = R.id.flingContentView;
		private int mFlingRevealLeftViewId = R.id.fling_reveal_left;
		private int mFlingRevealRightViewId = R.id.fling_reveal_right;


		@SuppressLint("NewApi")
		@Override
		public void populateView(View view, Cursor cursor, BaseExpandableListAdapter adapter, int flags)
		{
			TextView title = (TextView) view.findViewById(android.R.id.title);
			boolean isClosed = TaskFieldAdapters.IS_CLOSED.get(cursor);

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

					dueDateField.setText(new DueDateFormatter(view.getContext()).format(dueDate));

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
				colorbar.setBackgroundColor(TaskFieldAdapters.LIST_COLOR.get(cursor));
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
			Context context = view.getContext();
			long now = System.currentTimeMillis();
			int position = cursor.getPosition();

			// set list title
			TextView title = (TextView) view.findViewById(android.R.id.title);
			if (title != null)
			{
				title.setText(getTitle(cursor, view.getContext()));

			}
			// set search time
			TextView text1 = (TextView) view.findViewById(android.R.id.text1);
			if (text1 != null)
			{
				text1.setText(DateUtils.formatSameDayTime(cursor.getLong(cursor.getColumnIndex(SearchHistoryDatabaseHelper.SearchHistoryColumns.TIMESTAMP)),
					now, DateFormat.SHORT, DateFormat.SHORT));
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

			if (colorbar1 != null)
			{
				colorbar1.setVisibility(View.GONE);
			}
			if (colorbar2 != null)
			{
				colorbar2.setVisibility(View.GONE);
			}

			boolean isHistoric = cursor.getInt(cursor.getColumnIndex(SearchHistoryColumns.HISTORIC)) > 0;
			title.setTypeface(null, isHistoric ? Typeface.NORMAL : Typeface.ITALIC);

			// set history icon
			ImageView icon = (ImageView) view.findViewById(android.R.id.icon);
			if (isHistoric)
			{
				icon.setImageResource(R.drawable.ic_history);
				icon.setVisibility(View.VISIBLE);
			}
			else
			{
				icon.setVisibility(View.GONE);
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
		 *            A {@link Cursor} pointing to the current group.
		 * @return A {@link String} with the group name.
		 */
		private String getTitle(Cursor cursor, Context context)
		{
			return "\"" + cursor.getString(cursor.getColumnIndex(SearchHistoryDatabaseHelper.SearchHistoryColumns.SEARCH_QUERY)) + "\"";
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


	public BySearch(String authority, SearchHistoryHelper helper)
	{
		super(authority);
		mHelper = helper;
	}


	@Override
	public ExpandableChildDescriptor makeExpandableChildDescriptor(String authority)
	{
		return new SearchChildDescriptor(authority, SearchHistoryDatabaseHelper.SearchHistoryColumns.SEARCH_QUERY, TASK_PROJECTION, null,
			Tasks.DEFAULT_SORT_ORDER, null).setViewDescriptor(TASK_VIEW_DESCRIPTOR);

	}


	@Override
	public ExpandableGroupDescriptor makeExpandableGroupDescriptor(String authority)
	{
		return new ExpandableGroupDescriptor(new SearchHistoryCursorLoaderFactory(mHelper), makeExpandableChildDescriptor(authority))
			.setViewDescriptor(GROUP_VIEW_DESCRIPTOR);
	}


	@Override
	public int getId()
	{
		return R.id.task_group_search;
	}
}
