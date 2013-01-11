/*
 * StringFieldAdapter.java
 *
 * Copyright (C) 2012 Marten Gajda <marten@dmfs.org>
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

package org.dmfs.tasks.model.adapters;

import android.content.ContentValues;

/**
 * A StringFieldAdapter stores {@link String} values in a certain field of {@link ContentValues}.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class StringFieldAdapter extends FieldAdapter<String> {

    /**
     * The field name this adapter uses to store the values.
     */
    private final String mFieldName;

    /**
     * Constructor for a new StringFieldAdapter.
     * 
     * @param fieldName
     *            The name of the field to use when loading or storing the value.
     */
    public StringFieldAdapter(String fieldName) {
	mFieldName = fieldName;
    }

    @Override
    public String get(ContentValues values) {
	// return the value as String
	return values.getAsString(mFieldName);
    }

    @Override
    public void set(ContentValues values, String value) {
	values.put(mFieldName, value);
    }

}
