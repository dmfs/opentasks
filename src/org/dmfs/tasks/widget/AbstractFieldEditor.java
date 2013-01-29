/*
 * AbstractFieldEditor.java
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

import org.dmfs.tasks.model.FieldDescriptor;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.util.AttributeSet;


/**
 * Mother of all editor fields.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public abstract class AbstractFieldEditor extends AbstractFieldView
{

	private OnChangeListener mListener;


	public AbstractFieldEditor(Context context)
	{
		super(context);
	}


	public AbstractFieldEditor(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}


	public AbstractFieldEditor(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}


	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();
	}


	public void setValue(ContentValues values)
	{
		super.setValue(values);
	}


	public void setup(FieldDescriptor descriptor, Activity context)
	{
		super.setup(descriptor, context);
	}


	protected abstract void updateView();


	public void setOnChangeListender(OnChangeListener listener)
	{
		mListener = listener;
	}


	protected void notifyChange()
	{
		if (mListener != null)
		{
			mListener.onChange(this);
		}
	}

	public interface OnChangeListener
	{
		public void onChange(AbstractFieldEditor sender);
	}
}
