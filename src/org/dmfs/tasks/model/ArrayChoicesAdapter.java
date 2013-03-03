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

package org.dmfs.tasks.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.graphics.drawable.Drawable;
import android.util.Log;


public class ArrayChoicesAdapter implements IChoicesAdapter
{

	private static final String TAG = "ArrayChoicesAdapter";
	private final List<Object> mChoices = new ArrayList<Object>();
	private final List<Object> mVisibleChoices = new ArrayList<Object>();
	private final List<String> mTitles = new ArrayList<String>();
	private final List<Drawable> mDrawables = new ArrayList<Drawable>();


	public ArrayChoicesAdapter()
	{
	}


	@Override
	public String getTitle(Object object)
	{
		int index = mChoices.indexOf(object);
		if (index >= 0)
		{
			return mTitles.get(index);
		}
		return null;
	}


	@Override
	public Drawable getDrawable(Object object)
	{
		int index = mChoices.indexOf(object);
		if (index >= 0)
		{
			return mDrawables.get(index);
		}
		return null;

	}


	public int getIndex(Object object)
	{
		return mVisibleChoices.indexOf(object);
	}


	public ArrayChoicesAdapter addChoice(Object choice, String title, Drawable drawable)
	{
		mVisibleChoices.add(choice);
		mChoices.add(choice);
		mTitles.add(title);
		mDrawables.add(drawable);
		return this;
	}


	public ArrayChoicesAdapter addHiddenChoice(Object choice, String title, Drawable drawable)
	{
		mChoices.add(choice);
		mTitles.add(title);
		mDrawables.add(drawable);
		return this;
	}


	public List<Object> getChoices()
	{
		return Collections.unmodifiableList(mVisibleChoices);
	}

}
