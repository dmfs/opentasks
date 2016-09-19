package org.dmfs.tasks.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;


/**
 * Handles opening map application on the device with a location query string.
 *
 * @author Gabor Keszthelyi
 */
public class MapOpener
{
    private final Context mContext;


    public MapOpener(Context mContext)
    {
        this.mContext = mContext;
    }


    public void openMapWithLocation(String locationQuery)
    {
        Uri uri = Uri.parse("geo:0,0?q=" + Uri.encode(locationQuery));
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, uri);
        mContext.startActivity(mapIntent);
    }
}
