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
import android.content.SharedPreferences;
import android.content.res.Resources;
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
import androidx.preference.PreferenceManager;

import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
import static org.dmfs.tasks.model.TaskFieldAdapters.IS_CLOSED;
import static org.dmfs.tasks.model.TaskFieldAdapters.LIST_COLOR_RAW;


/**
 * A base implementation of a {@link ViewDescriptor}. It has a number of commonly used methods.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public abstract class BaseTaskViewDescriptor implements ViewDescriptor
{

    private final static int[] DRAWABLES = new int[] { R.drawable.ic_outline_check_box_24, R.drawable.ic_outline_check_box_outline_blank_24 };
    private final static Pattern DRAWABLE_PATTERN = Pattern.compile("((?:-\\s*)?\\[[xX]])|((?:-\\s*)?\\[\\s?])");
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
        Context context = view.getContext();
        Resources res = context.getResources();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isClosed = TaskAdapter.IS_CLOSED.getFrom(cursor);
        TextView descriptionView = getView(view, android.R.id.text1);
        int maxDescriptionLines = prefs.getInt(context.getString(R.string.opentasks_pref_appearance_list_description_lines),
                context.getResources().getInteger(R.integer.opentasks_preferences_description_lines_default));

        List<DescriptionItem> checkList = TaskFieldAdapters.DESCRIPTION_CHECKLIST.get(cursor);
        if (maxDescriptionLines > 0 && checkList.size() > 0 && !checkList.get(0).checkbox && !isClosed)
        {
            descriptionView.setVisibility(View.VISIBLE);
            descriptionView.setText(withCheckBoxes(descriptionView, checkList.get(0).text));
            descriptionView.setMaxLines(maxDescriptionLines);
        }
        else
        {
            descriptionView.setVisibility(View.GONE);
        }

        boolean showCheckListSummary = prefs.getBoolean(
                context.getString(R.string.opentasks_pref_appearance_check_list_summary),
                res.getBoolean(R.bool.opentasks_list_check_list_summary_default));
        TextView checkboxItemCountView = getView(view, R.id.checkbox_item_count);
        Iterable<DescriptionItem> checkedItems = new Sieved<>(item -> item.checkbox, checkList);
        int checkboxItemCount = new Reduced<DescriptionItem, Integer>(() -> 0, (count, ignored) -> count + 1, checkedItems).value();
        if (checkboxItemCount == 0 || isClosed || !showCheckListSummary)
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
                                context.getString(R.string.opentasks_checkbox_item_count_none_checked, checkboxItemCount)));
            }
            else if (checked == checkboxItemCount)
            {
                checkboxItemCountView.setText(
                        withCheckBoxes(checkboxItemCountView,
                                context.getString(R.string.opentasks_checkbox_item_count_all_checked, checkboxItemCount)));
            }
            else
            {
                checkboxItemCountView.setText(withCheckBoxes(checkboxItemCountView,
                        context.getString(R.string.opentasks_checkbox_item_count_partially_checked, checkboxItemCount - checked, checked)));
            }
        }

        View progressGradient = view.findViewById(R.id.task_progress_background);
        if (!isClosed && TaskFieldAdapters.PERCENT_COMPLETE.get(cursor) > 0
                && prefs.getBoolean(context.getString(R.string.opentasks_pref_appearance_progress_gradient),
                res.getBoolean(R.bool.opentasks_list_progress_gradient_default)))
        {
            progressGradient.setVisibility(View.VISIBLE);
            progressGradient.setPivotX(0);
            progressGradient.setScaleX(TaskFieldAdapters.PERCENT_COMPLETE.get(cursor) / 100f);
        }
        else
        {
            progressGradient.setVisibility(View.GONE);
        }
    }


    private Spannable withCheckBoxes(
            @NonNull TextView view,
            @NonNull String s)
    {
        return withDrawable(
                view,
                new SpannableString(s),
                DRAWABLE_PATTERN,
                DRAWABLES);
    }


    private Spannable withDrawable(
            @NonNull TextView view,
            @NonNull Spannable s,
            @NonNull Pattern pattern,
            @DrawableRes int[] drawable)
    {
        Context context = view.getContext();
        Matcher matcher = pattern.matcher(s.toString());
        while (matcher.find())
        {
            int idx = matcher.group(1) == null ? 1 : 0;
            Drawable drawable1 = ContextCompat.getDrawable(context, drawable[idx]);
            int lineHeight = view.getLineHeight();
            int additionalSpace = (int) ((lineHeight - view.getTextSize()) / 2);
            drawable1.setBounds(0, 0, lineHeight + additionalSpace, lineHeight + additionalSpace);
            drawable1.setTint(view.getCurrentTextColor());
            s.setSpan(new ImageSpan(drawable1, DynamicDrawableSpan.ALIGN_BOTTOM), matcher.start(), matcher.end(), SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return s;
    }


    protected void setPrio(SharedPreferences prefs, View view, Cursor cursor)
    {
        // display priority
        View prioLabel = getView(view, R.id.priority_label);
        prioLabel.setAlpha(IS_CLOSED.get(cursor) ? 0.4f : 1f);
        int priority = TaskFieldAdapters.PRIORITY.get(cursor);
        if (priority > 0 &&
                prefs.getBoolean(prioLabel.getContext().getString(R.string.opentasks_pref_appearance_list_show_priority), true))
        {
            if (priority < 5)
            {
                prioLabel.setBackgroundColor(new AttributeColor(prioLabel.getContext(), R.attr.colorHighPriority).argb());
            }
            if (priority == 5)
            {
                prioLabel.setBackgroundColor(new AttributeColor(prioLabel.getContext(), R.attr.colorMediumPriority).argb());
            }
            if (priority > 5)
            {
                prioLabel.setBackgroundColor(new AttributeColor(prioLabel.getContext(), R.attr.colorLowPriority).argb());
            }
            prioLabel.setVisibility(View.VISIBLE);
        }
        else
        {
            prioLabel.setVisibility(View.GONE);
        }
    }


    protected void setColorBar(View view, Cursor cursor)
    {
        MaterialCardView cardView = getView(view, R.id.flingContentView);
        if (cardView != null)
        {
            boolean isClosed = IS_CLOSED.get(cursor);
            cardView.findViewById(R.id.color_label).setBackgroundColor(LIST_COLOR_RAW.get(cursor));
            cardView.findViewById(R.id.card_background).setVisibility(isClosed ? View.VISIBLE : View.GONE);
            cardView.findViewById(R.id.color_label).setAlpha(isClosed ? 0.4f : 1f);
            cardView.setCardElevation(view.getResources().getDimensionPixelSize(
                    isClosed ?
                            R.dimen.opentasks_tasklist_card_elevation_closed :
                            R.dimen.opentasks_tasklist_card_elevation));
            ((TextView) cardView.findViewById(android.R.id.title))
                    .setTextColor(new AttributeColor(view.getContext(),
                            isClosed ?
                                    android.R.attr.textColorTertiary :
                                    android.R.attr.textColorPrimary).argb());
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
