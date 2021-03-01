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

package org.dmfs.tasks.widget;

import android.view.LayoutInflater;
import android.view.View;

import org.dmfs.iterables.decorators.DelegatingIterable;
import org.dmfs.jems.iterable.decorators.Mapped;

import androidx.annotation.LayoutRes;


/**
 * {@link Iterable} of {@link SmartView}s that are updated with the corresponding data items
 * from the given {@link Iterable} of <code>D</code>.
 *
 * @author Gabor Keszthelyi
 */
public final class UpdatedSmartViews<D, V extends View & SmartView<D>> extends DelegatingIterable<V>
{

    public UpdatedSmartViews(Iterable<D> dataIterable, LayoutInflater inflater, @LayoutRes int layout)
    {
        super(new Mapped<>((dataItem) ->
        {
            //noinspection unchecked
            V view = (V) inflater.inflate(layout, null);
            view.update(dataItem);
            return view;
        }, dataIterable));
    }

}
