package com.onus.demotest.threadpool;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

public interface CVExecutorService extends ExecutorService
{

	boolean remove(Runnable command);

	BlockingQueue<Runnable> getQueue();
}
