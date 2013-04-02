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

package org.dmfs.tasks.widget;

import org.dmfs.tasks.R;
import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.FieldDescriptor;
import org.dmfs.tasks.model.IChoicesAdapter;
import org.dmfs.tasks.model.adapters.FieldAdapter;
import org.dmfs.tasks.model.layout.LayoutOptions;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * Widget to display Integer values.
 * 
 * @author Arjun Naik <arjun@arjunnaik.in>
 * 
 */

public class ChoicesFieldView extends AbstractFieldView
{
	private FieldAdapter<Object> mAdapter;
	private TextView mText;
	private ImageView mImage;


	public ChoicesFieldView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);

	}


	public ChoicesFieldView(Context context, AttributeSet attrs)
	{
		super(context, attrs);

	}


	public ChoicesFieldView(Context context)
	{
		super(context);

	}


	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();
		mText = (TextView) findViewById(R.id.text);
		mImage = (ImageView) findViewById(R.id.choice_drawable);
	}


	@SuppressWarnings("unchecked")
	@Override
	public void setFieldDescription(FieldDescriptor descriptor, LayoutOptions layoutOptions)
	{
		super.setFieldDescription(descriptor, layoutOptions);
		mAdapter = (FieldAdapter<Object>) descriptor.getFieldAdapter();
		mText.setHint(descriptor.getHint());
	}


	@Override
	public void onContentChanged(ContentSet contentSet)
	{
		if (mValues != null && mAdapter.get(mValues) != null)
		{
			IChoicesAdapter choicesAdapter = fieldDescriptor.getChoices();
			if (choicesAdapter == null)
			{
				mText.setText(mAdapter.get(mValues).toString());
				mImage.setVisibility(View.GONE);
			}
			else
			{
				mText.setText(choicesAdapter.getTitle(mAdapter.get(mValues)));
				mImage.setImageDrawable(choicesAdapter.getDrawable(mAdapter.get(mValues)));
			}
		}
		else
		{
			setVisibility(View.GONE);
		}
	}

}
