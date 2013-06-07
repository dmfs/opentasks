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

package org.dmfs.tasks.utils;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.annotation.TargetApi;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;


/**
 * A helper to detect fling gestures on list view items.
 * 
 * Ensure there is no other {@link OnScrollListener} and no other {@link OnTouchListener} set for the {@link ListView}, otherwise things might break.
 * 
 * <p>
 * TODO: add support for flinging to the left.
 * </p>
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public class FlingDetector implements OnTouchListener, OnScrollListener
{

	private final int mMinimumFlingVelocity;
	private final int mMaximumFlingVelocity;
	private final int mTouchSlop;
	private final ListView mListView;
	private float mDownX;
	private float mDownY;
	private boolean mFlinging;
	private boolean mFlingEnabled;
	private int mDownItemPos;
	private View mDownChildView;
	private VelocityTracker mVelocityTracker;

	/**
	 * The {@link OnFlingListener} no notify on fling events.
	 */
	private OnFlingListener mListener;

	/**
	 * The listener interface for fling events.
	 * 
	 * @author Marten Gajda <marten@dmfs.org>
	 */
	public interface OnFlingListener
	{
		/**
		 * Return <code>true</code> if flinging is allowed for the item at position <code>pos</code> in {@link ListView} <code>listview</code>
		 * 
		 * @param listview
		 *            The parent {@link ListView} of the element that is about to be flung.
		 * @param pos
		 *            The position of the item that is about to be flung.
		 * @return <code>true</code> if flinging is allowed for this item, <code>false</code> otherwise.
		 */
		public boolean canFling(ListView listview, int pos);


		/**
		 * Notify the listener of a fling event.
		 * 
		 * @param listview
		 *            The parent {@link ListView} of the element that was flung.
		 * @param pos
		 *            The position of the item that was flung.
		 * @return <code>true</code> if the event has been handled, <code>false</code> otherwise.
		 */
		public boolean onFling(ListView listview, int pos);
	}


	/**
	 * Create a new {@link FlingDetector} for the given {@link ListView}.
	 * 
	 * @param listview
	 *            The {@link ListView}.
	 */
	public FlingDetector(ListView listview)
	{
		listview.setOnTouchListener(this);
		listview.setOnScrollListener(this);
		mListView = listview;

		ViewConfiguration vc = ViewConfiguration.get(listview.getContext());
		mTouchSlop = vc.getScaledTouchSlop();

		mMinimumFlingVelocity = vc.getScaledMinimumFlingVelocity() * 32; // we want the user to fling harder!
		mMaximumFlingVelocity = vc.getScaledMaximumFlingVelocity();

	}


	@Override
	public boolean onTouch(View v, MotionEvent event)
	{
		boolean handled = false;
		switch (event.getActionMasked())
		{
			case MotionEvent.ACTION_DOWN:
				// store down position
				mDownX = event.getX();
				mDownY = event.getY();

				// get the child that was tapped
				int mDownChildPos = getChildPosByCoords(mDownX, mDownY);

				if (mDownChildPos >= 0)
				{
					mDownItemPos = mDownChildPos + mListView.getFirstVisiblePosition();
					mDownChildView = mListView.getChildAt(mDownChildPos);

					mFlingEnabled = mDownChildView != null && mListener != null && mListener.canFling(mListView, mDownItemPos);

					if (mFlingEnabled)
					{
						if (mVelocityTracker == null)
						{
							// get a new VelocityTracker
							mVelocityTracker = VelocityTracker.obtain();
						}
						mVelocityTracker.addMovement(event);
						mFlinging = false;
						/*
						 * don't set handled = true, that would stop the touch event making it impossible to select a flingable list element
						 */
					}
				}
				else
				{
					// no child at that coordinates, nothing to fling
					mFlingEnabled = false;
				}
				break;

			case MotionEvent.ACTION_MOVE:
				if (mFlingEnabled)
				{
					mVelocityTracker.addMovement(event);
					float deltaX = Math.abs(event.getX() - mDownX);
					float deltaY = Math.abs(event.getY() - mDownY);

					// start flinging when the finger has moved at least mTouchSlop pixels and has moved mostly along the in x-axis
					mFlinging |= deltaX > mTouchSlop && deltaX > deltaY * 3;

					if (mFlinging)
					{
						translateView(mDownChildView, Math.max(0, (event.getX() - mDownX)));
						mListView.requestDisallowInterceptTouchEvent(true);

						// cancel the touch event for the listview, otherwise it might detect a "press and hold" event and highlight the view
						MotionEvent cancelEvent = MotionEvent.obtain(event);
						cancelEvent.setAction(MotionEvent.ACTION_CANCEL | (event.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
						mListView.onTouchEvent(cancelEvent);
						cancelEvent.recycle();

						handled = true;
					}
				}
				break;

			case MotionEvent.ACTION_UP:
				if (mFlinging)
				{
					mVelocityTracker.addMovement(event);

					// compute velocity in ms
					mVelocityTracker.computeCurrentVelocity(1);
					float deltaX = Math.abs(event.getX() - mDownX);

					if (mMinimumFlingVelocity < mVelocityTracker.getXVelocity() * 1000 && mVelocityTracker.getXVelocity() * 1000 < mMaximumFlingVelocity
						&& deltaX > mTouchSlop)
					{
						animateFling(mDownChildView, mDownItemPos, mVelocityTracker.getXVelocity());
					}
					else
					{
						// didn't fling hard enough
						resetView(mDownChildView);
					}

					mVelocityTracker.clear();
					mFlingEnabled = false;
					mFlinging = false;
					handled = true;
				}
				else if (mFlingEnabled)
				{
					// fling was enabled, but the user didn't fling actually
					mVelocityTracker.clear();
					mFlingEnabled = false;
				}
				break;

			case MotionEvent.ACTION_CANCEL:
				if (mFlinging)
				{
					resetView(mDownChildView);
					mVelocityTracker.clear();
					mFlingEnabled = false;
					handled = true;
				}
				else if (mFlingEnabled)
				{
					// fling was enabled, but the user didn't fling actually
					mVelocityTracker.clear();
					mFlingEnabled = false;
				}
				break;
		}
		return handled;
	}


	/**
	 * Set the {@link OnFlingListener} that is notified when the user flings an item view.
	 * 
	 * @param listener
	 *            The {@link OnFlingListener}.
	 */
	public void setOnFlingListener(OnFlingListener listener)
	{
		mListener = listener;
	}


	/**
	 * The the position from top of the child view at (x,y).
	 * 
	 * @param x
	 *            The position on the x-axis;
	 * @param y
	 *            The position on the y-axis;
	 * @return The position from top of the child at (x,y) or -1 if there is no child at this position.
	 */
	private int getChildPosByCoords(float x, float y)
	{
		int count = mListView.getChildCount();
		Rect rect = new Rect();
		for (int i = 0; i < count; i++)
		{
			View child = mListView.getChildAt(i);
			child.getHitRect(rect);
			if (rect.contains((int) x, (int) y))
			{
				return i;
			}
		}
		return -1;
	}


	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
	{
		// nothing to do
	}


	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState)
	{
		// disable flinging if scrolling starts
		mFlingEnabled &= scrollState == OnScrollListener.SCROLL_STATE_IDLE;
	}


	/**
	 * Translate a {@link View} to the given translation.
	 * 
	 * @param v
	 *            The {@link View}.
	 * @param translation
	 *            The translation.
	 */
	@TargetApi(14)
	private void translateView(View v, float translation)
	{
		// At present we don't animate on SDK levels < 14, so there is nothing do here
		// TODO: add support for older APIs
		if (android.os.Build.VERSION.SDK_INT >= 14 && v != null)
		{
			v.setTranslationX(translation);
			v.setAlpha(1 - translation / v.getWidth());
		}
	}


	/**
	 * Animate the fling of the given {@link View} at position <code>pos</code> and calls the onFling handler when the animation has finished.
	 * 
	 * @param v
	 *            The {@link View} to fling.
	 * @param pos
	 *            The position of the element in ListView.
	 * @param velocity
	 *            The velocity to use. The harder you fling the faster the animation will be.
	 */
	@TargetApi(14)
	private void animateFling(final View v, final int pos, float velocity)
	{
		if (android.os.Build.VERSION.SDK_INT >= 14 && v != null)
		{
			int parentWidth = ((View) v.getParent()).getWidth();

			if (parentWidth > v.getTranslationX()) // otherwise there is nothing to animate
			{
				v.animate().alpha(0).translationX(parentWidth).setDuration((long) ((parentWidth - v.getTranslationX()) / velocity))
					.setListener(new AnimatorListener()
					{

						@Override
						public void onAnimationStart(Animator animation)
						{
							// nothing to do
						}


						@Override
						public void onAnimationRepeat(Animator animation)
						{
							// nothing to do
						}


						@Override
						public void onAnimationEnd(Animator animation)
						{
							if (mListener != null)
							{
								// notify listener
								if (!mListener.onFling(mListView, pos))
								{
									// the event was not handled, so reset the view
									resetView(v);
								}
							}
						}


						@Override
						public void onAnimationCancel(Animator animation)
						{
							if (mListener != null)
							{
								// notify listener
								if (!mListener.onFling(mListView, pos))
								{
									// the event was not handled, so reset the view
									resetView(v);
								}
							}
						}
					}).start();
			}
		}
		else
		{
			// on older APIs we just call the listener without animation
			// TODO: add animation for older APIs
			if (mListener != null)
			{
				if (!mListener.onFling(mListView, pos))
				{
					// the event was not handled, so reset the view
					resetView(v);
				}
			}
		}
	}


	/**
	 * Reset {@link View} <code>v</code> to its original position. If possible, this is done using an animation.
	 * 
	 * @param v
	 *            The {@link View} to reset.
	 */
	@TargetApi(14)
	private void resetView(View v)
	{
		// At present we don't animate on SDK levels < 14, so there is nothing to reset
		// TODO: add reset code for older APIs once we animate them
		if (android.os.Build.VERSION.SDK_INT >= 14 && v != null)
		{
			v.animate().translationX(0).alpha(1).setDuration(100).setListener(null /* unset any previous listener! */).start();
		}
	}
}
