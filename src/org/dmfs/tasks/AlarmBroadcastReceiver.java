package org.dmfs.tasks;

import java.net.URI;

import org.dmfs.provider.tasks.TaskContract.Tasks;
import org.dmfs.provider.tasks.handler.AlarmNotificationHandler;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;


/**
 * A {@link BroadcastReceiver} to handle incoming alarms for tasks.
 * 
 * @author Tobias Reinsch <tobias@dmfs.org>
 * 
 */
public class AlarmBroadcastReceiver extends BroadcastReceiver
{

	/**
	 * Is called on an incoming alarm broadcast. Creates a notifications for this alarm.
	 * 
	 */
	@Override
	public void onReceive(Context context, Intent intent)
	{
		long taskId = intent.getLongExtra(AlarmNotificationHandler.EXTRA_TASK_ID, 0);
		// long dueTime = intent.getLongExtra(AlarmNotificationHandler.EXTRA_TASK_DUE_TIME, System.currentTimeMillis());
		String title = intent.getStringExtra(AlarmNotificationHandler.EXTRA_TASK_TITLE);

		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		// build notification
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context).setSmallIcon(R.drawable.ic_status_completed)
			.setContentTitle(context.getString(R.string.notification_task_due_title)).setContentText(title);

		// dismisses the notification on click
		mBuilder.setAutoCancel(true);

		// set status bar test
		mBuilder.setTicker(title);

		// Creates an explicit intent for an Activity in your app
		Intent resultIntent = new Intent(context, ViewTaskActivity.class);
		resultIntent.setData(getUriForTask(taskId));

		// The stack builder object will contain an artificial back stack for the
		// started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(ViewTaskActivity.class);
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

		mBuilder.setContentIntent(resultPendingIntent);
		notificationManager.notify(0, mBuilder.build());

	}


	/**
	 * Helper that constructs a task {@link URI} for a given task id.
	 * 
	 * @param taskId
	 *            The task row id.
	 * @return The task {@link URI}.
	 */
	private Uri getUriForTask(long taskId)
	{
		return Uri.withAppendedPath(Tasks.CONTENT_URI, "/" + taskId);
	}
}
