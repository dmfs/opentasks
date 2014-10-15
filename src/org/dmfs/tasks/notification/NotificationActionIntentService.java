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

import java.util.ArrayList;

import org.dmfs.provider.tasks.TaskContract.Tasks;
import org.dmfs.provider.tasks.broadcast.DueAlarmBroadcastHandler;
import org.dmfs.tasks.R;
import org.dmfs.tasks.notification.NotificationActionUtils.NotificationAction;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.Build;
import android.os.Parcel;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat.Action;
import android.support.v4.app.NotificationManagerCompat;
import android.text.format.Time;
import android.util.Log;


/**
 * Processes notification action {@link Intent}s that need to run off the main thread.
 * 
 * @author Tobias Reinsch <tobias@dmfs.org>
 * 
 */
public class NotificationActionIntentService extends IntentService
{
	private static final String TAG = "NotificationActionIntentService";
	private static final int REQUEST_CODE_COMPLETE = 1;
	private static final int REQUEST_CODE_DELAY = 2;

	// actions
	public static final String ACTION_COMPLETE = "org.dmfs.tasks.action.notification.COMPLETE";
	public static final String ACTION_DELAY_1H = "org.dmfs.tasks.action.notification.DELAY_1H";
	public static final String ACTION_DELAY_1D = "org.dmfs.tasks.action.notification.DELAY_1D";

	// extras
	public static final String EXTRA_NOTIFICATION_ID = "org.dmfs.tasks.extras.notification.NOTIFICATION_ID";
	public static final String EXTRA_TASK_ID = "org.dmfs.tasks.extras.notification.TASK_ID";
	public static final String EXTRA_TASK_DUE = "org.dmfs.tasks.extras.notification.TASK_DUE";
	public static final String EXTRA_TIMEZONE = "org.dmfs.tasks.extras.notification.TIMEZONE";

	private String mAuthority;
	private Uri mTasksUri;


	public NotificationActionIntentService()
	{
		super("NotificationActionIntentService");

	}


	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	@Override
	protected void onHandleIntent(Intent intent)
	{
		mAuthority = getString(R.string.org_dmfs_tasks_authority);
		mTasksUri = Tasks.getContentUri(mAuthority);

		final String action = intent.getAction();
		final Context context = this;

		if (intent.hasExtra(EXTRA_TASK_ID) && intent.hasExtra(EXTRA_NOTIFICATION_ID))
		{
			long taskId = intent.getLongExtra(EXTRA_TASK_ID, -1);
			int notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1);

			NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
			notificationManager.cancel(notificationId);

			if (ACTION_COMPLETE.equals(action))
			{
				markCompleted(taskId);

			}
			else if (intent.hasExtra(EXTRA_TASK_DUE) && intent.hasExtra(EXTRA_TIMEZONE))
			{
				long due = intent.getLongExtra(EXTRA_TASK_DUE, -1);
				String tz = intent.getStringExtra(EXTRA_TIMEZONE);
				if (ACTION_DELAY_1H.equals(action))
				{
					Time time = new Time(tz);
					time.set(due);
					time.hour++;
					time.normalize(true);
					delayTask(taskId, time.toMillis(true), tz);
				}
				else if (ACTION_DELAY_1D.equals(action))
				{
					Time time = new Time(tz);
					time.set(due);
					time.monthDay++;
					time.normalize(true);
					delayTask(taskId, time.toMillis(true), tz);
				}
			}

		}
		else if (intent.hasExtra(NotificationActionUtils.EXTRA_NOTIFICATION_ACTION))
		{

			/*
			 * Grab the alarm from the intent. Since the remote AlarmManagerService fills in the Intent to add some extra data, it must unparcel the
			 * NotificationAction object. It throws a ClassNotFoundException when unparcelling. To avoid this, do the marshalling ourselves.
			 */
			final NotificationAction notificationAction;
			final byte[] data = intent.getByteArrayExtra(NotificationActionUtils.EXTRA_NOTIFICATION_ACTION);
			if (data != null)
			{
				final Parcel in = Parcel.obtain();
				in.unmarshall(data, 0, data.length);
				in.setDataPosition(0);
				notificationAction = NotificationAction.CREATOR.createFromParcel(in, NotificationAction.class.getClassLoader());
			}
			else
			{
				return;
			}

			if (NotificationActionUtils.ACTION_UNDO.equals(action))
			{
				NotificationActionUtils.cancelUndoTimeout(context, notificationAction);
				NotificationActionUtils.cancelUndoNotification(context, notificationAction);
				resendNotification(notificationAction);
			}
			else if (ACTION_COMPLETE.equals(action))
			{
				// All we need to do is switch to an Undo notification
				NotificationActionUtils.createUndoNotification(context, notificationAction);
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

	}


	private void processDesctructiveNotification(NotificationAction notificationAction)
	{
		if (ACTION_COMPLETE.equals(notificationAction.getActionType()))
		{
			markCompleted(notificationAction.getTaskId());
		}

	}


	private void resendNotification(NotificationAction notificationAction)
	{
		if (ACTION_COMPLETE.equals(notificationAction.getActionType()))
		{
			Intent intent = new Intent(DueAlarmBroadcastHandler.BROADCAST_DUE_ALARM);
			// intent.setPackage(getApplicationContext().getPackageName());
			intent.putExtra(DueAlarmBroadcastHandler.EXTRA_TASK_DUE_TIME, notificationAction.getWhen());
			sendBroadcast(intent);
		}
	}


	private void markCompleted(long taskId)
	{
		ContentResolver contentResolver = getContentResolver();
		ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>(1);
		ContentProviderOperation.Builder operation = ContentProviderOperation.newUpdate(ContentUris.withAppendedId(mTasksUri, taskId));
		operation.withValue(Tasks.STATUS, Tasks.STATUS_COMPLETED);
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
			Log.e(TAG, "Unable to mark task completed: " + taskId);
			e.printStackTrace();
		}
	}


	private void delayTask(long taskId, long due, String timezone)
	{
		ContentResolver contentResolver = getContentResolver();
		ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>(1);
		ContentProviderOperation.Builder operation = ContentProviderOperation.newUpdate(ContentUris.withAppendedId(mTasksUri, taskId));
		operation.withValue(Tasks.DUE, due);
		operation.withValue(Tasks.TZ, timezone);
		operations.add(operation.build());
		try
		{
			contentResolver.applyBatch(mAuthority, operations);
		}
		catch (RemoteException e)
		{
			Log.e(TAG, "Remote exception during delay task action");
			e.printStackTrace();
		}
		catch (OperationApplicationException e)
		{
			Log.e(TAG, "Unable to delay task: " + taskId);
			e.printStackTrace();
		}
	}


	public static Action getCompleteAction(Context context, PendingIntent intent)
	{
		return new Action(R.drawable.no_image, context.getString(R.string.notification_action_complete), intent);
	}


	public static Action getCompleteAction(Context context, int notificationId, long taskId)
	{
		return new Action(R.drawable.no_image, context.getString(R.string.notification_action_complete), getCompleteActionIntent(context, notificationId,
			taskId));
	}


	public static Action getDelay1hAction(Context context, int notificationId, long taskId, long due, String timezone)
	{
		return new Action(R.drawable.no_image, context.getString(R.string.notification_action_delay_1h), getDelayActionIntent(context, notificationId, taskId,
			due, true, timezone));
	}


	public static Action getDelay1dAction(Context context, int notificationId, long taskId, long due, String timezone)
	{
		return new Action(R.drawable.no_image, context.getString(R.string.notification_action_delay_1d), getDelayActionIntent(context, notificationId, taskId,
			due, false, timezone));
	}


	private static PendingIntent getCompleteActionIntent(Context context, int notificationId, long taskId)
	{
		final Intent intent = new Intent(NotificationActionIntentService.ACTION_COMPLETE);
		intent.setPackage(context.getPackageName());
		intent.putExtra(EXTRA_TASK_ID, taskId);
		intent.putExtra(EXTRA_NOTIFICATION_ID, notificationId);
		final PendingIntent pendingIntent = PendingIntent.getService(context, REQUEST_CODE_COMPLETE, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		return pendingIntent;
	}


	private static PendingIntent getDelayActionIntent(Context context, int notificationId, long taskId, long due, boolean delay1h, String timezone)
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
		final Intent intent = new Intent(action);
		intent.setPackage(context.getPackageName());
		intent.putExtra(EXTRA_TASK_ID, taskId);
		intent.putExtra(EXTRA_TASK_DUE, due);
		intent.putExtra(EXTRA_TIMEZONE, timezone);
		intent.putExtra(EXTRA_NOTIFICATION_ID, notificationId);
		final PendingIntent pendingIntent = PendingIntent.getService(context, REQUEST_CODE_DELAY, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		return pendingIntent;
	}
}
