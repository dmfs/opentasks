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
 * {@link Color} decorator to adjust color of the task list for the FAB.
 * It gets slightly lighter color to stand out a bit more. If it's too light, it is darkened instead.
 *
 * @author Gabor Keszthelyi
 */
public final class AdjustedForFab implements Color
{

    private final Color mListColor;


    public AdjustedForFab(Color listColor)
    {
        mListColor = listColor;
    }


    public AdjustedForFab(@ColorInt int listColor)
    {
        this(new ValueColor(listColor));
    }


    @Override
    public int argb()
    {
        float[] hsv = new float[3];
        android.graphics.Color.colorToHSV(mListColor.argb(), hsv);
        if (hsv[2] * (1 - hsv[1]) < 0.4)
        {
            hsv[2] *= 1.2;
        }
        else
        {
            hsv[2] /= 1.2;
        }
        return android.graphics.Color.HSVToColor(hsv);
    }
}
