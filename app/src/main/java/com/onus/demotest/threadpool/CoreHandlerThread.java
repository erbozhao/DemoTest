package com.onus.demotest.threadpool;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

/**
 * 核心框架层使用的HandlerThread --- 框架处理核心的逻辑
 * 此线程主要用来框架核心层来独立于主线程抛消息
 * !!!!外部业务禁止使用!!!!
 * !!!!外部业务禁止使用!!!!
 * !!!!外部业务禁止使用!!!!
 * 
 * @Author: xiandongluo
 * @Date: 2020/12/21
 */
public class CoreHandlerThread extends HandlerThread
{

	private volatile static CoreHandlerThread	sInstance	= null;

	/**
	 * Handler
	 */
	private Handler								mHandler;

	/**
	 * !!!!只服务于底层框架，外部业务禁止使用!!!!
	 * 获取单例实例
	 * 
	 * @return
	 */
	public static CoreHandlerThread getInstance()
	{
		if (sInstance == null)
		{
			synchronized (CoreHandlerThread.class)
			{
				if (sInstance == null)
				{
					sInstance = new CoreHandlerThread();
				}
			}
		}
		return sInstance;
	}

	/**
	 * 私有化构造方法
	 */
	private CoreHandlerThread()
	{
		super("CoreHandlerThread");
		start();
		mHandler = new Handler(getLooper());
	}

	/**
	 * 获取Looper
	 * 不允许执行耗时任务
	 * !!!!只服务于底层框架，外部业务禁止使用!!!!
	 */
	@Override
	public Looper getLooper()
	{
		return super.getLooper();
	}

	/**
	 * 运行异步任务
	 * 不允许执行耗时任务
	 * !!!!只服务于底层框架，外部业务禁止使用!!!!
	 * 
	 * @param runnable
	 */
	public void post(Runnable runnable)
	{
		mHandler.post(runnable);
	}

	/**
	 * 运行异步任务
	 * 不允许执行耗时任务
	 * !!!!只服务于底层框架，外部业务禁止使用!!!!
	 * 
	 * @param runnable
	 * @param delayMillis
	 */
	public void postDelayed(Runnable runnable, long delayMillis)
	{
		mHandler.postDelayed(runnable, delayMillis);
	}

	/**
	 * 移除任务
	 * !!!!只服务于底层框架，外部业务禁止使用!!!!
	 * 
	 * @param runnable
	 */
	public void removeCallbacks(Runnable runnable)
	{
		mHandler.removeCallbacks(runnable);
	}
}
