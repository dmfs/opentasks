/*
 * TimeFieldView.java
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

import java.text.SimpleDateFormat;
import java.util.Date;

import org.dmfs.tasks.R;
import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.FieldDescriptor;
import org.dmfs.tasks.model.adapters.TimeFieldAdapter;

import android.app.Activity;
import android.content.Context;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;


/**
 * Widget to display DateTime values
 * 
 * @author Arjun Naik <arjun@arjunnaik.in>
 * 
 */
public class TimeFieldView extends AbstractFieldView
{
	private static final String TAG = "TimeFieldView";
	private TimeFieldAdapter mAdapter;
	private TextView mText;
	java.text.DateFormat defaultDateFormat, defaultTimeFormat;


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
	}


	@Override
	public void setup(FieldDescriptor descriptor, Activity context)
	{
		Log.d(TAG, "setup is called");
		super.setup(descriptor, context);
		mAdapter = (TimeFieldAdapter) descriptor.getFieldAdapter();
		mText.setHint(descriptor.getHint());
		defaultDateFormat = java.text.DateFormat.getDateInstance(SimpleDateFormat.LONG);
		defaultTimeFormat = DateFormat.getTimeFormat(getContext());
	}


	@Override
	public void onContentChanged(ContentSet contentSet, String key)
	{
		Log.d(TAG, "mText" + mText);
		Log.d(TAG, "mAdapter" + mAdapter);
		if (mValues != null && mAdapter.get(mValues) != null)
		{
			Log.d(TAG, "mValues is not null");
			Time dateTime = mAdapter.get(mValues);
			Date fullDate = new Date(dateTime.toMillis(false));
			String formattedTime;
			formattedTime = defaultDateFormat.format(fullDate);
			if (!dateTime.allDay)
			{
				// formattedTime = dateTime.format("%d/%m/%Y");
				formattedTime = formattedTime + " " + defaultTimeFormat.format(fullDate);
			}
			mText.setText(formattedTime);
		}
		else
		{
			setVisibility(View.GONE);
			Log.d(TAG, "mValues is null");
		}
	}

}
