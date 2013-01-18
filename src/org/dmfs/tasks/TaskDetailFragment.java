/*
 * TaskDetailFragment.java
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

package org.dmfs.tasks;

import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.dmfs.provider.tasks.TaskContract;
import org.dmfs.provider.tasks.TaskContract.Tasks;
import org.dmfs.tasks.model.Model;
import org.dmfs.tasks.model.Sources;
import org.dmfs.tasks.utils.AsyncContentLoader;
import org.dmfs.tasks.utils.ContentValueMapper;
import org.dmfs.tasks.utils.OnContentLoadedListener;

/**
 * A fragment representing a single Task detail screen. This fragment is either
 * contained in a {@link TaskListActivity} in two-pane mode (on tablets) or a
 * {@link TaskDetailActivity} on handsets.
 * 
 * @author Arjun Naik <arjun@arjunnaik.in>
 */

public class TaskDetailFragment extends Fragment implements
		OnContentLoadedListener {
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_ITEM_ID = "item_id";

	private static final String TAG = "TaskDetailFragment";

	private static final String KEY_VALUES = "key_values";

	private static final ContentValueMapper CONTENT_VALUE_MAPPER = new ContentValueMapper()
			.addString(Tasks.TITLE, Tasks.LOCATION, Tasks.DESCRIPTION,
					Tasks.GEO, Tasks.URL, Tasks.TZ, Tasks.DURATION)
			.addInteger(Tasks.PRIORITY, Tasks.COLOR, Tasks.COMPLETED,
					Tasks.DTSTART, Tasks.DUE, Tasks.STATUS,
					Tasks.CLASSIFICATION).addLong(Tasks.LIST_ID);

	/**
	 * The dummy content this fragment is presenting.
	 */
	private String taskUri;
	private Context appContext;

	private Intent appIntent;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public TaskDetailFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		taskUri = getArguments().getString(ARG_ITEM_ID);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		appContext = activity.getApplicationContext();
		appIntent = activity.getIntent();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_task_detail,
				container, false);

		// Show the dummy content as text in a TextView.
		/*
		 * if (taskId != null) {
		 * 
		 * Cursor selectedTaskCursor = appContext.getContentResolver().query(
		 * TaskContract.Tasks.CONTENT_URI, new String[] {
		 * TaskContract.Tasks._ID, TaskContract.Tasks.TITLE,
		 * TaskContract.Tasks.PRIORITY, TaskContract.Tasks.DESCRIPTION },
		 * TaskContract.Tasks._ID + "=?", new String[] { taskId }, null);
		 * 
		 * Log.d(TAG, "Are there results :" + (selectedTaskCursor.getCount() > 0
		 * ? "Yes" : "No"));
		 * 
		 * 
		 * int titleColumn =
		 * selectedTaskCursor.getColumnIndex(TaskContract.Tasks.TITLE); int
		 * priorityColumn =
		 * selectedTaskCursor.getColumnIndex(TaskContract.Tasks.PRIORITY); int
		 * descriptionColumn =
		 * selectedTaskCursor.getColumnIndex(TaskContract.Tasks.DESCRIPTION);
		 * 
		 * selectedTaskCursor.moveToFirst(); String taskTitle =
		 * selectedTaskCursor.getString(titleColumn); int taskPriority =
		 * selectedTaskCursor.getInt(priorityColumn); String taskDescription =
		 * selectedTaskCursor.getString(descriptionColumn);
		 * 
		 * Log.d(TAG, taskTitle); Log.d(TAG, "" + taskDescription); Log.d(TAG,
		 * "" + taskPriority);
		 * 
		 * ((TextView) rootView.findViewById(R.id.task_title))
		 * .setText(taskTitle); }
		 */

		if (taskUri != null) {
			((TextView) rootView.findViewById(R.id.task_title))
					.setText(taskUri);

			ViewGroup mContent = (ViewGroup) rootView
					.findViewById(R.id.content);

			Model mModel = Sources.getInstance(appContext).getModel("");

			Uri data = Uri.parse(taskUri);

			if (savedInstanceState == null) {
				new AsyncContentLoader(appContext, this, CONTENT_VALUE_MAPPER)
						.execute(data);
			} else {
				ArrayList<ContentValues> mValues = savedInstanceState
						.getParcelableArrayList(KEY_VALUES);

			}
		}

		return rootView;
	}

	@Override
	public void onContentLoaded(ContentValues values) {
		// TODO Auto-generated method stub

	}
}
