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

import android.support.annotation.ColorInt;

import org.dmfs.android.bolts.color.Color;
import org.dmfs.android.bolts.color.colors.ValueColor;


/**
 * {@link Color} decorator that darkens the color dynamically.
 * (Typically to use for status bar color.)
 *
 * @author Gabor Keszthelyi
 */
public final class Darkened implements Color
{
    private final Color mOriginal;


    public Darkened(Color original)
    {
        mOriginal = original;
    }


    public Darkened(@ColorInt int originalColorInt)
    {
        this(new ValueColor(originalColorInt));
    }


    @Override
    public int argb()
    {
        float[] hsv = new float[3];
        android.graphics.Color.colorToHSV(mOriginal.argb(), hsv);
        hsv[2] = hsv[2] * 0.75f;
        return android.graphics.Color.HSVToColor(hsv);
    }
}
