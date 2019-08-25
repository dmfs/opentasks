/*
 * Copyright 2017 dmfs GmbH
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
 */

package org.dmfs.provider.tasks;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import org.dmfs.provider.tasks.model.CursorContentValuesInstanceAdapter;
import org.dmfs.provider.tasks.model.CursorContentValuesTaskAdapter;
import org.dmfs.provider.tasks.model.InstanceAdapter;
import org.dmfs.provider.tasks.model.TaskAdapter;
import org.dmfs.provider.tasks.processors.tasks.Instantiating;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.tasks.contract.TaskContract;
import org.dmfs.tasks.contract.TaskContract.Instances;
import org.dmfs.tasks.contract.TaskContract.Tasks;

import java.util.TimeZone;


public enum ContentOperation
{
    /**
     * When the local timezone has been changed we need to update the due and start sorting values. This handler will take care of running the appropriate
     * update. In addition it fires an operation to update all notifications.
     */
    UPDATE_TIMEZONE(new OperationHandler()
    {
        @Override
        public void handleOperation(Context context, Uri uri, SQLiteDatabase db, ContentValues values)
        {
            long start = System.currentTimeMillis();

            // request an update of all instance values
            ContentValues vals = new ContentValues(1);
            Instantiating.addUpdateRequest(vals);

            // execute update that triggers a recalculation of all due and start sorting values
            int count = context.getContentResolver().update(
                    TaskContract.Tasks.getContentUri(uri.getAuthority()).buildUpon().appendQueryParameter(TaskContract.CALLER_IS_SYNCADAPTER, "true").build(),
                    vals, null, null);

            Log.i("TaskProvider", "time to update " + count + " tasks: " + (System.currentTimeMillis() - start) + " ms");

            // now update alarms as well
            UPDATE_NOTIFICATION_ALARM.fire(context, null);
        }
    }),

    /**
     * Takes care of everything we need to send task start and task due broadcasts.
     */
    POST_NOTIFICATIONS(new OperationHandler()
    {

        @Override
        public void handleOperation(Context context, Uri uri, SQLiteDatabase db, ContentValues values)
        {
            TimeZone localTimeZone = TimeZone.getDefault();

            // the date-time of when the last notification was shown
            DateTime lastAlarm = getLastAlarmTimestamp(context);
            // the current time, we show all notifications between <set> and now
            DateTime now = DateTime.nowAndHere();

            String lastAlarmString = Long.toString(lastAlarm.getInstance());
            String nowString = Long.toString(now.getInstance());

            // load all tasks that have started or became due since the last time we've shown a notification.
            Cursor instancesCursor = db.query(TaskDatabaseHelper.Tables.INSTANCE_VIEW, null, "((" + TaskContract.Instances.INSTANCE_DUE_SORTING + ">? and "
                    + TaskContract.Instances.INSTANCE_DUE_SORTING + "<=?) or (" + TaskContract.Instances.INSTANCE_START_SORTING + ">? and "
                    + TaskContract.Instances.INSTANCE_START_SORTING + "<=?)) and " + Instances.IS_CLOSED + " = 0 and " + Tasks._DELETED + "=0", new String[] {
                    lastAlarmString, nowString, lastAlarmString, nowString }, null, null, null);

            try
            {
                while (instancesCursor.moveToNext())
                {
                    InstanceAdapter task = new CursorContentValuesInstanceAdapter(InstanceAdapter._ID.getFrom(instancesCursor), instancesCursor, null);

                    DateTime instanceDue = task.valueOf(InstanceAdapter.INSTANCE_DUE);
                    if (instanceDue != null && !instanceDue.isFloating())
                    {
                        // make sure we compare instances in local time
                        instanceDue = instanceDue.shiftTimeZone(localTimeZone);
                    }

                    DateTime instanceStart = task.valueOf(InstanceAdapter.INSTANCE_START);
                    if (instanceStart != null && !instanceStart.isFloating())
                    {
                        // make sure we compare instances in local time
                        instanceStart = instanceStart.shiftTimeZone(localTimeZone);
                    }

                    if (instanceDue != null && lastAlarm.getInstance() < instanceDue.getInstance() && instanceDue.getInstance() <= now.getInstance())
                    {
                        // this task became due since the last alarm, send a due broadcast
                        sendBroadcast(context, TaskContract.ACTION_BROADCAST_TASK_DUE, task.uri(uri.getAuthority()));
                    }
                    else if (instanceStart != null && lastAlarm.getInstance() < instanceStart.getInstance() && instanceStart.getInstance() <= now.getInstance())
                    {
                        // this task has started since the last alarm, send a start broadcast
                        sendBroadcast(context, TaskContract.ACTION_BROADCAST_TASK_STARTING, task.uri(uri.getAuthority()));
                    }
                }
            }
            finally
            {
                instancesCursor.close();
            }

            // all notifications up to now have been triggered
            saveLastAlarmTime(context, now);

            // set the alarm for the next notification
            UPDATE_NOTIFICATION_ALARM.fire(context, null);
        }


        @SuppressLint("NewApi")
        private void saveLastAlarmTime(Context context, DateTime time)
        {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            Editor editor = prefs.edit();
            editor.putLong(PREFS_KEY_LAST_ALARM_TIMESTAMP, time.getTimestamp());
            editor.apply();
        }


        private DateTime getLastAlarmTimestamp(Context context)
        {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            return new DateTime(TimeZone.getDefault(), prefs.getLong(PREFS_KEY_LAST_ALARM_TIMESTAMP, System.currentTimeMillis()));
        }


        /**
         * Sends a notification broadcast for a task instance that has started or became due.
         *
         * @param context
         *         A {@link Context}.
         * @param action
         *         The broadcast action.
         * @param uri
         *         The task uri.
         */
        private void sendBroadcast(Context context, String action, Uri uri)
        {
            Intent intent = new Intent(action);
            intent.setData(uri);
            // only notify our own package
            intent.setPackage(context.getPackageName());
            context.sendBroadcast(intent);
        }
    }),

    /**
     * Determines the date-time of when the next task becomes due or starts (whatever happens first) and sets an alarm to trigger a notification.
     */
    UPDATE_NOTIFICATION_ALARM(new OperationHandler()
    {

        @Override
        public void handleOperation(Context context, Uri uri, SQLiteDatabase db, ContentValues values)
        {
            TimeZone localTimeZone = TimeZone.getDefault();
            DateTime lastAlarm = getLastAlarmTimestamp(context);
            DateTime now = DateTime.nowAndHere();

            if (now.before(lastAlarm))
            {
                // time went backwards, set last alarm time to now
                lastAlarm = now;
                saveLastAlarmTime(context, now);
            }

            String lastAlarmString = Long.toString(lastAlarm.getInstance());

            DateTime nextAlarm = null;

            // find the next task that starts
            Cursor nextInstanceStartCursor = db.query(TaskDatabaseHelper.Tables.INSTANCE_VIEW, null, TaskContract.Instances.INSTANCE_START_SORTING + ">? and "
                            + Instances.IS_CLOSED + " = 0 and " + Tasks._DELETED + "=0", new String[] { lastAlarmString }, null, null,
                    TaskContract.Instances.INSTANCE_START_SORTING, "1");

            try
            {
                if (nextInstanceStartCursor.moveToNext())
                {
                    TaskAdapter task = new CursorContentValuesTaskAdapter(TaskAdapter.INSTANCE_TASK_ID.getFrom(nextInstanceStartCursor),
                            nextInstanceStartCursor, null);
                    nextAlarm = task.valueOf(TaskAdapter.INSTANCE_START);
                    if (!nextAlarm.isFloating())
                    {
                        nextAlarm = nextAlarm.shiftTimeZone(localTimeZone);
                    }
                }
            }
            finally
            {
                nextInstanceStartCursor.close();
            }

            // find the next task that's due
            Cursor nextInstanceDueCursor = db.query(TaskDatabaseHelper.Tables.INSTANCE_VIEW, null, TaskContract.Instances.INSTANCE_DUE_SORTING + ">? and "
                            + Instances.IS_CLOSED + " = 0 and " + Tasks._DELETED + "=0", new String[] { lastAlarmString }, null, null,
                    TaskContract.Instances.INSTANCE_DUE_SORTING, "1");

            try
            {
                if (nextInstanceDueCursor.moveToNext())
                {
                    TaskAdapter task = new CursorContentValuesTaskAdapter(TaskAdapter.INSTANCE_TASK_ID.getFrom(nextInstanceDueCursor), nextInstanceDueCursor,
                            null);
                    DateTime nextDue = task.valueOf(TaskAdapter.INSTANCE_DUE);
                    if (!nextDue.isFloating())
                    {
                        nextDue = nextDue.shiftTimeZone(localTimeZone);
                    }

                    if (nextAlarm == null || nextAlarm.getInstance() > nextDue.getInstance())
                    {
                        nextAlarm = nextDue;
                    }
                }
            }
            finally
            {
                nextInstanceDueCursor.close();
            }

            if (nextAlarm != null)
            {
                TaskProviderBroadcastReceiver.planNotificationUpdate(context, nextAlarm);
            }
            else
            {
                saveLastAlarmTime(context, now);
            }
        }


        @SuppressLint("NewApi")
        private void saveLastAlarmTime(Context context, DateTime time)
        {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            Editor editor = prefs.edit();
            editor.putLong(PREFS_KEY_LAST_ALARM_TIMESTAMP, time.getTimestamp());
            editor.apply();
        }


        private DateTime getLastAlarmTimestamp(Context context)
        {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            return new DateTime(TimeZone.getDefault(), prefs.getLong(PREFS_KEY_LAST_ALARM_TIMESTAMP, System.currentTimeMillis()));
        }

    });

    /**
     * A lock object to serialize the execution of all incoming {@link ContentOperation}.
     */
    private final static Object mLock = new Object();

    /**
     * The base path of the Uri to trigger content operations.
     */
    private final static String BASE_PATH = "content_operation";

    /**
     * The {@link OperationHandler} that handles this {@link ContentOperation}.
     */
    private final OperationHandler mHandler;

    private static final String PREFS_NAME = "org.dmfs.provider.tasks";
    private static final String PREFS_KEY_LAST_ALARM_TIMESTAMP = "org.dmfs.provider.tasks.prefs.LAST_ALARM_TIMESTAMP";


    ContentOperation(OperationHandler handler)
    {
        mHandler = handler;
    }


    /**
     * Execute this {@link ContentOperation} with the given values.
     *
     * @param context
     *         A {@link Context}.
     * @param values
     *         Optional {@link ContentValues}, may be <code>null</code>.
     */
    public void fire(Context context, ContentValues values)
    {
        context.getContentResolver().update(uri(AuthorityUtil.taskAuthority(context)), values == null ? new ContentValues() : values, null, null);
    }


    /**
     * Run the operation on the given handler.
     *
     * @param context
     *         A {@link Context}.
     * @param handler
     *         A {@link Handler} to run the operation on.
     * @param uri
     *         The {@link Uri} that triggered this operation.
     * @param db
     *         The database.
     * @param values
     *         The {@link ContentValues} that were supplied.
     */
    void run(final Context context, Handler handler, final Uri uri, final SQLiteDatabase db, final ContentValues values)
    {
        handler.post(new Runnable()
        {
            @Override
            public void run()
            {
                synchronized (mLock)
                {
                    mHandler.handleOperation(context, uri, db, values);
                }
            }
        });
    }


    /**
     * Returns the {@link Uri} that triggers this {@link ContentOperation}.
     *
     * @param authority
     *         The authority of this provide.
     *
     * @return A {@link Uri}.
     */
    private Uri uri(String authority)
    {
        return new Uri.Builder().scheme("content").authority(authority).path(BASE_PATH).appendPath(this.toString()).build();
    }


    /**
     * Register the operations with the given {@link UriMatcher}.
     *
     * @param uriMatcher
     *         The {@link UriMatcher}.
     * @param authority
     *         The authority of this TaskProvider.
     * @param firstID
     *         Teh first Id to use for our Uris.
     */
    public static void register(UriMatcher uriMatcher, String authority, int firstID)
    {
        for (ContentOperation op : values())
        {
            Uri uri = op.uri(authority);
            uriMatcher.addURI(authority, uri.getPath().substring(1) /* remove leading slash */, firstID + op.ordinal());
        }
    }


    /**
     * Return a {@link ContentOperation} that belongs to the given id.
     *
     * @param id
     *         The id or the {@link ContentOperation}.
     * @param firstId
     *         The first ID to use for Uris.
     *
     * @return The respective {@link ContentOperation} or <code>null</code> if none was found.
     */
    public static ContentOperation get(int id, int firstId)
    {
        if (id < firstId)
        {
            return null;
        }

        if (id - firstId >= values().length)
        {
            return null;
        }

        return values()[id - firstId];
    }


    public interface OperationHandler
    {
        void handleOperation(Context context, Uri uri, SQLiteDatabase db, ContentValues values);
    }

}
