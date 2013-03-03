/*
 * Copyright (C) 2012 Marten Gajda <marten@dmfs.org>
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
 * 
 */

package org.dmfs.tasks.model;

import java.util.ArrayList;
import java.util.List;

public abstract class Model {

    final ArrayList<FieldDescriptor> mFields = new ArrayList<FieldDescriptor>();
    boolean inflated = false;

    private boolean mAllowRecurrence = false;
    private boolean mAllowExceptions = false;
    private int mIconId = -1;
    private int mLabelId = -1;
    private String mAccountType;
    private String mTaskName;

    public abstract void inflate() throws ModelInflaterException;

    public List<FieldDescriptor> getFields() {
	try {
	    inflate();
	} catch (ModelInflaterException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	return new ArrayList<FieldDescriptor>(mFields);
    }

    public boolean getAllowRecurrence() {
	return mAllowRecurrence;
    }

    void setAllowRecurrence(boolean allowRecurrence) {
	mAllowRecurrence = allowRecurrence;
    }

    public boolean getAllowExceptions() {
	return mAllowExceptions;
    }

    void setAllowExceptions(boolean allowExceptions) {
	mAllowExceptions = allowExceptions;
    }

    public int getIconId() {
	return mIconId;
    }

    void setIconId(int iconId) {
	mIconId = iconId;
    }

    public int getLabelId() {
	return mLabelId;
    }

    void setLabelId(int titleId) {
	mLabelId = titleId;
    }

    public String getAccountType() {
	return mAccountType;
    }

    void setAccountType(String accountType) {
	mAccountType = accountType;
    }

}
