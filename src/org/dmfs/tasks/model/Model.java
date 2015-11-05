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

import org.dmfs.provider.tasks.TaskContract;
import org.dmfs.provider.tasks.TaskContract.TaskLists;
import org.dmfs.tasks.ManageListActivity;

import android.accounts.Account;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.support.v4.util.SparseArrayCompat;
import android.text.TextUtils;


/**
 * An abstract model class.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public abstract class Model
{
	private final static String INTENT_CATEGORY_PREFIX = "org.dmfs.intent.category.";
	private final static String EXTRA_COLOR_HINT = "org.dmfs.COLOR_HINT";
	private final static String EXTRA_TITLE_HINT = "org.dmfs.TITLE_HINT";

	/**
	 * A {@link List} of {@link FieldDescriptor}s of all fields that a model supports.
	 */
	private final List<FieldDescriptor> mFields = new ArrayList<FieldDescriptor>();
	private final SparseArrayCompat<FieldDescriptor> mFieldIndex = new SparseArrayCompat<FieldDescriptor>(16);

	private final Context mContext;
	private final String mAuthority;

	boolean mInflated = false;

	private boolean mAllowRecurrence = false;
	private boolean mAllowExceptions = false;
	private int mIconId = -1;
	private int mLabelId = -1;
	private String mAccountType;

	private Boolean mSupportsInsertListIntent;
	private Boolean mSupportsEditListIntent;


	protected Model(Context context, String accountType)
	{
		mContext = context;
		mAccountType = accountType;
		mAuthority = TaskContract.taskAuthority(context);
	}


	public final Context getContext()
	{
		return mContext;
	}


	public abstract void inflate() throws ModelInflaterException;


	/**
	 * Adds another field (identified by its field descriptor) to this model.
	 * 
	 * @param descriptor
	 *            The {@link FieldDescriptor} of the field to add.
	 */
	protected void addField(FieldDescriptor descriptor)
	{
		mFields.add(descriptor);
		mFieldIndex.put(descriptor.getFieldId(), descriptor);
	}


	public FieldDescriptor getField(int fieldId)
	{
		return mFieldIndex.get(fieldId, null);
	}


	public List<FieldDescriptor> getFields()
	{
		try
		{
			inflate();
		}
		catch (ModelInflaterException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new ArrayList<FieldDescriptor>(mFields);
	}


	public boolean getAllowRecurrence()
	{
		return mAllowRecurrence;
	}


	void setAllowRecurrence(boolean allowRecurrence)
	{
		mAllowRecurrence = allowRecurrence;
	}


	public boolean getAllowExceptions()
	{
		return mAllowExceptions;
	}


	void setAllowExceptions(boolean allowExceptions)
	{
		mAllowExceptions = allowExceptions;
	}


	public int getIconId()
	{
		return mIconId;
	}


	void setIconId(int iconId)
	{
		mIconId = iconId;
	}


	public int getLabelId()
	{
		return mLabelId;
	}


	void setLabelId(int titleId)
	{
		mLabelId = titleId;
	}


	public String getAccountType()
	{
		return mAccountType;
	}


	public String getAccountLabel()
	{
		return "";
	}


	public void startInsertIntent(Activity activity, Account account)
	{
		if (!hasInsertActivity())
		{
			throw new IllegalStateException("Syncadapter for " + mAccountType + " does not support inserting lists.");
		}

		activity.startActivity(getListIntent(mContext, Intent.ACTION_INSERT, account));
	}


	public void startEditIntent(Activity activity, Account account, long listId, String nameHint, Integer colorHint)
	{
		if (!hasEditActivity())
		{
			throw new IllegalStateException("Syncadapter for " + mAccountType + " does not support editing lists.");
		}

		Intent intent = getListIntent(mContext, Intent.ACTION_EDIT, account);
		intent.setData(ContentUris.withAppendedId(TaskLists.getContentUri(mAuthority), listId));
		if (nameHint != null)
		{
			intent.putExtra(EXTRA_TITLE_HINT, nameHint);
		}
		if (colorHint != null)
		{
			intent.putExtra(EXTRA_COLOR_HINT, colorHint);
		}
		activity.startActivity(intent);
	}


	public boolean hasEditActivity()
	{
		if (mSupportsEditListIntent == null)
		{
			ComponentName editComponent = getListIntent(mContext, Intent.ACTION_EDIT, null).setData(
				ContentUris.withAppendedId(TaskLists.getContentUri(mAuthority), 0 /* for pure intent resolution it doesn't matter which id we append */))
				.resolveActivity(mContext.getPackageManager());
			mSupportsEditListIntent = editComponent != null;
		}

		return mSupportsEditListIntent;
	}


	public boolean hasInsertActivity()
	{
		if (mSupportsInsertListIntent == null)
		{
			ComponentName insertComponent = getListIntent(mContext, Intent.ACTION_INSERT, null).resolveActivity(mContext.getPackageManager());
			mSupportsInsertListIntent = insertComponent != null;
		}

		return mSupportsInsertListIntent;
	}


	private Intent getListIntent(Context context, String action, Account account)
	{
		// insert action
		Intent insertIntent = new Intent();
		insertIntent.setAction(action);
		insertIntent.setData(TaskLists.getContentUri(mAuthority));
		insertIntent.addCategory(INTENT_CATEGORY_PREFIX + mAccountType);
		if (account != null)
		{
			insertIntent.putExtra(ManageListActivity.EXTRA_ACCOUNT, account);
		}
		return insertIntent;
	}


	@Override
	public boolean equals(Object o)
	{
		if (!(o instanceof Model))
		{
			return false;
		}
		Class<?> otherClass = o.getClass();
		Class<?> myClass = getClass();

		return myClass.equals(otherClass) && TextUtils.equals(mAccountType, ((Model) o).mAccountType);
	}

}
