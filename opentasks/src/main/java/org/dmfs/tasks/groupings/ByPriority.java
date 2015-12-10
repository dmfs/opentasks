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

import org.dmfs.provider.tasks.TaskContract;
import org.dmfs.provider.tasks.TaskContract.Instances;
import org.dmfs.provider.tasks.TaskContract.Tasks;
import org.dmfs.tasks.QuickAddDialogFragment;
import org.dmfs.tasks.R;
import org.dmfs.tasks.groupings.cursorloaders.PriorityCursorFactory;
import org.dmfs.tasks.groupings.cursorloaders.PriorityCursorLoaderFactory;
import org.dmfs.tasks.model.ContentSet;
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
import android.os.Build.VERSION;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;


/**
 * Definition of the by-priority grouping.
 * 
 * @author Tobias Reinsch <tobias@dmfs.org>
 */
@TargetApi(11)
public class ByPriority extends AbstractGroupingFactory
{

	/**
	 * A {@link ViewDescriptor} that knows how to present the tasks in the task list grouped by priority.
	 */
	public final ViewDescriptor TASK_VIEW_DESCRIPTOR = new BaseTaskViewDescriptor()
	{

		private int mFlingContentViewId = R.id.flingContentView;
		private int mFlingRevealLeftViewId = R.id.fling_reveal_left;
		private int mFlingRevealRightViewId = R.id.fling_reveal_right;


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
			View quickAddTask = view.findViewById(R.id.quick_add_task);
			if (quickAddTask != null)
			{
				quickAddTask.setOnClickListener(quickAddClickListener);
				quickAddTask.setTag(cursor.getInt(2 /* max priority of this section */));
			}

			if ((flags & FLAG_IS_EXPANDED) != 0)
			{
				if (colorbar1 != null)
				{
					colorbar1.setBackgroundColor(cursor.getInt(2));
					colorbar1.setVisibility(View.VISIBLE);
				}
				if (colorbar2 != null)
				{
					colorbar2.setVisibility(View.GONE);
				}

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
				if (colorbar1 != null)
				{
					colorbar1.setVisibility(View.INVISIBLE);
				}
				if (colorbar2 != null)
				{
					colorbar2.setBackgroundColor(cursor.getInt(2));
					colorbar2.setVisibility(View.VISIBLE);
				}

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
				Integer tag = (Integer) v.getTag();
				if (tag != null)
				{
					ContentSet content = new ContentSet(Tasks.getContentUri(TaskContract.taskAuthority(v.getContext())));
					TaskFieldAdapters.PRIORITY.set(content, tag);
					QuickAddDialogFragment.newInstance(content).show(((FragmentActivity) v.getContext()).getSupportFragmentManager(), null);
				}
			}
		};


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
			return context.getString(cursor.getInt(cursor.getColumnIndex(PriorityCursorFactory.PRIORITY_TITLE_RES_ID)));
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


	public ByPriority(String authority)
	{
		super(authority);
	}


	@Override
	ExpandableChildDescriptor makeExpandableChildDescriptor(String authority)
	{
		return new ExpandableChildDescriptor(Instances.getContentUri(authority), INSTANCE_PROJECTION, Instances.VISIBLE + "=1 and (" + Instances.PRIORITY
			+ ">=? and " + Instances.PRIORITY + " <= ? or ? is null and " + Instances.PRIORITY + " <= ? or " + Instances.PRIORITY + " is ?)",
			Instances.INSTANCE_DUE_SORTING + " is null, " + Instances.INSTANCE_DUE_SORTING + ", " + Instances.TITLE + " COLLATE NOCASE ASC", 1, 2, 1, 2, 1)
			.setViewDescriptor(TASK_VIEW_DESCRIPTOR);
	}


	@Override
	ExpandableGroupDescriptor makeExpandableGroupDescriptor(String authority)
	{
		return new ExpandableGroupDescriptor(new PriorityCursorLoaderFactory(PriorityCursorFactory.DEFAULT_PROJECTION),
			makeExpandableChildDescriptor(authority)).setViewDescriptor(GROUP_VIEW_DESCRIPTOR);
	}


	@Override
	public int getId()
	{
		return R.id.task_group_by_priority;
	}

}
