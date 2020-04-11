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

package org.dmfs.opentaskspal.tasks;

import org.dmfs.android.contentpal.RowData;
import org.dmfs.android.contentpal.rowdata.CharSequenceRowData;
import org.dmfs.android.contentpal.rowdata.Composite;
import org.dmfs.android.contentpal.rowdata.DelegatingRowData;
import org.dmfs.tasks.contract.TaskContract;

import androidx.annotation.NonNull;


/**
 * The {@link RowData} of a property row.
 * <p>
 * This just sets the {@link TaskContract.Properties#MIMETYPE} field of the row and leaves everything else to the provided {@link RowData}.
 *
 * @author Marten Gajda
 */
public final class PropertyData extends DelegatingRowData<TaskContract.Properties>
{
    public PropertyData(@NonNull String mimeType, @NonNull RowData<TaskContract.Properties> delegate)
    {
        super(new Composite<>(
                new CharSequenceRowData<>(TaskContract.Properties.MIMETYPE, mimeType),
                delegate
        ));
    }
}
