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
import android.support.v4.content.Loader;

import org.dmfs.opentaskspal.datetime.general.StartOfNextDay;
import org.dmfs.tasks.utils.TimeChangeListener;
import org.dmfs.tasks.utils.TimeChangeObserver;


/**
 * A very simple {@link Loader} that returns the {@link Cursor} from a {@link TimeRangeCursorFactory}. It also delivers a new Cursor each time the time or the
 * time zone changes and each day at midnight.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public class TimeRangeCursorLoader extends CustomCursorLoader implements TimeChangeListener
{
    private final TimeChangeObserver mTimeChangeObserver;


    public TimeRangeCursorLoader(Context context, String[] projection)
    {
        super(context, new TimeRangeShortCursorFactory(projection));

        // set trigger at midnight
        mTimeChangeObserver = new TimeChangeObserver(context, this);
        mTimeChangeObserver.setNextAlarm(midnightTimestamp());
    }


    @Override
    public void onTimeUpdate(TimeChangeObserver observer)
    {
        // reset next alarm
        observer.setNextAlarm(midnightTimestamp());

        // notify LoaderManager
        onContentChanged();
    }


    @Override
    public void onAlarm(TimeChangeObserver observer)
    {
        // set next alarm
        observer.setNextAlarm(midnightTimestamp());

        // notify LoaderManager
        onContentChanged();
    }


    @Override
    protected void onReset()
    {
        mTimeChangeObserver.releaseReceiver();
        super.onReset();
    }


    private long midnightTimestamp()
    {
        return new StartOfNextDay().value().getTimestamp();
    }

}
