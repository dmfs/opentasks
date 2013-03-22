/*
 * 
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
package org.dmfs.tasks.model;

import java.util.TimeZone;

import org.dmfs.tasks.R;

import android.content.Context;


/**
 * ArrayAdapter which loads an array of timezones from the resources file.
 * 
 * @author Arjun Naik<arjun@arjunnaik.in>
 * 
 */
public class TimeZoneArrayChoicesAdapter extends ResourceArrayChoicesAdapter
{

	public TimeZoneArrayChoicesAdapter(Context context)
	{
		super(R.array.timezone_values, R.array.timezone_labels, context);
	}


	/**
	 * This function
	 * 
	 * @param object
	 *            The timezone string
	 */
	@Override
	public String getTitle(Object object)
	{
		TimeZone selectedTimeZone = TimeZone.getTimeZone(object.toString());
		String title = super.getTitle(object);
		if (title == null)
		{
			title = selectedTimeZone.getDisplayName();
		}
		return getGMTOffsetString(selectedTimeZone.getRawOffset()) + title;
	}


	private String getGMTOffsetString(long millis)
	{
		long absmillis = (millis < 0) ? -millis : millis;
		int minutes = (int) ((absmillis / (1000 * 60)) % 60);
		int hours = (int) ((absmillis / (1000 * 60 * 60)) % 24);
		StringBuilder builder = new StringBuilder("(GMT");
		builder.append((millis >= 0) ? '+' : '-');
		builder.append(String.format("%02d:%02d", hours, minutes)).append(") ");
		return builder.toString();
	}

}
