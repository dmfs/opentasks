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
import android.text.TextUtils;
import android.text.format.Time;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;

import org.dmfs.android.bolts.color.colors.AttributeColor;
import org.dmfs.tasks.R;
import org.dmfs.tasks.model.TaskFieldAdapters;
import org.dmfs.tasks.utils.DateFormatter;
import org.dmfs.tasks.utils.DateFormatter.DateFormatContext;
import org.dmfs.tasks.utils.ViewDescriptor;

import java.util.TimeZone;

import androidx.collection.SparseArrayCompat;

import static java.lang.Boolean.TRUE;
import static org.dmfs.tasks.contract.TaskContract.TaskColumns.STATUS_CANCELLED;
import static org.dmfs.tasks.model.TaskFieldAdapters.IS_CLOSED;
import static org.dmfs.tasks.model.TaskFieldAdapters.STATUS;


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
            else if (isClosed)
            {
                view.setTextAppearance(view.getContext(), R.style.task_list_due_text_closed);
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
    }


    protected void setDescription(View view, Cursor cursor)
    {
        String description = TaskFieldAdapters.DESCRIPTION.get(cursor);
        TextView descriptionView = getView(view, android.R.id.text1);
        View content = getView(view, R.id.cardcontent);
        if (TextUtils.isEmpty(description) || TRUE.equals(IS_CLOSED.get(cursor)))
        {
            content.setVisibility(View.GONE);
        }
        else
        {
            content.setVisibility(View.VISIBLE);
            if (description.length() > 150)
            {
                description = description.substring(0, 150);
            }
            descriptionView.setText(description);
        }
    }


    protected void setColorBar(View view, Cursor cursor)
    {
        MaterialCardView cardView = getView(view, R.id.flingContentView);
        if (cardView != null)
        {
            if (IS_CLOSED.get(cursor))
            {
                if (STATUS.get(cursor) == STATUS_CANCELLED)
                {
                    cardView.setCardBackgroundColor(0xfff0f0f0);
                    cardView.setStrokeColor(0);
                    ((TextView) cardView.findViewById(android.R.id.title)).setTextColor(0x50000000);
                    cardView.setStrokeWidth(view.getResources().getDimensionPixelSize(R.dimen.opentasks_cardlist_open_border_width));

                }
                else
                {
                    cardView.setCardBackgroundColor(new AttributeColor(view.getContext(), android.R.attr.windowBackground).argb());
                    //cardView.setCardElevation(1f);
                    ((TextView) cardView.findViewById(android.R.id.title)).setTextColor(0x80000000);
                    cardView.setStrokeColor(0xffc0c0c0);
                    cardView.setStrokeWidth(view.getResources().getDimensionPixelSize(R.dimen.opentasks_cardlist_closed_border_width));
                }
                cardView.setCardElevation(view.getResources().getDimensionPixelSize(R.dimen.opentasks_cardlist_closed_elevation));
            }
            else
            {
                cardView.setCardBackgroundColor(TaskFieldAdapters.LIST_COLOR.get(cursor));
                cardView.setStrokeColor(0);
                ((TextView) cardView.findViewById(android.R.id.title)).setTextColor(0xffffffff);
                cardView.setStrokeWidth(view.getResources().getDimensionPixelSize(R.dimen.opentasks_cardlist_open_border_width));
                cardView.setCardElevation(view.getResources().getDimensionPixelSize(R.dimen.opentasks_cardlist_open_elevation));
            }
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
