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

package l2server.gameserver.network.serverpackets;

import l2server.Config;
import l2server.gameserver.datatables.EnchantCostsTable;
import l2server.gameserver.datatables.EnchantCostsTable.EnchantSkillDetail;
import l2server.gameserver.model.L2EnchantSkillLearn;
import l2server.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author KenM
 */
public class ExEnchantSkillInfoDetail extends L2GameServerPacket
{
	private static final int TYPE_NORMAL_ENCHANT = 0;
	private static final int TYPE_SAFE_ENCHANT = 1;
	private static final int TYPE_UNTRAIN_ENCHANT = 2;
	private static final int TYPE_CHANGE_ENCHANT = 3;
	private static final int TYPE_IMMORTAL_ENCHANT = 4;

	private int bookId = 0;
	private int reqCount = 0;
	private int multi = 1;
	private final int type;
	private final int skillId;
	private final int skillLvl;
	private final int skillEnch;
	private final int chance;
	private int sp;
	private final int adenacount;

	public ExEnchantSkillInfoDetail(int type, int skillId, int skillLvl, int skillEnchRoute, int skillEnchLvl, L2PcInstance ply)
	{
		L2EnchantSkillLearn enchantLearn = EnchantCostsTable.getInstance().getSkillEnchantmentBySkillId(skillId);
		EnchantSkillDetail esd = null;
		// do we have this skill?
		if (enchantLearn != null)
		{
			if (skillEnchRoute > 0)
			{
				esd = enchantLearn.getEnchantSkillDetail(skillEnchRoute, skillEnchLvl);
			}
			else
			{
				esd = EnchantCostsTable.getInstance().getEnchantGroupDetails().get(0);
			}
		}

		if (esd == null)
		{
			throw new IllegalArgumentException("Skill " + skillId + " dont have enchant data for level " + skillLvl);
		}

		this.chance = type == TYPE_IMMORTAL_ENCHANT ? 100 : esd.getRate(ply);
		this.sp = esd.getSpCost();
		if (type == TYPE_NORMAL_ENCHANT)
		{
			this.multi = EnchantCostsTable.NORMAL_ENCHANT_COST_MULTIPLIER;
		}
		else if (type == TYPE_SAFE_ENCHANT)
		{
			this.multi = EnchantCostsTable.SAFE_ENCHANT_COST_MULTIPLIER;
		}
		else if (type == TYPE_IMMORTAL_ENCHANT)
		{
			this.multi = EnchantCostsTable.IMMORTAL_ENCHANT_COST_MULTIPLIER;
		}
		else if (type == TYPE_UNTRAIN_ENCHANT)
		{
			this.sp = (int) (0.8 * this.sp);
		}
		this.adenacount = esd.getAdenaCost() * this.multi;

		this.type = type;
		this.skillId = skillId;
		this.skillLvl = skillLvl;
		this.skillEnch = skillEnchRoute * 1000 + skillEnchLvl;

		this.reqCount = 1;
		switch (type)
		{
			case TYPE_NORMAL_ENCHANT:
				this.bookId = esd.getRange().getNormalBook();
				if (skillEnchLvl % 10 > 1)
				{
					this.reqCount = 0;
				}
				break;
			case TYPE_SAFE_ENCHANT:
				this.bookId = esd.getRange().getSafeBook();
				break;
			case TYPE_UNTRAIN_ENCHANT:
				this.bookId = esd.getRange().getUntrainBook();
				break;
			case TYPE_CHANGE_ENCHANT:
				this.bookId = esd.getRange().getChangeBook();
				break;
			case TYPE_IMMORTAL_ENCHANT:
				this.bookId = esd.getRange().getImmortalBook();
				break;
			default:
				return;
		}

		if (type != TYPE_SAFE_ENCHANT && !Config.ES_SP_BOOK_NEEDED)
		{
			this.reqCount = 0;
		}
	}

    /*
	  @see l2server.gameserver.network.serverpackets.L2GameServerPacket#getType()
     */

	/**
	 * @see l2server.gameserver.network.serverpackets.L2GameServerPacket#writeImpl()
	 */
	@Override
	protected final void writeImpl()
	{
		writeD(this.type);
		writeD(this.skillId);
		writeH(this.skillLvl);
		writeH(this.skillEnch);
		writeQ(this.sp * this.multi); // sp
		writeD(this.chance); // exp
		writeD(2); // items count?
		writeD(57); // adena //TODO unhardcode me
		writeD(this.adenacount); // adena count
		writeD(this.bookId); // ItemId Required
		writeD(this.reqCount);
	}
}
