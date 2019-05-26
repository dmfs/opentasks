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

package org.dmfs.tasks.utils;

import android.content.Context;
import android.content.res.Resources;
import androidx.annotation.VisibleForTesting;
import android.text.format.DateUtils;
import android.text.format.Time;

import org.dmfs.jems.pair.Pair;
import org.dmfs.jems.pair.elementary.ValuePair;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.tasks.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Formatter;
import java.util.Locale;
import java.util.TimeZone;

import static android.text.format.DateUtils.DAY_IN_MILLIS;
import static android.text.format.DateUtils.FORMAT_SHOW_TIME;
import static android.text.format.DateUtils.WEEK_IN_MILLIS;


/**
 * Helper class to format a date to present it to the user.
 *
 * @author Tobias Reinsch <tobias@dmfs.org>
 * @author Marten Gajda <marten@dmfs.org>
 */
public class DateFormatter
{

    public enum DateFormatContext
    {

        /**
         * Always uses a relative date. Use this when the date is with the past or next 6 days, otherwise you might get an absolute date.
         */
        RELATIVE
                {
                    @Override
                    public boolean useRelative(Time now, Time date)
                    {
                        return Math.abs(date.toMillis(false) - now.toMillis(false)) < 7 * 24 * 3600 * 1000;
                    }
                },

        /**
         * The date format in the details view.
         */
        DETAILS_VIEW
                {
                    @Override
                    public int getDateUtilsFlags(Time now, Time date)
                    {
                        if (date.allDay)
                        {
                            return DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_ABBREV_WEEKDAY;
                        }
                        else
                        {
                            return DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_ABBREV_WEEKDAY
                                    | FORMAT_SHOW_TIME;
                        }
                    }
                },

        /**
         * The date format in the list view.
         * <p>
         * Currently this inherits the default short format.
         */
        LIST_VIEW
                {
                    @Override
                    public boolean useRelative(Time now, Time date)
                    {
                        return Math.abs(date.toMillis(false) - now.toMillis(false)) < 7 * 24 * 3600 * 1000;
                    }
                },

        /**
         * The date format in the widget.
         * <p>
         * Currently this inherits the default short format.
         */
        WIDGET_VIEW
                {
                    @Override
                    public int getDateUtilsFlags(Time now, Time date)
                    {
                        int result = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH;
                        if (!date.allDay)
                        {
                            result |= FORMAT_SHOW_TIME;
                        }
                        if (now.year != date.year)
                        {
                            result |= DateUtils.FORMAT_SHOW_YEAR;
                        }
                        return result;
                    }
                },

        /**
         * The date format in the dash clock. This shows a time only.
         */
        DASHCLOCK_VIEW
                {
                    @Override
                    public int getDateUtilsFlags(Time now, Time date)
                    {
                        return FORMAT_SHOW_TIME;
                    }
                },

        /**
         * The date format in notifications.
         */
        NOTIFICATION_VIEW_DATE
                {
                    @Override
                    public int getDateUtilsFlags(Time now, Time date)
                    {
                        return DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH;
                    }


                    @Override
                    public boolean useRelative(Time now, Time date)
                    {
                        return true;
                    }
                },

        /**
         * The date format in notifications.
         */
        NOTIFICATION_VIEW_TIME
                {
                    @Override
                    public int getDateUtilsFlags(Time now, Time date)
                    {
                        return FORMAT_SHOW_TIME;
                    }


                    @Override
                    public boolean useRelative(Time now, Time date)
                    {
                        return false;
                    }
                };


        public int getDateUtilsFlags(Time now, Time date)
        {
            if (now.year == date.year && now.yearDay == date.yearDay)
            {
                // today, show time only
                return FORMAT_SHOW_TIME;
            }
            else if (now.year == date.year)
            {
                // this year, don't include the year
                return DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_ABBREV_WEEKDAY;
            }
            else
            {
                return DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_ABBREV_MONTH | DateUtils.FORMAT_SHOW_WEEKDAY
                        | DateUtils.FORMAT_ABBREV_WEEKDAY;
            }
        }


        public boolean useRelative(Time now, Time date)
        {
            return false;
        }
    }


    /**
     * The format we use for due dates other than today.
     */
    private final DateFormat mDateFormat = DateFormat.getDateInstance(SimpleDateFormat.MEDIUM);

    /**
     * A context to load resource string.
     */
    private Context mContext;

    /**
     * A helper to get the current date & time.
     */
    private Time mNow;

    private static Pair<Locale, Boolean> sIs12hourFormatCache;


    public DateFormatter(Context context)
    {
        mContext = context;
        mNow = new Time();
    }


    /**
     * Format the given due date. The result depends on the current date and on the all-day flag of the due date.
     *
     * @param date
     *         The due date to format.
     * @return A string with the formatted due date.
     */
    public String format(Time date, DateFormatContext dateContext)
    {
        mNow.clear(TimeZone.getDefault().getID());
        mNow.setToNow();
        return format(date, mNow, dateContext);
    }


    /**
     * Same as {@link #format(Time, DateFormatContext)} just with {@link DateTime}s.
     * ({@link Time} will eventually be replaced with {@link DateTime} in the project)
     */
    public String format(DateTime date, DateFormatContext dateContext)
    {
        return format(toTime(date), dateContext);
    }


    /**
     * Format the given due date. The result depends on the current date and on the all-day flag of the due date.
     *
     * @param date
     *         The due date to format.
     * @return A string with the formatted due date.
     */
    public String format(Time date, Time now, DateFormatContext dateContext)
    {

        // normalize time to ensure yearDay is set properly
        date.normalize(false);

        if (dateContext.useRelative(now, date))
        {
            long delta = Math.abs(now.toMillis(false) - date.toMillis(false));

            if (date.allDay)
            {
                Time allDayNow = new Time("UTC");
                allDayNow.set(now.monthDay, now.month, now.year);
                return DateUtils.getRelativeTimeSpanString(date.toMillis(false), allDayNow.toMillis(false), DAY_IN_MILLIS).toString();
            }
            else if (delta < 60 * 1000)
            {
                // the time is within this minute, show "now"
                return mContext.getString(R.string.now);
            }
            else if (delta < 60 * 60 * 1000)
            {
                // time is within this hour, show number of minutes left
                return DateUtils.getRelativeTimeSpanString(date.toMillis(false), now.toMillis(false), DateUtils.MINUTE_IN_MILLIS).toString();
            }
            else if (delta < 24 * 60 * 60 * 1000)
            {
                // time is within 24 hours, show relative string with time
                // FIXME: instead of using a fixed 24 hour interval this should be aligned to midnight tomorrow and yesterday
                return routingGetRelativeDateTimeString(mContext, date.toMillis(false), DAY_IN_MILLIS, WEEK_IN_MILLIS,
                        dateContext.getDateUtilsFlags(now, date)).toString();
            }
            else
            {
                return DateUtils.getRelativeTimeSpanString(date.toMillis(false), now.toMillis(false), DAY_IN_MILLIS).toString();
            }
        }

        return date.allDay ? formatAllDay(date, now, dateContext) : formatNonAllDay(date, now, dateContext);
    }


    /**
     * Same as {@link #format(Time, Time, DateFormatContext)} just with {@link DateTime}s.
     * ({@link Time} will eventually be replaced with {@link DateTime} in the project)
     */
    public String format(DateTime date, DateTime now, DateFormatContext dateContext)
    {
        return format(toTime(date), toTime(now), dateContext);
    }


    private String formatAllDay(Time date, Time now, DateFormatContext dateContext)
    {
        // use DataRange in order to set the correct timezone
        return DateUtils.formatDateRange(mContext, new Formatter(Locale.getDefault()), date.toMillis(false), date.toMillis(false),
                dateContext.getDateUtilsFlags(now, date), "UTC").toString();
    }


    private String formatNonAllDay(Time date, Time now, DateFormatContext dateContext)
    {
        return DateUtils.formatDateTime(mContext, date.toMillis(false), dateContext.getDateUtilsFlags(now, date));
    }


    /**
     * {@link Time} will eventually be replaced with {@link DateTime} in the project.
     * This conversion function is only needed in the transition period.
     */
    @VisibleForTesting
    Time toTime(DateTime dateTime)
    {
        if (dateTime.isFloating() && !dateTime.isAllDay())
        {
            throw new IllegalArgumentException("Cannot support floating DateTime that is not all-day, can't represent it with Time");
        }

        // Time always needs a TimeZone (default ctor falls back to TimeZone.getDefault())
        String timeZoneId = dateTime.getTimeZone() == null ? "UTC" : dateTime.getTimeZone().getID();
        Time time = new Time(timeZoneId);

        time.set(dateTime.getTimestamp());

        // TODO Would using time.set(monthDay, month, year) be better?
        if (dateTime.isAllDay())
        {
            time.allDay = true;
            // This is needed as per time.allDay docs:
            time.hour = 0;
            time.minute = 0;
            time.second = 0;
        }
        return time;
    }


    /**
     * Routes between old and current version of {@link DateUtils#getRelativeDateTimeString(Context, long, long, long, int)}
     * in order to work around the framework bug introduced in Android 6 for this method:
     * not using the user's 12/24 hours settings for the time format.
     * <p>
     * The reported bugs:
     * <p>
     * <a href="https://github.com/dmfs/opentasks/issues/396">opentasks/396</a>
     * <p>
     * <a href="https://issuetracker.google.com/issues/37127319">google/37127319</a>
     */
    private CharSequence routingGetRelativeDateTimeString(Context c, long time, long minResolution,
                                                          long transitionResolution, int flags)
    {
        return isDefaultLocale12HourFormat() ?
                oldGetRelativeDateTimeString(c, time, minResolution, transitionResolution, flags)
                : DateUtils.getRelativeDateTimeString(c, time, minResolution, transitionResolution, flags);
    }


    /**
     * This method is copied from Android 5.1.1 source for {@link DateUtils#getRelativeDateTimeString(Context, long, long, long, int)}
     * <p>
     * <a href="https://android.googlesource.com/platform/frameworks/base/+/android-5.1.1_r29/core/java/android/text/format/DateUtils.java">DateUtils 5.1.1
     * source</a>
     * <p>
     * because newer versions don't respect the 12/24h settings of the user, they use the locale's default instead.
     * <p>
     * Be aware of the original note inside the method, too.
     * <p>
     * ------ Original javadoc:
     * <p>
     * Return string describing the elapsed time since startTime formatted like
     * "[relative time/date], [time]".
     * <p>
     * Example output strings for the US date format.
     * <ul>
     * <li>3 mins ago, 10:15 AM</li>
     * <li>yesterday, 12:20 PM</li>
     * <li>Dec 12, 4:12 AM</li>
     * <li>11/14/2007, 8:20 AM</li>
     * </ul>
     *
     * @param time
     *         some time in the past.
     * @param minResolution
     *         the minimum elapsed time (in milliseconds) to report when showing relative times. For example, a time 3 seconds in the past will be reported as
     *         "0 minutes ago" if this is set to {@link DateUtils#MINUTE_IN_MILLIS}.
     * @param transitionResolution
     *         the elapsed time (in milliseconds) at which to stop reporting relative measurements. Elapsed times greater than this resolution will default to
     *         normal date formatting. For example, will transition from "6 days ago" to "Dec 12" when using {@link DateUtils#WEEK_IN_MILLIS}.
     */
    private CharSequence oldGetRelativeDateTimeString(Context c, long time, long minResolution,
                                                      long transitionResolution, int flags)
    {
        Resources r = c.getResources();
        long now = System.currentTimeMillis();
        long duration = Math.abs(now - time);
        // getRelativeTimeSpanString() doesn't correctly format relative dates
        // above a week or exact dates below a day, so clamp
        // transitionResolution as needed.
        if (transitionResolution > WEEK_IN_MILLIS)
        {
            transitionResolution = WEEK_IN_MILLIS;
        }
        else if (transitionResolution < DAY_IN_MILLIS)
        {
            transitionResolution = DAY_IN_MILLIS;
        }
        CharSequence timeClause = DateUtils.formatDateRange(c, time, time, FORMAT_SHOW_TIME);
        String result;
        if (duration < transitionResolution)
        {
            CharSequence relativeClause = DateUtils.getRelativeTimeSpanString(time, now, minResolution, flags);
            result = r.getString(R.string.opentasks_relative_time, relativeClause, timeClause);
        }
        else
        {
            CharSequence dateClause = DateUtils.getRelativeTimeSpanString(c, time, false);
            result = r.getString(R.string.opentasks_date_time, dateClause, timeClause);
        }
        return result;
    }


    /**
     * Returns whether the default locale uses 12 hour format (am/pm).
     * <p>
     * Based on the implementation in {@link android.text.format.DateFormat#is24HourFormat(Context)}.
     */
    private boolean isDefaultLocale12HourFormat()
    {
        if (sIs12hourFormatCache != null && sIs12hourFormatCache.left().equals(Locale.getDefault()))
        {
            return sIs12hourFormatCache.right();
        }

        Locale locale = Locale.getDefault();
        java.text.DateFormat natural = java.text.DateFormat.getTimeInstance(java.text.DateFormat.LONG, locale);

        boolean result;
        if (natural instanceof SimpleDateFormat)
        {
            result = ((SimpleDateFormat) natural).toPattern().indexOf('H') < 0;
        }
        else
        {
            // We don't know, so we fall back to true. (Same as in {@link DateFormat#is24HourFormat})
            result = true;
        }

        sIs12hourFormatCache = new ValuePair<>(locale, result);
        return result;
    }
}
