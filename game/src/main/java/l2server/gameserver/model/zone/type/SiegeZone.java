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

package l2server.gameserver.model.zone.type;

import l2server.Config;
import l2server.gameserver.datatables.MapRegionTable;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.instancemanager.FortManager;
import l2server.gameserver.instancemanager.FortSiegeManager;
import l2server.gameserver.instancemanager.TownManager;
import l2server.gameserver.model.Abnormal;
import l2server.gameserver.model.L2Clan;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.actor.instance.SiegeSummonInstance;
import l2server.gameserver.model.entity.Fort;
import l2server.gameserver.model.entity.FortSiege;
import l2server.gameserver.model.entity.Siegable;
import l2server.gameserver.model.zone.ZoneType;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;

import java.util.ArrayList;

/**
 * A  siege zone
 *
 * @author durgus
 */
public class SiegeZone extends ZoneType {
	private int siegableId = -1;
	private Siegable siege = null;
	private boolean isActiveSiege = false;
	private static final int DISMOUNT_DELAY = 5;

	public SiegeZone(int id) {
		super(id);
	}

	@Override
	public void setParameter(String name, String value) {
		switch (name) {
			case "castleId":
				if (siegableId != -1) {
					throw new IllegalArgumentException("Siege object already defined!");
				}
				siegableId = Integer.parseInt(value);
				break;
			case "fortId":
				if (siegableId != -1) {
					throw new IllegalArgumentException("Siege object already defined!");
				}
				siegableId = Integer.parseInt(value);
				break;
			case "clanHallId":
				if (siegableId != -1) {
					throw new IllegalArgumentException("Siege object already defined!");
				}
				siegableId = Integer.parseInt(value);
				//TODO clan hall siege
				break;
			default:
				super.setParameter(name, value);
				break;
		}
	}

	@Override
	protected void onEnter(Creature character) {
		if (isActiveSiege) {
			character.setInsideZone(Creature.ZONE_PVP, true);
			character.setInsideZone(Creature.ZONE_SIEGE, true);
			character.setInsideZone(Creature.ZONE_NOSUMMONFRIEND, true);

			if (character instanceof Player) {
				if (((Player) character).isRegisteredOnThisSiegeField(siegableId) || character.isGM()) {
					((Player) character).setIsInSiege(true); // in siege
					if (siege != null && siege.giveFame()) {
						((Player) character).startFameTask(siege.getFameFrequency() * 1000, siege.getFameAmount());
					}
				} else if (siegableId > 100 && !character.isGM()) {
					character.sendMessage("You are not registered at this siege!");
					character.teleToLocation(TownManager.getClosestTown(character).getSpawnLoc(), true);
				}
				character.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ENTERED_COMBAT_ZONE));
				if (!Config.ALLOW_WYVERN_DURING_SIEGE && ((Player) character).getMountType() == 2) {
					character.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.AREA_CANNOT_BE_ENTERED_WHILE_MOUNTED_WYVERN));
					((Player) character).enteredNoLanding(DISMOUNT_DELAY);
				}
			}
		}
	}

	@Override
	protected void onExit(Creature character) {
		character.setInsideZone(Creature.ZONE_PVP, false);
		character.setInsideZone(Creature.ZONE_SIEGE, false);
		character.setInsideZone(Creature.ZONE_NOSUMMONFRIEND, false);
		if (isActiveSiege) {
			if (character instanceof Player) {
				character.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.LEFT_COMBAT_ZONE));
				if (((Player) character).getMountType() == 2) {
					((Player) character).exitedNoLanding();
				}
				// Set pvp flag
				if (((Player) character).getPvpFlag() == 0) {
					((Player) character).updatePvPStatus();
				}
			}
		}
		if (character instanceof Player) {
			Player activeChar = (Player) character;
			activeChar.stopFameTask();
			activeChar.setIsInSiege(false);

			if (siege instanceof FortSiege && activeChar.getInventory().getItemByItemId(9819) != null) {
				// drop combat flag
				Fort fort = FortManager.getInstance().getFortById(siegableId);
				if (fort != null) {
					FortSiegeManager.getInstance().dropCombatFlag(activeChar, fort.getFortId());
				} else {
					int slot = activeChar.getInventory().getSlotFromItem(activeChar.getInventory().getItemByItemId(9819));
					activeChar.getInventory().unEquipItemInBodySlot(slot);
					activeChar.destroyItem("CombatFlag", activeChar.getInventory().getItemByItemId(9819), null, true);
				}
			}
		}

		if (character instanceof SiegeSummonInstance) {
			((SiegeSummonInstance) character).unSummon(((SiegeSummonInstance) character).getOwner());
		}
	}

	@Override
	public void onDieInside(Creature character, Creature killer) {
		super.onDieInside(character, killer);

		if (isActiveSiege) {
			// debuff participants only if they die inside siege zone
			if (character instanceof Player && ((Player) character).isRegisteredOnThisSiegeField(siegableId)) {
				int lvl = 1;
				final Abnormal e = character.getFirstEffect(5660);
				if (e != null) {
					lvl = Math.min(lvl + e.getLevel(), 5);
				}

				final Skill skill = SkillTable.getInstance().getInfo(5660, lvl);
				if (skill != null) {
					skill.getEffects(character, character);
				}
			}
		}
	}

	@Override
	public void onReviveInside(Creature character) {
	}

	public void updateZoneStatusForCharactersInside() {
		if (isActiveSiege) {
			for (Creature character : characterList.values()) {
				if (character != null) {
					onEnter(character);
				}
			}
		} else {
			for (Creature character : characterList.values()) {
				if (character == null) {
					continue;
				}
				character.setInsideZone(Creature.ZONE_PVP, false);
				character.setInsideZone(Creature.ZONE_SIEGE, false);
				character.setInsideZone(Creature.ZONE_NOSUMMONFRIEND, false);

				if (character instanceof Player) {
					character.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.LEFT_COMBAT_ZONE));
					((Player) character).stopFameTask();
					if (((Player) character).getMountType() == 2) {
						((Player) character).exitedNoLanding();
					}
				}
				if (character instanceof SiegeSummonInstance) {
					((SiegeSummonInstance) character).unSummon(((SiegeSummonInstance) character).getOwner());
				}
			}
		}
	}

	/**
	 * Sends a message to all players in this zone
	 *
	 */
	public void announceToPlayers(String message) {
		for (Creature temp : characterList.values()) {
			if (temp instanceof Player) {
				temp.sendMessage(message);
			}
		}
	}

	/**
	 * Returns all players within this zone
	 *
	 */
	public ArrayList<Player> getAllPlayers() {
		ArrayList<Player> players = new ArrayList<>();

		for (Creature temp : characterList.values()) {
			if (temp instanceof Player) {
				players.add((Player) temp);
			}
		}

		return players;
	}

	public int getSiegeObjectId() {
		return siegableId;
	}

	public boolean isActive() {
		return isActiveSiege;
	}

	public void setIsActive(boolean val) {
		isActiveSiege = val;
	}

	public void setSiegeInstance(Siegable siege) {
		this.siege = siege;
	}

	/**
	 * Removes all foreigners from the zone
	 *
	 */
	public void banishForeigners(L2Clan owningClan) {
		for (Creature temp : characterList.values()) {
			if (!(temp instanceof Player)) {
				continue;
			}
			if (((Player) temp).getClan() == owningClan || temp.isGM()) {
				continue;
			}

			temp.teleToLocation(MapRegionTable.TeleportWhereType.Town);
		}
	}
}
