/*
 * URLFieldEditor.java
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

import org.dmfs.tasks.R;
import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.FieldDescriptor;
import org.dmfs.tasks.model.adapters.UrlFieldAdapter;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;


/**
 * Editor Field for URLs.
 * 
 * @author Arjun Naik <arjun@arjunnaik.in>
 */

public class URLFieldEditor extends AbstractFieldEditor
{

	private UrlFieldAdapter mAdapter;
	private EditText mText;


	public URLFieldEditor(Context context)
	{
		super(context);
	}


	public URLFieldEditor(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}


	public URLFieldEditor(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}


	@Override
	public void onContentChanged(ContentSet contentSet, String key)
	{
		if (mValues != null && mAdapter.get(mValues) != null)
		{
			mText.setText(mAdapter.get(mValues).toString());
		}
	}


	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();
		mText = (EditText) findViewById(R.id.text);
	}


	@Override
	public void setup(FieldDescriptor descriptor, Activity context)
	{
		super.setup(descriptor, context);
		mAdapter = (UrlFieldAdapter) descriptor.getFieldAdapter();
		mText.setHint(descriptor.getHint());
	}

}
