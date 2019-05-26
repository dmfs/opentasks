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

package org.dmfs.tasks.utils.colors;

import androidx.annotation.ColorInt;

import org.dmfs.android.bolts.color.Color;
import org.dmfs.android.bolts.color.dynamic.lightness.HsvToned;
import org.dmfs.android.bolts.color.elementary.DelegatingColor;
import org.dmfs.android.bolts.color.elementary.ValueColor;


/**
 * {@link Color} decorator that darkens the original for displaying as status bar color.
 *
 * @author Gabor Keszthelyi
 */
public final class DarkenedForStatusBar extends DelegatingColor
{
    public DarkenedForStatusBar(Color original)
    {
        super(new HsvToned(lightness -> lightness * 0.75f, original));
    }


    public DarkenedForStatusBar(@ColorInt int original)
    {
        this(new ValueColor(original));
    }
}
