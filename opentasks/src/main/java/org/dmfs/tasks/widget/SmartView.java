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
 * Interface that can be implemented by any {@link View} that is 'smart', i.e. that takes care of how to update itself
 * from data/model and may also initiate actions, when clicked for example, instead of calling back.
 *
 * @author Gabor Keszthelyi
 */
public interface SmartView<D>
{

    /**
     * Called to update the View's content with the provided data.
     */
    void update(D data);

}
