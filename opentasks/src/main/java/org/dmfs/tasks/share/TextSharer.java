package org.dmfs.tasks.share;

import android.content.Context;
import android.content.Intent;


/**
 * Shares text content with other apps.
 *
 * @author Gabor Keszthelyi
 */
public class TextSharer
{
    private final Context mContext;


    public TextSharer(Context context)
    {
        mContext = context;
    }


    public void share(String subject, String text)
    {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        sendIntent.setType("text/plain");
        mContext.startActivity(sendIntent);
    }

}
