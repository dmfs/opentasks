/*
 * Copyright (C) 2014 Marten Gajda <marten@dmfs.org>
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

import java.util.HashMap;
import java.util.TimeZone;

import org.dmfs.tasks.R;
import org.dmfs.tasks.utils.DateFormatter;
import org.dmfs.tasks.utils.DateFormatter.DateFormatContext;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.widget.RemoteViews;


/**
 * Provides convenience methods for handling notification actions
 * 
 * @author Tobias Reinsch <tobias@dmfs.org>
 * 
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
public class NotificationActionUtils
{

	public final static String ACTION_UNDO = "org.dmfs.tasks.action.notification.UNDO";
	public final static String ACTION_DESTRUCT = "org.dmfs.tasks.action.notification.DESTRUCT";
	public final static String ACTION_UNDO_TIMEOUT = "org.dmfs.tasks.action.notification.ACTION_UNDO_TIMEOUT";

	public final static String EXTRA_NOTIFICATION_ACTION = "org.dmfs.tasks.extra.notification.EXTRA_NOTIFICATION_ACTION";

	/**
	 * Boolean extra to indicate that a notification is supposed to be silent, i.e. should not play a sound when it's fired.
	 */
	public final static String EXTRA_SILENT_NOTIFICATION = "org.dmfs.provider.tasks.extra.SILENT";

	private static long TIMEOUT_MILLIS = 10000;
	private static long sUndoTimeoutMillis = -1;

	/**
	 * If an undo notification is displayed, its timestamp ({@link android.app.Notification.Builder#setWhen(long)}) is stored here so we can use it for the
	 * original notification if the action is undone.
	 */
	public static final HashMap<Integer, Long> sNotificationTimestamps = new HashMap<Integer, Long>();


	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public static void sendDueAlarmNotification(Context context, String title, Uri taskUri, int notificationId, long dueDate, boolean dueAllDay,
		String timezone, boolean silent)
	{
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		String dueString = "";
		if (dueAllDay)
		{
			dueString = context.getString(R.string.notification_task_due_today);
		}
		else
		{
			dueString = context.getString(R.string.notification_task_due_date, formatTime(context, makeTime(dueDate, dueAllDay)));
		}

		// build notification
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context).setSmallIcon(R.drawable.ic_notification)
			.setContentTitle(context.getString(R.string.notification_task_due_title, title)).setContentText(dueString);

		// color
		mBuilder.setColor(context.getResources().getColor(R.color.primary));

		// dismisses the notification on click
		mBuilder.setAutoCancel(true);

		// set high priority to display heads-up notification
		if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN)
		{
			mBuilder.setPriority(Notification.PRIORITY_HIGH);
		}

		// set status bar test
		mBuilder.setTicker(title);

		// enable light, sound and vibration
		if (silent)
		{
			mBuilder.setDefaults(0);
		}
		else
		{
			mBuilder.setDefaults(Notification.DEFAULT_ALL);
		}

		// Creates an explicit intent for an Activity in your app
		Intent resultIntent = new Intent(Intent.ACTION_VIEW);
		resultIntent.setData(taskUri);

		// The stack builder object will contain an artificial back stack for the
		// started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

		// add actions
		if (android.os.Build.VERSION.SDK_INT >= VERSION_CODES.ICE_CREAM_SANDWICH)
		{
			// delay action
			mBuilder.addAction(NotificationUpdaterService.getDelay1dAction(context, notificationId, taskUri, dueDate, timezone, dueAllDay));

			// complete action
			NotificationAction completeAction = new NotificationAction(NotificationUpdaterService.ACTION_COMPLETE, title,
				R.string.notification_action_completed, notificationId, taskUri, dueDate);
			mBuilder.addAction(NotificationUpdaterService.getCompleteAction(context,
				NotificationActionUtils.getNotificationActionPendingIntent(context, completeAction)));
		}

		// set displayed time
		if (dueAllDay)
		{
			Time now = new Time();
			now.setToNow();
			now.set(0, 0, 0, now.monthDay, now.month, now.year);
			mBuilder.setWhen(now.toMillis(true));
		}
		else
		{
			mBuilder.setWhen(dueDate);
		}

		mBuilder.setContentIntent(resultPendingIntent);
		notificationManager.notify(notificationId, mBuilder.build());
	}


	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public static void sendStartNotification(Context context, String title, Uri taskUri, int notificationId, long startDate, boolean startAllDay,
		String timezone, boolean silent)
	{
		String startString = "";
		if (startAllDay)
		{
			startString = context.getString(R.string.notification_task_start_today);
		}
		else
		{
			startString = context.getString(R.string.notification_task_start_date, formatTime(context, makeTime(startDate, startAllDay)));
		}

		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		// build notification
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context).setSmallIcon(R.drawable.ic_notification)
			.setContentTitle(context.getString(R.string.notification_task_start_title, title)).setContentText(startString);

		// color
		mBuilder.setColor(context.getResources().getColor(R.color.primary));

		// dismisses the notification on click
		mBuilder.setAutoCancel(true);

		// set high priority to display heads-up notification
		if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN)
		{
			mBuilder.setPriority(Notification.PRIORITY_HIGH);
		}

		// set status bar test
		mBuilder.setTicker(title);

		// enable light, sound and vibration
		if (silent)
		{
			mBuilder.setDefaults(0);
		}
		else
		{
			mBuilder.setDefaults(Notification.DEFAULT_ALL);
		}

		// set notification time
		// set displayed time
		if (startAllDay)
		{
			Time now = new Time();
			now.setToNow();
			now.set(0, 0, 0, now.monthDay, now.month, now.year);
			mBuilder.setWhen(now.toMillis(true));
		}
		else
		{
			mBuilder.setWhen(startDate);
		}

		// add actions
		if (android.os.Build.VERSION.SDK_INT >= VERSION_CODES.ICE_CREAM_SANDWICH)
		{
			NotificationAction completeAction = new NotificationAction(NotificationUpdaterService.ACTION_COMPLETE, title,
				R.string.notification_action_completed, notificationId, taskUri, startDate);
			mBuilder.addAction(NotificationUpdaterService.getCompleteAction(context,
				NotificationActionUtils.getNotificationActionPendingIntent(context, completeAction)));

		}

		// Creates an explicit intent for an Activity in your app
		Intent resultIntent = new Intent(Intent.ACTION_VIEW);
		resultIntent.setData(taskUri);

		// The stack builder object will contain an artificial back stack for the
		// started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

		mBuilder.setContentIntent(resultPendingIntent);
		notificationManager.notify(notificationId, mBuilder.build());
	}


	/**
	 * Creates a {@link PendingIntent} for the specified notification action.
	 */
	public static PendingIntent getNotificationActionPendingIntent(Context context, NotificationAction action)
	{
		final Intent intent = new Intent(context, NotificationUpdaterService.class);
		intent.setAction(action.getActionType());
		intent.setData(action.getTaskUri());
		intent.setPackage(context.getPackageName());
		putNotificationActionExtra(intent, action);

		return PendingIntent.getService(context, action.getNotificationId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}


	/**
	 * Creates and displays an Undo notification for the specified {@link NotificationAction}.
	 */
	public static void createUndoNotification(final Context context, NotificationAction action)
	{
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		builder.setContentTitle(context.getString(action.getActionTextResId()

		));
		builder.setSmallIcon(R.drawable.ic_notification);
		builder.setWhen(action.getWhen());

		// disable sound & vibration
		builder.setDefaults(0);

		final RemoteViews undoView = new RemoteViews(context.getPackageName(), R.layout.undo_notification);
		undoView.setTextViewText(R.id.description_text, context.getString(action.mActionTextResId));

		final String packageName = context.getPackageName();

		final Intent clickIntent = new Intent(context, NotificationUpdaterService.class);
		clickIntent.setAction(ACTION_UNDO);
		clickIntent.setPackage(packageName);
		putNotificationActionExtra(clickIntent, action);
		final PendingIntent clickPendingIntent = PendingIntent.getService(context, action.getNotificationId(), clickIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		undoView.setOnClickPendingIntent(R.id.status_bar_latest_event_content, clickPendingIntent);
		builder.setContent(undoView);

		// When the notification is cleared, we perform the destructive action
		final Intent deleteIntent = new Intent(context, NotificationUpdaterService.class);
		deleteIntent.setAction(ACTION_DESTRUCT);
		deleteIntent.setPackage(packageName);
		putNotificationActionExtra(deleteIntent, action);
		final PendingIntent deletePendingIntent = PendingIntent
			.getService(context, action.getNotificationId(), deleteIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		builder.setDeleteIntent(deletePendingIntent);

		final Notification notification = builder.build();

		final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(action.getNotificationId(), notification);

		sNotificationTimestamps.put(action.getNotificationId(), action.mWhen);
	}


	/**
	 * Called when an Undo notification has been tapped.
	 */
	public static void cancelUndoNotification(final Context context, final NotificationAction notificationAction)
	{
		final int notificationId = notificationAction.getNotificationId();

		removeUndoNotification(context, notificationId);
	}


	/**
	 * Registers a timeout for the undo notification such that when it expires, the undo bar will disappear, and the action will be performed.
	 */
	public static void registerUndoTimeout(final Context context, final NotificationAction notificationAction)
	{

		if (sUndoTimeoutMillis == -1)
		{
			sUndoTimeoutMillis = TIMEOUT_MILLIS;
		}

		final AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		final long triggerAtMills = SystemClock.elapsedRealtime() + sUndoTimeoutMillis;
		final PendingIntent pendingIntent = createUndoTimeoutPendingIntent(context, notificationAction);
		alarmManager.set(AlarmManager.ELAPSED_REALTIME, triggerAtMills, pendingIntent);
	}


	/**
	 * Cancels the undo timeout for a notification action. This should be called if the undo notification is clicked (to prevent the action from being performed
	 * anyway) or cleared (since we have already performed the action).
	 */
	public static void cancelUndoTimeout(final Context context, final NotificationAction notificationAction)
	{
		final AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		final PendingIntent pendingIntent = createUndoTimeoutPendingIntent(context, notificationAction);
		alarmManager.cancel(pendingIntent);
	}


	/**
	 * Creates a {@link PendingIntent} to be used for creating and canceling the undo timeout alarm.
	 */
	private static PendingIntent createUndoTimeoutPendingIntent(final Context context, final NotificationAction notificationAction)
	{
		final Intent intent = new Intent(context, NotificationUpdaterService.class);
		intent.setAction(ACTION_UNDO_TIMEOUT);
		intent.setPackage(context.getPackageName());
		putNotificationActionExtra(intent, notificationAction);
		final int requestCode = notificationAction.mNotificationId;
		final PendingIntent pendingIntent = PendingIntent.getService(context, requestCode, intent, 0);
		return pendingIntent;
	}


	/**
	 * Removes the undo notification.
	 */
	private static void removeUndoNotification(final Context context, final int notificationId)
	{
		final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(notificationId);
	}


	/**
	 * If an undo notification is left alone for a long enough time, it will disappear, this method will be called, and the action will be finalized.
	 */
	public static void processUndoNotification(final Context context, final NotificationAction notificationAction)
	{
		removeUndoNotification(context, notificationAction.getNotificationId());
		sNotificationTimestamps.remove(notificationAction.getNotificationId());
	}


	/**
	 * <p>
	 * This is a slight hack to avoid an exception in the remote AlarmManagerService process. The AlarmManager adds extra data to this Intent which causes it to
	 * inflate. Since the remote process does not know about the NotificationAction class, it throws a ClassNotFoundException.
	 * </p>
	 * <p>
	 * To avoid this, we marshall the data ourselves and then parcel a plain byte[] array. The NotificationActionIntentService class knows to build the
	 * NotificationAction object from the byte[] array.
	 * </p>
	 */
	private static void putNotificationActionExtra(final Intent intent, final NotificationAction notificationAction)
	{
		final Parcel out = Parcel.obtain();
		notificationAction.writeToParcel(out, 0);
		out.setDataPosition(0);
		intent.putExtra(EXTRA_NOTIFICATION_ACTION, out.marshall());
	}


	private static Time makeTime(long timestamp, boolean allday)
	{
		Time result = new Time(allday ? Time.TIMEZONE_UTC : TimeZone.getDefault().getID());
		result.set(timestamp);
		result.allDay = allday;
		return result;
	}


	/**
	 * Returns a string representation for the time, with a relative date and an absolute time
	 */
	public static String formatTime(Context context, Time time)
	{
		Time now = new Time();
		now.setToNow();
		String dateString;
		if (time.allDay)
		{
			Time allDayNow = new Time("UTC");
			allDayNow.set(now.monthDay, now.month, now.year);
			dateString = DateUtils.getRelativeTimeSpanString(time.toMillis(false), allDayNow.toMillis(false), DateUtils.DAY_IN_MILLIS).toString();
		}
		else
		{
			dateString = DateUtils.getRelativeTimeSpanString(time.toMillis(false), now.toMillis(false), DateUtils.DAY_IN_MILLIS).toString();
		}

		// return combined date and time
		String timeString = new DateFormatter(context).format(time, DateFormatContext.NOTIFICATION_VIEW_TIME);
		return new StringBuilder().append(dateString).append(", ").append(timeString).toString();
	}

	public static class NotificationAction implements Parcelable
	{
		private String mActionType;
		private final int mActionTextResId;
		private final int mNotificationId;
		private final Uri mTaskUri;
		private final long mWhen;
		private final String mTitle;


		public NotificationAction(String actionType, String taskTitle, int actionTextResId, int notificationId, Uri taskUri, long when)
		{
			mActionType = actionType;
			mTitle = taskTitle;
			mActionTextResId = actionTextResId;
			mNotificationId = notificationId;
			mTaskUri = taskUri;
			mWhen = when;
		}


		@Override
		public int describeContents()
		{
			return 0;
		}


		@Override
		public void writeToParcel(final Parcel out, final int flags)
		{
			out.writeString(mActionType);
			out.writeInt(mActionTextResId);
			out.writeInt(mNotificationId);
			out.writeString(mTaskUri.toString());
			out.writeLong(mWhen);
			out.writeString(mTitle);
		}

		public static final Parcelable.Creator<NotificationAction> CREATOR = new Parcelable.Creator<NotificationAction>()
		{
			@Override
			public NotificationAction createFromParcel(final Parcel in)
			{
				return new NotificationAction(in, null);
			}


			@Override
			public NotificationAction[] newArray(final int size)
			{
				return new NotificationAction[size];
			}
		};


		private NotificationAction(final Parcel in, final ClassLoader loader)
		{
			mActionType = in.readString();
			mActionTextResId = in.readInt();
			mNotificationId = in.readInt();
			mTaskUri = Uri.parse(in.readString());
			mWhen = in.readLong();
			mTitle = in.readString();
		}


		public String getActionType()
		{
			return mActionType;
		}


		public int getActionTextResId()
		{
			return mActionTextResId;
		}


		public int getNotificationId()
		{
			return mNotificationId;
		}


		public Uri getTaskUri()
		{
			return mTaskUri;
		}


		public String title()
		{
			return mTitle;
		}


		public long getWhen()
		{
			return mWhen;
		}

	}
}