/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*
 * This file is available under and governed by the GNU General Public
 * License version 2 only, as published by the Free Software Foundation.
 * However, the following notice accompanied the original version of this
 * file:
 *
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package com.onus.demotest.threadpool.lib;


import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


/**
 * 该代码由作者 Doug Lea 的 ThreadPoolExecutor 改造而成，增加了线程池对于多个任务池的调度策略， 这里特别感谢原作者 Doug
 * Lea，感谢他对java语言的巨大贡献。
 * 将函数的注释重新用中文翻译了一遍，如果觉得中文不好理解，可以参考原版ThreadPoolExecutor的英文注释
 *
 * CommandThreadPoolExecutor 是任务执行的物理线程，完成任务的执行操作， 它对应到多个任务池 {@link CommandPool}
 *
 * create by niuniuyang 2020.6.20
 */
public class CommandThreadPoolExecutor
{

	/**
	 * 线程池实际大小数目存储在低位
	 */
	private static final int				COUNT_BITS			= Integer.SIZE - 3;
	private static final int				CAPACITY			= (1 << COUNT_BITS) - 1;
	/**
	 * 运行状态存储在高位
	 */
	private static final int				RUNNING				= -1 << COUNT_BITS;
	private static final int				SHUTDOWN			= 0 << COUNT_BITS;
	private static final int				STOP				= 1 << COUNT_BITS;
	private static final int				TIDYING				= 2 << COUNT_BITS;
	private static final int				TERMINATED			= 3 << COUNT_BITS;
	private static final RuntimePermission	shutdownPerm		= new RuntimePermission("modifyThread");
	private static final boolean			ONLY_ONE			= true;
	final Object							commandPoolsLock	= new Object();
	/**
	 * ctl 当前工作所有的工作worker数量，
	 *
	 * 每次addWorker之前都要执行自加 see {@link #compareAndIncrementWorkerCount(int)}
	 *
	 * 每个线程退出钱，都要执行自减 see {@link #compareAndDecrementWorkerCount(int)}
	 */
	private final AtomicInteger				ctl					= new AtomicInteger(ctlOf(RUNNING, 0));
	/**
	 * waitWorkerCount 等待执行的线程池数量,也就是空闲线程的数量
	 */
	private final AtomicInteger				waitWorkerCount		= new AtomicInteger(0);
	/**
	 * workQueue 当前等待排队的任务数，由于任务并发控制都交给了CommandPool，所有workQueue只是一个任务中转站，
	 * 任务会很快被线程调度
	 */
	private final BlockingQueue<Command>	workQueue;

	private final ReentrantLock				mainLock			= new ReentrantLock();

	/**
	 * 线程池里面的所有线程，包括繁忙的和空闲的线程， 访问workers需要加锁 mainLock.
	 */
	private final HashSet<Worker>			workers				= new HashSet<>();
	/**
	 * Wait condition to support awaitTermination.
	 */
	private final Condition					termination			= mainLock.newCondition();
	private PriorityQueue<ICommandPool>		commandPools		= new PriorityQueue<>();
	private IWorkerListener					workerListener;
	/**
	 * Tracks largest attained pool size. Accessed only under mainLock.
	 */
	private int								largestPoolSize;
	/**
	 * Counter for completed tasks. Updated only on termination of worker threads.
	 * Accessed only
	 * under mainLock.
	 */
	private long							completedTaskCount;
	/**
	 * 线程创建工厂
	 */
	private volatile ThreadFactory			threadFactory;
	/**
	 * 线程池的保活时间，如果线程执行{@link #getTask() 的poll超时，就会退出线程}
	 */
	private volatile long					keepAliveTime;
	/**
	 * 运行核心线程池超时开关
	 */
	private volatile boolean				allowCoreThreadTimeOut;
	/**
	 * 核心线程池数量，如果{@link #allowCoreThreadTimeOut} 为false，就不会超时退出
	 */
	private volatile int					corePoolSize;
	/**
	 * 最大的线程池数量
	 */
	private volatile int					maximumPoolSize;

	/**
	 * 创建一个物理线程池
	 *
	 * @param corePoolSize 核心线程池
	 * @param keepAliveTime 线程超时时间
	 * @param unit 超时时间单位
	 * @param workQueue 任务队列
	 */
	CommandThreadPoolExecutor(int corePoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Command> workQueue)
	{
		if (keepAliveTime < 0 || corePoolSize <= 0)
		{
			throw new IllegalArgumentException();
		}
		if (workQueue == null)
		{
			throw new NullPointerException();
		}
		this.corePoolSize = corePoolSize;
		this.workQueue = workQueue;
		this.keepAliveTime = unit.toNanos(keepAliveTime);
		this.threadFactory = Executors.defaultThreadFactory();
		this.maximumPoolSize = corePoolSize;
	}

	// Packing and unpacking ctl
	private static int runStateOf(int c)
	{
		return c & ~CAPACITY;
	}


	private static int workerCountOf(int c)
	{
		return c & CAPACITY;
	}

	private static int ctlOf(int rs, int wc)
	{
		return rs | wc;
	}

	private static boolean runStateLessThan(int c, int s)
	{
		return c < s;
	}

	private static boolean runStateAtLeast(int c, int s)
	{
		return c >= s;
	}

	private static boolean isRunning(int c)
	{
		return c < SHUTDOWN;
	}

	/**
	 * At tempts to CAS-increment the workerCount field of ctl.
	 */
	private boolean compareAndIncrementWorkerCount(int expect)
	{
		return ctl.compareAndSet(expect, expect + 1);
	}

	/**
	 * Attempts to CAS-decrement the workerCount field of ctl.
	 */
	private boolean compareAndDecrementWorkerCount(int expect)
	{
		return ctl.compareAndSet(expect, expect - 1);
	}

	/**
	 * 线程退出，需要修改workCount
	 */
	private void decrementWorkerCount()
	{
		do
		{
		}
		while (!compareAndDecrementWorkerCount(ctl.get()));
	}

	public void setWorkerListener(IWorkerListener workerListener)
	{
		this.workerListener = workerListener;
	}

	/**
	 * Transitions runState to given target, or leaves it alone if already at least
	 * the given
	 * target.
	 *
	 * @param targetState the desired state, either SHUTDOWN or STOP (but not
	 *            TIDYING or TERMINATED
	 *            -- use
	 *            tryTerminate for that)
	 */
	private void advanceRunState(int targetState)
	{
		for (;;)
		{
			int c = ctl.get();
			if (runStateAtLeast(c, targetState) || ctl.compareAndSet(c, ctlOf(targetState, workerCountOf(c))))
			{
				break;
			}
		}
	}

	/**
	 * Transitions to TERMINATED state if either (SHUTDOWN and pool and queue empty)
	 * or (STOP and
	 * pool empty). If
	 * otherwise eligible to terminate but workerCount is nonzero, interrupts an
	 * idle worker to
	 * ensure that shutdown
	 * signals propagate. This method must be called following any action that might
	 * make
	 * termination possible --
	 * reducing worker count or removing tasks from the queue during shutdown. The
	 * method is
	 * non-private to allow access
	 * from ScheduledThreadPoolExecutor.
	 */
	final void tryTerminate()
	{
		for (;;)
		{
			int c = ctl.get();
			if (isRunning(c) || runStateAtLeast(c, TIDYING) || (runStateOf(c) == SHUTDOWN && !workQueue.isEmpty()))
			{
				return;
			}
			if (hasWorker(c))
			{ // Eligible to terminate
				interruptIdleWorkers(ONLY_ONE);
				return;
			}

			final ReentrantLock mainLock = this.mainLock;
			mainLock.lock();
			try
			{
				if (ctl.compareAndSet(c, ctlOf(TIDYING, 0)))
				{
					try
					{
						terminated();
					}
					finally
					{
						ctl.set(ctlOf(TERMINATED, 0));
						termination.signalAll();
					}
					return;
				}
			}
			finally
			{
				mainLock.unlock();
			}
			// else retry on failed CAS
		}
	}

	/**
	 * If there is a security manager, makes sure caller has permission to shut down
	 * threads in
	 * general (see
	 * shutdownPerm). If this passes, additionally makes sure the caller is allowed
	 * to interrupt
	 * each worker thread.
	 * This might not be true even if first check passed, if the SecurityManager
	 * treats some threads
	 * specially.
	 */
	private void checkShutdownAccess()
	{
		SecurityManager security = System.getSecurityManager();
		if (security != null)
		{
			security.checkPermission(shutdownPerm);
			final ReentrantLock mainLock = this.mainLock;
			mainLock.lock();
			try
			{
				for (Worker w : workers)
				{
					security.checkAccess(w.thread);
				}
			}
			finally
			{
				mainLock.unlock();
			}
		}
	}

	/**
	 * Interrupts all threads, even if active. Ignores SecurityExceptions (in which
	 * case some
	 * threads may remain
	 * uninterrupted).
	 */
	private void interruptWorkers()
	{
		final ReentrantLock mainLock = this.mainLock;
		mainLock.lock();
		try
		{
			for (Worker w : workers)
			{
				w.interruptIfStarted();
			}
		}
		finally
		{
			mainLock.unlock();
		}
	}

	/**
	 * 退出空闲线程
	 *
	 * @param onlyOne 只退出一个
	 */
	private void interruptIdleWorkers(boolean onlyOne)
	{
		final ReentrantLock mainLock = this.mainLock;
		mainLock.lock();
		try
		{
			for (Worker w : workers)
			{
				Thread t = w.thread;
				if (!t.isInterrupted() && w.tryLock())
				{
					try
					{
						/**
						 * 会触发{@link #getTask()}catch (InterruptedException)
						 */
						t.interrupt();
					}
					catch (SecurityException ignore)
					{
						//do nothing
					}
					finally
					{
						w.unlock();
					}
				}
				if (onlyOne)
				{
					break;
				}
			}
		}
		finally
		{
			mainLock.unlock();
		}
	}

	/**
	 * Common form of interruptIdleWorkers, to avoid having to remember what the
	 * boolean argument
	 * means.
	 */
	private void interruptIdleWorkers()
	{
		interruptIdleWorkers(false);
	}

	/**
	 * Invokes the rejected execution handler for the given command.
	 * Package-protected for use by
	 * ScheduledThreadPoolExecutor.
	 */
	final void reject(Command command)
	{
		if (command != null)
		{
			command.commandPool.onReject(command);
		}
	}

	/**
	 * Performs any further cleanup following run state transition on invocation of
	 * shutdown. A
	 * no-op here, but used by
	 * ScheduledThreadPoolExecutor to cancel delayed tasks.
	 */
	void onShutdown()
	{
	}

	/**
	 * State check needed by ScheduledThreadPoolExecutor to enable running tasks
	 * during shutdown.
	 *
	 * @param shutdownOK true if should return true if SHUTDOWN
	 */
	final boolean isRunningOrShutdown(boolean shutdownOK)
	{
		int rs = runStateOf(ctl.get());
		return rs == RUNNING || (rs == SHUTDOWN && shutdownOK);
	}

	/**
	 * Drains the task queue into a new list, normally using drainTo. But if the
	 * queue is a
	 * DelayQueue or any other kind
	 * of queue for which poll or drainTo may fail to remove some elements, it
	 * deletes them one by
	 * one.
	 */
	private List<Command> drainQueue()
	{
		BlockingQueue<Command> q = workQueue;
		ArrayList<Command> taskList = new ArrayList<>();
		q.drainTo(taskList);
		if (!q.isEmpty())
		{
			for (Command r : q.toArray(new Command[0]))
			{
				if (q.remove(r))
				{
					taskList.add(r);
				}
			}
		}
		return taskList;
	}

	/**
	 * 检查是否需要创建一个新的工作线程worker
	 * 如果需要创建一个新的线程，会执行compareAndIncrementWorkerCount ，然后会调用addNewWorker
	 */
	private boolean addWorker(Command firstTask, boolean core)
	{
		retry: for (;;)
		{
			int c = ctl.get();
			int rs = runStateOf(c);

			// Check if queue empty only if necessary.
			if (noNeedAdd(firstTask, rs))
			{
				return false;
			}

			for (;;)
			{
				int wc = workerCountOf(c);
				if (wc >= CAPACITY || wc >= (core ? corePoolSize : maximumPoolSize))
				{
					return false;
				}
				if (compareAndIncrementWorkerCount(c))
				{
					break retry;
				}
				c = ctl.get(); // Re-read ctl
				if (runStateOf(c) != rs)
				{
					continue retry;
				}
				// else CAS failed due to workerCount change; retry inner loop
			}
		}

		return addNewWorker(firstTask);
	}

	/**
	 * check 是否有必要创建worker
	 */
	private boolean noNeedAdd(Command firstTask, int rs)
	{
		return rs >= SHUTDOWN && !(rs == SHUTDOWN && firstTask == null && !workQueue.isEmpty());
	}

	/**
	 * 创建新的线程，并启动它，这个函数调用后，会执行{@link #runWorker}
	 *
	 * @param firstTask 待执行待任务
	 * @return 是否创建成功
	 */
	private boolean addNewWorker(Command firstTask)
	{
		boolean workerStarted = false;
		boolean workerAdded = false;
		Worker w = null;
		try
		{
			w = new Worker(this, firstTask);
			final Thread t = w.thread;
			if (t != null)
			{
				final ReentrantLock mainLock = this.mainLock;
				mainLock.lock();
				try
				{
					// Recheck while holding lock.
					// Back out on ThreadFactory failure or if
					// shut down before lock acquired.
					int rs = runStateOf(ctl.get());

					if (rs < SHUTDOWN || (rs == SHUTDOWN && firstTask == null))
					{
						if (t.isAlive())
						{ // precheck that t is startable
							throw new IllegalThreadStateException();
						}
						workers.add(w);
						int s = workers.size();
						if (s > largestPoolSize)
						{
							largestPoolSize = s;
						}
						workerAdded = true;
					}
				}
				finally
				{
					mainLock.unlock();
				}
				if (workerAdded)
				{
					try
					{
						t.start();
						workerStarted = true;
						notifyWorkerAdd(w);
					}
					catch (OutOfMemoryError e)
					{
					}
				}
			}
		}
		finally
		{
			if (!workerStarted)
			{
				addWorkerFailed(w);
			}
		}
		return workerStarted;
	}

	/**
	 * 通知线程创建，用于上报统计
	 */
	private void notifyWorkerAdd(Worker w)
	{
		if (workerListener != null)
		{
			workerListener.onWorkerAdded(w.toString());
		}
	}

	/**
	 * Rolls back the worker thread creation. - removes worker from workers, if
	 * present - decrements
	 * worker count -
	 * rechecks for termination, in case the existence of this worker was holding up
	 * termination
	 */
	private void addWorkerFailed(Worker w)
	{
		final ReentrantLock mainLock = this.mainLock;
		mainLock.lock();
		try
		{
			if (w != null)
			{
				workers.remove(w);
			}
			decrementWorkerCount();
			tryTerminate();
		}
		finally
		{
			mainLock.unlock();
		}
	}

	/**
	 * Performs cleanup and bookkeeping for a dying worker. Called only from worker
	 * threads. Unless
	 * completedAbruptly is
	 * set, assumes that workerCount has already been adjusted to account for exit.
	 * This method
	 * removes thread from
	 * worker set, and possibly terminates the pool or replaces the worker if either
	 * it exited due
	 * to user task
	 * exception or if fewer than corePoolSize workers are running or queue is
	 * non-empty but there
	 * are no workers.
	 *
	 * @param w the worker
	 * @param completedAbruptly if the worker died due to user exception
	 */
	private void processWorkerExit(Worker w, boolean completedAbruptly)
	{
		if (completedAbruptly)
		{
			// If abrupt, then workerCount wasn't adjusted
			decrementWorkerCount();
		}
		removeWorker(w);
		notifyWorkerExit(w);
		tryTerminate();
		checkReplaceExitedThread(completedAbruptly);
	}

	/**
	 * 将worker从workers里面剔除，并且计数completedTaskCount
	 */
	private void removeWorker(Worker w)
	{
		final ReentrantLock mainLock = this.mainLock;
		mainLock.lock();
		try
		{
			completedTaskCount += w.completedTasks;
			workers.remove(w);
		}
		finally
		{
			mainLock.unlock();
		}
	}

	/**
	 * 当线程退出当时候，是否有必要创建新的线程替换它，
	 *
	 * @param completedAbruptly 线程异常退出 比如任务内部逻辑出现了空指针异常等情况，
	 *
	 *            这种情况一般应用程序都崩溃了，其实替换线程池，也没有多大的必要
	 *
	 *            如果线程是interruptIdleWorkers，或者 getTask超时退出，这是正常的退出
	 *            ,completedAbruptly 为false
	 */

	private void checkReplaceExitedThread(boolean completedAbruptly)
	{
		if (completedAbruptly)
		{
			Log.d("ExitedThread", "Thread exit completedAbruptly");
		}
		int c = ctl.get();
		if (runStateAtLeast(c, STOP))
		{
			return;
		}
		if (completedAbruptly)
		{
			replaceThread();
		}
		else if (workerCountOf(c) < getMinSize())
		{
			replaceThread();
		}
	}

	/**
	 * 当线程退出的时候，不管是正常退出，还是异常退出，如果队列没有排队的任务，就不需要创建新的线程替换它
	 */
	private void replaceThread()
	{
		if (!workQueue.isEmpty())
		{
			addWorker(null, false);
		}
	}

	/**
	 * 线程池数量的最小要求
	 */
	private int getMinSize()
	{
		int min = allowCoreThreadTimeOut ? 0 : corePoolSize;
		if (min == 0 && !workQueue.isEmpty())
		{
			min = 1;
		}
		return min;
	}

	/**
	 * 通知线程退出，可用于统计上报
	 */
	private void notifyWorkerExit(Worker w)
	{
		if (workerListener != null)
		{
			workerListener.onWorkerExited(w.toString());
		}
	}

	/**
	 * Performs blocking or timed wait for a task, depending on current
	 * configuration settings, or
	 * returns null if this
	 * worker must exit because of any of: 1. There are more than maximumPoolSize
	 * workers (due to a
	 * call to
	 * setMaximumPoolSize). 2. The pool is stopped. 3. The pool is shutdown and the
	 * queue is empty.
	 * 4. This worker timed
	 * out waiting for a task, and timed-out workers are subject to termination
	 * (that is, {@code
	 * allowCoreThreadTimeOut
	 * || workerCount > corePoolSize}) both before and after the timed wait, and if
	 * the queue is
	 * non-empty, this worker
	 * is not the last thread in the pool.
	 *
	 * @return task, or null if the worker must exit, in which case workerCount is
	 *         decremented
	 */
	private Command getTask()
	{
		boolean timedOut = false; // Did the last poll() time out?

		for (;;)
		{
			int c = ctl.get();
			int rs = runStateOf(c);

			// Check if queue empty only if necessary.
			if (shouldExitWorker(rs))
			{
				decrementWorkerCount();
				return null;// 不再执行任务调度,会退出当前线程
			}

			int wc = workerCountOf(c);

			// 当前worker是否应该销毁
			if (shouldExitThisWorker(timedOut, wc))
			{
				if (compareAndDecrementWorkerCount(c))
				{
					return null;//当前线程不再存在的必要，退出当前线程
				}
				continue;
			}
			try
			{
				waitWorkerCount.incrementAndGet();
				Command r = allowCoreThreadTimeOut || wc > corePoolSize ? workQueue.poll(keepAliveTime, TimeUnit.NANOSECONDS) : workQueue.take();
				waitWorkerCount.decrementAndGet();
				if (r != null)
				{
					return r;
				}
				timedOut = true;
			}
			catch (InterruptedException retry)
			{
				waitWorkerCount.decrementAndGet();
				timedOut = false;
			}
		}
	}

	/**
	 * 是否需要退出当前worker线程
	 */
	private boolean shouldExitWorker(int rs)
	{
		return rs >= SHUTDOWN && (rs >= STOP || workQueue.isEmpty());
	}

	/**
	 * 是否退出当前线程
	 *
	 * 1、wc > maximumPoolSize：工作线程大于核心线程
	 *
	 * 2、(allowCoreThreadTimeOut || wc > corePoolSize) && timedOut
	 * ：上次workQueue.pool超时
	 *
	 * 3、wc > 1 || workQueue.isEmpty() 当前有工作线程 或者 没有等待任务，
	 *
	 * @param timedOut workQueue.poll获取队列任务发生了超时
	 * @param wc 工作worker数量
	 */
	private boolean shouldExitThisWorker(boolean timedOut, int wc)
	{
		boolean maximumExceeded = wc > maximumPoolSize;
		boolean poolTimeOut = (allowCoreThreadTimeOut || wc > corePoolSize) && timedOut;
		return (maximumExceeded || poolTimeOut) && (wc > 1 || workQueue.isEmpty());
	}

	/**
	 * Main worker run loop.
	 * 所有任务都是从这里开始执行
	 *
	 * 目前这里任务的获取有两个队列，
	 *
	 * 一个来自getTask，Command来自于workQueue；
	 *
	 * 一个来自getWaitingCommand，Command来自commandPools
	 *
	 * @param w the worker
	 */
	final void runWorker(Worker w)
	{
		Thread wt = Thread.currentThread();
		Command task = w.firstTask;
		boolean isTaskCompleted = false;
		w.firstTask = null;
		w.unlock(); // allow interrupts
		boolean completedAbruptly = true;
		try
		{
			while (task != null || (task = getTask()) != null)
			{
				w.lock();
				isTaskCompleted = false;
				// If pool is stopping, ensure thread is interrupted;
				// if not, ensure thread is not interrupted.  This
				// requires a recheck in second case to deal with
				// shutdownNow race while clearing interrupt
				if (interruptWorker(wt))
				{
					wt.interrupt();
				}
				try
				{
					beforeExecute(task);
					Throwable thrown = null;
					try
					{
						task.runnable.run();
					}
					catch (RuntimeException x)
					{
						thrown = x;
						throw x;
					}
					catch (Error x)
					{
						thrown = x;
						throw x;
					}
					catch (Throwable x)
					{
						thrown = x;
						throw new Error(x);
					}
					finally
					{
						afterExecute(task, thrown);
					}
				}
				finally
				{
					isTaskCompleted = true;
					w.completedTasks++;
					task.commandPool.onCmdExecuted(task);
					task = getWaitingCommand(task);
					w.unlock();
				}
			}
			completedAbruptly = false;
		}
		finally
		{
			//interrupt会走到这里，如果task没有执行，需要reject
			if (!isTaskCompleted && task != null)
			{
				reject(task);
			}
			processWorkerExit(w, completedAbruptly);
		}
	}

	/**
	 * 线程池如果已经关闭了，该线程需要自我退出，不需要再执行task
	 */
	private boolean interruptWorker(Thread wt)
	{
		return (runStateAtLeast(ctl.get(), STOP) || (Thread.interrupted() && runStateAtLeast(ctl.get(), STOP))) && !wt.isInterrupted();
	}


	/**
	 * 注册一个新的任务队列，每注册一个新的队列都要更新maximumPoolSize，确保executor能满足业务对并发线程数量的要求。
	 */
	void registerCommandPool(ICommandPool commandPool)
	{
		if (commandPool == null || commandPool.getMaximumThreadSize() <= 0)
		{
			throw new IllegalArgumentException();
		}
		synchronized (commandPoolsLock)
		{
			commandPools.offer(commandPool);
		}
		updateMaximumPoolSize();
	}

	/**
	 * 取消一个任务队列，取消后，需要更新maximumPoolSize
	 *
	 * @param commandPool 取消注册的任务队列
	 */
	void unRegisterCommandPool(ICommandPool commandPool)
	{
		synchronized (commandPoolsLock)
		{
			if (commandPools.remove(commandPool))
			{
				updateMaximumPoolSize();
			}
		}
	}

	/**
	 * 更新当前线程池最大的线程池数量，最大线程池数量有CommandPool的maximumThreadSize之和决定
	 */
	void updateMaximumPoolSize()
	{
		synchronized (commandPoolsLock)
		{
			Iterator<ICommandPool> cmdProviders = commandPools.iterator();
			long maximumPoolSize = 0;
			while (cmdProviders.hasNext())
			{
				ICommandPool provider = cmdProviders.next();
				if (provider != null)
				{
					maximumPoolSize += provider.getMaximumThreadSize();
				}
			}
			int finalMaxPoolSize = (int) Math.min(maximumPoolSize, Integer.MAX_VALUE);
			setMaximumPoolSize(Math.max(corePoolSize, finalMaxPoolSize));
		}
	}

	/**
	 * 用于单元测试接口，外部不需要访问
	 */
	ICommandPool getFirstCommandPool()
	{
		PriorityQueue<ICommandPool> pools;
		synchronized (commandPoolsLock)
		{
			pools = new PriorityQueue<>(commandPools);
		}
		Iterator<ICommandPool> cmdProviders = pools.iterator();
		while (cmdProviders.hasNext())
		{
			return cmdProviders.next();
		}
		return null;
	}

	/**
	 * 返回CommandPool队列，外部不需要访问
	 */
	Queue<ICommandPool> getCommandPools()
	{
		synchronized (commandPoolsLock)
		{
			return new PriorityQueue<>(commandPools);
		}
	}

	/**
	 * 获取CommandPool队列commandPools中获取等待的执行任务，
	 *
	 * 优先从已经执行的task对于的CommandPool取等待的任务，如果task对于的CommandPool中没有可以执行的，就
	 *
	 * 在剩下的CommandPool中查询等待执行的Command
	 */
	Command getWaitingCommand(Command task)
	{
		Command waitingCmd = null;
		if (task.commandPool != null)
		{
			waitingCmd = task.commandPool.takeWaitingCommand();
		}
		if (waitingCmd == null)
		{
			PriorityQueue<ICommandPool> pools;
			synchronized (commandPoolsLock)
			{
				pools = new PriorityQueue<>(commandPools);
			}
			Iterator<ICommandPool> cmdProviders = pools.iterator();
			while (cmdProviders.hasNext())
			{
				ICommandPool provider = cmdProviders.next();
				if (provider != null && provider != task.commandPool)
				{
					waitingCmd = provider.takeWaitingCommand();
					if (waitingCmd != null)
					{
						break;
					}
				}
			}
		}
		return waitingCmd;
	}

	/**
	 * Executes the given task sometime in the future. The task may execute in a new
	 * thread or in an
	 * existing pooled
	 * thread.
	 * <p>
	 * If the task cannot be submitted for execution, either because this executor
	 * has been shutdown
	 * or because its
	 * capacity has been reached, the task is handled by the current {@code
	 * CommandRejectedExecutionHandler}.
	 *
	 * @param command the task to execute
	 * @throws RejectedExecutionException at discretion of
	 *             {@code CommandRejectedExecutionHandler},
	 *             if the task
	 *             cannot be accepted for execution
	 * @throws NullPointerException if {@code command} is null
	 */
	public void execute(Command command)
	{
		if (command == null)
		{
			throw new NullPointerException();
		}
		int c = ctl.get();
		if (canCreateCoreThread(c))
		{
			if (addWorker(command, true))
			{
				return;
			}
			c = ctl.get();
		}
		if (hasIdleWorker())
		{
			if (isRunning(c) && workQueue.offer(command))
			{
				if (isWorkerEmpty())
				{
					//再次check线程池是否是空的，为空就创建一个
					addWorker(null, false);
				}
			}
			else
			{
				executeCreate(command);
			}
		}
		else
		{
			executeCreate(command);
		}
	}

	/**
	 * 没有任何线程，即没有空闲线程，也没有正在执行的线程
	 *
	 * @return true 无线程 false 还有线程
	 */
	private boolean isWorkerEmpty()
	{
		return !hasWorker(ctl.get());
	}

	/**
	 * 核心线程还没有用满，直接创建新的线程执行command
	 */
	private boolean canCreateCoreThread(int c)
	{
		return workerCountOf(c) < corePoolSize;
	}

	private boolean hasWorker(int workerCount)
	{
		return workerCountOf(workerCount) != 0;
	}

	/**
	 * 是否有空闲的线程在等待执行command
	 */
	private boolean hasIdleWorker()
	{
		return waitWorkerCount.intValue() > 0;
	}

	/**
	 * 返回空闲等待的线程池数量，仅供单元测试用
	 *
	 * @return 空闲线程的数量
	 */
	int waitWorkerCount()
	{
		return waitWorkerCount.get();
	}

	/**
	 * 就创建线程去执行command，如果最大线程池没有满，就直接addWorker
	 *
	 * 如果最大线程池满，就入队列等待
	 *
	 * @param command 待执行的任务
	 */
	private void executeCreate(Command command)
	{
		int c = ctl.get();
		if ((workerCountOf(c) < maximumPoolSize))
		{
			if (addWorker(command, false))
			{
				return;
			}
			c = ctl.get();
		}
		if (isRunning(c) && workQueue.offer(command))
		{
			int recheck = ctl.get();
			if (!isRunning(recheck) && remove(command))
			{
				reject(command);
			}
			else if (!hasWorker(recheck))
			{
				addWorker(null, false);
			}
		}
		else
		{
			reject(command);
		}
	}

	/**
	 * Initiates an orderly shutdown in which previously submitted tasks are
	 * executed, but no new
	 * tasks will be
	 * accepted. Invocation has no additional effect if already shut down.
	 *
	 * <p>
	 * This method does not wait for previously submitted tasks to complete
	 * execution. Use {@link
	 * #awaitTermination
	 * awaitTermination} to do that.
	 */
	// android-note: Removed @throws SecurityException
	public void shutdown()
	{
		final ReentrantLock mainLock = this.mainLock;
		mainLock.lock();
		try
		{
			checkShutdownAccess();
			advanceRunState(SHUTDOWN);
			interruptIdleWorkers();
			onShutdown(); // hook for ScheduledThreadPoolExecutor
		}
		finally
		{
			mainLock.unlock();
		}
		tryTerminate();
	}

	/**
	 * Attempts to stop all actively executing tasks, halts the processing of
	 * waiting tasks, and
	 * returns a list of the
	 * tasks that were awaiting execution. These tasks are drained (removed) from
	 * the task queue
	 * upon return from this
	 * method.
	 *
	 * <p>
	 * This method does not wait for actively executing tasks to terminate. Use
	 * {@link
	 * #awaitTermination
	 * awaitTermination} to do that.
	 *
	 * <p>
	 * There are no guarantees beyond best-effort attempts to stop processing
	 * actively executing
	 * tasks. This
	 * implementation interrupts tasks via {@link Thread#interrupt}; any task that
	 * fails to respond
	 * to interrupts may
	 * never terminate.
	 */
	// android-note: Removed @throws SecurityException
	public List<Command> shutdownNow()
	{
		List<Command> tasks;
		final ReentrantLock mainLock = this.mainLock;
		mainLock.lock();
		try
		{
			checkShutdownAccess();
			advanceRunState(STOP);
			interruptWorkers();
			tasks = drainQueue();
		}
		finally
		{
			mainLock.unlock();
		}
		tryTerminate();
		return tasks;
	}

	public boolean isShutdown()
	{
		return !isRunning(ctl.get());
	}

	/**
	 * Returns true if this executor is in the process of terminating after
	 * {@link #shutdown} or
	 * {@link #shutdownNow}
	 * but has not completely terminated. This method may be useful for debugging. A
	 * return of
	 * {@code true} reported a
	 * sufficient period after shutdown may indicate that submitted tasks have
	 * ignored or suppressed
	 * interruption,
	 * causing this executor not to properly terminate.
	 *
	 * @return {@code true} if terminating but not yet terminated
	 */
	public boolean isTerminating()
	{
		int c = ctl.get();
		return !isRunning(c) && runStateLessThan(c, TERMINATED);
	}

	public boolean isTerminated()
	{
		return runStateAtLeast(ctl.get(), TERMINATED);
	}

	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException
	{
		long nanos = unit.toNanos(timeout);
		final ReentrantLock mainLock = this.mainLock;
		mainLock.lock();
		try
		{
			while (!runStateAtLeast(ctl.get(), TERMINATED))
			{
				if (nanos <= 0L)
				{
					return false;
				}
				nanos = termination.awaitNanos(nanos);
			}
			return true;
		}
		finally
		{
			mainLock.unlock();
		}
	}

	/**
	 * Invokes {@code shutdown} when this executor is no longer referenced and it
	 * has no threads.
	 */
	protected void finalize()
	{
		shutdown();
	}

	/**
	 * Returns the thread factory used to create new threads.
	 *
	 * @return the current thread factory
	 * @see #setThreadFactory(ThreadFactory)
	 */
	public ThreadFactory getThreadFactory()
	{
		return threadFactory;
	}

	/**
	 * Sets the thread factory used to create new threads.
	 *
	 * @param threadFactory the new thread factory
	 * @throws NullPointerException if threadFactory is null
	 * @see #getThreadFactory
	 */
	public void setThreadFactory(ThreadFactory threadFactory)
	{
		if (threadFactory == null)
		{
			throw new NullPointerException();
		}
		this.threadFactory = threadFactory;
	}

	/**
	 * Returns the core number of threads.
	 *
	 * @return the core number of threads
	 * @see #setCorePoolSize
	 */
	public int getCorePoolSize()
	{
		return corePoolSize;
	}

	/**
	 * 更新核心线程数量
	 */
	public void setCorePoolSize(int corePoolSize)
	{
		if (corePoolSize < 0)
		{
			throw new IllegalArgumentException();
		}
		int delta = corePoolSize - this.corePoolSize;
		this.corePoolSize = corePoolSize;
		if (workerCountOf(ctl.get()) > corePoolSize)
		{
			interruptIdleWorkers();
		}
		else if (delta > 0)
		{
			// We don't really know how many new threads are "needed".
			// As a heuristic, prestart enough new workers (up to new
			// core size) to handle the current number of tasks in
			// queue, but stop if queue becomes empty while doing so.
			int k = Math.min(delta, workQueue.size());
			while (k-- > 0 && addWorker(null, true))
			{
				if (workQueue.isEmpty())
				{
					break;
				}
			}
		}
	}

	/**
	 * 预加载一个核心线程池数量
	 */
	public boolean prestartCoreThread()
	{
		return workerCountOf(ctl.get()) < corePoolSize && addWorker(null, true);
	}

	/**
	 * Same as prestartCoreThread except arranges that at least one thread is
	 * started even if
	 * corePoolSize is 0.
	 */
	void ensurePrestart()
	{
		int wc = workerCountOf(ctl.get());
		if (wc < corePoolSize)
		{
			addWorker(null, true);
		}
		else if (wc == 0)
		{
			addWorker(null, false);
		}
	}

	/**
	 * 核心线程预加载
	 */
	public int prestartAllCoreThreads()
	{
		int n = 0;
		while (addWorker(null, true))
		{
			++n;
		}
		return n;
	}

	/**
	 * 返回核心线程是否允许超时
	 */
	public boolean allowsCoreThreadTimeOut()
	{
		return allowCoreThreadTimeOut;
	}

	/**
	 * 允许核心线程池超时
	 */
	public void allowCoreThreadTimeOut(boolean value)
	{
		if (value && keepAliveTime <= 0)
		{
			throw new IllegalArgumentException("Core threads must have nonzero keep alive times");
		}
		if (value != allowCoreThreadTimeOut)
		{
			allowCoreThreadTimeOut = value;
			if (value)
			{
				interruptIdleWorkers();
			}
		}
	}

	/**
	 * Returns the maximum allowed number of threads.
	 *
	 * @return the maximum allowed number of threads
	 * @see #setMaximumPoolSize
	 */
	public int getMaximumPoolSize()
	{
		return maximumPoolSize;
	}

	/**
	 * 每次有新接入的CommandPOo，都会更新线程最大数量
	 */
	private void setMaximumPoolSize(int maximumPoolSize)
	{
		if (maximumPoolSize <= 0 || maximumPoolSize < corePoolSize)
		{
			throw new IllegalArgumentException();
		}
		this.maximumPoolSize = maximumPoolSize;
		if (workerCountOf(ctl.get()) > maximumPoolSize)
		{
			interruptIdleWorkers();
		}
	}

	/**
	 * 设置线程超时时间
	 *
	 * @param time 时间
	 * @param unit 单位
	 */
	public void setKeepAliveTime(long time, TimeUnit unit)
	{
		if (time < 0)
		{
			throw new IllegalArgumentException();
		}
		if (time == 0 && allowsCoreThreadTimeOut())
		{
			throw new IllegalArgumentException("Core threads must have nonzero keep alive times");
		}
		long keepAliveTime = unit.toNanos(time);
		long delta = keepAliveTime - this.keepAliveTime;
		this.keepAliveTime = keepAliveTime;
		if (delta < 0)
		{
			interruptIdleWorkers();
		}
	}

	/**
	 * 返回超时时间
	 */
	public long getKeepAliveTime(TimeUnit unit)
	{
		return unit.convert(keepAliveTime, TimeUnit.NANOSECONDS);
	}

	/**
	 * 从任务队列删除一个任务
	 */
	public boolean remove(Command task)
	{
		boolean removed = workQueue.remove(task);
		tryTerminate();
		return removed;
	}

	/**
	 * @return 返回所有存活的线程池，包括空闲线程和非空闲线程
	 */
	public int getPoolSize()
	{
		final ReentrantLock mainLock = this.mainLock;
		mainLock.lock();
		try
		{
			return runStateAtLeast(ctl.get(), TIDYING) ? 0 : workers.size();
		}
		finally
		{
			mainLock.unlock();
		}
	}

	/**
	 * @return 返回正在运行的线程，也就是当前繁忙的线程，非空闲线程
	 */
	public int getActiveCount()
	{
		final ReentrantLock mainLock = this.mainLock;
		mainLock.lock();
		try
		{
			int n = 0;
			for (Worker w : workers)
			{
				if (w.isLocked())
				{
					++n;
				}
			}
			return n;
		}
		finally
		{
			mainLock.unlock();
		}
	}

	/**
	 * Returns the largest number of threads that have ever simultaneously been in
	 * the pool.
	 *
	 * @return the number of threads
	 */
	public int getLargestPoolSize()
	{
		final ReentrantLock mainLock = this.mainLock;
		mainLock.lock();
		try
		{
			return largestPoolSize;
		}
		finally
		{
			mainLock.unlock();
		}
	}

	/**
	 * Returns the approximate total number of tasks that have ever been scheduled
	 * for execution.
	 * Because the states of
	 * tasks and threads may change dynamically during computation, the returned
	 * value is only an
	 * approximation.
	 *
	 * @return the number of tasks
	 */
	public long getTaskCount()
	{
		final ReentrantLock mainLock = this.mainLock;
		mainLock.lock();
		try
		{
			long n = completedTaskCount;
			for (Worker w : workers)
			{
				n += w.completedTasks;
				if (w.isLocked())
				{
					++n;
				}
			}
			return n + workQueue.size();
		}
		finally
		{
			mainLock.unlock();
		}
	}

	/**
	 * @return 返回任务完成总数
	 */
	public long getCompletedTaskCount()
	{
		final ReentrantLock mainLock = this.mainLock;
		mainLock.lock();
		try
		{
			long n = completedTaskCount;
			for (Worker w : workers)
			{
				n += w.completedTasks;
			}
			return n;
		}
		finally
		{
			mainLock.unlock();
		}
	}

	/**
	 * Returns a string identifying this pool, as well as its state, including
	 * indications of run
	 * state and estimated
	 * worker and task counts.
	 *
	 * @return a string identifying this pool, as well as its state
	 */
	public String toString()
	{
		long completed;
		int workers;
		int active;
		final ReentrantLock mainLock = this.mainLock;
		mainLock.lock();
		try
		{
			completed = completedTaskCount;
			active = 0;
			workers = this.workers.size();
			for (Worker w : this.workers)
			{
				completed += w.completedTasks;
				if (w.isLocked())
				{
					++active;
				}
			}
		}
		finally
		{
			mainLock.unlock();
		}
		int c = ctl.get();
		String runState = runStateLessThan(c, SHUTDOWN) ? "Running" : runStateAtLeast(c, TERMINATED) ? "Terminated" : "Shutting down";
		return "Executor" + "[" + runState + ", totalWorkerSize = " + workers + ", activeWorkerSize = " + active + ", idleWorkerSize = "
				+ waitWorkerCount + ", queued tasks = " + workQueue.size() + ", completed tasks = " + completed + ", maxPoolSize = " + maximumPoolSize
				+ ", corePoolSize = " + corePoolSize + "]";
	}

	/* Extension hooks */

	/**
	 * 任务开始执行通知
	 */
	protected void beforeExecute(Command command)
	{
		if (command != null)
		{
			command.commandPool.beforeExecute(command);
		}
	}

	protected void afterExecute(Command r, Throwable t)
	{
		if (r != null)
		{
			r.commandPool.afterExecute(r);
		}
	}

	/**
	 * Method invoked when the Executor has terminated. Default implementation does
	 * nothing. Note:
	 * To properly nest
	 * multiple overridings, subclasses should generally invoke
	 * {@code super.terminated} within this
	 * method.
	 */
	protected void terminated()
	{
	}
}
