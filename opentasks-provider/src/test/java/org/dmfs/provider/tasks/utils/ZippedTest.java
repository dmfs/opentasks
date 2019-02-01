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

package org.dmfs.provider.tasks.utils;

import org.dmfs.jems.function.BiFunction;
import org.dmfs.jems.optional.elementary.Present;
import org.dmfs.jems.single.elementary.ValueSingle;
import org.junit.Test;

import static org.dmfs.jems.hamcrest.matchers.SingleMatcher.hasValue;
import static org.dmfs.jems.mockito.doubles.TestDoubles.dummy;
import static org.dmfs.jems.mockito.doubles.TestDoubles.failingMock;
import static org.dmfs.jems.optional.elementary.Absent.absent;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;


/**
 * @author Marten Gajda
 */
public class ZippedTest
{
    @Test
    public void testPresent()
    {
        Object dummyPresentValue = new Object();
        Object dummySingleValue = new Object();
        Object dummyResult = new Object();
        BiFunction<Object, Object, Object> mockFunction = failingMock(BiFunction.class);
        doReturn(dummyResult).when(mockFunction).value(dummyPresentValue, dummySingleValue);
        assertThat(new Zipped<>(new Present<>(dummyPresentValue), new ValueSingle<>(dummySingleValue), mockFunction), hasValue(sameInstance(dummyResult)));
    }


    @Test
    public void testAbsent()
    {
        Object dummyObject = new Object();
        assertThat(new Zipped<>(absent(), new ValueSingle<>(dummyObject), dummy(BiFunction.class)), hasValue(sameInstance(dummyObject)));
    }
}