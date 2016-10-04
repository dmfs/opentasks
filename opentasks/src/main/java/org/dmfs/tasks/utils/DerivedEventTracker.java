/*
 * Copyright 2016 Marten Gajda <marten@dmfs.org>
 *
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

import android.support.annotation.IntDef;


/**
 * Abstraction for tracking specific events to 'create' another event that depends on them based on any kind of
 * conditions. When the conditions are met, i.e. the derived event 'happens', the implementation can notify the listener, so
 * it can react to it.
 * <p>
 * A simple example is to get notified when a set of multiple, independent asynchronous operations finish.
 *
 * @author Gabor Keszthelyi
 */
public interface DerivedEventTracker
{
    /**
     * Tells this tracker that the given event happened, so it can track it or react to it.
     *
     * @param event
     *         the id of the event. Tip: use {@link IntDef} to define events
     */
    void event(int event);

    void setListener(Listener listener);

    /**
     * Interface used to notify listener that the derived event happened (implemented conditions are met).
     */
    interface Listener
    {
        /**
         * Called when the derived event (specified by conditions on incoming events) happened.
         */
        void onDerivedEvent();
    }

}
