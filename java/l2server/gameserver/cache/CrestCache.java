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
import l2server.L2DatabaseFactory;
import l2server.gameserver.datatables.ClanTable;
import l2server.gameserver.idfactory.IdFactory;
import l2server.gameserver.model.L2Clan;
import l2server.log.Log;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * @author Layane
 */
public class CrestCache
{
	private ConcurrentHashMap<Integer, byte[]> _mapPledge = new ConcurrentHashMap<>();

	private ConcurrentHashMap<Integer, byte[][]> _mapPledgeLarge = new ConcurrentHashMap<>();

	private ConcurrentHashMap<Integer, byte[]> _mapAlly = new ConcurrentHashMap<>();

	private int _loadedFiles;

	private long _bytesBuffLen;

	public static CrestCache getInstance()
	{
		return SingletonHolder._instance;
	}

	private CrestCache()
	{
		convertOldPedgeFiles();
		reload();
	}

	public void reload()
	{
		FileFilter filter = new BmpFilter();

		File dir = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "crests/");

		File[] files = dir.listFiles(filter);
		byte[] content;
		synchronized (this)
		{
			_loadedFiles = 0;
			_bytesBuffLen = 0;

			_mapPledge.clear();
			_mapPledgeLarge.clear();
			_mapAlly.clear();
		}

		for (File file : files)
		{
			RandomAccessFile f = null;
			synchronized (this)
			{
				try
				{
					f = new RandomAccessFile(file, "r");
					content = new byte[(int) f.length()];
					f.readFully(content);

					if (file.getName().startsWith("Crest_Large_"))
					{
						int subId = Integer.valueOf(file.getName().substring(12, 13));
						int id = Integer.valueOf(file.getName().substring(14, file.getName().length() - 4));
						byte[][] array = _mapPledgeLarge.get(id);
						if (array == null)
						{
							array = new byte[10][];
						}

						array[subId] = content;
						_mapPledgeLarge.put(id, array);
					}
					else if (file.getName().startsWith("Crest_"))
					{
						_mapPledge.put(Integer.valueOf(file.getName().substring(6, file.getName().length() - 4)),
								content);
					}
					else if (file.getName().startsWith("AllyCrest_"))
					{
						_mapAlly.put(Integer.valueOf(file.getName().substring(10, file.getName().length() - 4)),
								content);
					}
					_loadedFiles++;
					_bytesBuffLen += content.length;
				}
				catch (Exception e)
				{
					Log.log(Level.WARNING, "Problem with crest bmp file " + e.getMessage(), e);
				}
				finally
				{
					try
					{
						f.close();
					}
					catch (Exception ignored)
					{
					}
				}
			}
		}

		Log.info("Cache[Crest]: " + String.format("%.3f", getMemoryUsage()) + "MB on " + getLoadedFiles() +
				" files loaded.");
	}

	public void convertOldPedgeFiles()
	{
		File dir = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "crests/");

		File[] files = dir.listFiles(new OldPledgeFilter());

		for (File file : files)
		{
			int clanId = Integer.parseInt(file.getName().substring(7, file.getName().length() - 4));

			Log.info("Found old crest file \"" + file.getName() + "\" for clanId " + clanId);

			int newId = IdFactory.getInstance().getNextId();

			L2Clan clan = ClanTable.getInstance().getClan(clanId);

			if (clan != null)
			{
				removeOldPledgeCrest(clan.getCrestId());

				file.renameTo(new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "crests/Crest_" + newId + ".bmp"));
				Log.info("Renamed Clan crest to new format: Crest_" + newId + ".bmp");

				Connection con = null;

				try
				{
					con = L2DatabaseFactory.getInstance().getConnection();
					PreparedStatement statement =
							con.prepareStatement("UPDATE clan_data SET crest_id = ? WHERE clan_id = ?");
					statement.setInt(1, newId);
					statement.setInt(2, clan.getClanId());
					statement.executeUpdate();
					statement.close();
				}
				catch (SQLException e)
				{
					Log.log(Level.WARNING, "Could not update the crest id:" + e.getMessage(), e);
				}
				finally
				{
					L2DatabaseFactory.close(con);
				}

				clan.setCrestId(newId);
			}
			else
			{
				Log.info("Clan Id: " + clanId + " does not exist in table.. deleting.");
				file.delete();
			}
		}
	}

	public float getMemoryUsage()
	{
		return (float) _bytesBuffLen / 1048576;
	}

	public int getLoadedFiles()
	{
		return _loadedFiles;
	}

	public byte[] getPledgeCrest(int id)
	{
		return _mapPledge.get(id);
	}

	public byte[][] getPledgeCrestLarge(int id)
	{
		return _mapPledgeLarge.get(id);
	}

	public byte[] getAllyCrest(int id)
	{
		return _mapAlly.get(id);
	}

	public void removePledgeCrest(int id)
	{
		File crestFile = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "crests/Crest_" + id + ".bmp");
		_mapPledge.remove(id);
		try
		{
			crestFile.delete();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void removePledgeCrestLarge(int id)
	{
		File crestFile = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "crests/Crest_Large_" + id + ".bmp");
		_mapPledgeLarge.remove(id);
		try
		{
			crestFile.delete();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void removeOldPledgeCrest(int id)
	{
		File crestFile = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "crests/Pledge_" + id + ".bmp");
		try
		{
			crestFile.delete();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void removeAllyCrest(int id)
	{
		File crestFile = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "crests/AllyCrest_" + id + ".bmp");
		_mapAlly.remove(id);
		try
		{
			crestFile.delete();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public boolean savePledgeCrest(int newId, byte[] data)
	{
		File crestFile = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "crests/Crest_" + newId + ".bmp");
		FileOutputStream out = null;
		try
		{
			out = new FileOutputStream(crestFile);
			out.write(data);
			_mapPledge.put(newId, data);
			return true;
		}
		catch (IOException e)
		{
			Log.log(Level.INFO, "Error saving pledge crest" + crestFile + ":", e);
			return false;
		}
		finally
		{
			try
			{
				out.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public boolean savePledgeCrestLarge(int newId, int subId, byte[] data)
	{
		File crestFile = new File(Config.DATAPACK_ROOT,
				Config.DATA_FOLDER + "crests/Crest_Large_" + subId + "_" + newId + ".bmp");
		FileOutputStream out = null;
		try
		{
			out = new FileOutputStream(crestFile);
			out.write(data);
			byte[][] array = _mapPledgeLarge.get(newId);
			if (array == null)
			{
				array = new byte[10][];
			}
			array[subId] = data;
			_mapPledgeLarge.put(newId, array);
			return true;
		}
		catch (IOException e)
		{
			Log.log(Level.INFO, "Error saving Large pledge crest" + crestFile + ":", e);
			return false;
		}
		finally
		{
			try
			{
				out.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public boolean saveAllyCrest(int newId, byte[] data)
	{
		File crestFile = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "crests/AllyCrest_" + newId + ".bmp");
		FileOutputStream out = null;
		try
		{
			out = new FileOutputStream(crestFile);
			out.write(data);
			_mapAlly.put(newId, data);
			return true;
		}
		catch (IOException e)
		{
			Log.log(Level.INFO, "Error saving ally crest" + crestFile + ":", e);
			return false;
		}
		finally
		{
			try
			{
				out.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	private static class BmpFilter implements FileFilter
	{
		@Override
		public boolean accept(File file)
		{
			return file.getName().endsWith(".bmp");
		}
	}

	private static class OldPledgeFilter implements FileFilter
	{
		@Override
		public boolean accept(File file)
		{
			return file.getName().startsWith("Pledge_");
		}
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final CrestCache _instance = new CrestCache();
	}
}
