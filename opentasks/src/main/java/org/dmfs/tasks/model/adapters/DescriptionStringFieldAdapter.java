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

import android.database.Cursor;

import org.dmfs.tasks.model.ContentSet;


/**
 * Knows how to load and store descriptions from/to a combined description/check list field.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class DescriptionStringFieldAdapter extends StringFieldAdapter
{

    /**
     * Constructor for a new DescriptionStringFieldAdapter without default value.
     *
     * @param fieldName
     *         The name of the field to use when loading or storing the value.
     */
    public DescriptionStringFieldAdapter(String fieldName)
    {
        super(fieldName);
    }


    /**
     * Constructor for a new StringFieldAdapter with default value.
     *
     * @param fieldName
     *         The name of the field to use when loading or storing the value.
     * @param defaultValue
     *         The default value.
     */
    public DescriptionStringFieldAdapter(String fieldName, String defaultValue)
    {
        super(fieldName, defaultValue);
    }


    @Override
    public String get(ContentSet values)
    {
        return extractDescription(super.get(values));
    }


    @Override
    public String get(Cursor cursor)
    {
        return extractDescription(super.get(cursor));
    }


    @Override
    public void set(ContentSet values, String value)
    {
        String oldValue = super.get(values);
        if (oldValue != null && oldValue.length() > 0)
        {
            String oldDescription = extractDescription(oldValue);
            String oldChecklist = oldValue.substring(oldDescription.length());

            // store the new description with the old check list
            super.set(values, oldChecklist.length() == 0 ? value : oldChecklist.startsWith("\n") ? value + oldChecklist : value + "\n" + oldChecklist);
        }
        else
        {
            // there was no old check list
            super.set(values, value);
        }
    }


    /**
     * Extracts the leading description placed before the possible checklists from the combined description-checklist
     * value.
     * <p>
     * Checklist items can start with one of these four markers: [ ], [], [x], [X] (unchecked and checked items)
     * Checklist is only identified as one if a new line character precedes it or it starts the value ('[' is the first
     * char).
     *
     * @param value
     *         the combined value of the description + possible checklist items.
     *
     * @return the description without the checklists
     */
    static String extractDescription(String value)
    {
        if (value == null || value.length() < 2)
        {
            return value;
        }
        int valueLen = value.length();

        // check if checklist start right away, so there is no description
        if (value.charAt(0) == '[' && value.charAt(1) == ']')
        {
            return "";
        }
        if (valueLen > 2 && value.charAt(0) == '[' && value.charAt(2) == ']')
        {
            char checkMark = value.charAt(1);
            if (checkMark == ' ' || checkMark == 'x' || checkMark == 'X')
            {
                return "";
            }
        }

        // check if there is checklist at the rest of the value
        int checklistPos = -1;
        while ((checklistPos = value.indexOf("\n[", checklistPos + 1)) >= 0)
        {
            boolean foundChecklist = false;
            if (checklistPos + 2 < valueLen && value.charAt(checklistPos + 2) == ']')
            {
                foundChecklist = true;
            }
            if (checklistPos + 3 < valueLen && value.charAt(checklistPos + 3) == ']')
            {
                char checkMark = value.charAt(checklistPos + 2);
                if (checkMark == ' ' || checkMark == 'x' || checkMark == 'X')
                {
                    foundChecklist = true;
                }
            }
            if (foundChecklist)
            {
                if (checklistPos > 0 && value.charAt(checklistPos - 1) == 0x0d)
                {
                    // the list was separated by a CR LF sequence, remove the CR
                    --checklistPos;
                }
                return value.substring(0, checklistPos);
            }
        }

        // didn't find a valid check list
        return value;
    }
}
