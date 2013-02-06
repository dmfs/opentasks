/*
 * TimeFieldEditor.java
 *
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

import org.dmfs.tasks.R;
import org.dmfs.tasks.model.FieldDescriptor;
import org.dmfs.tasks.model.adapters.TimeFieldAdapter;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.ContentValues;
import android.content.Context;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TimePicker;


/**
 * Widget to edit DateTime values
 * 
 * @author Arjun Naik <arjun@arjunnaik.in>
 * 
 */

public class TimeFieldEditor extends AbstractFieldEditor implements OnDateSetListener, OnTimeSetListener
{
	private static final String TAG = "TimeFieldEditor";
	TimeFieldAdapter mAdapter;
	Button datePicker, timePicker;
	ImageButton clearDate;
	private DateFormat defaultDateFormat, defaultTimeFormat;
	private Time mDateTime;
	private boolean is24hour;


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
		datePicker = (Button) findViewById(R.id.task_date_picker);
		timePicker = (Button) findViewById(R.id.task_time_picker);
		clearDate = (ImageButton) findViewById(R.id.task_time_picker_remove);
		datePicker.setOnClickListener(DatePickerHandler);
		timePicker.setOnClickListener(TimePickerHandler);
		clearDate.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				mDateTime = null;
				mAdapter.set(mValues, mDateTime);
				updateView();
			}
		});
	}


	@Override
	public void setup(FieldDescriptor descriptor, Activity context)
	{
		super.setup(descriptor, context);
		mAdapter = (TimeFieldAdapter) descriptor.getFieldAdapter();
		defaultDateFormat = android.text.format.DateFormat.getDateFormat(mContext);
		defaultTimeFormat = android.text.format.DateFormat.getTimeFormat(mContext);
		is24hour = android.text.format.DateFormat.is24HourFormat(mContext);
	}


	@Override
	public void setValue(ContentValues values)
	{
		super.setValue(values);
		mDateTime = mAdapter.get(mValues);
	}


	@Override
	protected void updateView()
	{
		Log.d("TimeFieldEditor", "CALLED");

		if (mDateTime != null)
		{
			// Log.d(TAG, "mValues is not null");
			// Log.d(TAG, Long.toString(dateTime.toMillis(true)));
			Date currentDate = new Date(mDateTime.toMillis(false));
			String formattedDate = defaultDateFormat.format(currentDate);
			datePicker.setText(formattedDate);

			if (!mDateTime.allDay)
			{
				String formattedTime = defaultTimeFormat.format(currentDate);
				timePicker.setText(formattedTime);
				timePicker.setVisibility(View.VISIBLE);
			}
			else
			{
				timePicker.setVisibility(View.GONE);
			}
		}
		else
		{
			datePicker.setText("");
			timePicker.setText("");
			timePicker.setVisibility(View.VISIBLE);
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
			}

			DatePickerDialog dateDialog = new DatePickerDialog(mContext, TimeFieldEditor.this, mDateTime.year, mDateTime.month, mDateTime.monthDay);

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
			}

			TimePickerDialog timeDialog = new TimePickerDialog(mContext, TimeFieldEditor.this, mDateTime.hour, mDateTime.minute, is24hour);
			timeDialog.show();
		}
	};


	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
	{
		mDateTime.year = year;
		mDateTime.month = monthOfYear;
		mDateTime.monthDay = dayOfMonth;
		mAdapter.set(mValues, mDateTime);
		updateView();
	}


	@Override
	public void onTimeSet(TimePicker view, int hourOfDay, int minute)
	{
		mDateTime.hour = hourOfDay;
		mDateTime.minute = minute;
		mAdapter.set(mValues, mDateTime);
		updateView();
	}

}
