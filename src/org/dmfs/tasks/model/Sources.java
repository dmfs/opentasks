/*
 * Copyright (C) 2013 Marten Gajda <marten@dmfs.org>
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dmfs.provider.tasks.TaskContract;
import org.dmfs.tasks.utils.AsyncModelLoader;
import org.dmfs.tasks.utils.OnModelLoadedListener;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorDescription;
import android.accounts.OnAccountsUpdateListener;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SyncAdapterType;
import android.text.TextUtils;
import android.util.Log;


/**
 * Holds model definitions for all available task sources.
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class Sources extends BroadcastReceiver implements OnAccountsUpdateListener
{

	public final static String TAG = "org.dmfs.tasks.model.Sources";

	/**
	 * A Singleton instance in order to allow freeing it under memory pressure.
	 */
	private static Sources sInstance = null;

	/**
	 * Maps account types to their respective task model.
	 */
	private Map<String, Model> mAccountModelMap = new HashMap<String, Model>();

	/**
	 * Our application context.
	 */
	private final Context mContext;

	/**
	 * The cached account manager.
	 */
	private final AccountManager mAccountManager;

	private final String mAuthority;


	/**
	 * Get the Sources singleton instance. Don't call this from the UI thread since it may take a long time to gather all the information from the account
	 * manager.
	 */
	public static synchronized Sources getInstance(Context context)
	{
		if (sInstance == null)
		{
			sInstance = new Sources(context);
		}
		return sInstance;
	}


	/**
	 * Load a model asynchronously. This might be executed as a synchronous operation if the models have been loaded already.
	 * 
	 * @param context
	 *            A {@link Context}.
	 * @param accountType
	 *            The account type of the model to load.
	 * @param listener
	 *            The listener to call when the model has been loaded.
	 * @return <code>true</code> if the models were loaded already and the operation was executed synchronously, <code>false</code> otherwise.
	 */
	public static boolean loadModelAsync(Context context, String accountType, OnModelLoadedListener listener)
	{
		if (sInstance == null)
		{
			new AsyncModelLoader(context, listener).execute(accountType);
			return false;
		}
		else
		{
			Sources sources = getInstance(context);
			listener.onModelLoaded(sources.getModel(accountType));
			return true;
		}
	}


	/**
	 * Initialize all model sources.
	 * 
	 * @param context
	 */
	private Sources(Context context)
	{
		mContext = context.getApplicationContext();

		mAuthority = TaskContract.taskAuthority(context);

		// register to receive package changes
		IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
		filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
		filter.addDataScheme("package");
		mContext.registerReceiver(this, filter);

		// register to receive locale changes, we do that to reload labels and titles in that case
		filter = new IntentFilter(Intent.ACTION_LOCALE_CHANGED);
		mContext.registerReceiver(this, filter);

		// get accounts and build model map
		mAccountManager = AccountManager.get(mContext);
		mAccountManager.addOnAccountsUpdatedListener(this, null, false);
		getAccounts();
	}


	/**
	 * Builds the model map. This method determines all available task sources and loads their respective models from XML (falling back to {@link DefaultModel
	 * if no XML was found or it is broken).
	 */
	protected void getAccounts()
	{
		// remove old models if any
		mAccountModelMap.clear();

		final AuthenticatorDescription[] authenticators = mAccountManager.getAuthenticatorTypes();

		final SyncAdapterType[] syncAdapters = ContentResolver.getSyncAdapterTypes();

		for (SyncAdapterType syncAdapter : syncAdapters)
		{
			if (!mAuthority.equals(syncAdapter.authority))
			{
				// this sync-adapter is not for Tasks, skip it
				continue;
			}

			AuthenticatorDescription authenticator = getAuthenticator(authenticators, syncAdapter.accountType);

			if (authenticator == null)
			{
				// no authenticator for this account available
				continue;
			}

			Model model;
			try
			{
				// try to load the XML model
				model = new XmlModel(mContext, authenticator);
				model.inflate();
				Log.i(TAG, "inflated model for " + authenticator.type);
			}
			catch (ModelInflaterException e)
			{
				Log.e(TAG, "error inflating model for " + authenticator.packageName, e);
				model = new DefaultModel(mContext, authenticator.type);
				try
				{
					model.inflate();
				}
				catch (ModelInflaterException e1)
				{
					continue;
				}
			}

			if (model.getIconId() == -1)
			{
				model.setIconId(authenticator.iconId);
			}
			if (model.getLabelId() == -1)
			{
				model.setLabelId(authenticator.labelId);
			}

			mAccountModelMap.put(authenticator.type, model);
		}

		try
		{
			// add default model for LOCAL account type (i.e. the unsynced account).
			Model defaultModel = new DefaultModel(mContext, TaskContract.LOCAL_ACCOUNT_TYPE);
			defaultModel.inflate();
			mAccountModelMap.put(TaskContract.LOCAL_ACCOUNT_TYPE, defaultModel);
		}
		catch (ModelInflaterException e)
		{
			Log.e(TAG, "could not inflate default model", e);
		}

	}


	/**
	 * Return the {@link AuthenticatorDescription} for the given account type.
	 * 
	 * @param accountType
	 *            The account type to find.
	 * @return The {@link AuthenticatorDescription} for the given account type or {@code null} if no such account exists.
	 */
	private AuthenticatorDescription getAuthenticator(AuthenticatorDescription[] authenticators, String accountType)
	{
		for (AuthenticatorDescription auth : authenticators)
		{
			if (TextUtils.equals(accountType, auth.type))
			{
				return auth;
			}
		}
		// no authenticator for that account type found
		return null;
	}


	/**
	 * Return the task model for the given account type.
	 * 
	 * @param accountType
	 *            The account type.
	 * @return A {@link Model} instance for the given account type or {@code null} if no model was found.
	 */
	public Model getModel(String accountType)
	{
		return mAccountModelMap.get(accountType);
	}


	public Model getMinimalModel(String accountType)
	{
		Model result = new MinimalModel(mContext, accountType);
		try
		{
			result.inflate();
		}
		catch (ModelInflaterException e)
		{
			throw new RuntimeException("can't inflate mimimal model", e);
		}
		return result;
	}


	/**
	 * Return all accounts that support the task authority.
	 * 
	 * @return A {@link List} of {@link Account}s, will never be <code>null</code>.
	 */
	public List<Account> getExistingAccounts()
	{
		List<Account> result = new ArrayList<Account>();
		Account[] accounts = mAccountManager.getAccounts();
		for (Account account : accounts)
		{
			if (getModel(account.type) != null && ContentResolver.getIsSyncable(account, mAuthority) > 0)
			{
				result.add(account);
			}
		}
		return result;
	}


	/**
	 * Return a default model. This model can be used if {@link #getModel(String)} returned {@code null}. Which should not happen btw.
	 * 
	 * @return A {@link Model} instance.
	 */
	public Model getDefaultModel()
	{
		return mAccountModelMap.get(TaskContract.LOCAL_ACCOUNT_TYPE);
	}


	@Override
	public void onAccountsUpdated(Account[] accounts)
	{
		// the account list has changed, rebuild model map

		/*
		 * FIXME: Do we have to rebuild the model map? An account was added not a new model. Instead we could cache the existing accounts and update it here.
		 */
		getAccounts();
	}


	@Override
	public void onReceive(Context context, Intent intent)
	{
		// something has changed, rebuild model map
		// TODO: determine what exactly has changed and apply only necessary
		// modifications
		getAccounts();
	}
}
