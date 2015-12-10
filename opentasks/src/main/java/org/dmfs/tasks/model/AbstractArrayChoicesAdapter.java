/*
 * Copyright (C) 2013 Marten Gajda <marten@dmfs.org>
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

import java.util.List;

import android.graphics.drawable.Drawable;


/**
 * Abstract class used for array type adapter.
 * 
 * @author Arjun Naik<arjun@arjunnaik.in>
 * @author Marten Gajda<marten@dmfs.org>
 * 
 */
public abstract class AbstractArrayChoicesAdapter implements IChoicesAdapter
{
	protected List<Object> mChoices;
	protected List<Object> mVisibleChoices;
	protected List<String> mTitles;
	protected List<Drawable> mDrawables;


	@Override
	public String getTitle(Object object)
	{
		if (mChoices != null)
		{
			int index = mChoices.indexOf(object);
			if (index >= 0)
			{
				return mTitles.get(index);
			}
		}
		return null;
	}


	@Override
	public Drawable getDrawable(Object object)
	{
		if (mDrawables != null && mChoices != null)
		{
			int index = mChoices.indexOf(object);
			if (index >= 0)
			{
				return mDrawables.get(index);
			}
		}
		return null;
	}


	@Override
	public int getIndex(Object object)
	{
		int index = mVisibleChoices.indexOf(object);
		if (index == -1)
		{
			// not within visible choices, we should return an alternate value if we have any
			int hiddenIndex = mChoices.indexOf(object);
			if (hiddenIndex >= 0)
			{
				// there is a hidden element of that value, return the visible element with the same display value
				// TODO: we should introduce some kind of tag that uniquely identifies elements that are the same

				String title = mTitles.get(hiddenIndex);

				for (int i = 0, count = mVisibleChoices.size(); i < count; ++i)
				{
					Object o = mVisibleChoices.get(i);
					if (title.equals(getTitle(o)))
					{
						return i;
					}
				}
			}
		}
		return index;
	}


	@Override
	public int getCount()
	{
		return mVisibleChoices.size();
	}


	@Override
	public Object getItem(int position)
	{
		return mVisibleChoices.get(position);
	}

}
