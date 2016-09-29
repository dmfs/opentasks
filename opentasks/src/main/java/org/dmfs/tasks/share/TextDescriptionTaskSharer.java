/*
 * Copyright 2016 Marten Gajda <marten@dmfs.org>
 *
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

package org.dmfs.tasks.share;

import android.content.Context;
import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.Model;


/**
 * Shares the text description of the whole task with other apps.
 *
 * @author Gabor Keszthelyi
 */
public class TextDescriptionTaskSharer implements TaskSharer
{
    private final TaskTextDescriptionComposer mTextComposer;
    private final TextSharer mTextSharer;


    public TextDescriptionTaskSharer(Context context)
    {
        mTextComposer = new BasicTaskTextDescriptionComposer(context);
        mTextSharer = new TextSharer(context);
    }


    @Override
    public void share(ContentSet contentSet, Model model)
    {
        String title = mTextComposer.title(contentSet, model);
        String body = mTextComposer.body(contentSet, model);
        mTextSharer.share(title, body);
    }
}
