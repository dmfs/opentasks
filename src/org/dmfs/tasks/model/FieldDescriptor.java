/*
 * FieldDescriptor.java
 *
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

package org.dmfs.tasks.model;

import org.dmfs.tasks.R;
import org.dmfs.tasks.model.adapters.FieldAdapter;
import org.dmfs.tasks.model.layout.LayoutDescriptor;
import org.dmfs.tasks.widget.AbstractFieldEditor;
import org.dmfs.tasks.widget.AbstractFieldView;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.Adapter;


/**
 * A FieldDescriptor holds all information about a certain task property.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class FieldDescriptor
{

	private final static LayoutDescriptor DEFAULT_VIEW_LAYOUT = new LayoutDescriptor(R.layout.text_field_view);
	private final static LayoutDescriptor DEFAULT_EDIT_LAYOUT = new LayoutDescriptor(R.layout.text_field_editor);

	private final String mTitle;
	private String mHint;
	private final String mContentType;
	private final FieldAdapter<?> mFieldAdapter;
	private boolean mAllowNull = true;
	private IChoicesAdapter mChoices = null;
	private LayoutDescriptor mEditLayout = null;
	private LayoutDescriptor mViewLayout = null;


	/**
	 * Constructor for a new field description.
	 * 
	 * @param context
	 *            The context holding the title resource.
	 * @param titleId
	 *            The id of the title resource.
	 * @param fieldAdapter
	 *            A {@link FieldAdapter} for this field.
	 */
	public FieldDescriptor(Context context, int titleId, FieldAdapter<?> fieldAdapter)
	{
		this(context.getString(titleId), null, fieldAdapter);
	}


	/**
	 * Constructor for a new field description.
	 * 
	 * @param context
	 *            The context holding the title resource.
	 * @param titleId
	 *            The id of the title resource.
	 * @param contentType
	 *            The contentType of this property.
	 * @param fieldAdapter
	 *            A {@link FieldAdapter} for this field.
	 */
	public FieldDescriptor(Context context, int titleId, String contentType, FieldAdapter<?> fieldAdapter)
	{
		this(context.getString(titleId), contentType, fieldAdapter);
	}


	/**
	 * Constructor for a new field description.
	 * 
	 * @param title
	 *            A string for the title of this field.
	 * @param fieldAdapter
	 *            A {@link FieldAdapter} for this field.
	 */
	public FieldDescriptor(String title, FieldAdapter<?> fieldAdapter)
	{
		this(title, null, fieldAdapter);
	}


	/**
	 * Constructor for a new data field description (a field with a content type).
	 * 
	 * @param title
	 *            A string for the title of this field.
	 * @param contentType
	 *            The content type of the field.
	 * @param fieldAdapter
	 *            A {@link FieldAdapter} for this field.
	 */
	public FieldDescriptor(String title, String contentType, FieldAdapter<?> fieldAdapter)
	{
		if (fieldAdapter == null)
		{
			throw new NullPointerException("fieldAdapter must not be null!");
		}
		mTitle = title;
		mContentType = contentType;
		mHint = title; // use title as hint by default
		mFieldAdapter = fieldAdapter;
	}


	/**
	 * Returns the title of this field.
	 * 
	 * @return The title.
	 */
	public String getTitle()
	{
		return mTitle;
	}


	/**
	 * Return the content type for this field.
	 * 
	 * @return The content type or {@code null} if this field has no content type.
	 */
	public String getContentType()
	{
		return mContentType;
	}


	/**
	 * Returns the hint for this field.
	 * 
	 * @return The hint.
	 */
	public String getHint()
	{
		return mHint;
	}


	/**
	 * Sets the hint for this field.
	 * 
	 * @param hint
	 *            The hint for this field.
	 * @return This instance.
	 */
	public FieldDescriptor setHind(String hint)
	{
		mHint = hint;
		return this;
	}


	/**
	 * Returns a {@link FieldAdapter} for this field.
	 * 
	 * @return The {@link FieldAdapter} instance. Will never be {@code null}.
	 */
	public FieldAdapter<?> getFieldAdapter()
	{
		return mFieldAdapter;
	}


	/**
	 * Set whether this field allows {@code null} values.
	 * 
	 * @param allow
	 *            {@code true} if {@code null} values are allowed, {@code false} otherwise (default).
	 * @return This instance.
	 */
	public FieldDescriptor allowNull(boolean allow)
	{
		mAllowNull = allow;
		return this;
	}


	/**
	 * Return whether {@code null} is an allowed value for this field.
	 * 
	 * @return {@code true} if {@code null} values are allowed, {@code false} otherwise (default).
	 */
	public boolean nullAllowed()
	{
		return mAllowNull;
	}


	/**
	 * Return a choices adapter for this field.
	 * 
	 * @return An {@link Adapter}.
	 */
	public IChoicesAdapter getChoices()
	{
		return mChoices;
	}


	/**
	 * Set an {@link IChoicesAdapter} for this field.
	 * 
	 * @param choices
	 *            An {@link Adapter} or {@code null} to disable choices for this field.
	 * @return This instance.
	 */
	public FieldDescriptor setChoices(IChoicesAdapter choices)
	{
		mChoices = choices;
		return this;
	}


	public AbstractFieldEditor getEditorView(LayoutInflater inflater)
	{
		if (mEditLayout == null)
		{
			return (AbstractFieldEditor) DEFAULT_EDIT_LAYOUT.inflate(inflater);
		}
		else
		{
			return (AbstractFieldEditor) mEditLayout.inflate(inflater);
		}
	}


	public AbstractFieldView getViewView(LayoutInflater inflater)
	{
		if (mViewLayout == null)
		{
			return (AbstractFieldView) DEFAULT_VIEW_LAYOUT.inflate(inflater);
		}
		else
		{
			return (AbstractFieldView) mViewLayout.inflate(inflater);
		}
	}


	void setEditorLayout(LayoutDescriptor ld)
	{
		mEditLayout = ld;
	}


	void setViewLayout(LayoutDescriptor ld)
	{
		mViewLayout = ld;
	}
}
