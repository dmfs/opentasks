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

import android.support.annotation.IntDef;
import org.dmfs.tasks.ViewTaskFragment;
import org.dmfs.tasks.utils.DerivedEventTracker;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.List;

import static org.dmfs.tasks.share.ShareIntentUpdateEventTracker.Event.CONTENT_LOADED;
import static org.dmfs.tasks.share.ShareIntentUpdateEventTracker.Event.MENU_CREATED;
import static org.dmfs.tasks.share.ShareIntentUpdateEventTracker.Event.MODEL_LOADED;


/**
 * {@link DerivedEventTracker} to get notified when the share intent has to be updated for the {@link ViewTaskFragment}
 * menu. Share intent creation needs to wait for the Menu, ContentSet, Model to be created/loaded, and the intent has to
 * updated afterwards if any of them changes again.
 *
 * @author Gabor Keszthelyi
 */
public class ShareIntentUpdateEventTracker implements DerivedEventTracker
{
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ MODEL_LOADED, CONTENT_LOADED, MENU_CREATED })
    public @interface Event
    {
        int MODEL_LOADED = 0;
        int CONTENT_LOADED = 1;
        int MENU_CREATED = 2;
    }


    private final List<Boolean> mHasHappenedFlags = Arrays.asList(false, false, false);

    private Listener mListener;


    @Override
    public void event(@Event int event)
    {
        mHasHappenedFlags.set(event, true);
        if (mListener != null && !mHasHappenedFlags.contains(false))
        {
            mListener.onDerivedEvent();
        }
    }


    @Override
    public void setListener(Listener listener)
    {
        mListener = listener;
    }
}
