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
import android.os.Build.VERSION;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

import org.dmfs.tasks.R;
import org.dmfs.tasks.model.CheckListItem;
import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.FieldDescriptor;
import org.dmfs.tasks.model.adapters.ChecklistFieldAdapter;
import org.dmfs.tasks.model.layout.LayoutOptions;

import java.util.List;


/**
 * Editor widget for check lists.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public class CheckListFieldEditor extends AbstractFieldEditor implements OnCheckedChangeListener, OnFocusChangeListener
{
    private ChecklistFieldAdapter mAdapter;
    private ViewGroup mContainer;
    private EditText mText;

    private List<CheckListItem> mCurrentValue;
    private LayoutInflater mInflater;

    private boolean mBuilding = false;


    public CheckListFieldEditor(Context context)
    {
        super(context);
        mInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    public CheckListFieldEditor(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        mInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    public CheckListFieldEditor(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        mInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    protected void onFinishInflate()
    {
        super.onFinishInflate();

        mText = (EditText) findViewById(android.R.id.text1);

        mContainer = (ViewGroup) findViewById(R.id.checklist);
    }


    @Override
    public void setFieldDescription(FieldDescriptor descriptor, LayoutOptions layoutOptions)
    {
        super.setFieldDescription(descriptor, layoutOptions);
        mAdapter = (ChecklistFieldAdapter) descriptor.getFieldAdapter();
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    {
        if (!mBuilding && mValues != null)
        {
            if (mCurrentValue == null || mBuilding)
            {
                return;
            }

            ViewParent parent = buttonView.getParent();
            CheckItemTag tag = (CheckItemTag) ((View) parent).getTag();
            if (tag != null && tag.index < mCurrentValue.size())
            {
                mCurrentValue.get(tag.index).checked = isChecked;
                if (mValues != null)
                {
                    mAdapter.validateAndSet(mValues, mCurrentValue);
                }
                return;
            }
        }
    }


    @Override
    public void onFocusChange(View v, boolean hasFocus)
    {
        if (!hasFocus /* update only when loosing the focus */ && !mBuilding && mValues != null)
        {
            ViewParent parent = v.getParent();
            CheckItemTag tag = (CheckItemTag) ((View) parent).getTag();
            if (tag != null && tag.index < mCurrentValue.size())
            {
                if (mCurrentValue.get(tag.index).text.length() == 0)
                {
                    mCurrentValue.remove(tag.index);
                    buildCheckList(mCurrentValue);
                }
            }
            updateValues();
        }
    }


    @Override
    public void onContentLoaded(ContentSet contentSet)
    {
        super.onContentLoaded(contentSet);
        List<CheckListItem> newValue = mCurrentValue = mAdapter.get(contentSet);
        buildCheckList(newValue);
    }


    @Override
    public void onContentChanged(ContentSet contentSet)
    {
        if (mValues != null)
        {
            List<CheckListItem> newValue = mAdapter.get(mValues);
            if (newValue != null && !newValue.equals(mCurrentValue)) // don't trigger unnecessary updates
            {
                buildCheckList(newValue);
                mCurrentValue = newValue;
            }
        }
    }


    @Override
    public void updateValues()
    {
        mAdapter.validateAndSet(mValues, mCurrentValue);
    }


    private void buildCheckList(List<CheckListItem> list)
    {
        mBuilding = true;

        int count = 0;
        for (CheckListItem item : list)
        {
            ViewGroup vg = (ViewGroup) mContainer.getChildAt(count);
            CheckItemTag tag;
            if (vg != null)
            {
                tag = (CheckItemTag) vg.getTag();
                if (tag == null)
                {
                    // this might happen for the initial element
                    tag = new CheckItemTag(vg, count);
                }
            }
            else
            {
                vg = (ViewGroup) mInflater.inflate(R.layout.checklist_field_editor_element, mContainer, false);
                tag = new CheckItemTag(vg, count);
                mContainer.addView(vg);
            }

            tag.setItem(item.checked, item.text, false);

            ++count;
        }

        while (mContainer.getChildCount() > count)
        {
            mContainer.removeViewAt(count);
        }

        // add one empty element
        ViewGroup vg = (ViewGroup) mContainer.getChildAt(count);
        CheckItemTag tag;
        if (vg != null)
        {
            tag = (CheckItemTag) vg.getTag();
            if (tag == null)
            {
                // this might happen for the initial element
                tag = new CheckItemTag(vg, count);
            }
        }
        else
        {
            vg = (ViewGroup) mInflater.inflate(R.layout.checklist_field_editor_element, mContainer, false);
            tag = new CheckItemTag(vg, count);
            mContainer.addView(vg);
        }

        tag.setItem(false, "", true);

        mBuilding = false;
    }


    private class CheckItemTag
    {
        public final CheckBox checkbox;
        public final EditText editText;
        public final int index;
        private boolean mIsLast;


        public CheckItemTag(ViewGroup viewGroup, int index)
        {
            checkbox = (CheckBox) viewGroup.findViewById(android.R.id.checkbox);
            editText = (EditText) viewGroup.findViewById(android.R.id.text1);
            viewGroup.setTag(this);
            this.index = index;

            checkbox.setOnCheckedChangeListener(CheckListFieldEditor.this);

            /* unfortunately every EditText needs a separate TextWatcher, we only use this to add a new line once the last one is written to */
            editText.addTextChangedListener(new TextWatcher()
            {

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count)
                {
                }


                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after)
                {
                }


                @Override
                public void afterTextChanged(Editable s)
                {
                    if (mIsLast && !mBuilding && s.length() > 0)
                    {
                        mCurrentValue.add(new CheckListItem(checkbox.isChecked(), s.toString()));
                        updateValues();
                        buildCheckList(mCurrentValue);
                    }
                    else if (!mBuilding)
                    {
                        mCurrentValue.get(CheckItemTag.this.index).text = s.toString();
                    }
                }
            });
            editText.setOnFocusChangeListener(CheckListFieldEditor.this);
        }


        public void setItem(boolean checked, String text, boolean isLast)
        {
            checkbox.setChecked(checked);
            int selStart = 0;
            int selEnd = 0;
            if (editText.hasFocus())
            {
                selStart = Math.min(editText.getSelectionStart(), text.length());
                selEnd = Math.min(editText.getSelectionEnd(), text.length());
            }
            editText.setText(text);

            if (selEnd != 0 || selStart != 0)
            {
                editText.setSelection(selStart, selEnd);
            }

            mIsLast = isLast;
        }
    }
}
