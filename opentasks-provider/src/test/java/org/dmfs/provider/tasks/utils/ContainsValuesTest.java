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

package org.dmfs.provider.tasks.utils;

import android.content.ContentValues;
import android.database.MatrixCursor;

import org.dmfs.iterables.elementary.Seq;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.dmfs.jems.hamcrest.matchers.predicate.PredicateMatcher.satisfiedBy;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;


/**
 * @author Marten Gajda
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ContainsValuesTest
{
    @Test
    public void test()
    {
        ContentValues values = new ContentValues();
        values.put("a", 123);
        values.put("b", "stringValue");
        values.put("c", new byte[] { 3, 2, 1 });
        values.putNull("d");

        MatrixCursor cursor = new MatrixCursor(new String[] { "c", "b", "a", "d", "f" });
        cursor.addRow(new Seq<>(new byte[] { 3, 2, 1 }, "stringValue", 123, null, "xyz"));
        cursor.addRow(new Seq<>(new byte[] { 3, 2, 1 }, "stringValue", "123", null, "xyz"));
        cursor.addRow(new Seq<>(new byte[] { 3, 2 }, "stringValue", 123, null, "xyz"));
        cursor.addRow(new Seq<>(new byte[] { 3, 2, 1 }, "stringValueX", 123, null, "xyz"));
        cursor.addRow(new Seq<>(new byte[] { 3, 2, 1 }, "stringValue", 1234, null, "xyz"));
        cursor.addRow(new Seq<>(new byte[] { 3, 2, 1 }, "stringValue", 123, "123", "xyz"));
        cursor.addRow(new Seq<>(321, "stringValueX", "1234", "123", "xyz"));
        cursor.addRow(new Seq<>(new byte[] { 3, 2, 1, 0 }, "stringValueX", 1234, "123", "xyz"));

        cursor.moveToFirst();
        assertThat(new ContainsValues(values), is(satisfiedBy(cursor)));
        cursor.moveToNext();
        assertThat(new ContainsValues(values), is(satisfiedBy(cursor)));
        cursor.moveToNext();
        assertThat(new ContainsValues(values), is(not(satisfiedBy(cursor))));
        cursor.moveToNext();
        assertThat(new ContainsValues(values), is(not(satisfiedBy(cursor))));
        cursor.moveToNext();
        assertThat(new ContainsValues(values), is(not(satisfiedBy(cursor))));
        cursor.moveToNext();
        assertThat(new ContainsValues(values), is(not(satisfiedBy(cursor))));
        cursor.moveToNext();
        assertThat(new ContainsValues(values), is(not(satisfiedBy(cursor))));
        cursor.moveToNext();
        assertThat(new ContainsValues(values), is(not(satisfiedBy(cursor))));
    }


    @Test
    public void testMissingColumns()
    {
        ContentValues values = new ContentValues();
        values.put("a", 123);
        values.put("b", "stringValue");
        values.put("c", new byte[] { 3, 2, 1 });
        values.putNull("d");

        MatrixCursor cursor = new MatrixCursor(new String[] { "c", "b" });
        cursor.addRow(new Seq<>(new byte[] { 3, 2, 1 }, "stringValue"));

        cursor.moveToFirst();
        assertThat(new ContainsValues(values), is(not(satisfiedBy(cursor))));
    }
}