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

package org.dmfs.provider.tasks.handler;

import org.dmfs.provider.tasks.TaskContract.Property.Relation;
import org.dmfs.provider.tasks.TaskContract.Property.Relation.RelType;
import org.dmfs.provider.tasks.TaskContract.Tasks;
import org.dmfs.provider.tasks.TaskDatabaseHelper;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


/**
 * Handles any inserts, updates and deletes on the relations table.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public class RelationHandler extends PropertyHandler
{

	@Override
	public ContentValues validateValues(SQLiteDatabase db, long taskId, long propertyId, boolean isNew, ContentValues values, boolean isSyncAdapter)
	{
		if (values.containsKey(Relation.RELATED_CONTENT_URI))
		{
			throw new IllegalArgumentException("setting of RELATED_CONTENT_URI not allowed");
		}

		Long id = values.getAsLong(Relation.RELATED_ID);
		String uid = values.getAsString(Relation.RELATED_UID);
		String uri = values.getAsString(Relation.RELATED_URI);

		if (id == null && uri == null && uid != null)
		{
			values.putNull(Relation.RELATED_ID);
			values.putNull(Relation.RELATED_URI);
		}
		else if (id == null && uid == null && uri != null)
		{
			values.putNull(Relation.RELATED_ID);
			values.putNull(Relation.RELATED_UID);
		}
		else if (id != null && uid == null && uri == null)
		{
			values.putNull(Relation.RELATED_URI);
			values.putNull(Relation.RELATED_UID);
		}
		else
		{
			throw new IllegalArgumentException("exactly one of RELATED_ID, RELATED_UID and RELATED_URI must be non-null");
		}

		return values;
	}


	@Override
	public long insert(SQLiteDatabase db, long taskId, ContentValues values, boolean isSyncAdapter)
	{
		validateValues(db, taskId, -1, true, values, isSyncAdapter);
		resolveFields(db, values);
		updateParentId(db, taskId, values, null);
		return super.insert(db, taskId, values, isSyncAdapter);
	}


	@Override
	public int update(SQLiteDatabase db, long taskId, long propertyId, ContentValues values, Cursor oldValues, boolean isSyncAdapter)
	{
		validateValues(db, taskId, propertyId, false, values, isSyncAdapter);
		resolveFields(db, values);
		updateParentId(db, taskId, values, oldValues);
		return super.update(db, taskId, propertyId, values, oldValues, isSyncAdapter);
	}


	@Override
	public int delete(SQLiteDatabase db, long taskId, long propertyId, Cursor oldValues, boolean isSyncAdapter)
	{
		clearParentId(db, taskId, oldValues);
		return super.delete(db, taskId, propertyId, oldValues, isSyncAdapter);
	}


	/**
	 * Resolve <code>_id</code> or <code>_uid</code>, depending of which value is given. We can't resolve anything if only {@link Relation#RELATED_URI} is
	 * given. The given values are update in-place.
	 * <p>
	 * TODO: store links into the calendar provider if we find an event that matches the UID.
	 * </p>
	 * 
	 * @param db
	 *            The task database.
	 * @param values
	 *            The {@link ContentValues}.
	 */
	private void resolveFields(SQLiteDatabase db, ContentValues values)
	{
		Long id = values.getAsLong(Relation.RELATED_ID);
		String uid = values.getAsString(Relation.RELATED_UID);

		if (id != null)
		{
			values.put(Relation.RELATED_UID, resolveTaskStringField(db, Tasks._ID, id.toString(), Tasks._UID));
		}
		else if (uid != null)
		{
			values.put(Relation.RELATED_ID, resolveTaskLongField(db, Tasks._UID, uid, Tasks._ID));
		}
	}


	private Long resolveTaskLongField(SQLiteDatabase db, String selectionField, String selectionValue, String resultField)
	{
		String result = resolveTaskStringField(db, selectionField, selectionValue, resultField);
		if (result != null)
		{
			return Long.parseLong(result);
		}
		return null;
	}


	private String resolveTaskStringField(SQLiteDatabase db, String selectionField, String selectionValue, String resultField)
	{
		Cursor c = db.query(TaskDatabaseHelper.Tables.TASKS, new String[] { resultField }, selectionField + "=?", new String[] { selectionValue }, null, null,
			null);
		if (c != null)
		{
			try
			{
				if (c.moveToNext())
				{
					return c.getString(0);
				}
			}
			finally
			{
				c.close();
			}
		}
		return null;
	}


	/**
	 * Update {@link Tasks#PARENT_ID} when a parent is assigned to a child.
	 * 
	 * @param db
	 * @param taskId
	 * @param values
	 * @param oldValues
	 */
	private void updateParentId(SQLiteDatabase db, long taskId, ContentValues values, Cursor oldValues)
	{
		int type;
		if (values.containsKey(Relation.RELATED_TYPE))
		{
			type = values.getAsInteger(Relation.RELATED_TYPE);
		}
		else
		{
			type = oldValues.getInt(oldValues.getColumnIndex(Relation.RELATED_TYPE));
		}

		if (type == RelType.PARENT.ordinal())
		{
			// this is a link to the parent, we need to update the PARENT_ID of this task, if we can

			if (values.containsKey(Relation.RELATED_ID))
			{
				ContentValues taskValues = new ContentValues(1);
				taskValues.put(Tasks.PARENT_ID, values.getAsLong(Relation.RELATED_ID));
				db.update(TaskDatabaseHelper.Tables.TASKS, taskValues, Tasks._ID + "=" + taskId, null);
			}
			// else: the parent task is probably not synced yet, we have to fix this in RelationUpdaterHook
		}
		else if (type == RelType.CHILD.ordinal())
		{
			// this is a link to a child, we need to update the PARENT_ID of the linked task

			if (values.getAsLong(Relation.RELATED_ID) != null)
			{
				ContentValues taskValues = new ContentValues(1);
				taskValues.put(Tasks.PARENT_ID, taskId);
				db.update(TaskDatabaseHelper.Tables.TASKS, taskValues, Tasks._ID + "=" + values.getAsLong(Relation.RELATED_ID), null);
			}
			// else: the child task is probably not synced yet, we have to fix this in RelationUpdaterHook
		}
		else if (type == RelType.SIBLING.ordinal())
		{
			// this is a link to a sibling, we need to copy the PARENT_ID of the linked task to this task
			if (values.getAsLong(Relation.RELATED_ID) != null)
			{
				// get the parent of the other task first
				Long otherParent = resolveTaskLongField(db, Tasks._ID, values.getAsString(Relation.RELATED_ID), Tasks.PARENT_ID);

				ContentValues taskValues = new ContentValues(1);
				taskValues.put(Tasks.PARENT_ID, otherParent);
				db.update(TaskDatabaseHelper.Tables.TASKS, taskValues, Tasks._ID + "=" + taskId, null);
			}
			// else: the sibling task is probably not synced yet, we have to fix this in RelationUpdaterHook
		}
	}


	/**
	 * Clear {@link Tasks#PARENT_ID} if a link is removed.
	 * 
	 * @param db
	 * @param taskId
	 * @param oldValues
	 */
	private void clearParentId(SQLiteDatabase db, long taskId, Cursor oldValues)
	{
		int type = oldValues.getInt(oldValues.getColumnIndex(Relation.RELATED_TYPE));

		/*
		 * This is more complicated than it may sound. We don't know the order in which relations are created, updated or removed. So it's possible that a new
		 * parent relationship has been created and the old one is removed afterwards. In that case we can not simply clear the PARENT_ID.
		 * 
		 * FIXME: For now we ignore that fact. But we should fix it.
		 */

		if (type == RelType.PARENT.ordinal())
		{
			// this was a link to the parent, we're orphaned now, so clear PARENT_ID of this task

			ContentValues taskValues = new ContentValues(1);
			taskValues.putNull(Tasks.PARENT_ID);
			db.update(TaskDatabaseHelper.Tables.TASKS, taskValues, Tasks._ID + "=" + taskId, null);
		}
		else if (type == RelType.CHILD.ordinal())
		{
			// this was a link to a child, the child is orphaned now, clear its PARENT_ID

			int relIdCol = oldValues.getColumnIndex(Relation.RELATED_ID);
			if (!oldValues.isNull(relIdCol))
			{
				ContentValues taskValues = new ContentValues(1);
				taskValues.putNull(Tasks.PARENT_ID);
				db.update(TaskDatabaseHelper.Tables.TASKS, taskValues, Tasks._ID + "=" + oldValues.getLong(relIdCol), null);
			}
		}
		else if (type == RelType.SIBLING.ordinal())
		{
			/*
			 * This was a link to a sibling, since it's no longer our sibling either it or we're orphaned now We won't know unless we check all relations.
			 * 
			 * FIXME: properly handle this case
			 */
		}
	}
}
