package com.onus.demotest.threadpool.lib;

/**
 * A handler for tasks that cannot be executed by a
 * {@link CommandPool#onReject(Command)}.
 *
 * @author niuniuyang
 */
public interface CommandRejectedExecutionHandler
{

	/**
	 * 任务被拒绝，shutDown的时候，可能有任务还未执行
	 *
	 * @param r the runnable task requested to be executed
	 * @param commandPool 任务所在的任务池
	 * @throws CommandRejectedExecutionHandler if there is no remedy
	 */
	void rejectedExecution(Runnable r, CommandPool commandPool);
}
