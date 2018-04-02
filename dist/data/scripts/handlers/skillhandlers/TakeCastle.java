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
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.entity.Castle;
import l2server.gameserver.templates.skills.SkillType;

/**
 * @author _drunk_
 */
public class TakeCastle implements ISkillHandler {
	private static final SkillType[] SKILL_IDS = {SkillType.TAKECASTLE};

	/**
	 * @see l2server.gameserver.handler.ISkillHandler#useSkill(Creature, Skill, WorldObject[])
	 */
	@Override
	public void useSkill(Creature activeChar, Skill skill, WorldObject[] targets) {
		if (!(activeChar instanceof Player)) {
			return;
		}

		Player player = (Player) activeChar;

		if (player.getClan() == null || player.getClan().getLeaderId() != player.getObjectId()) {
			return;
		}

		Castle castle = CastleManager.getInstance().getCastle(player);
		if (castle == null || !player.checkIfOkToCastSealOfRule(castle, true, skill)) {
			return;
		}

		try {
			L2Clan originalOwner = ClanTable.getInstance().getClan(castle.getOwnerId());
			castle.engrave(player.getClan(), targets[0]);
			if (skill == FrequentSkill.IMPRINT_OF_DARKNESS.getSkill()) {
				castle.setTendency(Castle.TENDENCY_DARKNESS);
			} else {
				castle.setTendency(Castle.TENDENCY_LIGHT);
			}

			if (originalOwner != null) {
				originalOwner.checkTendency();
			}
			player.getClan().checkTendency();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see l2server.gameserver.handler.ISkillHandler#getSkillIds()
	 */
	@Override
	public SkillType[] getSkillIds() {
		return SKILL_IDS;
	}

	public static void main(String[] args) {
		new TakeCastle();
	}
}
