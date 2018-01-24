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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.OnContentChangeListener;
import org.dmfs.tasks.model.constraints.AbstractConstraint;

import java.util.LinkedList;
import java.util.List;


/**
 * Knows how to load and store a certain field in a {@link ContentSet}.
 *
 * @param <Type>
 *         The type of the value this adapter stores.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public abstract class FieldAdapter<Type>
{

    /**
     * A list of constraints that are applied when a new value is set.
     */
    private List<AbstractConstraint<Type>> mConstraints;


    /**
     * Get the value from the given {@link ContentSet}
     *
     * @param values
     *         The {@link ContentValues} that contain the value to return.
     *
     * @return The value.
     */
    @Nullable
    public abstract Type get(@NonNull ContentSet values);

    /**
     * Get the value from the given {@link Cursor}
     *
     * @param cursor
     *         The {@link Cursor} that contain the value to return.
     *
     * @return The value.
     */
    @Nullable
    public abstract Type get(@NonNull Cursor cursor);

    /**
     * Get a default value for this Adapter.
     *
     * @param values
     *         The {@link ContentSet}.
     *
     * @return A default Value
     */
    @Nullable
    public abstract Type getDefault(@NonNull ContentSet values);

    /**
     * Set a value in the given {@link ContentSet}.
     *
     * @param values
     *         The {@link ContentSet} where to store the new value.
     * @param value
     *         The new value to store.
     */
    public abstract void set(@NonNull ContentSet values, @Nullable Type value);

    /**
     * Set a value in the given {@link ContentValues}.
     *
     * @param values
     *         The {@link ContentValues} where to store the new value.
     * @param value
     *         The new value to store.
     */
    public abstract void set(@NonNull ContentValues values, @Nullable Type value);


    /**
     * Set a value in the given {@link ContentSet}, but validate against the constraints first.
     *
     * @param values
     *         The {@link ContentSet} where to store the new value.
     * @param value
     *         The new value to store.
     */
    public final void validateAndSet(@NonNull ContentSet values, @Nullable Type value)
    {
        Type oldValue = get(values);
        value = checkConstraints(values, oldValue, value);
        set(values, value);
    }


    /**
     * Register a listener for the fields that this adapter adapts.
     *
     * @param values
     *         The {@link ContentSet}.
     * @param listener
     *         The {@link OnContentChangeListener} to register.
     */
    public abstract void registerListener(@NonNull ContentSet values, @NonNull OnContentChangeListener listener, boolean initialNotification);

    /**
     * Unregister a listener for the fields that this adapter adapts.
     *
     * @param values
     *         The {@link ContentSet}.
     * @param listener
     *         The {@link OnContentChangeListener} to unregister.
     */
    public abstract void unregisterListener(@NonNull ContentSet values, @NonNull OnContentChangeListener listener);


    /**
     * Add a new constraint to this field adapter. Constraints are evaluated in the order they have been added.
     *
     * @param constraint
     *         The new constraint.
     */
    public final FieldAdapter<Type> addConstraint(@NonNull AbstractConstraint<Type> constraint)
    {
        if (mConstraints == null)
        {
            mConstraints = new LinkedList<>();
        }
        mConstraints.add(constraint);
        return this;
    }


    /**
     * Check all constraints and enforce them if possible.
     * <p>
     * TODO: Allow throwing an exception if any of the constraints could not be enforced. That requires some kind of transaction in {@link ContentSet}.
     *
     * @param currentValues
     *         The current {@link ContentSet}.
     * @param newValue
     *         The new value to check.
     */
    private Type checkConstraints(@NonNull ContentSet currentValues, @Nullable Type oldValue, @Nullable Type newValue)
    {
        if (mConstraints != null)
        {
            for (AbstractConstraint<Type> constraint : mConstraints)
            {
                newValue = constraint.apply(currentValues, oldValue, newValue);
            }
        }
        return newValue;
    }
}
