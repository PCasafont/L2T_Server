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
				this.r.run();
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
		this.effectsScheduledThreadPool = new ScheduledThreadPoolExecutor(Config.THREAD_P_EFFECTS,
				new PriorityThreadFactory("EffectsSTPool", Thread.NORM_PRIORITY));
		this.generalScheduledThreadPool = new ScheduledThreadPoolExecutor(Config.THREAD_P_GENERAL,
				new PriorityThreadFactory("GeneralSTPool", Thread.NORM_PRIORITY));
		this.ioPacketsThreadPool =
				new ThreadPoolExecutor(Config.IO_PACKET_THREAD_CORE_SIZE, Integer.MAX_VALUE, 5L, TimeUnit.SECONDS,
						new LinkedBlockingQueue<>(),
						new PriorityThreadFactory("I/O Packet Pool", Thread.NORM_PRIORITY + 1));
		this.generalPacketsThreadPool = new ThreadPoolExecutor(Config.GENERAL_PACKET_THREAD_CORE_SIZE,
				Config.GENERAL_PACKET_THREAD_CORE_SIZE + 2, 15L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(),
				new PriorityThreadFactory("Normal Packet Pool", Thread.NORM_PRIORITY + 1));
		this.generalThreadPool =
				new ThreadPoolExecutor(Config.GENERAL_THREAD_CORE_SIZE, Config.GENERAL_THREAD_CORE_SIZE + 2, 5L,
						TimeUnit.SECONDS, new LinkedBlockingQueue<>(),
						new PriorityThreadFactory("General Pool", Thread.NORM_PRIORITY));
		this.aiScheduledThreadPool = new ScheduledThreadPoolExecutor(Config.AI_MAX_THREAD,
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
			return this.effectsScheduledThreadPool.schedule(new RunnableWrapper(r), delay, TimeUnit.MILLISECONDS);
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
		return this.effectsScheduledThreadPool.remove(r);
	}

	public ScheduledFuture<?> scheduleGeneral(Runnable r, long delay)
	{
		try
		{
			delay = ThreadPoolManager.validateDelay(delay);
			return this.generalScheduledThreadPool.schedule(new RunnableWrapper(r), delay, TimeUnit.MILLISECONDS);
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
		return this.generalScheduledThreadPool.remove(r);
	}

	public ScheduledFuture<?> scheduleAi(Runnable r, long delay)
	{
		try
		{
			delay = ThreadPoolManager.validateDelay(delay);
			return this.aiScheduledThreadPool.schedule(new RunnableWrapper(r), delay, TimeUnit.MILLISECONDS);
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
		this.generalPacketsThreadPool.execute(pkt);
	}

	public void executeCommunityPacket(Runnable r)
	{
		this.generalPacketsThreadPool.execute(r);
	}

	public void executeIOPacket(Runnable pkt)
	{
		this.ioPacketsThreadPool.execute(pkt);
	}

	public void executeTask(Runnable r)
	{
		this.generalThreadPool.execute(r);
	}

	public void executeAi(Runnable r)
	{
		this.aiScheduledThreadPool.execute(new RunnableWrapper(r));
	}

	public String[] getStats()
	{
		return new String[]{
				"STP:",
				" + Effects:",
				" |- ActiveThreads:   " + this.effectsScheduledThreadPool.getActiveCount(),
				" |- getCorePoolSize: " + this.effectsScheduledThreadPool.getCorePoolSize(),
				" |- PoolSize:		" + this.effectsScheduledThreadPool.getPoolSize(),
				" |- MaximumPoolSize: " + this.effectsScheduledThreadPool.getMaximumPoolSize(),
				" |- CompletedTasks:  " + this.effectsScheduledThreadPool.getCompletedTaskCount(),
				" |- ScheduledTasks:  " + (this.effectsScheduledThreadPool.getTaskCount() -
						this.effectsScheduledThreadPool.getCompletedTaskCount()),
				" | -------",
				" + General:",
				" |- ActiveThreads:   " + this.generalScheduledThreadPool.getActiveCount(),
				" |- getCorePoolSize: " + this.generalScheduledThreadPool.getCorePoolSize(),
				" |- PoolSize:		" + this.generalScheduledThreadPool.getPoolSize(),
				" |- MaximumPoolSize: " + this.generalScheduledThreadPool.getMaximumPoolSize(),
				" |- CompletedTasks:  " + this.generalScheduledThreadPool.getCompletedTaskCount(),
				" |- ScheduledTasks:  " + (this.generalScheduledThreadPool.getTaskCount() -
						this.generalScheduledThreadPool.getCompletedTaskCount()),
				" | -------",
				" + AI:",
				" |- ActiveThreads:   " + this.aiScheduledThreadPool.getActiveCount(),
				" |- getCorePoolSize: " + this.aiScheduledThreadPool.getCorePoolSize(),
				" |- PoolSize:		" + this.aiScheduledThreadPool.getPoolSize(),
				" |- MaximumPoolSize: " + this.aiScheduledThreadPool.getMaximumPoolSize(),
				" |- CompletedTasks:  " + this.aiScheduledThreadPool.getCompletedTaskCount(),
				" |- ScheduledTasks:  " +
						(this.aiScheduledThreadPool.getTaskCount() - this.aiScheduledThreadPool.getCompletedTaskCount()),
				"TP:",
				" + Packets:",
				" |- ActiveThreads:   " + this.generalPacketsThreadPool.getActiveCount(),
				" |- getCorePoolSize: " + this.generalPacketsThreadPool.getCorePoolSize(),
				" |- MaximumPoolSize: " + this.generalPacketsThreadPool.getMaximumPoolSize(),
				" |- LargestPoolSize: " + this.generalPacketsThreadPool.getLargestPoolSize(),
				" |- PoolSize:		" + this.generalPacketsThreadPool.getPoolSize(),
				" |- CompletedTasks:  " + this.generalPacketsThreadPool.getCompletedTaskCount(),
				" |- QueuedTasks:	 " + this.generalPacketsThreadPool.getQueue().size(),
				" | -------",
				" + I/O Packets:",
				" |- ActiveThreads:   " + this.ioPacketsThreadPool.getActiveCount(),
				" |- getCorePoolSize: " + this.ioPacketsThreadPool.getCorePoolSize(),
				" |- MaximumPoolSize: " + this.ioPacketsThreadPool.getMaximumPoolSize(),
				" |- LargestPoolSize: " + this.ioPacketsThreadPool.getLargestPoolSize(),
				" |- PoolSize:		" + this.ioPacketsThreadPool.getPoolSize(),
				" |- CompletedTasks:  " + this.ioPacketsThreadPool.getCompletedTaskCount(),
				" |- QueuedTasks:	 " + this.ioPacketsThreadPool.getQueue().size(),
				" | -------",
				" + General Tasks:",
				" |- ActiveThreads:   " + this.generalThreadPool.getActiveCount(),
				" |- getCorePoolSize: " + this.generalThreadPool.getCorePoolSize(),
				" |- MaximumPoolSize: " + this.generalThreadPool.getMaximumPoolSize(),
				" |- LargestPoolSize: " + this.generalThreadPool.getLargestPoolSize(),
				" |- PoolSize:		" + this.generalThreadPool.getPoolSize(),
				" |- CompletedTasks:  " + this.generalThreadPool.getCompletedTaskCount(),
				" |- QueuedTasks:	 " + this.generalThreadPool.getQueue().size(),
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
			this.group = new ThreadGroup(this.name);
		}

		@Override
		public Thread newThread(Runnable r)
		{
			Thread t = new Thread(this.group, r);
			t.setName(this.name + "-" + this.threadNumber.getAndIncrement());
			t.setPriority(this.prio);
			return t;
		}

		public ThreadGroup getGroup()
		{
			return this.group;
		}
	}

	public void shutdown()
	{
		this.shutdown = true;
		try
		{
			this.generalPacketsThreadPool.awaitTermination(1, TimeUnit.SECONDS);
			this.ioPacketsThreadPool.awaitTermination(1, TimeUnit.SECONDS);
			this.effectsScheduledThreadPool.awaitTermination(1, TimeUnit.SECONDS);
			this.generalScheduledThreadPool.awaitTermination(1, TimeUnit.SECONDS);
			this.aiScheduledThreadPool.awaitTermination(1, TimeUnit.SECONDS);
			this.generalThreadPool.awaitTermination(1, TimeUnit.SECONDS);
			this.generalPacketsThreadPool.shutdown();
			this.ioPacketsThreadPool.shutdown();
			this.effectsScheduledThreadPool.shutdown();
			this.generalScheduledThreadPool.shutdown();
			this.aiScheduledThreadPool.shutdown();
			this.generalThreadPool.shutdown();
			Log.info("All ThreadPools are now stopped");
		}
		catch (InterruptedException e)
		{
			Log.log(Level.WARNING, "", e);
		}
	}

	public boolean isShutdown()
	{
		return this.shutdown;
	}

	public void purge()
	{
		this.effectsScheduledThreadPool.purge();
		this.generalScheduledThreadPool.purge();
		this.aiScheduledThreadPool.purge();
		this.ioPacketsThreadPool.purge();
		this.generalPacketsThreadPool.purge();
		this.generalThreadPool.purge();
	}

	public String getPacketStats()
	{
		final StringBuilder sb = new StringBuilder(1000);
		ThreadFactory tf = this.generalPacketsThreadPool.getThreadFactory();
		if (tf instanceof PriorityThreadFactory)
		{
			PriorityThreadFactory ptf = (PriorityThreadFactory) tf;
			int count = ptf.getGroup().activeCount();
			Thread[] threads = new Thread[count + 2];
			ptf.getGroup().enumerate(threads);
			StringUtil.append(sb, "General Packet Thread Pool:\r\n" + "Tasks in the queue: ",
					String.valueOf(this.generalPacketsThreadPool.getQueue().size()),
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
		ThreadFactory tf = this.ioPacketsThreadPool.getThreadFactory();

		if (tf instanceof PriorityThreadFactory)
		{
			PriorityThreadFactory ptf = (PriorityThreadFactory) tf;
			int count = ptf.getGroup().activeCount();
			Thread[] threads = new Thread[count + 2];
			ptf.getGroup().enumerate(threads);
			StringUtil.append(sb, "I/O Packet Thread Pool:\r\n" + "Tasks in the queue: ",
					String.valueOf(this.ioPacketsThreadPool.getQueue().size()),
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
		ThreadFactory tf = this.generalThreadPool.getThreadFactory();

		if (tf instanceof PriorityThreadFactory)
		{
			PriorityThreadFactory ptf = (PriorityThreadFactory) tf;
			int count = ptf.getGroup().activeCount();
			Thread[] threads = new Thread[count + 2];
			ptf.getGroup().enumerate(threads);
			StringUtil.append(sb, "General Thread Pool:\r\n" + "Tasks in the queue: ",
					String.valueOf(this.generalThreadPool.getQueue().size()),
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
