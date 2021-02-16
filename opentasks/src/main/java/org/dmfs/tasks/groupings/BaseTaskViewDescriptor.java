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
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.format.Time;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;

import org.dmfs.android.bolts.color.colors.AttributeColor;
import org.dmfs.iterables.decorators.Sieved;
import org.dmfs.jems.single.elementary.Reduced;
import org.dmfs.provider.tasks.model.TaskAdapter;
import org.dmfs.tasks.R;
import org.dmfs.tasks.model.DescriptionItem;
import org.dmfs.tasks.model.TaskFieldAdapters;
import org.dmfs.tasks.utils.DateFormatter;
import org.dmfs.tasks.utils.DateFormatter.DateFormatContext;
import org.dmfs.tasks.utils.ViewDescriptor;

import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.collection.SparseArrayCompat;
import androidx.core.content.ContextCompat;

import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
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

    private final static Pattern CHECKED_PATTERN = Pattern.compile("(-\\s*)?\\[[xX]]");
    private final static Pattern UNCHECKED_PATTERN = Pattern.compile("(-\\s*)?\\[\\s?]");
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
        boolean isClosed = TaskAdapter.IS_CLOSED.getFrom(cursor);
        TextView descriptionView = getView(view, android.R.id.text1);

        List<DescriptionItem> checkList = TaskFieldAdapters.DESCRIPTION_CHECKLIST.get(cursor);
        if (checkList.size() > 0 && !checkList.get(0).checkbox && !isClosed)
        {
            String description = checkList.get(0).text;
            descriptionView.setVisibility(View.VISIBLE);
            descriptionView.setText(description);
        }
        else
        {
            descriptionView.setVisibility(View.GONE);
        }

        TextView checkboxItemCountView = getView(view, R.id.checkbox_item_count);
        Iterable<DescriptionItem> checkedItems = new Sieved<>(item -> item.checkbox, checkList);
        int checkboxItemCount = new Reduced<DescriptionItem, Integer>(() -> 0, (count, ignored) -> count + 1, checkedItems).value();
        if (checkboxItemCount == 0 || isClosed)
        {
            checkboxItemCountView.setVisibility(View.GONE);
        }
        else
        {
            checkboxItemCountView.setVisibility(View.VISIBLE);
            int checked = new Reduced<DescriptionItem, Integer>(() -> 0, (count, ignored) -> count + 1,
                    new Sieved<>(item -> item.checked, checkedItems)).value();
            if (checked == 0)
            {
                checkboxItemCountView.setText(
                        withCheckBoxes(checkboxItemCountView,
                                view.getContext().getString(R.string.opentasks_checkbox_item_count_none_checked, checkboxItemCount)));
            }
            else if (checked == checkboxItemCount)
            {
                checkboxItemCountView.setText(
                        withCheckBoxes(checkboxItemCountView,
                                view.getContext().getString(R.string.opentasks_checkbox_item_count_all_checked, checkboxItemCount)));
            }
            else
            {
                checkboxItemCountView.setText(withCheckBoxes(checkboxItemCountView,
                        view.getContext().getString(R.string.opentasks_checkbox_item_count_partially_checked, checkboxItemCount - checked, checked)));
            }
        }
    }


    private Spannable withCheckBoxes(
            @NonNull TextView view,
            @NonNull String s)
    {
        return withDrawable(
                view,
                withDrawable(
                        view,
                        new SpannableString(s),
                        CHECKED_PATTERN,
                        R.drawable.ic_outline_check_box_24),
                UNCHECKED_PATTERN,
                R.drawable.ic_outline_check_box_outline_blank_24);
    }


    private Spannable withDrawable(
            @NonNull TextView view,
            @NonNull Spannable s,
            @NonNull Pattern pattern,
            @DrawableRes int drawable)
    {
        Context context = view.getContext();
        Matcher matcher = pattern.matcher(s.toString());
        while (matcher.find())
        {
            Drawable drawable1 = ContextCompat.getDrawable(context, drawable);
            int lineHeight = view.getLineHeight();
            int additionalSpace = (int) ((lineHeight - view.getTextSize()) / 2);
            drawable1.setBounds(0, 0, lineHeight + additionalSpace, lineHeight + additionalSpace);
            drawable1.setTint(view.getCurrentTextColor());
            s.setSpan(new ImageSpan(drawable1, DynamicDrawableSpan.ALIGN_BOTTOM), matcher.start(), matcher.end(), SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return s;
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
