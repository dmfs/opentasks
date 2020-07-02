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

package org.dmfs.tasks.readdata;

import android.content.ContentProviderClient;
import android.os.Build;

import org.dmfs.tasks.utils.rxjava.DelegatingDisposable;

import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;


/**
 * {@link Disposable} for {@link ContentProviderClient}.
 *
 * @author Gabor Keszthelyi
 */
public final class ContentProviderClientDisposable extends DelegatingDisposable
{
    public ContentProviderClientDisposable(final ContentProviderClient client)
    {
        super(Disposables.fromRunnable(() ->
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            {
                client.close();
            }
            else
            {
                client.release();
            }
        }));
    }

}
