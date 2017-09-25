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

import org.dmfs.tasks.contract.TaskContract.Tasks;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.dmfs.android.contentpal.testing.contentoperationbuilder.WithValues.withValuesOnly;
import static org.dmfs.android.contentpal.testing.contentvalues.NullValue.withNullValue;
import static org.dmfs.android.contentpal.testing.rowdata.RowDataMatcher.builds;
import static org.junit.Assert.assertThat;


/**
 * Unit test for {@link NoTimeData}.
 *
 * @author Gabor Keszthelyi
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public final class NoTimeDataTest
{

    @Test
    public void test_thatAllTimeRelatedValuesAreNulled()
    {
        assertThat(new NoTimeData(),
                builds(
                        withValuesOnly(
                                withNullValue(Tasks.DTSTART),
                                withNullValue(Tasks.TZ),
                                withNullValue(Tasks.IS_ALLDAY),

                                withNullValue(Tasks.DUE),

                                withNullValue(Tasks.DURATION),

                                withNullValue(Tasks.RDATE),
                                withNullValue(Tasks.RRULE),
                                withNullValue(Tasks.EXDATE)
                        )));
    }

}