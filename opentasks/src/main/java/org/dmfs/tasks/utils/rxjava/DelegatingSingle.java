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

package org.dmfs.tasks.utils.rxjava;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.SingleSource;


/**
 * Base class for {@link Single} that delegates to a {@link SingleSource}.
 *
 * @author Gabor Keszthelyi
 */
public abstract class DelegatingSingle<T> extends Single<T>
{
    private final SingleSource<T> mDelegate;


    protected DelegatingSingle(SingleSource<T> delegate)
    {
        mDelegate = delegate;
    }


    @Override
    protected final void subscribeActual(SingleObserver<? super T> observer)
    {
        mDelegate.subscribe(observer);
    }
}
