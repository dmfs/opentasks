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

package org.dmfs.opentaskspal.readdata.functions;

import org.dmfs.android.bolts.color.Color;
import org.dmfs.android.bolts.color.elementary.ValueColor;
import org.dmfs.jems.function.Function;


/**
 * Pure {@link Function} that converts a String to a {@link Color}
 *
 * @author Gabor Keszthelyi
 */
public final class StringToColor implements Function<String, Color>
{
    public static final Function<String, Color> FUNCTION = new StringToColor();


    private StringToColor()
    {
    }


    @Override
    public Color value(String stringValue)
    {
        return new ValueColor(Integer.valueOf(stringValue));
    }
}
