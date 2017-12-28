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
import android.support.annotation.NonNull;

import org.dmfs.jems.function.BiFunction;
import org.dmfs.opentaskspal.jems.optional.SingleOptional;
import org.dmfs.optional.Optional;
import org.dmfs.optional.Present;
import org.dmfs.optional.decorators.DelegatingOptional;

import static org.dmfs.optional.Absent.absent;


/**
 * {@link Optional} for a value from a {@link Cursor}.
 * <p>
 * Handles the cases where return values for primitive types cannot be assessed whether they represent absent value
 * (like 0 for {@link Cursor#getInt(int)}) by checking {@link Cursor#isNull(int)} first.
 *
 * @author Gabor Keszthelyi
 */
public final class OptionalCursorColumnValue<T> extends DelegatingOptional<T>
{
    public OptionalCursorColumnValue(@NonNull Cursor cursor,
                                     @NonNull String columnName,
                                     @NonNull BiFunction<Cursor, Integer, T> getFunction)
    {
        super(new SingleOptional<T>(() ->
        {
            int columnIndex = cursor.getColumnIndexOrThrow(columnName);
            return cursor.isNull(columnIndex) ? absent() : new Present<>(getFunction.value(cursor, columnIndex));
        }));
    }
}
