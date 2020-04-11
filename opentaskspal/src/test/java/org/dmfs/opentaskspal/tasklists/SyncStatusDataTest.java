/*
 * Copyright 2020 dmfs GmbH
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

package org.dmfs.opentaskspal.tasklists;

import org.dmfs.tasks.contract.TaskContract;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.dmfs.android.contentpal.testing.contentoperationbuilder.WithValues.withValuesOnly;
import static org.dmfs.android.contentpal.testing.contentvalues.Containing.containing;
import static org.dmfs.android.contentpal.testing.rowdata.RowDataMatcher.builds;
import static org.junit.Assert.assertThat;


/**
 * Uinit test for {@link SyncStatusData}
 *
 * @author Marten Gajda
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class SyncStatusDataTest
{
    @Test
    public void test()
    {
        assertThat(new SyncStatusData(),
                builds(
                        withValuesOnly(
                                containing(TaskContract.TaskLists.SYNC_ENABLED, 1))));

        assertThat(new SyncStatusData(true),
                builds(
                        withValuesOnly(
                                containing(TaskContract.TaskLists.SYNC_ENABLED, 1))));

        assertThat(new SyncStatusData(false),
                builds(
                        withValuesOnly(
                                containing(TaskContract.TaskLists.SYNC_ENABLED, 0))));
    }
}