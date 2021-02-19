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

package org.dmfs.tasks.model;

import android.text.format.Time;

import org.dmfs.jems.optional.Optional;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import org.dmfs.tasks.contract.TaskContract;
import org.dmfs.tasks.contract.TaskContract.Tasks;
import org.dmfs.tasks.model.adapters.BooleanFieldAdapter;
import org.dmfs.tasks.model.adapters.ChecklistFieldAdapter;
import org.dmfs.tasks.model.adapters.ColorFieldAdapter;
import org.dmfs.tasks.model.adapters.CustomizedDefaultFieldAdapter;
import org.dmfs.tasks.model.adapters.DateTimeFieldAdapter;
import org.dmfs.tasks.model.adapters.DescriptionFieldAdapter;
import org.dmfs.tasks.model.adapters.DescriptionStringFieldAdapter;
import org.dmfs.tasks.model.adapters.FieldAdapter;
import org.dmfs.tasks.model.adapters.FloatFieldAdapter;
import org.dmfs.tasks.model.adapters.FormattedStringFieldAdapter;
import org.dmfs.tasks.model.adapters.IntegerFieldAdapter;
import org.dmfs.tasks.model.adapters.OptionalLongFieldAdapter;
import org.dmfs.tasks.model.adapters.RRuleFieldAdapter;
import org.dmfs.tasks.model.adapters.StringFieldAdapter;
import org.dmfs.tasks.model.adapters.TimeFieldAdapter;
import org.dmfs.tasks.model.adapters.TimezoneFieldAdapter;
import org.dmfs.tasks.model.adapters.UriFieldAdapter;
import org.dmfs.tasks.model.constraints.AdjustPercentComplete;
import org.dmfs.tasks.model.constraints.After;
import org.dmfs.tasks.model.constraints.BeforeOrShiftTime;
import org.dmfs.tasks.model.constraints.ChecklistConstraint;
import org.dmfs.tasks.model.constraints.DescriptionConstraint;
import org.dmfs.tasks.model.defaults.DefaultAfter;
import org.dmfs.tasks.model.defaults.DefaultBefore;


/**
 * This class holds a static reference for all field adapters. That allows us to use them across different models.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class TaskFieldAdapters
{
    /**
     * Adapter for the all day flag of a task.
     */
    public final static BooleanFieldAdapter ALLDAY = new BooleanFieldAdapter(Tasks.IS_ALLDAY);

    /**
     * Adapter for the percent complete value of a task.
     */
    public final static IntegerFieldAdapter PERCENT_COMPLETE = new IntegerFieldAdapter(Tasks.PERCENT_COMPLETE);

    /**
     * Adapter for the status of a task.
     */
    public final static IntegerFieldAdapter STATUS = (IntegerFieldAdapter) new IntegerFieldAdapter(Tasks.STATUS, Tasks.STATUS_NEEDS_ACTION)
            .addContraint(new AdjustPercentComplete(PERCENT_COMPLETE));

    /**
     * Adapter for the priority value of a task.
     */
    public final static IntegerFieldAdapter PRIORITY = new IntegerFieldAdapter(Tasks.PRIORITY);

    /**
     * Adapter for the classification value of a task.
     */
    public final static IntegerFieldAdapter CLASSIFICATION = new IntegerFieldAdapter(Tasks.CLASSIFICATION);

    /**
     * Adapter for the list name of a task.
     */
    public final static StringFieldAdapter LIST_NAME = new StringFieldAdapter(Tasks.LIST_NAME);

    /**
     * Adapter for the account name of a task.
     */
    public final static StringFieldAdapter ACCOUNT_NAME = new StringFieldAdapter(Tasks.ACCOUNT_NAME);

    /**
     * Adapter for the account type of a task.
     */
    public final static StringFieldAdapter ACCOUNT_TYPE = new StringFieldAdapter(Tasks.ACCOUNT_TYPE);

    /**
     * Adapter for the title of a task.
     */
    public final static StringFieldAdapter TITLE = new StringFieldAdapter(Tasks.TITLE);

    /**
     * Adapter for the location of a task.
     */
    public final static StringFieldAdapter LOCATION = new StringFieldAdapter(Tasks.LOCATION);

    /**
     * Adapter for the description of a task.
     */
    public final static DescriptionStringFieldAdapter DESCRIPTION = new DescriptionStringFieldAdapter(Tasks.DESCRIPTION);

    /**
     * Adapter for the checklist of a task.
     */
    public final static ChecklistFieldAdapter CHECKLIST = (ChecklistFieldAdapter) new ChecklistFieldAdapter(Tasks.DESCRIPTION)
            .addContraint(new ChecklistConstraint(STATUS, PERCENT_COMPLETE));

    /**
     * Adapter for the checklist of a task.
     */
    public final static DescriptionFieldAdapter DESCRIPTION_CHECKLIST = (DescriptionFieldAdapter) new DescriptionFieldAdapter(Tasks.DESCRIPTION)
            .addContraint(new DescriptionConstraint(STATUS, PERCENT_COMPLETE));

    /**
     * Private adapter for the start date of a task. We need this to reference DTSTART from DUE.
     */
    private final static TimeFieldAdapter _DTSTART = new TimeFieldAdapter(Tasks.DTSTART, Tasks.TZ, Tasks.IS_ALLDAY);
    private final static TimeFieldAdapter _DUE = new TimeFieldAdapter(Tasks.DUE, Tasks.TZ, Tasks.IS_ALLDAY);

    /**
     * Adapter for the due date of a task.
     */
    public final static FieldAdapter<Time> DUE = new CustomizedDefaultFieldAdapter<Time>(_DUE, new DefaultAfter(_DTSTART)).addContraint(new After(_DTSTART));

    /**
     * Adapter for the start date of a task.
     */
    public final static FieldAdapter<Time> DTSTART = new CustomizedDefaultFieldAdapter<Time>(_DTSTART, new DefaultBefore(DUE)).addContraint(
            new BeforeOrShiftTime(DUE));

    /**
     * Adapter for the due date of a task.
     */
    public final static FieldAdapter<Optional<DateTime>> DUE_DATETIME = new DateTimeFieldAdapter(Tasks.DUE, Tasks.TZ, Tasks.IS_ALLDAY);

    /**
     * Adapter for the start date of a task.
     */
    public final static FieldAdapter<Optional<DateTime>> DTSTART_DATETIME = new DateTimeFieldAdapter(Tasks.DTSTART, Tasks.TZ, Tasks.IS_ALLDAY);

    /**
     * Adapter for the rrule of a task.
     */
    public final static FieldAdapter<Optional<RecurrenceRule>> RRULE = new RRuleFieldAdapter(Tasks.RRULE);

    /**
     * Adapter for the completed date of a task.
     */
    public final static TimeFieldAdapter COMPLETED = new TimeFieldAdapter(Tasks.COMPLETED, Tasks.TZ, null);

    /**
     * Adapter for the time zone of a task.
     */
    public final static TimezoneFieldAdapter TIMEZONE = new TimezoneFieldAdapter(Tasks.TZ, Tasks.IS_ALLDAY, Tasks.DUE);

    /**
     * Adapter for the URL of a task.
     */
    public final static UriFieldAdapter URL = new UriFieldAdapter(TaskContract.Tasks.URL);

    /**
     * Adapter for the Color of the task.
     */
    public final static IntegerFieldAdapter LIST_COLOR = new ColorFieldAdapter(TaskContract.Tasks.LIST_COLOR, 0.8f);

    /**
     * Adapter for the Raw Color Value of the task.
     */
    public final static IntegerFieldAdapter LIST_COLOR_RAW = new IntegerFieldAdapter(TaskContract.Tasks.LIST_COLOR);

    /**
     * Adpater for the ID of the task.
     */
    public static final IntegerFieldAdapter TASK_ID = new IntegerFieldAdapter(TaskContract.Tasks._ID);

    /**
     * Adpater for the TASK_ID of an instance of a task.
     */
    public static final IntegerFieldAdapter INSTANCE_TASK_ID = new IntegerFieldAdapter(TaskContract.Instances.TASK_ID);

    /**
     * Adpater for the ORIGINAL_INSTANCE_ID of an instance of a task.
     */
    public static final OptionalLongFieldAdapter ORIGINAL_INSTANCE_ID = new OptionalLongFieldAdapter(TaskContract.Instances.ORIGINAL_INSTANCE_ID);

    /**
     * Adapter for the IS_RECURRING flag of an instance.
     */
    public static final BooleanFieldAdapter IS_RECURRING_INSTANCE = new BooleanFieldAdapter(TaskContract.Instances.IS_RECURRING);

    /**
     * Adapter for the IS_CLOSED flag of a task.
     */
    public static final BooleanFieldAdapter IS_CLOSED = new BooleanFieldAdapter(TaskContract.Tasks.IS_CLOSED);

    /**
     * Adapter for the PINNED flag of a task.
     */
    public static final BooleanFieldAdapter PINNED = new BooleanFieldAdapter(TaskContract.Tasks.PINNED);

    /**
     * Adpater for the score (i.e. the relevance) of the task in a search.
     */
    public static final FloatFieldAdapter SCORE = new FloatFieldAdapter(TaskContract.Tasks.SCORE, 0f);

    /**
     * Adatper that contains list name and account name.
     */
    public static final FormattedStringFieldAdapter LIST_AND_ACCOUNT_NAME = new FormattedStringFieldAdapter("%1$s (%2$s)", LIST_NAME, ACCOUNT_NAME);


    /**
     * Private constructor to prevent instantiation.
     */
    private TaskFieldAdapters()
    {
    }
}
