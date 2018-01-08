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

package org.dmfs.opentaskspal.readdata;

import android.provider.BaseColumns;

import org.dmfs.android.contentpal.Projection;
import org.dmfs.android.contentpal.RowDataSnapshot;
import org.dmfs.android.contentpal.projections.SingleColProjection;
import org.dmfs.jems.single.Single;
import org.dmfs.jems.single.decorators.DelegatingSingle;
import org.dmfs.jems.single.elementary.ValueSingle;

import androidx.annotation.NonNull;


/**
 * {@link Single} for the id ({@link BaseColumns#_ID} of a row.
 *
 * @author Gabor Keszthelyi
 */
public final class Id extends DelegatingSingle<Long>
{
    public static final Projection<BaseColumns> PROJECTION = new SingleColProjection<>(BaseColumns._ID);


    public Id(@NonNull RowDataSnapshot<?> rowDataSnapshot)
    {
        super(new ValueSingle<>(rowDataSnapshot.data(BaseColumns._ID, Long::valueOf).value()));
    }

}
