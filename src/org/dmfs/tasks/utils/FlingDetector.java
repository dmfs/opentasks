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
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Vibrator;
import android.view.Gravity;
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
 * @author Marten Gajda <marten@dmfs.org>
 * @author Tobias Reinsch <tobias@dmfs.org>
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
	private View mFlingChildView;
	private View mItemChildView;
	private VelocityTracker mVelocityTracker;
	private int mContentViewId;
	private static Context mContext;
	private static Handler mHandler;

	private int mFlingDirection;

	// A runnable that is used for vibrating to indication a fling start
	private final Runnable mVibrateRunnable = new Runnable()
	{
		@Override
		public void run()
		{
			Vibrator vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
			vibrator.vibrate(VIBRATION_DURATION);

			// if we don't disallow that, fling doesn't work on some devices
			mListView.requestDisallowInterceptTouchEvent(true);
		}
	};

	/** Flag to indicate left direction fling gesture. */
	public static final int LEFT_FLING = 1;

	/** Flag to indicate right direction fling gesture. */
	public static final int RIGHT_FLING = 2;

	/** The the vibration duration in milliseconds for fling start */
	public static final int VIBRATION_DURATION = 25;

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
		 * @return Bitmask with LEFT_FLING or RIGHT_FLING set to indicate directions of fling which are enabled.
		 */
		public int canFling(ListView listview, int pos);


		/**
		 * Notify the listener of a fling event.
		 * 
		 * @param listview
		 *            The parent {@link ListView} of the element that was flung.
		 * @param listElement
		 *            The list element that is flinging
		 * @param pos
		 *            The position of the item that was flung.
		 * @param direction
		 *            Flag to indicate in which direction the fling was performed.
		 * @return <code>true</code> if the event has been handled, <code>false</code> otherwise.
		 */
		public boolean onFlingEnd(ListView listview, View listElement, int pos, int direction);


		/**
		 * Notify the listener of a fling event start or direction change. This method might be called twice or more without a call to
		 * {@link #onFlingEnd(ListView, View, int, int)} or {@link #onFlingCancel(int)} in between when the user changes the flinging direction.
		 * 
		 * @param listview
		 *            The parent {@link ListView} of the element that was flung.
		 * @param listElement
		 *            The list element that is flinging
		 * @param pos
		 *            The position of the item that was flung
		 * @param direction
		 *            Flag to indicate in which direction the fling was performed.
		 */
		public void onFlingStart(ListView listview, View listElement, int position, int direction);


		/**
		 * Notify the listener of a fling event being cancelled.
		 * 
		 * @param direction
		 *            Flag to indicate in which direction the fling was performed.
		 */
		public void onFlingCancel(int direction);
	}


	/**
	 * Create a new {@link FlingDetector} for the given {@link ListView}.
	 * 
	 * @param listview
	 *            The {@link ListView}.
	 */
	public FlingDetector(ListView listview)
	{
		this(listview, -1, null);
	}


	/**
	 * Create a new {@link FlingDetector} for the given {@link ListView}.
	 * 
	 * @param listview
	 *            The {@link ListView}.
	 * 
	 * @param flingContentViewId
	 *            The layout id of the inner content view that is supposed to fling
	 */
	public FlingDetector(ListView listview, int flingContentViewId, Context context)
	{
		listview.setOnTouchListener(this);
		listview.setOnScrollListener(this);
		mListView = listview;
		mContentViewId = flingContentViewId;
		mContext = context;

		ViewConfiguration vc = ViewConfiguration.get(listview.getContext());

		if (android.os.Build.VERSION.SDK_INT <= 10)
		{
			mTouchSlop = vc.getScaledTouchSlop() * 2 / 3;
		}
		else
		{
			mTouchSlop = vc.getScaledTouchSlop();
		}

		mMinimumFlingVelocity = vc.getScaledMinimumFlingVelocity() * 8; // we want the user to fling harder!
		mMaximumFlingVelocity = vc.getScaledMaximumFlingVelocity() * 8;

		mHandler = new Handler();
	}


	@SuppressLint("Recycle")
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

					mItemChildView = mFlingChildView = mListView.getChildAt(mDownChildPos);
					if (mContentViewId != -1)
					{
						mFlingChildView = mFlingChildView.findViewById(mContentViewId);
					}

					mFlingEnabled = mFlingChildView != null && mListener != null && mListener.canFling(mListView, mDownItemPos) > 0;
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

						// start vibration detection
						mHandler.postDelayed(mVibrateRunnable, ViewConfiguration.getTapTimeout());
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
					float deltaX = event.getX() - mDownX;
					float deltaY = event.getY() - mDownY;
					float deltaXabs = Math.abs(deltaX);
					float deltaYabs = Math.abs(deltaY);

					boolean leftFlingEnabled = (mListener.canFling(mListView, mDownItemPos) & LEFT_FLING) == LEFT_FLING;
					boolean rightFlingEnabled = (mListener.canFling(mListView, mDownItemPos) & RIGHT_FLING) == RIGHT_FLING;

					// The user should not move to begin the fling, otherwise the fling is aborted
					if (event.getEventTime() - event.getDownTime() < ViewConfiguration.getTapTimeout() && (deltaXabs > mTouchSlop || deltaYabs > mTouchSlop))
					{
						mFlingEnabled = false;
						mHandler.removeCallbacks(mVibrateRunnable);
						break;
					}

					boolean wasFlinging = mFlinging;

					// start flinging when the finger has moved at least mTouchSlop pixels and has moved mostly along the in x-axis
					mFlinging |= deltaXabs > mTouchSlop && deltaXabs > deltaYabs && ((leftFlingEnabled && deltaX < 0) || (rightFlingEnabled && deltaX > 0))
						&& (event.getEventTime() - event.getDownTime() > ViewConfiguration.getTapTimeout());

					if (mFlinging)
					{

						// inform the the listener when the flinging starts or the direction changes
						if ((!wasFlinging || (mFlingDirection == LEFT_FLING != (deltaX < 0))) && mListener != null)
						{
							mFlingDirection = deltaX < 0 ? LEFT_FLING : RIGHT_FLING;
							mListener.onFlingStart(mListView, mItemChildView, mDownItemPos, mFlingDirection);
						}

						translateView(mFlingChildView, deltaX);
						if (!wasFlinging)
						{
							mListView.requestDisallowInterceptTouchEvent(true);

							// cancel the touch event for the listview, otherwise it might detect a "press and hold" event and highlight the view
							MotionEvent cancelEvent = MotionEvent.obtain(event);
							cancelEvent.setAction(MotionEvent.ACTION_CANCEL | (event.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
							mListView.onTouchEvent(cancelEvent);
							cancelEvent.recycle();
						}
						handled = true;

					}
				}
				break;

			case MotionEvent.ACTION_UP:
				// cancel vibration
				mHandler.removeCallbacks(mVibrateRunnable);

				if (mFlinging)
				{
					mVelocityTracker.addMovement(event);

					// compute velocity in ms
					mVelocityTracker.computeCurrentVelocity(1);
					float deltaX = event.getX() - mDownX;
					float xVelocity = Math.abs(mVelocityTracker.getXVelocity() * 1000);
					if (mMinimumFlingVelocity < xVelocity && xVelocity < mMaximumFlingVelocity && Math.abs(deltaX) > mTouchSlop
						&& deltaX * mVelocityTracker.getXVelocity() > 0)
					{
						animateFling(mFlingChildView, mDownItemPos, mVelocityTracker.getXVelocity());
					}
					else
					{
						// didn't fling hard enough
						resetView(mFlingChildView);
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
				// cancel vibration
				mHandler.removeCallbacks(mVibrateRunnable);

				if (mFlinging)
				{
					resetView(mFlingChildView);
					mVelocityTracker.clear();
					mFlingEnabled = false;
					handled = true;
					if (mListener != null)
					{
						mListener.onFlingCancel(mFlingDirection);
					}
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

		// stop vibration if scrolling starts
		if (!mFlingEnabled)
		{
			mHandler.removeCallbacks(mVibrateRunnable);
		}
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
			// v.setAlpha(1 - Math.abs(translation) / v.getWidth());
		}
		else if (v != null)
		{
			android.view.ViewGroup.LayoutParams layoutParams = v.getLayoutParams();

			if (layoutParams instanceof android.widget.LinearLayout.LayoutParams)
			{
				android.widget.LinearLayout.LayoutParams linearLayoutParams = (android.widget.LinearLayout.LayoutParams) layoutParams;
				linearLayoutParams.setMargins((int) translation, linearLayoutParams.topMargin, -((int) translation), linearLayoutParams.bottomMargin);
				v.setLayoutParams(linearLayoutParams);
			}
			else if (layoutParams instanceof android.widget.RelativeLayout.LayoutParams)
			{
				android.widget.RelativeLayout.LayoutParams relativeLayoutParams = (android.widget.RelativeLayout.LayoutParams) layoutParams;
				relativeLayoutParams.setMargins((int) translation, relativeLayoutParams.topMargin, -((int) translation), relativeLayoutParams.bottomMargin);
				v.setLayoutParams(relativeLayoutParams);
			}
			else if (layoutParams instanceof android.widget.FrameLayout.LayoutParams)
			{
				android.widget.FrameLayout.LayoutParams frameLayoutParams = (android.widget.FrameLayout.LayoutParams) layoutParams;
				frameLayoutParams.setMargins((int) translation, frameLayoutParams.topMargin, -((int) translation), frameLayoutParams.bottomMargin);

				// in frame layout we need to set a gravity for the margins
				frameLayoutParams.gravity = Gravity.LEFT;

				v.setLayoutParams(frameLayoutParams);
			}
			else
			{
				// neither a linear or a relative layout was found, use padding as fall back method
				int paddingTop = v.getPaddingTop();
				int paddingBottom = v.getPaddingBottom();

				v.setPadding((int) translation, paddingTop, -((int) translation), paddingBottom);
			}

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

		final int direction = (velocity < 0) ? LEFT_FLING : RIGHT_FLING;

		if (android.os.Build.VERSION.SDK_INT >= 14 && v != null)
		{

			int parentWidth = ((View) v.getParent()).getWidth();
			final float viewTranslationX = v.getTranslationX();

			if (parentWidth > viewTranslationX) // otherwise there is nothing to animate
			{
				int translationWidth;
				long animationDuration;
				if (viewTranslationX < 0)
				{
					translationWidth = -parentWidth;
					animationDuration = (long) (parentWidth + viewTranslationX);
				}
				else
				{
					translationWidth = parentWidth;
					animationDuration = (long) (parentWidth - viewTranslationX);
				}
				v.animate()
				// .alpha(0)
					.translationX(translationWidth).setDuration((long) (animationDuration / Math.abs(velocity))).setListener(new AnimatorListener()
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

								if (!mListener.onFlingEnd(mListView, mItemChildView, pos, direction))
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
								if (!mListener.onFlingEnd(mListView, mItemChildView, pos, direction))
								{
									// the event was not handled, so reset the view
									resetView(v);
								}
							}
						}
					}).start();
			}
			else if (mListener != null)
			{
				// notify listener
				if (!mListener.onFlingEnd(mListView, mItemChildView, pos, direction))
				{
					// the event was not handled, so reset the view
					resetView(v);
				}
			}
		}
		else
		{
			// on older APIs we just call the listener without animation
			// TODO: add animation for older APIs
			if (mListener != null)
			{
				if (!mListener.onFlingEnd(mListView, mItemChildView, pos, direction))
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
		else if (v != null)
		{
			android.view.ViewGroup.LayoutParams layoutParams = v.getLayoutParams();

			if (layoutParams instanceof android.widget.LinearLayout.LayoutParams)
			{
				android.widget.LinearLayout.LayoutParams linearLayoutParams = (android.widget.LinearLayout.LayoutParams) layoutParams;
				linearLayoutParams.setMargins(0, linearLayoutParams.topMargin, 0, linearLayoutParams.bottomMargin);
				v.setLayoutParams(linearLayoutParams);
			}
			else if (layoutParams instanceof android.widget.RelativeLayout.LayoutParams)
			{
				android.widget.RelativeLayout.LayoutParams relativeLayoutParams = (android.widget.RelativeLayout.LayoutParams) layoutParams;
				relativeLayoutParams.setMargins(0, relativeLayoutParams.topMargin, 0, relativeLayoutParams.bottomMargin);
				v.setLayoutParams(relativeLayoutParams);
			}
			else if (layoutParams instanceof android.widget.FrameLayout.LayoutParams)
			{
				android.widget.FrameLayout.LayoutParams frameLayoutParams = (android.widget.FrameLayout.LayoutParams) layoutParams;
				frameLayoutParams.setMargins(0, frameLayoutParams.topMargin, 0, frameLayoutParams.bottomMargin);
				v.setLayoutParams(frameLayoutParams);
			}
			else
			{
				// neither a linear or a relative layout was found, use padding as fall back method
				int paddingTop = v.getPaddingTop();
				int paddingBottom = v.getPaddingBottom();

				v.setPadding(0, paddingTop, 0, paddingBottom);
			}
		}
	}
}
