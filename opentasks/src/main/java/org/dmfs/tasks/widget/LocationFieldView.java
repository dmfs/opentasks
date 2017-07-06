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

package org.dmfs.tasks.widget;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import org.dmfs.tasks.R;
import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.FieldDescriptor;
import org.dmfs.tasks.model.adapters.FieldAdapter;
import org.dmfs.tasks.model.layout.LayoutOptions;


/**
 * A widget that shows the location. When clicked, it opens maps.
 *
 * @author Gabor Keszthelyi
 */
public class LocationFieldView extends AbstractFieldView implements View.OnClickListener
{
    /**
     * The {@link FieldAdapter} of the field for this view.
     */
    private FieldAdapter<?> mAdapter;

    /**
     * The {@link TextView} to show the text in.
     */
    private TextView mTextView;

    private String mText;


    public LocationFieldView(Context context)
    {
        super(context);
    }


    public LocationFieldView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }


    public LocationFieldView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }


    @Override
    protected void onFinishInflate()
    {
        super.onFinishInflate();
        mTextView = (TextView) findViewById(R.id.text);
        setOnClickListener(this);
    }


    @Override
    public void setFieldDescription(FieldDescriptor descriptor, LayoutOptions layoutOptions)
    {
        super.setFieldDescription(descriptor, layoutOptions);
        mAdapter = descriptor.getFieldAdapter();
    }


    @Override
    public void onContentChanged(ContentSet contentSet)
    {
        if (mValues != null)
        {
            Object adapterValue = mAdapter.get(mValues);
            String adapterStringValue = adapterValue != null ? adapterValue.toString() : null;

            if (!TextUtils.isEmpty(adapterStringValue))
            {
                mText = adapterStringValue;
                mTextView.setText(adapterStringValue);
                setVisibility(View.VISIBLE);
            }
            else
            {
                // don't show empty values
                setVisibility(View.GONE);
            }
        }
    }


    @Override
    public void onClick(View v)
    {
        openMapWithLocation(mText);
    }


    private void openMapWithLocation(String locationQuery)
    {
        boolean resolved = tryOpeningMapApplication(locationQuery);
        if (!resolved)
        {
            tryOpenGoogleMapsInBrowser(locationQuery);
        }
    }


    private boolean tryOpeningMapApplication(String locationQuery)
    {
        Uri mapAppUri = Uri.parse("geo:0,0?q=" + Uri.encode(locationQuery));
        Intent mapAppIntent = new Intent(Intent.ACTION_VIEW, mapAppUri);
        if (mapAppIntent.resolveActivity(getContext().getPackageManager()) != null)
        {
            getContext().startActivity(mapAppIntent);
            return true;
        }
        return false;
    }


    private void tryOpenGoogleMapsInBrowser(String locationQuery)
    {
        Uri googleMapInBrowserUri = Uri.parse("http://maps.google.com/?q=" + Uri.encode(locationQuery));
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, googleMapInBrowserUri);
        if (browserIntent.resolveActivity(getContext().getPackageManager()) != null)
        {
            getContext().startActivity(browserIntent);
        }
    }
}
