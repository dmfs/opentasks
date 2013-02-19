package org.dmfs.tasks.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;


public class TimeChangeManager extends BroadcastReceiver
{
	private final TimeUpdateListener mListener;
	private Handler mHandler;


	public TimeChangeManager(Context context, TimeUpdateListener listener)
	{
		IntentFilter intentFilter = new IntentFilter(Intent.ACTION_TIME_CHANGED);
		intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
		context.registerReceiver(this, intentFilter);
		mListener = listener;
	}


	@Override
	public void onReceive(Context context, Intent intent)
	{
		mListener.onTimeUpdate(this);
	}


	public void setNextAlarm(long alarm)
	{
		if (mHandler == null)
		{
			mHandler = new Handler();
		}

		mHandler.removeCallbacks(mNotifier);
		mHandler.postAtTime(mNotifier, alarm - System.currentTimeMillis() + android.os.SystemClock.uptimeMillis());
	}

	private final Runnable mNotifier = new Runnable()
	{

		@Override
		public void run()
		{
			mListener.onTimeUpdate(TimeChangeManager.this);
		}

	};
}
