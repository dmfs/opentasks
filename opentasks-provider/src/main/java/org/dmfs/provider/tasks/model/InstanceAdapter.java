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

import org.dmfs.provider.tasks.model.adapters.DateTimeFieldAdapter;
import org.dmfs.provider.tasks.model.adapters.IntegerFieldAdapter;
import org.dmfs.provider.tasks.model.adapters.LongFieldAdapter;
import org.dmfs.provider.tasks.model.adapters.StringFieldAdapter;
import org.dmfs.tasks.contract.TaskContract;
import org.dmfs.tasks.contract.TaskContract.Instances;
import org.dmfs.tasks.contract.TaskContract.Tasks;

import java.util.Collection;
import java.util.HashSet;

import static java.util.Arrays.asList;


/**
 * Adapter to read instance values from primitive data sets like {@link Cursor}s or {@link ContentValues}s.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public interface InstanceAdapter extends EntityAdapter<InstanceAdapter>
{

    Collection<String> INSTANCE_COLUMN_NAMES = new HashSet<>(asList(
            TaskContract.Instances.INSTANCE_START,
            TaskContract.Instances.INSTANCE_START_SORTING,
            TaskContract.Instances.INSTANCE_DUE,
            TaskContract.Instances.INSTANCE_DUE_SORTING,
            TaskContract.Instances.INSTANCE_DURATION,
            TaskContract.Instances.INSTANCE_ORIGINAL_TIME,
            TaskContract.Instances.TASK_ID,
            TaskContract.Instances.DISTANCE_FROM_CURRENT,
            "_id:1"));

    /**
     * Adapter for the row id of a task instance.
     */
    LongFieldAdapter<InstanceAdapter> _ID = new LongFieldAdapter<InstanceAdapter>(Instances._ID);

    /**
     * Adapter for the due date of a task instance.
     */
    DateTimeFieldAdapter<InstanceAdapter> INSTANCE_DUE = new DateTimeFieldAdapter<>(Instances.INSTANCE_DUE, Tasks.TZ, Tasks.IS_ALLDAY);

    /**
     * Adapter for the start date of a task instance.
     */
    DateTimeFieldAdapter<InstanceAdapter> INSTANCE_START = new DateTimeFieldAdapter<>(Instances.INSTANCE_START, Tasks.TZ, Tasks.IS_ALLDAY);

    /**
     * Adapter for the start sorting of a task instance.
     */
    LongFieldAdapter<InstanceAdapter> INSTANCE_START_SORTING = new LongFieldAdapter<>(Instances.INSTANCE_START_SORTING);

    /**
     * Adapter for the due sorting of a task instance.
     */
    LongFieldAdapter<InstanceAdapter> INSTANCE_DUE_SORTING = new LongFieldAdapter<>(Instances.INSTANCE_DUE_SORTING);

    /**
     * Adapter for the original time of a task instance.
     */
    DateTimeFieldAdapter<InstanceAdapter> INSTANCE_ORIGINAL_TIME = new DateTimeFieldAdapter<>(Instances.INSTANCE_ORIGINAL_TIME, Tasks.TZ, Tasks.IS_ALLDAY);

    /**
     * Adapter for the distance of a task instance from the current instance.
     */
    IntegerFieldAdapter<InstanceAdapter> DISTANCE_FROM_CURRENT = new IntegerFieldAdapter<>(Instances.DISTANCE_FROM_CURRENT);

    /**
     * Adapter for the title of a task instance.
     */
    StringFieldAdapter<InstanceAdapter> TITLE = new StringFieldAdapter<>(Tasks.TITLE);

    /**
     * Adapter for the row id of the task.
     */
    LongFieldAdapter<InstanceAdapter> TASK_ID = new LongFieldAdapter<InstanceAdapter>(Instances.TASK_ID);

    @Override
    InstanceAdapter duplicate();

    /**
     * Returns a {@link TaskAdapter} for the task component of the instanced view.
     *
     * @return
     */
    TaskAdapter taskAdapter();
}
