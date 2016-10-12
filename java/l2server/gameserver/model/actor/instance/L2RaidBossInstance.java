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

package l2server.gameserver.model.actor.instance;

import l2server.Config;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.instancemanager.BossManager;
import l2server.gameserver.instancemanager.RaidBossPointsManager;
import l2server.gameserver.model.L2RandomMinionData;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.L2Spawn;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Summon;
import l2server.gameserver.model.olympiad.HeroesManager;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.chars.L2NpcTemplate;
import l2server.util.Rnd;

/**
 * This class manages all RaidBoss.
 * In a group mob, there are one master called RaidBoss and several slaves called Minions.
 *
 * @version $Revision: 1.20.4.6 $ $Date: 2005/04/06 16:13:39 $
 */
public class L2RaidBossInstance extends L2MonsterInstance
{
	private static final int RAIDBOSS_MAINTENANCE_INTERVAL = 30000; // 30 sec

	private boolean _useRaidCurse = true;

	/**
	 * Constructor of L2RaidBossInstance (use L2Character and L2NpcInstance constructor).<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Call the L2Character constructor to set the _template of the L2RaidBossInstance (copy skills from template to object and link _calculators to NPC_STD_CALCULATOR) </li>
	 * <li>Set the name of the L2RaidBossInstance</li>
	 * <li>Create a RandomAnimation Task that will be launched after the calculated delay if the server allow it </li><BR><BR>
	 *
	 * @param objectId Identifier of the object to initialized
	 */
	public L2RaidBossInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2RaidBossInstance);
		setIsRaid(true);
		BossManager.getInstance().registerBoss(this);
	}

	@Override
	protected int getMaintenanceInterval()
	{
		return RAIDBOSS_MAINTENANCE_INTERVAL;
	}

	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
		{
			return false;
		}

		L2PcInstance player = null;
		if (killer instanceof L2PcInstance)
		{
			player = (L2PcInstance) killer;
		}
		else if (killer instanceof L2Summon)
		{
			player = ((L2Summon) killer).getOwner();
		}

		if (player != null)
		{
			broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.RAID_WAS_SUCCESSFUL));
			if (player.getParty() != null)
			{
				for (L2PcInstance member : player.getParty().getPartyMembers())
				{
					RaidBossPointsManager.getInstance().addPoints(member, getNpcId(), getLevel() / 2 + Rnd.get(-5, 5));
					if (member.isNoble())
					{
						HeroesManager.getInstance().setRBkilled(member.getObjectId(), getNpcId());
					}
				}
			}
			else
			{
				RaidBossPointsManager.getInstance().addPoints(player, getNpcId(), getLevel() / 2 + Rnd.get(-5, 5));
				if (player.isNoble())
				{
					HeroesManager.getInstance().setRBkilled(player.getObjectId(), getNpcId());
				}
			}
		}

		L2RandomMinionData rMinionData = getTemplate().getRandomMinionData();

		if (rMinionData != null)
		{
			rMinionData.getLastSpawnedMinionIds().clear();
		}

		return true;
	}

	/**
	 * Spawn all minions at a regular interval Also if boss is too far from home
	 * location at the time of this check, teleport it home
	 */
	@Override
	protected void startMaintenanceTask()
	{
		if (getTemplate().getMinionData() != null || getTemplate().getRandomMinionData() != null)
		{
			getMinionList().spawnMinions();
		}

		_maintenanceTask = ThreadPoolManager.getInstance()
				.scheduleGeneralAtFixedRate(this::checkAndReturnToSpawn, 60000,
						getMaintenanceInterval() + Rnd.get(5000));
	}

	protected void checkAndReturnToSpawn()
	{
		if (isDead() || isMovementDisabled())
		{
			return;
		}

		// Gordon does not have permanent spawn
		if (getNpcId() == 29095)
		{
			return;
		}

		final L2Spawn spawn = getSpawn();
		if (spawn == null)
		{
			return;
		}

		final int spawnX = spawn.getX();
		final int spawnY = spawn.getY();
		final int spawnZ = spawn.getZ();

		if (!isInCombat() && !isMovementDisabled())
		{
			if (!isInsideRadius(spawnX, spawnY, spawnZ, Math.max(Config.MAX_DRIFT_RANGE, 200), true, false))
			{
				teleToLocation(spawnX, spawnY, spawnZ, false);
			}
		}
	}

	/**
	 * Reduce the current HP of the L2Attackable, update its _aggroList and launch the doDie Task if necessary.<BR><BR>
	 */
	@Override
	public void reduceCurrentHp(double damage, L2Character attacker, boolean awake, boolean isDOT, L2Skill skill)
	{
		super.reduceCurrentHp(damage, attacker, awake, isDOT, skill);
	}

	@Override
	public float getVitalityPoints(int damage)
	{
		return -super.getVitalityPoints(damage) / 100;
	}

	@Override
	public boolean useVitalityRate()
	{
		return false;
	}

	public void setUseRaidCurse(boolean val)
	{
		_useRaidCurse = val;
	}

	/* (non-Javadoc)
	 * @see l2server.gameserver.model.actor.L2Character#giveRaidCurse()
	 */
	@Override
	public boolean giveRaidCurse()
	{
		return _useRaidCurse;
	}
}
