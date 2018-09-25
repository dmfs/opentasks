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

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TimePicker;

import org.dmfs.tasks.R;
import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.FieldDescriptor;
import org.dmfs.tasks.model.adapters.FieldAdapter;
import org.dmfs.tasks.model.layout.LayoutOptions;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static org.dmfs.tasks.model.TaskFieldAdapters.ALLDAY;


/**
 * Widget to edit DateTime values.
 *
 * @author Arjun Naik <arjun@arjunnaik.in>
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class TimeFieldEditor extends AbstractFieldEditor implements OnDateSetListener, OnTimeSetListener, OnClickListener
{
    /**
     * The adapter to load the values from a {@link ContentSet}.
     */
    private FieldAdapter<Time> mAdapter;

    /**
     * The buttons to show the current date and time and to launch the date & time pickers.
     */
    private Button mDatePickerButton, mTimePickerButton;

    /**
     * The button to clear the current date.
     */
    private ImageButton mClearDateButton;

    /**
     * The {@link DateFormat} instances to format date and time in a local representation to present it to the user.
     */
    private DateFormat mDefaultDateFormat, mDefaultTimeFormat;

    /**
     * The current time this editor represents.
     */
    private Time mDateTime;

    /**
     * The last time zone used. This is used to restore the time zone when the date is switched to all-day and back.
     */
    private String mTimezone;

    /**
     * Indicates that the date has been changed and we have to update the UI.
     */
    private boolean mUpdated = false;

    /**
     * The hour we have shown last. This is used to restore the hour when switching to all-day and back.
     */
    private int mOldHour = -1;

    /**
     * The minutes we have shown last. This is used to restore the minutes when switching to all-day and back.
     */
    private int mOldMinutes = -1;

    /**
     * Indicates whether to show the time in 24 hour format or not.
     */
    private boolean mIs24hour;


    public TimeFieldEditor(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }


    public TimeFieldEditor(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }


    public TimeFieldEditor(Context context)
    {
        super(context);
    }


    @Override
    protected void onFinishInflate()
    {
        super.onFinishInflate();
        mDatePickerButton = (Button) findViewById(R.id.task_date_picker);
        mTimePickerButton = (Button) findViewById(R.id.task_time_picker);
        mClearDateButton = (ImageButton) findViewById(R.id.task_time_picker_remove);
        if (mDatePickerButton != null)
        {
            mDatePickerButton.setOnClickListener(this);
        }
        if (mTimePickerButton != null)
        {
            mTimePickerButton.setOnClickListener(this);
        }
        if (mClearDateButton != null)
        {
            mClearDateButton.setOnClickListener(this);
        }
    }


    @Override
    public void setFieldDescription(FieldDescriptor descriptor, LayoutOptions layoutOptions)
    {
        super.setFieldDescription(descriptor, layoutOptions);
        Context context = getContext();
        mAdapter = (FieldAdapter<Time>) descriptor.getFieldAdapter();
        mDefaultDateFormat = android.text.format.DateFormat.getDateFormat(context);
        mDefaultTimeFormat = android.text.format.DateFormat.getTimeFormat(context);
        mIs24hour = android.text.format.DateFormat.is24HourFormat(context);
    }


    @Override
    public void setValue(ContentSet values)
    {
        super.setValue(values);
        if (mValues != null)
        {
            mDateTime = mAdapter.get(mValues);
        }
    }


    @Override
    public void onClick(View view)
    {
        final int id = view.getId();
        if (id == R.id.task_date_picker || id == R.id.task_time_picker)
        {
            // one of the date or time buttons has been clicked

            if (mDateTime == null)
            {
                // initialize date and time
                mDateTime = mAdapter.getDefault(mValues);
                applyTimeInTimeZone(mDateTime, TimeZone.getDefault().getID());
            }

            // show the correct dialog
            Dialog dialog;
            if (id == R.id.task_date_picker)
            {
                dialog = getDatePickerWithSamsungWorkaround();
            }
            else
            {
                dialog = new TimePickerDialog(getContext(), TimeFieldEditor.this, mDateTime.hour, mDateTime.minute, mIs24hour);
            }
            dialog.show();
        }
        else if (id == R.id.task_time_picker_remove)
        {
            // the clear button as been pressed
            mUpdated = true;
            mAdapter.validateAndSet(mValues, null);
        }
    }


    /**
     * Updates a {@link Time} instance to date and time of the given time zone, but in the original time zone.
     * <p>
     * Example:
     * </p>
     * <p>
     * <pre>
     * input time: 2013-04-02 16:00 Europe/Berlin (GMT+02:00)
     * input timeZone: America/New_York (GMT-04:00)
     * </pre>
     * <p>
     * will result in
     * <p>
     * <pre>
     * 2013-04-02 10:00 Europe/Berlin (because the original time is equivalent to 2013-04-02 10:00 America/New_York)
     * </pre>
     * <p>
     * All-day times are not modified.
     *
     * @param time
     *         The {@link Time} to update.
     * @param timeZone
     *         A time zone id.
     */
    private void applyTimeInTimeZone(Time time, String timeZone)
    {
        if (!time.allDay)
        {
            /*
             * Switch to timeZone and reset back to original time zone. That updates date & time to the values in timeZone but keeps the original time zone.
             */
            String originalTimeZone = time.timezone;
            time.switchTimezone(timeZone);
            time.timezone = originalTimeZone;
            time.set(time.second, time.minute, time.hour, time.monthDay, time.month, time.year);
        }
    }


    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
    {
        if (ALLDAY.get(mValues))
        {
            mDateTime.timezone = Time.TIMEZONE_UTC;
            mDateTime.set(dayOfMonth, monthOfYear, year);
        }
        else
        {
            mDateTime.year = year;
            mDateTime.month = monthOfYear;
            mDateTime.monthDay = dayOfMonth;
            mDateTime.normalize(true);
        }
        mUpdated = true;
        mAdapter.validateAndSet(mValues, mDateTime);
    }


    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute)
    {
        mDateTime.hour = hourOfDay;
        mDateTime.minute = minute;
        mUpdated = true;
        mAdapter.validateAndSet(mValues, mDateTime);
    }


    @Override
    public void onContentChanged(ContentSet contentSet)
    {
        Time newTime = mAdapter.get(mValues);
        if (!mUpdated && newTime != null && mDateTime != null && Time.compare(newTime, mDateTime) == 0
                && TextUtils.equals(newTime.timezone, mDateTime.timezone) && newTime.allDay == mDateTime.allDay)
        {
            // nothing has changed
            return;
        }
        mUpdated = false;

        if (newTime != null)
        {
            if (mDateTime != null && mDateTime.timezone != null && !TextUtils.equals(mDateTime.timezone, newTime.timezone) && !newTime.allDay)
            {
                /*
                 * Time zone has been changed.
                 *
                 * We don't want to change date and hour in the editor, so apply the old time zone.
                 */
                applyTimeInTimeZone(newTime, mDateTime.timezone);
            }

            if (mDateTime != null && mDateTime.allDay != newTime.allDay)
            {
                /*
                 * The all-day flag has been changed, we may have to restore time and time zone for the UI.
                 */
                if (!newTime.allDay)
                {
                    /*
                     * Try to restore the time or set a reasonable time if we didn't have any before.
                     */
                    if (mOldHour >= 0 && mOldMinutes >= 0)
                    {
                        newTime.hour = mOldHour;
                        newTime.minute = mOldMinutes;
                    }
                    else
                    {
                        Time defaultDate = mAdapter.getDefault(contentSet);
                        applyTimeInTimeZone(defaultDate, TimeZone.getDefault().getID());
                        newTime.hour = defaultDate.hour;
                        newTime.minute = defaultDate.minute;
                    }
                    /*
                     * All-day events are floating and have no time zone (though it might be set to UTC).
                     *
                     * Restore previous time zone if possible, otherwise pick a reasonable default value.
                     */
                    newTime.timezone = mTimezone == null ? TimeZone.getDefault().getID() : mTimezone;
                    newTime.normalize(true);
                }
                else
                {
                    // apply time zone shift to end up with the right day
                    newTime.set(mDateTime.toMillis(false) + TimeZone.getTimeZone(mDateTime.timezone).getOffset(mDateTime.toMillis(false)));
                    newTime.set(newTime.monthDay, newTime.month, newTime.year);
                }
            }

            if (!newTime.allDay)
            {
                // preserve current time zone
                mTimezone = newTime.timezone;
            }

            /*
             * Update UI. Ensure we show the time in the correct time zone.
             */
            Date currentDate = new Date(newTime.toMillis(false));
            TimeZone timeZone = TimeZone.getTimeZone(newTime.timezone);

            if (mDatePickerButton != null)
            {
                mDefaultDateFormat.setTimeZone(timeZone);
                String formattedDate = mDefaultDateFormat.format(currentDate);
                mDatePickerButton.setText(formattedDate);
            }

            if (mTimePickerButton != null)
            {
                if (!newTime.allDay)
                {
                    mDefaultTimeFormat.setTimeZone(timeZone);
                    String formattedTime = mDefaultTimeFormat.format(currentDate);
                    mTimePickerButton.setText(formattedTime);
                    mTimePickerButton.setVisibility(View.VISIBLE);
                }
                else
                {
                    mTimePickerButton.setVisibility(View.GONE);
                }
            }

            if (!newTime.allDay)
            {
                mOldHour = newTime.hour;
                mOldMinutes = newTime.minute;
            }

            if (mClearDateButton != null)
            {
                mClearDateButton.setEnabled(true);
            }

            if (mDateTime == null || Time.compare(newTime, mDateTime) != 0 || !TextUtils.equals(newTime.timezone, mDateTime.timezone)
                    || newTime.allDay != mDateTime.allDay)
            {
                // We have modified the time, so update contentSet.
                mDateTime = newTime;
                mAdapter.validateAndSet(contentSet, newTime);
            }
        }
        else
        {
            if (mDatePickerButton != null)
            {
                mDatePickerButton.setText("");
            }

            if (mTimePickerButton != null)
            {
                mTimePickerButton.setText("");
                mTimePickerButton.setVisibility(ALLDAY.get(mValues) ? View.GONE : View.VISIBLE);
            }

            if (mClearDateButton != null)
            {
                mClearDateButton.setEnabled(false);
            }
            mTimezone = null;
        }

        mDateTime = newTime;
    }


    /**
     * A workaround method to display DatePicker while avoiding crashed on Samsung Android 5.0 devices
     *
     * @see http://stackoverflow.com/questions/28345413/datepicker-crash-in-samsung-with-android-5-0
     */
    private Dialog getDatePickerWithSamsungWorkaround()
    {
        // The datepicker on Samsung Android 5.0 devices crashes for certain languages, e.g. french and polish
        // We fall back to the holo datepicker in this case. German and English are confirmed to work.
        if (Build.VERSION.SDK_INT == VERSION_CODES.LOLLIPOP && Build.MANUFACTURER.equalsIgnoreCase("samsung")
                && !("en".equals(Locale.getDefault().getLanguage())))
        {
            // get holo picker
            DatePickerDialog dialog = new DatePickerDialog(getContext(), R.style.DatePickerHolo, TimeFieldEditor.this, mDateTime.year, mDateTime.month,
                    mDateTime.monthDay);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));

            // change divider color
            DatePicker dpView = dialog.getDatePicker();
            LinearLayout llFirst = (LinearLayout) dpView.getChildAt(0);
            LinearLayout llSecond = (LinearLayout) llFirst.getChildAt(0);
            for (int i = 0; i < llSecond.getChildCount(); i++)
            {
                NumberPicker picker = (NumberPicker) llSecond.getChildAt(i); // Numberpickers in llSecond
                // reflection - picker.setDividerDrawable(divider); << didn't seem to work.
                Field[] pickerFields = NumberPicker.class.getDeclaredFields();
                for (Field pf : pickerFields)
                {
                    if (pf.getName().equals("mSelectionDivider"))
                    {
                        pf.setAccessible(true);
                        try
                        {
                            pf.set(picker, new ColorDrawable(getResources().getColor(R.color.material_deep_teal_500)));
                        }
                        catch (IllegalArgumentException e)
                        {
                            e.printStackTrace();
                        }
                        catch (NotFoundException e)
                        {
                            e.printStackTrace();
                        }
                        catch (IllegalAccessException e)
                        {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
            }
            return dialog;
        }
        else
        {
            return new DatePickerDialog(getContext(), TimeFieldEditor.this, mDateTime.year, mDateTime.month, mDateTime.monthDay);
        }
    }
}
