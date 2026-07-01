package com.onus.demotest.threadpool;

/**
 * Description
 *
 * @author xiandongluo
 * @see
 * @since 2020/10/10
 */
public interface PriorityComparable
{

	/**
	 * Priority values. Requests will be processed from higher priorities to
	 * lower priorities, in FIFO order.
	 */
	enum Priority
	{
		LOW,
		NORMAL,
		HIGH,
		IMMEDIATE
	}

	Priority priority();

	int sequence();
}
