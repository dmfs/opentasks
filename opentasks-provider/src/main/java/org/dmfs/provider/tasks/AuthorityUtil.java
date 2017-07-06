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

package org.dmfs.provider.tasks;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;

import org.dmfs.tasks.contract.UriFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * TODO
 */
public final class AuthorityUtil
{
    /**
     * The task authority cache.
     */
    private static Map<String, String> sAuthorities = Collections.synchronizedMap(new HashMap<String, String>(4));


    /**
     * Returns the authority of the {@link TaskProvider} in the given {@link Context}.
     * <p/>
     * TODO: create an Authority class instead that handles everything about authorities. It could replace {@link UriFactory} as well. The Authority class could
     * have a generic parameter that identifies the authority provider or contract class.
     *
     * @param context
     *         A {@link Context} of an app that contains a {@link TaskProvider}.
     *
     * @return The authority.
     *
     * @throws RuntimeException
     *         if there is no {@link TaskProvider} in that {@link Context}.
     */
    public static synchronized String taskAuthority(Context context)
    {
        String packageName = context.getPackageName();
        if (sAuthorities.containsKey(packageName))
        {
            return sAuthorities.get(packageName);
        }

        PackageManager packageManager = context.getPackageManager();

        // first get the PackageInfo of this app.
        PackageInfo packageInfo;
        try
        {
            packageInfo = packageManager.getPackageInfo(context.getPackageName(), PackageManager.GET_PROVIDERS);
        }
        catch (PackageManager.NameNotFoundException e)
        {
            throw new RuntimeException("Could not find TaskProvider!", e);
        }

        // next scan all providers for TaskProvider
        for (ProviderInfo provider : packageInfo.providers)
        {
            Class<?> providerClass;
            try
            {
                providerClass = Class.forName(provider.name);
            }
            catch (ClassNotFoundException e)
            {
                continue;
            }

            if (!TaskProvider.class.isAssignableFrom(providerClass))
            {
                continue;
            }

            sAuthorities.put(packageName, provider.authority);
            return provider.authority;
        }
        throw new RuntimeException("Could not find TaskProvider! Make sure you added it to your AndroidManifest.xml.");
    }
}
