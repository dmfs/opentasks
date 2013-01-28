package org.dmfs.tasks;

/*
 * TaskViewActivity.java
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

import java.util.ArrayList;

import org.dmfs.tasks.model.Model;
import org.dmfs.tasks.model.Sources;
import org.dmfs.tasks.utils.AsyncContentLoader;
import org.dmfs.tasks.utils.ContentValueMapper;
import org.dmfs.tasks.utils.OnContentLoadedListener;
import org.dmfs.tasks.widget.TaskEdit;
import org.dmfs.tasks.widget.TaskView;
import org.dmfs.provider.tasks.TaskContract.Tasks;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;


public class TaskViewActivity extends Activity implements OnContentLoadedListener
{

	private static final String KEY_VALUES = "key_values";

	private static final ContentValueMapper CONTENT_VALUE_MAPPER = new ContentValueMapper()
		.addString(Tasks.TITLE, Tasks.LOCATION, Tasks.DESCRIPTION, Tasks.GEO, Tasks.URL, Tasks.TZ, Tasks.DURATION)
		.addInteger(Tasks.PRIORITY, Tasks.TASK_COLOR, Tasks.COMPLETED, Tasks.DTSTART, Tasks.DUE, Tasks.STATUS, Tasks.CLASSIFICATION).addLong(Tasks.LIST_ID);

	private static final String TAG = null;

	private ArrayList<ContentValues> mValues = new ArrayList<ContentValues>();
	private ViewGroup mContent;
	private Model mModel;


	public void onCreate(Bundle savedInstanceState)
	{

		super.onCreate(savedInstanceState);
		setContentView(R.layout.task_detail_view);
		mContent = (ViewGroup) findViewById(R.id.content);

		Intent intent = getIntent();
		mModel = Sources.getInstance(getApplicationContext()).getModel("");

		Uri data = intent.getData();

		if (savedInstanceState == null)
		{
			new AsyncContentLoader(this, this, CONTENT_VALUE_MAPPER).execute(data);
		}
		else
		{
			mValues = savedInstanceState.getParcelableArrayList(KEY_VALUES);
			updateView();
		}
	}


	private void updateView()
	{
		final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		mContent.removeAllViews();
		for (ContentValues values : mValues)
		{
			TaskView editor = (TaskView) inflater.inflate(R.layout.task_view, mContent, false);
			editor.setModel(mModel);
			Log.d(TAG, "Values : " + values.toString());
			editor.setValues(values);
			mContent.addView(editor);
		}
	}


	@Override
	public void onContentLoaded(ContentValues values)
	{
		if (values == null)
		{
			Toast.makeText(this, "Could not load Task", Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		mValues.add(values);
		updateView();
	}


	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putParcelableArrayList(KEY_VALUES, mValues);
	}

}
