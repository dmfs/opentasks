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

package org.dmfs.android.widgets;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import androidx.appcompat.widget.AppCompatCheckBox;
import android.util.AttributeSet;

import org.dmfs.tasks.R;


/**
 * A checkbox with a colored shape in the background and a check mark (if checked). The check mark is chosen by the lightness of the current color of the shape.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public class ColoredShapeCheckBox extends AppCompatCheckBox
{
    /**
     * The initial color in case no other color is set. This color is transparent but the check mark will be dark.
     */
    private final static int DEFAULT_COLOR = 0x00ffffff;

    /**
     * The shape in the background of the check mark.
     */
    private GradientDrawable mBackgroundShape;

    /**
     * The check mark used for dark background shapes.
     */
    private Drawable mLightCheckmark;

    /**
     * The check mark used for light background shapes.
     */
    private Drawable mDarkCheckmark;

    /**
     * A color state list that defines the background color.
     */
    private ColorStateList mColorStateList;

    /**
     * The current color.
     */
    private int mCurrentColor;


    public ColoredShapeCheckBox(Context context)
    {
        super(context);
        Resources resources = context.getResources();
        mBackgroundShape = (GradientDrawable) resources.getDrawable(R.drawable.oval_shape);
        mLightCheckmark = resources.getDrawable(R.drawable.org_dmfs_colorshape_checkbox_selector_dark);
        mDarkCheckmark = resources.getDrawable(R.drawable.org_dmfs_colorshape_checkbox_selector_light);
        setColor(DEFAULT_COLOR);
    }


    public ColoredShapeCheckBox(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        loadAttrs(attrs);
    }


    public ColoredShapeCheckBox(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        loadAttrs(attrs);
    }


    private void loadAttrs(AttributeSet attrs)
    {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ColoredShapeCheckBox);

        Resources resources = getResources();

        Drawable backgroundShape = typedArray.getDrawable(R.styleable.ColoredShapeCheckBox_backgroundShape);
        if (backgroundShape instanceof GradientDrawable)
        {
            mBackgroundShape = (GradientDrawable) backgroundShape;
        }
        else
        {
            mBackgroundShape = (GradientDrawable) resources.getDrawable(R.drawable.oval_shape);
        }

        Drawable darkCheckmark = typedArray.getDrawable(R.styleable.ColoredShapeCheckBox_darkCheckmark);
        if (darkCheckmark != null)
        {
            mDarkCheckmark = darkCheckmark;
        }
        else
        {
            mDarkCheckmark = resources.getDrawable(R.drawable.org_dmfs_colorshape_checkbox_selector_light);
        }

        Drawable lightCheckmark = typedArray.getDrawable(R.styleable.ColoredShapeCheckBox_lightCheckmark);
        if (lightCheckmark != null)
        {
            mLightCheckmark = lightCheckmark;
        }
        else
        {
            mLightCheckmark = resources.getDrawable(R.drawable.org_dmfs_colorshape_checkbox_selector_dark);
        }
        setColorStateList(typedArray.getColorStateList(R.styleable.ColoredShapeCheckBox_shapeColor));

        typedArray.recycle();
    }


    public void setColor(int color)
    {
        mColorStateList = null;
        applyColor(color);
    }


    private void applyColor(int color)
    {
        mCurrentColor = color;

        // get an approximation for the lightness of the given color
        int y = (3 * Color.red(color) + 4 * Color.green(color) + Color.blue(color)) >> 3;

        mBackgroundShape.setColor(color);

        LayerDrawable layerDrawable = new LayerDrawable(new Drawable[] { mBackgroundShape, y > 190 ? mDarkCheckmark : mLightCheckmark });
        setButtonDrawable(layerDrawable);
    }


    public void setColorStateList(ColorStateList colorStateList)
    {
        mColorStateList = colorStateList;
        applyColor(colorStateList == null ? DEFAULT_COLOR : colorStateList.getColorForState(getDrawableState(), DEFAULT_COLOR));
    }


    public void setColorStateList(int id)
    {
        setColorStateList(getResources().getColorStateList(id));
    }


    @Override
    protected void drawableStateChanged()
    {
        super.drawableStateChanged();
        if (mColorStateList != null)
        {
            int newColor = mColorStateList.getColorForState(getDrawableState(), mCurrentColor);
            if (newColor != mCurrentColor)
            {
                applyColor(newColor);
            }
        }
    }
}
