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

package org.dmfs.tasks.model;

import org.dmfs.tasks.model.adapters.FieldAdapter;
import org.dmfs.tasks.model.layout.LayoutDescriptor;
import org.dmfs.tasks.widget.AbstractFieldEditor;
import org.dmfs.tasks.widget.AbstractFieldView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;


/**
 * A FieldDescriptor holds all information about a certain task property or attribute.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class FieldDescriptor
{

	/**
	 * The title of the field.
	 */
	private final String mTitle;

	/**
	 * A hint. This is not used by all editors.
	 */
	private String mHint;

	/**
	 * The content type of an extended property.
	 * 
	 * This is currently unused and is subject to change in upcoming version.
	 */
	private final String mContentType;

	/**
	 * The {@link FieldAdapter} that knows how to load the values of this field form a {@link ContentSet}.
	 */
	private final FieldAdapter<?> mFieldAdapter;

	/**
	 * A class implementing an {@link IChoicesAdapter} that provides the choices for this field. Can be <code>null</code> if this field doesn't support choices.
	 */
	private IChoicesAdapter mChoices = null;

	/**
	 * A {@link LayoutDescriptor} that provides the layout of an editor for this field.
	 */
	private LayoutDescriptor mEditLayout = null;

	/**
	 * A {@link LayoutDescriptor} that provides the layout of a detail view for this field.
	 */
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
	public FieldDescriptor setHint(String hint)
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
	 * Return a choices adapter for this field.
	 * 
	 * @return An {@link IChoicesAdapter} or <code>null</code> if this field doesn't support choice.
	 */
	public IChoicesAdapter getChoices()
	{
		return mChoices;
	}


	/**
	 * Set an {@link IChoicesAdapter} for this field.
	 * 
	 * @param choices
	 *            An {@link IChoicesAdapter} or <code>null</code> to disable choices for this field.
	 * @return This instance.
	 */
	public FieldDescriptor setChoices(IChoicesAdapter choices)
	{
		mChoices = choices;
		return this;
	}


	/**
	 * Returns an inflated view to edit this field. This method takes a parent (that can be <code>null</code>) but it doesn't attach the editor to the parent.
	 * 
	 * @param inflater
	 *            A {@link LayoutInflater}.
	 * @param parent
	 *            The parent {@link ViewGroup} of the editor.
	 * @return An {@link AbstractFieldEditor} that can edit this field or <code>null</code> if this field is not editable.
	 */
	public AbstractFieldEditor getEditorView(LayoutInflater inflater, ViewGroup parent)
	{
		if (mEditLayout == null)
		{
			return null;
		}

		AbstractFieldEditor view = (AbstractFieldEditor) mEditLayout.inflate(inflater, parent, false);
		view.setFieldDescription(this, mEditLayout.getOptions());
		return view;
	}


	/**
	 * Returns an inflated view to edit this field.
	 * 
	 * @param inflater
	 *            A {@link LayoutInflater}.
	 * @return An {@link AbstractFieldEditor} that can edit this field or <code>null</code> if this field is not editable.
	 */
	public AbstractFieldEditor getEditorView(LayoutInflater inflater)
	{
		return getEditorView(inflater, null);
	}


	/**
	 * Returns an inflated view to show this field. This method takes a parent (that can be <code>null</code>) but it doesn't attach the detail view to the
	 * parent.
	 * 
	 * @param inflater
	 *            A {@link LayoutInflater}.
	 * @param parent
	 *            The parent {@link ViewGroup} of the detail view.
	 * @return An {@link AbstractFieldView} that can edit this field or <code>null</code> if this field can be viewed.
	 */
	public AbstractFieldView getDetailView(LayoutInflater inflater, ViewGroup parent)
	{
		if (mViewLayout == null)
		{
			return null;
		}

		AbstractFieldView view = (AbstractFieldView) mViewLayout.inflate(inflater, parent, false);
		view.setFieldDescription(this, mViewLayout.getOptions());
		return view;
	}


	/**
	 * Returns an inflated view to show this field.
	 * 
	 * @param inflater
	 *            A {@link LayoutInflater}.
	 */
	public AbstractFieldView getDetailView(LayoutInflater inflater)
	{
		if (mViewLayout == null)
		{
			return null;
		}

		AbstractFieldView view = (AbstractFieldView) mViewLayout.inflate(inflater);
		view.setFieldDescription(this, mViewLayout.getOptions());
		return view;
	}


	FieldDescriptor setEditorLayout(LayoutDescriptor layoutDescriptor)
	{
		mEditLayout = layoutDescriptor;
		return this;
	}


	FieldDescriptor setViewLayout(LayoutDescriptor layoutDescriptor)
	{
		mViewLayout = layoutDescriptor;
		return this;
	}
}
