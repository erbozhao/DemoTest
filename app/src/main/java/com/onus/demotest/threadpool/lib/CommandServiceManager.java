package com.onus.demotest.threadpool.lib;

import java.util.List;

/**
 * Created by niuniuyang on 2020-07-31. Description
 *
 * 单例管理CommandPoolSupplier，一个app进程里面，推荐使用一个CommandPoolSupplier
 *
 * 这里一个app进程才能更大复用线程池资源
 *
 * 当然根据业务的需要，你可以不使用这个单例，直接创建CommandPoolSupplier
 */
public class CommandServiceManager
{

	private static volatile CommandServiceManager	instance	= null;
	private CommandPoolSupplier						commandPoolSupplier;

	private CommandServiceManager()
	{
		commandPoolSupplier = new CommandPoolSupplier();
	}

	public static CommandServiceManager get()
	{
		if (instance == null)
		{
			synchronized (CommandServiceManager.class)
			{
				if (instance == null)
				{
					instance = new CommandServiceManager();
				}
			}
		}
		return instance;
	}

	/**
	 * 返回一个CommandPoolSupplier，用于创建CommandPool
	 */
	public CommandPoolSupplier getCommandSupplier()
	{
		return commandPoolSupplier;
	}

	public void shutdown()
	{
		commandPoolSupplier.shutdown();
		synchronized (CommandServiceManager.class)
		{
			instance = null;
		}
	}

	public List<Command> shutdownNow()
	{
		synchronized (CommandServiceManager.class)
		{
			instance = null;
		}
		return commandPoolSupplier.shutdownNow();
	}

}
