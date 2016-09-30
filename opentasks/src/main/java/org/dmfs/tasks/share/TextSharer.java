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

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;
import org.dmfs.tasks.R;


/**
 * Shares text content with other apps.
 *
 * @author Gabor Keszthelyi
 */
public class TextSharer
{
    // Needs Activity, not Context, because startActivity() from not Activity
    // crashes if Intent.FLAG_ACTIVITY_NEW_TASK is not used.
    private final Activity mActivity;


    public TextSharer(Activity activity)
    {
        mActivity = activity;
    }


    public void share(String subject, String text)
    {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        sendIntent.setType("text/plain");

        if (sendIntent.resolveActivity(mActivity.getPackageManager()) != null)
        {
            Intent chooserIntent = Intent.createChooser(sendIntent,
                    mActivity.getString(R.string.activity_task_details_share_task));
            mActivity.startActivity(chooserIntent);
        }
        else
        {
            // This will probably never happen, so no need to localize the text.
            Toast.makeText(mActivity, "No app found to receive shared text", Toast.LENGTH_LONG).show();
        }
    }

}
