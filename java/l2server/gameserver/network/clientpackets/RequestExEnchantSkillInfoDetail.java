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
	private int type;
	private int skillId;
	private int skillLvl;
	private int skillEnch;

	@Override
	protected void readImpl()
	{
		this.type = readD();
		this.skillId = readD();
		this.skillLvl = readH();
		this.skillEnch = readH();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see l2server.gameserver.clientpackets.ClientBasePacket#runImpl()
	 */
	@Override
	protected void runImpl()
	{
		if (this.skillId <= 0 || this.skillLvl <= 0 || this.skillEnch <= 0) // minimal sanity check
		{
			return;
		}

		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}

		L2EnchantSkillLearn esl = EnchantCostsTable.getInstance().getSkillEnchantmentBySkillId(this.skillId);
		if (esl == null)
		{
			return;
		}

		int enchRoute = this.skillEnch / 1000;
		int enchLvl = this.skillEnch % 1000;
		int reqEnchLvl = -2;

		L2Skill curSkill = activeChar.getKnownSkill(this.skillId);
		if (curSkill == null)
		{
			return;
		}

		if (this.type == 0 || this.type == 1 || this.type == 4)
		{
			reqEnchLvl = enchLvl - 1; // enchant
			if (esl.isMaxEnchant(enchRoute, curSkill.getEnchantLevel()))
			{
				reqEnchLvl = curSkill.getEnchantLevel();
			}
		}
		else if (this.type == 2)
		{
			reqEnchLvl = enchLvl + 1; // untrain
		}
		else if (this.type == 3)
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
			if (this.type == 3 && curSkill.getEnchantLevel() != enchLvl)
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
				new ExEnchantSkillInfoDetail(this.type, this.skillId, this.skillLvl, enchRoute, enchLvl, activeChar);
		activeChar.sendPacket(esd);
	}
}
