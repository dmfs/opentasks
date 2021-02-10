/*
 * Copyright 2021 dmfs GmbH
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

import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.recur.InvalidRecurrenceRuleException;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import org.dmfs.rfc5545.recurrenceset.RecurrenceRuleAdapter;
import org.dmfs.rfc5545.recurrenceset.RecurrenceSet;
import org.junit.Test;

import java.util.TimeZone;

import static org.dmfs.jems.hamcrest.matchers.iterator.IteratorMatcher.iteratorOf;
import static org.junit.Assert.assertThat;


/**
 * @author Marten Gajda
 */
public class TaskInstanceIteratorTest
{
    private final static String TIMEZONE = "Europe/Berlin";


    @Test
    public void testAbsolute() throws InvalidRecurrenceRuleException
    {
        RecurrenceSet recurrenceSet = new RecurrenceSet();
        recurrenceSet.addInstances(new RecurrenceRuleAdapter(new RecurrenceRule("FREQ=DAILY;COUNT=3")));
        DateTime start = DateTime.parse(TIMEZONE, "20210201T120000");

        assertThat(
                () -> new TaskInstanceIterator(start, recurrenceSet.iterator(TimeZone.getTimeZone(TIMEZONE), start.getTimestamp()), TIMEZONE),
                iteratorOf(
                        DateTime.parse(TIMEZONE, "20210201T120000"),
                        DateTime.parse(TIMEZONE, "20210202T120000"),
                        DateTime.parse(TIMEZONE, "20210203T120000")
                )
        );

        assertThat(
                () -> new TaskInstanceIterator(start, recurrenceSet),
                iteratorOf(
                        DateTime.parse(TIMEZONE, "20210201T120000"),
                        DateTime.parse(TIMEZONE, "20210202T120000"),
                        DateTime.parse(TIMEZONE, "20210203T120000")
                )
        );
    }


    @Test
    public void testFloating() throws InvalidRecurrenceRuleException
    {

        RecurrenceSet recurrenceSet = new RecurrenceSet();
        recurrenceSet.addInstances(new RecurrenceRuleAdapter(new RecurrenceRule("FREQ=DAILY;COUNT=3")));
        DateTime start = DateTime.parse("20210201T120000");

        assertThat(
                () -> new TaskInstanceIterator(start, recurrenceSet.iterator(null, start.getTimestamp()), null),
                iteratorOf(
                        DateTime.parse("20210201T120000"),
                        DateTime.parse("20210202T120000"),
                        DateTime.parse("20210203T120000")
                )
        );

        assertThat(
                () -> new TaskInstanceIterator(start, recurrenceSet),
                iteratorOf(
                        DateTime.parse("20210201T120000"),
                        DateTime.parse("20210202T120000"),
                        DateTime.parse("20210203T120000")
                )
        );
    }


    @Test
    public void testAllDay() throws InvalidRecurrenceRuleException
    {

        RecurrenceSet recurrenceSet = new RecurrenceSet();
        recurrenceSet.addInstances(new RecurrenceRuleAdapter(new RecurrenceRule("FREQ=DAILY;COUNT=3")));
        DateTime start = DateTime.parse("20210201");

        assertThat(
                () -> new TaskInstanceIterator(start, recurrenceSet.iterator(null, start.getTimestamp()), null),
                iteratorOf(
                        DateTime.parse("20210201"),
                        DateTime.parse("20210202"),
                        DateTime.parse("20210203")
                )
        );

        assertThat(
                () -> new TaskInstanceIterator(start, recurrenceSet),
                iteratorOf(
                        DateTime.parse("20210201"),
                        DateTime.parse("20210202"),
                        DateTime.parse("20210203")
                )
        );
    }
}