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

/*
  @author FBIagent
 */

package handlers.itemhandlers;

import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.datatables.NpcTable;
import l2server.gameserver.datatables.SummonItemsData;
import l2server.gameserver.handler.IItemHandler;
import l2server.gameserver.model.Item;
import l2server.gameserver.model.L2Spawn;
import l2server.gameserver.model.L2SummonItem;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.Playable;
import l2server.gameserver.model.actor.instance.PetInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.actor.instance.XmassTreeInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.*;
import l2server.gameserver.templates.chars.NpcTemplate;
import l2server.gameserver.util.Broadcast;

import java.util.Collection;

public class SummonItems implements IItemHandler {
	/**
	 * @see l2server.gameserver.handler.IItemHandler#useItem(Playable, Item, boolean)
	 */
	@Override
	public void useItem(Playable playable, Item item, boolean forceUse) {
		if (!(playable instanceof Player)) {
			return;
		}

		final Player activeChar = (Player) playable;

		if (activeChar.getEvent() != null && !activeChar.getEvent().onItemSummon(activeChar.getObjectId())) {
			return;
		}

		if (activeChar.getIsInsideGMEvent()) {
			return;
		}

		if (!activeChar.getFloodProtectors().getItemPetSummon().tryPerformAction("summon items")) {
			return;
		}

		if (activeChar.isSitting()) {
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_MOVE_SITTING));
			return;
		}

		if (activeChar.getBlockCheckerArena() != -1) {
			return;
		}

		if (activeChar.inObserverMode()) {
			return;
		}

		if (activeChar.isInOlympiadMode()) {
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
			return;
		}
		if (activeChar.isAllSkillsDisabled() || activeChar.isCastingNow()) {
			return;
		}

		final L2SummonItem sitem = SummonItemsData.getInstance().getSummonItem(item.getItemId());

		if ((activeChar.getPet() != null || activeChar.isMounted()) && sitem.isPetSummon()) {
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_ALREADY_HAVE_A_PET));
			return;
		}

		if (activeChar.isAttackingNow()) {
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_CANNOT_SUMMON_IN_COMBAT));
			return;
		}

		if (activeChar.isCursedWeaponEquipped() && sitem.isPetSummon()) {
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.STRIDER_CANT_BE_RIDDEN_WHILE_IN_BATTLE));
			return;
		}

		final int npcId = sitem.getNpcId();
		if (npcId == 0) {
			return;
		}

		final NpcTemplate npcTemplate = NpcTable.getInstance().getTemplate(npcId);
		if (npcTemplate == null) {
			return;
		}

		activeChar.stopMove(null, false);

		switch (sitem.getType()) {
			case 0: // static summons (like Christmas tree)
				try {
					Collection<Creature> characters = activeChar.getKnownList().getKnownCharactersInRadius(1200);
					for (Creature ch : characters) {
						if (ch instanceof XmassTreeInstance && npcTemplate.isSpecialTree()) {
							SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.CANNOT_SUMMON_S1_AGAIN);
							sm.addCharName(ch);
							activeChar.sendPacket(sm);
							return;
						}
					}

					if (activeChar.destroyItem("Summon", item.getObjectId(), 1, null, false)) {
						final L2Spawn spawn = new L2Spawn(npcTemplate);
						spawn.setX(activeChar.getX());
						spawn.setY(activeChar.getY());
						spawn.setZ(activeChar.getZ());
						spawn.setInstanceId(activeChar.getInstanceId());
						spawn.stopRespawn();
						final Npc npc = spawn.getNpc();
						spawn.doSpawn(true);
						npc.setTitle(activeChar.getName());
						npc.setIsRunning(false); // broadcast info
						if (sitem.getDespawnDelay() > 0) {
							npc.scheduleDespawn(sitem.getDespawnDelay() * 1000L);
						}
					}
				} catch (Exception e) {
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_CANT_FOUND));
				}
				break;
			case 1: // pet summons
				final WorldObject oldTarget = activeChar.getTarget();
				activeChar.setTarget(activeChar);
				Broadcast.toSelfAndKnownPlayers(activeChar, new MagicSkillUse(activeChar, 2046, 1, 5000, 0));
				activeChar.setTarget(oldTarget);
				activeChar.sendPacket(new SetupGauge(0, 5000));
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SUMMON_A_PET));
				activeChar.setIsCastingNow(true);

				ThreadPoolManager.getInstance().scheduleGeneral(new PetSummonFinalizer(activeChar, npcTemplate, item), 5000);
				break;
			case 2: // wyvern
				activeChar.mount(sitem.getNpcId(), item.getObjectId(), true);
				break;
			case 3: // Great Wolf
				activeChar.mount(sitem.getNpcId(), item.getObjectId(), false);
				break;
		}
	}

	static class PetSummonFeedWait implements Runnable {
		private final Player activeChar;
		private final PetInstance petSummon;

		PetSummonFeedWait(Player activeChar, PetInstance petSummon) {
			this.activeChar = activeChar;
			this.petSummon = petSummon;
		}

		@Override
		public void run() {
			try {
				if (petSummon.getCurrentFed() <= 0) {
					petSummon.unSummon(activeChar);
				} else {
					petSummon.startFeed();
				}
			} catch (Exception e) {
				log.error("", e);
			}
		}
	}

	// TODO: this should be inside skill handler
	static class PetSummonFinalizer implements Runnable {
		private final Player activeChar;
		private final Item item;
		private final NpcTemplate npcTemplate;

		PetSummonFinalizer(Player activeChar, NpcTemplate npcTemplate, Item item) {
			this.activeChar = activeChar;
			this.npcTemplate = npcTemplate;
			this.item = item;
		}

		@Override
		public void run() {
			try {
				activeChar.sendPacket(new MagicSkillLaunched(activeChar, 2046, 1));
				activeChar.setIsCastingNow(false);

				// check for summon item validity
				if (item == null || item.getOwnerId() != activeChar.getObjectId() || item.getLocation() != Item.ItemLocation.INVENTORY) {
					return;
				}

				final PetInstance petSummon = PetInstance.spawnPet(npcTemplate, activeChar, item);
				if (petSummon == null) {
					return;
				}

				petSummon.setShowSummonAnimation(true);
				petSummon.setTitle(activeChar.getName());

				if (!petSummon.isRespawned()) {
					petSummon.setCurrentHp(petSummon.getMaxHp());
					petSummon.setCurrentMp(petSummon.getMaxMp());
					petSummon.getStat().setExp(petSummon.getExpForThisLevel());
					petSummon.setCurrentFed(petSummon.getMaxFed());
				}

				petSummon.setRunning();

				if (!petSummon.isRespawned()) {
					petSummon.store();
				}

				activeChar.setPet(petSummon);

				//JIV remove - done on spawn
				//World.getInstance().storeObject(petSummon);
				petSummon.spawnMe(activeChar.getX() + 50, activeChar.getY() + 100, activeChar.getZ());
				petSummon.startFeed();
				item.setEnchantLevel(petSummon.getLevel());

				if (petSummon.getCurrentFed() <= 0) {
					ThreadPoolManager.getInstance().scheduleGeneral(new PetSummonFeedWait(activeChar, petSummon), 60000);
				} else {
					petSummon.startFeed();
				}

				petSummon.setFollowStatus(true);

				petSummon.getOwner().sendPacket(new PetItemList(petSummon));
				petSummon.broadcastStatusUpdate();
			} catch (Exception e) {
				log.error("", e);
			}
		}
	}
}
