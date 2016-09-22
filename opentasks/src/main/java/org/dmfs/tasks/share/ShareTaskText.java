/*
 * Copyright 2016 Marten Gajda <marten@dmfs.org>
 *
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

package org.dmfs.tasks.share;

import android.content.Context;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.text.format.Time;

import org.dmfs.provider.tasks.TaskContract;
import org.dmfs.tasks.R;
import org.dmfs.tasks.model.CheckListItem;
import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.Model;
import org.dmfs.tasks.model.TaskFieldAdapters;
import org.dmfs.tasks.model.adapters.TimeZoneWrapper;
import org.dmfs.tasks.utils.AbstractStringCharSequence;
import org.dmfs.tasks.utils.DateFormatter;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

/*
 <task title>
 ============

 <task description>
 [X] checked list item
 [ ] unchecked list item

 Location: <location>
 Start: <start date time> <timezone>
 Due: <due date time> <timezone>
 Completed: <due date time> <timezone>
 Priority: <priority text>
 Privacy: <privacy text>
 Status: <status text>
 <url>

 --
 Shared by OpenTasks
 */


/**
 * The text description for the whole task to share with other apps. (See the format above.)
 *
 * @author Gabor Keszthelyi
 */
public class ShareTaskText extends AbstractStringCharSequence implements CharSequence
{
    private static final String NEW_LINE = "\n";


    public ShareTaskText(ContentSet contentSet, Model model, Context context)
    {
        // TODO Use a Factory here when the corresponding classes are available from java tools library
        super(create(contentSet, model, context));
    }


    private static String create(ContentSet contentSet, Model model, Context context)
    {
        StringBuilder sb = new StringBuilder();

        appendTitle(sb, contentSet);
        sb.append(NEW_LINE);

        boolean appendedDesc = appendDescription(sb, contentSet);
        boolean appendedItems = appendChecklistItems(sb, contentSet);
        if (appendedDesc || appendedItems)
        {
            sb.append(NEW_LINE);
        }

        appendLocation(sb, contentSet, context);
        appendTimes(sb, contentSet, context);
        appendPriority(sb, contentSet, model, context);
        appendPrivacy(sb, contentSet, model, context);
        appendStatus(sb, contentSet, model, context);
        appendUrl(sb, contentSet);
        sb.append(NEW_LINE);

        appendFooter(sb, context);

        return sb.toString();

    }


    private static void appendTitle(StringBuilder sb, ContentSet contentSet)
    {
        String title = TaskFieldAdapters.TITLE.get(contentSet);
        if (title != null)
        {
            sb.append(title).append(NEW_LINE);

            // try to create about the same length of underline as the title ('=' char is wider than average char width):
            char[] underlineChars = new char[(int) (title.length() * 0.85)];
            Arrays.fill(underlineChars, '=');
            sb.append(new String(underlineChars)).append(NEW_LINE);
        }

    }


    private static boolean appendDescription(StringBuilder sb, ContentSet contentSet)
    {
        String description = TaskFieldAdapters.DESCRIPTION.get(contentSet);
        if (!TextUtils.isEmpty(description))
        {
            sb.append(description).append(NEW_LINE);
            return true;
        }
        return false;
    }


    private static boolean appendChecklistItems(StringBuilder sb, ContentSet contentSet)
    {
        boolean appended = false;
        List<CheckListItem> checkListItems = TaskFieldAdapters.CHECKLIST.get(contentSet);
        if (checkListItems != null)
        {
            for (CheckListItem item : checkListItems)
            {
                if (!TextUtils.isEmpty(item.text))
                {
                    sb.append('[').append(item.checked ? 'x' : ' ').append("] ").append(item.text).append(NEW_LINE);
                    appended = true;
                }
            }
        }
        return appended;
    }


    private static void appendLocation(StringBuilder sb, ContentSet contentSet, Context context)
    {
        String location = TaskFieldAdapters.LOCATION.get(contentSet);
        if (!TextUtils.isEmpty(location))
        {
            appendProperty(sb, R.string.task_location, location, context);
        }
    }


    private static void appendTimes(StringBuilder sb, ContentSet contentSet, Context context)
    {
        TimeZoneWrapper timeZoneW = getTimeZoneWrapper(contentSet);
        appendTime(sb, R.string.task_start, TaskFieldAdapters.DTSTART.get(contentSet), timeZoneW, context);
        appendTime(sb, R.string.task_due, TaskFieldAdapters.DUE.get(contentSet), timeZoneW, context);
        appendTime(sb, R.string.task_completed, TaskFieldAdapters.COMPLETED.get(contentSet), timeZoneW, context);
    }


    private static TimeZoneWrapper getTimeZoneWrapper(ContentSet contentSet)
    {
        TimeZone timeZone = TaskFieldAdapters.TIMEZONE.get(contentSet);
        return timeZone != null ? new TimeZoneWrapper(timeZone) : null;
    }


    private static void appendTime(StringBuilder sb, @StringRes int nameResId, Time time, TimeZoneWrapper timeZone, Context context)
    {
        if (time != null)
        {
            appendProperty(sb, nameResId, formatTime(time, timeZone, context), context);
        }
    }


    private static String formatTime(Time time, TimeZoneWrapper timeZone, Context context)
    {
        String dateTimeText = new DateFormatter(context).format(time, DateFormatter.DateFormatContext.DETAILS_VIEW);
        if (timeZone == null)
        {
            return dateTimeText;
        }
        String timeZoneText = timeZone.getDisplayName(timeZone.inDaylightTime(time.toMillis(false)),
                TimeZone.SHORT);
        return dateTimeText + " " + timeZoneText;
    }


    private static void appendPriority(StringBuilder sb, ContentSet contentSet, Model model, Context context)
    {
        Integer priorityValue = TaskFieldAdapters.PRIORITY.get(contentSet);
        if (priorityValue != null)
        {
            String priorityText = model.getField(R.id.task_field_priority).getChoices().getTitle(priorityValue);
            appendProperty(sb, R.string.task_priority, priorityText, context);
        }
    }


    private static void appendPrivacy(StringBuilder sb, ContentSet contentSet, Model model, Context context)
    {
        Integer classificationValue = TaskFieldAdapters.CLASSIFICATION.get(contentSet);
        if (classificationValue != null)
        {
            String classificationText = model.getField(R.id.task_field_classification)
                    .getChoices()
                    .getTitle(classificationValue);
            appendProperty(sb, R.string.task_classification, classificationText, context);
        }
    }


    private static void appendStatus(StringBuilder sb, ContentSet contentSet, Model model, Context context)
    {
        Integer statusValue = TaskFieldAdapters.STATUS.get(contentSet);
        if (statusValue != null && !statusValue.equals(TaskContract.Tasks.STATUS_COMPLETED))
        {
            String statusText = model.getField(R.id.task_field_status).getChoices().getTitle(statusValue);
            appendProperty(sb, R.string.task_status, statusText, context);
        }
    }


    private static void appendUrl(StringBuilder sb, ContentSet contentSet)
    {
        URL url = TaskFieldAdapters.URL.get(contentSet);
        if (url != null)
        {
            sb.append(url).append(NEW_LINE);
        }
    }


    private static void appendFooter(StringBuilder sb, Context context)
    {
        sb.append("--").append(NEW_LINE);
        sb.append(context.getString(R.string.opentasks_share_footer));
    }


    private static void appendProperty(StringBuilder sb, @StringRes int nameResId, String value, Context context)
    {
        sb.append(context.getString(nameResId)).append(": ").append(value).append(NEW_LINE);
    }
}

