package com.onus.demotest.threadpool;



/**
 * Description
 *
 * @author xiandongluo
 * @see
 * @since 2020/10/10
 */
public interface CVRejectedExecutionHandler
{

	void rejectedExecution(Runnable r, CVThreadPoolExecutor executor);

}
