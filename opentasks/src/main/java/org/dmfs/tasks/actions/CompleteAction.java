/*
 * Copyright 2019 dmfs GmbH
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

package org.dmfs.tasks.actions;

import org.dmfs.opentaskspal.tasks.StatusData;
import org.dmfs.tasks.contract.TaskContract;


/**
 * A {@link TaskAction} which completes a task.
 *
 * @author Marten Gajda
 */
public final class CompleteAction extends DelegatingTaskAction
{
    public CompleteAction()
    {
        super(new UpdateAction((snapshot) -> new StatusData<>(TaskContract.Tasks.STATUS_COMPLETED)));
    }
}
