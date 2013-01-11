package org.dmfs.tasks.model.layout;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;


public class LayoutDescriptor
{

	private final Resources mResources;
	private final int mLayoutId;
	private int mParentId = -1;


	public LayoutDescriptor(int layoutId)
	{
		mResources = null;
		mLayoutId = layoutId;
	}


	public LayoutDescriptor(Context context, int layoutId)
	{
		mResources = context.getResources();
		mLayoutId = layoutId;
	}


	public LayoutDescriptor(Resources resources, int layoutId)
	{
		mResources = resources;
		mLayoutId = layoutId;
	}


	public View inflate(LayoutInflater inflater)
	{
		if (mResources == null)
		{
			return inflater.inflate(mLayoutId, null);
		}
		else
		{
			return inflater.inflate(mResources.getLayout(mLayoutId), null);
		}
	}


	public void setParentId(int id)
	{
		mParentId = id;
	}


	public int getParentId()
	{
		return mParentId;
	}
}
