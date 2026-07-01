package com.onus.demotest.threadpool;

import android.util.Log;

import com.onus.demotest.threadpool.lib.CommandPool;
import com.onus.demotest.threadpool.lib.CommandRejectedExecutionHandler;
import com.onus.demotest.threadpool.lib.CommandServiceManager;
import com.onus.demotest.threadpool.lib.ICommandListener;

import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Description
 *
 * @author xiandongluo
 * @see
 * @since 2020/11/4
 */
public class CVThreadPoolExecutor extends AbstractExecutorService implements CVExecutorService
{

	public enum POOL_PRIORITY
	{
		/**
		 * 最高优先级，定义为UI及时相关的逻辑，如：用户等待中的请求等
		 */
		URGENT_DISPLAY,
		/**
		 * UI展示的第二优先级，主要场景为不影响用户主场景使用的逻辑，如：图片加载
		 */
		DISPLAY,
		/**
		 * 其它低优先级逻辑
		 */
		BACKGROUND
	}

	private CommandPool								mCommandPool;

	private BlockingQueue							mWorkQueue;

	/**
	 * The default rejected execution handler.
	 */
	private static final CVRejectedExecutionHandler	defaultHandler	= new CVRejectedExecutionHandler()
																	{
																		@Override
																		public void rejectedExecution(Runnable r, CVThreadPoolExecutor executor)
																		{
																			Log.d("ExecutionHandler", "rejectedExecution: " + r + ", executor: " + executor);
																		}
																	};


	/**
	 * @param maximumPoolSize --- 最大并发数
	 * @param workQueue --- 工作队列
	 */
	public CVThreadPoolExecutor(int maximumPoolSize, BlockingQueue<Runnable> workQueue)
	{
		this(maximumPoolSize, POOL_PRIORITY.BACKGROUND, workQueue);
	}

	/**
	 *
	 * @param maximumPoolSize --- 最大并发数
	 * @param priority --- 线程池优先级
	 * @param workQueue --- 工作队列
	 */
	public CVThreadPoolExecutor(int maximumPoolSize, POOL_PRIORITY priority, BlockingQueue<Runnable> workQueue)
	{
		this(maximumPoolSize, priority, workQueue, defaultHandler);
	}

	public CVThreadPoolExecutor(int maximumPoolSize, POOL_PRIORITY priority, BlockingQueue<Runnable> workQueue, CVRejectedExecutionHandler handler)
	{
		if (workQueue == null || handler == null)
			throw new NullPointerException();
		mWorkQueue = workQueue;
		mCommandPool = CommandServiceManager.get().getCommandSupplier().applyCommandPool(maximumPoolSize, priority.ordinal());
		mCommandPool.setCommandQueue(workQueue);
		mCommandPool.setExecutionHandler(new CommandExecutionHandler(this, handler));
	}


	public void setCommandListener(ICommandListener commandListener)
	{
		if (mCommandPool != null)
		{
			mCommandPool.setCommandListener(commandListener);
		}
	}

	public void enablePerformanceMonitor(boolean performanceMonitor)
	{
		if (mCommandPool != null)
		{
			mCommandPool.enablePerformanceMonitor(performanceMonitor);
		}
	}

	@Override
	public void shutdown()
	{
		mCommandPool.shutdown();
	}

	@Override
	public List<Runnable> shutdownNow()
	{
		return mCommandPool.shutdownNow();
	}

	@Override
	public boolean isShutdown()
	{
		return mCommandPool.isShutdown();
	}

	@Override
	public boolean isTerminated()
	{
		return mCommandPool.isTerminated();
	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException
	{
		return mCommandPool.awaitTermination(timeout, unit);
	}

	@Override
	public void execute(Runnable command)
	{
		if (mCommandPool.isPerformanceMonitor()) {
			mCommandPool.execute(new PerformanceRunnable(command));
		}else{
			mCommandPool.execute(command);
		}
	}

	@Override
	protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable)
	{
		return new CVFutureTask<>(callable);
	}

	@Override
	protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value)
	{
		return new CVFutureTask<T>(runnable, value);
	}

	@Override
	public boolean remove(Runnable command)
	{
		return mCommandPool.remove(command);
	}

	@Override
	public BlockingQueue<Runnable> getQueue()
	{
		return mWorkQueue;
	}

	private static class CommandExecutionHandler implements CommandRejectedExecutionHandler
	{

		private CVRejectedExecutionHandler	mHandler;
		private CVThreadPoolExecutor		mExecutor;

		public CommandExecutionHandler(CVThreadPoolExecutor executor, CVRejectedExecutionHandler handler)
		{
			this.mHandler = handler;
			this.mExecutor = executor;
		}

		@Override
		public void rejectedExecution(Runnable r, CommandPool commandPool)
		{
			if (mHandler != null)
			{
				mHandler.rejectedExecution(r, mExecutor);
			}
		}
	}


}
