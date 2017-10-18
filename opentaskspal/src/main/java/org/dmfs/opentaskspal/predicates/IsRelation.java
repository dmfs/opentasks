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

package org.dmfs.opentaskspal.predicates;

import org.dmfs.android.contentpal.Predicate;
import org.dmfs.android.contentpal.predicates.DelegatingPredicate;
import org.dmfs.tasks.contract.TaskContract;


/**
 * {@link Predicate} for selecting {@link TaskContract.Property.Relation} properties from {@link TaskContract.Properties} table.
 *
 * @author Gabor Keszthelyi
 */
public final class IsRelation extends DelegatingPredicate
{
    public IsRelation()
    {
        super(new IsProperty(TaskContract.Property.Relation.CONTENT_ITEM_TYPE));
    }
}
