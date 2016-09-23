package org.dmfs.tasks.utils;

import android.content.Context;
import android.content.Intent;


/**
 * @author Gabor Keszthelyi
 */

public class TextSharer
{
    private final Context mContext;


    public TextSharer(Context context)
    {
        mContext = context;
    }


    public void shareTextWithOtherApps(String title, String body)
    {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);

        /* TODO
          figure the usage of these extras out,
         ideally to be able to use with email (subject+body) and simple full text as well*/
        sendIntent.putExtra(Intent.EXTRA_TITLE, title);
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, title);
        sendIntent.putExtra(Intent.EXTRA_TEXT, body);

        sendIntent.setType("text/plain");
        mContext.startActivity(sendIntent);
    }

}
