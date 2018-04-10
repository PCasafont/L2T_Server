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

import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.datatables.MapRegionTable;
import l2server.gameserver.instancemanager.GrandBossManager;
import l2server.gameserver.model.Location;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.StatsSet;
import l2server.gameserver.templates.skills.SkillType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SkillTeleport extends Skill {
	private static Logger log = LoggerFactory.getLogger(SkillTeleport.class.getName());

	private final String recallType;
	private final Location loc;

	public SkillTeleport(StatsSet set) {
		super(set);

		recallType = set.getString("recallType", "");
		String coords = set.getString("teleCoords", null);
		if (coords != null) {
			String[] valuesSplit = coords.split(",");
			loc = new Location(Integer.parseInt(valuesSplit[0]), Integer.parseInt(valuesSplit[1]), Integer.parseInt(valuesSplit[2]));
		} else {
			loc = null;
		}
	}

	@Override
	public void useSkill(Creature activeChar, WorldObject[] targets) {
		if (activeChar instanceof Player) {
			// Thanks nbd
			if (((Player) activeChar).getEvent() != null && !((Player) activeChar).getEvent().onEscapeUse(activeChar.getObjectId())) {
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}

			if (activeChar.isAfraid() || activeChar.isInLove()) {
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}

			if (!((Player) activeChar).canEscape() || ((Player) activeChar).isCombatFlagEquipped()) {
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}

			if (((Player) activeChar).isInOlympiadMode()) {
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
				return;
			}
		}

		try {
			for (Creature target : (Creature[]) targets) {
				if (target instanceof Player) {
					Player targetChar = (Player) target;

					// Check to see if player is in jail
					if (targetChar.isInJail()) {
						targetChar.sendMessage("You can not escape from jail.");
						continue;
					}

					if (!targetChar.canEscape() || targetChar.isCombatFlagEquipped()) {
						continue;
					}

					// Check to see if player is in a duel
					if (targetChar.isInDuel()) {
						targetChar.sendMessage("You cannot use escape skills during a duel.");
						continue;
					}

					if (targetChar != activeChar) {
						if (targetChar.getEvent() != null && !targetChar.getEvent().onEscapeUse(targetChar.getObjectId())) {
							continue;
						}

						if (targetChar.isInOlympiadMode()) {
							continue;
						}

						if (GrandBossManager.getInstance().getZone(targetChar) != null) {
							continue;
						}

						if (targetChar.isCombatFlagEquipped()) {
							continue;
						}
					}
				}
				Location loc = null;
				if (getSkillType() == SkillType.TELEPORT) {
					if (this.loc != null) {
						// target is not player OR player is not flying or flymounted
						// TODO: add check for gracia continent coords
						if (!(target instanceof Player) || !(target.isFlying() || ((Player) target).isFlyingMounted())) {
							loc = this.loc;
						}
					}
				} else {
					if (recallType.equalsIgnoreCase("Castle")) {
						loc = MapRegionTable.getInstance().getTeleToLocation(target, MapRegionTable.TeleportWhereType.Castle);
					} else if (recallType.equalsIgnoreCase("ClanHall")) {
						loc = MapRegionTable.getInstance().getTeleToLocation(target, MapRegionTable.TeleportWhereType.ClanHall);
					} else if (recallType.equalsIgnoreCase("Fortress")) {
						loc = MapRegionTable.getInstance().getTeleToLocation(target, MapRegionTable.TeleportWhereType.Fortress);
					} else {
						loc = MapRegionTable.getInstance().getTeleToLocation(target, MapRegionTable.TeleportWhereType.Town);
					}
				}
				if (loc != null) {
					target.setInstanceId(0);

					final Location tpTo = loc;
					final Creature chararacter = target;
					ThreadPoolManager.getInstance().scheduleGeneral(() -> chararacter.teleToLocation(tpTo, true), 400);
				}
			}
		} catch (Exception e) {
			log.error("", e);
		}
	}
}
