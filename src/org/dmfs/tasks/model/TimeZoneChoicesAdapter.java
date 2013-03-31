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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.dmfs.tasks.R;
import org.dmfs.tasks.model.adapters.TimeZoneWrapper;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;


/**
 * Adapter which loads an array of timezones from the resources file.
 * 
 * @author Arjun Naik <arjun@arjunnaik.in>
 * @author Marten Gajda <marten@dmfs.org>
 * 
 */
public class TimeZoneChoicesAdapter implements IChoicesAdapter
{

	private final List<String> mIdList = new ArrayList<String>();
	private final Map<String, TimeZoneWrapper> mIdMap = new HashMap<String, TimeZoneWrapper>();
	private final Map<String, String> mNameMap = new HashMap<String, String>();


	public TimeZoneChoicesAdapter(Context context)
	{
		Resources resources = context.getResources();
		String[] titles = resources.getStringArray(R.array.timezone_labels);
		String[] ids = resources.getStringArray(R.array.timezone_values);

		/*
		 * Build time zone lists and maps.
		 */
		for (int i = 0; i < ids.length; ++i)
		{
			String id = ids[i];
			mNameMap.put(id, titles[i]);
			TimeZoneWrapper timezone = new TimeZoneWrapper(id);
			mIdMap.put(id, timezone);
			mIdList.add(id);
		}

		// add GMT if not in the list
		TimeZoneWrapper Gmt = new TimeZoneWrapper("GMT");
		if (!mIdMap.containsValue(Gmt))
		{
			mNameMap.put("GMT", "GMT");
			mIdMap.put("GMT", Gmt);
			mIdList.add("GMT");
		}

		// add any other missing time zone
		for (String id : TimeZone.getAvailableIDs())
		{
			if (!mIdMap.containsKey(id))
			{
				TimeZoneWrapper tz = new TimeZoneWrapper(id);
				boolean hasTz = mIdMap.containsValue(tz);
				if (!hasTz && !id.contains("/") && !id.startsWith("Etc/"))
				{
					// do not add to mNameMap, because we get the name dynamically to reflect summer time
					mIdMap.put(id, tz);
					mIdList.add(id);
				}
				else if (hasTz)
				{
					// tz already exists, we have to find the original tz and map the ID to it
					for (TimeZoneWrapper timezone : mIdMap.values())
					{
						if (tz.equals(timezone))
						{
							mIdMap.put(id, timezone);
							break;
						}
					}
				}
			}
		}

		Collections.sort(mIdList, new Comparator<String>()
		{

			@Override
			public int compare(String lhs, String rhs)
			{
				return mIdMap.get(lhs).getReferenceTimeOffset() - mIdMap.get(rhs).getReferenceTimeOffset();
			}

		});
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
		if (object instanceof TimeZoneWrapper)
		{
			TimeZoneWrapper timezone = (TimeZoneWrapper) object;
			String id = timezone.getID();
			String title = mNameMap.get(id);
			if (title == null)
			{
				title = timezone.getDisplayName(timezone.referenceInDaylightTime(), TimeZone.LONG);
			}
			return getGMTOffsetString(timezone.getReferenceTimeOffset()) + title;
		}
		return "";
	}


	@Override
	public int getIndex(Object object)
	{
		if (!(object instanceof TimeZoneWrapper))
		{
			return -1;
		}

		TimeZoneWrapper timeZone = mIdMap.get(((TimeZoneWrapper) object).getID());
		int idx = mIdList.indexOf(timeZone.getID());
		return idx;
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


	@Override
	public Drawable getDrawable(Object id)
	{
		return null;
	}


	@Override
	public int getCount()
	{
		return mIdList.size();
	}


	@Override
	public Object getItem(int position)
	{
		return mIdMap.get(mIdList.get(position));
	}

}
