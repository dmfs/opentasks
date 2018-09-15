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

package org.dmfs.tasks.utils;

/**
 * A listener that is invoked whenever a time event occurs or an alarm has been triggered.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public interface TimeChangeListener
{
    /**
     * Callback that is invoked when a time or time zone change has been detected.
     *
     * @param timeChangeObserver
     *         The {@TimeChangeObserver} that has detected this update.
     */
    void onTimeUpdate(TimeChangeObserver timeChangeObserver);

    /**
     * Callback that is invoked when an alarm has been triggered.
     *
     * @param timeChangeObserver
     *         The {@TimeChangeObserver} that has been triggered.
     */
    void onAlarm(TimeChangeObserver timeChangeObserver);
}
