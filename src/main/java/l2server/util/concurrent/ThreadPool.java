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
package l2server.util.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * @author _dev_ (savormix)
 * @author NB4L1
 */
public final class ThreadPool
{
	private static final Logger LOGGER = LoggerFactory.getLogger(ThreadPool.class);
	
	private static ScheduledThreadPoolExecutor SCHEDULED_THREAD_POOL_EXECUTOR;
	private static ThreadPoolExecutor THREAD_POOL_EXECUTOR;
	
	public static void initThreadPools(IThreadPoolInitializer initializer) throws Exception
	{
		if ((SCHEDULED_THREAD_POOL_EXECUTOR != null) || (THREAD_POOL_EXECUTOR != null))
		{
			throw new Exception("The thread pool has been already initialized!");
		}
		
		SCHEDULED_THREAD_POOL_EXECUTOR = new ScheduledThreadPoolExecutor(initializer.getScheduledThreadPoolSize(), new PoolThreadFactory("L2JU-SP-", Thread.NORM_PRIORITY));
		THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(initializer.getThreadPoolSize(), initializer.getThreadPoolSize(), 1, TimeUnit.MINUTES, new LinkedBlockingQueue<>(), new PoolThreadFactory("L2JU-IT-", Thread.NORM_PRIORITY));
		
		getThreadPools().forEach(tp ->
		{
			tp.setRejectedExecutionHandler(new RejectedExecutionHandlerImpl());
			tp.prestartAllCoreThreads();
		});
		
		scheduleAtFixedRate(ThreadPool::purge, 1, 1, TimeUnit.MINUTES);
		
		LOGGER.info("Initialized with");
		LOGGER.info("\t... " + SCHEDULED_THREAD_POOL_EXECUTOR.getPoolSize() + "/" + SCHEDULED_THREAD_POOL_EXECUTOR.getPoolSize() + " scheduled thread(s)."); // ScheduledThreadPoolExecutor has a fixed number of threads and maximumPoolSize has no effect
		LOGGER.info("\t... " + THREAD_POOL_EXECUTOR.getPoolSize() + "/" + THREAD_POOL_EXECUTOR.getMaximumPoolSize() + " thread(s).");
	}
	
	/**
	 * Gets the scheduled thread pool executor.
	 * @return the scheduled thread pool executor
	 */
	public static ScheduledThreadPoolExecutor getScheduledThreadPoolExecutor()
	{
		return SCHEDULED_THREAD_POOL_EXECUTOR;
	}
	
	/**
	 * Gets the thread pool executor.
	 * @return the thread pool executor
	 */
	public static ThreadPoolExecutor getThreadPoolExecutor()
	{
		return THREAD_POOL_EXECUTOR;
	}
	
	/**
	 * Gets a stream of all the thread pools.
	 * @return the stream of all the thread pools
	 */
	public static Stream<ThreadPoolExecutor> getThreadPools()
	{
		return Stream.of(SCHEDULED_THREAD_POOL_EXECUTOR, THREAD_POOL_EXECUTOR);
	}
	
	/**
	 * Schedules a task to be executed after the given delay.
	 * @param task the task to execute
	 * @param delay the delay in the given time unit
	 * @param timeUnit the time unit of the delay parameter
	 * @return a ScheduledFuture representing pending completion of the task, and whose get() method will throw an exception upon cancellation
	 */
	public static ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit timeUnit)
	{
		return SCHEDULED_THREAD_POOL_EXECUTOR.schedule(new RunnableWrapper(task), delay, timeUnit);
	}
	
	/**
	 * Schedules a task to be executed after the given delay at fixed rate.
	 * @param task the task to execute
	 * @param delay the delay in the given time unit
	 * @param period the period in the given time unit
	 * @param timeUnit the time unit of the delay parameter
	 * @return a ScheduledFuture representing pending completion of the task, and whose get() method will throw an exception upon cancellation
	 */
	public static ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long delay, long period, TimeUnit timeUnit)
	{
		return SCHEDULED_THREAD_POOL_EXECUTOR.scheduleAtFixedRate(new RunnableWrapper(task), delay, period, timeUnit);
	}
	
	/**
	 * Schedules a task to be executed after the given delay with fixed delay.
	 * @param task the task to execute
	 * @param delay the delay in the given time unit
	 * @param period the period in the given time unit
	 * @param timeUnit the time unit of the delay parameter
	 * @return a ScheduledFuture representing pending completion of the task, and whose get() method will throw an exception upon cancellation
	 */
	public static ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long delay, long period, TimeUnit timeUnit)
	{
		return SCHEDULED_THREAD_POOL_EXECUTOR.scheduleWithFixedDelay(new RunnableWrapper(task), delay, period, timeUnit);
	}
	
	/**
	 * Executes the given task sometime in the future.
	 * @param task the task to execute
	 */
	public static void execute(Runnable task)
	{
		THREAD_POOL_EXECUTOR.execute(new RunnableWrapper(task));
	}
	
	/**
	 * Submits a Runnable task for execution and returns a Future representing that task. The Future's get method will return null upon successful completion.
	 * @param task the task to submit
	 * @return a Future representing pending completion of the task
	 */
	public static Future<?> submit(Runnable task)
	{
		return THREAD_POOL_EXECUTOR.submit(new RunnableWrapper(task));
	}
	
	/**
	 * Purges all thread pools.
	 */
	public static void purge()
	{
		getThreadPools().forEach(ThreadPoolExecutor::purge);
	}
	
	/**
	 * Gets the thread pools stats.
	 * @return the stats
	 */
	public static List<String> getStats()
	{
		final List<String> list = new ArrayList<>(23);
		list.add("");
		list.add("Scheduled pool:");
		list.add("=================================================");
		list.add("\tgetActiveCount: ...... " + SCHEDULED_THREAD_POOL_EXECUTOR.getActiveCount());
		list.add("\tgetCorePoolSize: ..... " + SCHEDULED_THREAD_POOL_EXECUTOR.getCorePoolSize());
		list.add("\tgetPoolSize: ......... " + SCHEDULED_THREAD_POOL_EXECUTOR.getPoolSize());
		list.add("\tgetLargestPoolSize: .. " + SCHEDULED_THREAD_POOL_EXECUTOR.getLargestPoolSize());
		list.add("\tgetMaximumPoolSize: .. " + SCHEDULED_THREAD_POOL_EXECUTOR.getCorePoolSize()); // ScheduledThreadPoolExecutor has a fixed number of threads and maximumPoolSize has no effect
		list.add("\tgetCompletedTaskCount: " + SCHEDULED_THREAD_POOL_EXECUTOR.getCompletedTaskCount());
		list.add("\tgetQueuedTaskCount: .. " + SCHEDULED_THREAD_POOL_EXECUTOR.getQueue().size());
		list.add("\tgetTaskCount: ........ " + SCHEDULED_THREAD_POOL_EXECUTOR.getTaskCount());
		list.add("");
		list.add("Thread pool:");
		list.add("=================================================");
		list.add("\tgetActiveCount: ...... " + THREAD_POOL_EXECUTOR.getActiveCount());
		list.add("\tgetCorePoolSize: ..... " + THREAD_POOL_EXECUTOR.getCorePoolSize());
		list.add("\tgetPoolSize: ......... " + THREAD_POOL_EXECUTOR.getPoolSize());
		list.add("\tgetLargestPoolSize: .. " + THREAD_POOL_EXECUTOR.getLargestPoolSize());
		list.add("\tgetMaximumPoolSize: .. " + THREAD_POOL_EXECUTOR.getMaximumPoolSize());
		list.add("\tgetCompletedTaskCount: " + THREAD_POOL_EXECUTOR.getCompletedTaskCount());
		list.add("\tgetQueuedTaskCount: .. " + THREAD_POOL_EXECUTOR.getQueue().size());
		list.add("\tgetTaskCount: ........ " + THREAD_POOL_EXECUTOR.getTaskCount());
		list.add("");
		return list;
	}
	
	/**
	 * Shutdowns the thread pools waiting for tasks to finish.
	 */
	public static void shutdown()
	{
		if ((SCHEDULED_THREAD_POOL_EXECUTOR == null) && (THREAD_POOL_EXECUTOR == null))
		{
			return;
		}
		
		final long startTime = System.currentTimeMillis();
		
		LOGGER.info("Shutting down.");
		LOGGER.info("\t... executing {} scheduled tasks.", SCHEDULED_THREAD_POOL_EXECUTOR.getQueue().size());
		LOGGER.info("\t... executing {} tasks.", THREAD_POOL_EXECUTOR.getQueue().size());
		
		getThreadPools().forEach(tp ->
		{
			try
			{
				tp.shutdown();
			}
			catch (Throwable t)
			{
				LOGGER.warn("", t);
			}
		});
		
		getThreadPools().forEach(t ->
		{
			try
			{
				t.awaitTermination(15, TimeUnit.SECONDS);
			}
			catch (InterruptedException e)
			{
				LOGGER.warn("", e);
			}
		});
		
		if (!SCHEDULED_THREAD_POOL_EXECUTOR.isTerminated())
		{
			SCHEDULED_THREAD_POOL_EXECUTOR.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
			SCHEDULED_THREAD_POOL_EXECUTOR.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
			try
			{
				SCHEDULED_THREAD_POOL_EXECUTOR.awaitTermination(5, TimeUnit.SECONDS);
			}
			catch (Throwable t)
			{
				LOGGER.warn("", t);
			}
		}
		
		LOGGER.info("\t... success: {} in {} ms.", getThreadPools().allMatch(ThreadPoolExecutor::isTerminated), System.currentTimeMillis() - startTime);
		LOGGER.info("\t... {} scheduled tasks left.", SCHEDULED_THREAD_POOL_EXECUTOR.getQueue().size());
		LOGGER.info("\t... {} tasks left.", THREAD_POOL_EXECUTOR.getQueue().size());
	}
	
	private static final class PoolThreadFactory implements ThreadFactory
	{
		private final String _prefix;
		private final int _priority;
		private final AtomicInteger _threadId = new AtomicInteger();
		
		public PoolThreadFactory(String prefix, int priority)
		{
			_prefix = prefix;
			_priority = priority;
		}
		
		@Override
		public Thread newThread(Runnable r)
		{
			final Thread thread = new Thread(r, _prefix + _threadId.incrementAndGet());
			thread.setPriority(_priority);
			return thread;
		}
	}
}
