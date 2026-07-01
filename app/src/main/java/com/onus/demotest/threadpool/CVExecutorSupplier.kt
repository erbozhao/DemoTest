package com.onus.demotest.threadpool

import android.os.Process
import com.onus.demotest.threadpool.lib.CommandServiceManager
import com.onus.demotest.threadpool.lib.CommandThreadFactory
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.PriorityBlockingQueue
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

open class CVExecutorSupplier private constructor() {
    @Volatile
    private var mPictureExecutor: CVThreadPoolExecutor? = null
    private val mPictureExecutorLock = Any()

    @Volatile
    private var mHighPriorityNetworkExecutor: CVThreadPoolExecutor? = null
    private val mHighPriorityNetworkExecutorLock = Any()

    @Volatile
    private var mNetworkExecutor: CVThreadPoolExecutor? = null
    private val mNetworkExecutorLock = Any()

    @Volatile
    private var mCoreTaskExecutor: CVThreadPoolExecutor? = null
    private val mCoreTaskExecutorLock = Any()

    @Volatile
    private var mIoExecutor: CVThreadPoolExecutor? = null
    private val mIoExecutorLock = Any()

    @Volatile
    private var mDBExecutor: CVThreadPoolExecutor? = null
    private val mDBExecutorLock = Any()

    @Volatile
    private var mCpuBoundExecutor: CVThreadPoolExecutor? = null
    private val mCpuBoundExecutorLock = Any()

    @Volatile
    private var mImmediateExecutor: Executor? = null
    private val mImmediateExecutorLock = Any()

    @Volatile
    private var mMainThreadExecutor: CVMainThreadExecutor? = null
    private val mMainThreadExecutorLock = Any()

    @Volatile
    private var mShortTimeExecutor: CVThreadPoolExecutor? = null
    private val mShortTimeExecutorLock = Any()

    @Volatile
    private var mLongTimeExecutor: CVThreadPoolExecutor? = null
    private val mLongTimeExecutorLock = Any()

    private val mScheduledLock = Any()
    private var mScheduledExecutor: ScheduledExecutorService? = null

    init {
        CommandServiceManager.get().getCommandSupplier().init(15, 30, TimeUnit.SECONDS, null)
        try {
            NUM_CPU_BOUND_THREADS = Runtime.getRuntime().availableProcessors()
        } catch (_: Throwable) {
            NUM_CPU_BOUND_THREADS = 4
        }
        INNER_NUM_CPU_BOUND_THREADS = NUM_CPU_BOUND_THREADS
        if (INNER_NUM_CPU_BOUND_THREADS < 4) {
            INNER_NUM_CPU_BOUND_THREADS = 4
        }
    }

    fun getNumOfCPUBoundThreads(): Int {
        return NUM_CPU_BOUND_THREADS
    }

    fun getPictureExecutor(): CVExecutorService {
        if (mPictureExecutor == null) {
            synchronized(mPictureExecutorLock) {
                if (mPictureExecutor == null) {
                    mPictureExecutor = CVThreadPoolExecutor(
                        4,
                        CVThreadPoolExecutor.POOL_PRIORITY.DISPLAY,
                        LinkedBlockingQueue()
                    )
                }
            }
        }
        return mPictureExecutor!!
    }

    fun getHighPriorityNetworkExecutor(): CVExecutorService {
        if (mHighPriorityNetworkExecutor == null) {
            synchronized(mHighPriorityNetworkExecutorLock) {
                if (mHighPriorityNetworkExecutor == null) {
                    mHighPriorityNetworkExecutor = CVThreadPoolExecutor(
                        4,
                        CVThreadPoolExecutor.POOL_PRIORITY.URGENT_DISPLAY,
                        PriorityBlockingQueue(11, PriorityComparator.DEFAULT)
                    )
                }
            }
        }
        return mHighPriorityNetworkExecutor!!
    }

    fun getNetworkExecutor(): CVExecutorService {
        if (mNetworkExecutor == null) {
            synchronized(mNetworkExecutorLock) {
                if (mNetworkExecutor == null) {
                    mNetworkExecutor = CVThreadPoolExecutor(
                        4,
                        CVThreadPoolExecutor.POOL_PRIORITY.DISPLAY,
                        PriorityBlockingQueue(11, PriorityComparator.DEFAULT)
                    )
                }
            }
        }
        return mNetworkExecutor!!
    }

    fun getCoreTaskExecutor(): CVExecutorService {
        if (mCoreTaskExecutor == null) {
            synchronized(mCoreTaskExecutorLock) {
                if (mCoreTaskExecutor == null) {
                    mCoreTaskExecutor = CVThreadPoolExecutor(INNER_NUM_CPU_BOUND_THREADS + 1, LinkedBlockingQueue())
                }
            }
        }
        return mCoreTaskExecutor!!
    }

    fun getIOTaskExecutor(): CVExecutorService {
        if (mIoExecutor == null) {
            synchronized(mIoExecutorLock) {
                if (mIoExecutor == null) {
                    mIoExecutor = CVThreadPoolExecutor(4, LinkedBlockingQueue())
                }
            }
        }
        return mIoExecutor!!
    }

    fun getDBTaskExecutor(): CVExecutorService {
        if (mDBExecutor == null) {
            synchronized(mDBExecutorLock) {
                if (mDBExecutor == null) {
                    mDBExecutor = CVThreadPoolExecutor(4, LinkedBlockingQueue())
                }
            }
        }
        return mDBExecutor!!
    }

    fun getCpuBoundExecutor(): CVExecutorService {
        if (mCpuBoundExecutor == null) {
            synchronized(mCpuBoundExecutorLock) {
                if (mCpuBoundExecutor == null) {
                    mCpuBoundExecutor = CVThreadPoolExecutor(INNER_NUM_CPU_BOUND_THREADS + 1, LinkedBlockingQueue())
                }
            }
        }
        return mCpuBoundExecutor!!
    }

    fun getImmediateExecutor(): Executor {
        if (mImmediateExecutor == null) {
            synchronized(mImmediateExecutorLock) {
                if (mImmediateExecutor == null) {
                    mImmediateExecutor = ImmediateExecutor()
                }
            }
        }
        return mImmediateExecutor!!
    }

    fun getScheduledExecutor(): ScheduledExecutorService {
        if (mScheduledExecutor == null) {
            synchronized(mScheduledLock) {
                if (mScheduledExecutor == null) {
                    mScheduledExecutor = Executors.newSingleThreadScheduledExecutor(
                        CommandThreadFactory("Scheduled delay", Process.THREAD_PRIORITY_BACKGROUND)
                    )
                }
            }
        }
        return mScheduledExecutor!!
    }

    fun getMainThreadExecutor(): CVMainThreadExecutor {
        if (mMainThreadExecutor == null) {
            synchronized(mMainThreadExecutorLock) {
                if (mMainThreadExecutor == null) {
                    mMainThreadExecutor = CVMainThreadExecutor()
                }
            }
        }
        return mMainThreadExecutor!!
    }

    fun getShortTimeExecutor(): CVExecutorService {
        if (mShortTimeExecutor == null) {
            synchronized(mShortTimeExecutorLock) {
                if (mShortTimeExecutor == null) {
                    mShortTimeExecutor = CVThreadPoolExecutor(2, LinkedBlockingQueue())
                }
            }
        }
        return mShortTimeExecutor!!
    }

    fun getLongTimeExecutor(): CVExecutorService {
        if (mLongTimeExecutor == null) {
            synchronized(mLongTimeExecutorLock) {
                if (mLongTimeExecutor == null) {
                    mLongTimeExecutor = CVThreadPoolExecutor(2, LinkedBlockingQueue())
                }
            }
        }
        return mLongTimeExecutor!!
    }

    private class ImmediateExecutor : Executor {
        private val executionDepth = ThreadLocal<Int>()

        override fun execute(command: Runnable) {
            val depth = incrementDepth()
            try {
                if (depth <= MAX_DEPTH) {
                    command.run()
                } else {
                    forBackgroundTasks().execute(command)
                }
            } finally {
                decrementDepth()
            }
        }

        private fun incrementDepth(): Int {
            var oldDepth = executionDepth.get()
            if (oldDepth == null) {
                oldDepth = 0
            }
            val newDepth = oldDepth + 1
            executionDepth.set(newDepth)
            return newDepth
        }

        private fun decrementDepth(): Int {
            var oldDepth = executionDepth.get()
            if (oldDepth == null) {
                oldDepth = 0
            }
            val newDepth = oldDepth - 1
            if (newDepth == 0) {
                executionDepth.remove()
            } else {
                executionDepth.set(newDepth)
            }
            return newDepth
        }

        companion object {
            private const val MAX_DEPTH = 15
        }
    }

    companion object {
        @Volatile
        private var sInstance: CVExecutorSupplier? = null

        @JvmField
        var INNER_NUM_CPU_BOUND_THREADS = 1

        @JvmField
        var NUM_CPU_BOUND_THREADS = INNER_NUM_CPU_BOUND_THREADS

        @JvmStatic
        fun getInstance(): CVExecutorSupplier {
            if (sInstance == null) {
                synchronized(CVExecutorSupplier::class.java) {
                    if (sInstance == null) {
                        sInstance = CVExecutorSupplier()
                    }
                }
            }
            return sInstance!!
        }

        @JvmStatic
        fun forMainThreadTasks(): CVMainThreadExecutor {
            return getInstance().getMainThreadExecutor()
        }

        @JvmStatic
        fun forBackgroundTasks(): CVExecutorService {
            return getInstance().getCoreTaskExecutor()
        }

        @JvmStatic
        fun forDbTasks(): CVExecutorService {
            return getInstance().getDBTaskExecutor()
        }

        @JvmStatic
        fun forIoTasks(): CVExecutorService {
            return getInstance().getIOTaskExecutor()
        }

        @JvmStatic
        fun forPictureTasks(): CVExecutorService {
            return getInstance().getPictureExecutor()
        }

        @JvmStatic
        fun forCoreTasks(): CVExecutorService {
            return getInstance().getCoreTaskExecutor()
        }

        @JvmStatic
        fun forShortTimeTasks(): CVExecutorService {
            return getInstance().getShortTimeExecutor()
        }

        @JvmStatic
        fun forLongTimeTasks(): CVExecutorService {
            return getInstance().getLongTimeExecutor()
        }
    }
}
