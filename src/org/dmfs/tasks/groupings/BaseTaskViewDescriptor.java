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

import java.util.TimeZone;

import org.dmfs.tasks.R;
import org.dmfs.tasks.model.TaskFieldAdapters;
import org.dmfs.tasks.utils.DueDateFormatter;
import org.dmfs.tasks.utils.ViewDescriptor;

import android.database.Cursor;
import android.text.TextUtils;
import android.text.format.Time;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * A base implementation of a {@link ViewDescriptor}. It has a number of commonly used methods.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public abstract class BaseTaskViewDescriptor implements ViewDescriptor
{

	/**
	 * We use this to get the current time.
	 */
	protected Time mNow;


	protected void setDueDate(TextView view, ImageView dueIcon, Time dueDate, boolean isClosed)
	{
		Time now = mNow;
		if (now == null)
		{
			now = mNow = new Time();
		}
		now.clear(TimeZone.getDefault().getID());
		now.setToNow();
		now.normalize(true);

		if (view != null && dueDate != null)
		{
			dueDate.normalize(true);

			view.setText(new DueDateFormatter(view.getContext()).format(dueDate));
			if (dueIcon != null)
			{
				dueIcon.setVisibility(View.VISIBLE);
			}

			// highlight overdue dates & times, handle allDay tasks separately
			if ((!dueDate.allDay && dueDate.before(now) || dueDate.allDay
				&& (dueDate.year < now.year || dueDate.yearDay <= now.yearDay && dueDate.year == now.year))
				&& !isClosed)
			{
				view.setTextAppearance(view.getContext(), R.style.task_list_overdue_text);
			}
			else
			{
				view.setTextAppearance(view.getContext(), R.style.task_list_due_text);
			}
		}
		else if (view != null)
		{
			view.setText("");
			if (dueIcon != null)
			{
				dueIcon.setVisibility(View.GONE);
			}
		}
	}


	protected void setOverlay(View view, int position, int count)
	{
		View overlayTop = view.findViewById(R.id.overlay_top);
		View overlayBottom = view.findViewById(R.id.overlay_bottom);

		if (overlayTop != null)
		{
			overlayTop.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
		}

		if (overlayBottom != null)
		{
			overlayBottom.setVisibility(position == count - 1 ? View.VISIBLE : View.GONE);
		}
	}


	protected void setDescription(View view, Cursor cursor)
	{
		String description = TaskFieldAdapters.DESCRIPTION.get(cursor);
		TextView descriptionView = (TextView) view.findViewById(android.R.id.text1);
		if (TextUtils.isEmpty(description))
		{
			descriptionView.setVisibility(View.GONE);
		}
		else
		{
			description = description.replaceAll("\\[\\s?\\]", " ").replaceAll("\\[[xX]\\]", "✓");
			descriptionView.setVisibility(View.VISIBLE);
			descriptionView.setText(description);
		}
	}


	protected void setColorBar(View view, Cursor cursor)
	{
		View colorbar = view.findViewById(R.id.colorbar);
		if (colorbar != null)
		{
			colorbar.setBackgroundColor(TaskFieldAdapters.LIST_COLOR.get(cursor));
		}
	}
}
