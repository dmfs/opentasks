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
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.telephony.PhoneNumberUtils;
import android.text.Layout;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.dmfs.jems.iterable.elementary.Seq;
import org.dmfs.jems.procedure.composite.ForEach;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import io.reactivex.Completable;

import static io.reactivex.Completable.ambArray;
import static org.dmfs.tasks.linkify.ViewObservables.activityTouchEvents;
import static org.dmfs.tasks.linkify.ViewObservables.textChanges;


/**
 * Adds clickable links that, on click, present a (floating) action mode to the user (unless running on Android 5).
 */
public class ActionModeLinkify
{
    private final static String TEL_PATTERN = "(?:\\+\\d{1,5}\\s*)?(?:\\(\\d{1,6}\\)\\s*)?\\d[-, \\.\\/\\d]{4,}\\d";
    private final static String URL_PATTERN = "(?:https?:\\/\\/(?:www\\.|(?!www))[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|www\\.[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|https?:\\/\\/(?:www\\.|(?!www))[a-zA-Z0-9]+\\.[^\\s]{2,}|www\\.[a-zA-Z0-9]+\\.[^\\s]{2,})";
    private final static String MAIL_PATTERN = "(?:[a-zA-Z\\d!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-zA-Z\\d!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z\\d](?:[a-z\\d-]*[a-z\\d])?\\.)+[a-z\\d](?:[a-z\\d-]*[a-z\\d])?|\\[(?:(?:(?:2(?:5[0-5]|[0-4]\\d)|1\\d\\d|\\d?\\d))\\.){3}(?:(?:2(?:5[0-5]|[0-4]\\d)|1\\d\\d|\\d?\\d)|[a-z\\d-]*[a-z\\d]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";
    private final static Pattern LINK_PATTERN = Pattern.compile(String.format("(?:^|\\s+)((%s)|(%s)|(%s))(?:\\s+|$)", TEL_PATTERN, URL_PATTERN, MAIL_PATTERN));


    public interface ActionModeListener
    {
        boolean prepareMenu(TextView view, Uri uri, Menu menu);

        boolean onClick(TextView view, Uri uri, MenuItem item);
    }


    public static void linkify(TextView textView, ActionModeListener listener)
    {
        CharSequence text = textView.getText();
        Matcher m = LINK_PATTERN.matcher(text);
        SpannableString s = new SpannableString(text);
        new ForEach<>(new Seq<>(s.getSpans(0, s.length(), ClickableSpan.class))).process(s::removeSpan);
        int pos = 0;
        while (m.find(pos))
        {
            int start = m.start(1);
            int end = m.end(1);
            pos = end;
            Uri uri = null;
            if (m.group(2) != null)
            {
                uri = Uri.parse("tel:" + PhoneNumberUtils.normalizeNumber(m.group(2)));
            }
            else if (m.group(3) != null)
            {
                uri = Uri.parse(m.group(3));
                if (uri.getScheme() == null)
                {
                    uri = uri.buildUpon().scheme("https").build();
                }
            }
            else if (m.group(4) != null)
            {
                uri = Uri.parse("mailto:" + m.group(4));
            }
            Uri finalUri = uri;
            s.setSpan(new ClickableSpan()
                      {
                          @SuppressLint("CheckResult")
                          @Override
                          public void onClick(@NonNull View widget)
                          {
                              if (Build.VERSION.SDK_INT >= 23)
                              {
                                  Completable closeActionTrigger = ambArray(
                                          textChanges(textView).firstElement().ignoreElement(),
                                          activityTouchEvents(textView).firstElement().ignoreElement())
                                          .cache();

                                  ActionMode.Callback2 actionMode = new ActionMode.Callback2()
                                  {

                                      @Override
                                      public boolean onCreateActionMode(ActionMode mode, Menu menu)
                                      {
                                          return listener.prepareMenu(textView, finalUri, menu);
                                      }


                                      @Override
                                      public boolean onPrepareActionMode(ActionMode mode, Menu menu)
                                      {
                                          return true;
                                      }


                                      @Override
                                      public boolean onActionItemClicked(ActionMode mode, MenuItem item)
                                      {
                                          return listener.onClick(textView, finalUri, item);
                                      }


                                      @Override
                                      public void onDestroyActionMode(ActionMode mode)
                                      {
                                          closeActionTrigger.subscribe().dispose();
                                      }


                                      @Override
                                      public void onGetContentRect(ActionMode mode, View view, Rect outRect)
                                      {
                                          Layout layout = textView.getLayout();
                                          int firstLine = layout.getLineForOffset(start);
                                          int lastLine = layout.getLineForOffset(end);
                                          layout.getLineBounds(firstLine, outRect);
                                          if (firstLine == lastLine)
                                          {
                                              outRect.left = (int) layout.getPrimaryHorizontal(start);
                                              outRect.right = (int) layout.getPrimaryHorizontal(end);
                                          }
                                          else
                                          {
                                              Rect lastLineBounds = new Rect();
                                              layout.getLineBounds(lastLine, lastLineBounds);
                                              outRect.bottom = lastLineBounds.bottom;
                                          }
                                      }
                                  };

                                  ActionMode am = textView.startActionMode(actionMode, android.view.ActionMode.TYPE_FLOATING);
                                  if (am != null)
                                  {
                                      closeActionTrigger.subscribe(am::finish);
                                  }
                              }
                          }
                      },
                    start,
                    end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        textView.setText(s);
    }
}
