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

package l2server.gameserver.network.clientpackets;

import l2server.Config;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.instance.L2PetInstance;
import l2server.gameserver.model.actor.instance.L2SummonInstance;
import l2server.gameserver.templates.skills.L2SkillTargetType;
import l2server.gameserver.templates.skills.L2SkillType;

/**
 * @author KenM
 */
public class RequestDispel extends L2GameClientPacket
{
	private int _objectId;
	private int _skillId;
	private int _skillLevel;

	/**
	 * @see l2server.gameserver.network.clientpackets.L2GameClientPacket#readImpl()
	 */
	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_skillId = readD();
		_skillLevel = readD();
	}

	/**
	 * @see l2server.gameserver.network.clientpackets.L2GameClientPacket#runImpl()
	 */
	@Override
	protected void runImpl()
	{
		if (_skillId <= 0 || _skillLevel <= 0)
		{
			return;
		}

		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}

		L2Skill skill = SkillTable.getInstance().getInfo(_skillId, _skillLevel);
		if (skill == null)
		{
			return;
		}
		if (!skill.canBeDispeled() || skill.isStayAfterDeath() || skill.isDebuff())
		{
			return;
		}
		if (skill.getTransformId() > 0 && skill.getTargetType() != L2SkillTargetType.TARGET_SELF &&
				skill.getTargetType() != L2SkillTargetType.TARGET_PARTY &&
				skill.getSkillType() != L2SkillType.BUFF) //LasTravel: Self/Party transformation buffs can be cancelled
		{
			return;
		}
		if (skill.isDance() && !Config.DANCE_CANCEL_BUFF)
		{
			return;
		}
		if (activeChar.getObjectId() == _objectId)
		{
			activeChar.stopSkillEffects(_skillId);
		}
		else
		{
			final L2PetInstance pet = activeChar.getPet();
			if (pet != null && pet.getObjectId() == _objectId)
			{
				pet.stopSkillEffects(_skillId);
			}
			for (L2SummonInstance summon : activeChar.getSummons())
			{
				summon.stopSkillEffects(_skillId);
			}
		}
	}
}
