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
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import org.dmfs.tasks.R;
import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.FieldDescriptor;
import org.dmfs.tasks.model.IChoicesAdapter;
import org.dmfs.tasks.model.adapters.FieldAdapter;
import org.dmfs.tasks.model.layout.LayoutOptions;


/**
 * Widget for fields providing an {@link IChoicesAdapter}.
 *
 * @author Arjun Naik <arjun@arjunnaik.in>
 * @author Marten Gajda <marten@dmfs.org>
 */
public class ChoicesFieldEditor extends AbstractFieldEditor implements OnItemSelectedListener
{
    private FieldAdapter<Object> mAdapter;
    private ChoicesSpinnerAdapter mSpinnerAdapter;
    private Spinner mSpinner;
    private int mSelectedItem = ListView.INVALID_POSITION;


    public ChoicesFieldEditor(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }


    public ChoicesFieldEditor(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }


    public ChoicesFieldEditor(Context context)
    {
        super(context);
    }


    @Override
    protected void onFinishInflate()
    {
        super.onFinishInflate();
        mSpinner = (Spinner) findViewById(R.id.integer_choices_spinner);
        if (mSpinner == null)
        {
            // on older android versions this may happen if includes are used
            return;
        }

        mSpinner.setOnItemSelectedListener(this);
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        if (mSelectedItem != position && mValues != null)
        {
            // selection was changed, update the values
            mAdapter.validateAndSet(mValues, mSpinnerAdapter.getItem(position));
            mSelectedItem = position;
        }
    }


    @Override
    public void onNothingSelected(AdapterView<?> arg0)
    {
        // update values, set value to null
        mSelectedItem = ListView.INVALID_POSITION;
        mAdapter.validateAndSet(mValues, null);
    }


    @SuppressWarnings("unchecked")
    @Override
    public void setFieldDescription(FieldDescriptor descriptor, LayoutOptions layoutOptions)
    {
        super.setFieldDescription(descriptor, layoutOptions);
        mAdapter = (FieldAdapter<Object>) descriptor.getFieldAdapter();

        IChoicesAdapter choicesAdapter = mFieldDescriptor.getChoices();
        mSpinnerAdapter = new ChoicesSpinnerAdapter(getContext(), choicesAdapter);
        mSpinner.setAdapter(mSpinnerAdapter);
    }


    @Override
    public void onContentChanged(ContentSet contentSet)
    {
        if (mValues != null)
        {
            if (mSpinnerAdapter != null)
            {
                Object mAdapterValue = mAdapter.get(mValues);

                int pos = mSpinnerAdapter.getPosition(mAdapterValue);

                if (!mSpinnerAdapter.hasTitle(mAdapterValue))
                {
                    // hide spinner if the current element has no title
                    setVisibility(View.GONE);
                    return;
                }

                setVisibility(View.VISIBLE);

                if (pos != mSelectedItem)
                {
                    mSelectedItem = pos;
                    mSpinner.setSelection(mSelectedItem);
                }
                else
                {
                    // something else must have changed, better invalidate the list
                    mSpinnerAdapter.notifyDataSetChanged();
                }
            }
        }
    }


    /**
     * An adapter between {@link IChoicesAdapter}s and {@link SpinnerAdapter}s.
     * <p>
     * This makes is easy to show values provided by an {@link IChoicesAdapter} in a {@link Spinner}.
     * </p>
     *
     * @author Marten Gajda <marten@dmfs.org>
     */
    private class ChoicesSpinnerAdapter extends BaseAdapter implements SpinnerAdapter
    {
        private LayoutInflater mLayoutInflater;
        private IChoicesAdapter mAdapter;


        public ChoicesSpinnerAdapter(Context context, IChoicesAdapter a)
        {
            mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mAdapter = a;
        }


        /**
         * Populates a view with the values of the item at <code>position</code>
         *
         * @param position
         *         The position of the item.
         * @param view
         *         The {@link View} to populate.
         */
        private void populateView(int position, View view)
        {
            SpinnerItemTag tag = (SpinnerItemTag) view.getTag();

            String title = mAdapter.getTitle(getItem(position));
            Drawable image = mAdapter.getDrawable(getItem(position));

            if (image != null)
            {
                tag.image.setImageDrawable(image);
                tag.image.setVisibility(View.VISIBLE);
            }
            else
            {
                tag.image.setVisibility(View.GONE);
            }
            tag.text.setText(title);
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            if (convertView == null)
            {
                // inflate a new view and add a tag
                convertView = mLayoutInflater.inflate(R.layout.integer_choices_spinner_selected_item, parent, false);
                SpinnerItemTag tag = new SpinnerItemTag();
                tag.image = (ImageView) convertView.findViewById(R.id.integer_choice_item_image);
                tag.text = (TextView) convertView.findViewById(R.id.integer_choice_item_text);
                convertView.setTag(tag);
            }

            populateView(position, convertView);

            return convertView;
        }


        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent)
        {
            if (convertView == null)
            {
                // inflate a new view and add a tag
                convertView = mLayoutInflater.inflate(R.layout.integer_choices_spinner_item, parent, false);
                SpinnerItemTag tag = new SpinnerItemTag();
                tag.image = (ImageView) convertView.findViewById(R.id.integer_choice_item_image);
                tag.text = (TextView) convertView.findViewById(R.id.integer_choice_item_text);
                convertView.setTag(tag);
            }

            populateView(position, convertView);

            return convertView;
        }


        /**
         * Return the position of the first item that equals the given {@link Object}.
         *
         * @param object
         *         The object to match.
         *
         * @return The position of the item or <code>-1</code> if no such item has been found.
         */
        public int getPosition(Object object)
        {
            return mAdapter.getIndex(object);
        }


        @Override
        public int getCount()
        {
            return mAdapter.getCount();
        }


        @Override
        public Object getItem(int position)
        {
            return mAdapter.getItem(position);
        }


        @Override
        public long getItemId(int position)
        {
            return position;
        }


        /**
         * Checks if there is a title for the given {@link Object}.
         *
         * @param object
         *         The {@link Object} to check.
         *
         * @return <code>true</code> if the adapter provides a title for this item.
         */
        public boolean hasTitle(Object object)
        {
            return mAdapter.getTitle(object) != null;
        }


        /**
         * A tag that allows quick access to the child views in a view.
         */
        private class SpinnerItemTag
        {
            ImageView image;
            TextView text;
        }

    }
}
