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
import org.dmfs.tasks.utils.TaskText;

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
public class ShareTaskText extends AbstractStringCharSequence implements TaskText
{

    public ShareTaskText(ContentSet contentSet, Model model, Context context)
    {
        super(new Composer(contentSet, model, context).create());
    }


    private static class Composer
    {
        private static final String NEW_LINE = "\n";

        private final ContentSet mContentSet;
        private final Model mModel;
        private final Context mContext;
        private final DateFormatter mDateFormatter;


        public Composer(ContentSet contentSet, Model model, Context context)
        {
            mContentSet = contentSet;
            mModel = model;
            mContext = context;
            mDateFormatter = new DateFormatter(mContext);
        }


        public String create()
        {
            StringBuilder sb = new StringBuilder();

            appendTitle(sb);
            sb.append(NEW_LINE);

            boolean appendedDesc = appendDescription(sb);
            boolean appendedItems = appendChecklistItems(sb);
            if (appendedDesc || appendedItems)
            {
                sb.append(NEW_LINE);
            }

            appendLocation(sb);
            appendTimes(sb);
            appendPriority(sb);
            appendPrivacy(sb);
            appendStatus(sb);
            appendUrl(sb);
            sb.append(NEW_LINE);

            appendFooter(sb);

            return sb.toString();

        }


        private void appendTitle(StringBuilder sb)
        {
            String title = TaskFieldAdapters.TITLE.get(mContentSet);
            if (title != null)
            {
                sb.append(title).append(NEW_LINE);

                // try to create about the same length of underline as the title ('=' char is wider than average char width):
                char[] underlineChars = new char[(int) (title.length() * 0.85)];
                Arrays.fill(underlineChars, '=');
                sb.append(new String(underlineChars)).append(NEW_LINE);
            }

        }


        private boolean appendDescription(StringBuilder sb)
        {
            String description = TaskFieldAdapters.DESCRIPTION.get(mContentSet);
            if (!TextUtils.isEmpty(description))
            {
                sb.append(description).append(NEW_LINE);
                return true;
            }
            return false;
        }


        private boolean appendChecklistItems(StringBuilder sb)
        {
            boolean appended = false;
            List<CheckListItem> checkListItems = TaskFieldAdapters.CHECKLIST.get(mContentSet);
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


        private void appendLocation(StringBuilder sb)
        {
            String location = TaskFieldAdapters.LOCATION.get(mContentSet);
            if (!TextUtils.isEmpty(location))
            {
                appendProperty(sb, R.string.task_location, location);
            }
        }


        private void appendTimes(StringBuilder sb)
        {
            TimeZoneWrapper timeZoneW = getTimeZoneWrapper(mContentSet);
            appendTime(sb, R.string.task_start, TaskFieldAdapters.DTSTART.get(mContentSet), timeZoneW);
            appendTime(sb, R.string.task_due, TaskFieldAdapters.DUE.get(mContentSet), timeZoneW);
            appendTime(sb, R.string.task_completed, TaskFieldAdapters.COMPLETED.get(mContentSet), timeZoneW);
        }


        private TimeZoneWrapper getTimeZoneWrapper(ContentSet contentSet)
        {
            TimeZone timeZone = TaskFieldAdapters.TIMEZONE.get(contentSet);
            return timeZone != null ? new TimeZoneWrapper(timeZone) : null;
        }


        private void appendTime(StringBuilder sb, @StringRes int nameResId, Time time, TimeZoneWrapper timeZone)
        {
            if (time != null)
            {
                appendProperty(sb, nameResId, formatTime(time, timeZone));
            }
        }


        private String formatTime(Time time, TimeZoneWrapper timeZone)
        {
            String dateTimeText = mDateFormatter.format(time, DateFormatter.DateFormatContext.DETAILS_VIEW);
            if (timeZone == null)
            {
                return dateTimeText;
            }
            String timeZoneText = timeZone.getDisplayName(timeZone.inDaylightTime(time.toMillis(false)),
                    TimeZone.SHORT);
            return dateTimeText + " " + timeZoneText;
        }


        private void appendPriority(StringBuilder sb)
        {
            Integer priorityValue = TaskFieldAdapters.PRIORITY.get(mContentSet);
            if (priorityValue != null)
            {
                String priorityText = mModel.getField(R.id.task_field_priority).getChoices().getTitle(priorityValue);
                appendProperty(sb, R.string.task_priority, priorityText);
            }
        }


        private void appendPrivacy(StringBuilder sb)
        {
            Integer classificationValue = TaskFieldAdapters.CLASSIFICATION.get(mContentSet);
            if (classificationValue != null)
            {
                String classificationText = mModel.getField(R.id.task_field_classification)
                        .getChoices()
                        .getTitle(classificationValue);
                appendProperty(sb, R.string.task_classification, classificationText);
            }
        }


        private void appendStatus(StringBuilder sb)
        {
            Integer statusValue = TaskFieldAdapters.STATUS.get(mContentSet);
            if (statusValue != null && !statusValue.equals(TaskContract.Tasks.STATUS_COMPLETED))
            {
                String statusText = mModel.getField(R.id.task_field_status).getChoices().getTitle(statusValue);
                appendProperty(sb, R.string.task_status, statusText);
            }
        }


        private void appendUrl(StringBuilder sb)
        {
            URL url = TaskFieldAdapters.URL.get(mContentSet);
            if (url != null)
            {
                sb.append(url).append(NEW_LINE);
            }
        }


        private void appendFooter(StringBuilder sb)
        {
            sb.append("--").append(NEW_LINE);
            sb.append(mContext.getString(R.string.opentasks_share_footer));
        }


        private void appendProperty(StringBuilder sb, @StringRes int nameResId, String value)
        {
            sb.append(mContext.getString(nameResId)).append(": ").append(value).append(NEW_LINE);
        }
    }

}
