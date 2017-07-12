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

package org.dmfs.tasks;

import android.app.Application;
import android.os.Process;
import android.util.Log;


/**
 * The {@link Application} class for the app.
 *
 * @author Gabor Keszthelyi
 */
public final class TasksApplication extends Application
{

    private static final String TAG = "TasksApplication";


    @Override
    public void onCreate()
    {
        super.onCreate();
        checkAppReplacingState();
    }


    /*
     * Fix for https://github.com/dmfs/opentasks/issues/383
     * with workaround suggested at https://issuetracker.google.com/issues/36972466#comment14
     */
    private void checkAppReplacingState()
    {
        if (getResources() == null)
        {
            Log.w(TAG, "App is replacing and getResources() found to be null, killing process. (Workaround for framework bug 36972466");
            Process.killProcess(Process.myPid());
        }
    }
}
