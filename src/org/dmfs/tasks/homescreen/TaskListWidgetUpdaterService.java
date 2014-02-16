/*
 * 
 *
 * Copyright (C) 2013 Marten Gajda <marten@dmfs.org>
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

package org.dmfs.tasks.homescreen;

import java.util.TimeZone;

import org.dmfs.provider.tasks.TaskContract;
import org.dmfs.provider.tasks.TaskContract.Tasks;
import org.dmfs.tasks.R;
import org.dmfs.tasks.homescreen.utils.TaskListWidgetItem;
import org.dmfs.tasks.homescreen.utils.WidgetCusorListGenerator;
import org.dmfs.tasks.utils.DueDateFormatter;
import org.dmfs.tasks.utils.TimeChangeListener;
import org.dmfs.tasks.utils.TimeChangeObserver;

import android.annotation.SuppressLint;
import android.app.Application;
import android.appwidget.AppWidgetManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.Loader.OnLoadCompleteListener;
import android.text.format.Time;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;


/**
 * A service to keep the task list widget updated.
 * 
 * @author Arjun Naik<arjun@arjunnaik.in>
 */
@SuppressLint("NewApi")
public class TaskListWidgetUpdaterService extends RemoteViewsService
{
	private final static String TAG = "TaskListWidgetUpdaterService";

	/** The context of the {@link Application}. */
	private Context mContext;


	/*
	 * Save the {@link Context}
	 * 
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate()
	{
		super.onCreate();
		mContext = getApplicationContext();
	}


	/*
	 * Return an instance of {@link TaskListViewsFactory}
	 * 
	 * @see android.widget.RemoteViewsService#onGetViewFactory(android.content.Intent)
	 */
	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent)
	{
		return new TaskListViewsFactory(mContext, intent);
	}

	/**
	 * This class implements the {@link RemoteViewsFactory} interface. It provides the data for the {@link TaskListWidgetProvider}. It loads the due tasks
	 * asynchronously using a {@link CursorLoader}. It also provides methods to the remote views to retrieve the data.
	 */
	public static class TaskListViewsFactory implements RemoteViewsService.RemoteViewsFactory, OnLoadCompleteListener<Cursor>, TimeChangeListener
	{
		/** The {@link TaskListWidgetItem} array which stores the tasks to be displayed. When the cursor loads it is updated. */
		private TaskListWidgetItem[] mItems = null;

		/** The {@link Context} of the {@link Application} to which this widget belongs. */
		private final Context mContext;

		/** The app widget id. */
		private final int mAppWidgetId;

		/** The loader for loading the due tasks. */
		private CursorLoader mLoader;

		/** This variable is used to store the current time for reference. */
		private Time mNow;

		/** The resource from the {@link Application}. */
		private final Resources mResources;

		/** The due date formatter. */
		private final DueDateFormatter mDueDateFormatter;


		/**
		 * Instantiates a new task list views factory.
		 * 
		 * @param context
		 *            the context
		 * @param intent
		 *            the intent
		 */
		public TaskListViewsFactory(Context context, Intent intent)
		{
			mContext = context;
			mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
			mResources = context.getResources();
			mDueDateFormatter = new DueDateFormatter(context);
			new TimeChangeObserver(context, this);
		}


		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.RemoteViewsService.RemoteViewsFactory#onCreate()
		 */
		@Override
		public void onCreate()
		{
			initLoader();
		}


		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.RemoteViewsService.RemoteViewsFactory#onDestroy()
		 */
		@Override
		public void onDestroy()
		{
			// no-op
		}


		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.RemoteViewsService.RemoteViewsFactory#getCount()
		 */
		@Override
		public int getCount()
		{
			if (mItems == null)
			{
				return 0;
			}
			return (mItems.length);
		}


		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.RemoteViewsService.RemoteViewsFactory#getViewAt(int)
		 */
		@Override
		public RemoteViews getViewAt(int position)
		{

			/** We use this check because there is a small gap between when the database is updated and the widget is notified */
			if (position < 0 || position >= getCount())
			{
				return null;
			}

			RemoteViews row = new RemoteViews(mContext.getPackageName(), R.layout.task_list_widget_item);

			row.setTextViewText(android.R.id.title, mItems[position].getTaskTitle());
			row.setInt(R.id.task_list_color, "setBackgroundColor", mItems[position].getTaskColor());

			Time dueDate = mItems[position].getDueDate();

			if (dueDate != null)
			{
				if (mNow == null)
				{
					mNow = new Time();
				}
				mNow.clear(TimeZone.getDefault().getID());
				mNow.setToNow();

				row.setTextViewText(android.R.id.text1, mDueDateFormatter.format(dueDate));

				// highlight overdue dates & times
				if (dueDate.before(mNow) & !mItems[position].getIsClosed())
				{
					row.setTextColor(android.R.id.text1, mResources.getColor(android.R.color.holo_red_light));
				}
				else
				{
					row.setTextColor(android.R.id.text1, mResources.getColor(R.color.lighter_gray));
				}
			}
			else
			{
				row.setTextViewText(android.R.id.text1, null);
			}

			Uri taskUri = ContentUris.withAppendedId(Tasks.CONTENT_URI, mItems[position].getTaskId());
			Intent i = new Intent();
			i.setData(taskUri);

			row.setOnClickFillInIntent(R.id.widget_list_item, i);
			return (row);
		}


		/*
		 * Don't show any loading views
		 * 
		 * @see android.widget.RemoteViewsService.RemoteViewsFactory#getLoadingView()
		 */
		@Override
		public RemoteViews getLoadingView()
		{
			return null;
		}


		/*
		 * Only single type of list item.
		 * 
		 * @see android.widget.RemoteViewsService.RemoteViewsFactory#getViewTypeCount()
		 */
		@Override
		public int getViewTypeCount()
		{
			return (1);
		}


		/*
		 * The position corresponds to the ID.
		 * 
		 * @see android.widget.RemoteViewsService.RemoteViewsFactory#getItemId(int)
		 */
		@Override
		public long getItemId(int position)
		{
			return (position);
		}


		/*
		 * 
		 * 
		 * @see android.widget.RemoteViewsService.RemoteViewsFactory#hasStableIds()
		 */
		@Override
		public boolean hasStableIds()
		{
			return (true);
		}


		/*
		 * Nothing to do when data set is changed.
		 * 
		 * @see android.widget.RemoteViewsService.RemoteViewsFactory#onDataSetChanged()
		 */
		@Override
		public void onDataSetChanged()
		{
			// no-op
		}


		/**
		 * Initializes the loader.
		 */
		public void initLoader()
		{
			// Search for events from now until some time in the future
			mLoader = new CursorLoader(mContext, TaskContract.Instances.CONTENT_URI, null, TaskContract.Instances.VISIBLE + ">0 and "
				+ TaskContract.Instances.IS_CLOSED + "=0 AND (" + TaskContract.Instances.INSTANCE_START + "<=" + System.currentTimeMillis() + " OR "
				+ TaskContract.Instances.INSTANCE_START + " is null)", null, TaskContract.Instances.INSTANCE_DUE + " is null, "
				+ TaskContract.Instances.DEFAULT_SORT_ORDER);

			mLoader.registerListener(mAppWidgetId, this);
			mLoader.startLoading();
		}


		/*
		 * When the {@link Cursor} completes loading use a {@link WidgetCursorListGenerator} to load the tasks.
		 * 
		 * @see android.support.v4.content.Loader.OnLoadCompleteListener#onLoadComplete(android.support.v4.content.Loader, java.lang.Object)
		 */
		@Override
		public void onLoadComplete(Loader<Cursor> loader, Cursor cursor)
		{
			Log.d(TAG, "load complete for widget " + mAppWidgetId);

			WidgetCusorListGenerator generator = new WidgetCusorListGenerator(cursor);
			mItems = generator.getWidgetItems();

			AppWidgetManager widgetManager = AppWidgetManager.getInstance(mContext);
			widgetManager.notifyAppWidgetViewDataChanged(mAppWidgetId, R.id.task_list_widget_lv);
		}


		/*
		 * Reset the {@link CursorLoader} when a time change is detected.
		 * 
		 * @see org.dmfs.tasks.utils.TimeChangeListener#onTimeUpdate(org.dmfs.tasks.utils.TimeChangeObserver)
		 */
		@Override
		public void onTimeUpdate(TimeChangeObserver timeChangeObserver)
		{
			mLoader.reset();
		}


		/*
		 * This function is not used.
		 * 
		 * @see org.dmfs.tasks.utils.TimeChangeListener#onAlarm(org.dmfs.tasks.utils.TimeChangeObserver)
		 */
		@Override
		public void onAlarm(TimeChangeObserver timeChangeObserver)
		{
			// Not listening for Alarms in this service.
		}
	}
}
