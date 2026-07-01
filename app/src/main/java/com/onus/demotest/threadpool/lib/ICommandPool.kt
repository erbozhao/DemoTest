package com.onus.demotest.threadpool.lib;

/**
 * Created by niuniuyang on 2020-06-14. Description
 */
interface ICommandPool extends Comparable<ICommandPool>
{

	/**
	 * 任务执行完毕后，会向{@link CommandPool 获取正在等待执行的任务}
	 */
	Command takeWaitingCommand();

	/**
	 * 任务执行完毕后，会通知CommandPool，修改{@link CommandPool# runningCount}
	 */
	void onCmdExecuted(Command command);

	/**
	 * 每一个{@link CommandPool} 都会有一个最大线程数量，
	 *
	 * 用于{@link CommandThreadPoolExecutor#registerCommandPool(ICommandPool)}
	 *
	 * 工作线程池的动态扩容
	 */
	int getMaximumThreadSize();

	/**
	 * 任务被拒绝，shutDown的时候，可能有任务还未执行
	 */
	void onReject(Command command);

	/**
	 * 返回任务池的名字
	 */
	String getName();

	/**
	 * 执行前的通知，主要用于数据上报
	 */
	void beforeExecute(Command command);

	/**
	 * 执行完成后的通知，主要用于数据上报
	 */
	void afterExecute(Command command);

	/**
	 * 任务池已经终止
	 */
	boolean isTerminated();
}
