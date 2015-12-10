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

import java.util.List;

import org.dmfs.tasks.R;
import org.dmfs.tasks.model.CheckListItem;
import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.FieldDescriptor;
import org.dmfs.tasks.model.adapters.ChecklistFieldAdapter;
import org.dmfs.tasks.model.layout.LayoutOptions;

import android.content.Context;
import android.os.Build.VERSION;
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


/**
 * View widget for checklists.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public class CheckListFieldView extends AbstractFieldView implements OnCheckedChangeListener, OnViewSwapListener, OnClickListener
{
	private ChecklistFieldAdapter mAdapter;
	private DragLinearLayout mContainer;

	private List<CheckListItem> mCurrentValue;

	private boolean mBuilding = false;
	private LayoutInflater mInflater;
	private InputMethodManager mImm;


	public CheckListFieldView(Context context)
	{
		super(context);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mImm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
	}


	public CheckListFieldView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mImm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
	}


	public CheckListFieldView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mImm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
	}


	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();
		mContainer = (DragLinearLayout) findViewById(R.id.checklist);
		mContainer.setOnViewSwapListener(this);

		mContainer.findViewById(R.id.add_item).setOnClickListener(this);
	}


	@Override
	public void setFieldDescription(FieldDescriptor descriptor, LayoutOptions layoutOptions)
	{
		super.setFieldDescription(descriptor, layoutOptions);
		mAdapter = (ChecklistFieldAdapter) descriptor.getFieldAdapter();
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
			List<CheckListItem> newValue = mAdapter.get(mValues);
			if (newValue != null && !newValue.equals(mCurrentValue)) // don't trigger unnecessary updates
			{
				updateCheckList(newValue);
				mCurrentValue = newValue;
			}
		}
	}


	private void updateCheckList(List<CheckListItem> list)
	{
		setVisibility(VISIBLE);

		mBuilding = true;

		int count = 0;
		for (final CheckListItem item : list)
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
			CheckListItem item1 = mCurrentValue.get(position1);
			CheckListItem item2 = mCurrentValue.get(position2);

			// swap items in the list
			mCurrentValue.set(position2, item1);
			mCurrentValue.set(position1, item2);

			if (mValues != null)
			{
				mAdapter.validateAndSet(mValues, mCurrentValue);
			}
		}
	}


	/**
	 * Inflates a new check list element view.
	 * 
	 * @return
	 */
	private View createItemView()
	{
		return mInflater.inflate(R.layout.checklist_field_view_element, mContainer, false);
	}


	@SuppressWarnings("deprecation")
	private void bindItemView(final View itemView, final CheckListItem item)
	{
		// set the checkbox status
		CheckBox checkbox = (CheckBox) itemView.findViewById(android.R.id.checkbox);
		// make sure we don't receive our own updates
		checkbox.setOnCheckedChangeListener(null);
		checkbox.setChecked(item.checked);
		checkbox.setOnCheckedChangeListener(CheckListFieldView.this);

		// configure the title
		final EditText text = (EditText) itemView.findViewById(android.R.id.title);
		text.setTextAppearance(getContext(), item.checked ? R.style.checklist_checked_item_text : R.style.dark_text);
		text.setText(item.text);
		text.setOnFocusChangeListener(new OnFocusChangeListener()
		{

			@Override
			public void onFocusChange(View v, boolean hasFocus)
			{
				itemView.findViewById(R.id.drag_handle).setVisibility(hasFocus ? View.INVISIBLE : View.VISIBLE);
				itemView.findViewById(R.id.remove_item).setVisibility(hasFocus ? View.VISIBLE : View.INVISIBLE);

				String newText = text.getText().toString();
				if (!hasFocus && !newText.equals(item.text) && mValues != null && !mCurrentValue.equals(mAdapter.get(mValues)))
				{
					item.text = newText;
					mAdapter.validateAndSet(mValues, mCurrentValue);
				}
			}
		});
		text.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
		text.setMaxLines(100);
		text.setHorizontallyScrolling(false);
		text.setOnEditorActionListener(new OnEditorActionListener()
		{

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
			{
				if (actionId == EditorInfo.IME_ACTION_NEXT)
				{
					int pos = mContainer.indexOfChild(itemView);
					insertEmptyItem(pos + 1);
					return true;
				}
				return false;
			}
		});
		text.setImeOptions(EditorInfo.IME_ACTION_NEXT);

		// add TextWatcher that commits any edits to the checklist
		text.addTextChangedListener(new TextWatcher()
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
				item.text = s.toString();
			}
		});

		/*
		 * enable memory leak workaround on android < 4.3: disable spell checker
		 */
		if (VERSION.SDK_INT < 18)
		{
			int inputType = text.getInputType();
			text.setInputType(inputType | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		}

		// bind the remove button
		View removeButton = itemView.findViewById(R.id.remove_item);
		removeButton.setTag(item);
		removeButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				mImm.hideSoftInputFromWindow(text.getWindowToken(), 0);
				mCurrentValue.remove(v.getTag());

				mAdapter.validateAndSet(mValues, mCurrentValue);
				mContainer.removeDragView(itemView);
			}
		});
	}


	/**
	 * Insert an empty item at the given position. Nothing will be inserted if the check list already contains an empty item at the given position. The new (or
	 * exiting) emtpy item will be focused and the keyboard will be opened.
	 * 
	 * @param pos
	 *            The position of the new item.
	 */
	private void insertEmptyItem(int pos)
	{
		if (mCurrentValue.size() > pos && mCurrentValue.get(pos).text.length() == 0)
		{
			// there already is an empty item at this pos focus it and return
			View view = mContainer.getChildAt(pos);
			focusTitle(view);
			return;
		}

		// create a new empty item
		CheckListItem item = new CheckListItem(false, "");
		mCurrentValue.add(pos, item);
		View newItem = createItemView();
		bindItemView(newItem, item);

		// append it to the list
		mContainer.addDragView(newItem, newItem.findViewById(R.id.drag_handle), pos);

		// update the values now
		mAdapter.validateAndSet(mValues, mCurrentValue);
		focusTitle(newItem);
	}


	@Override
	public void onClick(View v)
	{
		int id = v.getId();
		if (id == R.id.add_item)
		{
			insertEmptyItem(mCurrentValue.size());
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
