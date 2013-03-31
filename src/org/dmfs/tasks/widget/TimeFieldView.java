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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.dmfs.tasks.R;
import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.FieldDescriptor;
import org.dmfs.tasks.model.adapters.TimeFieldAdapter;
import org.dmfs.tasks.model.adapters.TimeZoneWrapper;
import org.dmfs.tasks.model.layout.LayoutOptions;

import android.content.Context;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;


/**
 * Widget to display DateTime values
 * 
 * @author Arjun Naik <arjun@arjunnaik.in>
 * 
 */
public final class TimeFieldView extends AbstractFieldView
{
	private TimeFieldAdapter mAdapter;
	private TextView mText;
	private TextView mTimeZoneText;
	private java.text.DateFormat mDefaultDateFormat, mDefaultTimeFormat;
	private final static TimeZone UTC = TimeZone.getTimeZone(Time.TIMEZONE_UTC);
	private TimeZone defaultTimeZone = new TimeZoneWrapper();


	public TimeFieldView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}


	public TimeFieldView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}


	public TimeFieldView(Context context)
	{
		super(context);
	}


	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();
		mText = (TextView) findViewById(R.id.text);
		mTimeZoneText = (TextView) findViewById(R.id.timezone_text);
		mDefaultDateFormat = java.text.DateFormat.getDateInstance(SimpleDateFormat.LONG);
		mDefaultTimeFormat = DateFormat.getTimeFormat(getContext());
	}


	@Override
	public void setFieldDescription(FieldDescriptor descriptor, LayoutOptions layoutOptions)
	{
		super.setFieldDescription(descriptor, layoutOptions);
		mAdapter = (TimeFieldAdapter) descriptor.getFieldAdapter();
		mText.setHint(descriptor.getHint());
	}


	@Override
	public void onContentChanged(ContentSet contentSet, String key)
	{
		Time newValue = mAdapter.get(mValues);
		if (mValues != null && newValue != null)
		{
			Date fullDate = new Date(newValue.toMillis(false));
			String formattedTime;
			if (!newValue.allDay)
			{
				mDefaultDateFormat.setTimeZone(defaultTimeZone);
				mDefaultTimeFormat.setTimeZone(defaultTimeZone);

				formattedTime = mDefaultDateFormat.format(fullDate);
				TimeZoneWrapper taskTimeZone = new TimeZoneWrapper(newValue.timezone);

				formattedTime = formattedTime + " " + mDefaultTimeFormat.format(fullDate);

				if (!taskTimeZone.equals(defaultTimeZone) && ! Time.TIMEZONE_UTC.equals(taskTimeZone.getID()) && mTimeZoneText != null)
				{
					mDefaultDateFormat.setTimeZone(taskTimeZone);
					mDefaultTimeFormat.setTimeZone(taskTimeZone);

					mTimeZoneText.setText(mDefaultDateFormat.format(fullDate) + " " + mDefaultTimeFormat.format(fullDate) + " "
						+ taskTimeZone.getDisplayName(taskTimeZone.inDaylightTime(new Date(newValue.toMillis(false))), TimeZone.SHORT));
					mTimeZoneText.setVisibility(View.VISIBLE);
				}
				else
				{
					mTimeZoneText.setVisibility(View.GONE);
				}
			}
			else
			{
				mDefaultDateFormat.setTimeZone(UTC);
				formattedTime = mDefaultDateFormat.format(fullDate);
			}
			mText.setText(formattedTime);
		}
		else
		{
			setVisibility(View.GONE);
		}
	}
}
