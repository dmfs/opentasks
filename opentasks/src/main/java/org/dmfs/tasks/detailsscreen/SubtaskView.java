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

package org.dmfs.tasks.detailsscreen;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import org.dmfs.android.bolts.color.Color;
import org.dmfs.jems.optional.Optional;
import org.dmfs.jems.single.combined.Backed;
import org.dmfs.provider.tasks.AuthorityUtil;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.tasks.R;
import org.dmfs.tasks.contract.TaskContract;
import org.dmfs.tasks.databinding.OpentasksViewItemTaskDetailsSubtaskBinding;
import org.dmfs.tasks.readdata.TaskContentUri;
import org.dmfs.tasks.utils.DateFormatter;
import org.dmfs.tasks.utils.DateFormatter.DateFormatContext;
import org.dmfs.tasks.widget.ProgressBackgroundView;
import org.dmfs.tasks.widget.SmartView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;


/**
 * {@link View} for showing a subtask on the details screen.
 *
 * @author Gabor Keszthelyi
 */
public final class SubtaskView extends FrameLayout implements SmartView<SubtaskView.Params>
{

    public interface Params // i.e. fields of the subtask
    {
        Long id();

        Optional<CharSequence> title();

        Optional<DateTime> due();

        Color color();

        Optional<DateTime> completionTime();

        Optional<Integer> percentComplete();
    }


    public SubtaskView(@NonNull Context context, @Nullable AttributeSet attrs)
    {
        super(context, attrs);
    }


    @Override
    public void update(Params subtask)
    {
        OpentasksViewItemTaskDetailsSubtaskBinding views = DataBindingUtil.bind(this);

        views.opentasksTaskDetailsSubtaskTitle.setText(
                new Backed<>(subtask.title(), getContext().getString(R.string.opentasks_task_details_subtask_untitled)).value());

        if (subtask.due().isPresent())
        {
            views.opentasksTaskDetailsSubtaskDue.setText(
                    new DateFormatter(getContext()).format(subtask.due().value(), DateTime.now(), DateFormatContext.LIST_VIEW));
        }

        views.opentasksTaskDetailsSubtaskListRibbon.setBackgroundColor(subtask.color().argb());

        // checkbox color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            ColorStateList colorStateList = new ColorStateList(
                    new int[][] {

                            new int[] { -subtask.color().argb() }, //disabled
                            new int[] { subtask.color().argb() } //enabled
                    },
                    new int[] {
                            -subtask.color().argb(), //disabled
                            subtask.color().argb() //enabled
                    }
            );
            views.opentasksTaskDetailsSubtaskCheckbox.setButtonTintList(colorStateList);
        }
        views.opentasksTaskDetailsSubtaskCheckbox.setTextColor(subtask.color().argb());
        if (subtask.completionTime().isPresent())
        {
            views.opentasksTaskDetailsSubtaskDue.setText(
                    new DateFormatter(getContext()).format(subtask.completionTime().value(), DateTime.now(), DateFormatContext.LIST_VIEW));
        }

        views.opentasksTaskDetailsSubtaskCheckbox.setChecked(subtask.percentComplete().isPresent() && subtask.percentComplete().value() == 100);
        views.opentasksTaskDetailsSubtaskCheckbox.setOnCheckedChangeListener((buttonView, completedValue) -> {
            ContentValues values = new ContentValues();
            values.put(TaskContract.Tasks.STATUS, completedValue ? TaskContract.Tasks.STATUS_COMPLETED : TaskContract.Tasks.STATUS_IN_PROCESS);
            if (!completedValue)
            {
                values.put(TaskContract.Tasks.PERCENT_COMPLETE, 0);
            }
            Context ctx = buttonView.getContext();
            Uri taskUri = ContentUris.withAppendedId(TaskContract.Instances.getContentUri(AuthorityUtil.taskAuthority(ctx)), subtask.id());
            boolean completed = ctx.getApplicationContext().getContentResolver().update(taskUri, values, null, null) != 0;
        });

        new ProgressBackgroundView(views.opentasksTaskDetailsSubtaskProgressBackground)
                .update(subtask.percentComplete());

        views.getRoot().setOnClickListener((v) ->
        {
            Context ctx = v.getContext();
            // TODO Use BasicTaskDetailsUi class when #589 is merged
            ctx.startActivity(new Intent(Intent.ACTION_VIEW, new TaskContentUri(subtask.id(), ctx).value()));
        });
    }
}
