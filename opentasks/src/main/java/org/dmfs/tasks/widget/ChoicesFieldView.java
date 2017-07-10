/*
 * Copyright 2017 dmfs GmbH
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
 */

package org.dmfs.tasks.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import org.dmfs.tasks.R;
import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.FieldDescriptor;
import org.dmfs.tasks.model.IChoicesAdapter;
import org.dmfs.tasks.model.adapters.FieldAdapter;
import org.dmfs.tasks.model.layout.LayoutOptions;


/**
 * Widget to show the currently selected value of a field that provides an {@link IChoicesAdapter}.
 *
 * @author Arjun Naik <arjun@arjunnaik.in>
 */
public class ChoicesFieldView extends AbstractFieldView
{
    private FieldAdapter<Object> mAdapter;
    private TextView mText;


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
            IChoicesAdapter choicesAdapter = mFieldDescriptor.getChoices();
            if (choicesAdapter == null)
            {
                // no choices adapter -> nothing to show
                mText.setText(mAdapter.get(mValues).toString());
                mText.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }
            else
            {
                // just show title and drawable (if any)
                mText.setText(choicesAdapter.getTitle(mAdapter.get(mValues)));
                mText.setCompoundDrawables(choicesAdapter.getDrawable(mAdapter.get(mValues)), null, null, null);
            }
        }
        else
        {
            setVisibility(View.GONE);
        }
    }

}
