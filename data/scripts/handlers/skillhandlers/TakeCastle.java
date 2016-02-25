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

package handlers.skillhandlers;

import l2server.gameserver.datatables.ClanTable;
import l2server.gameserver.datatables.SkillTable.FrequentSkill;
import l2server.gameserver.handler.ISkillHandler;
import l2server.gameserver.instancemanager.CastleManager;
import l2server.gameserver.model.L2Clan;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.entity.Castle;
import l2server.gameserver.templates.skills.L2SkillType;

/**
 * @author _drunk_
 *
 */
public class TakeCastle implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS = { L2SkillType.TAKECASTLE };
	
	/**
	 *
	 * @see l2server.gameserver.handler.ISkillHandler#useSkill(l2server.gameserver.model.actor.L2Character, l2server.gameserver.model.L2Skill, l2server.gameserver.model.L2Object[])
	 */
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if (!(activeChar instanceof L2PcInstance))
			return;
		
		L2PcInstance player = (L2PcInstance) activeChar;
		
		if (player.getClan() == null || player.getClan().getLeaderId() != player.getObjectId())
			return;
		
		Castle castle = CastleManager.getInstance().getCastle(player);
		if (castle == null || !player.checkIfOkToCastSealOfRule(castle, true, skill))
			return;
		
		try
		{
			L2Clan originalOwner = ClanTable.getInstance().getClan(castle.getOwnerId());
			castle.engrave(player.getClan(), targets[0]);
			if (skill == FrequentSkill.IMPRINT_OF_DARKNESS.getSkill())
				castle.setTendency(Castle.TENDENCY_DARKNESS);
			else
				castle.setTendency(Castle.TENDENCY_LIGHT);
			
			if (originalOwner != null)
				originalOwner.checkTendency();
			player.getClan().checkTendency();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 *
	 * @see l2server.gameserver.handler.ISkillHandler#getSkillIds()
	 */
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
	
	public static void main(String[] args)
	{
		new TakeCastle();
	}
}
