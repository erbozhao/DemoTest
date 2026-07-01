package com.onus.demotest.threadpool;


import android.os.Process;

import com.onus.demotest.threadpool.lib.CommandServiceManager;
import com.onus.demotest.threadpool.lib.CommandThreadFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Description
 *
 * @author xiandongluo
 * @see
 * @since 2020/10/10
 */
public class CVExecutorSupplier
{
	private volatile static CVExecutorSupplier	sInstance							= null;

	static int									INNER_NUM_CPU_BOUND_THREADS			= 1;

	static int									NUM_CPU_BOUND_THREADS				= INNER_NUM_CPU_BOUND_THREADS;

	private volatile CVThreadPoolExecutor		mPictureExecutor;
	private final Object						mPictureExecutorLock				= new Object();

	private volatile CVThreadPoolExecutor		mHighPriorityNetworkExecutor;
	private final Object						mHighPriorityNetworkExecutorLock	= new Object();

	private volatile CVThreadPoolExecutor		mNetworkExecutor;
	private final Object						mNetworkExecutorLock				= new Object();

	private volatile CVThreadPoolExecutor		mCoreTaskExecutor;
	private final Object						mCoreTaskExecutorLock				= new Object();

	private volatile CVThreadPoolExecutor		mIoExecutor;
	private final Object						mIoExecutorLock						= new Object();

	private volatile CVThreadPoolExecutor		mDBExecutor;
	private final Object						mDBExecutorLock						= new Object();

	private volatile CVThreadPoolExecutor		mCpuBoundExecutor;
	private final Object						mCpuBoundExecutorLock				= new Object();

	private volatile Executor					mImmediateExecutor;
	private final Object						mImmediateExecutorLock				= new Object();

	private volatile CVMainThreadExecutor		mMainThreadExecutor;
	private final Object						mMainThreadExecutorLock				= new Object();

	private volatile CVThreadPoolExecutor		mShortTimeExecutor;
	private final Object						mShortTimeExecutorLock				= new Object();

	private volatile CVThreadPoolExecutor		mLongTimeExecutor;
	private final Object						mLongTimeExecutorLock				= new Object();

	Object										mScheduledLock						= new Object();
	ScheduledExecutorService					mScheduledExecutor;


	private CVExecutorSupplier()
	{
		CommandServiceManager.get().getCommandSupplier().init(15, 30, TimeUnit.SECONDS, null);
		try
		{
			NUM_CPU_BOUND_THREADS = Runtime.getRuntime().availableProcessors();
		}
		catch (Throwable t)
		{
			NUM_CPU_BOUND_THREADS = 4;
		}
		INNER_NUM_CPU_BOUND_THREADS = NUM_CPU_BOUND_THREADS;
		if (INNER_NUM_CPU_BOUND_THREADS < 4)
		{
			INNER_NUM_CPU_BOUND_THREADS = 4;
		}

	}

	public static CVExecutorSupplier getInstance()
	{
		if (sInstance == null)
		{
			synchronized (CVExecutorSupplier.class)
			{
				if (sInstance == null)
				{
					sInstance = new CVExecutorSupplier();
				}
			}
		}
		return sInstance;
	}

	public int getNumOfCPUBoundThreads()
	{
		return NUM_CPU_BOUND_THREADS;
	}

	/**
	 * 图片加载线程池
	 * 
	 * @return
	 */
	public CVExecutorService getPictureExecutor()
	{
		if (mPictureExecutor == null)
		{
			synchronized (mPictureExecutorLock)
			{
				if (mPictureExecutor == null)
				{
					mPictureExecutor = new CVThreadPoolExecutor(4, CVThreadPoolExecutor.POOL_PRIORITY.DISPLAY,
							new LinkedBlockingQueue());
				}
			}
		}
		return mPictureExecutor;
	}

	/**
	 * 高优先级网络请求线程池，一般用于需要快速呈现给用户的网络请求
	 * 
	 * @return
	 */
	public CVExecutorService getHighPriorityNetworkExecutor()
	{
		if (mHighPriorityNetworkExecutor == null)
		{
			synchronized (mHighPriorityNetworkExecutorLock)
			{
				if (mHighPriorityNetworkExecutor == null)
				{
					mHighPriorityNetworkExecutor = new CVThreadPoolExecutor(4, CVThreadPoolExecutor.POOL_PRIORITY.URGENT_DISPLAY,
							new PriorityBlockingQueue(11, PriorityComparator.DEFAULT));
				}
			}
		}
		return mHighPriorityNetworkExecutor;
	}

	/**
	 * 普通网络请求线程池
	 * 
	 * @return
	 */
	public CVExecutorService getNetworkExecutor()
	{
		if (mNetworkExecutor == null)
		{
			synchronized (mNetworkExecutorLock)
			{
				if (mNetworkExecutor == null)
				{
					mNetworkExecutor = new CVThreadPoolExecutor(4, CVThreadPoolExecutor.POOL_PRIORITY.DISPLAY,
							new PriorityBlockingQueue(11, PriorityComparator.DEFAULT));
				}
			}
		}
		return mNetworkExecutor;
	}

	/**
	 * 获取核心任务线程池
	 * 
	 * @return
	 */
	public CVExecutorService getCoreTaskExecutor()
	{
		if (mCoreTaskExecutor == null)
		{
			synchronized (mCoreTaskExecutorLock)
			{
				if (mCoreTaskExecutor == null)
				{
					mCoreTaskExecutor = new CVThreadPoolExecutor(INNER_NUM_CPU_BOUND_THREADS + 1,
							new LinkedBlockingQueue());
				}
			}
		}
		return mCoreTaskExecutor;
	}

	/**
	 * 获取IO线程池
	 *
	 * @return
	 */
	public CVExecutorService getIOTaskExecutor()
	{
		if (mIoExecutor == null)
		{
			synchronized (mIoExecutorLock)
			{
				if (mIoExecutor == null)
				{
					mIoExecutor = new CVThreadPoolExecutor(4, new LinkedBlockingQueue());
				}
			}
		}
		return mIoExecutor;
	}

	/**
	 * 获取IO线程池
	 *
	 * @return
	 */
	public CVExecutorService getDBTaskExecutor()
	{
		if (mDBExecutor == null)
		{
			synchronized (mDBExecutorLock)
			{
				if (mDBExecutor == null)
				{
					mDBExecutor = new CVThreadPoolExecutor(4, new LinkedBlockingQueue());
				}
			}
		}
		return mDBExecutor;
	}

	/**
	 * CPU线程池 针对纯运算操作（例如加解密，编解码等）
	 * 
	 * @return
	 */
	public CVExecutorService getCpuBoundExecutor()
	{
		if (mCpuBoundExecutor == null)
		{
			synchronized (mCpuBoundExecutorLock)
			{
				if (mCpuBoundExecutor == null)
				{
					mCpuBoundExecutor = new CVThreadPoolExecutor(INNER_NUM_CPU_BOUND_THREADS + 1,
							new LinkedBlockingQueue());
				}
			}
		}
		return mCpuBoundExecutor;
	}

	public Executor getImmediateExecutor()
	{
		if (mImmediateExecutor == null)
		{
			synchronized (mImmediateExecutorLock)
			{
				if (mImmediateExecutor == null)
				{
					mImmediateExecutor = new ImmediateExecutor();
				}
			}
		}

		return mImmediateExecutor;

	}

	/**
	 * 或区域
	 * 
	 * @return
	 */
	public ScheduledExecutorService getScheduledExecutor()
	{
		if (mScheduledExecutor == null)
		{
			synchronized (mScheduledLock)
			{
				if (mScheduledExecutor == null)
				{
					mScheduledExecutor = Executors
							.newSingleThreadScheduledExecutor(new CommandThreadFactory("Scheduled delay", Process.THREAD_PRIORITY_BACKGROUND));
				}
			}
		}

		return mScheduledExecutor;
	}

	/**
	 * 获取主线程线程池
	 * 
	 * @return
	 */
	public CVMainThreadExecutor getMainThreadExecutor()
	{
		if (mMainThreadExecutor == null)
		{
			synchronized (mMainThreadExecutorLock)
			{
				if (mMainThreadExecutor == null)
				{
					mMainThreadExecutor = new CVMainThreadExecutor();
				}
			}
		}
		return mMainThreadExecutor;
	}

	/**
	 * 短耗时任务线程池
	 */
	public CVExecutorService getShortTimeExecutor()
	{
		if (mShortTimeExecutor == null)
		{
			synchronized (mShortTimeExecutorLock)
			{
				if (mShortTimeExecutor == null)
				{
					mShortTimeExecutor = new CVThreadPoolExecutor(2,
							new LinkedBlockingQueue());
				}
			}
		}
		return mShortTimeExecutor;
	}

	/**
	 * 长耗时任务线程池
	 */
	public CVExecutorService getLongTimeExecutor()
	{
		if (mLongTimeExecutor == null)
		{
			synchronized (mLongTimeExecutorLock)
			{
				if (mLongTimeExecutor == null)
				{
					mLongTimeExecutor = new CVThreadPoolExecutor(2,
							new LinkedBlockingQueue());
				}
			}
		}
		return mLongTimeExecutor;
	}

	/**
	 * 针对需要在主线程执行的操作
	 *
	 * @return Executor
	 */
	public static CVMainThreadExecutor forMainThreadTasks()
	{
		return getInstance().getMainThreadExecutor();
	}

	/**
	 * 针对纯运算操作（例如加解密，编解码等）
	 *
	 * @return Executor
	 */
	public static CVExecutorService forBackgroundTasks()
	{
		return getInstance().getCoreTaskExecutor();
	}

	/**
	 * 针对数据库（读写）操作
	 *
	 * @return Executor
	 */
	public static CVExecutorService forDbTasks()
	{
		return getInstance().getDBTaskExecutor();
	}

	/**
	 * 针对本地文件读写（文件或文件夹读写）操作
	 *
	 * @return Executor
	 */
	public static CVExecutorService forIoTasks()
	{
		return getInstance().getIOTaskExecutor();
	}

	/**
	 * 图片加载
	 * 
	 * @return
	 */
	public static CVExecutorService forPictureTasks()
	{
		return getInstance().getPictureExecutor();
	}

	/**
	 * 针对计算类任务
	 *
	 * @return Executor
	 */
	public static CVExecutorService forCoreTasks()
	{
		return getInstance().getCoreTaskExecutor();
	}

	public static CVExecutorService forShortTimeTasks()
	{
		return getInstance().getShortTimeExecutor();
	}

	public static CVExecutorService forLongTimeTasks()
	{
		return getInstance().getLongTimeExecutor();
	}

	private static class ImmediateExecutor implements Executor
	{
		private static final int		MAX_DEPTH		= 15;
		private ThreadLocal<Integer>	executionDepth	= new ThreadLocal<Integer>();

		/**
		 * Increments the depth.
		 *
		 * @return the new depth value.
		 */
		private int incrementDepth()
		{
			Integer oldDepth = executionDepth.get();
			if (oldDepth == null)
			{
				oldDepth = 0;
			}
			int newDepth = oldDepth + 1;
			executionDepth.set(newDepth);
			return newDepth;
		}

		/**
		 * Decrements the depth.
		 *
		 * @return the new depth value.
		 */
		private int decrementDepth()
		{
			Integer oldDepth = executionDepth.get();
			if (oldDepth == null)
			{
				oldDepth = 0;
			}
			int newDepth = oldDepth - 1;
			if (newDepth == 0)
			{
				executionDepth.remove();
			}
			else
			{
				executionDepth.set(newDepth);
			}
			return newDepth;
		}

		@Override
		public void execute(Runnable command)
		{
			int depth = incrementDepth();
			try
			{
				if (depth <= MAX_DEPTH)
				{
					command.run();
				}
				else
				{
					CVExecutorSupplier.forBackgroundTasks().execute(command);
				}
			}
			finally
			{
				decrementDepth();
			}
		}
	}
}
