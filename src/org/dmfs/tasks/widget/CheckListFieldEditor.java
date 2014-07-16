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

import org.dmfs.tasks.R;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;


/**
 * Editor widget for checklists. It allows to switch between regular plain text and checklist mode. In checklist mode every line will be prepended by a check
 * box.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public class CheckListFieldEditor extends AbstractFieldEditor implements OnCheckedChangeListener, OnFocusChangeListener
{
	private StringFieldAdapter mAdapter;
	private ViewGroup mContainer;
	private EditText mText;

	private String mCurrentValue;
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
		if (mText != null)
		{
			/*
			 * enable memory leak workaround: disable spell checker
			 */
			int inputType = mText.getInputType();
			mText.setInputType(inputType | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		}

		mContainer = (ViewGroup) findViewById(R.id.checklist);
	}


	@Override
	public void setFieldDescription(FieldDescriptor descriptor, LayoutOptions layoutOptions)
	{
		super.setFieldDescription(descriptor, layoutOptions);
		mAdapter = (StringFieldAdapter) descriptor.getFieldAdapter();
	}


	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
	{
		if (!mBuilding && mValues != null)
		{
			updateValues();
		}
	}


	@Override
	public void onFocusChange(View v, boolean hasFocus)
	{
		if (!hasFocus /* update only when loosing the focus */&& !mBuilding && mValues != null)
		{
			updateValues();
			buildCheckList(mCurrentValue);
		}
	}


	@Override
	public void onContentLoaded(ContentSet contentSet)
	{
		super.onContentLoaded(contentSet);
		String newValue = mAdapter.get(contentSet);
		buildCheckList(newValue);
	}


	@Override
	public void onContentChanged(ContentSet contentSet)
	{
		if (mValues != null)
		{
			String newValue = mAdapter.get(mValues);
			if (!TextUtils.equals(mCurrentValue, newValue)) // don't trigger unnecessary updates
			{
				buildCheckList(newValue);
				mCurrentValue = newValue;
			}
		}
	}


	@Override
	public void updateValues()
	{
		final String newText = getCheckListDescription();
		final String oldText = mAdapter.get(mValues);

		if (!TextUtils.equals(newText, oldText)) // don't trigger unnecessary updates
		{
			mAdapter.validateAndSet(mValues, newText);
		}
	}


	private String getCheckListDescription()
	{
		StringBuilder builder = new StringBuilder(4 * 1024);
		String descriptionText = mText.getText().toString();
		builder.append(descriptionText);

		int count = mContainer.getChildCount();

		boolean first = descriptionText.length() == 0;
		for (int i = 0; i < count; ++i)
		{
			CheckItemTag tag = (CheckItemTag) mContainer.getChildAt(i).getTag();
			String text = tag.editText.getText().toString();
			if (text.length() > 0)
			{
				if (first)
				{
					first = false;
				}
				else
				{
					builder.append("\n");
				}

				builder.append(tag.checkbox.isChecked() ? "[x] " : "[ ] ");
				builder.append(text);
			}
		}
		return builder.toString();
	}


	private void buildCheckList(String text)
	{
		mBuilding = true;

		String[] items;
		if (text != null && text.length() > 0)
		{
			items = text.split("\n");
		}
		else
		{
			items = new String[0];
		}

		int count = 0;
		boolean inCheckListMode = false;
		int checkListStart = 0;

		for (int i = 0; i < items.length; ++i)
		{
			String item = items[i];
			boolean checked = false;
			if (item.startsWith("[x]") || item.startsWith("[X]"))
			{
				checked = true;
				item = item.substring(3).trim();
				inCheckListMode = true;
			}
			else if (item.startsWith("[ ]"))
			{
				item = item.substring(3).trim();
				inCheckListMode = true;
			}
			else if (!inCheckListMode)
			{
				checkListStart += item.length();
				if (i < items.length - 1)
				{
					// account for the removed new line character
					++checkListStart;
				}
				continue;
			}

			if (item.length() == 0)
			{
				continue;
			}

			ViewGroup vg = (ViewGroup) mContainer.getChildAt(count);
			CheckItemTag tag;
			if (vg != null)
			{
				tag = (CheckItemTag) vg.getTag();
				if (tag == null)
				{
					// this might happen for the initial element
					tag = new CheckItemTag(vg);
				}
			}
			else
			{
				vg = (ViewGroup) mInflater.inflate(R.layout.checklist_field_editor_element, mContainer, false);
				tag = new CheckItemTag(vg);
				mContainer.addView(vg);
			}

			tag.setItem(checked, item, false);

			++count;
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
				tag = new CheckItemTag(vg);
			}
		}
		else
		{
			vg = (ViewGroup) mInflater.inflate(R.layout.checklist_field_editor_element, mContainer, false);
			tag = new CheckItemTag(vg);
			mContainer.addView(vg);
		}

		tag.setItem(false, "", true);

		++count;

		while (mContainer.getChildCount() > count)
		{
			mContainer.removeViewAt(count);
		}

		mText.setText(text != null ? text.substring(0, checkListStart).trim() : null);
		mBuilding = false;
	}

	private class CheckItemTag
	{
		public final CheckBox checkbox;
		public final EditText editText;
		private boolean mIsLast;


		public CheckItemTag(ViewGroup viewGroup)
		{
			checkbox = (CheckBox) viewGroup.findViewById(android.R.id.checkbox);
			editText = (EditText) viewGroup.findViewById(android.R.id.text1);
			viewGroup.setTag(this);

			int inputType = editText.getInputType();
			editText.setInputType(inputType | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

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
						updateValues();
						buildCheckList(mCurrentValue);
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
