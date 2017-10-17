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

package org.dmfs.tasks.widget;

import android.view.View;


/**
 * Represents a View that can be populated with other Views, i.e. views can be added to it.
 *
 * @author Gabor Keszthelyi
 */
public interface PopulateableView<V extends View>
{
    /**
     * Adds the given views to this view.
     */
    void populate(Iterable<V> views);
}