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

package org.dmfs.provider.tasks.matchers;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;

import org.dmfs.android.contentpal.Operation;
import org.dmfs.android.contentpal.OperationsQueue;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import androidx.annotation.NonNull;
import androidx.test.platform.app.InstrumentationRegistry;


/**
 * @author Marten Gajda
 */
public final class NotifiesMatcher extends TypeSafeDiagnosingMatcher<Iterable<? extends Operation<?>>>
{
    private final Uri mUri;
    private final OperationsQueue mOperationsQueue;
    private final Matcher<Iterable<? extends Uri>> mDelegate;


    public static Matcher<Iterable<? extends Operation<?>>> notifies(@NonNull Uri uri, @NonNull OperationsQueue operationsQueue, @NonNull Matcher<Iterable<? extends Uri>> delegate)
    {
        return new NotifiesMatcher(uri, operationsQueue, delegate);
    }


    public NotifiesMatcher(Uri uri, @NonNull OperationsQueue operationsQueue, @NonNull Matcher<Iterable<? extends Uri>> delegate)
    {
        mUri = uri;
        mOperationsQueue = operationsQueue;
        mDelegate = delegate;
    }


    @Override
    protected boolean matchesSafely(Iterable<? extends Operation<?>> item, Description mismatchDescription)
    {
        Collection<Uri> notifications = Collections.synchronizedCollection(new HashSet<>());
        HandlerThread handlerThread = new HandlerThread("ObserverHandlerThread");
        handlerThread.start();

        ContentObserver observer = new ContentObserver(new Handler(handlerThread.getLooper()))
        {
            @Override
            public void onChange(boolean selfChange, Uri uri)
            {
                super.onChange(selfChange, uri);
                System.out.println("Notifcation: " + uri);
                notifications.add(uri);
            }
        };

        Context context = InstrumentationRegistry.getInstrumentation().getContext();
        context.getContentResolver().registerContentObserver(mUri, true, observer);
        try
        {
            try
            {
                mOperationsQueue.enqueue(item);
                mOperationsQueue.flush();
            }
            catch (Exception e)
            {
                throw new RuntimeException("Exception during executing the target OperationBatch", e);
            }

            Thread.sleep(100);
            if (!mDelegate.matches(notifications))
            {
                mismatchDescription.appendText("Wrong notifications ");
                mDelegate.describeMismatch(notifications, mismatchDescription);
                return false;
            }
            return true;
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
            return false;
        }
        finally
        {
            context.getContentResolver().unregisterContentObserver(observer);
            handlerThread.quit();
        }
    }


    @Override
    public void describeTo(Description description)
    {
        description.appendText("Notifies ").appendDescriptionOf(mDelegate);
    }
}
