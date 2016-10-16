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

package ai.individual.Summons;

import java.util.concurrent.ScheduledFuture;

import ai.group_template.L2AttackableAIScript;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author LasTravel
 * @author Pere
 *         <p>
 *         Summon Death Gate (skill id: 11266) AI
 */

public class DeathGate extends L2AttackableAIScript
{
	private static final int[] deathGateIds = {14927, 15200, 15201, 15202};
	private static final int summonDeathGateId = 11266;

	public DeathGate(int id, String name, String descr)
	{
		super(id, name, descr);

		for (int i : this.deathGateIds)
		{
			addSpawnId(i);
		}
	}

	@Override
	public final String onSpawn(L2Npc npc)
	{
		npc.disableCoreAI(true);
		npc.setIsInvul(true);

		DeathGateAI ai = new DeathGateAI(npc, npc.getOwner());

		ai.setSchedule(ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(ai, 1000, 4000));

		return null;
	}

	class DeathGateAI implements Runnable
	{
		private L2Skill gateVortex;
		private L2Skill gateRoot;
		private L2Skill lastSkillUsed;
		private L2Npc deathGate;
		private L2PcInstance owner;
		private ScheduledFuture<?> schedule = null;

		protected DeathGateAI(L2Npc npc, L2PcInstance owner)
		{
			this.deathGate = npc;

			this.owner = owner;
			if (this.owner == null)
			{
				return;
			}

			int skillLevel = this.owner.getSkillLevel(summonDeathGateId);
			if (skillLevel == -1)
			{
				return;
			}

			this.gateVortex = SkillTable.getInstance().getInfo(11291, skillLevel);
			this.gateRoot = SkillTable.getInstance().getInfo(11289, skillLevel);
		}

		public void setSchedule(ScheduledFuture<?> schedule)
		{
			this.schedule = schedule;
		}

		@Override
		public void run()
		{
			if (this.deathGate == null || this.deathGate.isDead() || this.deathGate.isDecayed() ||
					this.deathGate.getOwner().isAlikeDead())
			{
				if (this.schedule != null)
				{
					this.schedule.cancel(true);
					return;
				}
			}

			this.deathGate.setTarget(this.deathGate);

			if (lastSkillUsed == this.gateVortex)
			{
				lastSkillUsed = this.gateRoot;
			}
			else
			{
				lastSkillUsed = this.gateVortex;
			}

			this.deathGate.doCast(lastSkillUsed);
		}
	}

	public static void main(String[] args)
	{
		new DeathGate(-1, "DeathGate", "ai/individual");
	}
}
