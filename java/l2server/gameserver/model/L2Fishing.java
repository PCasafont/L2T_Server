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

import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.datatables.NpcTable;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.instance.L2PenaltyMonsterInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ExFishingHpRegen;
import l2server.gameserver.network.serverpackets.ExFishingStartCombat;
import l2server.gameserver.network.serverpackets.PlaySound;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.chars.L2NpcTemplate;
import l2server.util.Rnd;

import java.util.concurrent.Future;

public class L2Fishing implements Runnable
{
	// =========================================================
	// Data Field
	private L2PcInstance fisher;
	private int time;
	private int stop = 0;
	private int goodUse = 0;
	private int anim = 0;
	private int mode = 0;
	private int deceptiveMode = 0;
	private Future<?> fishAiTask;
	private boolean thinking;
	// Fish datas
	private int fishId;
	private int fishMaxHp;
	private int fishCurHp;
	private double regenHp;
	private boolean isUpperGrade;
	private int lureType;

	@Override
	public void run()
	{
		if (this.fisher == null)
		{
			return;
		}

		if (this.fishCurHp >= this.fishMaxHp * 2)
		{
			// The fish got away
			this.fisher.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.BAIT_STOLEN_BY_FISH));
			doDie(false);
		}
		else if (this.time <= 0)
		{
			// Time is up, so that fish got away
			this.fisher.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FISH_SPIT_THE_HOOK));
			doDie(false);
		}
		else
		{
			aiTask();
		}
	}

	// =========================================================
	public L2Fishing(L2PcInstance Fisher, FishData fish, boolean isNoob, boolean isUpperGrade)
	{
		this.fisher = Fisher;
		this.fishMaxHp = fish.getHP();
		this.fishCurHp = this.fishMaxHp;
		this.regenHp = fish.getHpRegen();
		this.fishId = fish.getId();
		this.time = fish.getCombatTime() / 1000;
		this.isUpperGrade = isUpperGrade;
		if (isUpperGrade)
		{
			this.deceptiveMode = Rnd.get(100) >= 90 ? 1 : 0;
			this.lureType = 2;
		}
		else
		{
			this.deceptiveMode = 0;
			this.lureType = isNoob ? 0 : 1;
		}
		this.mode = Rnd.get(100) >= 80 ? 1 : 0;

		ExFishingStartCombat efsc =
				new ExFishingStartCombat(this.fisher, this.time, this.fishMaxHp, this.mode, this.lureType, this.deceptiveMode);
		this.fisher.broadcastPacket(efsc);
		this.fisher.sendPacket(new PlaySound(1, "SF_S_01", 0, 0, 0, 0, 0));
		// Succeeded in getting a bite
		this.fisher.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.GOT_A_BITE));

		if (this.fishAiTask == null)
		{
			this.fishAiTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(this, 1000, 1000);
		}
	}

	public void changeHp(int hp, int pen)
	{
		this.fishCurHp -= hp;
		if (this.fishCurHp < 0)
		{
			this.fishCurHp = 0;
		}

		ExFishingHpRegen efhr =
				new ExFishingHpRegen(this.fisher, this.time, this.fishCurHp, this.mode, this.goodUse, this.anim, pen, this.deceptiveMode);
		this.fisher.broadcastPacket(efhr);
		this.anim = 0;
		if (this.fishCurHp > fishMaxHp * 2)
		{
			this.fishCurHp = this.fishMaxHp * 2;
			doDie(false);
		}
		else if (this.fishCurHp == 0)
		{
			doDie(true);
		}
	}

	public synchronized void doDie(boolean win)
	{
		if (this.fishAiTask != null)
		{
			this.fishAiTask.cancel(false);
			this.fishAiTask = null;
		}

		if (this.fisher == null)
		{
			return;
		}

		if (win)
		{
			int check = Rnd.get(100);
			if (check <= 5)
			{
				PenaltyMonster();
			}
			else
			{
				this.fisher.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_CAUGHT_SOMETHING));
				this.fisher.addItem("Fishing", this.fishId, 1, null, true);
			}
		}
		this.fisher.endFishing(win);
		this.fisher = null;
	}

	protected void aiTask()
	{
		if (this.thinking)
		{
			return;
		}
		this.thinking = true;
		this.time--;

		try
		{
			if (this.mode == 1)
			{
				if (this.deceptiveMode == 0)
				{
					this.fishCurHp += (int) this.regenHp;
				}
			}
			else
			{
				if (this.deceptiveMode == 1)
				{
					this.fishCurHp += (int) this.regenHp;
				}
			}
			if (this.stop == 0)
			{
				this.stop = 1;
				int check = Rnd.get(100);
				if (check >= 70)
				{
					this.mode = this.mode == 0 ? 1 : 0;
				}
				if (this.isUpperGrade)
				{
					check = Rnd.get(100);
					if (check >= 90)
					{
						this.deceptiveMode = this.deceptiveMode == 0 ? 1 : 0;
					}
				}
			}
			else
			{
				this.stop--;
			}
		}
		finally
		{
			this.thinking = false;
			ExFishingHpRegen efhr =
					new ExFishingHpRegen(this.fisher, this.time, this.fishCurHp, this.mode, 0, this.anim, 0, this.deceptiveMode);
			if (this.anim != 0)
			{
				this.fisher.broadcastPacket(efhr);
			}
			else
			{
				this.fisher.sendPacket(efhr);
			}
		}
	}

	public void useRealing(int dmg, int pen)
	{
		this.anim = 2;
		if (Rnd.get(100) > 90)
		{
			this.fisher.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FISH_RESISTED_ATTEMPT_TO_BRING_IT_IN));
			this.goodUse = 0;
			changeHp(0, pen);
			return;
		}
		if (this.fisher == null)
		{
			return;
		}
		if (this.mode == 1)
		{
			if (this.deceptiveMode == 0)
			{
				// Reeling is successful, Damage: $s1
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.REELING_SUCCESFUL_S1_DAMAGE);
				sm.addNumber(dmg);
				this.fisher.sendPacket(sm);
				if (pen == 50)
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.REELING_SUCCESSFUL_PENALTY_S1);
					sm.addNumber(pen);
					this.fisher.sendPacket(sm);
				}
				this.goodUse = 1;
				changeHp(dmg, pen);
			}
			else
			{
				// Reeling failed, Damage: $s1
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.FISH_RESISTED_REELING_S1_HP_REGAINED);
				sm.addNumber(dmg);
				this.fisher.sendPacket(sm);
				this.goodUse = 2;
				changeHp(-dmg, pen);
			}
		}
		else
		{
			if (this.deceptiveMode == 0)
			{
				// Reeling failed, Damage: $s1
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.FISH_RESISTED_REELING_S1_HP_REGAINED);
				sm.addNumber(dmg);
				this.fisher.sendPacket(sm);
				this.goodUse = 2;
				changeHp(-dmg, pen);
			}
			else
			{
				// Reeling is successful, Damage: $s1
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.REELING_SUCCESFUL_S1_DAMAGE);
				sm.addNumber(dmg);
				this.fisher.sendPacket(sm);
				if (pen == 50)
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.REELING_SUCCESSFUL_PENALTY_S1);
					sm.addNumber(pen);
					this.fisher.sendPacket(sm);
				}
				this.goodUse = 1;
				changeHp(dmg, pen);
			}
		}
	}

	public void usePomping(int dmg, int pen)
	{
		this.anim = 1;
		if (Rnd.get(100) > 90)
		{
			this.fisher.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FISH_RESISTED_ATTEMPT_TO_BRING_IT_IN));
			this.goodUse = 0;
			changeHp(0, pen);
			return;
		}
		if (this.fisher == null)
		{
			return;
		}
		if (this.mode == 0)
		{
			if (this.deceptiveMode == 0)
			{
				// Pumping is successful. Damage: $s1
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.PUMPING_SUCCESFUL_S1_DAMAGE);
				sm.addNumber(dmg);
				this.fisher.sendPacket(sm);
				if (pen == 50)
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.PUMPING_SUCCESSFUL_PENALTY_S1);
					sm.addNumber(pen);
					this.fisher.sendPacket(sm);
				}
				this.goodUse = 1;
				changeHp(dmg, pen);
			}
			else
			{
				// Pumping failed, Regained: $s1
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.FISH_RESISTED_PUMPING_S1_HP_REGAINED);
				sm.addNumber(dmg);
				this.fisher.sendPacket(sm);
				this.goodUse = 2;
				changeHp(-dmg, pen);
			}
		}
		else
		{
			if (this.deceptiveMode == 0)
			{
				// Pumping failed, Regained: $s1
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.FISH_RESISTED_PUMPING_S1_HP_REGAINED);
				sm.addNumber(dmg);
				this.fisher.sendPacket(sm);
				this.goodUse = 2;
				changeHp(-dmg, pen);
			}
			else
			{
				// Pumping is successful. Damage: $s1
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.PUMPING_SUCCESFUL_S1_DAMAGE);
				sm.addNumber(dmg);
				this.fisher.sendPacket(sm);
				if (pen == 50)
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.PUMPING_SUCCESSFUL_PENALTY_S1);
					sm.addNumber(pen);
					this.fisher.sendPacket(sm);
				}
				this.goodUse = 1;
				changeHp(dmg, pen);
			}
		}
	}

	private void PenaltyMonster()
	{
		int lvl = (int) Math.round(this.fisher.getLevel() * 0.1);

		int npcid;

		this.fisher.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_CAUGHT_SOMETHING_SMELLY_THROW_IT_BACK));
		switch (lvl)
		{
			case 0:
			case 1:
				npcid = 18319;
				break;
			case 2:
				npcid = 18320;
				break;
			case 3:
				npcid = 18321;
				break;
			case 4:
				npcid = 18322;
				break;
			case 5:
				npcid = 18323;
				break;
			case 6:
				npcid = 18324;
				break;
			case 7:
				npcid = 18325;
				break;
			case 8:
			case 9:
				npcid = 18326;
				break;
			default:
				npcid = 18319;
				break;
		}
		L2NpcTemplate temp;
		temp = NpcTable.getInstance().getTemplate(npcid);
		if (temp != null)
		{
			try
			{
				L2Spawn spawn = new L2Spawn(temp);
				spawn.setX(this.fisher.getX());
				spawn.setY(this.fisher.getY());
				spawn.setZ(this.fisher.getZ() + 20);
				spawn.setHeading(this.fisher.getHeading());
				spawn.stopRespawn();
				spawn.doSpawn();
				spawn.getNpc().scheduleDespawn(3 * 60 * 1000);
				((L2PenaltyMonsterInstance) spawn.getNpc()).setPlayerToKill(this.fisher);
			}
			catch (Exception e)
			{
				// Nothing
			}
		}
	}
}
