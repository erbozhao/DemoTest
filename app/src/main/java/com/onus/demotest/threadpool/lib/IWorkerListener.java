package com.onus.demotest.threadpool.lib;

/**
 * Created by niuniuyang on 2020-07-14. Description
 */
public interface IWorkerListener
{

	/**
	 * 监控线程创建
	 *
	 * @param workerId 返回workerId，用于区别不同线程
	 */
	void onWorkerAdded(String workerId);

	/**
	 * 监控线程退出
	 *
	 * @param workerId 返回workerId，用于区别不同线程
	 */
	void onWorkerExited(String workerId);
}
