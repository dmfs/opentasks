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
import org.dmfs.xmlobjects.ElementDescriptor;
import org.dmfs.xmlobjects.QualifiedName;
import org.dmfs.xmlobjects.builder.AbstractObjectBuilder;
import org.dmfs.xmlobjects.pull.ParserContext;
import org.dmfs.xmlobjects.pull.Recyclable;
import org.dmfs.xmlobjects.pull.XmlObjectPull;
import org.dmfs.xmlobjects.pull.XmlObjectPullParserException;
import org.dmfs.xmlobjects.pull.XmlPath;

import android.accounts.AccountManager;
import android.accounts.AuthenticatorDescription;
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

	public final static QualifiedName ATTR_KIND = QualifiedName.get("kind");

	/**
	 * This is a workaround for the transition from a combined description/checklist field to two separate fields.
	 * 
	 * TODO: remove once the new versions of CalDAV-Sync and SmoothSync are in use
	 */
	public final static QualifiedName ATTR_HIDECHECKLIST = QualifiedName.get("hideCheckList");

	private final static Map<String, FieldInflater> FIELD_INFLATER_MAP = new HashMap<String, FieldInflater>();

	private String mAccountLabel;

	/**
	 * POJO that stores the attributes of a &lt;datakind> element.
	 */
	private static class DataKind implements Recyclable
	{
		public String datakind;
		public int titleId = -1;
		public int hintId = -1;

		/**
		 * This is a workaround for the transition from a combined description/checklist field to two separate fields.
		 * 
		 * TODO: remove once the new versions of CalDAV-Sync and SmoothSync are in use
		 */
		public boolean hideCheckList = false;


		@Override
		public void recycle()
		{
			datakind = null;
			titleId = -1;
			hintId = -1;
			hideCheckList = false;
		}
	}

	/**
	 * POJO to store the state of the model parser.
	 */
	private static class ModelParserState
	{
		public boolean hasDue = false;
		public boolean hasStart = false;
		public FieldDescriptor alldayDescriptor = null;
	}

	private static final ElementDescriptor<XmlModel> XML_MODEL_DESCRIPTOR = ElementDescriptor.register(QualifiedName.get(NAMESPACE, "TaskSource"),
		new AbstractObjectBuilder<XmlModel>()
		{
			public XmlModel get(ElementDescriptor<XmlModel> descriptor, XmlModel recycle, ParserContext context) throws XmlObjectPullParserException
			{
				// ensure we have a state object
				context.setState(new ModelParserState());

				if (recycle == null)
				{
					throw new IllegalArgumentException("you must provide the XML model to populate as the object to recycle");
				}
				return recycle;
			};


			public XmlModel update(ElementDescriptor<XmlModel> descriptor, XmlModel object, QualifiedName attribute, String value, ParserContext context)
				throws XmlObjectPullParserException
			{
				// for now we ignore all attributes
				return object;
			};


			@SuppressWarnings("unchecked")
			public <V extends Object> XmlModel update(ElementDescriptor<XmlModel> descriptor, XmlModel object, ElementDescriptor<V> childDescriptor, V child,
				ParserContext context) throws XmlObjectPullParserException
			{
				if (childDescriptor == XML_DATAKIND)
				{
					DataKind datakind = (DataKind) child;
					FieldInflater inflater = FIELD_INFLATER_MAP.get(datakind.datakind);

					if (inflater != null)
					{
						Context appContext = object.getContext();
						FieldDescriptor fieldDescriptor = inflater.inflate(appContext, object.mModelContext, datakind);
						object.addField(fieldDescriptor);

						ModelParserState state = (ModelParserState) context.getState();

						if ("allday".equals(datakind.datakind))
						{
							state.alldayDescriptor = fieldDescriptor;
						}
						else if ("due".equals(datakind.datakind))
						{
							state.hasDue = true;
						}
						else if ("dtstart".equals(datakind.datakind))
						{
							state.hasStart = true;
						}
						else if ("description".equals(datakind.datakind) && !datakind.hideCheckList)
						{
							Log.i(TAG, "found old description data kind, adding checklist");
							object.addField(FIELD_INFLATER_MAP.get("checklist").inflate(appContext, object.mModelContext, datakind));
						}
					}
					// we don't need the datakind object anymore, so recycle it
					context.recycle((ElementDescriptor<DataKind>) childDescriptor, datakind);

				}
				return object;
			};


			@SuppressWarnings("unchecked")
			public XmlModel finish(ElementDescriptor<XmlModel> descriptor, XmlModel object, ParserContext context) throws XmlObjectPullParserException
			{
				ModelParserState state = (ModelParserState) context.getState();
				if (state.alldayDescriptor != null)
				{
					// add UpdateAllDay constraint of due or start fields are missing to keep the values in sync with the allday flag
					if (!state.hasDue)
					{
						((FieldAdapter<Boolean>) state.alldayDescriptor.getFieldAdapter()).addContraint(new UpdateAllDay(TaskFieldAdapters.DUE));
					}
					if (!state.hasStart)
					{
						((FieldAdapter<Boolean>) state.alldayDescriptor.getFieldAdapter()).addContraint(new UpdateAllDay(TaskFieldAdapters.DTSTART));
					}
				}
				return object;
			};
		});

	private final static ElementDescriptor<DataKind> XML_DATAKIND = ElementDescriptor.register(QualifiedName.get(NAMESPACE, "datakind"),
		new AbstractObjectBuilder<DataKind>()
		{
			public DataKind get(ElementDescriptor<DataKind> descriptor, DataKind recycle, ParserContext context) throws XmlObjectPullParserException
			{
				if (recycle != null)
				{
					recycle.recycle();
					return recycle;
				}

				return new DataKind();
			};


			public DataKind update(ElementDescriptor<DataKind> descriptor, DataKind object, QualifiedName attribute, String value, ParserContext context)
				throws XmlObjectPullParserException
			{
				if (attribute == ATTR_KIND)
				{
					object.datakind = value;
				}
				else if (attribute == ATTR_HIDECHECKLIST)
				{
					object.hideCheckList = Boolean.parseBoolean(value);
				}
				return object;
			};
		});

	private final PackageManager mPackageManager;
	private final String mPackageName;
	private final Context mModelContext;
	private boolean mInflated = false;


	public XmlModel(Context context, AuthenticatorDescription authenticator) throws ModelInflaterException
	{
		super(context, authenticator.type);
		mPackageName = authenticator.packageName;
		mPackageManager = context.getPackageManager();
		try
		{
			mModelContext = context.createPackageContext(authenticator.packageName, 0);
			AccountManager am = AccountManager.get(context);
			mAccountLabel = mModelContext.getString(authenticator.labelId);
		}
		catch (NameNotFoundException e)
		{
			throw new ModelInflaterException("No model definition found for package " + mPackageName);
		}

	}


	@Override
	public String getAccountLabel()
	{
		return mAccountLabel;
	}


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

		Context context = getContext();

		try
		{
			// add a field for the list
			addField(new FieldDescriptor(context, R.id.task_field_list_color, R.string.task_list, null, TaskFieldAdapters.LIST_COLOR)
				.setViewLayout(DefaultModel.LIST_COLOR_VIEW).setEditorLayout(DefaultModel.LIST_COLOR_VIEW).setNoAutoAdd(true));
			addField(new FieldDescriptor(context, R.id.task_field_list_name, R.string.task_list, null, new StringFieldAdapter(Tasks.LIST_NAME)).setViewLayout(
				new LayoutDescriptor(R.layout.text_field_view_nodivider_large).setOption(LayoutDescriptor.OPTION_NO_TITLE, true)).setNoAutoAdd(true));
			addField(new FieldDescriptor(context, R.id.task_field_account_name, R.string.task_list, null, new StringFieldAdapter(Tasks.ACCOUNT_NAME))
				.setViewLayout(new LayoutDescriptor(R.layout.text_field_view_nodivider_small).setOption(LayoutDescriptor.OPTION_NO_TITLE, true)).setNoAutoAdd(
					true));

			XmlObjectPull pullParser = new XmlObjectPull(parser);
			if (pullParser.pull(XML_MODEL_DESCRIPTOR, this, new XmlPath()) == null)
			{
				throw new ModelInflaterException("Invalid model definition in " + mPackageName + ": root node must be 'TaskSource'");
			}

			// task list name
			addField(new FieldDescriptor(context, R.id.task_field_list_and_account_name, R.string.task_list, null, TaskFieldAdapters.LIST_AND_ACCOUNT_NAME)
				.setViewLayout(DefaultModel.TEXT_VIEW_NO_LINKS).setIcon(R.drawable.ic_detail_list));

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
	 * Basic field inflater. It does some default inflating, but also allows customization.
	 * 
	 * @author Marten Gajda <marten@dmfs.org>
	 */
	private static class FieldInflater
	{
		private final FieldAdapter<?> mAdapter;
		private final int mFieldTitle;
		private final int mDetailsLayout;
		private final int mEditLayout;
		private final int mIconId;
		private final int mFieldId;
		private Map<String, Object> mDetailsLayoutOptions;
		private Map<String, Object> mEditLayoutOptions;


		public FieldInflater(FieldAdapter<?> adapter, int fieldId, int fieldTitle, int detailsLayout, int editLayout, int iconId)
		{
			mAdapter = adapter;
			mFieldTitle = fieldTitle;
			mDetailsLayout = detailsLayout;
			mEditLayout = editLayout;
			mIconId = iconId;
			mFieldId = fieldId;
		}


		public FieldDescriptor inflate(Context context, Context modelContext, DataKind kind)
		{
			int titleId = kind.titleId;
			FieldDescriptor descriptor;
			if (titleId != -1)
			{
				descriptor = new FieldDescriptor(modelContext, mFieldId, titleId, getContentType(), getFieldAdapter(kind));
			}
			else
			{
				descriptor = new FieldDescriptor(context, mFieldId, getDefaultTitleId(), getContentType(), getFieldAdapter(kind));
			}

			if (mIconId != 0)
			{
				descriptor.setIcon(mIconId);
			}

			customizeDescriptor(context, modelContext, descriptor, kind);
			return descriptor;
		}


		public FieldAdapter<?> getFieldAdapter(DataKind kind)
		{
			return mAdapter;
		}


		void customizeDescriptor(Context context, Context modelContext, FieldDescriptor descriptor, DataKind kind)
		{
			int hintId = kind.hintId;
			if (hintId != -1)
			{
				descriptor.setHint(modelContext.getString(hintId));
			}
			if (mDetailsLayout != -1)
			{
				LayoutDescriptor ld = new LayoutDescriptor(mDetailsLayout);
				if (mDetailsLayoutOptions != null)
				{
					for (Entry<String, Object> entry : mDetailsLayoutOptions.entrySet())
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
					for (Entry<String, Object> entry : mEditLayoutOptions.entrySet())
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
				mDetailsLayoutOptions = new HashMap<String, Object>(4);
			}
			mDetailsLayoutOptions.put(key, value);
			return this;
		}


		@SuppressWarnings("unused")
		public FieldInflater addEditLayoutOption(String key, boolean value)
		{
			if (mEditLayoutOptions == null)
			{
				mEditLayoutOptions = new HashMap<String, Object>(4);
			}
			mEditLayoutOptions.put(key, value);
			return this;
		}


		public FieldInflater addDetailsLayoutOption(String key, int value)
		{
			if (mDetailsLayoutOptions == null)
			{
				mDetailsLayoutOptions = new HashMap<String, Object>(4);
			}
			mDetailsLayoutOptions.put(key, value);
			return this;
		}


		@SuppressWarnings("unused")
		public FieldInflater addEditLayoutOption(String key, int value)
		{
			if (mEditLayoutOptions == null)
			{
				mEditLayoutOptions = new HashMap<String, Object>(4);
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

		FIELD_INFLATER_MAP.put("title", new FieldInflater(TaskFieldAdapters.TITLE, R.id.task_field_title, R.string.task_title, -1, R.layout.text_field_editor,
			R.drawable.ic_detail_description).addEditLayoutOption(LayoutDescriptor.OPTION_MULTILINE, false));
		FIELD_INFLATER_MAP.put("location", new FieldInflater(TaskFieldAdapters.LOCATION, R.id.task_field_location, R.string.task_location,
			R.layout.text_field_view, R.layout.text_field_editor, R.drawable.ic_detail_location).addDetailsLayoutOption(LayoutDescriptor.OPTION_LINKIFY, 0));
		FIELD_INFLATER_MAP.put("description", new FieldInflater(TaskFieldAdapters.DESCRIPTION, R.id.task_field_description, R.string.task_description,
			R.layout.text_field_view, R.layout.text_field_editor, R.drawable.ic_detail_description));
		FIELD_INFLATER_MAP.put("checklist", new FieldInflater(TaskFieldAdapters.CHECKLIST, R.id.task_field_checklist, R.string.task_checklist,
			R.layout.checklist_field_view, R.layout.checklist_field_editor, R.drawable.ic_detail_checklist));

		FIELD_INFLATER_MAP.put("dtstart", new FieldInflater(TaskFieldAdapters.DTSTART, R.id.task_field_dtstart, R.string.task_start, R.layout.time_field_view,
			R.layout.time_field_editor, R.drawable.ic_detail_start));
		FIELD_INFLATER_MAP.put("due", new FieldInflater(TaskFieldAdapters.DUE, R.id.task_field_due, R.string.task_due, -1, R.layout.time_field_editor,
			R.drawable.ic_detail_due).addDetailsLayoutOption(LayoutDescriptor.OPTION_TIME_FIELD_SHOW_ADD_BUTTONS, true));
		FIELD_INFLATER_MAP.put("completed", new FieldInflater(TaskFieldAdapters.COMPLETED, R.id.task_field_completed, R.string.task_completed,
			R.layout.time_field_view, R.layout.time_field_editor, R.drawable.ic_detail_completed));

		FIELD_INFLATER_MAP.put("percent_complete", new FieldInflater(TaskFieldAdapters.PERCENT_COMPLETE, R.id.task_field_percent_complete,
			R.string.task_percent_complete, R.layout.percentage_field_view, R.layout.percentage_field_editor, R.drawable.ic_detail_progress));
		FIELD_INFLATER_MAP.put("status", new FieldInflater(TaskFieldAdapters.STATUS, R.id.task_field_status, R.string.task_status, R.layout.choices_field_view,
			R.layout.choices_field_editor, R.drawable.ic_detail_status)
		{
			@Override
			void customizeDescriptor(Context context, Context modelContext, FieldDescriptor descriptor, DataKind kind)
			{
				super.customizeDescriptor(context, modelContext, descriptor, kind);
				ArrayChoicesAdapter aca = new ArrayChoicesAdapter();
				aca.addHiddenChoice(null, context.getString(R.string.status_needs_action), null);
				aca.addChoice(Tasks.STATUS_NEEDS_ACTION, context.getString(R.string.status_needs_action), null);
				aca.addChoice(Tasks.STATUS_IN_PROCESS, context.getString(R.string.status_in_process), null);
				aca.addChoice(Tasks.STATUS_COMPLETED, context.getString(R.string.status_completed), null);
				aca.addChoice(Tasks.STATUS_CANCELLED, context.getString(R.string.status_cancelled), null);
				descriptor.setChoices(aca);
			}
		});
		FIELD_INFLATER_MAP.put("priority", new FieldInflater(TaskFieldAdapters.PRIORITY, R.id.task_field_priority, R.string.task_priority,
			R.layout.choices_field_view, R.layout.choices_field_editor, R.drawable.ic_detail_priority)
		{
			@Override
			void customizeDescriptor(Context context, Context modelContext, FieldDescriptor descriptor, DataKind kind)
			{
				super.customizeDescriptor(context, modelContext, descriptor, kind);

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
		FIELD_INFLATER_MAP.put("classification", new FieldInflater(TaskFieldAdapters.CLASSIFICATION, R.id.task_field_classification,
			R.string.task_classification, R.layout.choices_field_view, R.layout.choices_field_editor, R.drawable.ic_detail_visibility)
		{
			@Override
			void customizeDescriptor(Context context, Context modelContext, FieldDescriptor descriptor, DataKind kind)
			{
				super.customizeDescriptor(context, modelContext, descriptor, kind);

				ArrayChoicesAdapter aca = new ArrayChoicesAdapter();
				aca.addChoice(null, context.getString(R.string.classification_not_specified), null);
				aca.addChoice(Tasks.CLASSIFICATION_PUBLIC, context.getString(R.string.classification_public), null);
				aca.addChoice(Tasks.CLASSIFICATION_PRIVATE, context.getString(R.string.classification_private), null);
				aca.addChoice(Tasks.CLASSIFICATION_CONFIDENTIAL, context.getString(R.string.classification_confidential), null);
				descriptor.setChoices(aca);
			}
		});

		FIELD_INFLATER_MAP.put("url", new FieldInflater(TaskFieldAdapters.URL, R.id.task_field_url, R.string.task_url, R.layout.url_field_view,
			R.layout.url_field_editor, R.drawable.ic_detail_url));

		FIELD_INFLATER_MAP.put("allday", new FieldInflater(null, R.id.task_field_all_day, R.string.task_all_day, -1, R.layout.boolean_field_editor,
			R.drawable.ic_detail_due)
		{
			@Override
			public FieldAdapter<?> getFieldAdapter(DataKind kind)
			{
				// return a non-static field adapter because we modify it
				return new BooleanFieldAdapter(Tasks.IS_ALLDAY);
			}
		});

		FIELD_INFLATER_MAP.put("timezone", new FieldInflater(TaskFieldAdapters.TIMEZONE, R.id.task_field_timezone, R.string.task_timezone, -1,
			R.layout.choices_field_editor, R.drawable.ic_detail_due)
		{
			@Override
			void customizeDescriptor(Context context, Context modelContext, FieldDescriptor descriptor, DataKind kind)
			{
				super.customizeDescriptor(context, modelContext, descriptor, kind);
				TimeZoneChoicesAdapter tzaca = new TimeZoneChoicesAdapter(context);
				descriptor.setChoices(tzaca);
			}

		});

	}
}
