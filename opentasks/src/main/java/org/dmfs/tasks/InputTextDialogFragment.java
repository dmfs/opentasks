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

package org.dmfs.tasks;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import org.dmfs.android.retentionmagic.SupportDialogFragment;
import org.dmfs.android.retentionmagic.annotations.Parameter;


/**
 * A simple prompt for text input.
 * <p/>
 * TODO: Use the style from the support library
 *
 * @author Marten Gajda <marten@dmfs.org>
 * @author Tristan Heinig <tristan@dmfs.org>
 */
public class InputTextDialogFragment extends SupportDialogFragment implements OnEditorActionListener, OnKeyListener
{

    protected final static String ARG_TITLE_TEXT = "title_text";
    protected final static String ARG_INITIAL_TEXT = "initial_text";
    protected final static String ARG_HINT_TEXT = "hint_text";
    protected final static String ARG_MESSAGE_TEXT = "message_text";

    @Parameter(key = ARG_TITLE_TEXT)
    protected String mTitle;
    @Parameter(key = ARG_HINT_TEXT)
    protected String mHint;
    @Parameter(key = ARG_MESSAGE_TEXT)
    protected String mMessage;
    @Parameter(key = ARG_INITIAL_TEXT)
    protected String mInitialText;

    protected EditText mEditText;
    protected TextView mErrorText;


    public InputTextDialogFragment()
    {
    }


    /**
     * Creates a {@link InputTextDialogFragment} with the given title and initial text value.
     *
     * @param title
     *         The title of the dialog.
     * @param message
     *         The text of the message field.
     * @param hint
     *         The hint of the input field.
     * @param initalText
     *         The initial text in the input field.
     *
     * @return A new {@link InputTextDialogFragment}.
     */
    public static InputTextDialogFragment newInstance(String title, String hint, String initalText, String message)
    {
        InputTextDialogFragment fragment = new InputTextDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE_TEXT, title);
        args.putString(ARG_MESSAGE_TEXT, message);
        args.putString(ARG_INITIAL_TEXT, initalText);
        args.putString(ARG_HINT_TEXT, hint);
        fragment.setArguments(args);
        return fragment;
    }


    /**
     * Creates a {@link InputTextDialogFragment} with the given title and initial text value.
     *
     * @param title
     *         The title of the dialog.
     * @param hint
     *         The hint of the input field.
     *
     * @return A new {@link InputTextDialogFragment}.
     */
    public static InputTextDialogFragment newInstance(String title, String hint, String initalText)
    {
        return newInstance(title, hint, initalText, null);
    }


    /**
     * Creates a {@link InputTextDialogFragment} with the given title and initial text value.
     *
     * @param title
     *         The title of the dialog.
     * @return A new {@link InputTextDialogFragment}.
     */
    public static InputTextDialogFragment newInstance(String title, String hint)
    {
        return newInstance(title, hint, null, null);
    }


    /**
     * Creates a {@link InputTextDialogFragment} with the given title and initial text value.
     *
     * @param title
     *         The title of the dialog.
     *
     * @return A new {@link InputTextDialogFragment}.
     */
    public static InputTextDialogFragment newInstance(String title)
    {
        return newInstance(title, null, null, null);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {

        final Context contextThemeWrapperLight = new ContextThemeWrapper(getActivity(), R.style.ThemeOverlay_AppCompat_Light);
        LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapperLight);

        View view = localInflater.inflate(R.layout.fragment_input_text_dialog, container);

        mEditText = (EditText) view.findViewById(android.R.id.input);
        mErrorText = (TextView) view.findViewById(R.id.error);
        if (savedInstanceState == null && mInitialText != null)
        {
            mEditText.setText(mInitialText);
        }
        if (savedInstanceState == null && mHint != null)
        {
            mEditText.setHint(mHint);
        }
        if (mMessage != null)
        {
            TextView mMessageView = (TextView) view.findViewById(android.R.id.message);
            mMessageView.setText(mMessage);
            mMessageView.setVisibility(View.VISIBLE);
        }
        ((TextView) view.findViewById(android.R.id.title)).setText(mTitle);

        mEditText.requestFocus();
        getDialog().getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        mEditText.setOnEditorActionListener(this);

        view.findViewById(android.R.id.button1).setOnClickListener(new OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                handleSave();
            }
        });

        view.findViewById(android.R.id.button2).setOnClickListener(new OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                handleCancel();
            }
        });

        return view;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        // hides the actual dialog title, we have one already...
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        // we want to listen to clicks on back button
        dialog.setOnKeyListener(this);
        return dialog;
    }


    /*
     * When the user clicks the back button, we assume that he wants to cancel the input, so we have to handle the back click event.
     */
    @Override
    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event)
    {
        // if the user clicks the back button
        if ((keyCode == android.view.KeyEvent.KEYCODE_BACK))
        {
            // filter only the touch down event
            if (event.getAction() != KeyEvent.ACTION_DOWN)
            {
                // we assume that he don't want to save his input
                handleCancel();
            }
            else
            {
                return false;
            }
        }
        return false;
    }


    /**
     * Dismisses the input dialog and calls the listener about the user abort.
     */
    protected void handleCancel()
    {
        Fragment parentFragment = getParentFragment();
        Activity activity = getActivity();

        if (parentFragment instanceof InputTextListener)
        {
            ((InputTextListener) parentFragment).onCancelInputDialog();
        }
        else if (activity instanceof InputTextListener)
        {
            ((InputTextListener) activity).onCancelInputDialog();

        }
        InputTextDialogFragment.this.dismiss();
    }


    @Override
    public void onCancel(DialogInterface dialog)
    {
        super.onCancel(dialog);
        handleCancel();
    }


    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
    {
        if (EditorInfo.IME_ACTION_DONE == actionId)
        {
            handleSave();
            return true;
        }
        return false;
    }


    /**
     * Dismisses the input dialog and calls the input listener and forwards the user input.
     */
    protected void handleSave()
    {
        String input = mEditText.getText().toString().trim();
        mEditText.setText(input);
        if (validate(input))
        {
            Fragment parentFragment = getParentFragment();
            Activity activity = getActivity();

            if (parentFragment instanceof InputTextListener)
            {
                ((InputTextListener) parentFragment).onInputTextChanged(input);
            }
            else if (activity instanceof InputTextListener)
            {
                ((InputTextListener) activity).onInputTextChanged(input);

            }
            InputTextDialogFragment.this.dismiss();
        }
    }


    /**
     * Validates the user input and returns true if the input is valid.
     *
     * @param input
     *         the text of the {@link EditText} field.
     *
     * @return true, if there is user input, otherwise false.
     */
    protected boolean validate(String input)
    {
        if (input == null || input.trim().length() < 1)
        {
            mErrorText.setVisibility(View.VISIBLE);
            mErrorText.setText(R.string.task_list_name_dialog_error);
            return false;
        }
        mErrorText.setVisibility(View.INVISIBLE);
        return true;
    }


    /**
     * Interface to listen to InputTextDialog events.
     *
     * @author Tristan Heinig <tristan@dmfs.org>
     */
    public interface InputTextListener
    {
        /**
         * Is Called, when the user wants to save his input.
         *
         * @param inputText
         *         the user input.
         */
        void onInputTextChanged(String inputText);

        /**
         * Is Called, when the user want to cancel the input.
         */
        void onCancelInputDialog();
    }

}
