package com.onus.demotest.threadpool.lib;

/**
 * Created by niuniuyang on 2020-07-27. Description
 */
public interface RunnableListener
{

	/**
	 * 任务开始执行
	 */
	void beforeExecute(Runnable runnable);

	/**
	 * 任务结束执行
	 */
	void afterExecute(Runnable runnable);
}
