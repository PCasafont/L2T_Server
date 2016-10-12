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
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.model.L2CharPosition;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.L2Spawn;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Summon;
import l2server.gameserver.network.serverpackets.NpcSay;
import l2server.gameserver.templates.chars.L2NpcTemplate;
import l2server.log.Log;

import java.util.List;

public class L2FortCommanderInstance extends L2DefenderInstance
{
	private boolean _canTalk;

	public L2FortCommanderInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2FortCommanderInstance);
		_canTalk = true;
	}

	/**
	 * Return True if a siege is in progress and the L2Character attacker isn't a Defender.<BR><BR>
	 *
	 * @param attacker The L2Character that the L2CommanderInstance try to attack
	 */
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		if (attacker == null || !(attacker instanceof L2PcInstance))
		{
			return false;
		}

		// Attackable during siege by all except defenders
		return getFort() != null && getFort().getFortId() > 0 && getFort().getSiege().getIsInProgress() &&
				!getFort().getSiege().checkIsDefender(((L2PcInstance) attacker).getClan());
	}

	@Override
	public void addDamageHate(L2Character attacker, int damage, int aggro)
	{
		if (attacker == null)
		{
			return;
		}

		if (!(attacker instanceof L2FortCommanderInstance))
		{
			super.addDamageHate(attacker, damage, aggro);
		}
	}

	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
		{
			return false;
		}

		if (getFort().getSiege().getIsInProgress())
		{
			getFort().getSiege().killedCommander(this);
		}

		return true;
	}

	/**
	 * This method forces guard to return to home location previously set
	 */
	@Override
	public void returnHome()
	{
		if (!isInsideRadius(getSpawn().getX(), getSpawn().getY(), 200, false))
		{
			if (Config.DEBUG)
			{
				Log.info(getObjectId() + ": moving home");
			}
			setisReturningToSpawnPoint(true);
			clearAggroList();

			if (hasAI())
			{
				getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,
						new L2CharPosition(getSpawn().getX(), getSpawn().getY(), getSpawn().getZ(), 0));
			}
		}
	}

	@Override
	public final void addDamage(L2Character attacker, int damage, L2Skill skill)
	{
		L2Spawn spawn = getSpawn();
		if (spawn != null && canTalk())
		{
			List<L2Spawn> commanders = getFort().getCommanderSpawns();
			for (L2Spawn spawn2 : commanders)
			{
				if (spawn2.getNpcId() == spawn.getNpcId())
				{
					String text = "";
					if (getTemplate().Title.equalsIgnoreCase("Archer"))
					{
						text = "Attacking the enemy's reinforcements is necesary. Time to Die!";
					}
					else if (getTemplate().Title.equalsIgnoreCase("Guard"))
					{
						if (attacker instanceof L2Summon)
						{
							attacker = ((L2Summon) attacker).getOwner();
						}
						text = "Everyone, concentrate your attacks on " + attacker.getName() +
								"! Show the enemy your resolve!";
					}
					else if (getTemplate().Title.equalsIgnoreCase("Support Unit"))
					{
						text = "Spirit of Fire, unleash your power! Burn the enemy!!";
					}

					if (!text.isEmpty())
					{
						broadcastPacket(new NpcSay(getObjectId(), 1, getNpcId(), text));
						setCanTalk(false);
						ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleTalkTask(), 10000);
					}
				}
			}
		}
		super.addDamage(attacker, damage, skill);
	}

	private class ScheduleTalkTask implements Runnable
	{

		public ScheduleTalkTask()
		{
		}

		@Override
		public void run()
		{
			setCanTalk(true);
		}
	}

	void setCanTalk(boolean val)
	{
		_canTalk = val;
	}

	private boolean canTalk()
	{
		return _canTalk;
	}

	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}
}
