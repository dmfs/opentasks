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

package org.dmfs.tasks.widget;

import org.dmfs.tasks.R;
import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.FieldDescriptor;
import org.dmfs.tasks.model.adapters.FieldAdapter;
import org.dmfs.tasks.model.layout.LayoutDescriptor;
import org.dmfs.tasks.model.layout.LayoutOptions;

import android.content.Context;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;


/**
 * A widget that shows the string representation of an object.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public class TextFieldView extends AbstractFieldView
{
	/**
	 * The {@link FieldAdapter} of the field for this view.
	 */
	private FieldAdapter<?> mAdapter;

	/**
	 * The {@link TextView} to show the text in.
	 */
	private TextView mText;


	public TextFieldView(Context context)
	{
		super(context);
	}


	public TextFieldView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}


	public TextFieldView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}


	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();
		mText = (TextView) findViewById(R.id.text);
	}


	@Override
	public void setFieldDescription(FieldDescriptor descriptor, LayoutOptions layoutOptions)
	{
		super.setFieldDescription(descriptor, layoutOptions);
		mAdapter = (FieldAdapter<?>) descriptor.getFieldAdapter();
		mText.setHint(descriptor.getHint());
		mText.setAutoLinkMask(layoutOptions.getInt(LayoutDescriptor.OPTION_LINKIFY, Linkify.ALL));
	}


	@Override
	public void onContentChanged(ContentSet contentSet)
	{
		if (mValues != null)
		{
			Object adapterValue = mAdapter.get(mValues);
			String adapterStringValue = adapterValue != null ? adapterValue.toString() : null;

			if (!TextUtils.isEmpty(adapterStringValue))
			{
				mText.setText(adapterStringValue);
				setVisibility(View.VISIBLE);
			}
			else
			{
				// don't show empty values
				setVisibility(View.GONE);
			}
		}
	}
}
