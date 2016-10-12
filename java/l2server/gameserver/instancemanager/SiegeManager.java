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

package l2server.gameserver.instancemanager;

import l2server.Config;
import l2server.L2DatabaseFactory;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.model.L2Clan;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.Location;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.entity.Castle;
import l2server.gameserver.model.entity.Siege;
import l2server.log.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

public class SiegeManager
{

	public static SiegeManager getInstance()
	{
		return SingletonHolder._instance;
	}

	// =========================================================
	// Data Field
	private int _attackerMaxClans = 500; // Max number of clans
	private int _attackerRespawnDelay = 0; // Time in ms. Changeable in siege.config
	private int _defenderMaxClans = 500; // Max number of clans

	private int _flagMaxCount = 1; // Changeable in siege.config
	private int _siegeClanMinLevel = 5; // Changeable in siege.config
	private int _siegeLength = 120; // Time in minute. Changeable in siege.config
	private int _bloodAllianceReward = 0; // Number of Blood Alliance items reward for successful castle defending

	// =========================================================
	// Constructor
	private SiegeManager()
	{
		Log.info("Initializing SiegeManager");
		load();
	}

	// =========================================================
	// Method - Public
	public final void addSiegeSkills(L2PcInstance character)
	{
		for (L2Skill sk : SkillTable.getInstance()
				.getSiegeSkills(character.isNoble(), character.getClan().getHasCastle() > 0))
		{
			character.addSkill(sk, false);
		}
	}

	/**
	 * Return true if character summon<BR><BR>
	 *
	 * @param activeChar The L2Character of the character can summon
	 */
	public final boolean checkIfOkToSummon(L2Character activeChar, boolean isCheckOnly)
	{
		if (!(activeChar instanceof L2PcInstance))
		{
			return false;
		}

		String text = "";
		L2PcInstance player = (L2PcInstance) activeChar;
		Castle castle = CastleManager.getInstance().getCastle(player);

		if (castle == null || castle.getCastleId() <= 0)
		{
			text = "You must be on castle ground to summon this";
		}
		else if (!castle.getSiege().getIsInProgress())
		{
			text = "You can only summon this during a siege.";
		}
		else if (player.getClanId() != 0 && castle.getSiege().getAttackerClan(player.getClanId()) == null)
		{
			text = "You can only summon this as a registered attacker.";
		}
		else
		{
			return true;
		}

		if (!isCheckOnly)
		{
			player.sendMessage(text);
		}
		return false;
	}

	/**
	 * Return true if the clan is registered or owner of a castle<BR><BR>
	 *
	 * @param clan The L2Clan of the player
	 */
	public final boolean checkIsRegistered(L2Clan clan, int castleid)
	{
		if (clan == null)
		{
			return false;
		}

		if (clan.getHasCastle() > 0)
		{
			return true;
		}

		Connection con = null;
		boolean register = false;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement =
					con.prepareStatement("SELECT clan_id FROM siege_clans where clan_id=? and castle_id=?");
			statement.setInt(1, clan.getClanId());
			statement.setInt(2, castleid);
			ResultSet rs = statement.executeQuery();

			if (rs.next())
			{
				register = true;
			}

			rs.close();
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "Exception: checkIsRegistered(): " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		return register;
	}

	public final void removeSiegeSkills(L2PcInstance character)
	{
		for (L2Skill sk : SkillTable.getInstance()
				.getSiegeSkills(character.isNoble(), character.getClan().getHasCastle() > 0))
		{
			character.removeSkill(sk);
		}
	}

	// =========================================================
	// Method - Private
	private void load()
	{
		InputStream is = null;
		try
		{
			is = new FileInputStream(new File(Config.CONFIG_DIRECTORY + Config.CONFIG_FILE));
			Properties siegeSettings = new Properties();
			siegeSettings.load(is);

			// Siege setting
			_attackerMaxClans = Integer.decode(siegeSettings.getProperty("AttackerMaxClans", "500"));
			_attackerRespawnDelay = Integer.decode(siegeSettings.getProperty("AttackerRespawn", "0"));
			_defenderMaxClans = Integer.decode(siegeSettings.getProperty("DefenderMaxClans", "500"));
			_flagMaxCount = Integer.decode(siegeSettings.getProperty("MaxFlags", "1"));
			_siegeClanMinLevel = Integer.decode(siegeSettings.getProperty("SiegeClanMinLevel", "5"));
			_siegeLength = Integer.decode(siegeSettings.getProperty("SiegeLength", "120"));
			_bloodAllianceReward = Integer.decode(siegeSettings.getProperty("BloodAllianceReward", "0"));

			for (Castle castle : CastleManager.getInstance().getCastles())
			{
				MercTicketManager.MERCS_MAX_PER_CASTLE[castle.getCastleId() - 1] = Integer.parseInt(siegeSettings
						.getProperty(castle.getName() + "MaxMercenaries",
								Integer.toString(MercTicketManager.MERCS_MAX_PER_CASTLE[castle.getCastleId() - 1]))
						.trim());
			}
		}
		catch (Exception e)
		{
			//_initialized = false;
			Log.log(Level.WARNING, "Error while loading siege data: " + e.getMessage(), e);
		}
		finally
		{
			try
			{
				is.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	// =========================================================
	// Property - Public
	public final int getAttackerMaxClans()
	{
		return _attackerMaxClans;
	}

	public final int getAttackerRespawnDelay()
	{
		return _attackerRespawnDelay;
	}

	public final int getDefenderMaxClans()
	{
		return _defenderMaxClans;
	}

	public final int getFlagMaxCount()
	{
		return _flagMaxCount;
	}

	public final Siege getSiege(L2Object activeObject)
	{
		return getSiege(activeObject.getX(), activeObject.getY(), activeObject.getZ());
	}

	public final Siege getSiege(int x, int y, int z)
	{
		for (Castle castle : CastleManager.getInstance().getCastles())
		{
			if (castle.getSiege().checkIfInZone(x, y, z))
			{
				return castle.getSiege();
			}
		}
		return null;
	}

	public final int getSiegeClanMinLevel()
	{
		return _siegeClanMinLevel;
	}

	public final int getSiegeLength()
	{
		return _siegeLength;
	}

	public final int getBloodAllianceReward()
	{
		return _bloodAllianceReward;
	}

	public final List<Siege> getSieges()
	{
		ArrayList<Siege> sieges = new ArrayList<>();
		for (Castle castle : CastleManager.getInstance().getCastles())
		{
			sieges.add(castle.getSiege());
		}
		return sieges;
	}

	public static class SiegeSpawn
	{
		Location _location;
		private int _npcId;
		private int _heading;
		private int _castleId;
		private int _hp;

		public SiegeSpawn(int castle_id, int x, int y, int z, int heading, int npc_id)
		{
			_castleId = castle_id;
			_location = new Location(x, y, z, heading);
			_heading = heading;
			_npcId = npc_id;
		}

		public SiegeSpawn(int castle_id, int x, int y, int z, int heading, int npc_id, int hp)
		{
			_castleId = castle_id;
			_location = new Location(x, y, z, heading);
			_heading = heading;
			_npcId = npc_id;
			_hp = hp;
		}

		public int getCastleId()
		{
			return _castleId;
		}

		public int getNpcId()
		{
			return _npcId;
		}

		public int getHeading()
		{
			return _heading;
		}

		public int getHp()
		{
			return _hp;
		}

		public Location getLocation()
		{
			return _location;
		}
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final SiegeManager _instance = new SiegeManager();
	}
}
