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

package org.dmfs.tasks.utils.permission.dialog;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import org.dmfs.tasks.R;
import org.dmfs.tasks.utils.permission.BasicAppPermissions;


/**
 * @author Gabor Keszthelyi
 */
// TODO Parameterize with args for permission name, title, message, button labels. Class name to DismissiblePermissionRequestDialogFragment?
public final class PermissionRequestDialogFragment extends DialogFragment
{

    // TODO Figure out better way when Permission lib is available
    public static final String PREFS_STORE_IGNORED_PERMISSIONS = "opentasks.prefs.permissions.userignored";


    public static DialogFragment newInstance()
    {
        return new PermissionRequestDialogFragment();
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.opentasks_permission_request_dialog_getaccounts_title)
                .setMessage(R.string.opentasks_permission_request_dialog_getaccounts_message)
                .setPositiveButton(R.string.opentasks_permission_request_dialog_getaccounts_button_positive,
                        new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int whichButton)
                            {
                                new BasicAppPermissions(getContext()).forName(Manifest.permission.GET_ACCOUNTS)
                                        .request().send(getActivity());
                            }
                        }
                )
                .setNegativeButton(R.string.opentasks_permission_request_dialog_getaccounts_button_negative,
                        new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int whichButton)
                            {
                                SharedPreferences prefs = getContext().getSharedPreferences(PREFS_STORE_IGNORED_PERMISSIONS, Context.MODE_PRIVATE);
                                prefs.edit().putBoolean(Manifest.permission.GET_ACCOUNTS, true).apply();
                            }
                        }
                )
                .create();
    }
}
