package com.onus.demotest.threadpool.lib;

import android.os.Process;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by niuniuyang on 2020-07-09. Description
 *
 * 任务池提供者，业务申请任务池，统一收拢在这里申请，方便监控资源都申请以及后续的数据集中上报
 */
public class CommandPoolSupplier implements IWorkerListener, ICommandListener
{

	/**
	 * corePoolSize 核心线程的数量，常驻线程，不会因为超时而被销毁
	 * 默认值1，可以修改corePoolSize的修改，see{@link #init(int, long, TimeUnit, ThreadFactory)}
	 */
	private int									corePoolSize				= 1;
	/**
	 * keepAliveTime最大线程数量的存活时间，
	 *
	 * 默认值30秒，可以进行修改 see{@link #init(int, long, TimeUnit, ThreadFactory)
	 */
	private long								keepAliveTime				= 60;
	/**
	 * timeUnit keepAliveTime的超时时间的单位 默认是秒，可以进行修改
	 *
	 * see{@link #init(int, long, TimeUnit, ThreadFactory)
	 */
	private TimeUnit							timeUnit					= TimeUnit.SECONDS;
	/**
	 * 默认线程池工厂，可以进行修改 see{@link #init(int, long, TimeUnit, ThreadFactory)
	 */
	private ThreadFactory						threadFactory				= new CommandThreadFactory("command_thread");
	/**
	 * executor 物理线程池，用于执行真正的Command任务
	 */
	private volatile CommandThreadPoolExecutor	executor;
	private Object								executorLock				= new Object();
	private SystemTimeProvider					timeProvider;																//用于获取系统时间，统计使用
	private boolean								enablePerformanceMonitor	= false;
	private AtomicInteger						commandPoolCount			= new AtomicInteger(0);
	private ICommandListener					commandExeListener;
	private IWorkerListener						workerListener				= null;

	public CommandPoolSupplier()
	{

	}

	/**
	 * 设置Command的执行监控 see
	 * {@link #onCmdCompleted(CommandPool, Command)}
	 */
	public void setCommandListener(ICommandListener commandExeListener)
	{
		this.commandExeListener = commandExeListener;
	}

	/**
	 * 设置线程池的数据监控
	 *
	 * see {@link #onWorkerAdded(String)} and {@link #onWorkerExited(String)}
	 * (String)}
	 */
	public void setWorkerListener(IWorkerListener workerListener)
	{
		this.workerListener = workerListener;
	}

	/**
	 * 打开性能监控开关
	 */
	public void enablePerformanceMonitor(boolean enablePerformanceMonitor)
	{
		this.enablePerformanceMonitor = enablePerformanceMonitor;
	}

	/**
	 * 初始化线程池对参数，建议在应用入口地方初始化，比如AppLication对OnCreate，不会耗时
	 *
	 * @param corePoolSize 核心线程数量
	 * @param keepAliveTime 非核心线程的超时时间
	 * @param threadFactory 线程工厂
	 * @param unit keepAliveTime的单位{@link TimeUnit}
	 */
	public void init(int corePoolSize, long keepAliveTime, TimeUnit unit, ThreadFactory threadFactory)
	{
		if (corePoolSize > 0)
		{
			this.corePoolSize = corePoolSize;
		}
		if (keepAliveTime > 0)
		{
			this.keepAliveTime = keepAliveTime;
		}
		if (unit != null)
		{
			this.timeUnit = unit;
		}
		if (threadFactory != null)
		{
			this.threadFactory = threadFactory;
		}
	}

	private void initExecutor()
	{
		if (executor == null)
		{
			synchronized (executorLock)
			{
				if (executor == null)
				{
					executor = new CommandThreadPoolExecutor(corePoolSize, keepAliveTime, timeUnit,
							new PriorityBlockingQueue<>(11, new Comparator<Command>()
							{
								@Override
								public int compare(Command o1, Command o2)
								{
									if (o1 instanceof Comparable && o2 instanceof Comparable)
									{
										return ((Comparable) o1).compareTo(o2);
									}
									return 0;
								}
							}));
					executor.setThreadFactory(threadFactory);
				}
				if (enablePerformanceMonitor)
				{
					executor.setWorkerListener(this);
				}
			}
		}
	}

	/**
	 * 申请一个任务池
	 *
	 * @param maximumPoolSize 任务池最大都并发任务数目，即线程池会为你申请都最大都并发线程数为你服务
	 * @param priority
	 *            任务池的优先级，由于一个executor对应多个CommandPool，executor执行排队CommandPool，值越大，优先级越低
	 *
	 *            同时影响任务池的优先级和线程运行的优先级
	 *
	 *            from {@link Process#THREAD_PRIORITY_LOWEST} to{@link
	 *            Process#THREAD_PRIORITY_URGENT_AUDIO} 值越小，优先级越大
	 *
	 *            a.任务池优先级： executor线程空闲时，会优先选择优先级高的任务池的排队任务
	 *
	 *            see {@link CommandThreadPoolExecutor#getWaitingCommand(Command)}
	 *            and {@link
	 *            CommandPool#compareTo(ICommandPool)}
	 *
	 *            *
	 *            b.线程运行的优先级：see{@link CommandThreadPoolExecutor#runWorker(Worker)}
	 * @param name 任务池的名字，主要用于性能监控，同一个项目中name 不要重复，这样出现统计出错的问题
	 *
	 *            需要将CommandPool进行优先级排队，priority越小，优先级越高
	 * @return 返回一个任务池
	 */
	public CommandPool applyCommandPool(int maximumPoolSize, int priority, String name)
	{
		initExecutor();
		commandPoolCount.incrementAndGet();
		CommandPool commandPool = new CommandPool(maximumPoolSize, priority);
		commandPool.setExecutor(executor);
		if (name == null || name.length() <= 0)
		{
			name = createDefaultName();
		}
		commandPool.setName(name);
		initTimeProvider();
		commandPool.setSystemTimeProvider(timeProvider);
		if (enablePerformanceMonitor)
		{
			commandPool.setCommandListener(this);
			commandPool.enablePerformanceMonitor(true);
		}
		return commandPool;
	}

	/**
	 * 对齐原生的线程池创建接口
	 *
	 * @param nThreads 最大线程并发数
	 * @param name 名字
	 */
	public Executor newFixedThreadPool(int nThreads, String name)
	{
		return applyCommandPool(nThreads, name);
	}

	/**
	 * 申请一个任务池
	 *
	 * @param maximumPoolSize 任务池最大都并发任务数目，即线程池会为你申请都最大都并发线程数为你服务
	 * @return 返回一个任务池
	 */
	public CommandPool applyCommandPool(int maximumPoolSize)
	{
		return applyCommandPool(maximumPoolSize, Process.THREAD_PRIORITY_DEFAULT, null);
	}

	/**
	 * 申请一个任务池
	 *
	 * @param maximumPoolSize 任务池最大都并发任务数目，即线程池会为你申请都最大都并发线程数为你服务
	 * @param priority 任务池优先级，值越大，优先级越低
	 * @return 返回一个任务池
	 */
	public CommandPool applyCommandPool(int maximumPoolSize, int priority)
	{
		return applyCommandPool(maximumPoolSize, priority, null);
	}

	/**
	 * 申请一个任务池
	 *
	 * @param maximumPoolSize 任务池最大都并发任务数目，即线程池会为你申请都最大都并发线程数为你服务
	 * @return 返回一个任务池
	 */
	public CommandPool applyCommandPool(int maximumPoolSize, String name)
	{
		return applyCommandPool(maximumPoolSize, Process.THREAD_PRIORITY_DEFAULT, name);
	}


	private void initTimeProvider()
	{
		if (timeProvider == null)
		{
			timeProvider = new SystemTimeProvider();
		}
	}

	/**
	 * 系统时间提供，不同的平台不一样
	 */
	public void setTimeProvider(SystemTimeProvider timeProvider)
	{
		this.timeProvider = timeProvider;
	}

	/**
	 * 返回任务池对应的执行线程池
	 */
	public CommandThreadPoolExecutor getExecutor()
	{
		initExecutor();
		return executor;
	}

	/**
	 * 通知线程worker的创建，主要用于统计线程的存活时间
	 *
	 * @param workerId 返回workerId，用于区别不同线程
	 */
	@Override
	public void onWorkerAdded(String workerId)
	{
		if (workerListener != null)
		{
			workerListener.onWorkerAdded(workerId);
		}
	}

	/**
	 * 通知线程worker的销毁，主要用于统计线程的存活时间
	 *
	 * @param workerId 返回workerId，用于区别不同线程
	 */
	@Override
	public void onWorkerExited(String workerId)
	{
		if (workerListener != null)
		{
			workerListener.onWorkerExited(workerId);
		}
	}

	private String createDefaultName()
	{
		return "CommandPool_" + commandPoolCount.get();
	}


	@Override
	public void onCmdStart(CommandPool commandPool, Command command) {
		if(commandExeListener != null)
		{
			commandExeListener.onCmdStart(commandPool, command);
		}
	}

	/**
	 * 单个任务执行完成的通知
	 *
	 * @param commandPool 对应的任务池
	 * @param command 对应的任务
	 */
	@Override
	public void onCmdCompleted(CommandPool commandPool, Command command)
	{
		if (commandExeListener != null)
		{
			commandExeListener.onCmdCompleted(commandPool, command);
		}
	}

	/**
	 * 任务池的任务最大排队数量的变化通知，用于统计任务排队峰值情况
	 *
	 * @param commandPool 对应的任务池
	 * @param maxQueueSize 最大峰值的排队任务数量
	 */
	@Override
	public void onQueueMaxSizeChanged(CommandPool commandPool, int maxQueueSize)
	{
		if (commandExeListener != null)
		{
			commandExeListener.onQueueMaxSizeChanged(commandPool, maxQueueSize);
		}
	}

	/**
	 * 打印出当前线程池的运行状态，用于上报debug问题使用
	 */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("CommandPoolSupplier:\n");
		Queue<ICommandPool> commandPools = null;
		if (executor != null)
		{
			commandPools = executor.getCommandPools();
			sb.append("    " + executor.toString() + "\n");
		}
		if (commandPools != null)
		{
			Iterator<ICommandPool> cmdPools = commandPools.iterator();
			while (cmdPools.hasNext())
			{
				sb.append("    " + cmdPools.next().toString() + "\n");
			}
		}
		return sb.toString();
	}

	public void shutdown()
	{
		if (executor != null)
		{
			executor.shutdown();
		}
	}

	public List<Command> shutdownNow()
	{
		if (executor != null)
		{
			return executor.shutdownNow();
		}
		return null;
	}
}
