package com.onus.demotest.threadpool;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;

/**
 * UI线程线程池
 * 
 * @xiandongluo
 */
public class CVMainThreadExecutor implements Executor
{
	private Handler mHandler;

	public CVMainThreadExecutor()
	{
		mHandler = new Handler(Looper.getMainLooper());
	}

	@Override
	public void execute(Runnable command)
	{
		mHandler.post(command);
	}

	public void execute(Runnable command, long delay)
	{
		mHandler.postDelayed(command, delay);
	}

	public void remove(Runnable command)
	{
		mHandler.removeCallbacks(command);
	}
}
