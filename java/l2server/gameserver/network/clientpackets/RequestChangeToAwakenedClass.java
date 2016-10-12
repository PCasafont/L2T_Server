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
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.datatables.PlayerClassTable;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.L2SkillLearn;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.base.PlayerClass;
import l2server.gameserver.network.serverpackets.SocialAction;

/**
 * @author Pere
 */
public final class RequestChangeToAwakenedClass extends L2GameClientPacket
{
	private boolean _change;

	@Override
	protected void readImpl()
	{
		_change = readD() == 1;
	}

	/**
	 */
	@Override
	protected void runImpl()
	{
		if (!_change)
		{
			return;
		}

		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}

		if (player.getLastCheckedAwakeningClassId() <= 0)
		{
			return;
		}

		PlayerClass cl = PlayerClassTable.getInstance().getClassById(player.getLastCheckedAwakeningClassId());
		PlayerClass previousClass = player.getCurrentClass();

		if (player.getLevel() < 85)
		{
			player.sendMessage("You cannot awaken yet! Come back when you reach level 85.");
			return;
		}

		if (player.getCurrentClass().getLevel() != 76)
		{
			player.sendMessage("In order to awaken you must have at least 3rd profession.");
			return;
		}

		boolean isNatural = PlayerClassTable.getInstance().getAwakening(previousClass.getId()) == cl.getId();
		if (!isNatural)
		{
			player.sendMessage("Your current class cannot awaken to " + cl.getName() + ".");
			return;
		}

		int cloakId = 30171 + previousClass.getAwakeningClassId();
		if (previousClass.getAwakeningClassId() == 144)
		{
			cloakId++;
		}
		else if (previousClass.getAwakeningClassId() == 145)
		{
			cloakId--;
		}

		player.addItem("Awakening", cloakId, 1, player, true); // Corresponding cloak
		if (player.getClassId() == player.getBaseClass())
		{
			player.addItem("Awakening", 36949, 1, player, true); // Chaos Essence
		}
		else
		{
			player.addItem("Awakening", 37494, 1, player, true); // Chaos Essence - Dual Class
		}
		// TODO: More items?

		for (L2Skill skill : player.getAllSkills())
		{
			int skillId = skill.getId();
			boolean remove = true;
			for (L2SkillLearn sl : cl.getSkills().values())
			{
				if (sl.getMinLevel() >= 85 && (sl.getId() == skillId || sl.getCostSkills().contains(skillId)))
				{
					remove = false;
					break;
				}
			}

			if (remove)
			{
				player.removeSkill(skill, true);
			}
		}

		player.setClassId(cl.getId());

		if (!player.isSubClassActive() && previousClass.getId() == player.getBaseClass())
		{
			player.setBaseClass(cl.getId());
		}

		if (Config.isServer(Config.TENKAI_ESTHUS))
		{
			player.giveAvailableSkills(true);
		}

		// Add race skills
		player.addRaceSkills();

		// Send new skill list
		player.sendSkillList();

		// Start animation
		player.broadcastPacket(new SocialAction(player.getObjectId(), 20)); //All use same id since valiance

		player.broadcastUserInfo();
		player.setIsImmobilized(true);
		ThreadPoolManager.getInstance().scheduleGeneral(() ->
		{
			player.setIsImmobilized(false);

			// Is this even custom?
			//player.sendPacket(new ExShowUsmPacket(10));
		}, 7500L);
	}
}
