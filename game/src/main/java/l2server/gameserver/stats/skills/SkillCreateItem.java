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

import l2server.gameserver.model.Skill;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Playable;
import l2server.gameserver.model.actor.instance.PetInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.PetItemList;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.StatsSet;
import l2server.util.Rnd;

/**
 * @author Nemesiss
 */
public class SkillCreateItem extends Skill {
	private final int[] createItemId;
	private final int createItemCount;
	private final int randomCount;

	public SkillCreateItem(StatsSet set) {
		super(set);
		createItemId = set.getIntegerArray("create_item_id");
		createItemCount = set.getInteger("create_item_count", 0);
		randomCount = set.getInteger("random_count", 1);
	}

	/**
	 * @see Skill#useSkill(Creature, WorldObject[])
	 */
	@Override
	public void useSkill(Creature activeChar, WorldObject[] targets) {
		Player player = activeChar.getActingPlayer();
		if (activeChar.isAlikeDead()) {
			return;
		}
		if (activeChar instanceof Playable) {
			if (createItemId == null || createItemCount == 0) {
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_PREPARED_FOR_REUSE);
				sm.addSkillName(this);
				player.sendPacket(sm);
				return;
			}

			int count = createItemCount + Rnd.nextInt(randomCount);
			int rndid = Rnd.nextInt(createItemId.length);
			if (activeChar instanceof Player) {
				player.addItem("Skill", createItemId[rndid], count, activeChar, true);
			} else if (activeChar instanceof PetInstance) {
				activeChar.getInventory().addItem("Skill", createItemId[rndid], count, player, activeChar);
				player.sendPacket(new PetItemList((PetInstance) activeChar));
			}
		}
	}
}
