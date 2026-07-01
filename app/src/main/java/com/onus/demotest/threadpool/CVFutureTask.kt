package com.onus.demotest.threadpool;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * Description
 *
 * @author xiandongluo
 * @see
 * @since 2020/10/10
 */
public class CVFutureTask<V> extends FutureTask<V>
{
	public Object task;

	public CVFutureTask(Callable<V> callable)
	{
		super(callable);
		task = callable;
	}

	public CVFutureTask(Runnable runnable, V result)
	{
		super(runnable, result);
		task = runnable;
	}


}
