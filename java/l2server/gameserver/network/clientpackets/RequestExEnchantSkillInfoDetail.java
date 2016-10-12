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

import l2server.gameserver.datatables.EnchantCostsTable;
import l2server.gameserver.model.L2EnchantSkillLearn;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.serverpackets.ExEnchantSkillInfoDetail;

/**
 * Format (ch) ddd c: (id) 0xD0 h: (subid) 0x31 d: type d: skill id d: skill lvl
 *
 * @author -Wooden-
 */
public final class RequestExEnchantSkillInfoDetail extends L2GameClientPacket
{
	private int _type;
	private int _skillId;
	private int _skillLvl;
	private int _skillEnch;

	@Override
	protected void readImpl()
	{
		_type = readD();
		_skillId = readD();
		_skillLvl = readH();
		_skillEnch = readH();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see l2server.gameserver.clientpackets.ClientBasePacket#runImpl()
	 */
	@Override
	protected void runImpl()
	{
		if (_skillId <= 0 || _skillLvl <= 0 || _skillEnch <= 0) // minimal sanity check
		{
			return;
		}

		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}

		L2EnchantSkillLearn esl = EnchantCostsTable.getInstance().getSkillEnchantmentBySkillId(_skillId);
		if (esl == null)
		{
			return;
		}

		int enchRoute = _skillEnch / 1000;
		int enchLvl = _skillEnch % 1000;
		int reqEnchLvl = -2;

		L2Skill curSkill = activeChar.getKnownSkill(_skillId);
		if (curSkill == null)
		{
			return;
		}

		if (_type == 0 || _type == 1 || _type == 4)
		{
			reqEnchLvl = enchLvl - 1; // enchant
			if (esl.isMaxEnchant(enchRoute, curSkill.getEnchantLevel()))
			{
				reqEnchLvl = curSkill.getEnchantLevel();
			}
		}
		else if (_type == 2)
		{
			reqEnchLvl = enchLvl + 1; // untrain
		}
		else if (_type == 3)
		{
			reqEnchLvl = enchLvl; // change route
		}

		// if reqlvl is 100,200,.. check base skill lvl enchant
		if (reqEnchLvl == 0)
		{
			// if player dont have min level to enchant
			if (curSkill.getLevel() < esl.getBaseLevel())
			{
				return;
			}
		}
		else if (curSkill.getEnchantRouteId() != enchRoute)
		{
			// change route is different skill lvl but same enchant
			if (_type == 3 && curSkill.getEnchantLevel() != enchLvl)
			{
				return;
			}
		}
		else if (reqEnchLvl != curSkill.getEnchantLevel())
		{
			return;
		}

		// send skill enchantment detail
		ExEnchantSkillInfoDetail esd =
				new ExEnchantSkillInfoDetail(_type, _skillId, _skillLvl, enchRoute, enchLvl, activeChar);
		activeChar.sendPacket(esd);
	}
}
