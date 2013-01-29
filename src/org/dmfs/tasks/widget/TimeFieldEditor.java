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

import org.dmfs.tasks.R;
import org.dmfs.tasks.model.FieldDescriptor;
import org.dmfs.tasks.model.adapters.TimeFieldAdapter;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;


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
	private Context appContext;
	private Time dateTime;


	public TimeFieldEditor(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		appContext = context.getApplicationContext();
	}


	public TimeFieldEditor(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		appContext = context.getApplicationContext();
	}


	public TimeFieldEditor(Context context)
	{
		super(context);
		appContext = context.getApplicationContext();
	}


	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();
		datePicker = (Button) findViewById(R.id.task_date_picker);
		timePicker = (Button) findViewById(R.id.task_time_picker);
		// appContext = getContext();
	}


	@Override
	public void setup(FieldDescriptor descriptor, Activity context)
	{
		super.setup(descriptor, context);
		mAdapter = (TimeFieldAdapter) descriptor.getFieldAdapter();
	}


	@Override
	protected void updateView()
	{
		Log.d("TimeFieldEditor", "CALLED");
		if (mValues != null && mAdapter.get(mValues) != null)
		{
			// Log.d(TAG, "mValues is not null");
			dateTime = mAdapter.get(mValues);
			// Log.d(TAG, Long.toString(dateTime.toMillis(true)));
			String formattedDate = dateTime.format("%d/%m/%Y");
			datePicker.setText(formattedDate);
			datePicker.setOnClickListener(new DatePickerHandler());
			if (dateTime.allDay)
			{
				timePicker.setVisibility(View.GONE);

			}
			else
			{
				String formattedTime = dateTime.format("%H:%M:%S");
				timePicker.setText(formattedTime);
				timePicker.setOnClickListener(new TimePickerHandler());
			}

		}

	}

	private class DatePickerHandler implements OnClickListener
	{

		@Override
		public void onClick(View v)
		{
			DatePickerDialog dateDialog = new DatePickerDialog(mContext, TimeFieldEditor.this, dateTime.year, dateTime.month, dateTime.monthDay);
			dateDialog.show();
		}

	}

	private class TimePickerHandler implements OnClickListener
	{

		@Override
		public void onClick(View v)
		{
			TimePickerDialog timeDialog = new TimePickerDialog(mContext, TimeFieldEditor.this, dateTime.hour, dateTime.minute, dateTime.allDay);
			timeDialog.show();
		}

	}


	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
	{
		dateTime.year = year;
		dateTime.month = monthOfYear;
		dateTime.monthDay = dayOfMonth;
		updateDateTimeSpinners();
	}


	@Override
	public void onTimeSet(TimePicker view, int hourOfDay, int minute)
	{
		dateTime.hour = hourOfDay;
		dateTime.minute = minute;
		updateDateTimeSpinners();
		
	}
	
	private void updateDateTimeSpinners(){
		String formattedDate = dateTime.format("%d/%m/%Y");
		datePicker.setText(formattedDate);
		if(!dateTime.allDay){
			String formattedTime = dateTime.format("%H:%M:%S");
			timePicker.setText(formattedTime);
		}
	}
	
}
