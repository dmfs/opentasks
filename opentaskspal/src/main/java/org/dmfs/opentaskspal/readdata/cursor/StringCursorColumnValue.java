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

package org.dmfs.opentaskspal.readdata.cursor;

import android.database.Cursor;

import org.dmfs.optional.Optional;
import org.dmfs.optional.decorators.DelegatingOptional;


/**
 * * {@link Optional} for a {@link String} value in a {@link Cursor}.
 *
 * @author Gabor Keszthelyi
 */
public final class StringCursorColumnValue extends DelegatingOptional<String>
{
    public StringCursorColumnValue(Cursor cursor, String columnName)
    {
        super(new OptionalCursorColumnValue<>(cursor, columnName, Cursor::getString));
    }
}
