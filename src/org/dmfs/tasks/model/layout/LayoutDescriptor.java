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

package org.dmfs.tasks.model.layout;

import android.content.Context;
import android.content.res.Resources;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A descriptor that knows how the widget of a specific field is presented to the user.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public class LayoutDescriptor
{
	public final static String OPTION_USE_TASK_LIST_BACKGROUND_COLOR = "use_list_background_color";
	public final static String OPTION_USE_TASK_BACKGROUND_COLOR = "use_task_background_color";
	public final static String OPTION_NO_TITLE = "no_title";
	public final static String OPTION_MULTILINE = "multiline";

	/**
	 * <code>int</code> option to control the linkification of the displayed text. Use native {@link Linkify} options as parameter or <code>0</code> to disable
	 * links.
	 **/
	public final static String OPTION_LINKIFY = "linkify";
	public final static String OPTION_TIME_FIELD_SHOW_ADD_BUTTONS = "time_field_show_add_buttons";

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


	public View inflate(LayoutInflater inflater, ViewGroup parent)
	{
		if (mResources == null)
		{
			return inflater.inflate(mLayoutId, parent);
		}
		else
		{
			return inflater.inflate(mResources.getLayout(mLayoutId), parent);
		}
	}


	public View inflate(LayoutInflater inflater, ViewGroup parent, boolean attachToRoot)
	{
		if (mResources == null)
		{
			return inflater.inflate(mLayoutId, parent, attachToRoot);
		}
		else
		{
			return inflater.inflate(mResources.getLayout(mLayoutId), parent, attachToRoot);
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


	public LayoutDescriptor setOption(String key, int value)
	{
		if (mOptions == null)
		{
			mOptions = new LayoutOptions();
		}
		mOptions.put(key, value);
		return this;
	}


	public LayoutDescriptor setOption(String key, Object value)
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
