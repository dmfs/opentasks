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

package org.dmfs.tasks.model.adapters;

import android.content.ContentValues;
import android.database.Cursor;

import org.dmfs.jems.optional.Optional;
import org.dmfs.jems.optional.decorators.Mapped;
import org.dmfs.jems.optional.elementary.NullSafe;
import org.dmfs.jems.optional.elementary.Present;
import org.dmfs.jems.single.adapters.Unchecked;
import org.dmfs.rfc5545.recur.InvalidRecurrenceRuleException;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.OnContentChangeListener;

import static org.dmfs.jems.optional.elementary.Absent.absent;


/**
 * Knows how to load and store a {@link String} value in a certain field of a {@link ContentSet}.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public class RRuleFieldAdapter extends FieldAdapter<Optional<RecurrenceRule>>
{

    /**
     * The field name this adapter uses to store the values.
     */
    private final String mFieldName;

    /**
     * The default value, if any.
     */
    private final String mDefaultValue;


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
    public RRuleFieldAdapter(String fieldName, String defaultValue)
    {
        if (fieldName == null)
        {
            throw new IllegalArgumentException("fieldName must not be null");
        }
        mFieldName = fieldName;
        mDefaultValue = defaultValue;
    }


    @Override
    public Optional<RecurrenceRule> get(ContentSet values)
    {
        return new Mapped<>(rrule -> new Unchecked<>(() -> new RecurrenceRule(rrule)).value(), new NullSafe<>(values.getAsString(mFieldName)));
    }


    @Override
    public Optional<RecurrenceRule> get(Cursor cursor)
    {
        int columnIdx = cursor.getColumnIndex(mFieldName);
        if (columnIdx < 0)
        {
            throw new IllegalArgumentException("The fieldName column missing in cursor.");
        }
        try
        {
            return cursor.isNull(columnIdx) ? absent() : new Present<>(new RecurrenceRule(cursor.getString(columnIdx)));
        }
        catch (InvalidRecurrenceRuleException e)
        {
            return absent();
        }
    }


    @Override
    public Optional<RecurrenceRule> getDefault(ContentSet values)
    {
        return absent();
    }


    @Override
    public void set(ContentSet values, Optional<RecurrenceRule> value)
    {
        values.put(mFieldName, value.isPresent() ? value.value().toString() : null);
    }


    @Override
    public void set(ContentValues values, Optional<RecurrenceRule> value)
    {
        values.put(mFieldName, value.isPresent() ? value.value().toString() : null);
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
