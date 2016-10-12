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

package l2server.gameserver.model.actor.knownlist;

import l2server.gameserver.ai.CtrlEvent;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.ai.L2CharacterAI;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2MonsterInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.instance.L2RaidBossInstance;

public class MonsterKnownList extends AttackableKnownList
{
	public MonsterKnownList(L2MonsterInstance activeChar)
	{
		super(activeChar);
	}

	@Override
	public boolean addKnownObject(L2Object object)
	{
		if (!super.addKnownObject(object))
		{
			return false;
		}

		if (object instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) object;
			if (!player.isGM())
			{
				if (player.getPvpFlag() > 0 && getActiveChar() instanceof L2RaidBossInstance &&
						player.getLevel() > getActiveChar().getLevel() + 8 &&
						getActiveChar().isInsideRadius(object, 500, true, true))
				{
					L2Skill tempSkill = SkillTable.getInstance().getInfo(4515, 1);
					if (tempSkill != null)
					{
						tempSkill.getEffects(getActiveChar(), player);
					}
				}
			}
		}

		final L2CharacterAI ai = getActiveChar().getAI(); // force AI creation

		// Set the L2MonsterInstance Intention to AI_INTENTION_ACTIVE if the state was AI_INTENTION_IDLE
		if (object instanceof L2PcInstance && ai != null && ai.getIntention() == CtrlIntention.AI_INTENTION_IDLE)
		{
			ai.setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
		}

		return true;
	}

	@Override
	protected boolean removeKnownObject(L2Object object, boolean forget)
	{
		if (!super.removeKnownObject(object, forget))
		{
			return false;
		}

		if (!(object instanceof L2Character))
		{
			return true;
		}

		if (getActiveChar().hasAI())
		{
			// Notify the L2MonsterInstance AI with EVT_FORGET_OBJECT
			getActiveChar().getAI().notifyEvent(CtrlEvent.EVT_FORGET_OBJECT, object);
		}

		if (getActiveChar().isVisible() && getKnownPlayers().isEmpty() && getKnownSummons().isEmpty())
		{
			// Clear the _aggroList of the L2MonsterInstance
			getActiveChar().clearAggroList();

			// Remove all L2Object from _knownObjects and _knownPlayer of the L2MonsterInstance then cancel Attak or Cast and notify AI
			//removeAllKnownObjects();
		}

		return true;
	}

	@Override
	public final L2MonsterInstance getActiveChar()
	{
		return (L2MonsterInstance) super.getActiveChar();
	}
}
