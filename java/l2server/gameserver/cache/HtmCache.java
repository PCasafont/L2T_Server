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

package l2server.gameserver.cache;

import l2server.Config;
import l2server.gameserver.Reloadable;
import l2server.gameserver.ReloadableManager;
import l2server.gameserver.util.Util;
import l2server.log.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * @author Layane
 */
public class HtmCache implements Reloadable
{
	private final Map<Integer, String> _cache;

	private int _loadedFiles;
	private long _bytesBuffLen;

	public static HtmCache getInstance()
	{
		return SingletonHolder._instance;
	}

	private HtmCache()
	{
		if (Config.LAZY_CACHE)
		{
			_cache = new ConcurrentHashMap<>();
		}
		else
		{
			_cache = new HashMap<>();
		}
		reload();

		ReloadableManager.getInstance().register("htm", this);
	}

	@Override
	public boolean reload()
	{
		reload(Config.DATAPACK_ROOT);
		return true;
	}

	@Override
	public String getReloadMessage(boolean success)
	{
		return "Cache[HTML]: " + HtmCache.getInstance().getMemoryUsage() + " megabytes on " +
				HtmCache.getInstance().getLoadedFiles() + " files loaded";
	}

	public void reload(File f)
	{
		if (!Config.LAZY_CACHE)
		{
			Log.info("Html cache start...");
			parseDir(f);
			Log.info("Cache[HTML]: " + String.format("%.3f", getMemoryUsage()) + " megabytes on " + getLoadedFiles() +
					" files loaded");
		}
		else
		{
			_cache.clear();
			_loadedFiles = 0;
			_bytesBuffLen = 0;
			Log.info("Cache[HTML]: Running lazy cache");
		}
	}

	public void reloadPath(File f)
	{
		parseDir(f);
		Log.info("Cache[HTML]: Reloaded specified path.");
	}

	public double getMemoryUsage()
	{
		return (float) _bytesBuffLen / 1048576;
	}

	public int getLoadedFiles()
	{
		return _loadedFiles;
	}

	private static class HtmFilter implements FileFilter
	{
		@Override
		public boolean accept(File file)
		{
			if (!file.isDirectory())
			{
				return file.getName().endsWith(".htm") || file.getName().endsWith(".html");
			}
			return true;
		}
	}

	private void parseDir(File dir)
	{
		FileFilter filter = new HtmFilter();
		File[] files = dir.listFiles(filter);

		for (File file : files)
		{
			if (!file.isDirectory())
			{
				loadFile(file);
			}
			else
			{
				parseDir(file);
			}
		}
	}

	public String loadFile(File file)
	{
		final String relpath = Util.getRelativePath(Config.DATAPACK_ROOT, file);
		final int hashcode = relpath.hashCode();

		final HtmFilter filter = new HtmFilter();

		if (file.exists() && filter.accept(file) && !file.isDirectory())
		{
			String content;
			FileInputStream fis = null;

			try
			{
				fis = new FileInputStream(file);
				BufferedInputStream bis = new BufferedInputStream(fis);
				int bytes = bis.available();
				byte[] raw = new byte[bytes];

				bis.read(raw);
				content = new String(raw, "ISO-8859-1");
				content = content.replaceAll("\r\n", "\n");

				String oldContent = _cache.get(hashcode);

				if (oldContent == null)
				{
					_bytesBuffLen += bytes;
					_loadedFiles++;
				}
				else
				{
					_bytesBuffLen = _bytesBuffLen - oldContent.length() + bytes;
				}

				_cache.put(hashcode, content);

				return content;
			}
			catch (Exception e)
			{
				Log.log(Level.WARNING, "Problem with htm file " + e.getMessage(), e);
			}
			finally
			{
				try
				{
					fis.close();
				}
				catch (Exception ignored)
				{
				}
			}
		}

		return null;
	}

	public String getHtmForce(String prefix, String path)
	{
		String content = getHtm(prefix, path);

		if (content == null)
		{
			content = "<html><body>My text is missing:<br>" + path + "</body></html>";
			Log.warning("Cache[HTML]: Missing HTML page: " + path);
		}

		return content;
	}

	public String getHtm(String prefix, String path)
	{
		String newPath = null;
		String content;
		if (prefix != null && !prefix.isEmpty())
		{
			newPath = prefix + path;
			content = getHtm(newPath);
			if (content != null)
			{
				return content;
			}
		}

		String customPath = "data_" + Config.SERVER_NAME + "/html/" + path;
		content = getHtm(customPath);
		if (content != null)
		{
			return content;
		}

		if (path.contains(Config.DATA_FOLDER + ""))
		{
			content = getHtm(path);
		}
		else
		{
			content = getHtm(Config.DATA_FOLDER + "html/" + path);
		}

		if (content != null)
		{
			_cache.put(customPath.hashCode(), content);
			if (newPath != null)
			{
				_cache.put(newPath.hashCode(), content);
			}
		}

		return content;
	}

	public String getHtm(String path)
	{
		if (path == null || path.isEmpty())
		{
			return ""; // avoid possible NPE
		}

		final int hashCode = path.hashCode();
		String content = _cache.get(hashCode);

		if (Config.LAZY_CACHE && content == null)
		{
			content = loadFile(new File(Config.DATAPACK_ROOT, path));
		}

		return content;
	}

	public boolean contains(String path)
	{
		return _cache.containsKey(path.hashCode());
	}

	/**
	 * Check if an HTM exists and can be loaded
	 *
	 * @param path The path to the HTM
	 */
	public boolean isLoadable(String path)
	{
		File file = new File(Config.DATA_FOLDER + "html/" + path);
		HtmFilter filter = new HtmFilter();

		if (file.exists() && filter.accept(file) && !file.isDirectory())
		{
			return true;
		}

		file = new File("data_" + Config.SERVER_NAME + "/html/" + path);
		filter = new HtmFilter();

		return file.exists() && filter.accept(file) && !file.isDirectory();

	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final HtmCache _instance = new HtmCache();
	}
}
