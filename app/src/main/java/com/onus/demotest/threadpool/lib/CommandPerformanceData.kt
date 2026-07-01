package com.onus.demotest.threadpool.lib;

/**
 * Created by niuniuyang on 2020-07-11. Description
 *
 * 用于任务执行对数据收集，数据监控和评估使用
 */
public class CommandPerformanceData
{

	public long	startExeTime	= 0;
	public long	queueTime		= 0;
	public long	completeTime	= 0;
	public int	maxRunningCount	= 0;	//并发任务数的峰值，也是对于线程并发消耗的峰值

	/**
	 * 任务排队耗时,单位ms
	 */
	public long getQueueTimeCost()
	{
		return startExeTime - queueTime;
	}

	/**
	 * 任务执行耗时,单位ms
	 */
	public long getExeTimeCost()
	{
		return completeTime - startExeTime;
	}

	/**
	 * 任务完成总耗时，单位ms
	 */
	public long getTotalTimeCost()
	{
		return completeTime - queueTime;
	}
}
