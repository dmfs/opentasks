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
import android.text.format.Time;
import android.util.AttributeSet;
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
	private TimeFieldAdapter mAdapter;
	private Button mDatePickerButton, mTimePickerButton;
	private ImageButton mClearDateButton;
	private DateFormat mDefaultDateFormat, mDefaultTimeFormat;
	private Time mDateTime;
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
			}

			TimePickerDialog timeDialog = new TimePickerDialog(getContext(), TimeFieldEditor.this, mDateTime.hour, mDateTime.minute, mIs24hour);
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
	}


	@Override
	public void onTimeSet(TimePicker view, int hourOfDay, int minute)
	{
		mDateTime.hour = hourOfDay;
		mDateTime.minute = minute;
		mAdapter.set(mValues, mDateTime);
	}


	@Override
	public void onContentChanged(ContentSet contentSet, String key)
	{
		mDateTime = mAdapter.get(mValues);
		if (mDateTime != null)
		{
			Date currentDate = new Date(mDateTime.toMillis(false));
			String formattedDate = mDefaultDateFormat.format(currentDate);
			mDatePickerButton.setText(formattedDate);

			if (!mDateTime.allDay)
			{
				String formattedTime = mDefaultTimeFormat.format(currentDate);
				mTimePickerButton.setText(formattedTime);
				mTimePickerButton.setVisibility(View.VISIBLE);
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
			mTimePickerButton.setVisibility(View.VISIBLE);
			mClearDateButton.setEnabled(false);
		}
	}

}
