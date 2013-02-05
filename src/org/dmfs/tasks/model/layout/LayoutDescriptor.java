package org.dmfs.tasks.model.layout;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;


public class LayoutDescriptor
{

	public final static String OPTION_USE_TASK_LIST_BACKGROUND_COLOR = "use_list_background_color";
	public final static String OPTION_USE_TASK_BACKGROUND_COLOR = "use_task_background_color";
	public final static String OPTION_NO_TITLE = "no_title";
	public final static String OPTION_MULTILINE = "multiline";

	/**
	 * Empty layout options. We return it if there are no layout options. It adds the overhead of one object and a few virtual method calls, but it improves
	 * code readability and reduces the chance of forgetting a <code>!=null</code> check.
	 */
	private final static LayoutOptions DEFAULT_OPTIONS = new LayoutOptions();

	private final Resources mResources;
	private final int mLayoutId;
	private int mParentId = -1;
	private LayoutOptions mOptions;


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


	public LayoutDescriptor setParentId(int id)
	{
		mParentId = id;
		return this;
	}


	public int getParentId()
	{
		return mParentId;
	}


	public LayoutDescriptor setOption(String key, boolean value)
	{
		if (mOptions == null)
		{
			mOptions = new LayoutOptions();
		}
		mOptions.put(key, value);
		return this;
	}


	public LayoutOptions getOptions()
	{
		return mOptions != null ? mOptions : DEFAULT_OPTIONS;
	}
}
