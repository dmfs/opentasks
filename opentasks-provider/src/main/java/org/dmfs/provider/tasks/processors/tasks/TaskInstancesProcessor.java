/*
 * Copyright (C) 2015 Marten Gajda <marten@dmfs.org>
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

package org.dmfs.provider.tasks.processors.tasks;

import java.sql.RowId;
import java.util.TimeZone;

import org.dmfs.provider.tasks.TaskContract;
import org.dmfs.provider.tasks.TaskContract.Instances;
import org.dmfs.provider.tasks.TaskDatabaseHelper.Tables;
import org.dmfs.provider.tasks.model.TaskAdapter;
import org.dmfs.provider.tasks.model.adapters.BooleanFieldAdapter;
import org.dmfs.provider.tasks.processors.AbstractEntityProcessor;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.Duration;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;


/**
 * A processor that creates or updates any instance values for a task.
 * <p/>
 * TODO: At present this does not support recurrence.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public class TaskInstancesProcessor extends AbstractEntityProcessor<TaskAdapter>
{

	/**
	 * This is a field adapter for a pseudo column to indicate that the instances may need an update, even if no relevant value has changed. This is useful to
	 * force an update of the sorting values when the local timezone has been changed.
	 */
	private final static BooleanFieldAdapter<TaskAdapter> UPDATE_REQUESTED = new BooleanFieldAdapter<TaskAdapter>(
		"org.dmfs.tasks.TaskInstanceProcessor.UPDATE_REQUESTED");


	/**
	 * Add a pseudo column to the given {@link ContentValues} to request an instances update, even if no time value has changed.
	 * 
	 * @param values
	 *            The {@link ContentValues} to add the pseudo column to.
	 */
	public static void addUpdateRequest(ContentValues values)
	{
		UPDATE_REQUESTED.setIn(values, true);
	}


	@Override
	public void afterInsert(SQLiteDatabase db, TaskAdapter task, boolean isSyncAdapter)
	{
		createInstances(db, task);
	}


	@Override
	public void beforeUpdate(SQLiteDatabase db, TaskAdapter task, boolean isSyncAdapter)
	{
		// move the UPDATE requested value to the state
		if (task.isUpdated(UPDATE_REQUESTED))
		{
			task.setState(UPDATE_REQUESTED, task.valueOf(UPDATE_REQUESTED));
			task.unset(UPDATE_REQUESTED);
		}
	}


	@Override
	public void afterUpdate(SQLiteDatabase db, TaskAdapter task, boolean isSyncAdapter)
	{
		if (!task.isUpdated(TaskAdapter.DTSTART) && !task.isUpdated(TaskAdapter.DUE) && !task.isUpdated(TaskAdapter.DURATION)
			&& !task.getState(UPDATE_REQUESTED))
		{
			// date values didn't change and update not requested
			return;
		}
		updateInstances(db, task);
	}


	/**
	 * Create new {@link ContentValues} for insertion into the instances table.
	 * 
	 * @param task
	 *            The {@link TaskAdapter} of the task that's about to be inserted.
	 * @return {@link ContentValues} of the instance of this task.
	 */
	private ContentValues generateInstanceValues(TaskAdapter task)
	{
		ContentValues instanceValues = new ContentValues();

		// get the relevant values from values
		DateTime dtstart = task.valueOf(TaskAdapter.DTSTART);
		DateTime due = task.valueOf(TaskAdapter.DUE);
		Duration duration = task.valueOf(TaskAdapter.DURATION);

		TimeZone localTz = TimeZone.getDefault();

		if (dtstart != null)
		{
			// copy dtstart as is
			instanceValues.put(Instances.INSTANCE_START, dtstart.getTimestamp());
			instanceValues.put(Instances.INSTANCE_START_SORTING, dtstart.isAllDay() ? dtstart.getInstance() : dtstart.shiftTimeZone(localTz).getInstance());
		}
		else
		{
			instanceValues.putNull(Instances.INSTANCE_START);
			instanceValues.putNull(Instances.INSTANCE_START_SORTING);
		}

		if (due != null)
		{
			// copy due and calculate the actual duration, if any
			instanceValues.put(Instances.INSTANCE_DUE, due.getTimestamp());
			instanceValues.put(Instances.INSTANCE_DUE_SORTING, due.isAllDay() ? due.getInstance() : due.shiftTimeZone(localTz).getInstance());
			if (dtstart != null)
			{
				instanceValues.put(Instances.INSTANCE_DURATION, due.getTimestamp() - dtstart.getTimestamp());
			}
			else
			{
				instanceValues.putNull(Instances.INSTANCE_DURATION);
			}
		}
		else if (duration != null)
		{
			if (dtstart != null)
			{
				// calculate the actual due value from dtstart and the duration string
				due = dtstart.addDuration(duration);
				instanceValues.put(Instances.INSTANCE_DUE, due.getTimestamp());
				instanceValues.put(Instances.INSTANCE_DUE_SORTING, due.isAllDay() ? due.getInstance() : due.shiftTimeZone(localTz).getInstance());
				instanceValues.put(Instances.INSTANCE_DURATION, due.getTimestamp() - dtstart.getTimestamp());
			}
			else
			{
				// this case should be filtered by TaskValidatorProcessor, since setting a DURATION without DTSTART is invalid
				instanceValues.putNull(Instances.INSTANCE_DURATION);
				instanceValues.putNull(Instances.INSTANCE_DUE);
				instanceValues.putNull(Instances.INSTANCE_DUE_SORTING);
			}
		}
		else
		{
			instanceValues.putNull(Instances.INSTANCE_DURATION);
			instanceValues.putNull(Instances.INSTANCE_DUE);
			instanceValues.putNull(Instances.INSTANCE_DUE_SORTING);
		}
		return instanceValues;
	}


	/**
	 * Creates new instances for the given task {@link ContentValues}.
	 * <p>
	 * TODO: expand recurrence
	 * </p>
	 * 
	 * @param uri
	 *            The {@link Uri} used when inserting the task.
	 * @param values
	 *            The {@link ContentValues} of the task.
	 * @param rowId
	 *            The new {@link RowId} of the task.
	 */
	private void createInstances(SQLiteDatabase db, TaskAdapter task)
	{
		ContentValues instanceValues = generateInstanceValues(task);

		// set rowID of current Task
		instanceValues.put(Instances.TASK_ID, task.id());

		db.insert(Tables.INSTANCES, null, instanceValues);
	}


	private void updateInstances(SQLiteDatabase db, TaskAdapter task)
	{
		ContentValues instanceValues = generateInstanceValues(task);

		db.update(Tables.INSTANCES, instanceValues, TaskContract.Instances.TASK_ID + " = " + task.id(), null);
	}
}
