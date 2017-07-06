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
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.FieldDescriptor;
import org.dmfs.tasks.model.adapters.BooleanFieldAdapter;
import org.dmfs.tasks.model.layout.LayoutOptions;


/**
 * Editor for boolean values using a {@link CheckBox}.
 *
 * @author Arjun Naik<arjun@arjunnaik.in>
 */
public class BooleanFieldEditor extends AbstractFieldEditor implements OnCheckedChangeListener, OnClickListener
{
    private CheckBox mCheckBox;
    private BooleanFieldAdapter mAdapter;


    public BooleanFieldEditor(Context context)
    {
        super(context);
    }


    public BooleanFieldEditor(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }


    public BooleanFieldEditor(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }


    @Override
    protected void onFinishInflate()
    {
        super.onFinishInflate();
        this.setOnClickListener(this);
        mCheckBox = (CheckBox) findViewById(android.R.id.checkbox);
        if (mCheckBox != null)
        {
            mCheckBox.setOnCheckedChangeListener(this);
        }
    }


    @Override
    public void onClick(View v)
    {
        mCheckBox.toggle();
    }


    @Override
    public void setFieldDescription(FieldDescriptor descriptor, LayoutOptions layoutOptions)
    {
        super.setFieldDescription(descriptor, layoutOptions);
        mAdapter = (BooleanFieldAdapter) descriptor.getFieldAdapter();
    }


    @Override
    public void onContentChanged(ContentSet contentSet)
    {
        if (mValues != null)
        {
            Boolean newValue = mAdapter.get(mValues);
            if (newValue != null)
            {
                mCheckBox.setChecked(newValue.booleanValue());
            }
        }
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    {
        Boolean oldValue = mAdapter.get(mValues);
        if (oldValue == null || oldValue != isChecked) // don't trigger unnecessary updates
        {
            mAdapter.validateAndSet(mValues, isChecked);
        }
    }

}
