/*
 * Copyright (C) 2015 Marten Gajda <marten@dmfs.org>
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

package org.dmfs.tasks.notification;

import java.util.ArrayList;
import java.util.TimeZone;

import org.dmfs.provider.tasks.TaskContract;
import org.dmfs.provider.tasks.TaskContract.Tasks;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.Duration;
import org.dmfs.tasks.R;
import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.TaskFieldAdapters;
import org.dmfs.tasks.notification.NotificationActionUtils.NotificationAction;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Parcel;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Action;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.app.NotificationManagerCompat;
import android.text.format.Time;
import android.util.Log;


/**
 * A {@link Service} that triggers and updates {@link Notification}s for Due and Start alarms as well as pinned tasks.
 * 
 * @author Tobias Reinsch <tobias@dmfs.org>
 * 
 */
public class NotificationUpdaterService extends Service
{
	private static final String TAG = "NotificationUpdaterService";

	/** The duration in milliseconds of the heads up notification when pin tasks are due /start **/
	private static final int HEAD_UP_NOTIFICATION_DURATION = 5000;

	private static final int REQUEST_CODE_COMPLETE = 1;
	private static final int REQUEST_CODE_DELAY = 2;
	private static final int REQUEST_CODE_UNPIN = 3;

	// actions
	public static final String ACTION_PINNED_TASK_DUE = "org.dmfs.tasks.intent.ACTION_PINNED_TASK_DUE";
	public static final String ACTION_PINNED_TASK_START = "org.dmfs.tasks.intent.ACTION_PINNED_TASK_START";
	public static final String ACTION_NEXT_DAY = "org.dmfs.tasks.intent.ACTION_DAY_CHANGED";
	public static final String ACTION_PIN_TASK = "org.dmfs.tasks.intent.ACTION_PIN_TASK";
	public static final String ACTION_COMPLETE = "org.dmfs.tasks.intent.COMPLETE";
	public static final String ACTION_UNPIN = "org.dmfs.tasks.intent.UNPIN";
	public static final String ACTION_DELAY_1H = "org.dmfs.tasks.intent.DELAY_1H";
	public static final String ACTION_DELAY_1D = "org.dmfs.tasks.intent.DELAY_1D";
	public static final String ACTION_CANCEL_HEADUP_NOTIFICATION = "org.dmfs.tasks.intent.ACTION_CANCEL_HEADUP";

	// extras
	public static final String EXTRA_NEW_PINNED_TASK = "org.dmfs.intent.EXTRA_NEW_PINNED_TASK";
	public static final String EXTRA_NOTIFICATION_ID = "org.dmfs.tasks.extras.notification.NOTIFICATION_ID";
	public static final String EXTRA_TASK_DUE = "org.dmfs.tasks.extras.notification.TASK_DUE";
	public static final String EXTRA_TIMEZONE = "org.dmfs.tasks.extras.notification.TIMEZONE";
	public static final String EXTRA_ALLDAY = "org.dmfs.tasks.extras.notification.ALLDAY";

	private final NotificationCompat.Builder mBuilder = new Builder(this);
	private PendingIntent mDateChangePendingIntent;
	ArrayList<ContentSet> mTasksToPin;
	private String mAuthority;


	@Override
	public IBinder onBind(Intent intent)
	{
		// TODO Auto-generated method stub
		return null;
	}


	public NotificationUpdaterService()
	{
		super();
	}


	@Override
	public void onCreate()
	{
		mAuthority = TaskContract.taskAuthority(this);
		super.onCreate();
		updateNextDayAlarm();
	}


	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{

		String intentAction = intent.getAction();
		boolean silent = intent.getBooleanExtra(NotificationActionUtils.EXTRA_SILENT_NOTIFICATION, false);
		if (intentAction != null)
		{
			switch (intentAction)
			{
				case ACTION_PIN_TASK:
					// nothing special to do right now
					break;

				case ACTION_PINNED_TASK_START:
				case ACTION_PINNED_TASK_DUE:
					updateNotifications(true, !silent, !silent);
					if (!silent)
					{
						delayedCancelHeadsUpNotification();
					}
					break;

				case ACTION_COMPLETE:
					if (intent.hasExtra(NotificationActionUtils.EXTRA_NOTIFICATION_ACTION))
					{
						resolveUndoAction(intent);
						break;
					}
					resolveCompleteAction(intent);
					break;

				case ACTION_UNPIN:
					resolveUnpinAction(intent);
					break;

				case ACTION_DELAY_1D:
				case ACTION_DELAY_1H:
					resolveDelayAction(intent);
					break;

				case NotificationActionUtils.ACTION_UNDO:
				case NotificationActionUtils.ACTION_DESTRUCT:
				case NotificationActionUtils.ACTION_UNDO_TIMEOUT:
					resolveUndoAction(intent);
					break;

				case Intent.ACTION_BOOT_COMPLETED:
				case Intent.ACTION_REBOOT:
				case TaskNotificationHandler.ACTION_FASTBOOT:
					updateNotifications(true, false, false);
					break;

				case Intent.ACTION_DATE_CHANGED:
				case Intent.ACTION_TIME_CHANGED:
				case Intent.ACTION_TIMEZONE_CHANGED:
				case ACTION_NEXT_DAY:
					updateNextDayAlarm();
					updateNotifications(false, false, false);
					break;

				case ACTION_CANCEL_HEADUP_NOTIFICATION:
					updateNotifications(false, false, false);
					break;

				default:
					updateNotifications(false, false, false);
					break;
			}
		}

		// check if the service needs to kept alive
		if (mTasksToPin == null || mTasksToPin.isEmpty())
		{
			this.stopSelf();
		}
		return Service.START_NOT_STICKY;

	}


	@SuppressWarnings("unused")
	private void pinNewTask(Intent intent)
	{
		// check for new task to pin
		if (intent.hasExtra(EXTRA_NEW_PINNED_TASK))
		{
			ContentSet newTaskToPin = intent.getParcelableExtra(EXTRA_NEW_PINNED_TASK);
			makePinNotification(this, mBuilder, newTaskToPin, true, true, false);
		}
	}


	private void updateNotifications(boolean isReboot, boolean withSound, boolean withHeadsUpNotification)
	{
		// update pinned tasks
		mTasksToPin = queryTasksToPin();
		updatePinnedNotifications(mTasksToPin, isReboot, withSound, withHeadsUpNotification);
	}


	private void updatePinnedNotifications(ArrayList<ContentSet> tasksToPin, boolean isReboot, boolean withSound, boolean withHeadsUpNotification)
	{
		ArrayList<Uri> pinnedTaskUris = TaskNotificationHandler.getPinnedTaskUris(this);

		// show notifications
		NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
		for (ContentSet taskContentSet : tasksToPin)
		{
			boolean isAlreadyShown = pinnedTaskUris.contains(taskContentSet.getUri());
			Integer taskId = TaskFieldAdapters.TASK_ID.get(taskContentSet);
			notificationManager.notify(taskId,
				makePinNotification(this, mBuilder, taskContentSet, !isAlreadyShown || withSound, !isAlreadyShown || withSound, withHeadsUpNotification));
		}

		// remove old notifications
		if (!isReboot)
		{
			for (Uri uri : pinnedTaskUris)
			{
				if (uri == null || uri.toString().equals("null"))
				{
					break;
				}
				long taskId = ContentUris.parseId(uri);
				if (taskId > -1 == !containsTask(tasksToPin, uri))
				{

					Integer notificationId = Long.valueOf(ContentUris.parseId(uri)).intValue();
					if (notificationId != null)
					{
						notificationManager.cancel(notificationId);
					}
				}
			}
		}
		TaskNotificationHandler.savePinnedTasks(this, tasksToPin);
	}


	private boolean containsTask(ArrayList<ContentSet> tasks, Uri taskUri)
	{
		for (ContentSet contentSet : tasks)
		{
			if (taskUri.equals(contentSet.getUri()))
			{
				return true;
			}
		}
		return false;
	}


	@SuppressLint("InlinedApi")
	public void resendPinNotification(Uri taskUri)
	{
		if (taskUri == null)
		{
			return;
		}
		long taskId = ContentUris.parseId(taskUri);
		if (taskId < 0)
		{
			return;
		}
		Integer notificationId = Long.valueOf(taskId).intValue();
		mBuilder.setDefaults(Notification.DEFAULT_ALL);
		mBuilder.setOngoing(true);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
		{
			mBuilder.setPriority(Notification.PRIORITY_HIGH);
		}

		NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
		notificationManager.notify(notificationId, mBuilder.build());
	}


	private ArrayList<ContentSet> queryTasksToPin()
	{
		ArrayList<ContentSet> tasksToPin = new ArrayList<ContentSet>(20);

		final ContentResolver resolver = this.getContentResolver();
		final Uri contentUri = Tasks.getContentUri(TaskContract.taskAuthority(this));
		final Cursor cursor = resolver.query(contentUri, new String[] { Tasks._ID, Tasks.TITLE, Tasks.DESCRIPTION, Tasks.DTSTART, Tasks.DUE, Tasks.IS_ALLDAY,
			Tasks.STATUS, Tasks.PRIORITY }, Tasks.PINNED + "= 1", null, Tasks.PRIORITY + " is not null, " + Tasks.PRIORITY + ", " + Tasks.DUE + " is null, "
			+ Tasks.DUE + " DESC");
		try
		{
			if (cursor.moveToFirst())
			{
				do
				{
					Uri taskUri = ContentUris.withAppendedId(contentUri, TaskFieldAdapters.TASK_ID.get(cursor));
					ContentSet contentSet = new ContentSet(taskUri);
					contentSet.put(Tasks._ID, TaskFieldAdapters.TASK_ID.get(cursor));
					contentSet.put(Tasks.STATUS, TaskFieldAdapters.STATUS.get(cursor));
					contentSet.put(Tasks.TITLE, TaskFieldAdapters.TITLE.get(cursor));
					contentSet.put(Tasks.DESCRIPTION, TaskFieldAdapters.DESCRIPTION.get(cursor));
					contentSet.put(Tasks.DTSTART, cursor.getLong(cursor.getColumnIndex(Tasks.DTSTART)));
					contentSet.put(Tasks.DUE, cursor.getLong(cursor.getColumnIndex(Tasks.DUE)));
					contentSet.put(Tasks.IS_ALLDAY, cursor.getLong(cursor.getColumnIndex(Tasks.IS_ALLDAY)));
					contentSet.put(Tasks.PRIORITY, cursor.getLong(cursor.getColumnIndex(Tasks.PRIORITY)));
					tasksToPin.add(contentSet);

				} while (cursor.moveToNext());
			}
		}
		finally
		{
			cursor.close();
		}
		return tasksToPin;
	}


	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private static Notification makePinNotification(Context context, Builder builder, ContentSet task, boolean withSound, boolean withTickerText,
		boolean withHeadsUpNotification)
	{
		Resources resources = context.getResources();

		// reset actions
		builder.mActions = new ArrayList<Action>(2);

		// content
		builder.setSmallIcon(R.drawable.ic_pin_white_24dp).setContentTitle(TaskFieldAdapters.TITLE.get(task)).setOngoing(true).setShowWhen(false);

		// set priority for HeadsUpNotification
		if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN)
		{
			if (withHeadsUpNotification)
			{
				builder.setPriority(Notification.PRIORITY_HIGH);
			}
			else
			{
				builder.setPriority(Notification.PRIORITY_DEFAULT);
			}
		}

		// color is based on the priority of the task. If the task has no priority we use the primary color.
		Integer priority = TaskFieldAdapters.PRIORITY.get(task);
		if (priority != null && priority > 0)
		{
			if (priority < 5)
			{
				builder.setColor(resources.getColor(R.color.priority_red));
			}
			if (priority == 5)
			{
				builder.setColor(resources.getColor(R.color.priority_yellow));
			}
			if (priority > 5 && priority <= 9)
			{
				builder.setColor(resources.getColor(R.color.priority_green));
			}
		}
		else
		{
			builder.setColor(resources.getColor(R.color.primary));
		}

		// description
		String contentText = makePinNotificationContentText(context, task);
		if (contentText != null)
		{
			builder.setContentText(contentText);
		}

		// ticker text
		if (withTickerText)
		{
			builder.setTicker(context.getString(R.string.notification_task_pin_ticker, (TaskFieldAdapters.TITLE.get(task))));
		}

		// click action
		Intent resultIntent = new Intent(Intent.ACTION_VIEW);
		resultIntent.setData(task.getUri());
		resultIntent.setPackage(context.getPackageName());

		PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(resultPendingIntent);

		// complete action
		Boolean closed = TaskFieldAdapters.IS_CLOSED.get(task);
		if (closed == null || !closed)
		{
			Time dueTime = TaskFieldAdapters.DUE.get(task);
			long dueTimestamp = dueTime == null ? 0 : dueTime.toMillis(true);

			NotificationAction completeAction = new NotificationAction(NotificationUpdaterService.ACTION_COMPLETE, TaskFieldAdapters.TITLE.get(task),
				R.string.notification_action_completed, TaskFieldAdapters.TASK_ID.get(task), task.getUri(), dueTimestamp);
			builder.addAction(NotificationUpdaterService.getCompleteAction(context,
				NotificationActionUtils.getNotificationActionPendingIntent(context, completeAction)));
		}

		// unpin action
		builder.addAction(NotificationUpdaterService.getUnpinAction(context, TaskFieldAdapters.TASK_ID.get(task), task.getUri()));

		// sound
		if (withSound)
		{
			builder.setDefaults(Notification.DEFAULT_ALL);
		}
		else
		{
			builder.setDefaults(Notification.DEFAULT_LIGHTS);
		}

		return builder.build();
	}


	private static String makePinNotificationContentText(Context context, ContentSet task)
	{
		boolean isAllDay = TaskFieldAdapters.ALLDAY.get(task);
		Time now = new Time();
		now.setToNow();
		now.minute--;
		Time start = TaskFieldAdapters.DTSTART.get(task);
		Time due = TaskFieldAdapters.DUE.get(task);

		if (start != null && start.toMillis(true) > 0 && (now.before(start) || due == null))
		{
			start.allDay = isAllDay;
			String startString = context.getString(R.string.notification_task_start_date, NotificationActionUtils.formatTime(context, start));
			return startString;
		}

		if (due != null && due.toMillis(true) > 0)
		{
			due.allDay = isAllDay;
			String dueString = context.getString(R.string.notification_task_due_date, NotificationActionUtils.formatTime(context, due));
			return dueString;
		}

		String description = TaskFieldAdapters.DESCRIPTION.get(task);
		if (description != null)
		{
			description = description.replaceAll("\\[\\s?\\]", " ").replaceAll("\\[[xX]\\]", "✓");

		}
		return description;
	}


	@TargetApi(Build.VERSION_CODES.KITKAT)
	private void updateNextDayAlarm()
	{
		Intent intent = new Intent(this, NotificationUpdaterService.class);
		intent.setAction(ACTION_NEXT_DAY);
		intent.setPackage(getPackageName());
		mDateChangePendingIntent = PendingIntent.getService(this, 0, intent, 0);

		// tomorrow is today + 1 day
		DateTime tomorrow = DateTime.today().startOfDay().addDuration(new Duration(1, 1, 0));

		AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
		if (VERSION.SDK_INT >= VERSION_CODES.KITKAT)
		{
			alarmManager.setWindow(AlarmManager.RTC, tomorrow.swapTimeZone(TimeZone.getDefault()).getTimestamp(), 1000, mDateChangePendingIntent);
		}
		else
		{
			alarmManager.set(AlarmManager.RTC, tomorrow.swapTimeZone(TimeZone.getDefault()).getTimestamp(), mDateChangePendingIntent);
		}
	}


	private void processDesctructiveNotification(NotificationAction notificationAction)
	{
		if (ACTION_COMPLETE.equals(notificationAction.getActionType()))
		{
			markCompleted(notificationAction.getTaskUri());
		}

	}


	private void resendNotification(NotificationAction notificationAction)
	{
		if (ACTION_COMPLETE.equals(notificationAction.getActionType()))
		{
			// Start broadcast
			Intent startIntent = new Intent(TaskContract.ACTION_BROADCAST_TASK_STARTING);
			startIntent.setData(notificationAction.getTaskUri());
			startIntent.setPackage(getApplicationContext().getPackageName());
			startIntent.putExtra(TaskContract.EXTRA_TASK_TITLE, notificationAction.title());
			startIntent.putExtra(TaskContract.EXTRA_TASK_TIMESTAMP, notificationAction.getWhen());
			startIntent.putExtra(NotificationActionUtils.EXTRA_SILENT_NOTIFICATION, true);
			sendBroadcast(startIntent);

			// Due broadcast
			Intent dueIntent = new Intent(TaskContract.ACTION_BROADCAST_TASK_DUE);
			dueIntent.setData(notificationAction.getTaskUri());
			dueIntent.setPackage(getApplicationContext().getPackageName());
			dueIntent.putExtra(TaskContract.EXTRA_TASK_TITLE, notificationAction.title());
			dueIntent.putExtra(TaskContract.EXTRA_TASK_TIMESTAMP, notificationAction.getWhen());
			dueIntent.putExtra(NotificationActionUtils.EXTRA_SILENT_NOTIFICATION, true);
			sendBroadcast(dueIntent);
		}
	}


	private void cancelNotificationFromIntent(Intent intent)
	{
		if (!intent.hasExtra(EXTRA_NOTIFICATION_ID))
		{
			return;
		}
		int notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1);
		NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
		notificationManager.cancel(notificationId);
	}


	private void resolveCompleteAction(Intent intent)
	{
		cancelNotificationFromIntent(intent);
		Uri taskUri = intent.getData();
		markCompleted(taskUri);
	}


	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void resolveUndoAction(Intent intent)
	{
		if (!intent.hasExtra(NotificationActionUtils.EXTRA_NOTIFICATION_ACTION))
		{
			return;
		}

		/*
		 * Grab the alarm from the intent. Since the remote AlarmManagerService fills in the Intent to add some extra data, it must unparcel the
		 * NotificationAction object. It throws a ClassNotFoundException when unparcelling. To avoid this, do the marshalling ourselves.
		 */
		final NotificationAction notificationAction;
		final String action = intent.getAction();
		final byte[] data = intent.getByteArrayExtra(NotificationActionUtils.EXTRA_NOTIFICATION_ACTION);
		if (data != null)
		{
			final Parcel in = Parcel.obtain();
			in.unmarshall(data, 0, data.length);
			in.setDataPosition(0);
			notificationAction = NotificationAction.CREATOR.createFromParcel(in);
		}
		else
		{
			return;
		}

		if (NotificationActionUtils.ACTION_UNDO.equals(action))
		{
			NotificationActionUtils.cancelUndoTimeout(this, notificationAction);
			NotificationActionUtils.cancelUndoNotification(this, notificationAction);
			resendNotification(notificationAction);
		}
		else if (ACTION_COMPLETE.equals(action))
		{
			// All we need to do is switch to an Undo notification
			NotificationActionUtils.createUndoNotification(this, notificationAction);
			NotificationActionUtils.registerUndoTimeout(this, notificationAction);
		}
		else
		{
			if (NotificationActionUtils.ACTION_UNDO_TIMEOUT.equals(action) || NotificationActionUtils.ACTION_DESTRUCT.equals(action))
			{
				// Process the action
				NotificationActionUtils.cancelUndoTimeout(this, notificationAction);
				NotificationActionUtils.processUndoNotification(this, notificationAction);
				processDesctructiveNotification(notificationAction);
			}
		}
	}


	private void resolveDelayAction(Intent intent)
	{
		if (!(intent.hasExtra(EXTRA_TASK_DUE) && intent.hasExtra(EXTRA_TIMEZONE)))
		{
			return;
		}
		final String action = intent.getAction();
		final Uri taskUri = intent.getData();
		long due = intent.getLongExtra(EXTRA_TASK_DUE, -1);
		String tz = intent.getStringExtra(EXTRA_TIMEZONE);
		boolean allDay = intent.getBooleanExtra(EXTRA_ALLDAY, false);

		int notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1);

		NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
		notificationManager.cancel(notificationId);

		if (ACTION_DELAY_1H.equals(action))
		{
			Time time = new Time(tz);
			time.set(due);
			time.allDay = false;
			time.hour++;
			time.normalize(true);
			delayTask(taskUri, time);
		}
		else if (ACTION_DELAY_1D.equals(action))
		{
			if (tz == null)
			{
				tz = "UTC";
			}
			Time time = new Time(tz);
			time.set(due);
			time.allDay = allDay;
			time.monthDay++;
			time.normalize(true);
			delayTask(taskUri, time);
		}

	}


	private void resolveUnpinAction(Intent intent)
	{
		cancelNotificationFromIntent(intent);
		Uri taskUri = intent.getData();
		unpinTask(taskUri);
	}


	private void markCompleted(Uri taskUri)
	{
		ContentResolver contentResolver = getContentResolver();
		ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>(1);
		ContentProviderOperation.Builder operation = ContentProviderOperation.newUpdate(taskUri);
		operation.withValue(Tasks.STATUS, Tasks.STATUS_COMPLETED);
		operation.withValue(Tasks.PINNED, false);
		operations.add(operation.build());
		try
		{
			contentResolver.applyBatch(mAuthority, operations);
		}
		catch (RemoteException e)
		{
			Log.e(TAG, "Remote exception during complete task action");
			e.printStackTrace();
		}
		catch (OperationApplicationException e)
		{
			Log.e(TAG, "Unable to mark task completed: " + taskUri);
			e.printStackTrace();
		}
	}


	private void unpinTask(Uri taskUri)
	{
		ContentValues values = new ContentValues(1);
		TaskFieldAdapters.PINNED.set(values, false);
		getContentResolver().update(taskUri, values, null, null);
	}


	private void delayTask(Uri taskUri, Time dueTime)
	{
		ContentValues values = new ContentValues(4);
		TaskFieldAdapters.DUE.set(values, dueTime);
		getContentResolver().update(taskUri, values, null, null);
	}


	private void delayedCancelHeadsUpNotification()
	{
		Handler handler = new Handler(Looper.getMainLooper());
		final Runnable r = new Runnable()
		{
			public void run()
			{
				Intent intent = new Intent(getBaseContext(), NotificationUpdaterService.class);
				intent.setAction(ACTION_CANCEL_HEADUP_NOTIFICATION);
				startService(intent);
			}
		};
		handler.postDelayed(r, HEAD_UP_NOTIFICATION_DURATION);
	}


	public static Action getCompleteAction(Context context, PendingIntent intent)
	{
		return new Action(R.drawable.ic_action_complete, context.getString(R.string.notification_action_complete), intent);
	}


	public static Action getUnpinAction(Context context, PendingIntent intent)
	{
		return new Action(R.drawable.ic_action_complete, context.getString(R.string.notification_action_complete), intent);
	}


	public static Action getUnpinAction(Context context, int notificationId, Uri taskUri)
	{
		return new Action(R.drawable.ic_pin_off_white_24dp, context.getString(R.string.notification_action_unpin), getUnpinActionIntent(context,
			notificationId, taskUri));
	}


	public static Action getCompleteAction(Context context, int notificationId, Uri taskUri)
	{
		return new Action(R.drawable.ic_action_complete, context.getString(R.string.notification_action_complete), getCompleteActionIntent(context,
			notificationId, taskUri));
	}


	public static Action getDelay1hAction(Context context, int notificationId, Uri taskUri, long due, String timezone)
	{
		return new Action(R.drawable.ic_detail_delay_1h_inverse, context.getString(R.string.notification_action_delay_1h), getDelayActionIntent(context,
			notificationId, taskUri, due, true, timezone, false));
	}


	public static Action getDelay1dAction(Context context, int notificationId, Uri taskUri, long due, String timezone, boolean allday)
	{
		return new Action(R.drawable.ic_detail_delay_1d_inverse, context.getString(R.string.notification_action_delay_1d), getDelayActionIntent(context,
			notificationId, taskUri, due, false, timezone, allday));
	}


	public static PendingIntent getCompleteActionIntent(Context context, int notificationId, Uri taskUri)
	{
		final Intent intent = new Intent(context, NotificationUpdaterService.class);
		intent.setAction(NotificationUpdaterService.ACTION_COMPLETE);
		intent.setPackage(context.getPackageName());
		intent.setData(taskUri);
		intent.putExtra(EXTRA_NOTIFICATION_ID, notificationId);
		final PendingIntent pendingIntent = PendingIntent.getService(context, REQUEST_CODE_COMPLETE, intent, 0);
		return pendingIntent;
	}


	public static PendingIntent getUnpinActionIntent(Context context, int notificationId, Uri taskUri)
	{
		final Intent intent = new Intent(context, NotificationUpdaterService.class);
		intent.setAction(NotificationUpdaterService.ACTION_UNPIN);
		intent.setPackage(context.getPackageName());
		intent.setData(taskUri);
		intent.putExtra(EXTRA_NOTIFICATION_ID, notificationId);
		final PendingIntent pendingIntent = PendingIntent.getService(context, REQUEST_CODE_UNPIN, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		return pendingIntent;
	}


	private static PendingIntent getDelayActionIntent(Context context, int notificationId, Uri taskUri, long due, boolean delay1h, String timezone,
		boolean allday)
	{
		String action = null;
		if (delay1h)
		{
			action = ACTION_DELAY_1H;
		}
		else
		{
			action = ACTION_DELAY_1D;
		}
		final Intent intent = new Intent(context, NotificationUpdaterService.class);
		intent.setAction(action);
		intent.setPackage(context.getPackageName());
		intent.setData(taskUri);
		intent.putExtra(EXTRA_TASK_DUE, due);
		intent.putExtra(EXTRA_TIMEZONE, timezone);
		intent.putExtra(EXTRA_ALLDAY, allday);
		intent.putExtra(EXTRA_NOTIFICATION_ID, notificationId);
		final PendingIntent pendingIntent = PendingIntent.getService(context, REQUEST_CODE_DELAY, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		return pendingIntent;
	}

}