package com.onus.demotest.threadpool.lib;

import android.os.Process;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by niuniuyang on 2020/6/9.
 *
 * 线程创建工厂,主要用户线程命名和优先级
 */
public class CommandThreadFactory implements ThreadFactory
{

	private final AtomicInteger	threadNumber	= new AtomicInteger(1);
	private int					priority;
	private String				threadPoolName;

	public CommandThreadFactory(String threadPoolName)
	{
		this(threadPoolName, Process.THREAD_PRIORITY_DEFAULT);
	}

	/**
	 * 构造线程工厂
	 *
	 * @param threadPoolName 线程池的主名
	 * @param priority 线程优先级
	 */
	public CommandThreadFactory(String threadPoolName, int priority)
	{
		this.threadPoolName = threadPoolName;
		this.priority = priority;
	}

	@Override
	public Thread newThread(Runnable r)
	{
		Thread thread = new Thread(r, getThreadName())
		{
			@Override
			public void run()
			{
				try
				{
					Process.setThreadPriority(priority);
				}
				catch (Throwable e)
				{
				}
				super.run();
			}
		};
		return thread;
	}

	protected String getThreadName()
	{
		return "TP-" + threadPoolName + "-" + threadNumber.getAndIncrement();
	}
}
