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
import lombok.Getter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;

/**
 * Clan member class.
 */
public class L2ClanMember
{
	@Getter private L2Clan clan;
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
		apprentice = 0;
		sponsor = 0;
		this.sex = sex;
		this.raceOrdinal = raceOrdinal;
	}

	public L2ClanMember(L2Clan clan, L2PcInstance player)
	{
		this.clan = clan;
		name = player.getName();
		level = player.getLevel();
		classId = player.getCurrentClass().getId();
		objectId = player.getObjectId();
		pledgeType = player.getPledgeType();
		powerGrade = player.getPowerGrade();
		title = player.getTitle();
		sponsor = 0;
		apprentice = 0;
		sex = player.getAppearance().getSex();
		raceOrdinal = player.getRace().ordinal();
	}

	public L2ClanMember(L2PcInstance player)
	{
		if (player.getClan() == null)
		{
			throw new IllegalArgumentException("Can not create a ClanMember if player has a null clan.");
		}
		clan = player.getClan();
		this.player = player;
		name = this.player.getName();
		level = this.player.getLevel();
		classId = this.player.getCurrentClass().getId();
		objectId = this.player.getObjectId();
		powerGrade = this.player.getPowerGrade();
		pledgeType = this.player.getPledgeType();
		title = this.player.getTitle();
		apprentice = 0;
		sponsor = 0;
		sex = this.player.getAppearance().getSex();
		raceOrdinal = this.player.getRace().ordinal();
	}

	public void setPlayerInstance(L2PcInstance player)
	{
		if (player == null && this.player != null)
		{
			// this is here to keep the data when the player logs off
			name = this.player.getName();
			level = this.player.getLevel();
			classId = this.player.getCurrentClass().getId();
			objectId = this.player.getObjectId();
			powerGrade = this.player.getPowerGrade();
			pledgeType = this.player.getPledgeType();
			title = this.player.getTitle();
			apprentice = this.player.getApprentice();
			sponsor = this.player.getSponsor();
			sex = this.player.getAppearance().getSex();
			raceOrdinal = this.player.getRace().ordinal();
		}

		if (player != null)
		{
			clan.addSkillEffects(player);
			if (clan.getLevel() > 3 && player.isClanLeader())
			{
				SiegeManager.getInstance().addSiegeSkills(player);
			}
			if (player.isClanLeader())
			{
				clan.setLeader(this);
			}
		}

		this.player = player;
	}

	public L2PcInstance getPlayerInstance()
	{
		return player;
	}

	public boolean isOnline()
	{
		if (player == null || !player.isOnline())
		{
			return false;
		}
		if (player.getClient() == null)
		{
			return false;
		}
		return !player.getClient().isDetached();
	}

	/**
	 * @return Returns the classId.
	 */
	public int getCurrentClass()
	{
		if (player != null)
		{
			return player.getCurrentClass().getId();
		}
		return classId;
	}

	/**
	 * @return Returns the level.
	 */
	public int getLevel()
	{
		if (player != null)
		{
			return player.getLevel();
		}
		return level;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName()
	{
		if (player != null)
		{
			return player.getName();
		}
		return name;
	}

	/**
	 * @return Returns the objectId.
	 */
	public int getObjectId()
	{
		if (player != null)
		{
			return player.getObjectId();
		}
		return objectId;
	}

	public String getTitle()
	{
		if (player != null)
		{
			return player.getTitle();
		}
		return title;
	}

	public int getPledgeType()
	{
		if (player != null)
		{
			return player.getPledgeType();
		}
		return pledgeType;
	}

	public void setPledgeType(int pledgeType)
	{
		this.pledgeType = pledgeType;
		if (player != null)
		{
			player.setPledgeType(pledgeType);
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
			statement.setLong(1, pledgeType);
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
		if (player != null)
		{
			return player.getPowerGrade();
		}
		return powerGrade;
	}

	/**
	 * @param powerGrade
	 */
	public void setPowerGrade(int powerGrade)
	{
		this.powerGrade = powerGrade;
		if (player != null)
		{
			player.setPowerGrade(powerGrade);
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
			statement.setLong(1, powerGrade);
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
		apprentice = apprenticeID;
		sponsor = sponsorID;
	}

	public int getRaceOrdinal()
	{
		if (player != null)
		{
			return player.getRace().ordinal();
		}
		else
		{
			return raceOrdinal;
		}
	}

	public boolean getSex()
	{
		if (player != null)
		{
			return player.getAppearance().getSex();
		}
		else
		{
			return sex;
		}
	}

	public int getSponsor()
	{
		if (player != null)
		{
			return player.getSponsor();
		}
		else
		{
			return sponsor;
		}
	}

	public int getApprentice()
	{
		if (player != null)
		{
			return player.getApprentice();
		}
		else
		{
			return apprentice;
		}
	}

	public String getApprenticeOrSponsorName()
	{
		if (player != null)
		{
			apprentice = player.getApprentice();
			sponsor = player.getSponsor();
		}

		if (apprentice != 0)
		{
			L2ClanMember apprentice = clan.getClanMember(this.apprentice);
			if (apprentice != null)
			{
				return apprentice.getName();
			}
			else
			{
				return "Error";
			}
		}
		if (sponsor != 0)
		{
			L2ClanMember sponsor = clan.getClanMember(this.sponsor);
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
