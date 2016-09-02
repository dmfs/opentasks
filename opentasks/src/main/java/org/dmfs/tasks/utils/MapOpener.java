package org.dmfs.tasks.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;


/**
 * Handles Google Maps opening for a location string.
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


    public void openMapWithLocation(String location)
    {
        Uri uri = Uri.parse("geo:0,0?q=" + Uri.encode(location));
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, uri);
        mapIntent.setPackage("com.google.android.apps.maps");
        mContext.startActivity(mapIntent);
    }
}
