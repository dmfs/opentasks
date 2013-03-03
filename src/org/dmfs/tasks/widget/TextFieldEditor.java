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

import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.FieldDescriptor;
import org.dmfs.tasks.model.adapters.StringFieldAdapter;
import org.dmfs.tasks.model.layout.LayoutOptions;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;


/**
 * Editor widget for simple text fields.
 * <p>
 * There seems to be an memory leak issue with Android {@link EditText} views, see: <a
 * href="http://stackoverflow.com/questions/8497965/why-does-editview-retain-its-activitys-context-in-ice-cream-sandwich">Why does EditView retain its
 * Activity's Context in Ice Cream Sandwich</a> To workaround this issue we have to disable the spell checker for the text field.
 * </p>
 * <p>
 * TODO: find a way to enable the spell checker (at least temporarily).
 * </p>
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public class TextFieldEditor extends AbstractFieldEditor implements TextWatcher
{
	private StringFieldAdapter mAdapter;
	private EditText mText;


	public TextFieldEditor(Context context)
	{
		super(context);
	}


	public TextFieldEditor(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}


	public TextFieldEditor(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}


	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();
		mText = (EditText) findViewById(android.R.id.text1);
		mText.addTextChangedListener(this);

		/*
		 * enable memory leak workaround: disable spell checker
		 */
		int inputType = mText.getInputType();
		mText.setInputType(inputType | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
	}


	@Override
	public void setFieldDescription(FieldDescriptor descriptor, LayoutOptions layoutOptions)
	{
		super.setFieldDescription(descriptor, layoutOptions);
		mAdapter = (StringFieldAdapter) descriptor.getFieldAdapter();
		mText.setHint(descriptor.getHint());
	}


	@Override
	public void afterTextChanged(Editable s)
	{
		if (mValues != null)
		{
			final String newText = s.toString();
			final String oldText = mAdapter.get(mValues);
			if (!TextUtils.equals(newText, oldText)) // don't trigger unnecessary updates
			{
				mAdapter.set(mValues, newText);
			}
		}
	}


	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after)
	{
		// nothing to do here
	}


	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count)
	{
		// nothing to do here
	}


	@Override
	public void onContentChanged(ContentSet contentSet, String key)
	{
		if (mValues != null)
		{
			String newValue = mAdapter.get(mValues);
			String oldValue = mText.getText().toString();
			if (!TextUtils.equals(oldValue, newValue)) // don't trigger unnecessary updates
			{
				mText.setText(newValue);
			}
		}
	}
}
