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

package org.dmfs.tasks.notification;

import android.app.Notification;
import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

import org.dmfs.android.contentpal.predicates.AnyOf;
import org.dmfs.android.contentpal.predicates.EqArg;
import org.dmfs.android.contentpal.predicates.In;
import org.dmfs.android.contentpal.projections.Composite;
import org.dmfs.android.contentpal.rowsets.QueryRowSet;
import org.dmfs.android.contentpal.views.Sorted;
import org.dmfs.jems.iterable.composite.Diff;
import org.dmfs.jems.iterable.decorators.Mapped;
import org.dmfs.jems.optional.Optional;
import org.dmfs.jems.pair.Pair;
import org.dmfs.opentaskspal.readdata.EffectiveDueDate;
import org.dmfs.opentaskspal.readdata.Id;
import org.dmfs.opentaskspal.readdata.TaskIsClosed;
import org.dmfs.opentaskspal.readdata.TaskPin;
import org.dmfs.opentaskspal.readdata.TaskStart;
import org.dmfs.opentaskspal.readdata.TaskVersion;
import org.dmfs.opentaskspal.views.InstancesView;
import org.dmfs.tasks.JobIds;
import org.dmfs.tasks.R;
import org.dmfs.tasks.actions.utils.NotificationPrefs;
import org.dmfs.tasks.contract.TaskContract;
import org.dmfs.tasks.contract.TaskContract.Tasks;
import org.dmfs.tasks.notification.state.PrefState;
import org.dmfs.tasks.notification.state.RowState;
import org.dmfs.tasks.notification.state.StateInfo;
import org.dmfs.tasks.notification.state.TaskNotificationState;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationManagerCompat;


/**
 * A {@link Service} that triggers and updates {@link Notification}s for Due and Start alarms as well as pinned tasks.
 *
 * @author Tobias Reinsch <tobias@dmfs.org>
 */
public class TaskNotificationService extends JobIntentService
{
    public static void enqueueWork(@NonNull Context context, @NonNull Intent work)
    {
        enqueueWork(context, TaskNotificationService.class, JobIds.NOTIFICATION_SERVICE, work);
    }


    private SharedPreferences mNotificationPrefs;


    @Override
    public void onCreate()
    {
        super.onCreate();
        mNotificationPrefs = new NotificationPrefs(this).next();
    }


    @Override
    protected void onHandleWork(@NonNull Intent intent)
    {
        switch (intent.getAction())
        {
            case Intent.ACTION_MY_PACKAGE_REPLACED:
            case Intent.ACTION_BOOT_COMPLETED:
                /*
                 When the device boots up or the app has been updated we just repost all notifications.
                 */
                for (String uri : mNotificationPrefs.getAll().keySet())
                {
                    ActionService.startAction(this, ActionService.ACTION_RENOTIFY, Uri.parse(uri));
                }
                break;
            default:
                /*
                 * In any other case we synchronize our stored state with the database.
                 *
                 * Notifications of tasks which no longer exist are removed.
                 * Notifications of tasks which have been pinned are added.
                 * Notifications of tasks which have been unpinned are removed.
                 * Notifications of tasks which have changed otherwise are updated.
                 */
                String authority = getString(R.string.opentasks_authority);

                Iterable<TaskNotificationState> currentNotifications = new org.dmfs.tasks.utils.Sorted<>(
                        (o, o2) -> (int) (ContentUris.parseId(o.instance()) - ContentUris.parseId(o2.instance())),
                        new Mapped<>(
                                PrefState::new,
                                mNotificationPrefs.getAll().entrySet()));

                for (Pair<Optional<TaskNotificationState>, Optional<RowState>> diff : new Diff<>(
                        currentNotifications,
                        new Mapped<>(snapShot -> new RowState(authority, snapShot.values()),
                                new QueryRowSet<>(
                                        new Sorted<>(TaskContract.Instances._ID,
                                                new InstancesView<>(authority, getContentResolver().acquireContentProviderClient(authority))),
                                        new Composite<>(Id.PROJECTION, TaskVersion.PROJECTION, TaskPin.PROJECTION, TaskIsClosed.PROJECTION,
                                                EffectiveDueDate.PROJECTION, TaskStart.PROJECTION),
                                        new AnyOf<>(
                                                // task is either pinned or has a notification
                                                new EqArg<>(Tasks.PINNED, 1),
                                                new In<>(Tasks._ID, new Mapped<>(p -> ContentUris.parseId(p.instance()), currentNotifications))))),
                        (o, o2) -> (int) (ContentUris.parseId(o.instance()) - ContentUris.parseId(o2.instance()))))
                {
                    if (!diff.left().isPresent())
                    {
                        // new task not notified yet, must be pinned
                        ActionService.startAction(this, ActionService.ACTION_RENOTIFY, diff.right().value().instance());
                    }
                    else if (!diff.right().isPresent())
                    {
                        // task no longer present, remove notification
                        removeTaskNotification(diff.left().value().instance());
                    }
                    else
                    {
                        if (diff.left().value().taskVersion() != diff.right().value().taskVersion())
                        {
                            // the task has been updated -> update the notification if necessary
                            StateInfo before = diff.left().value().info();
                            StateInfo now = diff.right().value().info();
                            if (!now.pinned() && // don't remove pinned notifications
                                    (before.pinned() // pin was removed
                                            || before.started() && !now.started() // start was deferred or removed
                                            || !now.started() && before.due() && !now.due() // due was deferred or removed
                                            || !before.done() && now.done() // task was closed
                                    ))
                            {
                                // notification is obsolete
                                removeTaskNotification(diff.left().value().instance());
                            }
                            else
                            {
                                // task was updated, also update the notification
                                ActionService.startAction(this, ActionService.ACTION_RENOTIFY, diff.left().value().instance());
                            }
                        }
                    }
                }
        }
    }


    private void removeTaskNotification(Uri uri)
    {
        mNotificationPrefs.edit().remove(uri.toString()).apply();
        NotificationManagerCompat.from(this).cancel("tasks", (int) ContentUris.parseId(uri));
    }
}