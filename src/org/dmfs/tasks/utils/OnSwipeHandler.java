package org.dmfs.tasks.utils;

import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ListView;


public class OnSwipeHandler implements OnTouchListener
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
	private int mDownPos;
	private View mDownView;

	private OnSwipeListener mListener;


	public OnSwipeHandler(ListView v)
	{
		v.setOnTouchListener(this);
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
				mSwipeEnabled = mListener != null && mListener.allowSwipe(mSwipeView, mDownPos);
				Log.v("#######################", "down");
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
						if (mDownView != null)
						{
							mDownView.setTranslationX(mLastX - mDownX);
						}
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
					mDownView.animate().translationX(0).setDuration(100).start();

					mInSwipe = false;
				}
				break;
			case MotionEvent.ACTION_CANCEL:
				mDownView.animate().translationX(0).setDuration(100).start();
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
}
