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

package org.dmfs.tasks.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

import com.maltaisn.recurpicker.Recurrence;
import com.maltaisn.recurpicker.format.RRuleFormatter;
import com.maltaisn.recurpicker.format.RecurrenceFormatter;

import org.dmfs.jems.function.Function;
import org.dmfs.jems.optional.Optional;
import org.dmfs.jems.optional.adapters.FirstPresent;
import org.dmfs.jems.optional.decorators.Mapped;
import org.dmfs.jems.procedure.composite.ForEach;
import org.dmfs.jems.single.combined.Backed;
import org.dmfs.rfc5545.Weekday;
import org.dmfs.rfc5545.recur.Freq;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.FieldDescriptor;
import org.dmfs.tasks.model.TaskFieldAdapters;
import org.dmfs.tasks.model.adapters.RRuleFieldAdapter;
import org.dmfs.tasks.model.layout.LayoutOptions;
import org.dmfs.tasks.widget.recurrence.Conditional;
import org.dmfs.tasks.widget.recurrence.NotRepeating;
import org.dmfs.tasks.widget.recurrence.RecurrencePopupGenerator;
import org.dmfs.tasks.widget.recurrence.RepeatByRule;

import java.text.DateFormat;

import androidx.appcompat.widget.PopupMenu;

import static java.util.Arrays.asList;
import static org.dmfs.jems.optional.elementary.Absent.absent;


/**
 * An editor for the rrule field.
 */
public final class RRuleFieldEditor extends AbstractFieldEditor implements View.OnClickListener
{
    private final static RecurrenceFormatter RECURRENCE_FORMATTER = new RecurrenceFormatter(DateFormat.getDateInstance());
    private final static RRuleFormatter RULE_FORMATTER = new RRuleFormatter();

    private RRuleFieldAdapter mAdapter;
    private Button mButton;

    private final Function<Optional<RecurrenceRule>, String> ruleStringFunction = rule ->
            RECURRENCE_FORMATTER.format(
                    getContext(),
                    new Backed<>(
                            new Mapped<>(
                                    r -> RULE_FORMATTER.parse("RRULE:" + r.toString()),
                                    rule),
                            () -> Recurrence.DOES_NOT_REPEAT).value());

    private final RecurrencePopupGenerator mRecurrencePopupInitializer = new RecurrencePopupGenerator(
            new NotRepeating(ruleStringFunction),
            new RepeatByRule(ruleStringFunction, () -> new RecurrenceRule(Freq.DAILY)),
            new RepeatByRule(ruleStringFunction, () -> new RecurrenceRule(Freq.WEEKLY)),
            new Conditional(
                    dateTime -> dateTime.getDayOfWeek() > 0 && dateTime.getDayOfWeek() < 6, // don't show this on weekends
                    new RepeatByRule(ruleStringFunction, () -> {
                        RecurrenceRule x = new RecurrenceRule(Freq.WEEKLY);
                        x.setByDayPart(asList(
                                new RecurrenceRule.WeekdayNum(0, Weekday.MO),
                                new RecurrenceRule.WeekdayNum(0, Weekday.TU),
                                new RecurrenceRule.WeekdayNum(0, Weekday.WE),
                                new RecurrenceRule.WeekdayNum(0, Weekday.TH),
                                new RecurrenceRule.WeekdayNum(0, Weekday.FR)
                        ));
                        return x;
                    })),
            new RepeatByRule(ruleStringFunction, () -> new RecurrenceRule(Freq.MONTHLY)),
            new RepeatByRule(ruleStringFunction, () -> new RecurrenceRule(Freq.YEARLY))
    );


    public RRuleFieldEditor(Context context)
    {
        super(context);
    }


    public RRuleFieldEditor(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }


    public RRuleFieldEditor(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }


    @Override
    protected void onFinishInflate()
    {
        super.onFinishInflate();
        mButton = findViewById(android.R.id.text1);
        mButton.setOnClickListener(this);

    }


    @Override
    public void setValue(ContentSet values)
    {
        if (mValues != null)
        {
            TaskFieldAdapters.DTSTART_DATETIME.unregisterListener(mValues, this);
            TaskFieldAdapters.DUE_DATETIME.unregisterListener(mValues, this);
        }
        super.setValue(values);
        if (mValues != null)
        {
            TaskFieldAdapters.DTSTART_DATETIME.registerListener(mValues, this, false);
            TaskFieldAdapters.DUE_DATETIME.registerListener(mValues, this, false);
        }
    }


    @Override
    public void setFieldDescription(FieldDescriptor descriptor, LayoutOptions layoutOptions)
    {
        super.setFieldDescription(descriptor, layoutOptions);
        mAdapter = (RRuleFieldAdapter) descriptor.getFieldAdapter();
        mButton.setHint(descriptor.getHint());
    }


    @Override
    public void updateValues()
    {
        // make sure we don't try to store a recurrence rule for a task without start or due date
        if (!new FirstPresent<>(
                TaskFieldAdapters.DTSTART_DATETIME.get(mValues),
                TaskFieldAdapters.DUE_DATETIME.get(mValues)).isPresent())
        {
            mAdapter.set(mValues, absent());
        }
    }


    @Override
    public void onContentChanged(ContentSet contentSet)
    {
        if (!mValues.isInsert()
                || !new FirstPresent<>(
                TaskFieldAdapters.DTSTART_DATETIME.get(mValues),
                TaskFieldAdapters.DUE_DATETIME.get(mValues)).isPresent())
        {
            // for now we only show this for newly inserted tasks, because we still need to implement "this and future", etc.
            setVisibility(GONE);
        }
        else
        {
            setVisibility(VISIBLE);
            if (mValues != null)
            {
                setTitle(mAdapter.get(contentSet));
            }
        }
    }


    @Override
    public void onClick(View v)
    {
        new ForEach<>(new FirstPresent<>(
                TaskFieldAdapters.DTSTART_DATETIME.get(mValues),
                TaskFieldAdapters.DUE_DATETIME.get(mValues)))
                .process(start -> {
                    PopupMenu m = new PopupMenu(getContext(), v);
                    mRecurrencePopupInitializer.value(start, rule -> mAdapter.set(mValues, rule)).process(m.getMenu());
                    m.show();
                });
    }


    private void setTitle(Optional<RecurrenceRule> ruleOptional)
    {
        mButton.setText(ruleStringFunction.value(ruleOptional));
    }

}
