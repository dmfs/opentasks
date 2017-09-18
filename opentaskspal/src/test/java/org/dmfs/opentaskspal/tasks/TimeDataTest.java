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

package org.dmfs.opentaskspal.tasks;

import android.content.ContentProviderOperation;

import org.dmfs.android.contentpal.TransactionContext;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.Duration;
import org.junit.Test;
import org.mockito.Mockito;


/**
 * Unit test for {@link TimeData}.
 *
 * @author Gabor Keszthelyi
 */
public class TimeDataTest
{
    @Test
    public void test_whenStartAndDueCtor_setsThemAndNullsOutDuration()
    {
        DateTime start = DateTime.now();
        DateTime due = start.addDuration(new Duration(2, 1, 0));
        TransactionContext dummyTransContext = Mockito.mock(TransactionContext.class);

        // TODO Wait for Matcher support..
        ContentProviderOperation.Builder mockBuilder = Mockito.mock(ContentProviderOperation.Builder.class);

        new TimeData(start, due).updatedBuilder(dummyTransContext, mockBuilder);
    }
}