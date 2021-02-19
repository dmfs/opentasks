/*
 * Copyright 2017 dmfs GmbH
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
 */

package org.dmfs.provider.tasks;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.dmfs.jems.optional.adapters.First;
import org.dmfs.jems.predicate.elementary.Equals;
import org.dmfs.provider.tasks.model.CursorContentValuesTaskAdapter;
import org.dmfs.provider.tasks.model.TaskAdapter;
import org.dmfs.provider.tasks.processors.EntityProcessor;
import org.dmfs.provider.tasks.processors.NoOpProcessor;
import org.dmfs.provider.tasks.processors.tasks.Instantiating;
import org.dmfs.provider.tasks.utils.TableColumns;
import org.dmfs.tasks.contract.TaskContract;
import org.dmfs.tasks.contract.TaskContract.Properties;
import org.dmfs.tasks.contract.TaskContract.Property.Alarm;
import org.dmfs.tasks.contract.TaskContract.Property.Category;
import org.dmfs.tasks.contract.TaskContract.TaskLists;
import org.dmfs.tasks.contract.TaskContract.Tasks;

import java.util.Locale;


/**
 * Task database helper takes care of creating and updating the task database, including tables, indices and triggers.
 *
 * @author Marten Gajda <marten@dmfs.org>
 * @author Tobias Reinsch <tobias@dmfs.org>
 */
public class TaskDatabaseHelper extends SQLiteOpenHelper
{

    /**
     * Interface of a listener that's called when the database has been created or migrated.
     */
    public interface OnDatabaseOperationListener
    {
        void onDatabaseCreated(SQLiteDatabase db);

        void onDatabaseUpdate(SQLiteDatabase db, int oldVersion, int newVersion);
    }


    private static final String TAG = "TaskDatabaseHelper";

    /**
     * The name of our database file.
     */
    private static final String DATABASE_NAME = "tasks.db";

    /**
     * The database version.
     */
    private static final int DATABASE_VERSION = 23;


    /**
     * List of all tables we provide.
     */
    public interface Tables
    {
        String LISTS = "Lists";

        String WRITEABLE_LISTS = "Writeable_Lists";

        String TASKS = "Tasks";

        String TASKS_VIEW = "Task_View";

        String TASKS_PROPERTY_VIEW = "Task_Property_View";

        String INSTANCES = "Instances";

        String INSTANCE_VIEW = "Instance_View";

        String INSTANCE_CLIENT_VIEW = "Instance_Client_View";

        String INSTANCE_PROPERTY_VIEW = "Instance_Property_View";

        String INSTANCE_CATEGORY_VIEW = "Instance_Cagetory_View";

        String CATEGORIES = "Categories";

        String CATEGORIES_MAPPING = "Categories_Mapping";

        String PROPERTIES = "Properties";

        String ALARMS = "Alarms";

        String SYNCSTATE = "SyncState";
    }


    /**
     * Columns of internal table for the category mapping.
     */
    public interface CategoriesMapping
    {
        String TASK_ID = "task_id";

        String CATEGORY_ID = "category_id";

        String PROPERTY_ID = "property_id";

    }


    /**
     * SQL command to create a view that combines tasks with some data from the list they belong to.
     */
    private final static String SQL_CREATE_TASK_VIEW = "create view " + Tables.TASKS_VIEW + " as select " +
            Tables.TASKS + ".*, " +
            Tables.LISTS + "." + Tasks.ACCOUNT_NAME + ", " +
            Tables.LISTS + "." + Tasks.ACCOUNT_TYPE + ", " +
            Tables.LISTS + "." + Tasks.LIST_OWNER + ", " +
            Tables.LISTS + "." + Tasks.LIST_NAME + ", " +
            Tables.LISTS + "." + Tasks.LIST_ACCESS_LEVEL + ", " +
            Tables.LISTS + "." + Tasks.LIST_COLOR + ", " +
            Tables.LISTS + "." + Tasks.VISIBLE +
            " from " + Tables.TASKS + " join " + Tables.LISTS +
            " on (" + Tables.TASKS + "." + Tasks.LIST_ID + "=" + Tables.LISTS + "." + TaskLists._ID + ");";

    /**
     * SQL command to create a view that combines tasks with some data from the list they belong to.
     */
    private final static String SQL_CREATE_TASK_PROPERTY_VIEW = "create view " + Tables.TASKS_PROPERTY_VIEW + " as select " +
            Tables.TASKS + ".*, " +
            Tables.PROPERTIES + ".*, " +
            Tables.LISTS + "." + Tasks.ACCOUNT_NAME + ", " +
            Tables.LISTS + "." + Tasks.ACCOUNT_TYPE + ", " +
            Tables.LISTS + "." + Tasks.LIST_OWNER + ", " +
            Tables.LISTS + "." + Tasks.LIST_NAME + ", " +
            Tables.LISTS + "." + Tasks.LIST_ACCESS_LEVEL + ", " +
            Tables.LISTS + "." + Tasks.LIST_COLOR + ", " +
            Tables.LISTS + "." + Tasks.VISIBLE +
            " from " + Tables.TASKS + " join " + Tables.LISTS +
            " on (" + Tables.TASKS + "." + Tasks.LIST_ID + "=" + Tables.LISTS + "." + TaskLists._ID + ") " +
            "left join " + Tables.PROPERTIES + " on (" + Tables.TASKS + "." + Tasks._ID + "=" + Tables.PROPERTIES + "." + Properties.TASK_ID + ");";

    /**
     * SQL command to drop the task view.
     */
    private final static String SQL_DROP_TASK_VIEW = "DROP VIEW " + Tables.TASKS_VIEW + ";";

    /**
     * SQL command to create a view that combines task instances with some data from the list they belong to.
     */
    private final static String SQL_CREATE_INSTANCE_VIEW = "CREATE VIEW " + Tables.INSTANCE_VIEW + " AS SELECT "
            + Tables.INSTANCES + ".*, "
            + Tables.TASKS + ".*, "
            + Tables.LISTS + "." + Tasks.ACCOUNT_NAME + ", "
            + Tables.LISTS + "." + Tasks.ACCOUNT_TYPE + ", "
            + Tables.LISTS + "." + Tasks.LIST_OWNER + ", "
            + Tables.LISTS + "." + Tasks.LIST_NAME + ", "
            + Tables.LISTS + "." + Tasks.LIST_ACCESS_LEVEL + ", "
            + Tables.LISTS + "." + Tasks.LIST_COLOR + ", "
            + Tables.LISTS + "." + Tasks.VISIBLE
            + " FROM " + Tables.TASKS
            + " JOIN " + Tables.LISTS + " ON (" + Tables.TASKS + "." + TaskContract.Tasks.LIST_ID + "=" + Tables.LISTS + "." + TaskContract.Tasks._ID + ")"
            + " JOIN " + Tables.INSTANCES + " ON (" + Tables.TASKS + "." + TaskContract.Tasks._ID + "=" + Tables.INSTANCES + "." + TaskContract.Instances.TASK_ID + ");";

    /**
     * SQL command to create a view that combines task instances with some data from the list they belong to. This replaces the task DTSTART, DUE and
     * ORIGINAL_INSTANCE_TIME values with respective values of the instance.
     * <p>
     * This is the instances view as seen by the content provider clients.
     */
    private final static String SQL_CREATE_INSTANCE_CLIENT_VIEW = "CREATE VIEW " + Tables.INSTANCE_CLIENT_VIEW + " AS SELECT "
            + Tables.INSTANCES + ".*, "
            // override task due, start and original times with the instance values
            + Tables.INSTANCES + "." + TaskContract.Instances.INSTANCE_START + " as " + Tasks.DTSTART + ", "
            + Tables.INSTANCES + "." + TaskContract.Instances.INSTANCE_DUE + " as " + Tasks.DUE + ", "
            + Tables.INSTANCES + "." + TaskContract.Instances.INSTANCE_ORIGINAL_TIME + " as " + Tasks.ORIGINAL_INSTANCE_TIME + ", "
            // override task duration with null, we already have a due
            + "null as " + Tasks.DURATION + ", "
            // override recurrence values with null, instances themselves are not recurring
            + "null as " + Tasks.RRULE + ", "
            + "null as " + Tasks.RDATE + ", "
            + "null as " + Tasks.EXDATE + ", "
            // this instance is part of a recurring task if either it has recurrence values or overrides an instance
            + "not (" + Tasks.RRULE + " is null and " + Tasks.RDATE + " is null and " + Tasks.ORIGINAL_INSTANCE_ID + " is null and " + Tasks.ORIGINAL_INSTANCE_SYNC_ID + " is null) as " + TaskContract.Instances.IS_RECURRING + ", "
            + Tables.TASKS + ".*, "
            + Tables.LISTS + "." + Tasks.ACCOUNT_NAME + ", "
            + Tables.LISTS + "." + Tasks.ACCOUNT_TYPE + ", "
            + Tables.LISTS + "." + Tasks.LIST_OWNER + ", "
            + Tables.LISTS + "." + Tasks.LIST_NAME + ", "
            + Tables.LISTS + "." + Tasks.LIST_ACCESS_LEVEL + ", "
            + Tables.LISTS + "." + Tasks.LIST_COLOR + ", "
            + Tables.LISTS + "." + Tasks.VISIBLE
            + " FROM " + Tables.TASKS
            + " JOIN " + Tables.LISTS + " ON (" + Tables.TASKS + "." + TaskContract.Tasks.LIST_ID + "=" + Tables.LISTS + "." + TaskContract.TaskLists._ID + ")"
            + " JOIN " + Tables.INSTANCES + " ON (" + Tables.TASKS + "." + TaskContract.Tasks._ID + "=" + Tables.INSTANCES + "." + TaskContract.Instances.TASK_ID + ");";

    /**
     * SQL command to create a view that combines task instances view with the belonging properties.
     */
    private final static String SQL_CREATE_INSTANCE_PROPERTY_VIEW = "CREATE VIEW " + Tables.INSTANCE_PROPERTY_VIEW + " AS SELECT "
            + Tables.INSTANCES + ".*, "
            + Tables.PROPERTIES + ".*, "
            + Tables.TASKS + ".*, "
            + Tables.LISTS + "." + Tasks.ACCOUNT_NAME + ", "
            + Tables.LISTS + "." + Tasks.ACCOUNT_TYPE + ", "
            + Tables.LISTS + "." + Tasks.LIST_OWNER + ", "
            + Tables.LISTS + "." + Tasks.LIST_NAME + ", "
            + Tables.LISTS + "." + Tasks.LIST_ACCESS_LEVEL + ", "
            + Tables.LISTS + "." + Tasks.LIST_COLOR + ", "
            + Tables.LISTS + "." + Tasks.VISIBLE
            + " FROM " + Tables.TASKS
            + " JOIN " + Tables.LISTS + " ON (" + Tables.TASKS + "." + TaskContract.Tasks.LIST_ID + "=" + Tables.LISTS + "." + TaskContract.Tasks._ID + ")"
            + " JOIN " + Tables.INSTANCES + " ON (" + Tables.TASKS + "." + TaskContract.Tasks._ID + "=" + Tables.INSTANCES + "." + TaskContract.Instances.TASK_ID + ")"
            + " LEFT JOIN " + Tables.PROPERTIES + " ON (" + Tables.TASKS + "." + Tasks._ID + "=" + Tables.PROPERTIES + "." + Properties.TASK_ID + ");";

    /**
     * SQL command to create a view that combines task instances with some data from the list they belong to.
     */
    private final static String SQL_CREATE_INSTANCE_CATEGORY_VIEW = "CREATE VIEW " + Tables.INSTANCE_CATEGORY_VIEW + " AS SELECT "
            + Tables.INSTANCES + ".*, "
            + Tables.CATEGORIES_MAPPING + "." + CategoriesMapping.CATEGORY_ID + ", "
            + Tables.TASKS + ".*, "
            + Tables.LISTS + "." + Tasks.ACCOUNT_NAME + ", "
            + Tables.LISTS + "." + Tasks.ACCOUNT_TYPE + ", "
            + Tables.LISTS + "." + Tasks.LIST_OWNER + ", "
            + Tables.LISTS + "." + Tasks.LIST_NAME + ", "
            + Tables.LISTS + "." + Tasks.LIST_ACCESS_LEVEL + ", "
            + Tables.LISTS + "." + Tasks.LIST_COLOR + ", "
            + Tables.LISTS + "." + Tasks.VISIBLE
            + " FROM " + Tables.TASKS
            + " JOIN " + Tables.LISTS + " ON (" + Tables.TASKS + "." + TaskContract.Tasks.LIST_ID + "=" + Tables.LISTS + "." + TaskContract.Tasks._ID + ")"
            + " JOIN " + Tables.INSTANCES + " ON (" + Tables.TASKS + "." + TaskContract.Tasks._ID + "=" + Tables.INSTANCES + "." + TaskContract.Instances.TASK_ID + ")"
            + " LEFT JOIN " + Tables.CATEGORIES_MAPPING + " ON (" + Tables.CATEGORIES_MAPPING + "." + CategoriesMapping.TASK_ID + "=" + Tables.INSTANCES + "." + TaskContract.Instances.TASK_ID + ");";

    /**
     * SQL command to drop the instance view.
     */
    private final static String SQL_DROP_INSTANCE_VIEW = "DROP VIEW " + Tables.INSTANCE_VIEW + ";";

    /**
     * SQL command to drop the instance property view.
     */
    //private final static String SQL_DROP_INSTANCE_PROPERTY_VIEW = "DROP VIEW " + Tables.INSTANCE_PROPERTY_VIEW + ";";

    /**
     * SQL command to create the instances table.
     */
    private final static String SQL_CREATE_SYNCSTATE_TABLE =
            "CREATE TABLE " + Tables.SYNCSTATE + " ( " +
                    TaskContract.SyncState._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + TaskContract.SyncState.ACCOUNT_NAME + " TEXT, "
                    + TaskContract.SyncState.ACCOUNT_TYPE + " TEXT, "
                    + TaskContract.SyncState.DATA + " TEXT "
                    + ");";

    /**
     * SQL command to create the instances table.
     */
    private final static String SQL_CREATE_INSTANCES_TABLE =
            "CREATE TABLE " + Tables.INSTANCES + " ( " +
                    TaskContract.Instances._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + TaskContract.Instances.TASK_ID + " INTEGER NOT NULL, " // NOT NULL
                    + TaskContract.Instances.INSTANCE_START + " INTEGER, "
                    + TaskContract.Instances.INSTANCE_DUE + " INTEGER, "
                    + TaskContract.Instances.INSTANCE_START_SORTING + " INTEGER, "
                    + TaskContract.Instances.INSTANCE_DUE_SORTING + " INTEGER, "
                    + TaskContract.Instances.INSTANCE_DURATION + " INTEGER, "
                    + TaskContract.Instances.INSTANCE_ORIGINAL_TIME + " INTEGER DEFAULT 0, "
                    + TaskContract.Instances.DISTANCE_FROM_CURRENT + " INTEGER DEFAULT 0);";

    /**
     * SQL command to create a trigger to clean up data of removed tasks.
     */
    private final static String SQL_CREATE_TASKS_CLEANUP_TRIGGER =
            "CREATE TRIGGER task_cleanup_trigger AFTER DELETE ON " + Tables.TASKS
                    + " BEGIN "
                    + " DELETE FROM " + Tables.PROPERTIES + " WHERE " + TaskContract.Properties.TASK_ID + "= old." + TaskContract.Tasks._ID + ";"
                    + " DELETE FROM " + Tables.INSTANCES + " WHERE " + TaskContract.Instances.TASK_ID + "=old." + TaskContract.Tasks._ID + ";"
                    + " END;";

    /**
     * SQL command to create a trigger to clean up data of removed lists.
     */
    private final static String SQL_CREATE_LISTS_CLEANUP_TRIGGER =
            "CREATE TRIGGER list_cleanup_trigger AFTER DELETE ON " + Tables.LISTS
                    + " BEGIN "
                    + " DELETE FROM " + Tables.TASKS + " WHERE " + Tasks.LIST_ID + "= old." + TaskLists._ID + ";"
                    + " END;";

    /**
     * SQL command to drop the clean up trigger.
     */
    private final static String SQL_DROP_TASKS_CLEANUP_TRIGGER =
            "DROP TRIGGER task_cleanup_trigger;";

    /**
     * SQL command that counts and sets the alarm on deletion
     */
    private final static String SQL_COUNT_ALARMS_ON_DELETE =
            " BEGIN UPDATE " + Tables.TASKS + " SET " + Tasks.HAS_ALARMS
                    + " = (SELECT COUNT (*) FROM " + Tables.PROPERTIES
                    + " WHERE " + Properties.MIMETYPE + " = '" + Alarm.CONTENT_ITEM_TYPE + "' AND " + Alarm.ALARM_TYPE + " <> " + Alarm.ALARM_TYPE_NOTHING + " AND " + Properties.TASK_ID + " = OLD." + Properties.TASK_ID
                    + ") WHERE " + Tasks._ID + " = OLD." + Properties.TASK_ID
                    + "; END;";

    /**
     * SQL command that counts and sets the alarm on insert and update
     */
    private final static String SQL_COUNT_ALARMS =
            " BEGIN UPDATE " + Tables.TASKS + " SET " + Tasks.HAS_ALARMS
                    + " = (SELECT COUNT (*) FROM " + Tables.PROPERTIES
                    + " WHERE " + Properties.MIMETYPE + " = '" + Alarm.CONTENT_ITEM_TYPE + "' AND " + Alarm.ALARM_TYPE + " <> " + Alarm.ALARM_TYPE_NOTHING + " AND " + Properties.TASK_ID + " = NEW." + Properties.TASK_ID
                    + ") WHERE " + Tasks._ID + " = NEW." + Properties.TASK_ID
                    + "; END;";

    /**
     * SQL command to create a trigger that counts the alarms for a task on create
     */
    private final static String SQL_CREATE_ALARM_COUNT_CREATE_TRIGGER =
            "CREATE TRIGGER alarm_count_create_trigger AFTER INSERT ON " + Tables.PROPERTIES + " WHEN NEW." + Properties.MIMETYPE + " = '" + Alarm.CONTENT_ITEM_TYPE + "'"
                    + SQL_COUNT_ALARMS;

    /**
     * SQL command to create a trigger that counts the alarms for a task on update
     */
    private final static String SQL_CREATE_ALARM_COUNT_UPDATE_TRIGGER =
            "CREATE TRIGGER alarm_count_update_trigger AFTER UPDATE ON " + Tables.PROPERTIES + " WHEN NEW." + Properties.MIMETYPE + " = '" + Alarm.CONTENT_ITEM_TYPE + "'"
                    + SQL_COUNT_ALARMS;

    /**
     * SQL command to create a trigger that counts the alarms for a task on delete
     */
    private final static String SQL_CREATE_ALARM_COUNT_DELETE_TRIGGER =
            "CREATE TRIGGER alarm_count_delete_trigger AFTER DELETE ON " + Tables.PROPERTIES + " WHEN OLD." + Properties.MIMETYPE + " = '" + Alarm.CONTENT_ITEM_TYPE + "'"
                    + SQL_COUNT_ALARMS_ON_DELETE;

    /**
     * SQL command to create a trigger to clean up data of removed property.
     */
    private final static String SQL_CREATE_ALARM_PROPERTY_CLEANUP_TRIGGER =
            "CREATE TRIGGER alarm_property_cleanup_trigger AFTER DELETE ON " + Tables.PROPERTIES + " WHEN OLD." + Properties.MIMETYPE + " = '" + Alarm.CONTENT_ITEM_TYPE + "'"
                    + " BEGIN "
                    + " DELETE FROM " + Tables.ALARMS + " WHERE " + TaskContract.Alarms.ALARM_ID + "= OLD." + TaskContract.Properties.PROPERTY_ID + ";"
                    + " END;";

    /**
     * SQL command to create a trigger to clean up data of removed property.
     */
    private final static String SQL_CREATE_CATEGORY_PROPERTY_CLEANUP_TRIGGER =
            "CREATE TRIGGER category_property_cleanup_trigger AFTER DELETE ON " + Tables.PROPERTIES + " WHEN OLD." + Properties.MIMETYPE + " = '" + Category.CONTENT_ITEM_TYPE + "'"
                    + " BEGIN "
                    + " DELETE FROM " + Tables.CATEGORIES_MAPPING + " WHERE " + CategoriesMapping.PROPERTY_ID + "= OLD." + TaskContract.Properties.PROPERTY_ID + ";"
                    + " END;";

    /**
     * SQL command to create a trigger to clean up property data of removed task.
     */
    private final static String SQL_CREATE_TASK_PROPERTY_CLEANUP_TRIGGER =
            "CREATE TRIGGER task_property_cleanup_trigger AFTER DELETE ON " + Tables.TASKS + " BEGIN "
                    + " DELETE FROM " + Tables.PROPERTIES + " WHERE " + Properties.TASK_ID + "= OLD." + Tasks._ID + ";"
                    + " END;";

    /**
     * SQL command to create a trigger to increment task version number on every update.
     */
    private final static String SQL_CREATE_TASK_VERSION_TRIGGER =
            "CREATE TRIGGER task_version_trigger BEFORE UPDATE ON " + Tables.TASKS + " BEGIN "
                    + " UPDATE " + Tables.TASKS + " SET " + Tasks.VERSION + " = OLD." + Tasks.VERSION + " + 1 where " + Tasks._ID + " = NEW." + Tasks._ID + ";"
                    + " END;";

    /**
     * SQL command to create the task list table.
     */
    private final static String SQL_CREATE_LISTS_TABLE =
            "CREATE TABLE " + Tables.LISTS + " ( "
                    + TaskContract.TaskLists._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + TaskContract.TaskLists.ACCOUNT_NAME + " TEXT,"
                    + TaskContract.TaskLists.ACCOUNT_TYPE + " TEXT,"
                    + TaskContract.TaskLists.LIST_NAME + " TEXT,"
                    + TaskContract.TaskLists.LIST_COLOR + " INTEGER,"
                    + TaskContract.TaskLists.ACCESS_LEVEL + " INTEGER,"
                    + TaskContract.TaskLists.VISIBLE + " INTEGER,"
                    + TaskContract.TaskLists.SYNC_ENABLED + " INTEGER,"
                    + TaskContract.TaskLists.OWNER + " TEXT,"
                    + TaskContract.TaskLists._DIRTY + " INTEGER DEFAULT 0,"
                    + TaskContract.TaskLists._SYNC_ID + " TEXT,"
                    + TaskContract.TaskLists.SYNC_VERSION + " TEXT,"
                    + TaskContract.TaskLists.SYNC1 + " TEXT,"
                    + TaskContract.TaskLists.SYNC2 + " TEXT,"
                    + TaskContract.TaskLists.SYNC3 + " TEXT,"
                    + TaskContract.TaskLists.SYNC4 + " TEXT,"
                    + TaskContract.TaskLists.SYNC5 + " TEXT,"
                    + TaskContract.TaskLists.SYNC6 + " TEXT,"
                    + TaskContract.TaskLists.SYNC7 + " TEXT,"
                    + TaskContract.TaskLists.SYNC8 + " TEXT);";

    /**
     * SQL command to create the task table.
     */
    private final static String SQL_CREATE_TASKS_TABLE =
            "CREATE TABLE " + Tables.TASKS + " ( "
                    + TaskContract.Tasks._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + TaskContract.Tasks.VERSION + " INTEGER DEFAULT 0,"
                    + TaskContract.Tasks.LIST_ID + " INTEGER NOT NULL, "
                    + TaskContract.Tasks.TITLE + " TEXT,"
                    + TaskContract.Tasks.LOCATION + " TEXT,"
                    + TaskContract.Tasks.GEO + " TEXT,"
                    + TaskContract.Tasks.DESCRIPTION + " TEXT,"
                    + TaskContract.Tasks.URL + " TEXT,"
                    + TaskContract.Tasks.ORGANIZER + " TEXT,"
                    + TaskContract.Tasks.PRIORITY + " INTEGER, "
                    + TaskContract.Tasks.TASK_COLOR + " INTEGER,"
                    + TaskContract.Tasks.CLASSIFICATION + " INTEGER,"
                    + TaskContract.Tasks.COMPLETED + " INTEGER,"
                    + TaskContract.Tasks.COMPLETED_IS_ALLDAY + " INTEGER,"
                    + TaskContract.Tasks.PERCENT_COMPLETE + " INTEGER,"
                    + TaskContract.Tasks.STATUS + " INTEGER DEFAULT " + TaskContract.Tasks.STATUS_DEFAULT + ","
                    + TaskContract.Tasks.IS_NEW + " INTEGER,"
                    + TaskContract.Tasks.IS_CLOSED + " INTEGER,"
                    + TaskContract.Tasks.DTSTART + " INTEGER,"
                    + TaskContract.Tasks.CREATED + " INTEGER,"
                    + TaskContract.Tasks.LAST_MODIFIED + " INTEGER,"
                    + TaskContract.Tasks.IS_ALLDAY + " INTEGER,"
                    + TaskContract.Tasks.TZ + " TEXT,"
                    + TaskContract.Tasks.DUE + " INTEGER,"
                    + TaskContract.Tasks.DURATION + " TEXT,"
                    + TaskContract.Tasks.RDATE + " TEXT,"
                    + TaskContract.Tasks.EXDATE + " TEXT,"
                    + TaskContract.Tasks.RRULE + " TEXT,"
                    + TaskContract.Tasks.PARENT_ID + " INTEGER,"
                    + TaskContract.Tasks.SORTING + " TEXT,"
                    + TaskContract.Tasks.HAS_ALARMS + " INTEGER,"
                    + TaskContract.Tasks.HAS_PROPERTIES + " INTEGER,"
                    + TaskContract.Tasks.PINNED + " INTEGER,"
                    + TaskContract.Tasks.ORIGINAL_INSTANCE_SYNC_ID + " TEXT,"
                    + TaskContract.Tasks.ORIGINAL_INSTANCE_ID + " INTEGER,"
                    + TaskContract.Tasks.ORIGINAL_INSTANCE_TIME + " INTEGER,"
                    + TaskContract.Tasks.ORIGINAL_INSTANCE_ALLDAY + " INTEGER,"
                    + TaskContract.Tasks._DIRTY + " INTEGER DEFAULT 1," // a new task is always dirty
                    + TaskContract.Tasks._DELETED + " INTEGER DEFAULT 0," // new tasks are not deleted by default
                    + TaskContract.Tasks._SYNC_ID + " TEXT,"
                    + TaskContract.Tasks._UID + " TEXT,"
                    + TaskContract.Tasks.SYNC_VERSION + " TEXT,"
                    + TaskContract.Tasks.SYNC1 + " TEXT,"
                    + TaskContract.Tasks.SYNC2 + " TEXT,"
                    + TaskContract.Tasks.SYNC3 + " TEXT,"
                    + TaskContract.Tasks.SYNC4 + " TEXT,"
                    + TaskContract.Tasks.SYNC5 + " TEXT,"
                    + TaskContract.Tasks.SYNC6 + " TEXT,"
                    + TaskContract.Tasks.SYNC7 + " TEXT,"
                    + TaskContract.Tasks.SYNC8 + " TEXT);";

    /**
     * SQL command to create the categories table.
     */
    private final static String SQL_CREATE_CATEGORIES_TABLE =
            "CREATE TABLE " + Tables.CATEGORIES
                    + " ( " + TaskContract.Categories._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + TaskContract.Categories.ACCOUNT_NAME + " TEXT,"
                    + TaskContract.Categories.ACCOUNT_TYPE + " TEXT,"
                    + TaskContract.Categories.NAME + " TEXT,"
                    + TaskContract.Categories.COLOR + " INTEGER);";

    /**
     * SQL command to create the categories table.
     */
    private final static String SQL_CREATE_CATEGORIES_MAPPING_TABLE =
            "CREATE TABLE " + Tables.CATEGORIES_MAPPING
                    + " ( " + CategoriesMapping.TASK_ID + " INTEGER,"
                    + CategoriesMapping.CATEGORY_ID + " INTEGER,"
                    + CategoriesMapping.PROPERTY_ID + " INTEGER,"
                    + "FOREIGN KEY (" + CategoriesMapping.TASK_ID + ") REFERENCES " + Tables.TASKS + "(" + TaskContract.Tasks._ID + "),"
                    + "FOREIGN KEY (" + CategoriesMapping.PROPERTY_ID + ") REFERENCES " + Tables.PROPERTIES + "(" + TaskContract.Properties.PROPERTY_ID + "),"
                    + "FOREIGN KEY (" + CategoriesMapping.CATEGORY_ID + ") REFERENCES " + Tables.CATEGORIES + "(" + TaskContract.Categories._ID + "));";

    /**
     * SQL command to create the alarms table the stores the already triggered alarms.
     */
    private final static String SQL_CREATE_ALARMS_TABLE =
            "CREATE TABLE " + Tables.ALARMS
                    + " ( " + TaskContract.Alarms.ALARM_ID + " INTEGER,"
                    + TaskContract.Alarms.LAST_TRIGGER + " TEXT,"
                    + TaskContract.Alarms.NEXT_TRIGGER + " TEXT);";

    /**
     * SQL command to create the table for extended properties.
     */
    private final static String SQL_CREATE_PROPERTIES_TABLE =
            "CREATE TABLE " + Tables.PROPERTIES + " ( "
                    + TaskContract.Properties.PROPERTY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + TaskContract.Properties.TASK_ID + " INTEGER,"
                    + TaskContract.Properties.MIMETYPE + " INTEGER,"
                    + TaskContract.Properties.VERSION + " INTEGER,"
                    + TaskContract.Properties.DATA0 + " TEXT,"
                    + TaskContract.Properties.DATA1 + " TEXT,"
                    + TaskContract.Properties.DATA2 + " TEXT,"
                    + TaskContract.Properties.DATA3 + " TEXT,"
                    + TaskContract.Properties.DATA4 + " TEXT,"
                    + TaskContract.Properties.DATA5 + " TEXT,"
                    + TaskContract.Properties.DATA6 + " TEXT,"
                    + TaskContract.Properties.DATA7 + " TEXT,"
                    + TaskContract.Properties.DATA8 + " TEXT,"
                    + TaskContract.Properties.DATA9 + " TEXT,"
                    + TaskContract.Properties.DATA10 + " TEXT,"
                    + TaskContract.Properties.DATA11 + " TEXT,"
                    + TaskContract.Properties.DATA12 + " TEXT,"
                    + TaskContract.Properties.DATA13 + " TEXT,"
                    + TaskContract.Properties.DATA14 + " TEXT,"
                    + TaskContract.Properties.DATA15 + " TEXT,"
                    + TaskContract.Properties.SYNC1 + " TEXT,"
                    + TaskContract.Properties.SYNC2 + " TEXT,"
                    + TaskContract.Properties.SYNC3 + " TEXT,"
                    + TaskContract.Properties.SYNC4 + " TEXT,"
                    + TaskContract.Properties.SYNC5 + " TEXT,"
                    + TaskContract.Properties.SYNC6 + " TEXT,"
                    + TaskContract.Properties.SYNC7 + " TEXT,"
                    + TaskContract.Properties.SYNC8 + " TEXT);";

    /**
     * SQL command to drop the task view.
     */
    private final static String SQL_DROP_PROPERTIES_TABLE = "DROP TABLE " + Tables.PROPERTIES + ";";


    /**
     * Builds a string that creates an index on the given table for the given columns.
     *
     * @param table
     *         The table to create the index on.
     * @param fields
     *         The fields to index.
     *
     * @return An SQL command string.
     */
    public static String createIndexString(String table, boolean unique, String... fields)
    {
        if (fields == null || fields.length < 1)
        {
            throw new IllegalArgumentException("need at least one field to build an index!");
        }

        StringBuffer buffer = new StringBuffer();

        // Index name is constructed like this: tablename_fields[0]_idx
        buffer.append("CREATE ");
        if (unique)
        {
            buffer.append(" UNIQUE ");
        }
        buffer.append("INDEX IF NOT EXISTS ");
        buffer.append(table).append("_").append(fields[0]).append("_idx ON ");
        buffer.append(table).append(" (");
        buffer.append(fields[0]);
        for (int i = 1; i < fields.length; i++)
        {
            buffer.append(", ").append(fields[i]);
        }
        buffer.append(");");

        return buffer.toString();

    }


    private final OnDatabaseOperationListener mListener;


    TaskDatabaseHelper(Context context, OnDatabaseOperationListener listener)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mListener = listener;
    }


    /**
     * Creates the tables, views, triggers and indices.
     * <p>
     * TODO: move all strings to separate final static variables.
     */
    @Override
    public void onCreate(SQLiteDatabase db)
    {

        // create task list table
        db.execSQL(SQL_CREATE_LISTS_TABLE);

        // trigger that removes tasks of a list that has been removed
        db.execSQL("CREATE TRIGGER task_list_cleanup_trigger AFTER DELETE ON " + Tables.LISTS + " BEGIN DELETE FROM " + Tables.TASKS + " WHERE "
                + TaskContract.Tasks.LIST_ID + "= old." + TaskContract.TaskLists._ID + "; END");

        // create task table
        db.execSQL(SQL_CREATE_TASKS_TABLE);

        // trigger that marks a list as dirty if a task in that list gets marked as dirty or deleted
        db.execSQL("CREATE TRIGGER task_list_make_dirty_on_update AFTER UPDATE ON " + Tables.TASKS + " BEGIN UPDATE " + Tables.LISTS + " SET "
                + TaskContract.TaskLists._DIRTY + "=" + TaskContract.TaskLists._DIRTY + " + " + "new." + TaskContract.Tasks._DIRTY + " + " + "new."
                + TaskContract.Tasks._DELETED + " WHERE " + TaskContract.TaskLists._ID + "= new." + TaskContract.Tasks.LIST_ID + "; END");

        // trigger that marks a list as dirty if a task in that list gets marked as dirty or deleted
        db.execSQL("CREATE TRIGGER task_list_make_dirty_on_insert AFTER INSERT ON " + Tables.TASKS + " BEGIN UPDATE " + Tables.LISTS + " SET "
                + TaskContract.TaskLists._DIRTY + "=" + TaskContract.TaskLists._DIRTY + " + " + "new." + TaskContract.Tasks._DIRTY + " + " + "new."
                + TaskContract.Tasks._DELETED + " WHERE " + TaskContract.TaskLists._ID + "= new." + TaskContract.Tasks.LIST_ID + "; END");

        // create task version update trigger
        db.execSQL(SQL_CREATE_TASK_VERSION_TRIGGER);

        // create instances table and view
        db.execSQL(SQL_CREATE_INSTANCES_TABLE);

        // create categories table
        db.execSQL(SQL_CREATE_CATEGORIES_TABLE);

        // create categories mapping table
        db.execSQL(SQL_CREATE_CATEGORIES_MAPPING_TABLE);

        // create alarms table
        db.execSQL(SQL_CREATE_ALARMS_TABLE);

        // create properties table
        db.execSQL(SQL_CREATE_PROPERTIES_TABLE);

        // create syncstate table
        db.execSQL(SQL_CREATE_SYNCSTATE_TABLE);

        // create views
        db.execSQL(SQL_CREATE_TASK_VIEW);
        db.execSQL(SQL_CREATE_TASK_PROPERTY_VIEW);
        db.execSQL(SQL_CREATE_INSTANCE_VIEW);
        db.execSQL(SQL_CREATE_INSTANCE_CLIENT_VIEW);
        db.execSQL(SQL_CREATE_INSTANCE_PROPERTY_VIEW);
        db.execSQL(SQL_CREATE_INSTANCE_CATEGORY_VIEW);

        // create indices
        db.execSQL(createIndexString(Tables.INSTANCES, false, TaskContract.Instances.TASK_ID, TaskContract.Instances.INSTANCE_START,
                TaskContract.Instances.INSTANCE_DUE));
        db.execSQL(createIndexString(Tables.INSTANCES, false, TaskContract.Instances.INSTANCE_START_SORTING));
        db.execSQL(createIndexString(Tables.INSTANCES, false, TaskContract.Instances.INSTANCE_DUE_SORTING));
        db.execSQL(createIndexString(Tables.INSTANCES, false, TaskContract.Instances.INSTANCE_ORIGINAL_TIME));
        db.execSQL(createIndexString(Tables.LISTS, false, TaskContract.TaskLists.ACCOUNT_NAME, // not sure if necessary
                TaskContract.TaskLists.ACCOUNT_TYPE));
        db.execSQL(createIndexString(Tables.TASKS, false, TaskContract.Tasks.STATUS, TaskContract.Tasks.LIST_ID, TaskContract.Tasks._SYNC_ID));
        db.execSQL(createIndexString(Tables.PROPERTIES, false, TaskContract.Properties.MIMETYPE, TaskContract.Properties.TASK_ID));
        db.execSQL(createIndexString(Tables.PROPERTIES, false, TaskContract.Properties.TASK_ID));
        db.execSQL(createIndexString(Tables.CATEGORIES, false, TaskContract.Categories.ACCOUNT_NAME, TaskContract.Categories.ACCOUNT_TYPE,
                TaskContract.Categories.NAME));
        db.execSQL(createIndexString(Tables.CATEGORIES, false, TaskContract.Categories.NAME));
        db.execSQL(createIndexString(Tables.SYNCSTATE, true, TaskContract.SyncState.ACCOUNT_NAME, TaskContract.SyncState.ACCOUNT_TYPE));

        // trigger that removes properties of a task that has been removed
        db.execSQL(SQL_CREATE_TASKS_CLEANUP_TRIGGER);

        // trigger that removes alarms when an alarm property was deleted
        db.execSQL(SQL_CREATE_ALARM_PROPERTY_CLEANUP_TRIGGER);

        // trigger that removes tasks when a list was removed
        db.execSQL(SQL_CREATE_LISTS_CLEANUP_TRIGGER);

        // trigger that counts the alarms for tasks
        db.execSQL(SQL_CREATE_ALARM_COUNT_CREATE_TRIGGER);
        db.execSQL(SQL_CREATE_ALARM_COUNT_UPDATE_TRIGGER);
        db.execSQL(SQL_CREATE_ALARM_COUNT_DELETE_TRIGGER);

        // add cleanup trigger for orphaned properties
        db.execSQL(SQL_CREATE_TASK_PROPERTY_CLEANUP_TRIGGER);

        // initialize FTS
        FTSDatabaseHelper.onCreate(db);

        if (mListener != null)
        {
            mListener.onDatabaseCreated(db);
        }
    }


    /**
     * Manages the database schema migration.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        Log.i(TAG, "updgrading db from " + oldVersion + " to " + newVersion);
        if (oldVersion < 2)
        {
            // add IS_NEW and IS_CLOSED columns and update their values
            db.execSQL("ALTER TABLE " + Tables.TASKS + " ADD COLUMN " + TaskContract.Tasks.IS_NEW + " INTEGER");
            db.execSQL("ALTER TABLE " + Tables.TASKS + " ADD COLUMN " + TaskContract.Tasks.IS_CLOSED + " INTEGER");
            db.execSQL("UPDATE " + Tables.TASKS + " SET " + TaskContract.Tasks.IS_NEW + " = 1 WHERE " + TaskContract.Tasks.STATUS + " = "
                    + TaskContract.Tasks.STATUS_NEEDS_ACTION);
            db.execSQL("UPDATE " + Tables.TASKS + " SET " + TaskContract.Tasks.IS_NEW + " = 0 WHERE " + TaskContract.Tasks.STATUS + " != "
                    + TaskContract.Tasks.STATUS_NEEDS_ACTION);
            db.execSQL("UPDATE " + Tables.TASKS + " SET " + TaskContract.Tasks.IS_CLOSED + " = 1 WHERE " + TaskContract.Tasks.STATUS + " > "
                    + TaskContract.Tasks.STATUS_IN_PROCESS);
            db.execSQL("UPDATE " + Tables.TASKS + " SET " + TaskContract.Tasks.IS_CLOSED + " = 0 WHERE " + TaskContract.Tasks.STATUS + " <= "
                    + TaskContract.Tasks.STATUS_IN_PROCESS);
        }

        if (oldVersion < 3)
        {
            // add instance sortings
            db.execSQL("ALTER TABLE " + Tables.INSTANCES + " ADD COLUMN " + TaskContract.Instances.INSTANCE_START_SORTING + " INTEGER");
            db.execSQL("ALTER TABLE " + Tables.INSTANCES + " ADD COLUMN " + TaskContract.Instances.INSTANCE_DUE_SORTING + " INTEGER");
            db.execSQL("UPDATE " + Tables.INSTANCES + " SET " + TaskContract.Instances.INSTANCE_START_SORTING + " = " + TaskContract.Instances.INSTANCE_START
                    + ", " + TaskContract.Instances.INSTANCE_DUE_SORTING + " = " + TaskContract.Instances.INSTANCE_DUE);
        }
        if (oldVersion < 4)
        {
            // drop old view before altering the schema
            db.execSQL(SQL_DROP_TASK_VIEW);
            db.execSQL(SQL_DROP_INSTANCE_VIEW);

            // change property id column name to work with the left join in task view
            db.execSQL(SQL_DROP_TASKS_CLEANUP_TRIGGER);
            db.execSQL(SQL_DROP_PROPERTIES_TABLE);
            db.execSQL(SQL_CREATE_PROPERTIES_TABLE);
            db.execSQL(SQL_CREATE_TASKS_CLEANUP_TRIGGER);

            // create categories mapping table
            db.execSQL(SQL_CREATE_CATEGORIES_MAPPING_TABLE);

            // create alarms table
            db.execSQL(SQL_CREATE_ALARMS_TABLE);

            // update views
            db.execSQL(SQL_CREATE_TASK_VIEW);
            db.execSQL(SQL_CREATE_TASK_PROPERTY_VIEW);
            db.execSQL(SQL_CREATE_INSTANCE_VIEW);
            db.execSQL(SQL_CREATE_INSTANCE_PROPERTY_VIEW);
            db.execSQL(SQL_CREATE_INSTANCE_CATEGORY_VIEW);

            // create Indices
            db.execSQL(createIndexString(Tables.PROPERTIES, false, TaskContract.Properties.MIMETYPE, TaskContract.Properties.TASK_ID));
            db.execSQL(createIndexString(Tables.PROPERTIES, false, TaskContract.Properties.TASK_ID));
            db.execSQL(createIndexString(Tables.CATEGORIES, false, TaskContract.Categories.ACCOUNT_NAME, TaskContract.Categories.ACCOUNT_TYPE,
                    TaskContract.Categories.NAME));
            db.execSQL(createIndexString(Tables.CATEGORIES, false, TaskContract.Categories.NAME));

            // add new triggers
            db.execSQL(SQL_CREATE_ALARM_PROPERTY_CLEANUP_TRIGGER);
            db.execSQL(SQL_CREATE_ALARM_COUNT_CREATE_TRIGGER);
            db.execSQL(SQL_CREATE_ALARM_COUNT_UPDATE_TRIGGER);
            db.execSQL(SQL_CREATE_ALARM_COUNT_DELETE_TRIGGER);

        }
        if (oldVersion < 6)
        {
            db.execSQL("alter table " + Tables.TASKS + " add column " + Tasks.PARENT_ID + " integer;");
            db.execSQL("alter table " + Tables.TASKS + " add column " + Tasks.HAS_ALARMS + " integer;");
            db.execSQL("alter table " + Tables.TASKS + " add column " + Tasks.SORTING + " text;");
        }
        if (oldVersion < 7)
        {
            db.execSQL(SQL_CREATE_LISTS_CLEANUP_TRIGGER);
        }
        if (oldVersion < 8)
        {
            // replace priority 0 by null. We need this to sort the widget properly. Since 0 is the default this is no problem when syncing.
            db.execSQL("update " + Tables.TASKS + " set " + Tasks.PRIORITY + "=null where " + Tasks.PRIORITY + "=0;");
        }
        if (oldVersion < 9)
        {
            // add missing column _UID
            db.execSQL("alter table " + Tables.TASKS + " add column " + Tasks._UID + " integer;");
            // add cleanup trigger for orphaned properties
            db.execSQL(SQL_CREATE_TASK_PROPERTY_CLEANUP_TRIGGER);
        }
        if (oldVersion < 10)
        {
            // add property column to categories_mapping table. Since adding a constraint is not supported by SQLite we have to remove and recreate the entire
            // table
            db.execSQL("drop table " + Tables.CATEGORIES_MAPPING);
            db.execSQL(SQL_CREATE_CATEGORIES_MAPPING_TABLE);
            db.execSQL(SQL_CREATE_CATEGORY_PROPERTY_CLEANUP_TRIGGER);
        }
        if (oldVersion < 11)
        {
            db.execSQL("alter table " + Tables.TASKS + " add column " + Tasks.PINNED + " integer;");
            db.execSQL("alter table " + Tables.TASKS + " add column " + Tasks.HAS_PROPERTIES + " integer;");
        }

        if (oldVersion < 12)
        {
            // rename the local account type
            ContentValues values = new ContentValues(1);
            values.put(TaskLists.ACCOUNT_TYPE, TaskContract.LOCAL_ACCOUNT_TYPE);
            db.update(Tables.LISTS, values, TaskLists.ACCOUNT_TYPE + "=?", new String[] { "LOCAL" });
        }

        if (oldVersion < 13)
        {
            db.execSQL(SQL_CREATE_SYNCSTATE_TABLE);
        }

        if (oldVersion < 14)
        {
            // create a unique index for account name and account type on the sync state table
            db.execSQL(createIndexString(Tables.SYNCSTATE, true, TaskContract.SyncState.ACCOUNT_NAME, TaskContract.SyncState.ACCOUNT_TYPE));
        }

        if (oldVersion < 16)
        {
            db.execSQL(createIndexString(Tables.INSTANCES, false, TaskContract.Instances.INSTANCE_START_SORTING));
            db.execSQL(createIndexString(Tables.INSTANCES, false, TaskContract.Instances.INSTANCE_DUE_SORTING));
        }

        if (oldVersion < 17)
        {
            db.execSQL("alter table " + Tables.INSTANCES + " add column " + TaskContract.Instances.INSTANCE_ORIGINAL_TIME + " integer default 0;");
            db.execSQL(createIndexString(Tables.INSTANCES, false, TaskContract.Instances.INSTANCE_ORIGINAL_TIME));
        }

        if (oldVersion < 18)
        {
            db.execSQL("alter table " + Tables.INSTANCES + " add column " + TaskContract.Instances.DISTANCE_FROM_CURRENT + " integer default 0;");
        }

        if (oldVersion < 19)
        {
            db.execSQL(SQL_CREATE_INSTANCE_CLIENT_VIEW);
        }

        if (oldVersion < 22)
        {
            // create version column, unless it already exists
            if (!new First<>(new TableColumns(Tables.TASKS).value(db), new Equals<>(Tasks.VERSION)).isPresent())
            {
                // create task version column and update trigger
                db.execSQL("alter table " + Tables.TASKS + " add column " + Tasks.VERSION + " Integer default 0;");
                db.execSQL(SQL_CREATE_TASK_VERSION_TRIGGER);
            }
        }

        if (oldVersion < 22)
        {
            db.beginTransaction();
            try
            {
                // make sure we upgrade the instances of every recurring task
                EntityProcessor<TaskAdapter> processor = new Instantiating(new NoOpProcessor<>());
                try (Cursor c = db.query(Tables.TASKS,
                        new String[] {
                                TaskContract.Tasks._ID, Tasks.ORIGINAL_INSTANCE_ID, Tasks.DTSTART, Tasks.DUE, Tasks.DURATION, Tasks.IS_CLOSED, Tasks.TZ,
                                Tasks.IS_ALLDAY, Tasks.RRULE, Tasks.RDATE, Tasks.EXDATE, Tasks.ORIGINAL_INSTANCE_TIME, Tasks.ORIGINAL_INSTANCE_ALLDAY },
                        String.format(Locale.ENGLISH, "%s is null", TaskContract.Tasks.ORIGINAL_INSTANCE_ID),
                        null, null, null, null))
                {
                    while (c.moveToNext())
                    {
                        ContentValues values = new ContentValues();
                        Instantiating.addUpdateRequest(values);
                        TaskAdapter adapter = new CursorContentValuesTaskAdapter(c, values);
                        processor.update(db, adapter, false);
                    }
                }
                db.setTransactionSuccessful();
            }
            finally
            {
                db.endTransaction();
            }
        }

        if (oldVersion < 23)
        {
            db.execSQL("drop view " + Tables.INSTANCE_CLIENT_VIEW + ";");
            db.execSQL(SQL_CREATE_INSTANCE_CLIENT_VIEW);
        }

        // upgrade FTS
        FTSDatabaseHelper.onUpgrade(db, oldVersion, newVersion);

        if (mListener != null)
        {
            mListener.onDatabaseUpdate(db, oldVersion, newVersion);
        }
    }

}
