package com.onus.demotest.threadpool.lib;

import android.os.SystemClock;

/**
 * Created by niuniuyang on 2020-07-11. Description
 *
 * 用于单测的mock
 */
public class SystemTimeProvider
{

	public long currentTime()
	{
		return SystemClock.elapsedRealtime();
	}
}
