/*
 * Copyright 2021 dmfs GmbH
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

package org.dmfs.tasks.linkify;

import android.annotation.SuppressLint;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposables;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;


public final class ViewObservables
{
    public static Observable<CharSequence> textChanges(TextView view)
    {
        return Observable.create(emitter -> {
            TextWatcher textWatcher = new TextWatcher()
            {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after)
                {
                    // nothing
                }


                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count)
                {
                    // nothing
                }


                @Override
                public void afterTextChanged(Editable s)
                {
                    emitter.onNext(s);
                }
            };
            emitter.setDisposable(Disposables.fromRunnable(() -> view.removeTextChangedListener(textWatcher)));
            view.addTextChangedListener(textWatcher);
        });
    }


    @SuppressLint("ClickableViewAccessibility")
    public static Observable<MotionEvent> activityTouchEvents(View view)
    {
        return Observable.create(emitter -> {
            // set up a trap to receive touch events outside the ActionMode view.
            View touchTrap = new View(view.getContext());
            touchTrap.setLayoutParams(new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT));
            ViewGroup root = (ViewGroup) view.getRootView();
            root.addView(touchTrap);

            emitter.setDisposable(Disposables.fromRunnable(() -> {
                touchTrap.setOnTouchListener(null);
                root.removeView(touchTrap);
            }));

            touchTrap.setOnTouchListener((v, event) -> {
                emitter.onNext(event);
                return false;
            });
        });
    }
}