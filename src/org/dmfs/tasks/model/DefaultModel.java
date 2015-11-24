/*
 * Copyright (C) 2013 Marten Gajda <marten@dmfs.org>
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

import org.dmfs.provider.tasks.TaskContract.Tasks;
import org.dmfs.tasks.R;
import org.dmfs.tasks.model.layout.LayoutDescriptor;

import android.content.Context;
import android.text.util.Linkify;


/**
 * The default model for sync adapters that don't provide a model definition.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public class DefaultModel extends Model
{
	final static LayoutDescriptor TEXT_VIEW = new LayoutDescriptor(R.layout.text_field_view).setOption(LayoutDescriptor.OPTION_LINKIFY, Linkify.ALL);
	final static LayoutDescriptor TEXT_VIEW_NO_LINKS = new LayoutDescriptor(R.layout.text_field_view).setOption(LayoutDescriptor.OPTION_LINKIFY, 0);
	private final static LayoutDescriptor TEXT_EDIT = new LayoutDescriptor(R.layout.text_field_editor);
	private final static LayoutDescriptor TEXT_EDIT_SINGLE_LINE = new LayoutDescriptor(R.layout.text_field_editor).setOption(LayoutDescriptor.OPTION_MULTILINE,
		false);
	private final static LayoutDescriptor CHECKLIST_VIEW = new LayoutDescriptor(R.layout.checklist_field_view);
	private final static LayoutDescriptor CHECKLIST_EDIT = new LayoutDescriptor(R.layout.checklist_field_editor);
	private final static LayoutDescriptor CHOICES_VIEW = new LayoutDescriptor(R.layout.choices_field_view);
	private final static LayoutDescriptor CHOICES_EDIT = new LayoutDescriptor(R.layout.choices_field_editor);
	private final static LayoutDescriptor PROGRESS_VIEW = new LayoutDescriptor(R.layout.percentage_field_view);
	private final static LayoutDescriptor PROGRESS_EDIT = new LayoutDescriptor(R.layout.percentage_field_editor);
	private final static LayoutDescriptor TIME_VIEW = new LayoutDescriptor(R.layout.time_field_view);
	private final static LayoutDescriptor TIME_VIEW_ADD_BUTTON = new LayoutDescriptor(R.layout.time_field_view).setOption(
		LayoutDescriptor.OPTION_TIME_FIELD_SHOW_ADD_BUTTONS, true);
	private final static LayoutDescriptor TIME_EDIT = new LayoutDescriptor(R.layout.time_field_editor);
	@SuppressWarnings("unused")
	private final static LayoutDescriptor BOOLEAN_VIEW = new LayoutDescriptor(R.layout.boolean_field_view);
	private final static LayoutDescriptor BOOLEAN_EDIT = new LayoutDescriptor(R.layout.boolean_field_editor);
	private final static LayoutDescriptor URL_VIEW = new LayoutDescriptor(R.layout.url_field_view);
	private final static LayoutDescriptor URL_EDIT = new LayoutDescriptor(R.layout.url_field_editor);

	final static LayoutDescriptor LIST_COLOR_VIEW = new LayoutDescriptor(R.layout.list_color_view);


	public DefaultModel(Context context, String accountType)
	{
		super(context, accountType);
	}


	@Override
	public void inflate()
	{
		if (mInflated)
		{
			return;
		}

		Context context = getContext();

		/*
		 * Add a couple of fields to the model.
		 */
		// task list color
		addField(new FieldDescriptor(context, R.id.task_field_list_color, R.string.task_list, null, TaskFieldAdapters.LIST_COLOR)
			.setViewLayout(LIST_COLOR_VIEW).setEditorLayout(LIST_COLOR_VIEW).setNoAutoAdd(true));

		// task list name
		addField(new FieldDescriptor(context, R.id.task_field_list_name, R.string.task_list, null, TaskFieldAdapters.LIST_NAME).setViewLayout(
			new LayoutDescriptor(R.layout.text_field_view_nodivider_large).setOption(LayoutDescriptor.OPTION_NO_TITLE, true)).setNoAutoAdd(true));
		// account name
		addField(new FieldDescriptor(context, R.id.task_field_account_name, R.string.task_list, null, TaskFieldAdapters.ACCOUNT_NAME).setViewLayout(
			new LayoutDescriptor(R.layout.text_field_view_nodivider_small).setOption(LayoutDescriptor.OPTION_NO_TITLE, true)).setNoAutoAdd(true));

		// task title
		addField(new FieldDescriptor(context, R.id.task_field_title, R.string.task_title, TaskFieldAdapters.TITLE).setEditorLayout(TEXT_EDIT_SINGLE_LINE));

		ArrayChoicesAdapter aca = new ArrayChoicesAdapter();
		aca.addHiddenChoice(null, context.getString(R.string.status_needs_action), null);
		aca.addChoice(Tasks.STATUS_NEEDS_ACTION, context.getString(R.string.status_needs_action), null);
		aca.addChoice(Tasks.STATUS_IN_PROCESS, context.getString(R.string.status_in_process), null);
		aca.addChoice(Tasks.STATUS_COMPLETED, context.getString(R.string.status_completed), null);
		aca.addChoice(Tasks.STATUS_CANCELLED, context.getString(R.string.status_cancelled), null);

		// status
		addField(new FieldDescriptor(context, R.id.task_field_status, R.string.task_status, TaskFieldAdapters.STATUS).setViewLayout(CHOICES_VIEW)
			.setEditorLayout(CHOICES_EDIT).setChoices(aca).setIcon(R.drawable.ic_detail_status));

		// location
		addField(new FieldDescriptor(context, R.id.task_field_location, R.string.task_location, TaskFieldAdapters.LOCATION).setViewLayout(TEXT_VIEW)
			.setEditorLayout(TEXT_EDIT).setIcon(R.drawable.ic_detail_location));

		// description
		addField(new FieldDescriptor(context, R.id.task_field_description, R.string.task_description, TaskFieldAdapters.DESCRIPTION)
			.setViewLayout(TEXT_VIEW.setOption(LayoutDescriptor.OPTION_LINKIFY, Linkify.ALL)).setEditorLayout(TEXT_EDIT)
			.setIcon(R.drawable.ic_detail_description));

		// description
		addField(new FieldDescriptor(context, R.id.task_field_checklist, R.string.task_checklist, TaskFieldAdapters.CHECKLIST).setViewLayout(CHECKLIST_VIEW)
			.setEditorLayout(CHECKLIST_EDIT).setIcon(R.drawable.ic_detail_checklist));

		// start
		addField(new FieldDescriptor(context, R.id.task_field_dtstart, R.string.task_start, TaskFieldAdapters.DTSTART).setViewLayout(TIME_VIEW)
			.setEditorLayout(TIME_EDIT).setIcon(R.drawable.ic_detail_start));

		// due
		addField(new FieldDescriptor(context, R.id.task_field_due, R.string.task_due, TaskFieldAdapters.DUE).setEditorLayout(TIME_EDIT).setIcon(
			R.drawable.ic_detail_due));

		// all day flag
		addField(new FieldDescriptor(context, R.id.task_field_all_day, R.string.task_all_day, TaskFieldAdapters.ALLDAY).setEditorLayout(BOOLEAN_EDIT));

		TimeZoneChoicesAdapter tzaca = new TimeZoneChoicesAdapter(context);
		// time zone
		addField(new FieldDescriptor(context, R.id.task_field_timezone, R.string.task_timezone, TaskFieldAdapters.TIMEZONE).setEditorLayout(CHOICES_EDIT)
			.setChoices(tzaca));

		// completed
		addField(new FieldDescriptor(context, R.id.task_field_completed, R.string.task_completed, TaskFieldAdapters.COMPLETED).setViewLayout(TIME_VIEW)
			.setEditorLayout(TIME_EDIT).setIcon(R.drawable.ic_detail_completed));

		// percent complete
		addField(new FieldDescriptor(context, R.id.task_field_percent_complete, R.string.task_percent_complete, TaskFieldAdapters.PERCENT_COMPLETE)
			.setViewLayout(PROGRESS_VIEW).setEditorLayout(PROGRESS_EDIT).setIcon(R.drawable.ic_detail_progress));

		ArrayChoicesAdapter aca2 = new ArrayChoicesAdapter();
		aca2.addChoice(null, context.getString(R.string.priority_undefined), null);
		aca2.addHiddenChoice(0, context.getString(R.string.priority_undefined), null);
		aca2.addChoice(9, context.getString(R.string.priority_low), null);
		aca2.addHiddenChoice(8, context.getString(R.string.priority_low), null);
		aca2.addHiddenChoice(7, context.getString(R.string.priority_low), null);
		aca2.addHiddenChoice(6, context.getString(R.string.priority_low), null);
		aca2.addChoice(5, context.getString(R.string.priority_medium), null);
		aca2.addHiddenChoice(4, context.getString(R.string.priority_high), null);
		aca2.addHiddenChoice(3, context.getString(R.string.priority_high), null);
		aca2.addHiddenChoice(2, context.getString(R.string.priority_high), null);
		aca2.addChoice(1, context.getString(R.string.priority_high), null);

		// priority
		addField(new FieldDescriptor(context, R.id.task_field_priority, R.string.task_priority, TaskFieldAdapters.PRIORITY).setViewLayout(CHOICES_VIEW)
			.setEditorLayout(CHOICES_EDIT).setChoices(aca2).setIcon(R.drawable.ic_detail_priority));

		ArrayChoicesAdapter aca3 = new ArrayChoicesAdapter();
		aca3.addChoice(null, context.getString(R.string.classification_not_specified), null);
		aca3.addChoice(Tasks.CLASSIFICATION_PUBLIC, context.getString(R.string.classification_public), null);
		aca3.addChoice(Tasks.CLASSIFICATION_PRIVATE, context.getString(R.string.classification_private), null);
		aca3.addChoice(Tasks.CLASSIFICATION_CONFIDENTIAL, context.getString(R.string.classification_confidential), null);

		// privacy
		addField(new FieldDescriptor(context, R.id.task_field_classification, R.string.task_classification, TaskFieldAdapters.CLASSIFICATION)
			.setViewLayout(CHOICES_VIEW).setEditorLayout(CHOICES_EDIT).setChoices(aca3).setIcon(R.drawable.ic_detail_visibility));

		// url
		addField(new FieldDescriptor(context, R.id.task_field_url, R.string.task_url, TaskFieldAdapters.URL).setViewLayout(URL_VIEW).setEditorLayout(URL_EDIT)
			.setIcon(R.drawable.ic_detail_url));

		// task list name
		addField(new FieldDescriptor(context, R.id.task_field_list_and_account_name, R.string.task_list, null, TaskFieldAdapters.LIST_AND_ACCOUNT_NAME)
			.setViewLayout(TEXT_VIEW_NO_LINKS).setIcon(R.drawable.ic_detail_list));

		setAllowRecurrence(false);
		setAllowExceptions(false);

		mInflated = true;
	}
}
