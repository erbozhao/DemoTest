package com.onus.demotest.threadpool.lib;


/**
 * Created by niuniuyang on 2019/6/10. Description
 *
 * 业务如果需要关心任务的优先级，任务可以直接集成PriorityRunnable
 *
 * 使用PriorityRunnable ，必须调用{@link CommandPool#setCommandQueue} 设置 PriorityQueue
 */
public abstract class PriorityRunnable implements Runnable, Comparable<PriorityRunnable>
{

	private long priority = 0;

	public PriorityRunnable()
	{

	}

	public long getPriority()
	{
		return priority;
	}

	/**
	 * 优先级对值越大，任务优先级越高，针对排队对任务 {@link CommandPool#updatePriority(Runnable)}
	 */
	public void setPriority(long priority)
	{
		this.priority = priority;
	}

	/**
	 * this 和 o的对比，mPriority越大就越靠前，return -1，表示排在前面
	 */
	@Override
	public int compareTo(PriorityRunnable o)
	{
		long l = priority - o.priority;
		if (l > 0)
		{
			return -1;
		}
		else if (l < 0)
		{
			return 1;
		}
		else
		{
			return 0;
		}
	}
}
