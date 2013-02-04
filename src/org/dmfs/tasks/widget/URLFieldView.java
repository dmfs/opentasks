/*
 * URLFieldView.java
 *
 * Copyright (C) 2012 Marten Gajda <marten@dmfs.org>
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
 * 
 */
package org.dmfs.tasks.widget;

import org.dmfs.tasks.R;
import org.dmfs.tasks.model.FieldDescriptor;
import org.dmfs.tasks.model.adapters.FieldAdapter;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

/**
 * A view that shows the URL which is clickable.
 * 
 * @author Arjun Naik <arjun@arjunnaik.in>
 */

public class URLFieldView extends AbstractFieldView {

	private FieldAdapter<?> mAdapter;
	private TextView mText;

	public URLFieldView(Context context) {
		super(context);

	}

	public URLFieldView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public URLFieldView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void updateView() {
		if (mValues != null && mAdapter.get(mValues) != null) {
			String urlString = mAdapter.get(mValues).toString();
			mText.setText(Html.fromHtml("<a href='" + urlString + "'>"
					+ urlString + "</a>"));
			mText.setMovementMethod(LinkMovementMethod.getInstance());
		}
		else{
			setVisibility(View.GONE);
		}

	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mText = (TextView) findViewById(R.id.text);
	}

	@Override
	public void setup(FieldDescriptor descriptor, Activity context) {
		super.setup(descriptor, context);
		mAdapter = (FieldAdapter<?>) descriptor.getFieldAdapter();
		mText.setHint(descriptor.getHint());
	}

}
