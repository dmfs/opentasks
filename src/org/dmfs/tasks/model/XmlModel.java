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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.dmfs.provider.tasks.TaskContract.Tasks;
import org.dmfs.tasks.R;
import org.dmfs.tasks.model.adapters.BooleanFieldAdapter;
import org.dmfs.tasks.model.adapters.FieldAdapter;
import org.dmfs.tasks.model.adapters.StringFieldAdapter;
import org.dmfs.tasks.model.contraints.UpdateAllDay;
import org.dmfs.tasks.model.layout.LayoutDescriptor;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ServiceInfo;
import android.content.res.XmlResourceParser;
import android.util.Log;


/**
 * A model that reads its definition from an XML resource.
 * 
 * The idea is to give a sync adapter control over what will displayed and how it looks like.
 * 
 * <pre>
 * &lt;TaskSource>
 * 
 * &lt;datakind kind="title" title="@string/title_title" hint="@string/title_hint">
 * 
 * &lt;datakind kind="location" title="@string/location_title" hint="@string/location_hint">
 * 
 * &lt;/TaskSource>
 * </pre>
 * 
 * At present the attributes are ignored.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public class XmlModel extends Model
{

	private final static String TAG = "org.dmfs.tasks.model.XmlModel";

	public final static String METADATA_TASKS = "org.dmfs.tasks.TASKS";
	public final static String NAMESPACE = "org.dmfs.tasks";

	private final static Map<String, FieldInflater> FIELD_INFLATER_MAP = new HashMap<String, FieldInflater>();

	private final PackageManager mPackageManager;
	private final String mPackageName;
	private final Context mModelContext;
	private final Context mContext;
	private boolean mInflated = false;


	public XmlModel(Context context, String packageName) throws ModelInflaterException
	{
		mContext = context;
		mPackageName = packageName;
		mPackageManager = context.getPackageManager();
		try
		{
			mModelContext = context.createPackageContext(packageName, 0);
		}
		catch (NameNotFoundException e)
		{
			throw new ModelInflaterException("No model definition found for package " + mPackageName);
		}

	}


	@SuppressWarnings("unchecked")
	@Override
	public void inflate() throws ModelInflaterException
	{
		if (mInflated)
		{
			return;
		}

		XmlResourceParser parser = getParser();

		if (parser == null)
		{
			throw new ModelInflaterException("No model definition found for package " + mPackageName);
		}

		try
		{
			// add a field for the list
			mFields.add(new FieldDescriptor(mContext, R.string.task_list, null, new StringFieldAdapter(Tasks.LIST_NAME)).setViewLayout(new LayoutDescriptor(
				R.layout.text_field_view_nodivider_large).setOption(LayoutDescriptor.OPTION_NO_TITLE, true).setOption(
				LayoutDescriptor.OPTION_USE_TASK_LIST_BACKGROUND_COLOR, true)));
			mFields.add(new FieldDescriptor(mContext, R.string.task_list, null, new StringFieldAdapter(Tasks.ACCOUNT_NAME)).setViewLayout(new LayoutDescriptor(
				R.layout.text_field_view_nodivider_small).setOption(LayoutDescriptor.OPTION_NO_TITLE, true).setOption(
				LayoutDescriptor.OPTION_USE_TASK_LIST_BACKGROUND_COLOR, true)));

			int eventType;

			// find first tag
			do
			{
				eventType = parser.next();
			} while (eventType != XmlResourceParser.END_DOCUMENT && eventType != XmlResourceParser.START_TAG);

			if (!"TaskSource".equals(parser.getName()) || !NAMESPACE.equals(parser.getNamespace()))
			{
				throw new ModelInflaterException("Invalid model definition in " + mPackageName + ": root node must be 'TaskSource'");
			}

			setAllowRecurrence(parser.getAttributeBooleanValue(null, "allowRecurrence", false));
			setAllowExceptions(parser.getAttributeBooleanValue(null, "allowExceptions", false));

			if (parser.getAttributeIntValue(null, "iconId", -1) != -1)
			{
				setIconId(parser.getAttributeIntValue(null, "iconId", -1));
			}
			if (parser.getAttributeIntValue(null, "labelId", -1) != -1)
			{
				setLabelId(parser.getAttributeIntValue(null, "labelId", -1));
			}

			int depth = 1;

			boolean hasDue = false;
			boolean hasStart = false;

			FieldDescriptor alldayDescriptor = null;

			eventType = parser.next();
			while (eventType != XmlResourceParser.END_DOCUMENT)
			{
				if (eventType == XmlResourceParser.START_TAG)
				{
					depth++;
					Log.v(TAG, "'" + parser.getName() + "'   " + depth);
					if (depth == 2 && "datakind".equals(parser.getName()) && NAMESPACE.equals(parser.getNamespace()))
					{
						// TODO: let inflateField step forward till the end tag
						FieldDescriptor descriptor = inflateField(parser);
						mFields.add(descriptor);

						FieldAdapter<?> fa = descriptor.getFieldAdapter();
						if (fa instanceof BooleanFieldAdapter && Tasks.IS_ALLDAY.equals(((BooleanFieldAdapter) fa).getFieldName()))
						{
							alldayDescriptor = descriptor;
						}
						else if (fa == TaskFieldAdapters.DUE)
						{
							hasDue = true;
						}
						else if (fa == TaskFieldAdapters.DTSTART)
						{
							hasStart = true;
						}
					}
				}
				else if (eventType == XmlResourceParser.END_TAG)
				{
					depth--;
				}
				else
				{
					throw new ModelInflaterException("Invalid tag " + parser.getName() + " " + mPackageName);
				}

				eventType = parser.next();
			}

			if (alldayDescriptor != null)
			{
				if (!hasDue)
				{
					// no due date descriptor present, ensure we update keep the all-day flag in sync
					((FieldAdapter<Boolean>) alldayDescriptor.getFieldAdapter()).addContraint(new UpdateAllDay(TaskFieldAdapters.DUE));
				}

				if (!hasStart)
				{
					// no start date descriptor present, ensure we update keep the all-day flag in sync
					((FieldAdapter<Boolean>) alldayDescriptor.getFieldAdapter()).addContraint(new UpdateAllDay(TaskFieldAdapters.DTSTART));
				}
			}

		}
		catch (Exception e)
		{
			throw new ModelInflaterException("Error during inflation of model for " + mPackageName, e);
		}

		mInflated = true;
	}


	private XmlResourceParser getParser()
	{
		try
		{
			PackageInfo info = mPackageManager.getPackageInfo(mPackageName, PackageManager.GET_SERVICES | PackageManager.GET_META_DATA);
			ServiceInfo[] sinfo = info.services;

			XmlResourceParser parser;
			for (ServiceInfo i : sinfo)
			{
				parser = i.loadXmlMetaData(mPackageManager, METADATA_TASKS);
				if (parser != null)
				{
					return parser;
				}
			}
		}
		catch (NameNotFoundException e)
		{
		}
		return null;
	}


	/**
	 * Inflate the current field.
	 * 
	 * @param parser
	 *            A parser that points to a datakind.
	 * @return A {@link FieldDescriptor} for the field.
	 * @throws IllegalDataKindException
	 */
	private FieldDescriptor inflateField(XmlResourceParser parser) throws IllegalDataKindException
	{
		String kind = parser.getAttributeValue(null, "kind");
		Log.v(TAG, "inflating kind " + kind);
		FieldInflater inflater = FIELD_INFLATER_MAP.get(kind);
		if (inflater == null)
		{
			throw new IllegalDataKindException("invalid data kind " + kind);
		}
		return inflater.inflate(mContext, mModelContext, parser);
	}

	/**
	 * Basic field inflater. It does some default inflating.
	 * 
	 * @author Marten Gajda <marten@dmfs.org>
	 */
	private static class FieldInflater
	{
		private final FieldAdapter<?> mAdapter;
		private final int mFieldTitle;
		private final int mDetailsLayout;
		private final int mEditLayout;
		private Map<String, Boolean> mDetailsLayoutOptions;
		private Map<String, Boolean> mEditLayoutOptions;


		public FieldInflater(FieldAdapter<?> adapter, int fieldTitle, int detailsLayout, int editLayout)
		{
			mAdapter = adapter;
			mFieldTitle = fieldTitle;
			mDetailsLayout = detailsLayout;
			mEditLayout = editLayout;
		}


		public FieldDescriptor inflate(Context context, Context modelContext, XmlResourceParser parser)
		{
			int titleId = parser.getAttributeResourceValue(null, "title_id", -1);
			FieldDescriptor descriptor;
			if (titleId != -1)
			{
				descriptor = new FieldDescriptor(modelContext, titleId, getContentType(), getFieldAdapter());
			}
			else
			{
				descriptor = new FieldDescriptor(context, getDefaultTitleId(), getContentType(), getFieldAdapter());
			}
			customizeDescriptor(context, modelContext, descriptor, parser);
			return descriptor;
		}


		public FieldAdapter<?> getFieldAdapter()
		{
			return mAdapter;
		}


		void customizeDescriptor(Context context, Context modelContext, FieldDescriptor descriptor, XmlResourceParser parser)
		{
			int hintId = parser.getAttributeResourceValue(null, "hint_id", -1);
			if (hintId != -1)
			{
				descriptor.setHint(modelContext.getString(hintId));
			}
			if (mDetailsLayout != -1)
			{
				LayoutDescriptor ld = new LayoutDescriptor(mDetailsLayout);
				if (mDetailsLayoutOptions != null)
				{
					for (Entry<String, Boolean> entry : mDetailsLayoutOptions.entrySet())
					{
						ld.setOption(entry.getKey(), entry.getValue());
					}
				}
				descriptor.setViewLayout(ld);

			}
			if (mEditLayout != -1)
			{
				LayoutDescriptor ld = new LayoutDescriptor(mEditLayout);
				if (mEditLayoutOptions != null)
				{
					for (Entry<String, Boolean> entry : mEditLayoutOptions.entrySet())
					{
						ld.setOption(entry.getKey(), entry.getValue());
					}
				}
				descriptor.setEditorLayout(ld);
			}
		}


		String getContentType()
		{
			return null;
		}


		int getDefaultTitleId()
		{
			return mFieldTitle;
		}


		public FieldInflater addDetailsLayoutOption(String key, boolean value)
		{
			if (mDetailsLayoutOptions == null)
			{
				mDetailsLayoutOptions = new HashMap<String, Boolean>(4);
			}
			mDetailsLayoutOptions.put(key, value);
			return this;
		}


		@SuppressWarnings("unused")
		public FieldInflater addEditLayoutOption(String key, boolean value)
		{
			if (mEditLayoutOptions == null)
			{
				mEditLayoutOptions = new HashMap<String, Boolean>(4);
			}
			mEditLayoutOptions.put(key, value);
			return this;
		}
	}

	static
	{
		/*
		 * Add definitions for all supported fields:
		 */

		FIELD_INFLATER_MAP.put("title", new FieldInflater(TaskFieldAdapters.TITLE, R.string.task_title, R.layout.text_field_view, R.layout.text_field_editor));
		FIELD_INFLATER_MAP.put("location", new FieldInflater(TaskFieldAdapters.LOCATION, R.string.task_location, R.layout.text_field_view,
			R.layout.text_field_editor));
		FIELD_INFLATER_MAP.put("description", new FieldInflater(TaskFieldAdapters.DESCRIPTION, R.string.task_description, R.layout.text_field_view,
			R.layout.text_field_editor));

		FIELD_INFLATER_MAP.put("dtstart", new FieldInflater(TaskFieldAdapters.DTSTART, R.string.task_start, R.layout.time_field_view,
			R.layout.time_field_editor));
		FIELD_INFLATER_MAP.put("due", new FieldInflater(TaskFieldAdapters.DUE, R.string.task_due, R.layout.time_field_view, R.layout.time_field_editor)
			.addDetailsLayoutOption(LayoutDescriptor.OPTION_TIME_FIELD_SHOW_ADD_BUTTONS, true));
		FIELD_INFLATER_MAP.put("completed", new FieldInflater(TaskFieldAdapters.COMPLETED, R.string.task_completed, R.layout.time_field_view,
			R.layout.time_field_editor));

		FIELD_INFLATER_MAP.put("percent_complete", new FieldInflater(TaskFieldAdapters.PERCENT_COMPLETE, R.string.task_percent_complete,
			R.layout.percentage_field_view, R.layout.percentage_field_editor));
		FIELD_INFLATER_MAP.put("status", new FieldInflater(TaskFieldAdapters.STATUS, R.string.task_status, R.layout.choices_field_view,
			R.layout.choices_field_editor)
		{
			@Override
			void customizeDescriptor(Context context, Context modelContext, FieldDescriptor descriptor, XmlResourceParser parser)
			{
				super.customizeDescriptor(context, modelContext, descriptor, parser);
				ArrayChoicesAdapter aca = new ArrayChoicesAdapter();
				aca.addHiddenChoice(null, context.getString(R.string.status_needs_action), null);
				aca.addChoice(Tasks.STATUS_NEEDS_ACTION, context.getString(R.string.status_needs_action), null);
				aca.addChoice(Tasks.STATUS_IN_PROCESS, context.getString(R.string.status_in_process), null);
				aca.addChoice(Tasks.STATUS_COMPLETED, context.getString(R.string.status_completed), null);
				aca.addChoice(Tasks.STATUS_CANCELLED, context.getString(R.string.status_cancelled), null);
				descriptor.setChoices(aca);
			}
		});
		FIELD_INFLATER_MAP.put("priority", new FieldInflater(TaskFieldAdapters.PRIORITY, R.string.task_priority, R.layout.choices_field_view,
			R.layout.choices_field_editor)
		{
			@Override
			void customizeDescriptor(Context context, Context modelContext, FieldDescriptor descriptor, XmlResourceParser parser)
			{
				super.customizeDescriptor(context, modelContext, descriptor, parser);

				ArrayChoicesAdapter aca = new ArrayChoicesAdapter();
				aca.addChoice(null, context.getString(R.string.priority_undefined), null);
				aca.addHiddenChoice(0, context.getString(R.string.priority_undefined), null);
				aca.addChoice(9, context.getString(R.string.priority_low), null);
				aca.addHiddenChoice(8, context.getString(R.string.priority_low), null);
				aca.addHiddenChoice(7, context.getString(R.string.priority_low), null);
				aca.addHiddenChoice(6, context.getString(R.string.priority_low), null);
				aca.addChoice(5, context.getString(R.string.priority_medium), null);
				aca.addHiddenChoice(4, context.getString(R.string.priority_high), null);
				aca.addHiddenChoice(3, context.getString(R.string.priority_high), null);
				aca.addHiddenChoice(2, context.getString(R.string.priority_high), null);
				aca.addChoice(1, context.getString(R.string.priority_high), null);
				descriptor.setChoices(aca);
			}
		});
		FIELD_INFLATER_MAP.put("classification", new FieldInflater(TaskFieldAdapters.CLASSIFICATION, R.string.task_classification, R.layout.choices_field_view,
			R.layout.choices_field_editor)
		{
			@Override
			void customizeDescriptor(Context context, Context modelContext, FieldDescriptor descriptor, XmlResourceParser parser)
			{
				super.customizeDescriptor(context, modelContext, descriptor, parser);

				ArrayChoicesAdapter aca = new ArrayChoicesAdapter();
				aca.addChoice(null, context.getString(R.string.classification_not_specified), null);
				aca.addChoice(Tasks.CLASSIFICATION_PUBLIC, context.getString(R.string.classification_public), null);
				aca.addChoice(Tasks.CLASSIFICATION_PRIVATE, context.getString(R.string.classification_private), null);
				aca.addChoice(Tasks.CLASSIFICATION_CONFIDENTIAL, context.getString(R.string.classification_confidential), null);
				descriptor.setChoices(aca);
			}
		});

		FIELD_INFLATER_MAP.put("url", new FieldInflater(TaskFieldAdapters.URL, R.string.task_url, R.layout.url_field_view, R.layout.url_field_editor));

		FIELD_INFLATER_MAP.put("allday", new FieldInflater(null, R.string.task_all_day, -1, R.layout.boolean_field_editor)
		{
			@Override
			public FieldAdapter<?> getFieldAdapter()
			{
				// return a non-static field adapter because we modify it
				return new BooleanFieldAdapter(Tasks.IS_ALLDAY);
			}
		});

		FIELD_INFLATER_MAP.put("timezone", new FieldInflater(TaskFieldAdapters.TIMEZONE, R.string.task_timezone, -1, R.layout.choices_field_editor)
		{
			@Override
			void customizeDescriptor(Context context, Context modelContext, FieldDescriptor descriptor, XmlResourceParser parser)
			{
				super.customizeDescriptor(context, modelContext, descriptor, parser);
				TimeZoneChoicesAdapter tzaca = new TimeZoneChoicesAdapter(context);
				descriptor.setChoices(tzaca);
			}

		});

	}
}
