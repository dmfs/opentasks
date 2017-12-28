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

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.support.v4.util.SparseArrayCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.dmfs.rfc5545.DateTime;
import org.dmfs.tasks.R;
import org.dmfs.tasks.model.TaskFieldAdapters;
import org.dmfs.tasks.utils.DateFormatter;
import org.dmfs.tasks.utils.DateFormatter.DateFormatContext;
import org.dmfs.tasks.utils.ViewDescriptor;


/**
 * A base implementation of a {@link ViewDescriptor}. It has a number of commonly used methods.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public abstract class BaseTaskViewDescriptor implements ViewDescriptor
{

    // TODO Add Nullable, NonNull annotations
    protected void setDueDate(TextView view, ImageView dueIcon, DateTime dueDate, boolean isClosed)
    {
        if (view == null)
        {
            // TODO can this happen?
            return;
        }

        if (dueDate != null)
        {
            // TODO cache nowAndHere of some time (it was 5 secs before)
            DateTime nowAndHere = DateTime.nowAndHere();

            view.setText(new DateFormatter(view.getContext()).format(dueDate, nowAndHere, DateFormatContext.LIST_VIEW));
            if (dueIcon != null)
            {
                dueIcon.setVisibility(View.VISIBLE);
            }

            // overdue tasks are highlighted
            int textStyle = dueDate.after(nowAndHere) && !isClosed ? R.style.task_list_overdue_text : R.style.task_list_due_text;
            view.setTextAppearance(view.getContext(), textStyle);

        }
        else
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

        if (flingContentView.getTranslationX() != 0)
        {
            flingContentView.setTranslationX(0);
            flingContentView.setAlpha(1);
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
