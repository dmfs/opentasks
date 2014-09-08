/*
 * Copyright (C) 2014 Marten Gajda <marten@dmfs.org>
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

package org.dmfs.tasks.groupings.cursorloaders;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.text.format.Time;


/**
 * A factory that builds shorter {@link Cursor}s with time ranges.
 * 
 * 
 * @author Tobias Reinsch <tobias@dmfs.org>
 */
public class TimeRangeStartCursorFactory extends TimeRangeCursorFactory
{

	public TimeRangeStartCursorFactory(String[] projection)
	{
		super(projection);

	}


	@Override
	public Cursor getCursor()
	{

		mTime.setToNow();
		;

		MatrixCursor result = new MatrixCursor(mProjection);

		// get time of today 00:00:00
		Time time = new Time(mTime.timezone);
		time.set(mTime.monthDay, mTime.month, mTime.year);

		// already started row
		long t1 = time.toMillis(false);
		result.addRow(makeRow(1, TYPE_OVERDUE, MIN_TIME, t1));

		time.hour = 0;
		time.minute = 0;
		time.second = 0;

		time.monthDay += 1;
		time.yearDay += 1;
		time.normalize(true);

		// today row
		long t2 = time.toMillis(false);
		result.addRow(makeRow(2, TYPE_END_OF_TODAY, t1, t2));

		time.monthDay += 1;
		time.yearDay += 1;
		time.normalize(true);

		// tomorrow row
		long t3 = time.toMillis(false);
		result.addRow(makeRow(3, TYPE_END_OF_TOMORROW, t2, t3));

		time.monthDay += 5;
		time.yearDay += 5;
		time.normalize(true);

		// next week row
		long t4 = time.toMillis(false);
		result.addRow(makeRow(4, TYPE_END_IN_7_DAYS, t3, t4));

		time.monthDay += 1;
		time.normalize(true);

		// open past future for future tasks (including tasks without dates)
		if (mProjectionList.contains(RANGE_OPEN_FUTURE))
		{
			result.addRow(makeRow(5, TYPE_NO_END, t4, MAX_TIME));
		}

		return result;
	}

}
