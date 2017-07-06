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

import android.content.Context;
import android.os.Build.VERSION;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;

import org.dmfs.tasks.R;
import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.FieldDescriptor;
import org.dmfs.tasks.model.adapters.FieldAdapter;
import org.dmfs.tasks.model.adapters.UrlFieldAdapter;
import org.dmfs.tasks.model.layout.LayoutOptions;

import java.net.MalformedURLException;
import java.net.URL;


/**
 * Editor Field for URLs.
 *
 * @author Arjun Naik <arjun@arjunnaik.in>
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class UrlFieldEditor extends AbstractFieldEditor implements TextWatcher
{
    /**
     * The {@link FieldAdapter} of the field for this view.
     */
    private UrlFieldAdapter mAdapter;

    /**
     * The {@link EditText} to edit the URL.
     */
    private EditText mText;


    public UrlFieldEditor(Context context)
    {
        super(context);
    }


    public UrlFieldEditor(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }


    public UrlFieldEditor(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }


    @Override
    protected void onFinishInflate()
    {
        super.onFinishInflate();
        mText = (EditText) findViewById(R.id.text);
        if (mText != null)
        {
            mText.addTextChangedListener(this);

			/*
             * enable memory leak workaround on android < 4.3: disable spell checker
			 */
            int inputType = mText.getInputType();
            if (VERSION.SDK_INT < 18)
            {
                inputType = inputType | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS;
            }
            mText.setInputType(inputType | InputType.TYPE_TEXT_VARIATION_URI);
        }
    }


    @Override
    public void onContentChanged(ContentSet contentSet)
    {
        if (mValues != null)
        {
            URL newUrl = mAdapter.get(mValues);
            if (newUrl == null)
            {
                mText.setText(null);
            }
            else
            {
                String oldValue = mText.getText().toString();
                String newValue = newUrl.toString();
                if (!TextUtils.equals(oldValue, newValue)) // don't trigger unnecessary updates
                {
                    mText.setText(newValue);
                }
            }
        }
    }


    @Override
    public void setFieldDescription(FieldDescriptor descriptor, LayoutOptions layoutOptions)
    {
        super.setFieldDescription(descriptor, layoutOptions);
        mAdapter = (UrlFieldAdapter) descriptor.getFieldAdapter();
        mText.setHint(descriptor.getHint());
    }


    @Override
    public void afterTextChanged(Editable s)
    {
        if (mValues != null)
        {
            try
            {
                String text = mText.getText().toString();
                if (!TextUtils.isEmpty(text))
                {
                    mAdapter.set(mValues, new URL(text));
                }
                else
                {
                    mAdapter.set(mValues, null);
                }
                mText.setError(null);
            }
            catch (MalformedURLException e)
            {
                mText.setError(getContext().getString(R.string.activity_edit_task_error_invalid_url));
                e.printStackTrace();
            }
        }
    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after)
    {
        // not used
    }


    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count)
    {
        // not used
    }

}
