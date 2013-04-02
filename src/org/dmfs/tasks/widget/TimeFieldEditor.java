/*
 * Copyright (C) 2012 Marten Gajda <marten@dmfs.org>
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

package org.dmfs.tasks.widget;

import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.dmfs.tasks.R;
import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.FieldDescriptor;
import org.dmfs.tasks.model.adapters.TimeFieldAdapter;
import org.dmfs.tasks.model.layout.LayoutOptions;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TimePicker;


/**
 * Widget to edit DateTime values.
 * 
 * @author Arjun Naik <arjun@arjunnaik.in>
 * @author Marten Gajda <marten@dmfs.org>
 */
public class TimeFieldEditor extends AbstractFieldEditor implements OnDateSetListener, OnTimeSetListener
{
	private TimeFieldAdapter mAdapter;
	private Button mDatePickerButton, mTimePickerButton;
	private ImageButton mClearDateButton;
	private DateFormat mDefaultDateFormat, mDefaultTimeFormat;
	private Time mDateTime;
	private String mTimezone;
	private boolean mOldAllDay = false;
	private int mOldHour = -1;
	private int mOldMinutes = -1;
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
		if (mDatePickerButton != null && mTimePickerButton != null && mClearDateButton != null)
		{
			mDatePickerButton.setOnClickListener(DatePickerHandler);
			mTimePickerButton.setOnClickListener(TimePickerHandler);
			mClearDateButton.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					mDateTime = null;
					mAdapter.set(mValues, mDateTime);
				}
			});
		}
	}


	@Override
	public void setFieldDescription(FieldDescriptor descriptor, LayoutOptions layoutOptions)
	{
		super.setFieldDescription(descriptor, layoutOptions);
		Context context = getContext();
		mAdapter = (TimeFieldAdapter) descriptor.getFieldAdapter();
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

	private OnClickListener DatePickerHandler = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			if (mDateTime == null)
			{
				mDateTime = mAdapter.getDefault(mValues);
				applyTimeInTimeZone(mDateTime, TimeZone.getDefault().getID());
			}

			DatePickerDialog dateDialog = new DatePickerDialog(getContext(), TimeFieldEditor.this, mDateTime.year, mDateTime.month, mDateTime.monthDay);

			dateDialog.show();
		}
	};

	private final OnClickListener TimePickerHandler = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			if (mDateTime == null)
			{
				mDateTime = mAdapter.getDefault(mValues);
				applyTimeInTimeZone(mDateTime, TimeZone.getDefault().getID());
			}

			TimePickerDialog timeDialog = new TimePickerDialog(getContext(), TimeFieldEditor.this, mDateTime.hour, mDateTime.minute, mIs24hour);
			timeDialog.show();
		}
	};


	/**
	 * Updates a {@link Time} instance to date and time of the given time zone, but in the original time zone.
	 * <p>
	 * Example:
	 * </p>
	 * 
	 * <pre>
	 * time: 2013-04-02 16:00 Europe/Berlin (GMT+02:00)
	 * timeZone: America/New_York (GMT-04:00)
	 * 
	 * will result in
	 * 
	 * 2013-04-02 10:00 Europe/Berlin (because the original time is equivalent to 2013-04-02 10:00 America/New_York)
	 * </pre>
	 * 
	 * All-day times are not affected.
	 * 
	 * @param time
	 *            The {@link Time} to update.
	 * @param timeZone
	 *            A time zone id.
	 * 
	 */
	private void applyTimeInTimeZone(Time time, String timeZone)
	{
		if (!time.allDay)
		{
			/*
			 * The default value will be <now> in any time zone. What we want is that the date picker shows the current local time not the time in that time
			 * zone.
			 * 
			 * To fix that we switch to the local time zone and reset the time zone to original time zone. That updates date & time to the local values and
			 * keeps
			 * the original time zone.
			 */
			String originalTimeZone = mDateTime.timezone;
			mDateTime.switchTimezone(timeZone);
			mDateTime.timezone = originalTimeZone;
			mDateTime.set(mDateTime.second, mDateTime.minute, mDateTime.hour, mDateTime.monthDay, mDateTime.month, mDateTime.year);
		}
	}


	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
	{
		mDateTime.year = year;
		mDateTime.month = monthOfYear;
		mDateTime.monthDay = dayOfMonth;
		mAdapter.set(mValues, mDateTime);
	}


	@Override
	public void onTimeSet(TimePicker view, int hourOfDay, int minute)
	{
		mDateTime.hour = hourOfDay;
		mDateTime.minute = minute;
		mAdapter.set(mValues, mDateTime);
	}


	@Override
	public void onContentChanged(ContentSet contentSet)
	{
		mDateTime = mAdapter.get(mValues);
		if (mDateTime != null)
		{
			long oldTime = mDateTime.toMillis(false);

			if (mTimezone != null && !TextUtils.equals(mTimezone, mDateTime.timezone) && !mDateTime.allDay)
			{
				/*
				 * Time zone has changed.
				 * 
				 * We don't want to change date and hour in the editor, so change the values.
				 */
				applyTimeInTimeZone(mDateTime, mTimezone);
			}

			if (mOldAllDay != mDateTime.allDay)
			{
				/*
				 * The allday flag has changed, we may have to restore time and time zone for the UI or to store a valid all-day time.
				 */
				mOldAllDay = mDateTime.allDay;
				if (!mDateTime.allDay)
				{
					/*
					 * Try to restore the time or set a reasonable time if we didn't have any before.
					 */
					if (mOldHour >= 0 && mOldMinutes >= 0)
					{
						mDateTime.hour = mOldHour;
						mDateTime.minute = mOldMinutes;
					}
					else
					{
						Time defaultDate = mAdapter.getDefault(contentSet);
						applyTimeInTimeZone(defaultDate, TimeZone.getDefault().getID());
						mDateTime.hour = defaultDate.hour;
						mDateTime.minute = defaultDate.minute;
					}
					/*
					 * All-day events are floating and have no time zone (though it might be set to UTC).
					 * 
					 * Restore previous time zone if possible, otherwise pick a reasonable default value.
					 */
					mDateTime.timezone = mTimezone == null ? TimeZone.getDefault().getID() : mTimezone;
				}
				else
				{
					// ensure the stored time actually is all day (i.e. timezone == UTC, hours == 0, minutes == 0 and seconds == 0)
					mAdapter.set(contentSet, mDateTime);
					return;
				}
			}
			if (!mDateTime.allDay)
			{
				// preserve current time zone
				mTimezone = mDateTime.timezone;
			}

			if (oldTime != mDateTime.toMillis(false))
			{
				// we changed the time, so update the content set
				mAdapter.set(contentSet, mDateTime);
				return;
			}

			/*
			 * Update UI. Ensure we show the time in the correct time zone.
			 */
			Date currentDate = new Date(mDateTime.toMillis(false));
			mDefaultDateFormat.setTimeZone(TimeZone.getTimeZone(mDateTime.timezone));
			String formattedDate = mDefaultDateFormat.format(currentDate);
			mDatePickerButton.setText(formattedDate);

			if (!mDateTime.allDay)
			{
				mDefaultTimeFormat.setTimeZone(TimeZone.getTimeZone(mDateTime.timezone));
				String formattedTime = mDefaultTimeFormat.format(currentDate);
				mTimePickerButton.setText(formattedTime);
				mTimePickerButton.setVisibility(View.VISIBLE);
				mOldHour = mDateTime.hour;
				mOldMinutes = mDateTime.minute;
			}
			else
			{
				mTimePickerButton.setVisibility(View.GONE);
			}
			mClearDateButton.setEnabled(true);
		}
		else
		{
			mDatePickerButton.setText("");
			mTimePickerButton.setText("");
			mTimePickerButton.setVisibility(mAdapter.isAllDay(mValues) ? View.GONE : View.VISIBLE);
			mClearDateButton.setEnabled(false);
			mTimezone = null;
		}
	}

}
