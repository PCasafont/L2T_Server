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

package l2server.gameserver.model;

import l2server.L2DatabaseFactory;
import l2server.gameserver.instancemanager.SiegeManager;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.log.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;

/**
 * Clan member class.
 */
public class L2ClanMember
{
	private L2Clan clan;
	private int objectId;
	private String name;
	private String title;
	private int powerGrade;
	private int level;
	private int classId;
	private boolean sex;
	private int raceOrdinal;
	private L2PcInstance player;
	private int pledgeType;
	private int apprentice;
	private int sponsor;

	public L2ClanMember(L2Clan clan, String name, int level, int classId, int objectId, int pledgeType, int powerGrade, String title, boolean sex, int raceOrdinal)
	{
		if (clan == null)
		{
			throw new IllegalArgumentException("Can not create a ClanMember with a null clan.");
		}
		this.clan = clan;
		this.name = name;
		this.level = level;
		this.classId = classId;
		this.objectId = objectId;
		this.powerGrade = powerGrade;
		this.title = title;
		this.pledgeType = pledgeType;
		this.apprentice = 0;
		this.sponsor = 0;
		this.sex = sex;
		this.raceOrdinal = raceOrdinal;
	}

	public L2ClanMember(L2Clan clan, L2PcInstance player)
	{
		this.clan = clan;
		this.name = player.getName();
		this.level = player.getLevel();
		this.classId = player.getCurrentClass().getId();
		this.objectId = player.getObjectId();
		this.pledgeType = player.getPledgeType();
		this.powerGrade = player.getPowerGrade();
		this.title = player.getTitle();
		this.sponsor = 0;
		this.apprentice = 0;
		this.sex = player.getAppearance().getSex();
		this.raceOrdinal = player.getRace().ordinal();
	}

	public L2ClanMember(L2PcInstance player)
	{
		if (player.getClan() == null)
		{
			throw new IllegalArgumentException("Can not create a ClanMember if player has a null clan.");
		}
		this.clan = player.getClan();
		this.player = player;
		this.name = this.player.getName();
		this.level = this.player.getLevel();
		this.classId = this.player.getCurrentClass().getId();
		this.objectId = this.player.getObjectId();
		this.powerGrade = this.player.getPowerGrade();
		this.pledgeType = this.player.getPledgeType();
		this.title = this.player.getTitle();
		this.apprentice = 0;
		this.sponsor = 0;
		this.sex = this.player.getAppearance().getSex();
		this.raceOrdinal = this.player.getRace().ordinal();
	}

	public void setPlayerInstance(L2PcInstance player)
	{
		if (player == null && this.player != null)
		{
			// this is here to keep the data when the player logs off
			this.name = this.player.getName();
			this.level = this.player.getLevel();
			this.classId = this.player.getCurrentClass().getId();
			this.objectId = this.player.getObjectId();
			this.powerGrade = this.player.getPowerGrade();
			this.pledgeType = this.player.getPledgeType();
			this.title = this.player.getTitle();
			this.apprentice = this.player.getApprentice();
			this.sponsor = this.player.getSponsor();
			this.sex = this.player.getAppearance().getSex();
			this.raceOrdinal = this.player.getRace().ordinal();
		}

		if (player != null)
		{
			this.clan.addSkillEffects(player);
			if (this.clan.getLevel() > 3 && player.isClanLeader())
			{
				SiegeManager.getInstance().addSiegeSkills(player);
			}
			if (player.isClanLeader())
			{
				this.clan.setLeader(this);
			}
		}

		this.player = player;
	}

	public L2PcInstance getPlayerInstance()
	{
		return this.player;
	}

	public boolean isOnline()
	{
		if (this.player == null || !this.player.isOnline())
		{
			return false;
		}
		if (this.player.getClient() == null)
		{
			return false;
		}
		return !this.player.getClient().isDetached();

	}

	/**
	 * @return Returns the classId.
	 */
	public int getCurrentClass()
	{
		if (this.player != null)
		{
			return this.player.getCurrentClass().getId();
		}
		return this.classId;
	}

	/**
	 * @return Returns the level.
	 */
	public int getLevel()
	{
		if (this.player != null)
		{
			return this.player.getLevel();
		}
		return this.level;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName()
	{
		if (this.player != null)
		{
			return this.player.getName();
		}
		return this.name;
	}

	/**
	 * @return Returns the objectId.
	 */
	public int getObjectId()
	{
		if (this.player != null)
		{
			return this.player.getObjectId();
		}
		return this.objectId;
	}

	public String getTitle()
	{
		if (this.player != null)
		{
			return this.player.getTitle();
		}
		return this.title;
	}

	public int getPledgeType()
	{
		if (this.player != null)
		{
			return this.player.getPledgeType();
		}
		return this.pledgeType;
	}

	public void setPledgeType(int pledgeType)
	{
		this.pledgeType = pledgeType;
		if (this.player != null)
		{
			this.player.setPledgeType(pledgeType);
		}
		else
		{
			//db save if char not logged in
			updatePledgeType();
		}
	}

	public void updatePledgeType()
	{
		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET subpledge=? WHERE charId=?");
			statement.setLong(1, this.pledgeType);
			statement.setInt(2, getObjectId());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "Could not update pledge type: " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public int getPowerGrade()
	{
		if (this.player != null)
		{
			return this.player.getPowerGrade();
		}
		return this.powerGrade;
	}

	/**
	 * @param powerGrade
	 */
	public void setPowerGrade(int powerGrade)
	{
		this.powerGrade = powerGrade;
		if (this.player != null)
		{
			this.player.setPowerGrade(powerGrade);
		}
		else
		{
			// db save if char not logged in
			updatePowerGrade();
		}
	}

	/**
	 * Update the characters table of the database with power grade.<BR><BR>
	 */
	public void updatePowerGrade()
	{
		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET power_grade=? WHERE charId=?");
			statement.setLong(1, this.powerGrade);
			statement.setInt(2, getObjectId());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "Could not update power _grade: " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public void initApprenticeAndSponsor(int apprenticeID, int sponsorID)
	{
		this.apprentice = apprenticeID;
		this.sponsor = sponsorID;
	}

	public int getRaceOrdinal()
	{
		if (this.player != null)
		{
			return this.player.getRace().ordinal();
		}
		else
		{
			return this.raceOrdinal;
		}
	}

	public boolean getSex()
	{
		if (this.player != null)
		{
			return this.player.getAppearance().getSex();
		}
		else
		{
			return this.sex;
		}
	}

	public int getSponsor()
	{
		if (this.player != null)
		{
			return this.player.getSponsor();
		}
		else
		{
			return this.sponsor;
		}
	}

	public int getApprentice()
	{
		if (this.player != null)
		{
			return this.player.getApprentice();
		}
		else
		{
			return this.apprentice;
		}
	}

	public String getApprenticeOrSponsorName()
	{
		if (this.player != null)
		{
			this.apprentice = this.player.getApprentice();
			this.sponsor = this.player.getSponsor();
		}

		if (this.apprentice != 0)
		{
			L2ClanMember apprentice = this.clan.getClanMember(this.apprentice);
			if (apprentice != null)
			{
				return apprentice.getName();
			}
			else
			{
				return "Error";
			}
		}
		if (this.sponsor != 0)
		{
			L2ClanMember sponsor = this.clan.getClanMember(this.sponsor);
			if (sponsor != null)
			{
				return sponsor.getName();
			}
			else
			{
				return "Error";
			}
		}
		return "";
	}

	public L2Clan getClan()
	{
		return this.clan;
	}

	public int calculatePledgeClass(L2PcInstance player)
	{
		int pledgeClass = 0;

		if (player == null)
		{
			return pledgeClass;
		}

		L2Clan clan = player.getClan();
		if (clan != null)
		{
			switch (player.getClan().getLevel())
			{
				case 4:
					if (player.isClanLeader())
					{
						pledgeClass = 3;
					}
					break;
				case 5:
					if (player.isClanLeader())
					{
						pledgeClass = 4;
					}
					else
					{
						pledgeClass = 2;
					}
					break;
				case 6:
					switch (player.getPledgeType())
					{
						case -1:
							pledgeClass = 1;
							break;
						case 100:
						case 200:
							pledgeClass = 2;
							break;
						case 0:
							if (player.isClanLeader())
							{
								pledgeClass = 5;
							}
							else
							{
								switch (clan.getLeaderSubPledge(player.getObjectId()))
								{
									case 100:
									case 200:
										pledgeClass = 4;
										break;
									case -1:
									default:
										pledgeClass = 3;
										break;
								}
							}
							break;
					}
					break;
				case 7:
					switch (player.getPledgeType())
					{
						case -1:
							pledgeClass = 1;
							break;
						case 100:
						case 200:
							pledgeClass = 3;
							break;
						case 1001:
						case 1002:
						case 2001:
						case 2002:
							pledgeClass = 2;
							break;
						case 0:
							if (player.isClanLeader())
							{
								pledgeClass = 7;
							}
							else
							{
								switch (clan.getLeaderSubPledge(player.getObjectId()))
								{
									case 100:
									case 200:
										pledgeClass = 6;
										break;
									case 1001:
									case 1002:
									case 2001:
									case 2002:
										pledgeClass = 5;
										break;
									case -1:
									default:
										pledgeClass = 4;
										break;
								}
							}
							break;
					}
					break;
				case 8:
					switch (player.getPledgeType())
					{
						case -1:
							pledgeClass = 1;
							break;
						case 100:
						case 200:
							pledgeClass = 4;
							break;
						case 1001:
						case 1002:
						case 2001:
						case 2002:
							pledgeClass = 3;
							break;
						case 0:
							if (player.isClanLeader())
							{
								pledgeClass = 8;
							}
							else
							{
								switch (clan.getLeaderSubPledge(player.getObjectId()))
								{
									case 100:
									case 200:
										pledgeClass = 7;
										break;
									case 1001:
									case 1002:
									case 2001:
									case 2002:
										pledgeClass = 6;
										break;
									case -1:
									default:
										pledgeClass = 5;
										break;
								}
							}
							break;
					}
					break;
				case 9:
					switch (player.getPledgeType())
					{
						case -1:
							pledgeClass = 1;
							break;
						case 100:
						case 200:
							pledgeClass = 5;
							break;
						case 1001:
						case 1002:
						case 2001:
						case 2002:
							pledgeClass = 4;
							break;
						case 0:
							if (player.isClanLeader())
							{
								pledgeClass = 9;
							}
							else
							{
								switch (clan.getLeaderSubPledge(player.getObjectId()))
								{
									case 100:
									case 200:
										pledgeClass = 8;
										break;
									case 1001:
									case 1002:
									case 2001:
									case 2002:
										pledgeClass = 7;
										break;
									case -1:
									default:
										pledgeClass = 6;
										break;
								}
							}
							break;
					}
					break;
				case 10:
					switch (player.getPledgeType())
					{
						case -1:
							pledgeClass = 1;
							break;
						case 100:
						case 200:
							pledgeClass = 6;
							break;
						case 1001:
						case 1002:
						case 2001:
						case 2002:
							pledgeClass = 5;
							break;
						case 0:
							if (player.isClanLeader())
							{
								pledgeClass = 10;
							}
							else
							{
								switch (clan.getLeaderSubPledge(player.getObjectId()))
								{
									case 100:
									case 200:
										pledgeClass = 9;
										break;
									case 1001:
									case 1002:
									case 2001:
									case 2002:
										pledgeClass = 8;
										break;
									case -1:
									default:
										pledgeClass = 7;
										break;
								}
							}
							break;
					}
					break;
				case 11:
					switch (player.getPledgeType())
					{
						case -1:
							pledgeClass = 1;
							break;
						case 100:
						case 200:
							pledgeClass = 7;
							break;
						case 1001:
						case 1002:
						case 2001:
						case 2002:
							pledgeClass = 6;
							break;
						case 0:
							if (player.isClanLeader())
							{
								pledgeClass = 11;
							}
							else
							{
								switch (clan.getLeaderSubPledge(player.getObjectId()))
								{
									case 100:
									case 200:
										pledgeClass = 10;
										break;
									case 1001:
									case 1002:
									case 2001:
									case 2002:
										pledgeClass = 9;
										break;
									case -1:
									default:
										pledgeClass = 8;
										break;
								}
							}
							break;
					}
					break;
				default:
					pledgeClass = 1;
					break;
			}
		}
		return pledgeClass;
	}

	public void saveApprenticeAndSponsor(int apprentice, int sponsor)
	{
		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement =
					con.prepareStatement("UPDATE characters SET apprentice=?,sponsor=? WHERE charId=?");
			statement.setInt(1, apprentice);
			statement.setInt(2, sponsor);
			statement.setInt(3, getObjectId());
			statement.execute();
			statement.close();
		}
		catch (SQLException e)
		{
			Log.log(Level.WARNING, "Could not save apprentice/sponsor: " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}
}
