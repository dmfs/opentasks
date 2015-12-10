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

package org.dmfs.tasks.utils;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


/**
 * Maintains the widget configuration in a databse.
 * 
 * @author Tobias Reinsch <tobias@dmfs.org>
 */
public class WidgetConfigurationDatabaseHelper extends SQLiteOpenHelper
{

	/**
	 * Database schema version number.
	 */
	private final static int VERSION = 1;

	/**
	 * Name of the database.
	 */
	private final static String LIST_CONFIGURATION_DATABASE = "list_configuration.db";

	/**
	 * Columns of the widget configuration table.
	 */
	public interface WidgetConfigurationColumns
	{
		/**
		 * The row id.
		 */
		public static final String _ID = "_id";

		/**
		 * The id of the widget
		 */
		public static final String WIDGET_ID = "widget_id";

		/**
		 * The list name for the widget
		 */
		public static final String LIST_ID = "list_id";

	}

	static final String[] PROJECTION = new String[] { WidgetConfigurationColumns.WIDGET_ID, WidgetConfigurationColumns.LIST_ID };

	/**
	 * The table name.
	 */
	static final String WIDGET_CONFIGURATION_TABLE = "widget_config";

	// @formatter:off
	private final static String SQL_CREATE_WIDGET_CONFIGURATION_TABLE =
		"CREATE TABLE " + WIDGET_CONFIGURATION_TABLE + " ( "
			+ WidgetConfigurationColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ WidgetConfigurationColumns.WIDGET_ID + " INTEGER, "
			+ WidgetConfigurationColumns.LIST_ID + " INTEGER,  UNIQUE ( " + WidgetConfigurationColumns.WIDGET_ID + ", " + WidgetConfigurationColumns.LIST_ID + " ) ON CONFLICT IGNORE )";
	// @formatter:on

	// @formatter:off
	private static final String SQL_DELETE_CONFIGURATION_TABLE =
	    "DROP TABLE IF EXISTS " + WIDGET_CONFIGURATION_TABLE;
	// @formatter:on

	public WidgetConfigurationDatabaseHelper(Context context)
	{
		super(context, LIST_CONFIGURATION_DATABASE, null, VERSION);
	}


	@Override
	public void onCreate(SQLiteDatabase db)
	{
		db.execSQL(SQL_CREATE_WIDGET_CONFIGURATION_TABLE);
	}


	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		db.execSQL(SQL_DELETE_CONFIGURATION_TABLE);
		onCreate(db);
	}


	public void deleteWidgetConfiguration(int[] widgetIds)
	{
		SQLiteDatabase db = getWritableDatabase();
		deleteConfiguration(db, widgetIds);
		db.close();
	}


	public static void deleteConfiguration(SQLiteDatabase db, int[] widgetIds)
	{
		StringBuilder selection = new StringBuilder();
		for (int i = 0; i < widgetIds.length; i++)
		{
			int id = widgetIds[i];
			selection.append(WidgetConfigurationColumns.WIDGET_ID).append(" = ").append(id);
			if (i < widgetIds.length - 1)
			{
				selection.append(" OR ");
			}

		}
		db.delete(WIDGET_CONFIGURATION_TABLE, selection.toString(), null);
	}


	public static void deleteConfiguration(SQLiteDatabase db, int widgetId)
	{
		db.delete(WIDGET_CONFIGURATION_TABLE, WidgetConfigurationColumns.WIDGET_ID + " = " + widgetId, null);
	}


	public static void insertTaskList(SQLiteDatabase db, int widgetId, Long taskId)
	{
		ContentValues values = new ContentValues();
		values.put(WidgetConfigurationColumns.WIDGET_ID, widgetId);
		values.put(WidgetConfigurationColumns.LIST_ID, taskId);
		db.insert(WIDGET_CONFIGURATION_TABLE, null, values);
	}


	public static ArrayList<Long> loadTaskLists(SQLiteDatabase db, int widgetId)
	{
		Cursor c = db.query(WIDGET_CONFIGURATION_TABLE, PROJECTION, WidgetConfigurationColumns.WIDGET_ID + " = " + widgetId, null, null, null, null);

		ArrayList<Long> lists = new ArrayList<Long>(c.getCount());
		if (!c.moveToFirst())
		{
			return lists;
		}
		do
		{
			Long listId = c.getLong(c.getColumnIndex(WidgetConfigurationColumns.LIST_ID));
			lists.add(listId);
		} while (c.moveToNext());
		c.close();
		return lists;

	}
}
