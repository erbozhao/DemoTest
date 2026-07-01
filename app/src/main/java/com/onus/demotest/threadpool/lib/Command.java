package com.onus.demotest.threadpool.lib;

/**
 * Created by niuniuyang on 2020-06-20. Description
 *
 * Runnable的包装，Command用于对接CommandThreadPoolExecutor，
 *
 * Command对开发者不可见，开发者只需要关心Runnable，保证新线程池对现有业务的兼容
 *
 * 开发者通过 {@link CommandPool#execute(Runnable)} 方法执行任务，最终会包装成Command
 *
 * 投递给{@link CommandThreadPoolExecutor#execute(Command)}
 */
public class Command
{

	public ICommandPool				commandPool;
	public Runnable					runnable;
	public CommandPerformanceData	performanceData	= new CommandPerformanceData();
	public int								priority;

	Command(Runnable runnable, ICommandPool commandPool, int priority)
	{
		this.runnable = runnable;
		this.commandPool = commandPool;
		this.priority = priority;
	}

	public void recordQueueTime(long l)
	{
		performanceData.queueTime = l;
	}

	public void recordStartExeTime(long l)
	{
		performanceData.startExeTime = l;
	}

	public void recordCompletedTime(long l)
	{
		performanceData.completeTime = l;
	}
}
