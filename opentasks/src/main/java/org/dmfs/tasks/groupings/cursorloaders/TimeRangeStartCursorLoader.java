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

import android.content.Context;
import android.database.Cursor;
import androidx.loader.content.Loader;
import android.text.format.Time;

import org.dmfs.tasks.utils.TimeChangeListener;
import org.dmfs.tasks.utils.TimeChangeObserver;

import java.util.TimeZone;


/**
 * A very simple {@link Loader} that returns the {@link Cursor} from a {@link TimeRangeStartCursorFactory}. It also delivers a new Cursor each time the time or
 * the time zone changes and each day at midnight.
 *
 * @author Tobias Reinsch <tobias@dmfs.org>
 */
public class TimeRangeStartCursorLoader extends CustomCursorLoader implements TimeChangeListener
{
    /**
     * A helper to retrieve the timestamp for midnight.
     */
    private final Time mMidnight = new Time();
    private final TimeChangeObserver mTimeChangeObserver;


    public TimeRangeStartCursorLoader(Context context, String[] projection)
    {
        super(context, new TimeRangeStartCursorFactory(projection));

        // set trigger at midnight
        mTimeChangeObserver = new TimeChangeObserver(context, this);
        mTimeChangeObserver.setNextAlarm(getMidnightTimestamp());
    }


    @Override
    public void onTimeUpdate(TimeChangeObserver observer)
    {
        // reset next alarm
        observer.setNextAlarm(getMidnightTimestamp());

        // notify LoaderManager
        onContentChanged();
    }


    @Override
    public void onAlarm(TimeChangeObserver observer)
    {
        // set next alarm
        observer.setNextAlarm(getMidnightTimestamp());

        // notify LoaderManager
        onContentChanged();
    }


    @Override
    protected void onReset()
    {
        mTimeChangeObserver.releaseReceiver();
        super.onReset();
    }


    private long getMidnightTimestamp()
    {
        mMidnight.clear(TimeZone.getDefault().getID());
        mMidnight.setToNow();
        mMidnight.set(mMidnight.monthDay, mMidnight.month, mMidnight.year);
        ++mMidnight.monthDay;
        mMidnight.normalize(true);
        return mMidnight.toMillis(false);
    }

}
