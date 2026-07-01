package com.onus.demotest.threadpool.lib;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by niuniuyang on 2020-06-09. Description
 *
 * 项目介绍：KM文章《任务和线程分离的可复用线程池》http://km.oa.com/articles/show/459163
 *
 * 逻辑任务池，不会创建执行线程，executor是由外部传入，维护自己的最大并发执行的任务，如果超过最大的执行任务
 *
 * 就会把任务放入waitingCommands缓存，CommandPool会监听executor执行结果onCmdExecuted，
 *
 * 然后重新计数runningCount 的值，executor 会重新通过 ICommandPool的 takeWaitingCommand来获取新的任务
 */
public class CommandPool extends AbstractExecutorService implements ICommandPool
{

	private static final int					RUNNING				= 0;
	private static final int					SHUT_DOWN			= 1;
	private static final int					TERMINATED			= 2;
	/**
	 * executor Command执行的物理线程
	 */
	private CommandThreadPoolExecutor			executor;
	/**
	 * waitingCommands 任务是否入队等待是由 {@link #maximumPoolSize}和{@link #runningCount}决定
	 */
	private Queue<Runnable>						waitingCommands		= new LinkedList<>();
	/**
	 * maximumPoolSize 业务需要的最大并发线程数量，根据业务需求设置，并发数量超过它，任务就得排队
	 */
	private volatile int						maximumPoolSize;
	/**
	 * runningCount 当前并发任务数量，执行一个任务会执行 runningCount.incrementAndGet
	 *
	 * see {@link #execute(Runnable)}
	 *
	 * 任务执行完毕后，会执行runningCount.decrementAndGet see {@link #onCmdExecuted(Command)}
	 */
	private AtomicInteger						runningCount		= new AtomicInteger(0);
	/**
	 * priority 任务池的优先级，同时影响任务池的优先级和线程运行的优先级
	 *
	 * 值的范围 {@link android.os.Process#THREAD_PRIORITY_LOWEST} to{@link
	 * android.os.Process#THREAD_PRIORITY_URGENT_AUDIO} 值越小，优先级越大，获取cpu的时间片越多
	 *
	 * a.任务池优先级： executor线程空闲时，会优先选择优先级高的任务池的排队任务
	 *
	 * see {@link CommandThreadPoolExecutor#getWaitingCommand(Command)} and {@link
	 * #compareTo(ICommandPool)}
	 *
	 * b.线程运行的优先级：see{@link CommandThreadPoolExecutor#runWorker(Worker)}
	 */
	private int									priority;
	private ReentrantLock						commandLock			= new ReentrantLock();
	private final Condition						termination			= commandLock.newCondition();
	/**
	 * 任务池的运行状态 RUNNING->SHUT_DOWN->TERMINATED
	 */
	private AtomicInteger						state				= new AtomicInteger(RUNNING);
	/**
	 * 下面是性能监控，主要用在数据统计和上报
	 */
	private boolean								performanceMonitor	= false;
	private ICommandListener					commandListener;
	// queueTimeCaches 缓存入队排列的起始时间，用于计算任务队列排列时间
	private ConcurrentHashMap<Integer, Long>	queueTimeCaches		= new ConcurrentHashMap<>();
	private SystemTimeProvider					systemTimeProvider;
	private AtomicInteger						maxQueueCount		= new AtomicInteger(0);
	private AtomicInteger						completedTaskCount	= new AtomicInteger(0);
	private String								name;
	private CommandRejectedExecutionHandler		executionHandler;
	private RunnableListener					runnableListener;
	private int									maxRunningCount		= 0;

	CommandPool(int maximumPoolSize, int priority)
	{
		if (maximumPoolSize <= 0)
		{
			throw new IllegalArgumentException();
		}
		this.maximumPoolSize = maximumPoolSize;
		this.priority = priority;
	}

	void setSystemTimeProvider(SystemTimeProvider systemTimeProvider)
	{
		this.systemTimeProvider = systemTimeProvider;
	}

	public void enablePerformanceMonitor(boolean performanceMonitor)
	{
		this.performanceMonitor = performanceMonitor;
	}

	public boolean isPerformanceMonitor() {
		return performanceMonitor;
	}

	/**
	 * 更新当前任务池的最大并发线程数
	 */
	private boolean updateMaxPoolSize(int newMaxPoolSize)
	{
		if (this.maximumPoolSize != newMaxPoolSize)
		{
			this.maximumPoolSize = newMaxPoolSize;
			return true;
		}
		return false;
	}

	/**
	 * 执行线程池由外部传入
	 */
	public void setExecutor(CommandThreadPoolExecutor executor)
	{
		this.executor = executor;
		this.executor.registerCommandPool(this);
	}

	/**
	 * 销毁当前逻辑任务池，把state置为SHUT_DOWN状态，尝试调用tryTerminate
	 */
	public void shutdown()
	{
		state.compareAndSet(RUNNING, SHUT_DOWN);
		tryTerminate();
	}

	/**
	 * 销毁当前逻辑任务池，取消正在排队的任务执行
	 *
	 * @return 返回等待还未执行的任务
	 */
	public List<Runnable> shutdownNow()
	{
		ReentrantLock commandLock = this.commandLock;
		commandLock.lock();
		ArrayList commandLists = new ArrayList<Runnable>();
		try
		{
			commandLists.addAll(waitingCommands);
			waitingCommands.clear();
		}
		catch (OutOfMemoryError e)
		{
		}
		finally
		{
			commandLock.unlock();
		}
		shutdown();
		return commandLists;
	}

	/**
	 * 物理线程池的状态isShutdown
	 */
	@Override
	public boolean isShutdown()
	{
		return state.get() != RUNNING;
	}

	/**
	 * 物理线程池的状态isTerminated
	 */
	@Override
	public boolean isTerminated()
	{
		return state.get() == TERMINATED;
	}

	/**
	 * 等待当前任务池的销毁，如果不是TERMINATED的状态，会等待tryTerminate的执行
	 */
	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException
	{
		long nanos = unit.toNanos(timeout);
		final ReentrantLock commandLock = this.commandLock;
		commandLock.lock();
		try
		{
			while (!isTerminated())
			{
				if (nanos <= 0L)
				{
					return false;
				}
				nanos = termination.awaitNanos(nanos);
			}
		}
		finally
		{
			commandLock.unlock();
		}
		return true;
	}

	/**
	 * 设置任务的排队队列，比如外部有定制优先级队列，可以替换默认的任务队列
	 */
	public void setCommandQueue(Queue<Runnable> queue)
	{
		this.waitingCommands = queue;
	}

	/**
	 * 执行一个外部的任务，先把任务放入队列排队，然后调用getWaitingCmd取出满足调度条件的任务进行执行
	 *
	 * @param runnable 待执行的任务
	 */
	public void execute(Runnable runnable)
	{
		if (isShutdown())
		{
			rejectCommand(runnable);
			return;
		}
		ReentrantLock commandLock = this.commandLock;
		Command waitingCmd = null;
		commandLock.lock();
		try
		{
			cacheQueueTime(runnable);
			if (!isRunningMaxSize())
			{
				//没有到最大的并发数
				if (waitingCommands.isEmpty())
				{
					//没有排队任务，就直接调度，不用再入队
					waitingCmd = createCmd(runnable);
				}
				else
				{
					//有存在的排队任务，就入队排队
					boolean offerSuccess = waitingCommands.offer(runnable);
					waitingCmd = createCmd(waitingCommands.poll());
					if (!offerSuccess && !waitingCommands.offer(runnable))
					{
						rejectCommand(runnable);
					}
				}
			}
			else
			{
				//已经达到最大并发数，就入队等待
				if (!waitingCommands.offer(runnable))
				{
					rejectCommand(runnable);
				}
			}
		}
		finally
		{
			commandLock.unlock();
		}
		if (waitingCmd != null)
		{
			executor.execute(waitingCmd);
		}
		else
		{
			checkNotifyQueueSize();
		}
	}

	/**
	 * 拒绝执行任务
	 */
	private void rejectCommand(Runnable runnable)
	{
		if (executionHandler != null)
		{
			executionHandler.rejectedExecution(runnable, this);
		}
	}

	/**
	 * 缓存任务入队列的起始时间，用于后续统计任务的排队时间
	 */
	private void cacheQueueTime(Runnable runnable)
	{
		if (performanceMonitor)
		{
			queueTimeCaches.put(runnable.hashCode(), systemTimeProvider.currentTime());
		}
	}

	/**
	 * 通知队列最大排队的任务数量变化
	 */
	private void checkNotifyQueueSize()
	{
		if (performanceMonitor && commandListener != null)
		{
			int waitingCmdSize = waitingCommands.size();
			if (waitingCmdSize > maxQueueCount.get())
			{
				maxQueueCount.set(waitingCmdSize);
				commandListener.onQueueMaxSizeChanged(this, maxQueueCount.get());
			}
		}
	}

	/**
	 * 获取一个等待的任务，如果当前运行的数量大于已经设置的最大数量，返回null
	 *
	 * getWaitingCmd 都会poll一个任务，最终会投递给executor，所以runningCount.incrementAndGet()
	 */
	private Command createCmd(Runnable runnable)
	{
		if (runnable != null)
		{
			int newRunningCount = runningCount.incrementAndGet();
			maxRunningCount = Math.max(newRunningCount, maxRunningCount);
			return new Command(runnable, this, priority);
		}
		return null;
	}

	/**
	 * 是否已经到达最大的并发任务数
	 */
	private boolean isRunningMaxSize()
	{
		return runningCount.get() >= maximumPoolSize;
	}

	/**
	 * 当executor有一个空闲的线程的时候，会在这里获取一个队列里面排队的command
	 */
	@Override
	public Command takeWaitingCommand()
	{
		ReentrantLock commandLock = this.commandLock;
		commandLock.lock();
		try
		{
			if (!isRunningMaxSize())
			{
				return createCmd(waitingCommands.poll());
			}
		}
		finally
		{
			commandLock.unlock();
		}
		return null;
	}

	/**
	 * 每个CommandPool都有自己都maximumPoolSize，所有都CommandPool的maximumPoolSize加起来
	 *
	 * 决定executor的maximumPoolSize
	 */
	@Override
	public int getMaximumThreadSize()
	{
		return maximumPoolSize;
	}

	/**
	 * 如果executor执行完毕后，会调用到这里
	 */
	@Override
	public void onCmdExecuted(Command command)
	{
		decrementRunningCount();
		completedTaskCount.incrementAndGet();
	}

	/**
	 * 每次runningCount.decrementAndGet后，需要check是否需要执行{@link #tryTerminate()}
	 *
	 * 因为tryTerminate函数里面有runningCount.get() == 0条件
	 */
	private void decrementRunningCount()
	{
		runningCount.decrementAndGet();
		tryTerminate();
	}

	/**
	 * 所有任务执行完了，任务池才进入TERMINATED的状态，{@link #awaitTermination(long, TimeUnit)}
	 *
	 * 1、没有排队的任务，2、state处于SHUT_DOWN状态 3、没有正在运行的任务
	 *
	 * 满足以上3个条件，即可TERMINATED
	 */
	private final void tryTerminate()
	{
		ReentrantLock commandLock = this.commandLock;
		commandLock.lock();
		try
		{
			if (waitingCommands.isEmpty() && state.get() == SHUT_DOWN && runningCount.get() == 0)
			{
				state.set(TERMINATED);
				if (this.executor != null)
				{
					this.executor.unRegisterCommandPool(this);
				}
				termination.signalAll();
			}
		}
		finally
		{
			commandLock.unlock();
		}

	}

	/**
	 * 执行前的回调，这里主要用于记录任务开始执行的时间
	 */
	@Override
	public void beforeExecute(Command command)
	{
		notifyBeforeExecute(command);
		if (runnableListener != null)
		{
			runnableListener.beforeExecute(command.runnable);
		}
	}

	/**
	 * 性能监控
	 */
	private void notifyBeforeExecute(Command command)
	{
		if (performanceMonitor)
		{
			Long queueTime = queueTimeCaches.remove(command.runnable.hashCode());
			if (queueTime != null)
			{
				command.recordQueueTime(queueTime);
				command.recordStartExeTime(systemTimeProvider.currentTime());
				if(commandListener != null)
				{
					commandListener.onCmdStart(this, command);
				}
			}
		}
	}

	/**
	 * 执行前的回调，这里主要用于记录任务执行结束的时间
	 */
	@Override
	public void afterExecute(Command command)
	{
		notifyAfterExecute(command);
		if (runnableListener != null)
		{
			runnableListener.afterExecute(command.runnable);
		}
	}

	/**
	 * 性能监控
	 */
	private void notifyAfterExecute(Command command)
	{
		if (performanceMonitor)
		{
			command.recordCompletedTime(systemTimeProvider.currentTime());
			if (commandListener != null)
			{
				commandListener.onCmdCompleted(this, command);
			}
		}
	}

	/**
	 * 任务被拒绝,这里一般是线程池被关闭，被调度的任务无法执行，任务会被拒绝
	 */
	@Override
	public void onReject(Command command)
	{
		decrementRunningCount();
		rejectCommand(command.runnable);
	}

	/**
	 * priority 值越小，排队越靠前，优先级越高
	 */
	@Override
	public int compareTo(ICommandPool o)
	{
		int priority = ((CommandPool) o).priority;
		if (this.priority < priority)
		{
			return -1;
		}
		else if (this.priority > priority)
		{
			return 1;
		}
		return 0;
	}

	/**
	 * 设置任务运行状态的监听，可以用于统计上报
	 */
	public void setCommandListener(ICommandListener commandListener)
	{
		this.commandListener = commandListener;
	}

	@Override
	public String getName()
	{
		return name;
	}

	/**
	 * 给任务池一个名字，后续用于监控上报
	 */
	void setName(String name)
	{
		this.name = name;
	}

	public void cancelAll()
	{
		ReentrantLock commandLock = this.commandLock;
		commandLock.lock();
		try
		{
			waitingCommands.clear();
		}
		finally
		{
			commandLock.unlock();
		}
	}

	/**
	 * 仅仅用于取消正在排队的任务
	 *
	 * @param runnable 待取消的任务
	 */
	public boolean remove(Runnable runnable)
	{
		ReentrantLock commandLock = this.commandLock;
		commandLock.lock();
		try
		{
			if (waitingCommands.remove(runnable))
			{
				return true;
			}
		}
		finally
		{
			commandLock.unlock();
		}
		return false;
	}

	/**
	 * 更新任务的优先级，仅用于排队对任务
	 */
	public boolean updatePriority(Runnable runnable)
	{
		if (remove(runnable))
		{
			execute(runnable);
			return true;
		}
		return false;
	}

	/**
	 * 任务拒绝 see {@link #onReject(Command)}
	 */
	public void setExecutionHandler(CommandRejectedExecutionHandler executionHandler)
	{
		this.executionHandler = executionHandler;
	}

	/**
	 * 监听runnable的执行生命周期 see {@link RunnableListener#beforeExecute(Runnable)}
	 *
	 * 和 {@link RunnableListener#afterExecute(Runnable)}
	 */
	public void setRunnableListener(RunnableListener runnableListener)
	{
		this.runnableListener = runnableListener;
	}

	/**
	 * 统计最大的的线程并发数目，监控无限量大小的任务池，对于线程的最大消耗，太大就是不太合理的
	 */
	public int getMaxRunningCount()
	{
		return maxRunningCount;
	}

	/**
	 * 获取任务池执行完成的任务数
	 */
	public int getCompletedTaskCount()
	{
		return completedTaskCount.get();
	}

	/**
	 * 返回最大的线程池并发数
	 */
	public int getMaximumPoolSize()
	{
		return maximumPoolSize;
	}

	/**
	 * 用于业务调整maximumPoolSize
	 *
	 * 如果executor!=null 需要重新更新executor的线程池大小
	 *
	 * @param maximumPoolSize 新的线程池大小
	 */
	public void setMaximumPoolSize(int maximumPoolSize)
	{
		if (maximumPoolSize <= 0)
		{
			throw new IllegalArgumentException();
		}
		if (updateMaxPoolSize(maximumPoolSize))
		{
			if (executor != null)
			{
				executor.updateMaximumPoolSize();
			}
		}
	}

	/**
	 * 获取最大的任务排队数
	 */
	public int getMaxQueueCount()
	{
		return maxQueueCount.get();
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(name);
		sb.append("[");
		sb.append(", maximumPoolSize = " + maximumPoolSize);
		sb.append(", waitingCommandSize = " + waitingCommands.size());
		sb.append(", runningCount = " + runningCount.get());
		sb.append(", completed = " + completedTaskCount.get());
		sb.append(", maxQueueCount = " + maxQueueCount.get());
		sb.append(", maxRunningCount = " + maxRunningCount);
		sb.append("]");
		return sb.toString();
	}
}
