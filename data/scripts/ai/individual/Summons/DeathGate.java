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

import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import ai.group_template.L2AttackableAIScript;

/**
 * @author LasTravel
 * @author Pere
 *         <p>
 *         Summon Death Gate (skill id: 11266) AI
 */

public class DeathGate extends L2AttackableAIScript
{
    private static final int[] _deathGateIds = {14927, 15200, 15201, 15202};
    private static final int _summonDeathGateId = 11266;

    public DeathGate(int id, String name, String descr)
    {
        super(id, name, descr);

        for (int i : _deathGateIds)
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
        private L2Skill _gateVortex;
        private L2Skill _gateRoot;
        private L2Skill lastSkillUsed;
        private L2Npc _deathGate;
        private L2PcInstance _owner;
        private ScheduledFuture<?> _schedule = null;

        protected DeathGateAI(L2Npc npc, L2PcInstance owner)
        {
            _deathGate = npc;

            _owner = owner;
            if (_owner == null)
            {
                return;
            }

            int skillLevel = _owner.getSkillLevel(_summonDeathGateId);
            if (skillLevel == -1)
            {
                return;
            }

            _gateVortex = SkillTable.getInstance().getInfo(11291, skillLevel);
            _gateRoot = SkillTable.getInstance().getInfo(11289, skillLevel);
        }

        public void setSchedule(ScheduledFuture<?> schedule)
        {
            _schedule = schedule;
        }

        @Override
        public void run()
        {
            if (_deathGate == null || _deathGate.isDead() || _deathGate.isDecayed() || _deathGate.getOwner()
                    .isAlikeDead())
            {
                if (_schedule != null)
                {
                    _schedule.cancel(true);
                    return;
                }
            }

            _deathGate.setTarget(_deathGate);

            if (lastSkillUsed == _gateVortex)
            {
                lastSkillUsed = _gateRoot;
            }
            else
            {
                lastSkillUsed = _gateVortex;
            }

            _deathGate.doCast(lastSkillUsed);
        }
    }

    public static void main(String[] args)
    {
        new DeathGate(-1, "DeathGate", "ai/individual");
    }
}
