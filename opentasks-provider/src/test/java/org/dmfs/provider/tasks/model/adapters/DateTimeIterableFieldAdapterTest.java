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

package org.dmfs.provider.tasks.model.adapters;

import android.content.ContentValues;

import org.dmfs.iterables.EmptyIterable;
import org.dmfs.iterables.elementary.Seq;
import org.dmfs.provider.tasks.model.TaskAdapter;
import org.dmfs.rfc5545.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.dmfs.jems.hamcrest.matchers.IterableMatcher.iteratesTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;


/**
 * @author Marten Gajda
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class DateTimeIterableFieldAdapterTest
{
    @Test
    public void testFieldName()
    {
        assertThat(new DateTimeIterableFieldAdapter<>("x", "y").fieldName(), is("x"));
    }


    @Test
    public void testGetFromCVAllDay1()
    {
        ContentValues values = new ContentValues();
        FieldAdapter<Iterable<DateTime>, ?> adapter = new DateTimeIterableFieldAdapter<TaskAdapter>("x", "y");
        values.put("x", "20180109");
        assertThat(adapter.getFrom(values), iteratesTo(DateTime.parse("20180109")));
    }


    @Test
    public void testGetFromCVAllDay2()
    {
        ContentValues values = new ContentValues();
        FieldAdapter<Iterable<DateTime>, ?> adapter = new DateTimeIterableFieldAdapter<TaskAdapter>("x", "y");
        values.put("x", "20180109,20180110");
        assertThat(adapter.getFrom(values), iteratesTo(DateTime.parse("20180109"), DateTime.parse("20180110")));
    }


    @Test
    public void testGetFromCVFloating1()
    {
        ContentValues values = new ContentValues();
        FieldAdapter<Iterable<DateTime>, ?> adapter = new DateTimeIterableFieldAdapter<TaskAdapter>("x", "y");
        values.put("x", "20180109T140000");
        values.putNull("y");
        assertThat(adapter.getFrom(values), iteratesTo(DateTime.parse("20180109T140000")));
    }


    @Test
    public void testGetFromCVFloating2()
    {
        ContentValues values = new ContentValues();
        FieldAdapter<Iterable<DateTime>, ?> adapter = new DateTimeIterableFieldAdapter<TaskAdapter>("x", "y");
        values.put("x", "20180109T140000,20180110T140000");
        values.putNull("y");
        assertThat(adapter.getFrom(values), iteratesTo(DateTime.parse("20180109T140000"), DateTime.parse("20180110T140000")));
    }


    @Test
    public void testGetFromCVAbsolute1()
    {
        ContentValues values = new ContentValues();
        FieldAdapter<Iterable<DateTime>, ?> adapter = new DateTimeIterableFieldAdapter<TaskAdapter>("x", "y");
        values.put("x", "20180109T140000Z");
        values.put("y", "Europe/Berlin");
        assertThat(adapter.getFrom(values), iteratesTo(DateTime.parse("Europe/Berlin", "20180109T150000")));
    }


    @Test
    public void testGetFromCVAbsolute2()
    {
        ContentValues values = new ContentValues();
        FieldAdapter<Iterable<DateTime>, ?> adapter = new DateTimeIterableFieldAdapter<TaskAdapter>("x", "y");
        values.put("x", "20180109T140000Z,20180110T140000Z");
        values.put("y", "Europe/Berlin");
        assertThat(adapter.getFrom(values), iteratesTo(DateTime.parse("Europe/Berlin", "20180109T150000"), DateTime.parse("Europe/Berlin", "20180110T150000")));
    }


    @Test
    public void testSetInNull()
    {
        ContentValues values = new ContentValues();
        FieldAdapter<Iterable<DateTime>, ?> adapter = new DateTimeIterableFieldAdapter<TaskAdapter>("x", "y");
        adapter.setIn(values, null);
        assertThat(values.getAsString("x"), nullValue());
    }


    @Test
    public void testSetInEmpty()
    {
        ContentValues values = new ContentValues();
        FieldAdapter<Iterable<DateTime>, ?> adapter = new DateTimeIterableFieldAdapter<TaskAdapter>("x", "y");
        adapter.setIn(values, EmptyIterable.instance());
        assertThat(values.getAsString("x"), nullValue());
    }


    @Test
    public void testSetInSingleAllDay()
    {
        ContentValues values = new ContentValues();
        FieldAdapter<Iterable<DateTime>, ?> adapter = new DateTimeIterableFieldAdapter<TaskAdapter>("x", "y");
        adapter.setIn(values, new Seq<>(DateTime.parse("20180109")));
        assertThat(values.getAsString("x"), is("20180109"));
    }


    @Test
    public void testSetInSingleFloating()
    {
        ContentValues values = new ContentValues();
        FieldAdapter<Iterable<DateTime>, ?> adapter = new DateTimeIterableFieldAdapter<TaskAdapter>("x", "y");
        adapter.setIn(values, new Seq<>(DateTime.parse("20180109T150000")));
        assertThat(values.getAsString("x"), is("20180109T150000"));
    }


    @Test
    public void testSetInSingleAbsolute()
    {
        ContentValues values = new ContentValues();
        FieldAdapter<Iterable<DateTime>, ?> adapter = new DateTimeIterableFieldAdapter<TaskAdapter>("x", "y");
        adapter.setIn(values, new Seq<>(DateTime.parse("Europe/Berlin", "20180109T150000")));
        assertThat(values.getAsString("x"), is("20180109T140000Z"));
    }


    @Test
    public void testSetInDoubleAllDay()
    {
        ContentValues values = new ContentValues();
        FieldAdapter<Iterable<DateTime>, ?> adapter = new DateTimeIterableFieldAdapter<TaskAdapter>("x", "y");
        adapter.setIn(values, new Seq<>(DateTime.parse("20180109"), DateTime.parse("20180110")));
        assertThat(values.getAsString("x"), is("20180109,20180110"));
    }


    @Test
    public void testSetInDoubleFloating()
    {
        ContentValues values = new ContentValues();
        FieldAdapter<Iterable<DateTime>, ?> adapter = new DateTimeIterableFieldAdapter<TaskAdapter>("x", "y");
        adapter.setIn(values, new Seq<>(DateTime.parse("20180109T150000"), DateTime.parse("20180110T150000")));
        assertThat(values.getAsString("x"), is("20180109T150000,20180110T150000"));
    }


    @Test
    public void testSetInDoubleAbsolute()
    {
        ContentValues values = new ContentValues();
        FieldAdapter<Iterable<DateTime>, ?> adapter = new DateTimeIterableFieldAdapter<TaskAdapter>("x", "y");
        adapter.setIn(values, new Seq<>(DateTime.parse("Europe/Berlin", "20180109T150000"), DateTime.parse("Europe/Berlin", "20180110T150000")));
        assertThat(values.getAsString("x"), is("20180109T140000Z,20180110T140000Z"));
    }


    @Test
    public void testSetInMultiAllDay()
    {
        ContentValues values = new ContentValues();
        FieldAdapter<Iterable<DateTime>, ?> adapter = new DateTimeIterableFieldAdapter<TaskAdapter>("x", "y");
        adapter.setIn(values, new Seq<>(DateTime.parse("20180109"), DateTime.parse("20180110"), DateTime.parse("20180111")));
        assertThat(values.getAsString("x"), is("20180109,20180110,20180111"));
    }


    @Test
    public void testSetInMultiFloating()
    {
        ContentValues values = new ContentValues();
        FieldAdapter<Iterable<DateTime>, ?> adapter = new DateTimeIterableFieldAdapter<TaskAdapter>("x", "y");
        adapter.setIn(values, new Seq<>(DateTime.parse("20180109T150000"), DateTime.parse("20180110T150000"), DateTime.parse("20180111T150000")));
        assertThat(values.getAsString("x"), is("20180109T150000,20180110T150000,20180111T150000"));
    }


    @Test
    public void testSetInMultiAbsolute()
    {
        ContentValues values = new ContentValues();
        FieldAdapter<Iterable<DateTime>, ?> adapter = new DateTimeIterableFieldAdapter<TaskAdapter>("x", "y");
        adapter.setIn(values, new Seq<>(DateTime.parse("Europe/Berlin", "20180109T150000"), DateTime.parse("Europe/Berlin", "20180110T150000"),
                DateTime.parse("Europe/Berlin", "20180111T150000")));
        assertThat(values.getAsString("x"), is("20180109T140000Z,20180110T140000Z,20180111T140000Z"));
    }
}