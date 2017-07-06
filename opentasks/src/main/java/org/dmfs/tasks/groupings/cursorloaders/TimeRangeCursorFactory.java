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

package org.dmfs.tasks.groupings.cursorloaders;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.text.format.Time;

import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;


/**
 * A factory that builds shiny new {@link Cursor}s with time ranges.
 * <p>
 * Note that all times are all-day and normalized to UTC. That means 2014-09-08 will be returned as 2014-09-08 00:00 UTC, no matter which time zone you're in.
 * <p>
 * TODO: fix javadoc
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public class TimeRangeCursorFactory extends AbstractCustomCursorFactory
{

    public final static String RANGE_ID = "_id";
    public final static String RANGE_TYPE = "type";

    public final static int TYPE_END_OF_DAY = 0x01;
    public final static int TYPE_END_OF_YESTERDAY = 0x02 | TYPE_END_OF_DAY;
    public final static int TYPE_END_OF_TODAY = 0x04 | TYPE_END_OF_DAY;
    public final static int TYPE_END_OF_TOMORROW = 0x08 | TYPE_END_OF_DAY;

    public final static int TYPE_END_IN_7_DAYS = 0x10 | TYPE_END_OF_DAY;

    /**
     * Not supported yet
     */
    public final static int TYPE_END_OF_A_WEEK = 0x0100;
    /**
     * Not supported yet
     */
    public final static int TYPE_END_OF_LAST_WEEK = 0x0200 | TYPE_END_OF_A_WEEK;
    /**
     * Not supported yet
     */
    public final static int TYPE_END_OF_THIS_WEEK = 0x0400 | TYPE_END_OF_A_WEEK;
    /**
     * Not supported yet
     */
    public final static int TYPE_END_OF_NEXT_WEEK = 0x0800 | TYPE_END_OF_A_WEEK;

    public final static int TYPE_END_OF_A_MONTH = 0x010000;
    public final static int TYPE_END_OF_LAST_MONTH = 0x020000 | TYPE_END_OF_A_MONTH;
    public final static int TYPE_END_OF_THIS_MONTH = 0x040000 | TYPE_END_OF_A_MONTH;
    public final static int TYPE_END_OF_NEXT_MONTH = 0x080000 | TYPE_END_OF_A_MONTH;

    public final static int TYPE_END_OF_A_YEAR = 0x01000000;
    public final static int TYPE_END_OF_LAST_YEAR = 0x02000000 | TYPE_END_OF_A_YEAR;
    public final static int TYPE_END_OF_THIS_YEAR = 0x04000000 | TYPE_END_OF_A_YEAR;
    public final static int TYPE_END_OF_NEXT_YEAR = 0x08000000 | TYPE_END_OF_A_YEAR;

    public final static int TYPE_OVERDUE = 0x20000000;

    public final static int TYPE_NO_END = 0x80000000;

    public final static String RANGE_START = "start";

    public final static String RANGE_END = "end";

    public final static String RANGE_YEAR = "year";

    public final static String RANGE_MONTH = "month";

    public final static String RANGE_OPEN_FUTURE = "open_future";

    public final static String RANGE_OPEN_PAST = "open_past";

    public final static String RANGE_NULL_ROW = "null_row";

    public final static String RANGE_START_TZ_OFFSET = "start_tz_offset";

    public final static String RANGE_END_TZ_OFFSET = "end_tz_offset";

    public static final String[] DEFAULT_PROJECTION = new String[] {
            RANGE_START, RANGE_END, RANGE_ID, RANGE_YEAR, RANGE_MONTH, RANGE_OPEN_PAST,
            RANGE_OPEN_FUTURE, RANGE_NULL_ROW, RANGE_TYPE, RANGE_START_TZ_OFFSET, RANGE_END_TZ_OFFSET };

    protected final static long MAX_TIME = Long.MAX_VALUE / 2;
    protected final static long MIN_TIME = Long.MIN_VALUE / 2;

    protected final List<String> mProjectionList;

    protected final Time mTime;
    protected final TimeZone mTimezone;


    public TimeRangeCursorFactory(String[] projection)
    {
        super(projection);
        mProjectionList = Arrays.asList(projection);
        mTimezone = TimeZone.getDefault();
        mTime = new Time(mTimezone.getID());
    }


    public Cursor getCursor()
    {
        mTime.setToNow();

        MatrixCursor result = new MatrixCursor(mProjection);

        // get time of today 00:00:00
        Time time = new Time(mTimezone.getID());
        time.set(mTime.monthDay, mTime.month, mTime.year);

        // null row, for tasks without due date
        if (mProjectionList.contains(RANGE_NULL_ROW))
        {
            result.addRow(makeRow(1, 0, null, null));
        }

        long t1 = time.toMillis(false);

        // open past row for overdue tasks
        if (mProjectionList.contains(RANGE_OPEN_PAST))
        {
            result.addRow(makeRow(2, TYPE_END_OF_YESTERDAY, MIN_TIME, t1));
        }

        time.monthDay += 1;
        time.yearDay += 1;
        time.normalize(true);

        // today row
        long t2 = time.toMillis(false);
        result.addRow(makeRow(3, TYPE_END_OF_TODAY, t1, t2));

        time.monthDay += 1;
        time.yearDay += 1;
        time.normalize(true);

        // tomorrow row
        long t3 = time.toMillis(false);
        result.addRow(makeRow(4, TYPE_END_OF_TOMORROW, t2, t3));

        time.monthDay += 5;
        time.yearDay += 5;
        time.normalize(true);

        // next week row
        long t4 = time.toMillis(false);
        result.addRow(makeRow(5, TYPE_END_IN_7_DAYS, t3, t4));

        time.set(1, time.month + 1, time.year);
        time.normalize(true);

        // month row
        long t5 = time.toMillis(false);
        result.addRow(makeRow(6, TYPE_END_OF_A_MONTH, t4, t5));

        time.set(1, 0, time.year + 1);
        // rest of year row
        long t6 = time.toMillis(false);
        result.addRow(makeRow(7, TYPE_END_OF_A_YEAR, t5, t6));

        // open future for future tasks
        if (mProjectionList.contains(RANGE_OPEN_FUTURE))
        {
            result.addRow(makeRow(8, TYPE_NO_END, t6, MAX_TIME));
        }

        return result;
    }


    protected Object[] makeRow(int id, int type, Long start, Long end)
    {
        Object[] result = new Object[mProjection.length];

        insertValue(result, RANGE_ID, id);
        insertValue(result, RANGE_TYPE, type);
        insertValue(result, RANGE_START, start);
        insertValue(result, RANGE_END, end);

        if (start != null && start > MIN_TIME && end != null && end < MAX_TIME)
        {
            mTime.set((start + end) >> 1);
            insertValue(result, RANGE_YEAR, mTime.year);
            insertValue(result, RANGE_MONTH, mTime.month);
        }

        if (start == null || start <= MIN_TIME)
        {
            insertValue(result, RANGE_OPEN_PAST, 1);
            insertValue(result, RANGE_START_TZ_OFFSET, 0);
        }
        else
        {
            insertValue(result, RANGE_START_TZ_OFFSET, mTimezone.getOffset(start));
        }

        if (end == null || end >= MAX_TIME)
        {
            insertValue(result, RANGE_OPEN_FUTURE, 1);
            insertValue(result, RANGE_END_TZ_OFFSET, 0);
        }
        else
        {
            insertValue(result, RANGE_END_TZ_OFFSET, mTimezone.getOffset(end));
        }

        if (start == null && end == null)
        {
            insertValue(result, RANGE_NULL_ROW, 1);
        }

        return result;
    }


    private void insertValue(Object[] row, String column, Object value)
    {
        int index = mProjectionList.indexOf(column);
        if (index >= 0)
        {
            row[index] = value;
        }
    }
}
