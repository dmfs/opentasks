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
import org.dmfs.android.bolts.color.elementary.ValueColor;


/**
 * Color mixed from two others. See implementation for details.
 *
 * @author Gabor Keszthelyi
 */
public final class Mixed implements Color
{
    private final Color mColor1;
    private final Color mColor2;


    public Mixed(Color color1, Color color2)
    {
        mColor1 = color1;
        mColor2 = color2;
    }


    public Mixed(@ColorInt int color1, @ColorInt int color2)
    {
        this(new ValueColor(color1), new ValueColor(color2));
    }


    @Override
    public int argb()
    {
        int color1Argb = mColor1.argb();
        int color2Argb = mColor2.argb();

        int a1 = android.graphics.Color.alpha(color1Argb);

        int r1 = android.graphics.Color.red(color1Argb);
        int g1 = android.graphics.Color.green(color1Argb);
        int b1 = android.graphics.Color.blue(color1Argb);

        int r2 = android.graphics.Color.red(color2Argb);
        int g2 = android.graphics.Color.green(color2Argb);
        int b2 = android.graphics.Color.blue(color2Argb);

        int r3 = (r1 * a1 + r2 * (255 - a1)) / 255;
        int g3 = (g1 * a1 + g2 * (255 - a1)) / 255;
        int b3 = (b1 * a1 + b2 * (255 - a1)) / 255;

        return android.graphics.Color.rgb(r3, g3, b3);
    }
}
