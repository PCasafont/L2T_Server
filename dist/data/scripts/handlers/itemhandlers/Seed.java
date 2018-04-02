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

package handlers.itemhandlers;

import l2server.gameserver.datatables.MapRegionTable;
import l2server.gameserver.handler.IItemHandler;
import l2server.gameserver.instancemanager.CastleManorManager;
import l2server.gameserver.model.Item;
import l2server.gameserver.model.L2Manor;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.Playable;
import l2server.gameserver.model.actor.instance.ChestInstance;
import l2server.gameserver.model.actor.instance.MonsterInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.stats.SkillHolder;

/**
 * @author l3x
 */
public class Seed implements IItemHandler {
	/**
	 * @see l2server.gameserver.handler.IItemHandler#useItem(Playable, Item, boolean)
	 */
	@Override
	public void useItem(Playable playable, Item item, boolean forceUse) {
		if (!(playable instanceof Player)) {
			return;
		}

		if (CastleManorManager.getInstance().isDisabled()) {
			return;
		}

		final WorldObject tgt = playable.getTarget();
		if (!(tgt instanceof Npc)) {
			playable.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
			playable.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (!(tgt instanceof MonsterInstance) || tgt instanceof ChestInstance || ((Creature) tgt).isRaid()) {
			playable.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THE_TARGET_IS_UNAVAILABLE_FOR_SEEDING));
			playable.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		final MonsterInstance target = (MonsterInstance) tgt;
		if (target.isDead()) {
			playable.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
			playable.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (target.isSeeded()) {
			playable.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		final int seedId = item.getItemId();
		if (areaValid(seedId, MapRegionTable.getInstance().getAreaCastle(playable))) {
			target.setSeeded(seedId, (Player) playable);
			final SkillHolder[] skills = item.getEtcItem().getSkills();
			if (skills != null) {
				if (skills[0] == null) {
					return;
				}

				Skill itemskill = skills[0].getSkill();
				((Player) playable).useMagic(itemskill, false, false);
			}
		} else {
			playable.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THIS_SEED_MAY_NOT_BE_SOWN_HERE));
		}
	}

	/**
	 * @param seedId
	 * @param castleId
	 * @return
	 */
	private boolean areaValid(int seedId, int castleId) {
		return L2Manor.getInstance().getCastleIdForSeed(seedId) == castleId;
	}
}
