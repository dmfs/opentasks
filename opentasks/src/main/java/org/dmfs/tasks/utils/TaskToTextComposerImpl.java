package org.dmfs.tasks.utils;

import android.content.Context;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.text.format.Time;
import org.dmfs.tasks.R;
import org.dmfs.tasks.model.*;

import java.util.Arrays;
import java.util.List;


/**
 * @author Gabor Keszthelyi
 */
public class TaskToTextComposerImpl implements TaskToTextComposer
{
    private static final String NEW_LINE = "\n";

    private final Context mContext;
    private final DateFormatter mDateFormatter;

    public TaskToTextComposerImpl(Context context)
    {
        mContext = context;
        mDateFormatter = new DateFormatter(mContext);
    }


    @Override
    public String title(ContentSet contentSet)
    {
        return TaskFieldAdapters.TITLE.get(contentSet);
    }


    @Override
    public String body(ContentSet contentSet)
    {
        StringBuilder sb = new StringBuilder();

        appendTitle(sb, contentSet);
        sb.append(NEW_LINE);
        appendDescription(sb, contentSet);
        appendChecklistItems(sb, contentSet);
        sb.append(NEW_LINE);
        appendTimes(sb, contentSet);

        /*
        TODO
        Priority: <priority text>
        Privacy: <privacy text>
        <url>
        + "We still need to add the overall status of the task (i.e. needs action/in progress/completed/cancelled)."
         */

        return sb.toString();
    }


    private void appendTimes(StringBuilder sb, ContentSet contentSet)
    {
        // TODO Review logic in {@link TimeFieldView#OnContentChanged}, maybe extract that to be re-usable here.
        appendTime(sb, R.string.task_start, TaskFieldAdapters.DTSTART.get(contentSet));
        appendTime(sb, R.string.task_due, TaskFieldAdapters.DUE.get(contentSet));
        appendTime(sb, R.string.task_completed, TaskFieldAdapters.COMPLETED.get(contentSet));
    }

    private void appendTime(StringBuilder sb, @StringRes int nameResId, Time time)
    {
        if (time != null)
        {
            sb.append(mContext.getString(nameResId)).append(": ").append(formatTime(time)).append(NEW_LINE);
        }
    }


    private String formatTime(Time dueTime)
    {
        return mDateFormatter.format(dueTime, DateFormatter.DateFormatContext.DETAILS_VIEW);
    }


    private void appendChecklistItems(StringBuilder sb, ContentSet contentSet)
    {
        List<CheckListItem> checkListItems = TaskFieldAdapters.CHECKLIST.get(contentSet);
        if (checkListItems != null)
        {
            for (CheckListItem item : checkListItems)
            {
                if (!TextUtils.isEmpty(item.text))
                {
                    sb.append('[').append(item.checked ? 'x' : ' ').append("] ").append(item.text).append(NEW_LINE);
                }
            }
        }
    }


    private void appendDescription(StringBuilder sb, ContentSet contentSet)
    {
        String description = TaskFieldAdapters.DESCRIPTION.get(contentSet);
        if (description != null)
        {
            sb.append(description).append(NEW_LINE);
        }
    }


    private void appendTitle(StringBuilder sb, ContentSet contentSet)
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
}
