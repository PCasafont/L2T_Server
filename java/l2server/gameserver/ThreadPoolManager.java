/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package l2server.gameserver;

import l2server.Config;
import l2server.log.Log;
import l2server.util.StringUtil;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

/**
 * <p>This class is made to handle all the ThreadPools used in L2j.</p>
 * <p>Scheduled Tasks can either be sent to a {@link #generalScheduledThreadPool "general"} or {@link #effectsScheduledThreadPool "effects"} {@link ScheduledThreadPoolExecutor ScheduledThreadPool}:
 * The "effects" one is used for every effects (skills, hp/mp regen ...) while the "general" one is used for
 * everything else that needs to be scheduled.<br>
 * There also is an {@link #aiScheduledThreadPool "ai"} {@link ScheduledThreadPoolExecutor ScheduledThreadPool} used for AI Tasks.</p>
 * <p>Tasks can be sent to {@link ScheduledThreadPoolExecutor ScheduledThreadPool} either with:
 * <ul>
 * <li>{@link #scheduleEffect(Runnable, long)} : for effects Tasks that needs to be executed only once.</li>
 * <li>{@link #scheduleGeneral(Runnable, long)} : for scheduled Tasks that needs to be executed once.</li>
 * <li>{@link #scheduleAi(Runnable, long)} : for AI Tasks that needs to be executed once</li>
 * </ul>
 * or
 * <ul>
 * <li>{@link #scheduleEffectAtFixedRate(Runnable, long, long)(Runnable, long)} : for effects Tasks that needs to be executed periodicaly.</li>
 * <li>{@link #scheduleGeneralAtFixedRate(Runnable, long, long)(Runnable, long)} : for scheduled Tasks that needs to be executed periodicaly.</li>
 * <li>{@link #scheduleAiAtFixedRate(Runnable, long, long)(Runnable, long)} : for AI Tasks that needs to be executed periodicaly</li>
 * </ul></p>
 * <p>
 * <p>For all Tasks that should be executed with no delay asynchronously in a ThreadPool there also are usual {@link ThreadPoolExecutor ThreadPools}
 * that can grow/shrink according to their load.:
 * <ul>
 * <li>{@link #generalPacketsThreadPool GeneralPackets} where most packets handler are executed.</li>
 * <li>{@link #ioPacketsThreadPool I/O Packets} where all the i/o packets are executed.</li>
 * <li>There will be an AI ThreadPool where AI events should be executed</li>
 * <li>A general ThreadPool where everything else that needs to run asynchronously with no delay should be executed ({@link l2server.gameserver.model.actor.knownlist KnownList} updates, SQL updates/inserts...)?</li>
 * </ul>
 * </p>
 *
 * @author -Wooden-
 */
public class ThreadPoolManager
{

	private static final class RunnableWrapper implements Runnable
	{
		private final Runnable r;

		public RunnableWrapper(final Runnable r)
		{
			this.r = r;
		}

		@Override
		public final void run()
		{
			try
			{
				r.run();
			}
			catch (final Throwable e)
			{
				final Thread t = Thread.currentThread();
				final UncaughtExceptionHandler h = t.getUncaughtExceptionHandler();
				if (h != null)
				{
					h.uncaughtException(t, e);
				}
			}
		}
	}

	private ScheduledThreadPoolExecutor effectsScheduledThreadPool;
	private ScheduledThreadPoolExecutor generalScheduledThreadPool;
	private ScheduledThreadPoolExecutor aiScheduledThreadPool;
	private ThreadPoolExecutor generalPacketsThreadPool;
	private ThreadPoolExecutor ioPacketsThreadPool;
	private ThreadPoolExecutor generalThreadPool;

	/**
	 * temp workaround for VM issue
	 */
	private static final long MAX_DELAY = Long.MAX_VALUE / 1000000 / 2;

	private boolean shutdown;

	public static ThreadPoolManager getInstance()
	{
		return SingletonHolder.instance;
	}

	private ThreadPoolManager()
	{
		effectsScheduledThreadPool = new ScheduledThreadPoolExecutor(Config.THREAD_P_EFFECTS,
				new PriorityThreadFactory("EffectsSTPool", Thread.NORM_PRIORITY));
		generalScheduledThreadPool = new ScheduledThreadPoolExecutor(Config.THREAD_P_GENERAL,
				new PriorityThreadFactory("GeneralSTPool", Thread.NORM_PRIORITY));
		ioPacketsThreadPool =
				new ThreadPoolExecutor(Config.IO_PACKET_THREAD_CORE_SIZE, Integer.MAX_VALUE, 5L, TimeUnit.SECONDS,
						new LinkedBlockingQueue<>(),
						new PriorityThreadFactory("I/O Packet Pool", Thread.NORM_PRIORITY + 1));
		generalPacketsThreadPool = new ThreadPoolExecutor(Config.GENERAL_PACKET_THREAD_CORE_SIZE,
				Config.GENERAL_PACKET_THREAD_CORE_SIZE + 2, 15L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(),
				new PriorityThreadFactory("Normal Packet Pool", Thread.NORM_PRIORITY + 1));
		generalThreadPool =
				new ThreadPoolExecutor(Config.GENERAL_THREAD_CORE_SIZE, Config.GENERAL_THREAD_CORE_SIZE + 2, 5L,
						TimeUnit.SECONDS, new LinkedBlockingQueue<>(),
						new PriorityThreadFactory("General Pool", Thread.NORM_PRIORITY));
		aiScheduledThreadPool = new ScheduledThreadPoolExecutor(Config.AI_MAX_THREAD,
				new PriorityThreadFactory("AISTPool", Thread.NORM_PRIORITY));

		scheduleGeneralAtFixedRate(new PurgeTask(), 10 * 60 * 1000L, 5 * 60 * 1000L);
	}

	public static long validateDelay(long delay)
	{
		if (delay < 1)
		{
			delay = 1;
		}
		else if (delay > MAX_DELAY)
		{
			delay = MAX_DELAY;
		}
		return delay;
	}

	public ScheduledFuture<?> scheduleEffect(Runnable r, long delay)
	{
		try
		{
			delay = ThreadPoolManager.validateDelay(delay);
			return effectsScheduledThreadPool.schedule(new RunnableWrapper(r), delay, TimeUnit.MILLISECONDS);
		}
		catch (RejectedExecutionException e)
		{
			return null;
		}
	}

	public ScheduledFuture<?> scheduleEffectAtFixedRate(Runnable r, long initial, long delay)
	{
		try
		{
			delay = ThreadPoolManager.validateDelay(delay);
			initial = ThreadPoolManager.validateDelay(initial);
			return effectsScheduledThreadPool
					.scheduleAtFixedRate(new RunnableWrapper(r), initial, delay, TimeUnit.MILLISECONDS);
		}
		catch (RejectedExecutionException e)
		{
			return null; /* shutdown, ignore */
		}
	}

	@Deprecated
	public boolean removeEffect(RunnableScheduledFuture<?> r)
	{
		return effectsScheduledThreadPool.remove(r);
	}

	public ScheduledFuture<?> scheduleGeneral(Runnable r, long delay)
	{
		try
		{
			delay = ThreadPoolManager.validateDelay(delay);
			return generalScheduledThreadPool.schedule(new RunnableWrapper(r), delay, TimeUnit.MILLISECONDS);
		}
		catch (RejectedExecutionException e)
		{
			return null; /* shutdown, ignore */
		}
	}

	public ScheduledFuture<?> scheduleGeneralAtFixedRate(Runnable r, long initial, long delay)
	{
		try
		{
			delay = ThreadPoolManager.validateDelay(delay);
			initial = ThreadPoolManager.validateDelay(initial);
			return generalScheduledThreadPool
					.scheduleAtFixedRate(new RunnableWrapper(r), initial, delay, TimeUnit.MILLISECONDS);
		}
		catch (RejectedExecutionException e)
		{
			return null; /* shutdown, ignore */
		}
	}

	@Deprecated
	public boolean removeGeneral(RunnableScheduledFuture<?> r)
	{
		return generalScheduledThreadPool.remove(r);
	}

	public ScheduledFuture<?> scheduleAi(Runnable r, long delay)
	{
		try
		{
			delay = ThreadPoolManager.validateDelay(delay);
			return aiScheduledThreadPool.schedule(new RunnableWrapper(r), delay, TimeUnit.MILLISECONDS);
		}
		catch (RejectedExecutionException e)
		{
			return null; /* shutdown, ignore */
		}
	}

	public ScheduledFuture<?> scheduleAiAtFixedRate(Runnable r, long initial, long delay)
	{
		try
		{
			delay = ThreadPoolManager.validateDelay(delay);
			initial = ThreadPoolManager.validateDelay(initial);
			return aiScheduledThreadPool
					.scheduleAtFixedRate(new RunnableWrapper(r), initial, delay, TimeUnit.MILLISECONDS);
		}
		catch (RejectedExecutionException e)
		{
			return null; /* shutdown, ignore */
		}
	}

	public void executePacket(Runnable pkt)
	{
		generalPacketsThreadPool.execute(pkt);
	}

	public void executeCommunityPacket(Runnable r)
	{
		generalPacketsThreadPool.execute(r);
	}

	public void executeIOPacket(Runnable pkt)
	{
		ioPacketsThreadPool.execute(pkt);
	}

	public void executeTask(Runnable r)
	{
		generalThreadPool.execute(r);
	}

	public void executeAi(Runnable r)
	{
		aiScheduledThreadPool.execute(new RunnableWrapper(r));
	}

	public String[] getStats()
	{
		return new String[]{
				"STP:",
				" + Effects:",
				" |- ActiveThreads:   " + effectsScheduledThreadPool.getActiveCount(),
				" |- getCorePoolSize: " + effectsScheduledThreadPool.getCorePoolSize(),
				" |- PoolSize:		" + effectsScheduledThreadPool.getPoolSize(),
				" |- MaximumPoolSize: " + effectsScheduledThreadPool.getMaximumPoolSize(),
				" |- CompletedTasks:  " + effectsScheduledThreadPool.getCompletedTaskCount(),
				" |- ScheduledTasks:  " + (effectsScheduledThreadPool.getTaskCount() - effectsScheduledThreadPool.getCompletedTaskCount()),
				" | -------",
				" + General:",
				" |- ActiveThreads:   " + generalScheduledThreadPool.getActiveCount(),
				" |- getCorePoolSize: " + generalScheduledThreadPool.getCorePoolSize(),
				" |- PoolSize:		" + generalScheduledThreadPool.getPoolSize(),
				" |- MaximumPoolSize: " + generalScheduledThreadPool.getMaximumPoolSize(),
				" |- CompletedTasks:  " + generalScheduledThreadPool.getCompletedTaskCount(),
				" |- ScheduledTasks:  " + (generalScheduledThreadPool.getTaskCount() - generalScheduledThreadPool.getCompletedTaskCount()),
				" | -------",
				" + AI:",
				" |- ActiveThreads:   " + aiScheduledThreadPool.getActiveCount(),
				" |- getCorePoolSize: " + aiScheduledThreadPool.getCorePoolSize(),
				" |- PoolSize:		" + aiScheduledThreadPool.getPoolSize(),
				" |- MaximumPoolSize: " + aiScheduledThreadPool.getMaximumPoolSize(),
				" |- CompletedTasks:  " + aiScheduledThreadPool.getCompletedTaskCount(),
				" |- ScheduledTasks:  " +
						(aiScheduledThreadPool.getTaskCount() - aiScheduledThreadPool.getCompletedTaskCount()),
				"TP:",
				" + Packets:",
				" |- ActiveThreads:   " + generalPacketsThreadPool.getActiveCount(),
				" |- getCorePoolSize: " + generalPacketsThreadPool.getCorePoolSize(),
				" |- MaximumPoolSize: " + generalPacketsThreadPool.getMaximumPoolSize(),
				" |- LargestPoolSize: " + generalPacketsThreadPool.getLargestPoolSize(),
				" |- PoolSize:		" + generalPacketsThreadPool.getPoolSize(),
				" |- CompletedTasks:  " + generalPacketsThreadPool.getCompletedTaskCount(),
				" |- QueuedTasks:	 " + generalPacketsThreadPool.getQueue().size(),
				" | -------",
				" + I/O Packets:",
				" |- ActiveThreads:   " + ioPacketsThreadPool.getActiveCount(),
				" |- getCorePoolSize: " + ioPacketsThreadPool.getCorePoolSize(),
				" |- MaximumPoolSize: " + ioPacketsThreadPool.getMaximumPoolSize(),
				" |- LargestPoolSize: " + ioPacketsThreadPool.getLargestPoolSize(),
				" |- PoolSize:		" + ioPacketsThreadPool.getPoolSize(),
				" |- CompletedTasks:  " + ioPacketsThreadPool.getCompletedTaskCount(),
				" |- QueuedTasks:	 " + ioPacketsThreadPool.getQueue().size(),
				" | -------",
				" + General Tasks:",
				" |- ActiveThreads:   " + generalThreadPool.getActiveCount(),
				" |- getCorePoolSize: " + generalThreadPool.getCorePoolSize(),
				" |- MaximumPoolSize: " + generalThreadPool.getMaximumPoolSize(),
				" |- LargestPoolSize: " + generalThreadPool.getLargestPoolSize(),
				" |- PoolSize:		" + generalThreadPool.getPoolSize(),
				" |- CompletedTasks:  " + generalThreadPool.getCompletedTaskCount(),
				" |- QueuedTasks:	 " + generalThreadPool.getQueue().size(),
				" | -------"
		};
	}

	private static class PriorityThreadFactory implements ThreadFactory
	{
		private int prio;
		private String name;
		private AtomicInteger threadNumber = new AtomicInteger(1);
		private ThreadGroup group;

		public PriorityThreadFactory(String name, int prio)
		{
			this.prio = prio;
			this.name = name;
			group = new ThreadGroup(this.name);
		}

		@Override
		public Thread newThread(Runnable r)
		{
			Thread t = new Thread(group, r);
			t.setName(name + "-" + threadNumber.getAndIncrement());
			t.setPriority(prio);
			return t;
		}

		public ThreadGroup getGroup()
		{
			return group;
		}
	}

	public void shutdown()
	{
		shutdown = true;
		try
		{
			generalPacketsThreadPool.awaitTermination(1, TimeUnit.SECONDS);
			ioPacketsThreadPool.awaitTermination(1, TimeUnit.SECONDS);
			effectsScheduledThreadPool.awaitTermination(1, TimeUnit.SECONDS);
			generalScheduledThreadPool.awaitTermination(1, TimeUnit.SECONDS);
			aiScheduledThreadPool.awaitTermination(1, TimeUnit.SECONDS);
			generalThreadPool.awaitTermination(1, TimeUnit.SECONDS);
			generalPacketsThreadPool.shutdown();
			ioPacketsThreadPool.shutdown();
			effectsScheduledThreadPool.shutdown();
			generalScheduledThreadPool.shutdown();
			aiScheduledThreadPool.shutdown();
			generalThreadPool.shutdown();
			Log.info("All ThreadPools are now stopped");
		}
		catch (InterruptedException e)
		{
			Log.log(Level.WARNING, "", e);
		}
	}

	public boolean isShutdown()
	{
		return shutdown;
	}

	public void purge()
	{
		effectsScheduledThreadPool.purge();
		generalScheduledThreadPool.purge();
		aiScheduledThreadPool.purge();
		ioPacketsThreadPool.purge();
		generalPacketsThreadPool.purge();
		generalThreadPool.purge();
	}

	public String getPacketStats()
	{
		final StringBuilder sb = new StringBuilder(1000);
		ThreadFactory tf = generalPacketsThreadPool.getThreadFactory();
		if (tf instanceof PriorityThreadFactory)
		{
			PriorityThreadFactory ptf = (PriorityThreadFactory) tf;
			int count = ptf.getGroup().activeCount();
			Thread[] threads = new Thread[count + 2];
			ptf.getGroup().enumerate(threads);
			StringUtil.append(sb, "General Packet Thread Pool:\r\n" + "Tasks in the queue: ",
					String.valueOf(generalPacketsThreadPool.getQueue().size()),
					"\r\n" + "Showing threads stack trace:\r\n" + "There should be ", String.valueOf(count),
					" Threads\r\n");
			for (Thread t : threads)
			{
				if (t == null)
				{
					continue;
				}

				StringUtil.append(sb, t.getName(), "\r\n");
				for (StackTraceElement ste : t.getStackTrace())
				{
					StringUtil.append(sb, ste.toString(), "\r\n");
				}
			}
		}

		sb.append("Packet Tp stack traces printed.\r\n");

		return sb.toString();
	}

	public String getIOPacketStats()
	{
		final StringBuilder sb = new StringBuilder(1000);
		ThreadFactory tf = ioPacketsThreadPool.getThreadFactory();

		if (tf instanceof PriorityThreadFactory)
		{
			PriorityThreadFactory ptf = (PriorityThreadFactory) tf;
			int count = ptf.getGroup().activeCount();
			Thread[] threads = new Thread[count + 2];
			ptf.getGroup().enumerate(threads);
			StringUtil.append(sb, "I/O Packet Thread Pool:\r\n" + "Tasks in the queue: ",
					String.valueOf(ioPacketsThreadPool.getQueue().size()),
					"\r\n" + "Showing threads stack trace:\r\n" + "There should be ", String.valueOf(count),
					" Threads\r\n");

			for (Thread t : threads)
			{
				if (t == null)
				{
					continue;
				}

				StringUtil.append(sb, t.getName(), "\r\n");

				for (StackTraceElement ste : t.getStackTrace())
				{
					StringUtil.append(sb, ste.toString(), "\r\n");
				}
			}
		}

		sb.append("Packet Tp stack traces printed.\r\n");

		return sb.toString();
	}

	public String getGeneralStats()
	{
		final StringBuilder sb = new StringBuilder(1000);
		ThreadFactory tf = generalThreadPool.getThreadFactory();

		if (tf instanceof PriorityThreadFactory)
		{
			PriorityThreadFactory ptf = (PriorityThreadFactory) tf;
			int count = ptf.getGroup().activeCount();
			Thread[] threads = new Thread[count + 2];
			ptf.getGroup().enumerate(threads);
			StringUtil.append(sb, "General Thread Pool:\r\n" + "Tasks in the queue: ",
					String.valueOf(generalThreadPool.getQueue().size()),
					"\r\n" + "Showing threads stack trace:\r\n" + "There should be ", String.valueOf(count),
					" Threads\r\n");

			for (Thread t : threads)
			{
				if (t == null)
				{
					continue;
				}

				StringUtil.append(sb, t.getName(), "\r\n");

				for (StackTraceElement ste : t.getStackTrace())
				{
					StringUtil.append(sb, ste.toString(), "\r\n");
				}
			}
		}

		sb.append("Packet Tp stack traces printed.\r\n");

		return sb.toString();
	}

	private class PurgeTask implements Runnable
	{
		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run()
		{
			effectsScheduledThreadPool.purge();
			generalScheduledThreadPool.purge();
			aiScheduledThreadPool.purge();
		}
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final ThreadPoolManager instance = new ThreadPoolManager();
	}
}
