/*
 * XmlModel.java
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

import java.util.HashMap;
import java.util.Map;

import org.dmfs.tasks.R;
import org.dmfs.tasks.model.adapters.FieldAdapter;
import org.dmfs.tasks.model.adapters.IntegerFieldAdapter;
import org.dmfs.tasks.model.adapters.StringFieldAdapter;
import org.dmfs.tasks.model.adapters.TimeFieldAdapter;
import org.dmfs.tasks.model.adapters.UrlFieldAdapter;
import org.dmfs.provider.tasks.TaskContract;

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
 * <TaskSource>
 * 
 * <datakind kind="title" title="@string/title_title" hint="@string/title_hint">
 * 
 * <datakind kind="location" title="@string/location_title" hint="@string/location_hint">
 * 
 * </TaskSource>
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public class XmlModel extends Model
{

	private final static String TAG = "org.dmfs.tasks.model.XmlModel";

	public final static String METADATA_TASKS = "ms.jung.android.caldavtodo.TASKS";
    public final static String NAMESPACE = "ms.jung.android.caldavtodo";

	private final static Map<String, FieldInflater> FIELD_INFLATER_MAP = new HashMap<String, FieldInflater>();
	private final static Map<String, FieldInflater> PROPERTY_INFLATER_MAP = new HashMap<String, FieldInflater>();

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

			eventType = parser.next();
			while (eventType != XmlResourceParser.END_DOCUMENT)
			{
				if (eventType == XmlResourceParser.START_TAG)
				{
					depth++;
					Log.v(TAG, "'" + parser.getName() + "'   " + depth);
					if (depth == 2 && "datakind".equals(parser.getName()) && NAMESPACE.equals(parser.getNamespace()))
					{
						FieldDescriptor descriptor = inflateField(parser);
						mFields.add(descriptor);
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
	 * An abstract field inflater. It does some default inflating.
	 * 
	 * @author Marten Gajda <marten@dmfs.org>
	 */
	private abstract static class FieldInflater
	{
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
			customizeDescriptor(modelContext, descriptor, parser);
			return descriptor;
		}


		abstract FieldAdapter<?> getFieldAdapter();


		void customizeDescriptor(Context modelContext, FieldDescriptor descriptor, XmlResourceParser parser)
		{
			int hintId = parser.getAttributeResourceValue(null, "hint_id", -1);
			if (hintId != -1)
			{
				descriptor.setHind(modelContext.getString(hintId));
			}
		}


		String getContentType()
		{
			return null;
		}


		abstract int getDefaultTitleId();
	}

	static
	{
		/*
		 * Add definitions for all supported fields:
		 */

		FIELD_INFLATER_MAP.put("title", new FieldInflater()
		{
			@Override
			public FieldAdapter<?> getFieldAdapter()
			{
				return new StringFieldAdapter(TaskContract.Tasks.TITLE);
			}


			@Override
			int getDefaultTitleId()
			{
				return R.string.task_title;
			}
		});

		FIELD_INFLATER_MAP.put("location", new FieldInflater()
		{
			@Override
			public FieldAdapter<?> getFieldAdapter()
			{
				return new StringFieldAdapter(TaskContract.Tasks.LOCATION);
			}


			@Override
			int getDefaultTitleId()
			{
				return R.string.task_location;
			}
		});

		FIELD_INFLATER_MAP.put("description", new FieldInflater()
		{
			@Override
			public FieldAdapter<?> getFieldAdapter()
			{
				return new StringFieldAdapter(TaskContract.Tasks.DESCRIPTION);
			}


			@Override
			int getDefaultTitleId()
			{
				return R.string.task_description;
			}
		});

		FIELD_INFLATER_MAP.put("dtstart", new FieldInflater()
		{
			@Override
			public FieldAdapter<?> getFieldAdapter()
			{
				return new TimeFieldAdapter(TaskContract.Tasks.DTSTART, TaskContract.Tasks.TZ, TaskContract.Tasks.IS_ALLDAY);
			}


			@Override
			int getDefaultTitleId()
			{
				return R.string.task_start;
			}
		});

		FIELD_INFLATER_MAP.put("start_date", new FieldInflater()
		{
			@Override
			public FieldAdapter<?> getFieldAdapter()
			{
				return new TimeFieldAdapter(TaskContract.Tasks.DTSTART, TaskContract.Tasks.TZ, TaskContract.Tasks.IS_ALLDAY);
			}


			@Override
			int getDefaultTitleId()
			{
				return R.string.task_start;
			}
		});

		FIELD_INFLATER_MAP.put("start_time", new FieldInflater()
		{
			@Override
			public FieldAdapter<?> getFieldAdapter()
			{
				return new TimeFieldAdapter(TaskContract.Tasks.DTSTART, TaskContract.Tasks.TZ, TaskContract.Tasks.IS_ALLDAY);
			}


			@Override
			int getDefaultTitleId()
			{
				return R.string.task_start;
			}
		});

		FIELD_INFLATER_MAP.put("due", new FieldInflater()
		{
			@Override
			public FieldAdapter<?> getFieldAdapter()
			{
				return new TimeFieldAdapter(TaskContract.Tasks.DUE, TaskContract.Tasks.TZ, TaskContract.Tasks.IS_ALLDAY);
			}


			@Override
			int getDefaultTitleId()
			{
				return R.string.task_due;
			}
		});

		FIELD_INFLATER_MAP.put("due_date", new FieldInflater()
		{
			@Override
			public FieldAdapter<?> getFieldAdapter()
			{
				return new TimeFieldAdapter(TaskContract.Tasks.DUE, TaskContract.Tasks.TZ, TaskContract.Tasks.IS_ALLDAY);
			}


			@Override
			int getDefaultTitleId()
			{
				return R.string.task_due;
			}
		});

		FIELD_INFLATER_MAP.put("due_time", new FieldInflater()
		{
			@Override
			public FieldAdapter<?> getFieldAdapter()
			{
				return new TimeFieldAdapter(TaskContract.Tasks.DUE, TaskContract.Tasks.TZ, TaskContract.Tasks.IS_ALLDAY);
			}


			@Override
			int getDefaultTitleId()
			{
				return R.string.task_due;
			}
		});

		FIELD_INFLATER_MAP.put("completed", new FieldInflater()
		{
			@Override
			public FieldAdapter<?> getFieldAdapter()
			{
				return new TimeFieldAdapter(TaskContract.Tasks.COMPLETED, TaskContract.Tasks.TZ, TaskContract.Tasks.IS_ALLDAY);
			}


			@Override
			int getDefaultTitleId()
			{
				return R.string.task_completed;
			}
		});

		FIELD_INFLATER_MAP.put("completed_date", new FieldInflater()
		{
			@Override
			public FieldAdapter<?> getFieldAdapter()
			{
				return new TimeFieldAdapter(TaskContract.Tasks.COMPLETED, TaskContract.Tasks.TZ, TaskContract.Tasks.IS_ALLDAY);
			}


			@Override
			int getDefaultTitleId()
			{
				return R.string.task_completed;
			}
		});

		FIELD_INFLATER_MAP.put("completed_time", new FieldInflater()
		{
			@Override
			public FieldAdapter<?> getFieldAdapter()
			{
				return new TimeFieldAdapter(TaskContract.Tasks.COMPLETED, TaskContract.Tasks.TZ, TaskContract.Tasks.IS_ALLDAY);
			}


			@Override
			int getDefaultTitleId()
			{
				return R.string.task_completed;
			}
		});

		FIELD_INFLATER_MAP.put("percent_complete", new FieldInflater()
		{
			@Override
			public FieldAdapter<?> getFieldAdapter()
			{
				return new IntegerFieldAdapter(TaskContract.Tasks.PERCENT_COMPLETE);
			}


			@Override
			int getDefaultTitleId()
			{
				return R.string.task_percent_complete;
			}
		});

		FIELD_INFLATER_MAP.put("status", new FieldInflater()
		{
			@Override
			public FieldAdapter<?> getFieldAdapter()
			{
				return new IntegerFieldAdapter(TaskContract.Tasks.STATUS);
			}


			@Override
			int getDefaultTitleId()
			{
				return R.string.task_status;
			}
		});
		
		FIELD_INFLATER_MAP.put("priority", new FieldInflater()
		{
			@Override
			public FieldAdapter<?> getFieldAdapter()
			{
				return new IntegerFieldAdapter(TaskContract.Tasks.STATUS);
			}


			@Override
			int getDefaultTitleId()
			{
				return R.string.task_status;
			}
		});

		FIELD_INFLATER_MAP.put("url", new FieldInflater()
		{
			@Override
			public FieldAdapter<?> getFieldAdapter()
			{
				return new UrlFieldAdapter(TaskContract.Tasks.URL);
			}


			@Override
			int getDefaultTitleId()
			{
				return R.string.task_url;
			}
		});

		/*
		 * Same for the properties
		 */
		/*
		 * PROPERTY_INFLATER_MAP.put("attendee", new FieldInflater() {
		 * 
		 * @Override public FieldAdapter<?> getFieldAdapter() { return new StringFieldAdapter(TaskContract.Tasks.URL); }
		 * 
		 * @Override String getContentType() { return TaskContract.Property.Attendee.CONTENT_ITEM_TYPE; } });
		 */
	}
}
