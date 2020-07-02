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
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


/**
 * {@link Single} decorator that sets the threading so that
 * the work is on {@link Schedulers#io()} and delivery is on main thread.
 * <p>
 * Important: it has to be applied last normally, since <code>.observeOn(mainThread)</code> results in
 * doing everything on main thread after this decorator is applied.
 *
 * @author Gabor Keszthelyi
 */
public final class Offloading<T> extends DelegatingSingle<T>
{

    public Offloading(Single<T> delegate)
    {
        super(delegate
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()));
    }
}
