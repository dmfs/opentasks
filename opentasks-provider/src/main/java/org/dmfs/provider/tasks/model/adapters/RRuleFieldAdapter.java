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

package org.dmfs.provider.tasks.model.adapters;

import android.content.ContentValues;
import android.database.Cursor;

import org.dmfs.rfc5545.recur.InvalidRecurrenceRuleException;
import org.dmfs.rfc5545.recur.RecurrenceRule;


/**
 * Knows how to load and store a {@link RecurrenceRule} from a {@link Cursor} or {@link ContentValues}.
 *
 * @param <EntityType>
 *         The type of the entity the field belongs to.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class RRuleFieldAdapter<EntityType> extends SimpleFieldAdapter<RecurrenceRule, EntityType>
{

    /**
     * The field name this adapter uses to store the values.
     */
    private final String mFieldName;


    /**
     * Constructor for a new {@link RRuleFieldAdapter}.
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
    }


    @Override
    String fieldName()
    {
        return mFieldName;
    }


    @Override
    public RecurrenceRule getFrom(ContentValues values)
    {
        String rrule = values.getAsString(mFieldName);
        if (rrule == null)
        {
            return null;
        }
        try
        {
            return new RecurrenceRule(rrule);
        }
        catch (InvalidRecurrenceRuleException e)
        {
            throw new IllegalArgumentException("can not parse RRULE '" + rrule + "'", e);
        }
    }


    @Override
    public RecurrenceRule getFrom(Cursor cursor)
    {
        int columnIdx = cursor.getColumnIndex(mFieldName);
        if (columnIdx < 0)
        {
            throw new IllegalArgumentException("The column '" + mFieldName + "' is missing in cursor.");
        }
        if (cursor.isNull(columnIdx))
        {
            return null;
        }

        try
        {
            return new RecurrenceRule(cursor.getString(columnIdx));
        }
        catch (InvalidRecurrenceRuleException e)
        {
            throw new IllegalArgumentException("can not parse RRULE '" + cursor.getString(columnIdx) + "'", e);
        }
    }


    @Override
    public void setIn(ContentValues values, RecurrenceRule value)
    {
        if (value != null)
        {
            values.put(mFieldName, value.toString());
        }
        else
        {
            values.putNull(mFieldName);
        }
    }

}
