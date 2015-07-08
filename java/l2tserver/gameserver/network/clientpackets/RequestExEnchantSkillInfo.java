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
package l2tserver.gameserver.network.clientpackets;

import l2tserver.gameserver.datatables.EnchantCostsTable;
import l2tserver.gameserver.datatables.SkillTable;
import l2tserver.gameserver.model.L2Skill;
import l2tserver.gameserver.model.actor.instance.L2PcInstance;
import l2tserver.gameserver.network.serverpackets.ExEnchantSkillInfo;

/**
 * Format (ch) dd
 * c: (id) 0xD0
 * h: (subid) 0x06
 * d: skill id
 * d: skill lvl
 * @author -Wooden-
 *
 */
public final class RequestExEnchantSkillInfo extends L2GameClientPacket
{
	private static final String _C__D0_0E_REQUESTEXENCHANTSKILLINFO = "[C] D0:0E RequestExEnchantSkillInfo";
	
	private int _skillId;
	private int _skillLvl;
	private int _skillEnchant;
	
	@Override
	protected void readImpl()
	{
		_skillId = readD();
		_skillLvl = readH();
		_skillEnchant = readH();
	}
	
	/* (non-Javadoc)
	 * @see l2tserver.gameserver.clientpackets.ClientBasePacket#runImpl()
	 */
	@Override
	protected void runImpl()
	{
		if (_skillId <= 0 || _skillLvl <= 0) // minimal sanity check
			return;

		L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
			return;
		
		if (activeChar.getLevel() < 76)
			return;
		
		/* L2Npc trainer = activeChar.getLastFolkNPC();
		if (!(trainer instanceof L2NpcInstance))
			return;

		if (!trainer.canInteract(activeChar) && !activeChar.isGM())
			return;*/
		
		L2Skill skill = SkillTable.getInstance().getInfo(_skillId, _skillLvl, _skillEnchant);
		if (skill == null || skill.getId() != _skillId)
			return;
		
		if (EnchantCostsTable.getInstance().getSkillEnchantmentBySkillId(_skillId) == null)
			return;
		
		int playerSkillLvl = activeChar.getSkillLevelHash(_skillId);
		if (playerSkillLvl == -1 || playerSkillLvl != _skillLvl + (_skillEnchant << 16))
			return;
		
		activeChar.sendPacket(new ExEnchantSkillInfo(_skillId, _skillLvl, _skillEnchant / 1000, _skillEnchant % 1000));
	}
	
	/* (non-Javadoc)
	 * @see l2tserver.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__D0_0E_REQUESTEXENCHANTSKILLINFO;
	}
	
}