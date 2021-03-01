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

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import org.dmfs.android.bolts.color.Color;
import org.dmfs.tasks.R;
import org.dmfs.tasks.widget.PopulateableViewGroup;
import org.dmfs.tasks.widget.SmartView;
import org.dmfs.tasks.widget.UpdatedSmartViews;


/**
 * {@link SmartView} for the subtasks section of the task details screen.
 *
 * @author Gabor Keszthelyi
 */
public final class SubtasksView implements SmartView<SubtasksView.Params>
{
    public interface Params
    {
        Color taskListColor();

        Iterable<SubtaskView.Params> subtasks();
    }


    private final ViewGroup mContentView;


    public SubtasksView(ViewGroup contentView)
    {
        mContentView = contentView;
    }


    @Override
    public void update(SubtasksView.Params params)
    {
        if (!params.subtasks().iterator().hasNext())
        {
            // Don't show the subtasks UI section if there are no subtasks
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(mContentView.getContext());

        inflater.inflate(R.layout.opentasks_view_item_divider, mContentView);

        TextView sectionHeader = (TextView) inflater.inflate(R.layout.opentasks_view_item_task_details_subtitles_section_header, null);
        sectionHeader.setTextColor(new Darkened(params.taskListColor()).argb());
        mContentView.addView(sectionHeader);

        new PopulateableViewGroup<SubtaskView>(mContentView)
                .populate(new UpdatedSmartViews<>(params.subtasks(), inflater, R.layout.opentasks_view_item_task_details_subtask));
    }


    // TODO Remove when #522 is merged, use the version from there
    private static final class Darkened implements Color
    {
        private final Color mOriginal;


        private Darkened(Color original)
        {
            mOriginal = original;
        }


        @Override
        public int argb()
        {
            float[] hsv = new float[3];
            android.graphics.Color.colorToHSV(mOriginal.argb(), hsv);
            hsv[2] = hsv[2] * 0.75f;
            return android.graphics.Color.HSVToColor(hsv);
        }
    }
}
