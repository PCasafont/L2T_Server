/*
 * Copyright (C) 2004-2015 L2J Unity
 * 
 * This file is part of L2J Unity.
 * 
 * L2J Unity is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Unity is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2server.util.concurrent

import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.stream.Stream

/**
 * @author _dev_ (savormix)
 * @author NB4L1
 */
object ThreadPool {
    private val LOGGER = LoggerFactory.getLogger(ThreadPool::class.java)

    /**
     * Gets the scheduled thread pool executor.
     * @return the scheduled thread pool executor
     */
    lateinit var scheduledThreadPoolExecutor: ScheduledThreadPoolExecutor
        private set
    /**
     * Gets the thread pool executor.
     * @return the thread pool executor
     */
    lateinit var threadPoolExecutor: ThreadPoolExecutor
        private set

    /**
     * Gets a stream of all the thread pools.
     * @return the stream of all the thread pools
     */
    val threadPools: Stream<ThreadPoolExecutor>
        get() = Stream.of(scheduledThreadPoolExecutor, threadPoolExecutor)

    /**
     * Gets the thread pools stats.
     * @return the stats
     */
    // ScheduledThreadPoolExecutor has a fixed number of threads and maximumPoolSize has no effect
    val stats: List<String>
        get() {
            val list = ArrayList<String>(23)
            list.add("")
            list.add("Scheduled pool:")
            list.add("=================================================")
            list.add("\tgetActiveCount: ...... " + scheduledThreadPoolExecutor.activeCount)
            list.add("\tgetCorePoolSize: ..... " + scheduledThreadPoolExecutor.corePoolSize)
            list.add("\tgetPoolSize: ......... " + scheduledThreadPoolExecutor.poolSize)
            list.add("\tgetLargestPoolSize: .. " + scheduledThreadPoolExecutor.largestPoolSize)
            list.add("\tgetMaximumPoolSize: .. " + scheduledThreadPoolExecutor.corePoolSize)
            list.add("\tgetCompletedTaskCount: " + scheduledThreadPoolExecutor.completedTaskCount)
            list.add("\tgetQueuedTaskCount: .. " + scheduledThreadPoolExecutor.queue.size)
            list.add("\tgetTaskCount: ........ " + scheduledThreadPoolExecutor.taskCount)
            list.add("")
            list.add("Thread pool:")
            list.add("=================================================")
            list.add("\tgetActiveCount: ...... " + threadPoolExecutor.activeCount)
            list.add("\tgetCorePoolSize: ..... " + threadPoolExecutor.corePoolSize)
            list.add("\tgetPoolSize: ......... " + threadPoolExecutor.poolSize)
            list.add("\tgetLargestPoolSize: .. " + threadPoolExecutor.largestPoolSize)
            list.add("\tgetMaximumPoolSize: .. " + threadPoolExecutor.maximumPoolSize)
            list.add("\tgetCompletedTaskCount: " + threadPoolExecutor.completedTaskCount)
            list.add("\tgetQueuedTaskCount: .. " + threadPoolExecutor.queue.size)
            list.add("\tgetTaskCount: ........ " + threadPoolExecutor.taskCount)
            list.add("")
            return list
        }

    @Throws(Exception::class)
    fun initThreadPools(initializer: IThreadPoolInitializer) {
        if (::scheduledThreadPoolExecutor.isInitialized || ::threadPoolExecutor.isInitialized) {
            throw Exception("The thread pool has been already initialized!")
        }

        scheduledThreadPoolExecutor = ScheduledThreadPoolExecutor(initializer.scheduledThreadPoolSize, PoolThreadFactory("L2JU-SP-", Thread.NORM_PRIORITY))
        threadPoolExecutor = ThreadPoolExecutor(initializer.threadPoolSize, initializer.threadPoolSize, 1, TimeUnit.MINUTES, LinkedBlockingQueue(), PoolThreadFactory("L2JU-IT-", Thread.NORM_PRIORITY))

        threadPools.forEach { tp ->
            tp.rejectedExecutionHandler = RejectedExecutionHandlerImpl()
            tp.prestartAllCoreThreads()
        }

        scheduleAtFixedRate({ purge() }, 1, 1, TimeUnit.MINUTES)

        LOGGER.info("Initialized with")
        LOGGER.info("\t... " + scheduledThreadPoolExecutor.poolSize + "/" + scheduledThreadPoolExecutor.poolSize + " scheduled thread(s).") // ScheduledThreadPoolExecutor has a fixed number of threads and maximumPoolSize has no effect
        LOGGER.info("\t... " + threadPoolExecutor.poolSize + "/" + threadPoolExecutor.maximumPoolSize + " thread(s).")
    }

    /**
     * Schedules a task to be executed after the given delay.
     * @param task the task to execute
     * @param delay the delay in the given time unit
     * @param timeUnit the time unit of the delay parameter
     * @return a ScheduledFuture representing pending completion of the task, and whose get() method will throw an exception upon cancellation
     */
    @JvmOverloads
    fun schedule(task: () -> Unit, delay: Long, timeUnit: TimeUnit = TimeUnit.MILLISECONDS): ScheduledFuture<*> {
        return scheduledThreadPoolExecutor.schedule(RunnableWrapper(task), delay, timeUnit)
    }

    /**
     * Schedules a task to be executed after the given delay at fixed rate.
     * @param task the task to execute
     * @param delay the delay in the given time unit
     * @param period the period in the given time unit
     * @param timeUnit the time unit of the delay parameter
     * @return a ScheduledFuture representing pending completion of the task, and whose get() method will throw an exception upon cancellation
     */
    @JvmOverloads
    fun scheduleAtFixedRate(task: () -> Unit, delay: Long, period: Long, timeUnit: TimeUnit = TimeUnit.MILLISECONDS): ScheduledFuture<*> {
        return scheduledThreadPoolExecutor.scheduleAtFixedRate(RunnableWrapper(task), delay, period, timeUnit)
    }

    /**
     * Schedules a task to be executed after the given delay with fixed delay.
     * @param task the task to execute
     * @param delay the delay in the given time unit
     * @param period the period in the given time unit
     * @param timeUnit the time unit of the delay parameter
     * @return a ScheduledFuture representing pending completion of the task, and whose get() method will throw an exception upon cancellation
     */
    @JvmOverloads
    fun scheduleWithFixedDelay(task: () -> Unit, delay: Long, period: Long, timeUnit: TimeUnit = TimeUnit.MILLISECONDS): ScheduledFuture<*> {
        return scheduledThreadPoolExecutor.scheduleWithFixedDelay(RunnableWrapper(task), delay, period, timeUnit)
    }

    /**
     * Executes the given task sometime in the future.
     * @param task the task to execute
     */
    fun execute(task: () -> Unit) {
        threadPoolExecutor.execute(RunnableWrapper(task))
    }

    /**
     * Submits a Runnable task for execution and returns a Future representing that task. The Future's get method will return null upon successful completion.
     * @param task the task to submit
     * @return a Future representing pending completion of the task
     */
    fun submit(task: Runnable): Future<*> {
        return threadPoolExecutor.submit(RunnableWrapper(task))
    }

    /**
     * Purges all thread pools.
     */
    fun purge() {
        threadPools.forEach {
            it.purge()
        }
    }

    /**
     * Shutdowns the thread pools waiting for tasks to finish.
     */
    fun shutdown() {
        if (!::scheduledThreadPoolExecutor.isInitialized && !::threadPoolExecutor.isInitialized) {
            return
        }

        val startTime = System.currentTimeMillis()

        LOGGER.info("Shutting down.")
        LOGGER.info("\t... executing {} scheduled tasks.", scheduledThreadPoolExecutor.queue.size)
        LOGGER.info("\t... executing {} tasks.", threadPoolExecutor.queue.size)

        threadPools.forEach { tp ->
            try {
                tp.shutdown()
            } catch (t: Throwable) {
                LOGGER.warn("", t)
            }
        }

        threadPools.forEach { t ->
            try {
                t.awaitTermination(15, TimeUnit.SECONDS)
            } catch (e: InterruptedException) {
                LOGGER.warn("", e)
            }
        }

        if (!scheduledThreadPoolExecutor.isTerminated) {
            scheduledThreadPoolExecutor.executeExistingDelayedTasksAfterShutdownPolicy = false
            scheduledThreadPoolExecutor.continueExistingPeriodicTasksAfterShutdownPolicy = false
            try {
                scheduledThreadPoolExecutor.awaitTermination(5, TimeUnit.SECONDS)
            } catch (t: Throwable) {
                LOGGER.warn("", t)
            }

        }

        LOGGER.info("\t... success: {} in {} ms.", threadPools.allMatch{ it.isTerminated }, System.currentTimeMillis() - startTime)
        LOGGER.info("\t... {} scheduled tasks left.", scheduledThreadPoolExecutor.queue.size)
        LOGGER.info("\t... {} tasks left.", threadPoolExecutor.queue.size)
    }

    private class PoolThreadFactory(private val _prefix: String, private val _priority: Int) : ThreadFactory {
        private val _threadId = AtomicInteger()

        override fun newThread(r: Runnable): Thread {
            val thread = Thread(r, _prefix + _threadId.incrementAndGet())
            thread.priority = _priority
            return thread
        }
    }
}
