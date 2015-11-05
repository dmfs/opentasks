package org.dmfs.tasks.utils;

import org.dmfs.provider.tasks.TaskContract;
import org.dmfs.provider.tasks.TaskContract.TaskLists;
import org.dmfs.tasks.R;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;


public class DatabaseInitializedReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		if (context.getResources().getBoolean(R.bool.opentasks_support_local_lists))
		{
			// The database was just created, insert a local task list
			ContentValues listValues = new ContentValues(5);
			listValues.put(TaskLists.LIST_NAME, context.getString(R.string.initial_local_task_list_name));
			listValues.put(TaskLists.LIST_COLOR, Color.rgb(30, 136, 229) /* material blue 600 */);
			listValues.put(TaskLists.VISIBLE, 1);
			listValues.put(TaskLists.SYNC_ENABLED, 1);
			listValues.put(TaskLists.OWNER, "");

			context.getContentResolver().insert(
				TaskContract.TaskLists.getContentUri(TaskContract.taskAuthority(context)).buildUpon()
					.appendQueryParameter(TaskContract.CALLER_IS_SYNCADAPTER, "true")
					.appendQueryParameter(TaskContract.ACCOUNT_NAME, TaskContract.LOCAL_ACCOUNT_NAME)
					.appendQueryParameter(TaskContract.ACCOUNT_TYPE, TaskContract.LOCAL_ACCOUNT_TYPE).build(), listValues);
		}
	}
}
