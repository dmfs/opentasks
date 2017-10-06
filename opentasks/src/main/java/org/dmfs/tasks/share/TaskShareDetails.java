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

package org.dmfs.tasks.share;

import android.content.Context;
import android.util.Log;

import org.dmfs.android.carrot.bindings.AndroidBindings;
import org.dmfs.android.carrot.locaters.RawResourceLocator;
import org.dmfs.jems.single.Single;
import org.dmfs.tasks.R;
import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.Model;

import au.com.codeka.carrot.CarrotEngine;
import au.com.codeka.carrot.CarrotException;
import au.com.codeka.carrot.Configuration;
import au.com.codeka.carrot.bindings.Composite;
import au.com.codeka.carrot.bindings.SingletonBindings;

/*
 <task title>
 ============

 <task description>

 [X] checked list item
 [ ] unchecked list item

 Location: <location>
 Start: <start date time> <timezone>
 Due: <due date time> <timezone>
 Completed: <due date time> <timezone>
 Priority: <priority text>
 Privacy: <privacy text>
 Status: <status text>
 <url>

 --
 Shared by OpenTasks
 */


/**
 * {@link CharSequence} detailing information about the task, used when sharing.
 * <p>
 * Implementation uses <code>carrot</code> template engine.
 *
 * @author Gabor Keszthelyi
 */
public final class TaskShareDetails implements Single<CharSequence>
{
    private final ContentSet mContentSet;
    private final Model mModel;
    private final Context mContext;


    public TaskShareDetails(ContentSet contentSet, Model model, Context context)
    {
        mContentSet = contentSet;
        mModel = model;
        mContext = context.getApplicationContext();
    }


    @Override
    public CharSequence value()
    {
        CarrotEngine engine = new CarrotEngine(new Configuration.Builder().setResourceLocator(new RawResourceLocator.Builder(mContext)).build());
        try
        {
            String output = engine.process(String.valueOf(R.raw.sharetask),
                    new Composite(
                            new AndroidBindings(mContext),
                            new SingletonBindings("$task", new TaskBindings(mContentSet, mModel)),
                            new SingletonBindings("tformat", new TimeFormatter(mContext, mContentSet))));

            Log.v("TaskShareDetails", output);
            return output;
        }
        catch (CarrotException e)
        {
            throw new RuntimeException("Failed to process template with carrot", e);
        }
    }
}
