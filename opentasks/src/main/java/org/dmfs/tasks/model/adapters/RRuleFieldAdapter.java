/*
 * Copyright 2018 dmfs GmbH
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

package org.dmfs.tasks.model.adapters;

import android.content.ContentValues;
import android.database.Cursor;

import org.dmfs.jems.optional.decorators.Mapped;
import org.dmfs.optional.NullSafe;
import org.dmfs.rfc5545.recur.InvalidRecurrenceRuleException;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.OnContentChangeListener;


/**
 * Knows how to load and store a {@link RecurrenceRule} value in a certain field of a {@link ContentSet}.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public class RRuleFieldAdapter extends FieldAdapter<RecurrenceRule>
{

    /**
     * The field name this adapter uses to store the values.
     */
    private final String mFieldName;

    /**
     * The default value, if any.
     */
    private final RecurrenceRule mDefaultValue;


    /**
     * Constructor for a new StringFieldAdapter without default value.
     *
     * @param fieldName
     *         The name of the field to use when loading or storing the value.
     */
    public RRuleFieldAdapter(String fieldName)
    {
        if (fieldName == null)
        {
            throw new IllegalArgumentException("fieldName must not be null");
        }
        mFieldName = fieldName;
        mDefaultValue = null;
    }


    /**
     * Constructor for a new StringFieldAdapter with default value.
     *
     * @param fieldName
     *         The name of the field to use when loading or storing the value.
     * @param defaultValue
     *         The default value.
     */
    public RRuleFieldAdapter(String fieldName, RecurrenceRule defaultValue)
    {
        if (fieldName == null)
        {
            throw new IllegalArgumentException("fieldName must not be null");
        }
        mFieldName = fieldName;
        mDefaultValue = defaultValue;
    }


    @Override
    public RecurrenceRule get(ContentSet values)
    {
        return new Mapped<>((rule) ->
        {
            try
            {
                return new RecurrenceRule(rule, RecurrenceRule.RfcMode.RFC5545_LAX);
            }
            catch (InvalidRecurrenceRuleException e)
            {
                throw new RuntimeException("Got invalid recurrence rule", e);
            }
        }, new NullSafe<>(values.getAsString(mFieldName))).value(null);
    }


    @Override
    public RecurrenceRule get(Cursor cursor)
    {
        int columnIdx = cursor.getColumnIndex(mFieldName);
        if (columnIdx < 0)
        {
            throw new IllegalArgumentException("The fieldName column missing in cursor.");
        }
        return new Mapped<>((rule) ->
        {
            try
            {
                return new RecurrenceRule(rule, RecurrenceRule.RfcMode.RFC5545_LAX);
            }
            catch (InvalidRecurrenceRuleException e)
            {
                throw new RuntimeException("Got invalid recurrence rule", e);
            }
        }, new NullSafe<>(cursor.getString(columnIdx))).value(null);
    }


    @Override
    public RecurrenceRule getDefault(ContentSet values)
    {
        return mDefaultValue;
    }


    @Override
    public void set(ContentSet values, RecurrenceRule value)
    {
        values.put(mFieldName, new Mapped<>(RecurrenceRule::toString, new NullSafe<>(value)).value(null));
    }


    @Override
    public void set(ContentValues values, RecurrenceRule value)
    {
        values.put(mFieldName, new Mapped<>(RecurrenceRule::toString, new NullSafe<>(value)).value(null));
    }


    @Override
    public void registerListener(ContentSet values, OnContentChangeListener listener, boolean initalNotification)
    {
        values.addOnChangeListener(listener, mFieldName, initalNotification);
    }


    @Override
    public void unregisterListener(ContentSet values, OnContentChangeListener listener)
    {
        values.removeOnChangeListener(listener, mFieldName);
    }
}
