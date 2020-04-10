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

package org.dmfs.provider.tasks.model;

import android.content.ContentValues;
import android.database.Cursor;

import org.dmfs.provider.tasks.model.adapters.BinaryFieldAdapter;
import org.dmfs.provider.tasks.model.adapters.BooleanFieldAdapter;
import org.dmfs.provider.tasks.model.adapters.DateTimeFieldAdapter;
import org.dmfs.provider.tasks.model.adapters.DateTimeIterableFieldAdapter;
import org.dmfs.provider.tasks.model.adapters.DurationFieldAdapter;
import org.dmfs.provider.tasks.model.adapters.IntegerFieldAdapter;
import org.dmfs.provider.tasks.model.adapters.LongFieldAdapter;
import org.dmfs.provider.tasks.model.adapters.RRuleFieldAdapter;
import org.dmfs.provider.tasks.model.adapters.StringFieldAdapter;
import org.dmfs.provider.tasks.model.adapters.UrlFieldAdapter;
import org.dmfs.tasks.contract.TaskContract;
import org.dmfs.tasks.contract.TaskContract.Instances;
import org.dmfs.tasks.contract.TaskContract.Tasks;


/**
 * Adapter to read task values from primitive data sets like {@link Cursor}s or {@link ContentValues}s.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public interface TaskAdapter extends EntityAdapter<TaskAdapter>
{
    /**
     * Adapter for the row id of a task.
     */
    LongFieldAdapter<TaskAdapter> _ID = new LongFieldAdapter<TaskAdapter>(Tasks._ID);

    /**
     * Adapter for the version of a task.
     */
    LongFieldAdapter<TaskAdapter> VERSION = new LongFieldAdapter<>(Tasks.VERSION);

    /**
     * Adapter for the task row id of as instance.
     */
    LongFieldAdapter<TaskAdapter> INSTANCE_TASK_ID = new LongFieldAdapter<TaskAdapter>(Instances.TASK_ID);

    /**
     * Adapter for the row id of the list of a task.
     */
    LongFieldAdapter<TaskAdapter> LIST_ID = new LongFieldAdapter<TaskAdapter>(Tasks.LIST_ID);

    /**
     * Adapter for the owner of the list of a task.
     */
    StringFieldAdapter<TaskAdapter> LIST_OWNER = new StringFieldAdapter<TaskAdapter>(Tasks.LIST_OWNER);

    /**
     * Adapter for the row id of original instance of a task.
     */
    LongFieldAdapter<TaskAdapter> ORIGINAL_INSTANCE_ID = new LongFieldAdapter<TaskAdapter>(Tasks.ORIGINAL_INSTANCE_ID);

    /**
     * Adapter for the sync_id of original instance of a task.
     */
    StringFieldAdapter<TaskAdapter> ORIGINAL_INSTANCE_SYNC_ID = new StringFieldAdapter<TaskAdapter>(Tasks.ORIGINAL_INSTANCE_SYNC_ID);

    /**
     * Adapter for the original instance all day flag of a task.
     */
    BooleanFieldAdapter<TaskAdapter> ORIGINAL_INSTANCE_ALLDAY = new BooleanFieldAdapter<TaskAdapter>(Tasks.ORIGINAL_INSTANCE_ALLDAY);

    /**
     * Adapter for the parent_id of a task.
     */
    LongFieldAdapter<TaskAdapter> PARENT_ID = new LongFieldAdapter<TaskAdapter>(Tasks.PARENT_ID);

    /**
     * Adapter for the all day flag of a task.
     */
    BooleanFieldAdapter<TaskAdapter> IS_ALLDAY = new BooleanFieldAdapter<TaskAdapter>(Tasks.IS_ALLDAY);

    /**
     * Adapter for the percent complete value of a task.
     */
    IntegerFieldAdapter<TaskAdapter> PERCENT_COMPLETE = new IntegerFieldAdapter<TaskAdapter>(Tasks.PERCENT_COMPLETE);

    /**
     * Adapter for the status of a task.
     */
    IntegerFieldAdapter<TaskAdapter> STATUS = new IntegerFieldAdapter<TaskAdapter>(Tasks.STATUS);

    /**
     * Adapter for the priority value of a task.
     */
    IntegerFieldAdapter<TaskAdapter> PRIORITY = new IntegerFieldAdapter<TaskAdapter>(Tasks.PRIORITY);

    /**
     * Adapter for the classification value of a task.
     */
    IntegerFieldAdapter<TaskAdapter> CLASSIFICATION = new IntegerFieldAdapter<TaskAdapter>(Tasks.CLASSIFICATION);

    /**
     * Adapter for the list name of a task.
     */
    StringFieldAdapter<TaskAdapter> LIST_NAME = new StringFieldAdapter<TaskAdapter>(Tasks.LIST_NAME);

    /**
     * Adapter for the account name of a task.
     */
    StringFieldAdapter<TaskAdapter> ACCOUNT_NAME = new StringFieldAdapter<TaskAdapter>(Tasks.ACCOUNT_NAME);

    /**
     * Adapter for the account type of a task.
     */
    StringFieldAdapter<TaskAdapter> ACCOUNT_TYPE = new StringFieldAdapter<TaskAdapter>(Tasks.ACCOUNT_TYPE);

    /**
     * Adapter for the title of a task.
     */
    StringFieldAdapter<TaskAdapter> TITLE = new StringFieldAdapter<TaskAdapter>(Tasks.TITLE);

    /**
     * Adapter for the location of a task.
     */
    StringFieldAdapter<TaskAdapter> LOCATION = new StringFieldAdapter<TaskAdapter>(Tasks.LOCATION);

    /**
     * Adapter for the description of a task.
     */
    StringFieldAdapter<TaskAdapter> DESCRIPTION = new StringFieldAdapter<TaskAdapter>(Tasks.DESCRIPTION);

    /**
     * Adapter for the start date of a task.
     */
    DateTimeFieldAdapter<TaskAdapter> DTSTART = new DateTimeFieldAdapter<TaskAdapter>(Tasks.DTSTART, Tasks.TZ, Tasks.IS_ALLDAY);

    /**
     * Adapter for the original date of a task.
     */
    DateTimeFieldAdapter<TaskAdapter> ORIGINAL_INSTANCE_TIME = new DateTimeFieldAdapter<TaskAdapter>(Tasks.ORIGINAL_INSTANCE_TIME, Tasks.TZ,
            Tasks.ORIGINAL_INSTANCE_ALLDAY);

    /**
     * Adapter for the raw start date timestamp of a task.
     */
    LongFieldAdapter<TaskAdapter> DTSTART_RAW = new LongFieldAdapter<TaskAdapter>(Tasks.DTSTART);

    /**
     * Adapter for the due date of a task.
     */
    DateTimeFieldAdapter<TaskAdapter> DUE = new DateTimeFieldAdapter<TaskAdapter>(Tasks.DUE, Tasks.TZ, Tasks.IS_ALLDAY);

    /**
     * Adapter for the raw due date timestamp of a task.
     */
    LongFieldAdapter<TaskAdapter> DUE_RAW = new LongFieldAdapter<TaskAdapter>(Tasks.DUE);

    /**
     * Adapter for the start date of a task.
     */
    DurationFieldAdapter<TaskAdapter> DURATION = new DurationFieldAdapter<TaskAdapter>(Tasks.DURATION);

    /**
     * Adapter for the dirty flag of a task.
     */
    BooleanFieldAdapter<TaskAdapter> _DIRTY = new BooleanFieldAdapter<TaskAdapter>(Tasks._DIRTY);

    /**
     * Adapter for the deleted flag of a task.
     */
    BooleanFieldAdapter<TaskAdapter> _DELETED = new BooleanFieldAdapter<TaskAdapter>(Tasks._DELETED);

    /**
     * Adapter for the completed date of a task.
     */
    DateTimeFieldAdapter<TaskAdapter> COMPLETED = new DateTimeFieldAdapter<TaskAdapter>(Tasks.COMPLETED, null, null);

    /**
     * Adapter for the created date of a task.
     */
    DateTimeFieldAdapter<TaskAdapter> CREATED = new DateTimeFieldAdapter<TaskAdapter>(Tasks.CREATED, null, null);

    /**
     * Adapter for the last modified date of a task.
     */
    DateTimeFieldAdapter<TaskAdapter> LAST_MODIFIED = new DateTimeFieldAdapter<TaskAdapter>(Tasks.LAST_MODIFIED, null, null);

    /**
     * Adapter for the URL of a task.
     */
    UrlFieldAdapter<TaskAdapter> URL = new UrlFieldAdapter<TaskAdapter>(TaskContract.Tasks.URL);

    /**
     * Adapter for the UID of a task.
     */
    StringFieldAdapter<TaskAdapter> _UID = new StringFieldAdapter<TaskAdapter>(TaskContract.Tasks._UID);

    /**
     * Adapter for the raw time zone of a task.
     */
    StringFieldAdapter<TaskAdapter> TIMEZONE_RAW = new StringFieldAdapter<TaskAdapter>(TaskContract.Tasks.TZ);

    /**
     * Adapter for the Color of the task.
     */
    IntegerFieldAdapter<TaskAdapter> LIST_COLOR = new IntegerFieldAdapter<TaskAdapter>(TaskContract.Tasks.LIST_COLOR);

    /**
     * Adapter for the access level of the task list.
     */
    IntegerFieldAdapter<TaskAdapter> LIST_ACCESS_LEVEL = new IntegerFieldAdapter<TaskAdapter>(TaskContract.Tasks.LIST_ACCESS_LEVEL);

    /**
     * Adapter for the visibility setting of the task list.
     */
    BooleanFieldAdapter<TaskAdapter> LIST_VISIBLE = new BooleanFieldAdapter<TaskAdapter>(TaskContract.Tasks.VISIBLE);

    /**
     * Adpater for the ID of the task.
     */
    IntegerFieldAdapter<TaskAdapter> TASK_ID = new IntegerFieldAdapter<TaskAdapter>(TaskContract.Tasks._ID);

    /**
     * Adapter for the IS_CLOSED flag of a task.
     */
    BooleanFieldAdapter<TaskAdapter> IS_CLOSED = new BooleanFieldAdapter<TaskAdapter>(TaskContract.Tasks.IS_CLOSED);

    /**
     * Adapter for the IS_NEW flag of a task.
     */
    BooleanFieldAdapter<TaskAdapter> IS_NEW = new BooleanFieldAdapter<TaskAdapter>(TaskContract.Tasks.IS_NEW);

    /**
     * Adapter for the PINNED flag of a task.
     */
    BooleanFieldAdapter<TaskAdapter> PINNED = new BooleanFieldAdapter<TaskAdapter>(TaskContract.Tasks.PINNED);

    /**
     * Adapter for the HAS_ALARMS flag of a task.
     */
    BooleanFieldAdapter<TaskAdapter> HAS_ALARMS = new BooleanFieldAdapter<TaskAdapter>(TaskContract.Tasks.HAS_ALARMS);

    /**
     * Adapter for the HAS_PROPERTIES flag of a task.
     */
    BooleanFieldAdapter<TaskAdapter> HAS_PROPERTIES = new BooleanFieldAdapter<TaskAdapter>(TaskContract.Tasks.HAS_PROPERTIES);

    /**
     * Adapter for the RRULE of a task.
     */
    RRuleFieldAdapter<TaskAdapter> RRULE = new RRuleFieldAdapter<TaskAdapter>(TaskContract.Tasks.RRULE);

    /**
     * Adapter for the RDATE of a task.
     */
    DateTimeIterableFieldAdapter<TaskAdapter> RDATE = new DateTimeIterableFieldAdapter<TaskAdapter>(TaskContract.Tasks.RDATE,
            TaskContract.Tasks.TZ);

    /**
     * Adapter for the EXDATE of a task.
     */
    DateTimeIterableFieldAdapter<TaskAdapter> EXDATE = new DateTimeIterableFieldAdapter<TaskAdapter>(TaskContract.Tasks.EXDATE,
            TaskContract.Tasks.TZ);

    /**
     * Adapter for the SYNC1 field of a task.
     */
    BinaryFieldAdapter<TaskAdapter> SYNC1 = new BinaryFieldAdapter<TaskAdapter>(TaskContract.Tasks.SYNC1);

    /**
     * Adapter for the SYNC2 field of a task.
     */
    BinaryFieldAdapter<TaskAdapter> SYNC2 = new BinaryFieldAdapter<TaskAdapter>(TaskContract.Tasks.SYNC2);

    /**
     * Adapter for the SYNC3 field of a task.
     */
    BinaryFieldAdapter<TaskAdapter> SYNC3 = new BinaryFieldAdapter<TaskAdapter>(TaskContract.Tasks.SYNC3);

    /**
     * Adapter for the SYNC4 field of a task.
     */
    BinaryFieldAdapter<TaskAdapter> SYNC4 = new BinaryFieldAdapter<TaskAdapter>(TaskContract.Tasks.SYNC4);

    /**
     * Adapter for the SYNC5 field of a task.
     */
    BinaryFieldAdapter<TaskAdapter> SYNC5 = new BinaryFieldAdapter<TaskAdapter>(TaskContract.Tasks.SYNC5);

    /**
     * Adapter for the SYNC6 field of a task.
     */
    BinaryFieldAdapter<TaskAdapter> SYNC6 = new BinaryFieldAdapter<TaskAdapter>(TaskContract.Tasks.SYNC6);

    /**
     * Adapter for the SYNC7 field of a task.
     */
    BinaryFieldAdapter<TaskAdapter> SYNC7 = new BinaryFieldAdapter<TaskAdapter>(TaskContract.Tasks.SYNC7);

    /**
     * Adapter for the SYNC8 field of a task.
     */
    BinaryFieldAdapter<TaskAdapter> SYNC8 = new BinaryFieldAdapter<TaskAdapter>(TaskContract.Tasks.SYNC8);

    /**
     * Adapter for the SYNC_VERSION field of a task.
     */
    BinaryFieldAdapter<TaskAdapter> SYNC_VERSION = new BinaryFieldAdapter<TaskAdapter>(TaskContract.Tasks.SYNC_VERSION);

    /**
     * Adapter for the SYNC_ID field of a task.
     */
    StringFieldAdapter<TaskAdapter> SYNC_ID = new StringFieldAdapter<TaskAdapter>(TaskContract.Tasks._SYNC_ID);

    /**
     * Adapter for the due date of a task instance.
     */
    DateTimeFieldAdapter<TaskAdapter> INSTANCE_DUE = new DateTimeFieldAdapter<TaskAdapter>(Instances.INSTANCE_DUE, Tasks.TZ,
            Tasks.IS_ALLDAY);

    /**
     * Adapter for the start date of a task instance.
     */
    DateTimeFieldAdapter<TaskAdapter> INSTANCE_START = new DateTimeFieldAdapter<TaskAdapter>(Instances.INSTANCE_START, Tasks.TZ,
            Tasks.IS_ALLDAY);

    /**
     * Returns whether the adapted task is recurring.
     *
     * @return <code>true</code> if the task is recurring, <code>false</code> otherwise.
     */
    boolean isRecurring();

    /**
     * Returns whether any value that's relevant for recurrence has been modified thought this adapter. This returns true if any of
     * {@link TaskContract.TaskColumns#DTSTART}, {@link TaskContract.TaskColumns#DUE},{@link TaskContract.TaskColumns#DURATION},
     * {@link TaskContract.TaskColumns#RRULE}, {@link TaskContract.TaskColumns#RDATE} or {@link TaskContract.TaskColumns#EXDATE} has been modified.
     *
     * @return <code>true</code> if the recurrence set has changed, <code>false</code> otherwise.
     */
    boolean recurrenceUpdated();

    /***
     * Creates a {@link TaskAdapter} for a new task initialized with the values of this task (except for _ID).
     *
     * @return A new task having the same values.
     */
    @Override
    TaskAdapter duplicate();
}
