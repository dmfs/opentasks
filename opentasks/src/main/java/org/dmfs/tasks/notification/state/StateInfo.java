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

package org.dmfs.tasks.notification.state;

/**
 * Gives information about the status of a task with a notification.
 *
 * @author Marten Gajda
 */
public interface StateInfo
{
    /**
     * The task is pinned or was pinned when notified.
     */
    boolean pinned();

    /**
     * The task is due or was due when notified.
     */
    boolean due();

    /**
     * The task is started or was started when notified.
     */
    boolean started();

    /**
     * The task is done or was done when notified.
     */
    boolean done();
}
