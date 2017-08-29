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

package org.dmfs.opentaskspal.tables;

import org.dmfs.android.contentpal.Table;
import org.dmfs.android.contentpal.tables.BaseTable;
import org.dmfs.android.contentpal.tables.DelegatingTable;
import org.dmfs.tasks.contract.TaskContract;


/**
 * {@link Table} for {@link TaskContract.Instances}.
 *
 * @author Gabor Keszthelyi
 */
public final class InstanceTable extends DelegatingTable<TaskContract.Instances>
{
    public InstanceTable(String authority)
    {
        super(new BaseTable<TaskContract.Instances>(TaskContract.Instances.getContentUri(authority)));
    }
}
