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

package org.dmfs.provider.tasks.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import org.dmfs.provider.tasks.model.adapters.FieldAdapter;


/**
 * Adapter to read values of a specific entity type from primitive data sets like {@link Cursor}s or {@link ContentValues}s.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public interface EntityAdapter<EntityType>
{
    /**
     * Returns the row id of the entity or <code>-1</code> if the entity has not been stored yet.
     *
     * @return The entity row id or <code>-1</code>.
     */
    long id();

    /**
     * Returns the {@link Uri} of the entity using the given authority.
     *
     * @param authority
     *         The authority of this provider.
     *
     * @return A {@link Uri} or <code>null</code> if this entity has not been stored yet.
     */
    Uri uri(String authority);

    /**
     * Returns the value identified by the given {@link FieldAdapter}.
     *
     * @param fieldAdapter
     *         The {@link FieldAdapter} of the value to return.
     *
     * @return The value, maybe be <code>null</code>.
     */
    <T> T valueOf(FieldAdapter<T, EntityType> fieldAdapter);

    /**
     * Returns the old value identified by the given {@link FieldAdapter}. This will be equal to the value returned by {@link #valueOf(FieldAdapter)} unless it
     * has been overridden, in which case this returns the former value.
     *
     * @param fieldAdapter
     *         The {@link FieldAdapter} of the value to return.
     *
     * @return The value, maybe be <code>null</code>.
     */
    <T> T oldValueOf(FieldAdapter<T, EntityType> fieldAdapter);

    /**
     * Returns whether the given field has been overridden or not.
     *
     * @param fieldAdapter
     *         The {@link FieldAdapter} of the field to check.
     *
     * @return <code>true</code> if the field has been overridden, <code>false</code> otherwise.
     */
    boolean isUpdated(FieldAdapter<?, EntityType> fieldAdapter);

    /**
     * Returns whether this adapter supports modifying values.
     *
     * @return <code>true</code> if the task values can be changed by this adapter, false otherwise.
     */
    boolean isWriteable();

    /**
     * Returns whether any value has been modified.
     *
     * @return <code>true</code> if there are modified values, false otherwise.
     */
    boolean hasUpdates();

    /**
     * Sets a value of the adapted entity. The value is identified by a {@link FieldAdapter}.
     *
     * @param fieldAdapter
     *         The {@link FieldAdapter} of the value to set.
     * @param value
     *         The new value.
     */
    <T> void set(FieldAdapter<T, EntityType> fieldAdapter, T value);

    /**
     * Remove a value from the change set. In effect the respective field will keep it's old value.
     *
     * @param fieldAdapter
     *         The {@link FieldAdapter} of the field to un-set.
     */
    void unset(FieldAdapter<?, EntityType> fieldAdapter);

    /**
     * Commit all changes to the database.
     *
     * @param db
     *         A writable database.
     *
     * @return The number of entries affected. This may be <code>0</code> if no fields have been changed.
     */
    int commit(SQLiteDatabase db);

    /**
     * Return the value of a temporary state field. The state of an entity is not committed to the database, it's only bound to the instances of this
     * {@link EntityAdapter} and will be lost once it gets garbage collected.
     *
     * @param stateFieldAdater
     *         The {@link FieldAdapter} of a state field.
     *
     * @return The value of the state field.
     */
    <T> T getState(FieldAdapter<T, EntityType> stateFieldAdater);

    /**
     * Set the value of a state field. This value is not stored in the database. Instead it only exists as long as this {@link EntityAdapter} exists.
     *
     * @param stateFieldAdater
     *         The {@link FieldAdapter} of the state field to set.
     * @param value
     *         The new state value.
     */
    <T> void setState(FieldAdapter<T, EntityType> stateFieldAdater, T value);

    /***
     * Creates a {@link EntityAdapter} for a new entity initialized with the values of this entity (except for _ID).
     *
     * @return A new {@link EntityAdapter} having the same values.
     */
    EntityAdapter<EntityType> duplicate();
}
