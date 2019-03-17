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

import org.dmfs.iterables.elementary.Seq;


/**
 * A {@link TaskAction} which removes a notification.
 *
 * @author Marten Gajda
 */
public final class WipeNotificationAction extends DelegatingTaskAction
{
    public WipeNotificationAction()
    {
        super(new Composite(new Seq<>(new RemoveNotificationAction(), new CancelNotificationAction())));
    }
}
