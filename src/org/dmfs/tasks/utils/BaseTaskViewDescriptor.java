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

package org.dmfs.tasks.utils;

import java.util.TimeZone;

import org.dmfs.tasks.R;

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
}
