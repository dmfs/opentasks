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

package org.dmfs.opentaskspal.tasks;

import android.net.Uri;

import org.dmfs.android.contentpal.RowSnapshot;
import org.dmfs.android.contentpal.references.RowUriReference;
import org.dmfs.tasks.contract.TaskContract;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.dmfs.android.contentpal.testing.contentoperationbuilder.WithValues.withValuesOnly;
import static org.dmfs.android.contentpal.testing.contentvalues.Containing.containing;
import static org.dmfs.android.contentpal.testing.rowdata.RowDataMatcher.builds;
import static org.dmfs.jems.mockito.doubles.TestDoubles.failingMock;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;


/**
 * Unit test for {@link RelationData}
 *
 * @author Marten Gajda
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class RelationDataTest
{
    @Test
    public void test()
    {
        RowSnapshot<TaskContract.Tasks> child = failingMock(RowSnapshot.class);
        RowSnapshot<TaskContract.Tasks> parent = failingMock(RowSnapshot.class);

        doReturn(new RowUriReference<>(Uri.parse("content://tasks/123"))).when(parent).reference();
        doReturn(new RowUriReference<>(Uri.parse("content://tasks/124"))).when(child).reference();

        assertThat(new RelationData(parent, TaskContract.Property.Relation.RELTYPE_CHILD, child),
                builds(
                        withValuesOnly(
                                containing(TaskContract.Property.Relation.MIMETYPE, TaskContract.Property.Relation.CONTENT_ITEM_TYPE),
                                containing(TaskContract.Property.Relation.RELATED_TYPE, TaskContract.Property.Relation.RELTYPE_CHILD),
                                containing(TaskContract.Property.Relation.TASK_ID, 123L),
                                containing(TaskContract.Property.Relation.RELATED_ID, 124L))));

        assertThat(new RelationData(parent, TaskContract.Property.Relation.RELTYPE_CHILD, "xyz"),
                builds(
                        withValuesOnly(
                                containing(TaskContract.Property.Relation.MIMETYPE, TaskContract.Property.Relation.CONTENT_ITEM_TYPE),
                                containing(TaskContract.Property.Relation.RELATED_TYPE, TaskContract.Property.Relation.RELTYPE_CHILD),
                                containing(TaskContract.Property.Relation.TASK_ID, 123L),
                                containing(TaskContract.Property.Relation.RELATED_UID, "xyz"))));
    }
}