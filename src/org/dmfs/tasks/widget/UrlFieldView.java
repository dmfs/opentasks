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

import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.FieldDescriptor;
import org.dmfs.tasks.model.adapters.FieldAdapter;
import org.dmfs.tasks.model.layout.LayoutOptions;

import android.content.Context;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;


/**
 * A view that shows the a clickable URL.
 * 
 * @author Arjun Naik <arjun@arjunnaik.in>
 */
public final class UrlFieldView extends AbstractFieldView
{
	/**
	 * The {@link FieldAdapter} of the field for this view.
	 */
	private FieldAdapter<?> mAdapter;

	/**
	 * The {@link TextView} to show the URL in.
	 */
	private TextView mText;


	public UrlFieldView(Context context)
	{
		super(context);

	}


	public UrlFieldView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}


	public UrlFieldView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}


	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();
		mText = (TextView) findViewById(android.R.id.text1);

		if (mText == null)
		{
			// on older Android version onFinishInflate can be called multiple times if the view contains includes
			return;
		}

		MovementMethod mMethod = LinkMovementMethod.getInstance();
		mText.setMovementMethod(mMethod);
	}


	@Override
	public void setFieldDescription(FieldDescriptor descriptor, LayoutOptions layoutOptions)
	{
		super.setFieldDescription(descriptor, layoutOptions);
		mAdapter = (FieldAdapter<?>) descriptor.getFieldAdapter();
		mText.setHint(descriptor.getHint());
	}


	@Override
	public void onContentChanged(ContentSet contentSet)
	{
		Object value;
		if (mValues != null && (value = mAdapter.get(mValues)) != null)
		{
			String urlString = value.toString();
			mText.setText(Html.fromHtml("<a href='" + urlString + "'>" + urlString + "</a>"));
			setVisibility(View.VISIBLE);
		}
		else
		{
			setVisibility(View.GONE);
		}

	}
}
