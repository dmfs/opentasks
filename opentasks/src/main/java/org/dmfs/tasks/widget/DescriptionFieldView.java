/*
 * Copyright 2019 dmfs GmbH
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

package org.dmfs.tasks.widget;

import android.animation.LayoutTransition;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;

import com.jmedeisis.draglinearlayout.DragLinearLayout;
import com.jmedeisis.draglinearlayout.DragLinearLayout.OnViewSwapListener;

import org.dmfs.android.bolts.color.colors.AttributeColor;
import org.dmfs.jems.function.Function;
import org.dmfs.jems.iterable.composite.Joined;
import org.dmfs.jems.optional.Optional;
import org.dmfs.jems.optional.decorators.Mapped;
import org.dmfs.jems.optional.elementary.Present;
import org.dmfs.jems.procedure.Procedure;
import org.dmfs.jems.procedure.composite.ForEach;
import org.dmfs.tasks.R;
import org.dmfs.tasks.linkify.ActionModeLinkify;
import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.DescriptionItem;
import org.dmfs.tasks.model.FieldDescriptor;
import org.dmfs.tasks.model.adapters.DescriptionFieldAdapter;
import org.dmfs.tasks.model.layout.LayoutOptions;
import org.dmfs.tasks.utils.DescriptionMovementMethod;

import java.util.List;

import androidx.core.view.ViewCompat;

import static org.dmfs.jems.optional.elementary.Absent.absent;


/**
 * View widget for descriptions with checklists.
 *
 * @author Marten Gajda <marten@dmfs.org>
 */
public class DescriptionFieldView extends AbstractFieldView implements OnCheckedChangeListener, OnViewSwapListener, OnClickListener, ActionModeLinkify.ActionModeListener
{
    private DescriptionFieldAdapter mAdapter;
    private DragLinearLayout mContainer;
    private List<DescriptionItem> mCurrentValue;
    private boolean mBuilding = false;
    private LayoutInflater mInflater;
    private InputMethodManager mImm;
    private View mActionView;


    public DescriptionFieldView(Context context)
    {
        super(context);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mImm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
    }


    public DescriptionFieldView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mImm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
    }


    public DescriptionFieldView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mImm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
    }


    @Override
    protected void onFinishInflate()
    {
        super.onFinishInflate();
        mContainer = findViewById(R.id.checklist);
        mContainer.setOnViewSwapListener(this);
        mContainer.findViewById(R.id.add_item).setOnClickListener(this);
        mActionView = mInflater.inflate(R.layout.description_field_view_element_actions, mContainer, false);
    }


    @Override
    public void setFieldDescription(FieldDescriptor descriptor, LayoutOptions layoutOptions)
    {
        super.setFieldDescription(descriptor, layoutOptions);
        mAdapter = (DescriptionFieldAdapter) descriptor.getFieldAdapter();
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    {
        if (mCurrentValue == null || mBuilding)
        {
            return;
        }

        int childCount = mContainer.getChildCount();
        for (int i = 0; i < childCount; ++i)
        {
            if (mContainer.getChildAt(i).findViewById(android.R.id.checkbox) == buttonView)
            {
                mCurrentValue.get(i).checked = isChecked;
                ((TextView) mContainer.getChildAt(i).findViewById(android.R.id.title)).setTextAppearance(getContext(),
                        isChecked ? R.style.checklist_checked_item_text : R.style.dark_text);
                if (mValues != null)
                {
                    mAdapter.validateAndSet(mValues, mCurrentValue);
                }
                return;
            }
        }
    }


    @Override
    public void updateValues()
    {
        mAdapter.validateAndSet(mValues, mCurrentValue);
    }


    @Override
    public void onContentLoaded(ContentSet contentSet)
    {
        super.onContentLoaded(contentSet);
    }


    @Override
    public void onContentChanged(ContentSet contentSet)
    {
        if (mValues != null)
        {
            List<DescriptionItem> newValue = mAdapter.get(mValues);
            if (newValue != null && !newValue.equals(mCurrentValue)) // don't trigger unnecessary updates
            {
                updateCheckList(newValue);
                mCurrentValue = newValue;
            }
        }
    }


    private void updateCheckList(List<DescriptionItem> list)
    {
        setVisibility(VISIBLE);

        mBuilding = true;

        int count = 0;
        for (final DescriptionItem item : list)
        {
            View itemView = mContainer.getChildAt(count);
            if (itemView == null || itemView.getId() != R.id.checklist_element)
            {
                itemView = createItemView();
                mContainer.addDragView(itemView, itemView.findViewById(R.id.drag_handle), mContainer.getChildCount() - 1);
            }

            bindItemView(itemView, item);

            ++count;
        }

        while (mContainer.getChildCount() > count + 1)
        {
            View view = mContainer.getChildAt(count);
            mContainer.removeDragView(view);
        }

        mBuilding = false;
    }


    @Override
    public void onSwap(View view1, int position1, View view2, int position2)
    {
        if (mCurrentValue != null)
        {
            DescriptionItem item1 = mCurrentValue.get(position1);
            DescriptionItem item2 = mCurrentValue.get(position2);

            // swap items in the list
            mCurrentValue.set(position2, item1);
            mCurrentValue.set(position1, item2);
        }
    }


    /**
     * Inflates a new check list element view.
     *
     * @return
     */
    private View createItemView()
    {
        View item = mInflater.inflate(R.layout.description_field_view_element, mContainer, false);
        // disable transition animations
        LayoutTransition transition = ((ViewGroup) item).getLayoutTransition();
        transition.disableTransitionType(LayoutTransition.CHANGE_APPEARING);
        transition.disableTransitionType(LayoutTransition.CHANGE_DISAPPEARING);
        transition.disableTransitionType(LayoutTransition.CHANGING);
        transition.disableTransitionType(LayoutTransition.APPEARING);
        transition.disableTransitionType(LayoutTransition.DISAPPEARING);
        ((TextView) item.findViewById(android.R.id.title)).setMovementMethod(DescriptionMovementMethod.getInstance());
        return item;
    }


    private void bindItemView(final View itemView, final DescriptionItem item)
    {
        // set the checkbox status
        CheckBox checkbox = itemView.findViewById(android.R.id.checkbox);

        // make sure we don't receive our own updates
        checkbox.setOnCheckedChangeListener(null);
        checkbox.setChecked(item.checked && item.checkbox);
        checkbox.jumpDrawablesToCurrentState();
        checkbox.setOnCheckedChangeListener(DescriptionFieldView.this);

        checkbox.setVisibility(item.checkbox ? VISIBLE : GONE);

        // configure the title
        final EditText text = itemView.findViewById(android.R.id.title);
        text.setTextAppearance(getContext(), item.checked && item.checkbox ? R.style.checklist_checked_item_text : R.style.dark_text);
        if (text.getTag() != null)
        {
            text.removeTextChangedListener((TextWatcher) text.getTag());
        }
        text.setText(item.text);

        ColorStateList colorStateList = new ColorStateList(
                new int[][] { new int[] { android.R.attr.state_focused }, new int[] { -android.R.attr.state_focused } },
                new int[] { new AttributeColor(getContext(), R.attr.colorPrimary).argb(), 0 });
        ViewCompat.setBackgroundTintList(text, colorStateList);

        text.setOnFocusChangeListener((v, hasFocus) -> {
            String newText = text.getText().toString();
            if (!hasFocus && !newText.equals(item.text))
            {
                item.text = newText;
            }

            if (hasFocus)
            {
                addActionView(itemView, item);
                setupActionView(item);
            }
            else
            {
                ActionModeLinkify.linkify(text, DescriptionFieldView.this);
                ((ViewGroup) itemView.findViewById(R.id.action_bar)).removeAllViews();
            }
        });
        text.setOnKeyListener((view, i, keyEvent) -> {
            // intercept DEL key so we can join lines
            if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && keyEvent.getKeyCode() == KeyEvent.KEYCODE_DEL && text.getSelectionStart() == 0)
            {
                int pos = mContainer.indexOfChild(itemView);
                if (pos > 0)
                {
                    EditText previousEditText = mContainer.getChildAt(pos - 1).findViewById(android.R.id.title);
                    String previousText = previousEditText.getText().toString();
                    int selectorPos = previousText.length();

                    String newText = previousText + text.getText().toString();
                    // concat content of this item to the previous one
                    previousEditText.setText(newText);
                    previousEditText.requestFocus();
                    mCurrentValue.get(pos - 1).text = newText;
                    mCurrentValue.remove(item);
                    mContainer.removeDragView(itemView);
                    mAdapter.validateAndSet(mValues, mCurrentValue);
                    previousEditText.setSelection(Math.min(selectorPos, previousEditText.getText().length()));
                    return true;
                }
            }
            if (item.checkbox && keyEvent.getAction() == KeyEvent.ACTION_DOWN && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)
            {
                // we own this event
                return true;
            }
            if (item.checkbox && keyEvent.getAction() == KeyEvent.ACTION_UP && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)
            {
                if (text.getText().length() == 0)
                {
                    // convert to unchecked text
                    item.checkbox = false;
                    new Animated(checkbox, v -> (ViewGroup) v.getParent()).process(c -> c.setVisibility(View.GONE));
                    text.requestFocus();
                    text.setSingleLine(false);
                    return true;
                }
                // split current
                String current = text.getText().toString();
                int sel = Math.max(0, Math.min(current.length(), text.getSelectionStart()));
                String newText = current.substring(sel);
                item.text = current.substring(0, sel);
                text.setText(item.text);
                text.clearFocus();
                // create new item with new test
                int pos = mContainer.indexOfChild(itemView);
                insertItem(item.checkbox, pos + 1, newText);
                EditText editText = ((EditText) mContainer.getChildAt(pos + 1).findViewById(android.R.id.title));
                editText.setSelection(0);
                editText.setSingleLine(true);
                editText.setMaxLines(Integer.MAX_VALUE);
                editText.setHorizontallyScrolling(false);
                return true;
            }
            return false;
        });

        text.setSingleLine(item.checkbox);
        text.setMaxLines(Integer.MAX_VALUE);
        text.setHorizontallyScrolling(false);

        TextWatcher watcher = new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {

            }


            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {

            }


            @Override
            public void afterTextChanged(Editable editable)
            {
                item.text = editable.toString();
            }
        };

        text.setTag(watcher);
        ActionModeLinkify.linkify(text, this);
        text.addTextChangedListener(watcher);
    }


    @Override
    public boolean prepareMenu(TextView view, Uri uri, Menu menu)
    {
        Optional<String> optAction = actionForUri(uri);
        new ForEach<>(new Joined<>(
                new Mapped<>(action -> getContext().getPackageManager()
                        .queryIntentActivities(new Intent(action).setData(uri), PackageManager.GET_RESOLVED_FILTER | PackageManager.GET_META_DATA),
                        optAction)))
                .process(
                        resolveInfo -> menu.add(titleForAction(optAction.value()))
                                .setIcon(resolveInfo.loadIcon(getContext().getPackageManager()))
                                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                );
        return menu.size() > 0;
    }


    @Override
    public boolean onClick(TextView view, Uri uri, MenuItem item)
    {
        new ForEach<>(actionForUri(uri)).process(
                action -> getContext().startActivity(new Intent(action).setData(uri)));
        return false;
    }


    private static Optional<String> actionForUri(Uri uri)
    {
        if ("http".equals(uri.getScheme()) || "https".equals(uri.getScheme()))
        {
            return new Present<>(Intent.ACTION_VIEW);
        }
        else if ("mailto".equals(uri.getScheme()))
        {
            return new Present<>(Intent.ACTION_SENDTO);
        }
        else if ("tel".equals(uri.getScheme()))
        {
            return new Present<>(Intent.ACTION_DIAL);
        }
        return absent();
    }


    private static int titleForAction(String action)
    {
        switch (action)
        {
            case Intent.ACTION_DIAL:
                return R.string.opentasks_actionmode_call;
            case Intent.ACTION_SENDTO:
                return R.string.opentasks_actionmode_mail_to;
            case Intent.ACTION_VIEW:
                return R.string.opentasks_actionmode_open;
        }
        return -1;
    }


    /**
     * Insert an empty item at the given position. Nothing will be inserted if the check list already contains an empty item at the given position. The new (or
     * exiting) emtpy item will be focused and the keyboard will be opened.
     *
     * @param withCheckBox
     * @param pos
     */
    private void insertItem(boolean withCheckBox, int pos, String initialText)
    {
        if (mCurrentValue.size() > pos && mCurrentValue.get(pos).text.length() == 0)
        {
            // there already is an empty item at this pos focus it and return
            View view = mContainer.getChildAt(pos);
            ((EditText) view.findViewById(android.R.id.title)).setText(initialText);
            focusTitle(view);
            return;
        }
        mContainer.clearFocus();

        // create a new empty item
        DescriptionItem item = new DescriptionItem(withCheckBox, false, initialText);
        mCurrentValue.add(pos, item);
        View newItem = createItemView();
        bindItemView(newItem, item);

        // append it to the list
        mContainer.addDragView(newItem, newItem.findViewById(R.id.drag_handle), pos);

        focusTitle(newItem);
    }


    @Override
    public void onClick(View v)
    {
        int id = v.getId();
        if (id == R.id.add_item)
        {
            insertItem(!mCurrentValue.isEmpty(), mCurrentValue.size(), "");
        }
    }


    /**
     * Focus the title element of the given view and open the keyboard if necessary.
     *
     * @param view
     */
    private void focusTitle(View view)
    {
        View titleView = view.findViewById(android.R.id.title);
        if (titleView != null)
        {
            titleView.requestFocus();
            mImm.showSoftInput(titleView, InputMethodManager.SHOW_IMPLICIT);
        }
    }


    private void addActionView(View itemView, DescriptionItem item)
    {
        // attach the action view
        ((ViewGroup) itemView.findViewById(R.id.action_bar)).addView(mActionView);
        mActionView.findViewById(R.id.delete).setOnClickListener((view -> {
            mCurrentValue.remove(item);
            mContainer.removeDragView(itemView);
            mAdapter.validateAndSet(mValues, mCurrentValue);
        }));
    }


    private void setupActionView(DescriptionItem item)
    {
        TextView toggleCheckableButton = mActionView.findViewById(R.id.toggle_checkable);
        toggleCheckableButton.setText(item.checkbox ? R.string.opentasks_hide_tick_box : R.string.opentasks_show_tick_box);
        toggleCheckableButton.setCompoundDrawablesWithIntrinsicBounds(item.checkbox ? R.drawable.ic_text_24px : R.drawable.ic_list_24px, 0, 0, 0);
        toggleCheckableButton.setOnClickListener(button -> {
            int idx = mCurrentValue.indexOf(item);
            int origidx = idx;
            mCurrentValue.remove(item);
            if (!item.checkbox)
            {
                String[] lines = item.text.split("\n");

                if (lines.length == 1)
                {
                    DescriptionItem newItem = new DescriptionItem(true, item.checked, item.text);
                    mCurrentValue.add(idx, newItem);
                    new Animated(mContainer.getChildAt(origidx), v -> (ViewGroup) v).process(v -> bindItemView(v, newItem));
                    setupActionView(newItem);
                }
                else
                {
                    for (String i : lines)
                    {
                        DescriptionItem newItem = new DescriptionItem(true, false, i);
                        mCurrentValue.add(idx, newItem);
                        if (idx == origidx)
                        {
                            new Animated(mContainer.getChildAt(origidx), v -> (ViewGroup) v).process(v -> bindItemView(v, newItem));
                        }
                        else
                        {
                            View itemView = createItemView();
                            bindItemView(itemView, newItem);
                            mContainer.addDragView(itemView, itemView.findViewById(R.id.drag_handle), idx);
                        }

                        idx += 1;
                    }
                }
            }
            else
            {
                DescriptionItem newItem = new DescriptionItem(false, item.checked, item.text);
                mCurrentValue.add(idx, newItem);
                if (idx == 0 || mCurrentValue.get(idx - 1).checkbox)
                {
                    new Animated(mContainer.getChildAt(idx), v -> (ViewGroup) v).process(v -> bindItemView(v, newItem));
                }
                setupActionView(newItem);
            }
            mAdapter.validateAndSet(mValues, mCurrentValue);

            if (mCurrentValue.size() > 0)
            {
                setupActionView(mCurrentValue.get(Math.min(origidx, mCurrentValue.size() - 1)));
            }
        });

        mActionView.postDelayed(
                () -> mActionView.requestRectangleOnScreen(new Rect(0, 0, mActionView.getWidth(), mActionView.getHeight()), false), 1);

    }


    public static final class Animated implements Procedure<Procedure<? super View>>
    {
        private final View mView;
        private final Function<View, ViewGroup> mViewGroupFunction;


        public Animated(View view, Function<View, ViewGroup> viewGroupFunction)
        {
            mView = view;
            mViewGroupFunction = viewGroupFunction;
        }


        @Override
        public void process(Procedure<? super View> arg)
        {
            LayoutTransition transition = mViewGroupFunction.value(mView).getLayoutTransition();
            transition.enableTransitionType(LayoutTransition.CHANGE_APPEARING);
            transition.enableTransitionType(LayoutTransition.CHANGE_DISAPPEARING);
            transition.enableTransitionType(LayoutTransition.CHANGING);
            transition.enableTransitionType(LayoutTransition.APPEARING);
            transition.enableTransitionType(LayoutTransition.DISAPPEARING);
            arg.process(mView);
            transition.disableTransitionType(LayoutTransition.CHANGE_APPEARING);
            transition.disableTransitionType(LayoutTransition.CHANGE_DISAPPEARING);
            transition.disableTransitionType(LayoutTransition.CHANGING);
            transition.disableTransitionType(LayoutTransition.APPEARING);
            transition.disableTransitionType(LayoutTransition.DISAPPEARING);
        }
    }
}
