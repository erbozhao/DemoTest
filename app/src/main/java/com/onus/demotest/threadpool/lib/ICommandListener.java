package com.onus.demotest.threadpool.lib;

/**
 * Created by niuniuyang on 2020-07-10.
 *
 * Description 用于外部监控任务执行的状态
 */
public interface ICommandListener
{

	/**
	 * 单个任务开始执行的通知
	 */
	void onCmdStart(CommandPool commandPool, Command command);

	/**
	 * 单个任务执行完成的通知
	 */
	void onCmdCompleted(CommandPool commandPool, Command command);

	/**
	 * 任务池的任务最大排队数量的变化通知，用于统计任务排队峰值情况
	 *
	 * @param commandPool 对应的任务池
	 * @param maxQueueSize 最大峰值的排队任务数量
	 */
	void onQueueMaxSizeChanged(CommandPool commandPool, int maxQueueSize);
}
