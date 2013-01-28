package org.dmfs.tasks.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.graphics.drawable.Drawable;


public class ArrayChoicesAdapter implements IChoicesAdapter
{

	private final List<Object> mChoices = new ArrayList<Object>();
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


	public ArrayChoicesAdapter addChoice(Object choice, String title, Drawable drawable)
	{
		mChoices.add(choice);
		mTitles.add(title);
		mDrawables.add(drawable);
		return this;
	}


	public List<Object> getChoices()
	{
		return Collections.unmodifiableList(mChoices);
	}

}
