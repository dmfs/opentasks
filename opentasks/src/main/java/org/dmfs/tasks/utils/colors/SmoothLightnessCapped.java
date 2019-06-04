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
 * {@link Color} decorator that limits the HSV-V value to the given <code>maxLightness</code> with a smoothed curved formula.
 * <p>
 * See visualized <a href="http://fooplot.com/?lang=de#W3sidHlwZSI6MCwiZXEiOiJ4KngqeCp4KngqKDAuOC0xKSt4IiwiY29sb3IiOiIjMDAwMDAwIn0seyJ0eXBlIjoxMDAwLCJ3aW5kb3ciOlsiLTAuMjY3ODU2MTYxMjgwMDAwMiIsIjEuNDc2OTc0MzAyNzIiLCItMC4xNTU2Njc5MTA4NjU0NTQ4NCIsIjAuOTE4MDczOTEzMTM0NTQ1NyJdLCJzaXplIjpbNjUwLDU1MF19XQ--">graph</a>
 * (for maxLightness 0.8).
 *
 * @author Gabor Keszthelyi
 */
public final class SmoothLightnessCapped extends DelegatingColor
{
    public SmoothLightnessCapped(final float maxLightness, Color original)
    {
        super(new HsvToned(
                lightness ->
                        lightness * lightness * lightness * lightness * lightness * (maxLightness - 1) + lightness,
                original
        ));
    }


    public SmoothLightnessCapped(float maxLightness, @ColorInt int original)
    {
        this(maxLightness, new ValueColor(original));
    }

}
