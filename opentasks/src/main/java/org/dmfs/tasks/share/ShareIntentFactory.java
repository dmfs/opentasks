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
import android.content.Intent;

import org.dmfs.jems.single.Single;
import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.Model;
import org.dmfs.tasks.utils.TaskIntentFactory;


/**
 * Creates intent for sharing a whole task's text description with other apps.
 *
 * @author Gabor Keszthelyi
 */
public final class ShareIntentFactory implements TaskIntentFactory
{

    /**
     * Creates an intent for sharing the description of the whole task in the {@link ContentSet} with other apps.
     *
     * @param contentSet
     *         actual {@link ContentSet} for the task
     * @param model
     *         the model used currently
     *
     * @return the created intent
     */
    @Override
    public Intent create(ContentSet contentSet, Model model, Context context)
    {
        Single<CharSequence> title = new TaskShareTitle(contentSet);
        Single<CharSequence> body = new TaskShareDetails(contentSet, model, context);

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, title.value().toString());
        sendIntent.putExtra(Intent.EXTRA_TEXT, body.value().toString());
        sendIntent.setType("text/plain");

        return sendIntent;
    }
}
