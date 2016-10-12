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

package l2server.gameserver.stats.skills;

import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.StatsSet;

public class L2SkillMount extends L2Skill
{
	private int _npcId;
	private int _itemId;

	public L2SkillMount(StatsSet set)
	{
		super(set);
		_npcId = set.getInteger("npcId", 0);
		_itemId = set.getInteger("itemId", 0);
	}

	@Override
	public void useSkill(L2Character caster, L2Object[] targets)
	{
		if (!(caster instanceof L2PcInstance))
		{
			return;
		}

		L2PcInstance activePlayer = (L2PcInstance) caster;

		if (activePlayer.getEvent() != null && !activePlayer.getEvent().onItemSummon(activePlayer.getObjectId()))
		{
			return;
		}

		if (!activePlayer.getFloodProtectors().getItemPetSummon().tryPerformAction("mount"))
		{
			return;
		}

		// Dismount Action
		if (_npcId == 0)
		{
			activePlayer.dismount();
			return;
		}

		if (activePlayer.isSitting())
		{
			activePlayer.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_MOVE_SITTING));
			return;
		}

		if (activePlayer.inObserverMode())
		{
			return;
		}

		if (activePlayer.isInOlympiadMode())
		{
			activePlayer.sendPacket(
					SystemMessage.getSystemMessage(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
			return;
		}

		if (activePlayer.getPet() != null || activePlayer.isMounted())
		{
			activePlayer.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_ALREADY_HAVE_A_PET));
			return;
		}

		if (activePlayer.isAttackingNow() || activePlayer.isCursedWeaponEquipped())
		{
			activePlayer.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_CANNOT_SUMMON_IN_COMBAT));
			return;
		}

		activePlayer.mount(_npcId, _itemId, false);
	}
}
