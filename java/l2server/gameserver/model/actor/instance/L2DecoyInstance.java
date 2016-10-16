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

import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.ai.L2AttackableAI;
import l2server.gameserver.ai.L2CharacterAI;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Attackable;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.knownlist.DecoyKnownList;
import l2server.gameserver.model.actor.stat.DecoyStat;
import l2server.gameserver.network.serverpackets.CharInfo;
import l2server.gameserver.stats.skills.L2SkillDecoy;
import l2server.gameserver.taskmanager.DecayTaskManager;
import l2server.gameserver.templates.chars.L2NpcTemplate;
import l2server.gameserver.templates.item.L2Weapon;
import l2server.log.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;
import java.util.logging.Level;

public class L2DecoyInstance extends L2Attackable
{
	private L2PcInstance owner;
	private int totalLifeTime;
	private int timeRemaining;
	private Future<?> decoyLifeTask;
	private List<Future<?>> skillSpam = new ArrayList<>();

	public L2DecoyInstance(int objectId, L2NpcTemplate template, L2PcInstance owner, L2Skill skill)
	{
		super(objectId, template);
		this.owner = owner;
		setXYZ(owner.getX(), owner.getY(), owner.getZ());
		setIsInvul(false);
		setInstanceType(InstanceType.L2DecoyInstance);
		if (skill != null)
		{
			this.totalLifeTime = ((L2SkillDecoy) skill).getTotalLifeTime();
		}
		else
		{
			this.totalLifeTime = 20000;
		}
		this.timeRemaining = this.totalLifeTime;
		int delay = 1000;
		this.decoyLifeTask = ThreadPoolManager.getInstance()
				.scheduleGeneralAtFixedRate(new DecoyLifetime(getOwner(), this), delay, delay);
		if (template.getSkills() != null)
		{
			for (L2Skill s : template.getSkills().values())
			{
				if (s.isActive())
				{
					this.skillSpam.add(ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(
							new SkillSpam(this, SkillTable.getInstance().getInfo(s.getId(), s.getLevelHash())), 2000,
							5000));
				}
			}
		}
		if (getName().equalsIgnoreCase("Clone Attack") && getNpcId() >= 13319 && getNpcId() <= 13322)
		{
			this.skillSpam.add(ThreadPoolManager.getInstance()
					.scheduleGeneralAtFixedRate(new SkillSpam(this, null), 100, 100));
		}
	}

	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
		{
			return false;
		}
		for (Future<?> spam : this.skillSpam)
		{
			spam.cancel(true);
		}
		this.skillSpam.clear();
		this.totalLifeTime = 0;
		DecayTaskManager.getInstance().addDecayTask(this);
		return true;
	}

	@Override
	public L2CharacterAI getAI()
	{
		L2CharacterAI ai = this.ai; // copy handle
		if (ai == null)
		{
			synchronized (this)
			{
				if (this.ai == null)
				{
					if (getNpcId() >= 13319 && getNpcId() <= 13322)
					{
						this.ai = new L2AttackableAI(new L2Attackable.AIAccessor());
					}
					else
					{
						this.ai = new L2CharacterAI(new L2Character.AIAccessor());
					}
				}

				return this.ai;
			}
		}
		setIsRunning(true);
		return ai;
	}

	@Override
	public DecoyKnownList getKnownList()
	{
		return (DecoyKnownList) super.getKnownList();
	}

	@Override
	public void initKnownList()
	{
		setKnownList(new DecoyKnownList(this));
	}

	@Override
	public final DecoyStat getStat()
	{
		return (DecoyStat) super.getStat();
	}

	@Override
	public void initCharStat()
	{
		setStat(new DecoyStat(this));
	}

	static class DecoyLifetime implements Runnable
	{
		private L2PcInstance activeChar;

		private L2DecoyInstance decoy;

		DecoyLifetime(L2PcInstance activeChar, L2DecoyInstance Decoy)
		{
			this.activeChar = activeChar;
			this.decoy = Decoy;
		}

		@Override
		public void run()
		{
			try
			{
				double newTimeRemaining;
				this.decoy.decTimeRemaining(1000);
				newTimeRemaining = this.decoy.getTimeRemaining();
				if (newTimeRemaining < 0)
				{
					this.decoy.unSummon(this.activeChar);
				}
			}
			catch (Exception e)
			{
				Log.log(Level.SEVERE, "Decoy Error: ", e);
			}
		}
	}

	static class SkillSpam implements Runnable
	{
		private L2DecoyInstance activeChar;

		private L2Skill skill;

		SkillSpam(L2DecoyInstance activeChar, L2Skill Hate)
		{
			this.activeChar = activeChar;
			this.skill = Hate;
		}

		@Override
		public void run()
		{
			try
			{
				if (this.skill != null)
				{
					this.activeChar.setTarget(this.activeChar);
					this.activeChar.doCast(this.skill);
				}
				else if (this.activeChar.getOwner().getTarget() instanceof L2Character)
				{
					L2Character target = (L2Character) this.activeChar.getOwner().getTarget();
					this.activeChar.addDamageHate(target, 1, 1);
					//_activeChar.doAttack(target);
					this.activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
				}
			}
			catch (Throwable e)
			{
				Log.log(Level.SEVERE, "Decoy Error: ", e);
			}
		}
	}

	public void decTimeRemaining(int value)
	{
		this.timeRemaining -= value;
	}

	public int getTimeRemaining()
	{
		return this.timeRemaining;
	}

	public int getTotalLifeTime()
	{
		return this.totalLifeTime;
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();
		getOwner().sendPacket(new CharInfo(this));
	}

	@Override
	public void updateAbnormalEffect()
	{
		Collection<L2PcInstance> plrs = getKnownList().getKnownPlayers().values();
		//synchronized (getKnownList().getKnownPlayers())
		{
			for (L2PcInstance player : plrs)
			{
				if (player != null)
				{
					player.sendPacket(new CharInfo(this));
				}
			}
		}
	}

	public void stopDecay()
	{
		DecayTaskManager.getInstance().cancelDecayTask(this);
	}

	@Override
	public void onDecay()
	{
		deleteMe(this.owner);
	}

	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return this.owner.isAutoAttackable(attacker);
	}

	@Override
	public L2ItemInstance getActiveWeaponInstance()
	{
		return null;
	}

	@Override
	public L2Weapon getActiveWeaponItem()
	{
		return null;
	}

	@Override
	public L2ItemInstance getSecondaryWeaponInstance()
	{
		return null;
	}

	@Override
	public L2Weapon getSecondaryWeaponItem()
	{
		return null;
	}

	public void deleteMe(L2PcInstance owner)
	{
		decayMe();
		getKnownList().removeAllKnownObjects();
		owner.setDecoy(null);
	}

	public synchronized void unSummon(L2PcInstance owner)
	{
		if (this.decoyLifeTask != null)
		{
			this.decoyLifeTask.cancel(true);
			this.decoyLifeTask = null;
		}
		for (Future<?> spam : this.skillSpam)
		{
			spam.cancel(true);
		}
		this.skillSpam.clear();

		if (isVisible() && !isDead())
		{
			if (getWorldRegion() != null)
			{
				getWorldRegion().removeFromZones(this);
			}
			owner.setDecoy(null);
			decayMe();
			getKnownList().removeAllKnownObjects();
		}
	}

	@Override
	public final L2PcInstance getOwner()
	{
		return this.owner;
	}

	@Override
	public L2PcInstance getActingPlayer()
	{
		return this.owner;
	}

	@Override
	public void sendInfo(L2PcInstance activeChar)
	{
		activeChar.sendPacket(new CharInfo(this));
	}
}
