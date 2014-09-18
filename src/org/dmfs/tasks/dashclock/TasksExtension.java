/*
 * Copyright (C) 2014 Marten Gajda <marten@dmfs.org>
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

package org.dmfs.tasks.dashclock;

import java.util.Calendar;

import org.dmfs.provider.tasks.TaskContract.Instances;
import org.dmfs.provider.tasks.TaskContract.Tasks;
import org.dmfs.tasks.EditTaskActivity;
import org.dmfs.tasks.R;
import org.dmfs.tasks.model.adapters.TimeFieldAdapter;
import org.dmfs.tasks.utils.DueDateFormatter;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.text.format.Time;

import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;


/**
 * 
 * 
 * @author Tobias Reinsch <tobias@dmfs.org>
 * 
 */
public class TasksExtension extends DashClockExtension
{
	private static final String[] INSTANCE_PROJECTION = new String[] { Instances._ID, Instances.TASK_ID, Instances.ACCOUNT_NAME, Instances.ACCOUNT_TYPE,
		Instances.TITLE, Instances.DESCRIPTION, Instances.STATUS, Instances.DUE, Instances.DTSTART, Instances.TZ, Instances.IS_ALLDAY };

	private static final String INSTANCE_DUE_SELECTION = Instances.IS_ALLDAY + " = 0 AND (" + Instances.DUE + " > ? AND " + Instances.DUE + " < ? )";

	private String mAuthority;


	@Override
	protected void onUpdateData(int reason)
	{
		mAuthority = getString(R.string.org_dmfs_tasks_authority);
		publishDueTasksUpdate();
	}


	protected void publishDueTasksUpdate()
	{
		DueDateFormatter formatter = new DueDateFormatter(this, DueDateFormatter.TIME_DATEUTILS_FLAGS);
		Calendar calendar = Calendar.getInstance();
		long now = calendar.getTimeInMillis();

		calendar.add(Calendar.DAY_OF_YEAR, 1);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		long nextDay = calendar.getTimeInMillis();

		// get next task that is due
		Cursor c = getContentResolver().query(Instances.getContentUri(mAuthority), INSTANCE_PROJECTION, INSTANCE_DUE_SELECTION,
			new String[] { String.valueOf(now), String.valueOf(nextDay) }, Instances.DUE);

		int numberOfTasks = c.getCount();
		if (numberOfTasks > 0)
		{
			c.moveToFirst();

			TimeFieldAdapter timeFieldAdapter = new TimeFieldAdapter(Instances.DUE, Instances.TZ, Instances.IS_ALLDAY);
			Time dueTime = timeFieldAdapter.get(c);
			String dueTimeString = formatter.format(dueTime, false);
			String title = getString(R.string.dashclock_widget_title_due_expanded, c.getString(c.getColumnIndex(Tasks.TITLE)), dueTimeString);
			String description = c.getString(c.getColumnIndex(Tasks.DESCRIPTION));
			description = description.replaceAll("\\[\\s?\\]", " ").replaceAll("\\[[xX]\\]", "✓");

			// intent
			String accountType = c.getString(c.getColumnIndex(Instances.ACCOUNT_TYPE));
			Long taskId = c.getLong(c.getColumnIndex(Instances.TASK_ID));
			Intent clickIntent = buildClickIntent(taskId, accountType);

			// Publish the extension data update.
			publishUpdate(new ExtensionData().visible(true).icon(R.drawable.ic_notification_completed).status(String.valueOf(numberOfTasks))
				.expandedTitle(title).expandedBody(description).clickIntent(clickIntent));
		}
		else
		{
			// no upcoming task -> empty update
			publishUpdate(null);
		}

	}


	protected Intent buildClickIntent(long taskId, String accountType)
	{
		Intent clickIntent = new Intent(Intent.ACTION_VIEW);
		clickIntent.setData(ContentUris.withAppendedId(Tasks.getContentUri(mAuthority), taskId));
		clickIntent.putExtra(EditTaskActivity.EXTRA_DATA_ACCOUNT_TYPE, accountType);

		return clickIntent;
	}
}
