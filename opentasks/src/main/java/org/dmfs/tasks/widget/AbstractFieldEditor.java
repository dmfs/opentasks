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

import android.content.Context;
import android.util.AttributeSet;


/**
 * Mother of all editor fields.
 * <p>
 * At present it doesn't do anything beside inheriting from {@link AbstractFieldView}, but it might come in handy for future developments, so we till keep it.
 * </p>
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public abstract class AbstractFieldEditor extends AbstractFieldView
{
    public AbstractFieldEditor(Context context)
    {
        super(context);
    }


    public AbstractFieldEditor(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }


    public AbstractFieldEditor(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }
}
