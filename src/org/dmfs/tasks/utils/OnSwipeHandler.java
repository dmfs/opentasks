package org.dmfs.tasks.utils;

import android.annotation.TargetApi;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;


public class OnSwipeHandler implements OnTouchListener, OnScrollListener
{

	public final static float MIN_VELOCITY = 1.3f;

	private ListView mSwipeView;
	private float mDownX;
	private float mDownY;
	private float mLastX;
	private float mLastY;
	private long mLastTime;
	private float mLastX2;
	private float mLastY2;
	private long mLastTime2;
	private boolean mInSwipe;
	private boolean mSwipeEnabled;
	private boolean mScrolling = false;
	private int mDownPos;
	private View mDownView;

	private OnSwipeListener mListener;


	public OnSwipeHandler(ListView v)
	{
		v.setOnTouchListener(this);
		v.setOnScrollListener(this);
		mSwipeView = v;
	}


	@Override
	public boolean onTouch(View v, MotionEvent event)
	{
		switch (event.getActionMasked())
		{
			case MotionEvent.ACTION_DOWN:
				mDownX = mLastX = event.getX();
				mDownY = mLastY = event.getY();
				mLastTime = event.getDownTime();
				mDownPos = getDownPos(mDownX, mDownY);
				mDownView = mSwipeView.getChildAt(mDownPos - mSwipeView.getFirstVisiblePosition());
				mSwipeEnabled = mDownView != null && mListener != null && mListener.allowSwipe(mSwipeView, mDownPos) && !mScrolling;
				Log.v("#######################", "down " + mListener.allowSwipe(mSwipeView, mDownPos));
				break;
			case MotionEvent.ACTION_MOVE:
				if (mSwipeEnabled)
				{
					mInSwipe = Math.abs(event.getX() - mDownX) > Math.abs(event.getY() - mDownY) * 2 && mListener != null;

					if (mInSwipe)
					{
						mLastX2 = mLastX;
						mLastY2 = mLastY;
						mLastTime2 = mLastTime;

						mLastX = event.getX();
						mLastY = event.getY();
						mLastTime = event.getEventTime();
						animateView(mDownView, Math.max(0, (mLastX - mDownX)));
						mSwipeView.requestDisallowInterceptTouchEvent(true);

						MotionEvent cancelEvent = MotionEvent.obtain(event);
						cancelEvent.setAction(MotionEvent.ACTION_CANCEL | (event.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
						mSwipeView.onTouchEvent(cancelEvent);
						cancelEvent.recycle();

					}
					Log.v("#######################", "move" + mLastTime);
				}
				break;
			case MotionEvent.ACTION_UP:
				if (mSwipeEnabled)
				{
					Log.v("#######################", "up" + event.getEventTime());
					Log.v("#######################", "up" + +(event.getX() - mLastX2) / (event.getEventTime() - mLastTime2));

					if (mInSwipe)
					{
						Long time = event.getEventTime();
						if ((event.getX() - mLastX2) / (time - mLastTime2) > MIN_VELOCITY)
						{
							Log.v("#######################", "swipe detected ");
							mListener.onSwipe(mSwipeView, mDownPos, (event.getX() - mLastX2) / (time - mLastTime2));
							mInSwipe = false;
							return true;
						}
					}
					resetView(mDownView);
					mInSwipe = false;
				}
				break;
			case MotionEvent.ACTION_CANCEL:
				resetView(mDownView);
				mInSwipe = false;
				break;
		}
		return false;
	}


	public void setOnSwipeListener(OnSwipeListener listener)
	{
		mListener = listener;
	}


	private int getDownPos(float x, float y)
	{
		int count = mSwipeView.getChildCount();
		Rect rect = new Rect();
		for (int i = 0; i < count; i++)
		{
			View child = mSwipeView.getChildAt(i);
			child.getHitRect(rect);
			if (rect.contains((int) x, (int) y))
			{
				return mSwipeView.getFirstVisiblePosition() + i;
			}
		}
		return -1;
	}

	public interface OnSwipeListener
	{
		public boolean allowSwipe(ListView v, int pos);


		public boolean onSwipe(ListView v, int pos, float velocity);
	}


	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
	{
	}


	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState)
	{
		mScrolling = scrollState != OnScrollListener.SCROLL_STATE_IDLE;
		mSwipeEnabled &= !mScrolling;
	}


	@TargetApi(14)
	private void animateView(View v, float translate)
	{
		if (android.os.Build.VERSION.SDK_INT >= 14 && v != null)
		{
			v.setTranslationX(translate);
			v.setAlpha(1 - translate / v.getWidth());
		}
	}


	@TargetApi(14)
	private void resetView(View v)
	{
		if (android.os.Build.VERSION.SDK_INT >= 14 && v != null)
		{
			v.animate().translationX(0).alpha(1).setDuration(100).setListener(null).start();
		}
	}
}
