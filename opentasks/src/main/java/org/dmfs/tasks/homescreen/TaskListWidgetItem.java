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

package org.dmfs.tasks.homescreen;

import android.text.format.Time;


/**
 * This Class is used to for storing data of a single task in the task list widget.
 *
 * @author Arjun Naik<arjun@arjunnaik.in>
 * @author Marten Gajda<marten@dmfs.org>
 */
public class TaskListWidgetItem
{

    /**
     * The task title.
     */
    private final String mTaskTitle;

    /**
     * The due date.
     */
    private final Time mDueDate;

    /**
     * The task color.
     */
    private final int mTaskColor;

    /**
     * The instance ID.
     */
    private final int mInstanceId;

    /**
     * The flag to indicate if task is closed.
     */
    private final boolean mIsClosed;


    /**
     * Instantiates a new task list widget item.
     *
     * @param id
     *         the id of the task
     * @param title
     *         the title of the task
     * @param due
     *         the due date of the task
     * @param color
     *         the color of the list of the task
     * @param isClosed
     *         the flag to indicate if closed
     */
    public TaskListWidgetItem(int id, String title, Time due, int color, boolean isClosed)
    {
        mInstanceId = id;
        mTaskTitle = title;
        mDueDate = due;
        mTaskColor = color;
        mIsClosed = isClosed;
    }


    /**
     * Gets the task color.
     *
     * @return the task color
     */
    public int getTaskColor()
    {
        return mTaskColor;
    }


    /**
     * Gets the due date.
     *
     * @return the due date
     */
    public Time getDueDate()
    {
        return mDueDate;
    }


    /**
     * Gets the task title.
     *
     * @return the task title
     */
    public String getTaskTitle()
    {
        return mTaskTitle;
    }


    /**
     * Gets the task id.
     *
     * @return the task id
     */
    public long getInstanceId()
    {
        return mInstanceId;
    }


    /**
     * Gets the checks if is closed.
     *
     * @return the checks if is closed
     */
    public boolean getIsClosed()
    {
        return mIsClosed;
    }
}
