/*
 * Copyright 2019 dmfs GmbH
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

package org.dmfs.tasks.notification;

import android.content.ContentProviderClient;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;

import org.dmfs.android.contentpal.RowSnapshot;
import org.dmfs.android.contentpal.predicates.EqArg;
import org.dmfs.android.contentpal.rowsets.QueryRowSet;
import org.dmfs.opentaskspal.readdata.EffectiveDueDate;
import org.dmfs.opentaskspal.readdata.EffectiveTaskColor;
import org.dmfs.opentaskspal.readdata.Id;
import org.dmfs.opentaskspal.readdata.TaskIsClosed;
import org.dmfs.opentaskspal.readdata.TaskPin;
import org.dmfs.opentaskspal.readdata.TaskStart;
import org.dmfs.opentaskspal.readdata.TaskTitle;
import org.dmfs.opentaskspal.readdata.TaskVersion;
import org.dmfs.opentaskspal.views.InstancesView;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.Duration;
import org.dmfs.tasks.JobIds;
import org.dmfs.tasks.actions.CancelDelayedAction;
import org.dmfs.tasks.actions.CancelNotificationAction;
import org.dmfs.tasks.actions.CompleteAction;
import org.dmfs.tasks.actions.Composite;
import org.dmfs.tasks.actions.Conditional;
import org.dmfs.tasks.actions.DeferDueAction;
import org.dmfs.tasks.actions.DelayedAction;
import org.dmfs.tasks.actions.NotifyStickyAction;
import org.dmfs.tasks.actions.OpenAction;
import org.dmfs.tasks.actions.PinAction;
import org.dmfs.tasks.actions.PostUndoAction;
import org.dmfs.tasks.actions.RemoveNotificationAction;
import org.dmfs.tasks.actions.TaskAction;
import org.dmfs.tasks.actions.UpdateWidgetsAction;
import org.dmfs.tasks.actions.WipeNotificationAction;
import org.dmfs.tasks.contract.TaskContract;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;


/**
 * A {@link JobIntentService} which handles notification actions.
 * <p>
 * TODO: convert to a more generic plugin mechanism which can be used on other places as well
 *
 * @author Marten Gajda
 */
public final class ActionService extends JobIntentService
{
    public static final String CHANNEL_PINNED = "org.dmfs.opentasks.PINNED";
    public static final String CHANNEL_DUE_DATES = "org.dmfs.opentasks.DUE_DATES";

    public static final String ACTION_PIN_TASK = "org.dmfs.tasks.intent.ACTION_PIN_TASK";
    public static final String ACTION_COMPLETE = "org.dmfs.tasks.intent.COMPLETE";
    public static final String ACTION_UNPIN = "org.dmfs.tasks.intent.UNPIN";
    public static final String ACTION_OPEN_TASK = "org.dmfs.tasks.intent.OPEN_TASK";
    public static final String ACTION_OPEN_TASK_CANCEL_NOTIFICATION = "org.dmfs.tasks.intent.OPEN_CANCEL_TASK";
    public static final String ACTION_REMOVE_NOTIFICATION = "org.dmfs.tasks.intent.CANCEL_NOTIFICATION";
    public static final String ACTION_NEXT_DAY = "org.dmfs.tasks.intent.ACTION_DAY_CHANGED";
    public static final String ACTION_RENOTIFY = "org.dmfs.tasks.intent.NOTIFY";
    public static final String ACTION_DEFER_1D = "org.dmfs.tasks.action.notification.DELAY_1D";
    public static final String ACTION_UNDO_COMPLETE = "org.dmfs.tasks.action.notification.UNDO_COMPLETE";
    public static final String ACTION_FINISH_COMPLETE = "org.dmfs.tasks.action.notification.FINISH_COMPLETE";

    private final static Duration ONE_DAY = new Duration(1, 1, 0);

    private static int UNDO_TIMEOUT_MILLIS = 10000;


    public static void startAction(Context context, String action, Uri taskUri)
    {
        enqueueWork(context, new Intent(context, ActionReceiver.class).setData(taskUri).setAction(action));
    }


    public static void enqueueWork(@NonNull Context context, @NonNull Intent work)
    {
        enqueueWork(context, ActionService.class, JobIds.NOTIFICATION_ACTION_SERVICE, work);
    }


    @Override
    protected void onHandleWork(@NonNull Intent intent)
    {
        try
        {
            Uri instanceUri = intent.getData();
            if (instanceUri == null || instanceUri.getAuthority() == null)
            {
                throw new RuntimeException(String.format("Invalid task instance Uri %s", instanceUri));
            }

            ContentProviderClient contentProviderClient = getContentResolver().acquireContentProviderClient(instanceUri);
            for (RowSnapshot<TaskContract.Instances> snapshot : new QueryRowSet<>(
                    new InstancesView<>(instanceUri.getAuthority(), contentProviderClient),
                    new org.dmfs.android.contentpal.projections.Composite<>(
                            Id.PROJECTION,
                            EffectiveDueDate.PROJECTION,
                            TaskStart.PROJECTION,
                            TaskPin.PROJECTION,
                            EffectiveTaskColor.PROJECTION,
                            TaskTitle.PROJECTION,
                            TaskVersion.PROJECTION,
                            TaskIsClosed.PROJECTION),
                    new EqArg<>(TaskContract.Instances._ID, ContentUris.parseId(instanceUri))))
            {
                resolveAction(intent.getAction()).execute(this, contentProviderClient, snapshot.values(), instanceUri);
            }
        }
        catch (RuntimeException | RemoteException | OperationApplicationException e)
        {
            Log.e("ActionService", String.format("unable to execute action %s", intent.getAction()), e);
        }
    }


    private TaskAction resolveAction(String action)
    {
        switch (action)
        {
            case ACTION_COMPLETE:
                return new Composite(
                        // remove the notification, without storing the change yet
                        new CancelNotificationAction(),
                        // create undo notification
                        new PostUndoAction(),
                        // create delayed action to finish the completion
                        new DelayedAction(ACTION_FINISH_COMPLETE, UNDO_TIMEOUT_MILLIS));

            case ACTION_PIN_TASK:
                // just pin, let TaskNotificationService do the rest
                return new PinAction(true);

            case ACTION_UNPIN:
                // unpin, let TaskNotificationService do the rest
                return new PinAction(false);

            case ACTION_OPEN_TASK:
                // just open the task
                return new OpenAction();

            case ACTION_OPEN_TASK_CANCEL_NOTIFICATION:
                // just open the task and cancel the notification
                return new Composite(new OpenAction(), new WipeNotificationAction());

            case ACTION_REMOVE_NOTIFICATION:
                // remove the notification
                return new RemoveNotificationAction();

            case ACTION_DEFER_1D:
                // defer the due date and remove notification if not pinned and due date is in the future
                return new Composite(
                        new DeferDueAction(ONE_DAY),
                        new Conditional(
                                (context, data) -> !new TaskPin(data).value() &&
                                        new EffectiveDueDate(data).value().addDuration(ONE_DAY).after(DateTime.nowAndHere()),
                                new WipeNotificationAction()));

            case TaskContract.ACTION_BROADCAST_TASK_DUE:
            case TaskContract.ACTION_BROADCAST_TASK_STARTING:
                return new Composite(
                        // post start and due notification on the due date channel
                        new NotifyStickyAction(data -> CHANNEL_DUE_DATES, true),
                        new UpdateWidgetsAction());

            case ACTION_UNDO_COMPLETE:
                return new Composite(
                        // cancel the delayed action
                        new CancelDelayedAction(ACTION_FINISH_COMPLETE),
                        // remove the undo notification
                        new CancelNotificationAction("tasks.undo"),
                        // repost notification
                        new NotifyStickyAction(
                                data -> new TaskPin(data).value() ? CHANNEL_PINNED : CHANNEL_DUE_DATES,
                                false));

            case ACTION_FINISH_COMPLETE:
                return new Composite(
                        // cancel any delayed action, in case we're called before the timeout elapsed
                        new CancelDelayedAction(ACTION_FINISH_COMPLETE),
                        // unpin the task
                        new PinAction(false),
                        // finish the completion
                        new CompleteAction(),
                        // remove the undo notification
                        new CancelNotificationAction("tasks.undo"));

            // TODO: trigger these for every notified task
            case Intent.ACTION_DATE_CHANGED:
            case Intent.ACTION_TIME_CHANGED:
            case Intent.ACTION_TIMEZONE_CHANGED:
            case ACTION_NEXT_DAY:
            case ACTION_RENOTIFY:
                // post pinned task on the pinned channel and other tasks on the due date channel
                return new NotifyStickyAction(
                        data -> new TaskPin(data).value() ? CHANNEL_PINNED : CHANNEL_DUE_DATES,
                        false);

            default:
                throw new RuntimeException(String.format("Unhandled action %s", action));
        }
    }
}
