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

import org.dmfs.jems.optional.Optional;
import org.dmfs.tasks.R;


/**
 * {@link SmartView} adapting any {@link View} so that its background can be updated with a progress in percent,
 * so that it shows the value as a proportionate horizontal overlay.
 *
 * @author Gabor Keszthelyi
 */
public final class ProgressBackgroundView implements SmartView<Optional<Integer>>
{
    private final View mBackgroundView;


    public ProgressBackgroundView(View backgroundView)
    {
        mBackgroundView = backgroundView;
    }


    @Override
    public void update(Optional<Integer> percentComplete)
    {
        if (percentComplete.isPresent())
        {
            mBackgroundView.setPivotX(0);
            if (percentComplete.value() < 100)
            {
                mBackgroundView.setScaleX(percentComplete.value() / 100f);
                mBackgroundView.setBackgroundResource(R.drawable.task_progress_background_shade);
            }
            else
            {
                mBackgroundView.setScaleX(1);
                mBackgroundView.setBackgroundResource(R.drawable.complete_task_background_overlay);
            }
        }
    }
}
