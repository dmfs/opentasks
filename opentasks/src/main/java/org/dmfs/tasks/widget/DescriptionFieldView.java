/*
 * Copyright 2019 dmfs GmbH
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
import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.jmedeisis.draglinearlayout.DragLinearLayout;
import com.jmedeisis.draglinearlayout.DragLinearLayout.OnViewSwapListener;

import org.dmfs.android.bolts.color.colors.AttributeColor;
import org.dmfs.tasks.R;
import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.DescriptionItem;
import org.dmfs.tasks.model.FieldDescriptor;
import org.dmfs.tasks.model.adapters.DescriptionFieldAdapter;
import org.dmfs.tasks.model.layout.LayoutOptions;

import java.util.List;

import androidx.core.view.ViewCompat;


/**
 * View widget for descriptions with checklists.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public class DescriptionFieldView extends AbstractFieldView implements OnCheckedChangeListener, OnViewSwapListener, OnClickListener
{
    private DescriptionFieldAdapter mAdapter;
    private DragLinearLayout mContainer;

    private List<DescriptionItem> mCurrentValue;

    private boolean mBuilding = false;
    private LayoutInflater mInflater;
    private InputMethodManager mImm;


    public DescriptionFieldView(Context context)
    {
        super(context);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mImm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
    }


    public DescriptionFieldView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mImm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
    }


    public DescriptionFieldView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mImm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
    }


    @Override
    protected void onFinishInflate()
    {
        super.onFinishInflate();
        mContainer = findViewById(R.id.checklist);
        mContainer.setOnViewSwapListener(this);

        mContainer.findViewById(R.id.add_item).setOnClickListener(this);
    }


    @Override
    public void setFieldDescription(FieldDescriptor descriptor, LayoutOptions layoutOptions)
    {
        super.setFieldDescription(descriptor, layoutOptions);
        mAdapter = (DescriptionFieldAdapter) descriptor.getFieldAdapter();
    }


    @SuppressWarnings("deprecation")
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    {
        if (mCurrentValue == null || mBuilding)
        {
            return;
        }

        int childCount = mContainer.getChildCount();
        for (int i = 0; i < childCount; ++i)
        {
            if (mContainer.getChildAt(i).findViewById(android.R.id.checkbox) == buttonView)
            {
                mCurrentValue.get(i).checked = isChecked;
                ((TextView) mContainer.getChildAt(i).findViewById(android.R.id.title)).setTextAppearance(getContext(),
                        isChecked ? R.style.checklist_checked_item_text : R.style.dark_text);
                if (mValues != null)
                {
                    mAdapter.validateAndSet(mValues, mCurrentValue);
                }
                return;
            }
        }
    }


    @Override
    public void updateValues()
    {
        mAdapter.validateAndSet(mValues, mCurrentValue);
    }


    @Override
    public void onContentLoaded(ContentSet contentSet)
    {
        super.onContentLoaded(contentSet);
    }


    @Override
    public void onContentChanged(ContentSet contentSet)
    {
        if (mValues != null)
        {
            List<DescriptionItem> newValue = mAdapter.get(mValues);
            if (newValue != null && !newValue.equals(mCurrentValue)) // don't trigger unnecessary updates
            {
                updateCheckList(newValue);
                mCurrentValue = newValue;
            }
        }
    }


    private void updateCheckList(List<DescriptionItem> list)
    {
        setVisibility(VISIBLE);

        mBuilding = true;

        int count = 0;
        for (final DescriptionItem item : list)
        {
            View itemView = mContainer.getChildAt(count);
            if (itemView == null || itemView.getId() != R.id.checklist_element)
            {
                itemView = createItemView();
                mContainer.addView(itemView, mContainer.getChildCount() - 1);
                mContainer.setViewDraggable(itemView, itemView.findViewById(R.id.drag_handle));
            }

            bindItemView(itemView, item);

            ++count;
        }

        while (mContainer.getChildCount() > count + 1)
        {
            View view = mContainer.getChildAt(count);
            mContainer.removeDragView(view);
        }

        mBuilding = false;
    }


    @Override
    public void onSwap(View view1, int position1, View view2, int position2)
    {
        if (mCurrentValue != null)
        {
            DescriptionItem item1 = mCurrentValue.get(position1);
            DescriptionItem item2 = mCurrentValue.get(position2);

            // swap items in the list
            mCurrentValue.set(position2, item1);
            mCurrentValue.set(position1, item2);
        }
    }


    /**
     * Inflates a new check list element view.
     *
     * @return
     */
    private View createItemView()
    {
        return mInflater.inflate(R.layout.description_field_view_element, mContainer, false);
    }


    @SuppressWarnings("deprecation")
    private void bindItemView(final View itemView, final DescriptionItem item)
    {
        // set the checkbox status
        CheckBox checkbox = itemView.findViewById(android.R.id.checkbox);
        // make sure we don't receive our own updates
        checkbox.setOnCheckedChangeListener(null);
        checkbox.setChecked(item.checked && item.checkbox);
        checkbox.jumpDrawablesToCurrentState();
        checkbox.setOnCheckedChangeListener(DescriptionFieldView.this);
        checkbox.setVisibility(item.checkbox ? VISIBLE : GONE);

        // configure the title
        final EditText text = itemView.findViewById(android.R.id.title);
        text.setTextAppearance(getContext(), item.checked && item.checkbox ? R.style.checklist_checked_item_text : R.style.dark_text);
        if (text.getTag() != null)
        {
            text.removeTextChangedListener((TextWatcher) text.getTag());
        }
        text.setText(item.text);
        ColorStateList colorStateList = new ColorStateList(
                new int[][] { new int[] { android.R.attr.state_focused }, new int[] { -android.R.attr.state_focused } },
                new int[] { new AttributeColor(getContext(), R.attr.colorPrimary).argb(), 0 });
        ViewCompat.setBackgroundTintList(text, colorStateList);
        text.setOnFocusChangeListener(new OnFocusChangeListener()
        {

            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                View tools = itemView.findViewById(R.id.tools);
                tools.setVisibility(hasFocus ? VISIBLE : GONE);
                String newText = text.getText().toString();
                if (!hasFocus && !newText.equals(item.text) && mValues != null && !mCurrentValue.equals(mAdapter.get(mValues)))
                {
                    item.text = newText;
                }

                if (hasFocus)
                {
                    v.postDelayed(
                            () -> tools.requestRectangleOnScreen(new Rect(0, 0, tools.getWidth(), tools.getHeight()), false),
                            200);
                }
            }
        });
        if (item.checkbox)
        {
            text.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        }
        else
        {
            text.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        }
        text.setOnEditorActionListener(new OnEditorActionListener()
        {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if (actionId == EditorInfo.IME_ACTION_NEXT)
                {
                    int pos = mContainer.indexOfChild(itemView);
                    insertEmptyItem(item.checkbox, pos + 1);
                    return true;
                }
                return false;
            }
        });
        text.setImeOptions(EditorInfo.IME_ACTION_NEXT);

        TextWatcher watcher = new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {

            }


            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {

            }


            @Override
            public void afterTextChanged(Editable editable)
            {
                item.text = editable.toString();
            }
        };

        text.setTag(watcher);
        text.addTextChangedListener(watcher);

        // bind the remove button
        View removeButton = itemView.findViewById(R.id.delete);
        removeButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //   mImm.hideSoftInputFromWindow(text.getWindowToken(), 0);
                mCurrentValue.remove(item);

                mContainer.removeDragView(itemView);
                mAdapter.validateAndSet(mValues, mCurrentValue);
            }
        });

        // bind the remove button
        TextView toggleCheckableButton = itemView.findViewById(R.id.toggle_checkable);
        toggleCheckableButton.setText(item.checkbox ? R.string.opentasks_hide_tick_box : R.string.opentasks_show_tick_box);
        toggleCheckableButton.setCompoundDrawablesWithIntrinsicBounds(item.checkbox ? R.drawable.ic_text_24px : R.drawable.ic_list_24px, 0, 0, 0);
        toggleCheckableButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //     mImm.hideSoftInputFromWindow(text.getWindowToken(), 0);
                int idx = mCurrentValue.indexOf(item);
                mCurrentValue.remove(item);
                if (!item.checkbox)
                {
                    String[] lines = item.text.split("\n");

                    if (lines.length == 1)
                    {
                        DescriptionItem newItem = new DescriptionItem(true, item.checked, item.text);
                        mCurrentValue.add(idx, newItem);
                    }
                    else
                    {
                        for (String i : lines)
                        {
                            DescriptionItem newItem = new DescriptionItem(true, false, i);
                            mCurrentValue.add(idx, newItem);
                            idx += 1;

                        }
                    }
                }
                else
                {
                    DescriptionItem newItem = new DescriptionItem(false, item.checked, item.text);
                    mCurrentValue.add(idx, newItem);
                    bindItemView(itemView, newItem);
                }
                updateCheckList(mCurrentValue);
                mAdapter.validateAndSet(mValues, mCurrentValue);
            }
        });
    }


    /**
     * Insert an empty item at the given position. Nothing will be inserted if the check list already contains an empty item at the given position. The new (or
     * exiting) emtpy item will be focused and the keyboard will be opened.
     *
     * @param withCheckBox
     * @param pos
     */
    private void insertEmptyItem(boolean withCheckBox, int pos)
    {
        if (mCurrentValue.size() > pos && mCurrentValue.get(pos).text.length() == 0)
        {
            // there already is an empty item at this pos focus it and return
            View view = mContainer.getChildAt(pos);
            focusTitle(view);
            return;
        }

        // create a new empty item
        DescriptionItem item = new DescriptionItem(withCheckBox, false, "");
        mCurrentValue.add(pos, item);
        View newItem = createItemView();
        bindItemView(newItem, item);

        // append it to the list
        mContainer.addDragView(newItem, newItem.findViewById(R.id.drag_handle), pos);

        focusTitle(newItem);
    }


    @Override
    public void onClick(View v)
    {
        int id = v.getId();
        if (id == R.id.add_item)
        {
            insertEmptyItem(!mCurrentValue.isEmpty(), mCurrentValue.size());
        }
    }


    /**
     * Focus the title element of the given view and open the keyboard if necessary.
     *
     * @param view
     */
    private void focusTitle(View view)
    {
        View titleView = view.findViewById(android.R.id.title);
        if (titleView != null)
        {
            titleView.requestFocus();
            mImm.showSoftInput(titleView, InputMethodManager.SHOW_IMPLICIT);
        }
    }

}
