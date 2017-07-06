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
import android.widget.CheckBox;

import org.dmfs.tasks.R;
import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.FieldDescriptor;
import org.dmfs.tasks.model.adapters.BooleanFieldAdapter;
import org.dmfs.tasks.model.layout.LayoutOptions;


/**
 * Widget to display boolean values through a {@link CheckBox}.
 *
 * @author Arjun Naik<arjun@arjunnaik.in>
 */
public class BooleanFieldView extends AbstractFieldView
{
    private BooleanFieldAdapter mAdapter;
    private CheckBox mCheckBox;


    public BooleanFieldView(Context context)
    {
        super(context);
    }


    public BooleanFieldView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }


    public BooleanFieldView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }


    protected void onFinishInflate()
    {
        super.onFinishInflate();
        mCheckBox = (CheckBox) findViewById(R.id.checkbox);
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
        Boolean adapterValue = mAdapter.get(mValues);

        if (mValues != null && adapterValue != null)
        {
            mCheckBox.setChecked(adapterValue.booleanValue());
            setVisibility(View.VISIBLE);
        }
        else
        {
            // don't show empty values
            setVisibility(View.GONE);
        }
    }

}
