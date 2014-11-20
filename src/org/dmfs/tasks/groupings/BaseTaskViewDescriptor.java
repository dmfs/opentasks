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
import org.dmfs.tasks.utils.DateFormatter;
import org.dmfs.tasks.utils.DateFormatter.DateFormatContext;
import org.dmfs.tasks.utils.ViewDescriptor;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.support.v4.util.SparseArrayCompat;
import android.text.TextUtils;
import android.text.format.Time;
import android.view.View;
import android.widget.FrameLayout.LayoutParams;
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
		if (view != null && dueDate != null)
		{
			Time now = mNow;
			if (now == null)
			{
				now = mNow = new Time();
			}
			if (!now.timezone.equals(TimeZone.getDefault().getID()))
			{
				now.clear(TimeZone.getDefault().getID());
			}

			if (Math.abs(now.toMillis(false) - System.currentTimeMillis()) > 5000)
			{
				now.setToNow();
				now.normalize(true);
			}

			dueDate.normalize(true);

			view.setText(new DateFormatter(view.getContext()).format(dueDate, now, DateFormatContext.LIST_VIEW));
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
		View overlayTop = getView(view, R.id.overlay_top);
		View overlayBottom = getView(view, R.id.overlay_bottom);

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
		TextView descriptionView = (TextView) getView(view, android.R.id.text1);
		if (TextUtils.isEmpty(description))
		{
			descriptionView.setVisibility(View.GONE);
		}
		else
		{
			descriptionView.setVisibility(View.VISIBLE);
			if (description.length() > 150)
			{
				description = description.substring(0, 150);
			}
			descriptionView.setText(description);
		}
	}


	protected void setColorBar(View view, Cursor cursor)
	{
		View colorbar = getView(view, R.id.colorbar);
		if (colorbar != null)
		{
			colorbar.setBackgroundColor(TaskFieldAdapters.LIST_COLOR.get(cursor));
		}
	}


	@SuppressLint("NewApi")
	protected void resetFlingView(View view)
	{
		View flingContentView = getView(view, getFlingContentViewId());
		if (flingContentView == null)
		{
			flingContentView = view;
		}

		if (android.os.Build.VERSION.SDK_INT >= 14)
		{
			if (flingContentView.getTranslationX() != 0)
			{
				flingContentView.setTranslationX(0);
				flingContentView.setAlpha(1);
			}
		}
		else
		{
			LayoutParams layoutParams = (LayoutParams) flingContentView.getLayoutParams();
			if (layoutParams.leftMargin != 0 || layoutParams.rightMargin != 0)
			{
				layoutParams.setMargins(0, layoutParams.topMargin, 0, layoutParams.bottomMargin);
				flingContentView.setLayoutParams(layoutParams);
			}
		}
	}


	protected <T extends View> T getView(View view, int viewId)
	{
		SparseArrayCompat<View> viewHolder = (SparseArrayCompat<View>) view.getTag();
		if (viewHolder == null)
		{
			viewHolder = new SparseArrayCompat<View>();
			view.setTag(viewHolder);
		}
		View res = viewHolder.get(viewId);
		if (res == null)
		{
			res = view.findViewById(viewId);
			viewHolder.put(viewId, res);
		}
		return (T) res;
	}

}
