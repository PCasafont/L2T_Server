package l2server.log;

import l2server.gameserver.gui.ConsoleTab;
import l2server.gameserver.gui.ConsoleTab.ConsoleFilter;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Log
{
	private static final Logger log = Logger.getLogger("GameServer");

	public static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";

	public static boolean isLoggable(Level level)
	{
		return log.isLoggable(level);
	}

	public static void log(Level level, String msg)
	{
		log.log(level, msg);

		ConsoleTab.appendMessage(ConsoleFilter.Console, msg);
	}

	public static void log(Level level, String msg, int val)
	{
		log.log(level, msg, val);

		ConsoleTab.appendMessage(ConsoleFilter.Console, msg);
	}

	public static void log(Level level, String msg, Throwable t)
	{
		log.log(level, msg, t);

		ConsoleTab.appendMessage(ConsoleFilter.Console, msg);
	}

	public static void severe(String msg)
	{
		log.severe(msg);

		ConsoleTab.appendMessage(ConsoleFilter.Errors, msg);
	}

	public static void warning(String msg)
	{
		log.warning(msg);

		ConsoleTab.appendMessage(ConsoleFilter.Warnings, msg);
	}

	//static long this.t = 0;
	public static void info(String msg)
	{
		log.info(msg);
		/*long t = System.currentTimeMillis();
        if (this.t > 0 && t - this.t > 10)
			System.out.println("Time spent before last log: " + (t - this.t) + "ms");
		this.t = t;*/

		ConsoleTab.appendMessage(ConsoleFilter.Info, msg);
	}

	public static void config(String msg)
	{
		log.config(msg);
	}

	public static void fine(String msg)
	{
		log.fine(msg);
	}

	public static void finer(String msg)
	{
		log.finer(msg);
	}

	public static void finest(String msg)
	{
		log.finest(msg);
	}
}
