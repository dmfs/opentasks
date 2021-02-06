/*
 * Copyright 2019 dmfs GmbH
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

import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.DescriptionItem;
import org.dmfs.tasks.model.OnContentChangeListener;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Knows how to load and store check list from/to a combined description/check list field.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class DescriptionFieldAdapter extends FieldAdapter<List<DescriptionItem>>
{
    private final static Pattern CHECKMARK_PATTERN = Pattern.compile("([-*] )?\\[([xX ])\\](.*)");

    /**
     * The field name this adapter uses to store the values.
     */
    private final String mFieldName;

    /**
     * The default value, if any.
     */
    private final List<DescriptionItem> mDefaultValue;


    /**
     * Constructor for a new StringFieldAdapter without default value.
     *
     * @param fieldName
     *         The name of the field to use when loading or storing the value.
     */
    public DescriptionFieldAdapter(String fieldName)
    {
        this(fieldName, null);
    }


    /**
     * Constructor for a new StringFieldAdapter without default value.
     *
     * @param fieldName
     *         The name of the field to use when loading or storing the value.
     * @param defaultValue
     *         The default check list
     */
    public DescriptionFieldAdapter(String fieldName, List<DescriptionItem> defaultValue)
    {
        if (fieldName == null)
        {
            throw new IllegalArgumentException("fieldName must not be null");
        }
        mFieldName = fieldName;
        mDefaultValue = defaultValue;
    }


    @Override
    public List<DescriptionItem> get(ContentSet values)
    {
        // return the check list
        return parseDescription(values.getAsString(mFieldName));
    }


    @Override
    public List<DescriptionItem> get(Cursor cursor)
    {
        int columnIdx = cursor.getColumnIndex(mFieldName);
        if (columnIdx < 0)
        {
            throw new IllegalArgumentException("The fieldName column missing in cursor.");
        }
        return parseDescription(cursor.getString(columnIdx));
    }


    @Override
    public List<DescriptionItem> getDefault(ContentSet values)
    {
        return mDefaultValue;
    }


    @Override
    public void set(ContentSet values, List<DescriptionItem> value)
    {
        if (value != null && !value.isEmpty())
        {
            StringBuilder sb = new StringBuilder(1024);
            serializeDescription(sb, value);

            values.put(mFieldName, sb.toString());
        }
        else
        {
            // store the current value just without check list
            values.put(mFieldName, (String) null);
        }
    }


    @Override
    public void set(ContentValues values, List<DescriptionItem> value)
    {
        if (value != null && !value.isEmpty())
        {
            StringBuilder sb = new StringBuilder(1024);

            serializeDescription(sb, value);

            values.put(mFieldName, sb.toString());
        }
        else
        {
            values.putNull(mFieldName);
        }

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


    private static List<DescriptionItem> parseDescription(String description)
    {
        List<DescriptionItem> result = new ArrayList<DescriptionItem>(16);
        if (description == null)
        {
            return result;
        }
        Matcher matcher = CHECKMARK_PATTERN.matcher("");
        StringBuilder currentParagraph = new StringBuilder();
        boolean currentHasCheckedMark = false;
        boolean currentIsChecked = false;
        for (String line : description.split("\n"))
        {
            matcher.reset(line);

            if (matcher.lookingAt())
            {
                // start a new paragraph, if we already had one
                if (currentParagraph.length() > 0)
                {
                    result.add(new DescriptionItem(currentHasCheckedMark, currentIsChecked,
                            currentHasCheckedMark ? currentParagraph.toString().trim() : currentParagraph.toString()));
                }
                currentHasCheckedMark = true;
                currentIsChecked = "x".equals(matcher.group(2).toLowerCase());
                currentParagraph.setLength(0);
                currentParagraph.append(matcher.group(3));
            }
            else
            {
                if (currentHasCheckedMark)
                {
                    // start a new paragraph, if the last one had a tick mark
                    if (currentParagraph.length() > 0)
                    {
                        // close last paragraph
                        result.add(new DescriptionItem(currentHasCheckedMark, currentIsChecked, currentParagraph.toString().trim()));
                    }
                    currentHasCheckedMark = false;
                    currentParagraph.setLength(0);
                }
                if (currentParagraph.length() > 0)
                {
                    currentParagraph.append("\n");
                }
                currentParagraph.append(line);
            }
        }

        // close paragraph
        if (currentHasCheckedMark || currentParagraph.length() > 0)
        {
            result.add(new DescriptionItem(currentHasCheckedMark, currentIsChecked,
                    currentHasCheckedMark ? currentParagraph.toString().trim() : currentParagraph.toString()));
        }
        return result;
    }


    private static void serializeDescription(StringBuilder sb, List<DescriptionItem> checklist)
    {
        if (checklist == null || checklist.isEmpty())
        {
            return;
        }

        boolean first = true;
        for (DescriptionItem item : checklist)
        {
            if (first)
            {
                first = false;
            }
            else
            {
                sb.append('\n');
            }
            if (item.checkbox)
            {
                sb.append(item.checked ? "- [x] " : "- [ ] ");
            }
            sb.append(item.text);
        }
    }

}
