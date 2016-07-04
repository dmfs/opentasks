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

import android.annotation.SuppressLint;
import android.app.Application;
import android.appwidget.AppWidgetManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v4.content.CursorLoader;
import android.text.format.Time;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import org.dmfs.provider.tasks.TaskContract;
import org.dmfs.provider.tasks.TaskContract.Instances;
import org.dmfs.provider.tasks.TaskContract.Tasks;
import org.dmfs.tasks.R;
import org.dmfs.tasks.model.TaskFieldAdapters;
import org.dmfs.tasks.utils.DateFormatter;
import org.dmfs.tasks.utils.DateFormatter.DateFormatContext;
import org.dmfs.tasks.utils.TimeChangeListener;
import org.dmfs.tasks.utils.TimeChangeObserver;
import org.dmfs.tasks.utils.WidgetConfigurationDatabaseHelper;

import java.util.ArrayList;
import java.util.TimeZone;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


/**
 * A service to keep the task list widget updated.
 * <p>
 * TODO: add support for multiple widgets with different configuration
 * </p>
 * 
 * @author Arjun Naik<arjun@arjunnaik.in>
 * @author Marten Gajda <marten@dmfs.org>
 */
@SuppressLint("NewApi")
public class TaskListWidgetUpdaterService extends RemoteViewsService
{
	private final static String TAG = "TaskListWidgetUpdaterService";


	/*
	 * Return an instance of {@link TaskListViewsFactory}
	 * 
	 * @see android.widget.RemoteViewsService#onGetViewFactory(android.content.Intent)
	 */
	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent)
	{

		return new TaskListViewsFactory(this, intent);
	}

	/**
	 * This class implements the {@link RemoteViewsFactory} interface. It provides the data for the {@link TaskListWidgetProvider}. It loads the due tasks
	 * asynchronously using a {@link CursorLoader}. It also provides methods to the remote views to retrieve the data.
	 */
	public static class TaskListViewsFactory implements RemoteViewsService.RemoteViewsFactory, TimeChangeListener
	{
		/** The {@link TaskListWidgetItem} array which stores the tasks to be displayed. When the cursor loads it is updated. */
		private TaskListWidgetItem[] mItems = null;

		/** The {@link Context} of the {@link Application} to which this widget belongs. */
		private Context mContext;

		/** The app widget id. */
		private int mAppWidgetId = -1;

		/** This variable is used to store the current time for reference. */
		private Time mNow;

		/** The resource from the {@link Application}. */
		private Resources mResources;

		/** The due date formatter. */
		private DateFormatter mDueDateFormatter;

		/**
		 * The executor to reload the tasks.
		 */
		private final Executor mExecutor = Executors.newSingleThreadExecutor();

		private String mAuthority;

		/** A status variable that is used in onDataSetChanged to switch between updating the view and reloading the the whole dataset **/
		private boolean mDoNotReload;


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
			mDueDateFormatter = new DateFormatter(context);
			new TimeChangeObserver(context, this);
			mAuthority = TaskContract.taskAuthority(context);
		}


		public void reload()
		{
			mItems = null;
			mExecutor.execute(mReloadTasks);
		}


		/**
		 * Required for the broadcast receiver.
		 */
		public TaskListViewsFactory()
		{
		}


		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.RemoteViewsService.RemoteViewsFactory#onCreate()
		 */
		@Override
		public void onCreate()
		{
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
			TaskListWidgetItem[] items = mItems;

			/** We use this check because there is a small gap between when the database is updated and the widget is notified */
			if (items == null || position < 0 || position >= items.length)
			{
				return null;
			}
			RemoteViews row = new RemoteViews(mContext.getPackageName(), R.layout.task_list_widget_item);

			String taskTitle = items[position].getTaskTitle();
			row.setTextViewText(android.R.id.title, taskTitle);
			row.setInt(R.id.task_list_color, "setBackgroundColor", items[position].getTaskColor());

			Time dueDate = items[position].getDueDate();
			if (dueDate != null)
			{
				if (mNow == null)
				{
					mNow = new Time();
				}
				mNow.clear(TimeZone.getDefault().getID());
				mNow.setToNow();
				dueDate.normalize(true);
				mNow.normalize(true);

				row.setTextViewText(android.R.id.text1, mDueDateFormatter.format(dueDate, DateFormatContext.WIDGET_VIEW));

				// highlight overdue dates & times
				if ((!dueDate.allDay && dueDate.before(mNow) || dueDate.allDay
					&& (dueDate.year < mNow.year || dueDate.yearDay <= mNow.yearDay && dueDate.year == mNow.year))
					&& !items[position].getIsClosed())
				{
					row.setTextColor(android.R.id.text1, mResources.getColor(R.color.holo_red_light));
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


			Uri taskUri = ContentUris.withAppendedId(Tasks.getContentUri(mAuthority), items[position].getTaskId());
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
			return 1;
		}


		/*
		 * The position corresponds to the ID.
		 * 
		 * @see android.widget.RemoteViewsService.RemoteViewsFactory#getItemId(int)
		 */
		@Override
		public long getItemId(int position)
		{
			return position;
		}


		/*
		 * 
		 * 
		 * @see android.widget.RemoteViewsService.RemoteViewsFactory#hasStableIds()
		 */
		@Override
		public boolean hasStableIds()
		{
			return true;
		}


		/*
		 * Nothing to do when data set is changed.
		 * 
		 * @see android.widget.RemoteViewsService.RemoteViewsFactory#onDataSetChanged()
		 */
		@Override
		public void onDataSetChanged()
		{
			if (mDoNotReload)
			{
				mDoNotReload = false;
			}
			else
			{
				reload();
			}
		}


		/*
		 * @see org.dmfs.tasks.utils.TimeChangeListener#onTimeUpdate(org.dmfs.tasks.utils.TimeChangeObserver)
		 */
		@Override
		public void onTimeUpdate(TimeChangeObserver timeChangeObserver)
		{
			// reload the tasks
			reload();
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


		/**
		 * Gets the array of {@link TaskListWidgetItem}s.
		 * 
		 * @return the widget items
		 */
		public static TaskListWidgetItem[] getWidgetItems(Cursor mTasksCursor)
		{
			if (mTasksCursor.getCount() > 0)
			{
				TaskListWidgetItem[] items = new TaskListWidgetItem[mTasksCursor.getCount()];
				int itemIndex = 0;

				while (mTasksCursor.moveToNext())
				{
					items[itemIndex] = new TaskListWidgetItem(TaskFieldAdapters.INSTANCE_TASK_ID.get(mTasksCursor), TaskFieldAdapters.TITLE.get(mTasksCursor),
						TaskFieldAdapters.DUE.get(mTasksCursor), TaskFieldAdapters.LIST_COLOR.get(mTasksCursor), TaskFieldAdapters.IS_CLOSED.get(mTasksCursor));
					itemIndex++;
				}
				return items;
			}
			return null;
		}

		/**
		 * A {@link Runnable} that loads the tasks to show in the widget.
		 */
		private Runnable mReloadTasks = new Runnable()
		{

			@Override
			public void run()
			{
				// load TaskLists for this widget
				WidgetConfigurationDatabaseHelper configHelper = new WidgetConfigurationDatabaseHelper(mContext);
				SQLiteDatabase db = configHelper.getWritableDatabase();

				ArrayList<Long> lists = WidgetConfigurationDatabaseHelper.loadTaskLists(db, mAppWidgetId);
				db.close();

				// build selection string
				StringBuilder selection = new StringBuilder(TaskContract.Instances.VISIBLE + ">0 and " + TaskContract.Instances.IS_CLOSED + "=0 AND ("
					+ TaskContract.Instances.INSTANCE_START + "<=" + System.currentTimeMillis() + " OR " + TaskContract.Instances.INSTANCE_START
					+ " is null)");

				if (lists != null && lists.size() > 0)
				{
					selection.append(" AND ( ");

					for (int i = 0; i < lists.size(); i++)
					{
						Long listId = lists.get(i);

						if (i < lists.size() - 1)
						{
							selection.append(Instances.LIST_ID).append(" = ").append(listId).append(" OR ");
						}
						else
						{
							selection.append(Instances.LIST_ID).append(" = ").append(listId).append(" ) ");
						}
					}
				}

				// load all upcoming non-completed tasks
				Cursor c = mContext.getContentResolver().query(
					TaskContract.Instances.getContentUri(mAuthority),
					null,
					selection.toString(),
					null,
					TaskContract.Instances.INSTANCE_DUE + " is null, " + TaskContract.Instances.DEFAULT_SORT_ORDER + ", " + TaskContract.Instances.PRIORITY
						+ " is null, " + TaskContract.Instances.PRIORITY + ", " + TaskContract.Instances.CREATED + " DESC");

				if (c != null)
				{
					try
					{
						mItems = getWidgetItems(c);
					}
					finally
					{
						c.close();
					}
				}
				else
				{
					mItems = new TaskListWidgetItem[0];
				}

				// tell to only update the view in the next onDataSetChanged();
				mDoNotReload = true;

				// notify the widget manager about the update
				AppWidgetManager widgetManager = AppWidgetManager.getInstance(mContext);
				if (mAppWidgetId != -1)
				{
					widgetManager.notifyAppWidgetViewDataChanged(mAppWidgetId, R.id.task_list_widget_lv);

				}
			}
		};
	}
}
