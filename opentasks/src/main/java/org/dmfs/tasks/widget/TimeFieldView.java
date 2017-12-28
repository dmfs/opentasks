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

package org.dmfs.tasks.widget;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import org.dmfs.opentaskspal.datetime.general.TimeZones;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.Duration;
import org.dmfs.tasks.R;
import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.FieldDescriptor;
import org.dmfs.tasks.model.adapters.FieldAdapter;
import org.dmfs.tasks.model.adapters.TimeZoneWrapper;
import org.dmfs.tasks.model.layout.LayoutDescriptor;
import org.dmfs.tasks.model.layout.LayoutOptions;
import org.dmfs.tasks.utils.DateFormatter;
import org.dmfs.tasks.utils.DateFormatter.DateFormatContext;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


/**
 * Widget to display DateTime values
 *
 * @author Arjun Naik <arjun@arjunnaik.in>
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class TimeFieldView extends AbstractFieldView implements OnClickListener
{
    /**
     * The {@link FieldAdapter} of the field for this view.
     */
    private FieldAdapter<DateTime> mAdapter;

    /**
     * The text view that shows the time in the local time zone.
     */
    private TextView mText;

    /**
     * The text view that shows the time in the task's original time zone if it's different from the local time zone.
     */
    private TextView mTimeZoneText;

    /**
     * Formatters for date and time.
     */
    private java.text.DateFormat mDefaultDateFormat, mDefaultTimeFormat;

    /**
     * The default time zone on this device. Usually what the user has configured in the settings or what the provider returns.
     */
    private TimeZone mDefaultTimeZone = new TimeZoneWrapper();

    private TextView mAddOneHourButton;
    private TextView mAddOneDayButton;

    private final DateFormatter mDateFormatter;


    public TimeFieldView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        mDateFormatter = new DateFormatter(context);
    }


    public TimeFieldView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        mDateFormatter = new DateFormatter(context);
    }


    public TimeFieldView(Context context)
    {
        super(context);
        mDateFormatter = new DateFormatter(context);
    }


    @Override
    protected void onFinishInflate()
    {
        super.onFinishInflate();
        mText = (TextView) findViewById(android.R.id.text1);
        mTimeZoneText = (TextView) findViewById(android.R.id.text2);
        mDefaultDateFormat = java.text.DateFormat.getDateInstance(SimpleDateFormat.LONG);
        mDefaultTimeFormat = DateFormat.getTimeFormat(getContext());
        mAddOneDayButton = (TextView) findViewById(R.id.button_add_one_day);
        if (mAddOneDayButton != null)
        {
            // might be called to early in Android 2.x
            mAddOneDayButton.setOnClickListener(this);
        }
        mAddOneHourButton = (TextView) findViewById(R.id.button_add_one_hour);
        if (mAddOneHourButton != null)
        {
            // might be called to early in Android 2.x
            mAddOneHourButton.setOnClickListener(this);
        }
    }


    @Override
    public void setFieldDescription(FieldDescriptor descriptor, LayoutOptions layoutOptions)
    {
        super.setFieldDescription(descriptor, layoutOptions);
        mAdapter = (FieldAdapter<DateTime>) descriptor.getFieldAdapter();
        mText.setHint(descriptor.getHint());
        findViewById(R.id.buttons).setVisibility(layoutOptions.getBoolean(LayoutDescriptor.OPTION_TIME_FIELD_SHOW_ADD_BUTTONS, false) ? VISIBLE : GONE);
    }


    @Override
    public void onContentChanged(ContentSet contentSet)
    {
        DateTime newValue = mAdapter.get(mValues);
        if (mValues != null && newValue != null)
        {
            Date fullDate = new Date(newValue.getTimestamp());
            String formattedTime;
            if (!newValue.isAllDay())
            {
                mDefaultDateFormat.setTimeZone(mDefaultTimeZone);
                mDefaultTimeFormat.setTimeZone(mDefaultTimeZone);
                TimeZoneWrapper taskTimeZone = new TimeZoneWrapper(newValue.getTimeZone());

                formattedTime = mDateFormatter.format(newValue, DateFormatContext.DETAILS_VIEW);

                if (!taskTimeZone.equals(mDefaultTimeZone) && !TimeZones.UTC.equals(newValue.getTimeZone()) && mTimeZoneText != null)
                {
                    /*
                     * The date has a time zone that is different from the default time zone, so show the original time too.
                     */
                    mDefaultDateFormat.setTimeZone(taskTimeZone);
                    mDefaultTimeFormat.setTimeZone(taskTimeZone);

                    mTimeZoneText.setText(mDefaultDateFormat.format(fullDate) + " " + mDefaultTimeFormat.format(fullDate) + " "
                            + taskTimeZone.getDisplayName(taskTimeZone.inDaylightTime(fullDate), TimeZone.SHORT));
                    mTimeZoneText.setVisibility(View.VISIBLE);
                }
                else
                {
                    mTimeZoneText.setVisibility(View.GONE);
                }

                mAddOneHourButton.setVisibility(VISIBLE);
            }
            else
            {
                // all-day times are always in UTC
                if (mTimeZoneText != null)
                {
                    mTimeZoneText.setVisibility(View.GONE);
                }
                formattedTime = mDateFormatter.format(newValue, DateFormatContext.DETAILS_VIEW);
                mAddOneHourButton.setVisibility(INVISIBLE);
            }
            mText.setText(formattedTime);
            setVisibility(View.VISIBLE);
        }
        else
        {
            setVisibility(View.GONE);
        }
    }


    @Override
    public void onClick(View v)
    {
        int id = v.getId();
        DateTime dateTime = mAdapter.get(mValues);
        if (id == R.id.button_add_one_day)
        {
            dateTime = dateTime.addDuration(new Duration(1, 1, 0));
        }
        else if (id == R.id.button_add_one_hour)
        {
            dateTime = dateTime.addDuration(new Duration(1, 0, 1, 0, 0));
        }
        mAdapter.validateAndSet(mValues, dateTime);
    }
}
