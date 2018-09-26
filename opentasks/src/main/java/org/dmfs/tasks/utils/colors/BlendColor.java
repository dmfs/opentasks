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

import org.dmfs.android.bolts.color.Color;


/**
 * Color which mixes with another color to a given one.
 * <p>
 * This is essentially the reverse of {@link Mixed}. So if {@link Mixed} blends the colors A and B into C, this class returns B when you give it A (baseColor),
 * C (targetColor) and the alpha value of B.
 * <p>
 * This is used in the editor to determine the color of the action bar, so it mixes with the background in a way that keeps the result the same (while the alpha
 * of the action bar goes towards opaque).
 *
 * @author Gabor Keszthelyi
 */
public final class BlendColor implements Color
{
    private final Color mBaseColor;
    private final Color mTargetColor;
    private final int mAlpha;


    public BlendColor(Color baseColor, Color targetColor, int alpha)
    {
        mBaseColor = baseColor;
        mTargetColor = targetColor;
        mAlpha = alpha;
    }


    @Override
    public int argb()
    {
        if (mAlpha <= 0 || mAlpha > 254)
        {
            return mTargetColor.argb();
        }

        int baseArgb = mBaseColor.argb();
        int targetArgb = mTargetColor.argb();

        int r1 = android.graphics.Color.red(baseArgb);
        int g1 = android.graphics.Color.green(baseArgb);
        int b1 = android.graphics.Color.blue(baseArgb);

        int r3 = android.graphics.Color.red(targetArgb);
        int g3 = android.graphics.Color.green(targetArgb);
        int b3 = android.graphics.Color.blue(targetArgb);

        int r2 = (int) Math.ceil((Math.max(0, r3 * 255 - r1 * (255 - mAlpha))) / mAlpha);
        int g2 = (int) Math.ceil((Math.max(0, g3 * 255 - g1 * (255 - mAlpha))) / mAlpha);
        int b2 = (int) Math.ceil((Math.max(0, b3 * 255 - b1 * (255 - mAlpha))) / mAlpha);

        return android.graphics.Color.argb(mAlpha, r2, g2, b2);
    }
}
