/*
 * DefaultModel.java
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
import org.dmfs.tasks.model.adapters.StringFieldAdapter;
import org.dmsf.provider.tasks.TaskContract.Tasks;

import android.content.Context;


/**
 * The default model for sync adapters that don't provide a model definition.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public class DefaultModel extends Model
{

	private final Context mContext;


	public DefaultModel(Context context)
	{
		mContext = context;
	}


	@Override
	public void inflate()
	{
		if (inflated)
		{
			return;
		}

		mFields.add(new FieldDescriptor(mContext, R.string.task_title, new StringFieldAdapter(Tasks.TITLE)));
		mFields.add(new FieldDescriptor(mContext, R.string.task_location, new StringFieldAdapter(Tasks.LOCATION)));
		mFields.add(new FieldDescriptor(mContext, R.string.task_description, new StringFieldAdapter(Tasks.DESCRIPTION)));
		// mFields.add(new FieldDescriptor(R.string.task_status, new IntegerFieldAdapter(Tasks.STATUS)));
		// mFields.add(new FieldDescriptor(R.string.task_due, new TimeFieldAdapter(Tasks.DUE, Tasks.TZ, Tasks.SYNC1)));

		setAllowRecurrence(false);
		setAllowExceptions(false);

		inflated = true;
	}

}
