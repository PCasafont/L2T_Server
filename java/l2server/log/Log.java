package l2server.log;

import l2server.gameserver.gui.ConsoleTab;
import l2server.gameserver.gui.ConsoleTab.ConsoleFilter;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Log
{
	private static final Logger _log = Logger.getLogger("GameServer");

	public static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";

	public static boolean isLoggable(Level level)
	{
		return _log.isLoggable(level);
	}

	public static void log(Level level, String msg)
	{
		_log.log(level, msg);

		ConsoleTab.appendMessage(ConsoleFilter.Console, msg);
	}

	public static void log(Level level, String msg, int val)
	{
		_log.log(level, msg, val);

		ConsoleTab.appendMessage(ConsoleFilter.Console, msg);
	}

	public static void log(Level level, String msg, Throwable t)
	{
		_log.log(level, msg, t);

		ConsoleTab.appendMessage(ConsoleFilter.Console, msg);
	}

	public static void severe(String msg)
	{
		_log.severe(msg);

		ConsoleTab.appendMessage(ConsoleFilter.Errors, msg);
	}

	public static void warning(String msg)
	{
		_log.warning(msg);

		ConsoleTab.appendMessage(ConsoleFilter.Warnings, msg);
	}

	//static long _t = 0;
	public static void info(String msg)
	{
		_log.info(msg);
		/*long t = System.currentTimeMillis();
        if (_t > 0 && t - _t > 10)
			System.out.println("Time spent before last log: " + (t - _t) + "ms");
		_t = t;*/

		ConsoleTab.appendMessage(ConsoleFilter.Info, msg);
	}

	public static void config(String msg)
	{
		_log.config(msg);
	}

	public static void fine(String msg)
	{
		_log.fine(msg);
	}

	public static void finer(String msg)
	{
		_log.finer(msg);
	}

	public static void finest(String msg)
	{
		_log.finest(msg);
	}
}
