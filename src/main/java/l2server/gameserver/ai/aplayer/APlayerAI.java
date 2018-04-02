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

package l2server.gameserver.ai.aplayer;

import l2server.gameserver.GameApplication;
import l2server.gameserver.GeoData;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.ai.PlayerAI;
import l2server.gameserver.datatables.EnchantItemTable;
import l2server.gameserver.datatables.MapRegionTable.TeleportWhereType;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.handler.IItemHandler;
import l2server.gameserver.handler.ItemHandler;
import l2server.gameserver.model.*;
import l2server.gameserver.model.actor.Attackable;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.ApInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.templates.item.ArmorType;
import l2server.gameserver.templates.item.ItemTemplate;
import l2server.gameserver.templates.item.WeaponType;
import l2server.gameserver.templates.skills.SkillType;
import l2server.util.Point3D;
import l2server.util.Rnd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledFuture;

/**
 * @author Pere
 * This is the abstract superclass of APlayer AI
 */
public abstract class APlayerAI extends PlayerAI implements Runnable {
	private static Logger log = LoggerFactory.getLogger(GameApplication.class.getName());
	
	protected ApInstance player;
	protected long timer;
	
	//protected TIntIntHashMap hate = new TIntIntHashMap();
	
	private ScheduledFuture<?> task;
	
	public APlayerAI(Creature creature) {
		super(creature);
		player = (ApInstance) actor;
		task = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(this, 5000, 1000);
		
		// The players always run
		player.setRunning();
		
		//player.setTitle("L2 Tenkai");
		
		checkGear();
		
		for (Item item : player.getInventory().getItems()) {
			if (item == null || !item.isEquipped()) {
				continue;
			}
			
			boolean hasElement = false;
			if (item.getElementals() != null) {
				for (Elementals elem : item.getElementals()) {
					if (elem != null && elem.getValue() > 0) {
						hasElement = true;
					}
				}
			}
			
			if (hasElement) {
				continue;
			}
			
			if (!(item.getItem().getItemType() == WeaponType.FISHINGROD || item.isShadowItem() || item.isCommonItem() || item.isPvp() ||
					item.isHeroItem() || item.isTimeLimitedItem() || item.getItemId() >= 7816 && item.getItemId() <= 7831 ||
					item.getItem().getItemType() == WeaponType.NONE ||
					item.getItem().getItemGradePlain() != ItemTemplate.CRYSTAL_S && item.getItem().getItemGradePlain() != ItemTemplate.CRYSTAL_R ||
					item.getItem().getBodyPart() == ItemTemplate.SLOT_BACK || item.getItem().getBodyPart() == ItemTemplate.SLOT_R_BRACELET ||
					item.getItem().getBodyPart() == ItemTemplate.SLOT_UNDERWEAR || item.getItem().getBodyPart() == ItemTemplate.SLOT_BELT ||
					item.getItem().getBodyPart() == ItemTemplate.SLOT_NECK || (item.getItem().getBodyPart() & ItemTemplate.SLOT_R_EAR) != 0 ||
					(item.getItem().getBodyPart() & ItemTemplate.SLOT_R_FINGER) != 0 || item.getItem().getElementals() != null ||
					item.getItemType() == ArmorType.SHIELD || item.getItemType() == ArmorType.SIGIL)) {
				if (item.isWeapon()) {
					item.setElementAttr((byte) Rnd.get(6), 300);
				} else {
					for (int elem = 0; elem < 6; elem += 2) {
						item.setElementAttr((byte) (elem + Rnd.get(2)), 120);
					}
				}
			}
		}
	}
	
	protected abstract int[] getRandomGear();
	
	private void checkGear() {
		if (player.getActiveWeaponItem() != null && player.getActiveWeaponItem().getCrystalType() > ItemTemplate.CRYSTAL_D) {
			return;
		}
		
		for (int itemId : getRandomGear()) {
			Item item = player.getInventory().addItem("", itemId, 1, player, player);
			if (item != null && EnchantItemTable.isEnchantable(item)) {
				item.setEnchantLevel(10 + Rnd.get(5));
			}
		}
		
		for (Item item : player.getInventory().getItems()) {
			if (item == null) {
				continue;
			}
			
			if (item.isEquipable() && !item.isEquipped()) {
				if (item.getItem().getCrystalType() > ItemTemplate.CRYSTAL_D) {
					player.useEquippableItem(item, false);
				} else {
					player.destroyItem("Destroy", item, player, false);
				}
			}
		}
	}
	
	protected Creature decideTarget() {
		Creature target = player.getTarget() instanceof Creature ? (Creature) player.getTarget() : null;
		
		if (player.getParty() != null) {
			target = player.getParty().getTarget();
			//if (!player.isInsideRadius(target, 3000, false, false))
			//	player.teleToLocation(target.getX(), target.getY(), target.getZ());
			for (Player member : player.getParty().getPartyMembers()) {
				if (member.isDead() && member.getClassId() == 146) {
					interactWith(member);
				}
			}
		}
		
		//if (target == null || target.isDead())
		//	World.getInstance().getPlayer("lolol");
		
		if (target == null || target.getDistanceSq(player) > 2000 * 2000 || !target.isVisible()) {
			for (Creature cha : player.getKnownList().getKnownCharacters()) {
				if (!cha.isDead() && cha.isVisible() && player.isEnemy(cha)) {
					target = cha;
					break;
				}
			}
		}
		
		if (target != null && !player.isEnemy(target)) {
			target = null;
		}
		
		player.setTarget(target);
		
		return target;
	}
	
	protected void travelTo(Creature target) {
		Point3D direction = new Point3D(target.getX() - player.getX(), target.getY() - player.getY(), target.getZ() - player.getZ());
		double length = Math.sqrt((long) direction.getX() * (long) direction.getX() + (long) direction.getY() * (long) direction.getY());
		double angle = Math.acos(direction.getX() / length);
		if (direction.getY() < 0) {
			angle = Math.PI * 2 - angle;
		}
		
		int newX = player.getX() + (int) (1000 * Math.cos(angle));
		int newY = player.getY() + (int) (1000 * Math.sin(angle));
		int newZ = GeoData.getInstance().getHeight(newX, newY, player.getZ());
		//int newX = target.getX();
		//int newY = target.getY();
		//int newZ = target.getZ();
		
		double offset = 0.1;
		while (!GeoData.getInstance().canMoveFromToTarget(player.getX(), player.getY(), player.getZ(), newX, newY, newZ, 0) && offset < Math.PI) {
			newX = player.getX() + (int) (1000 * Math.cos(angle + offset));
			newY = player.getY() + (int) (1000 * Math.sin(angle + offset));
			newZ = GeoData.getInstance().getHeight(newX, newY, player.getZ() + 50);
			// This makes offset alternate direction and increment a bit at each loop
			offset += offset * -2.01;
		}
		
		//log.info(player.getX() + " " + player.getY() + " " + player.getZ() + " " + newX + " " + newY + " " + newZ + " " + offset);
		
		//setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(-14190, 123542, newZ, 0));
		setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(newX, newY, newZ, 0));
	}
	
	protected boolean interactWith(Creature target) {
		if (player.isAlly(target)) {
			if (target.isDead()) {
				// TODO: Resurrect (in healers override it)
				
				for (Player member : player.getParty().getPartyMembers()) {
					if (member.getClassId() == 146 && !member.isDead() || member.isCastingNow()) {
						return false;
					}
				}
				if (!player.isCastingNow()) {
					for (Skill skill : player.getAllSkills()) {
						if (skill.getSkillType() != SkillType.RESURRECT) {
							continue;
						}
						
						if (player.useMagic(skill, true, false)) {
							return true;
						}
					}
					
					Item scroll = player.getInventory().addItem("", 737, 1, player, player);
					IItemHandler handler = ItemHandler.getInstance().getItemHandler(scroll.getEtcItem());
					if (handler == null) {
						log.warn("No item handler registered for Herb - item ID " + scroll.getItemId() + ".");
					} else {
						player.setTarget(target);
						handler.useItem(player, scroll, false);
					}
				}
			} else {
				setIntention(CtrlIntention.AI_INTENTION_FOLLOW, target);
			}
			return true;
		}
		
		return false;
	}
	
	protected void think() {
		// If this object no longer belongs to the player, cancel the task
		if (this != actor.getAI()) {
			task.cancel(false);
			return;
		}
		
		// If the player left, cancel the task
		if (!player.isOnline()) {
			task.cancel(false);
			return;
		}
		
		if (player.getParty() != null && player.getParty().getLeader() == player) {
			player.getParty().think();
		}
		
		// If dead, make a little timer
		if (player.isDead()) {
			player.setReputation(0);
			
			// If there's some nearby ally, wait
			for (Player player : player.getKnownList().getKnownPlayers().values()) {
				if (!player.isInsideZone(Creature.ZONE_TOWN) && player.isAlly(player) && !player.isDead() &&
						player.isInsideRadius(player, 2000, true, false)) {
					return;
				}
			}
			
			if (timer == 0) {
				timer = System.currentTimeMillis() + 5000L;
			} else if (timer < System.currentTimeMillis()) {
				player.doRevive();
				player.teleToLocation(TeleportWhereType.Town);
				timer = 0;
			}
			return;
		}
		
		// Check if the player was disarmed and try to equip the weapon again
		if (player.getActiveWeaponInstance() == null && !player.isDisarmed()) {
			for (Item item : player.getInventory().getItems()) {
				if (item.isWeapon() && item.getItem().getCrystalType() > ItemTemplate.CRYSTAL_D) {
					player.useEquippableItem(item, false);
					break;
				}
			}
			
			if (player.getActiveWeaponInstance() == null) {
				checkGear();
			}
		}
		
		// Check shots
		Item item = player.getInventory().getItemByItemId(17754);
		if (item == null || item.getCount() < 1000) {
			player.getInventory().addItem("", 17754, 1000, player, player);
		}
		item = player.getInventory().getItemByItemId(19442);
		if (item == null || item.getCount() < 1000) {
			player.getInventory().addItem("", 19442, 1000, player, player);
		}
		player.checkAutoShots();
		
		// Check spirit ores
		item = player.getInventory().getItemByItemId(3031);
		if (item == null || item.getCount() < 100) {
			player.getInventory().addItem("", 3031, 100, player, player);
		}
		
		SkillTable.getInstance().getInfo(14779, 1).getEffects(getActor(), getActor());
		SkillTable.getInstance().getInfo(14780, 1).getEffects(getActor(), getActor());
		SkillTable.getInstance().getInfo(14781, 1).getEffects(getActor(), getActor());
		SkillTable.getInstance().getInfo(14782, 1).getEffects(getActor(), getActor());
		SkillTable.getInstance().getInfo(14783, 1).getEffects(getActor(), getActor());
		SkillTable.getInstance().getInfo(14784, 1).getEffects(getActor(), getActor());
		switch (((Player) getActor()).getClassId()) {
			case 139:
				SkillTable.getInstance().getInfo(14785, 1).getEffects(getActor(), getActor());
				break;
			case 140:
			case 141:
			case 142:
			case 144:
				SkillTable.getInstance().getInfo(14785, 1).getEffects(getActor(), getActor());
				break;
			case 143:
			case 145:
			case 146:
				SkillTable.getInstance().getInfo(14785, 1).getEffects(getActor(), getActor());
				break;
		}
		SkillTable.getInstance().getInfo(14788, 1).getEffects(getActor(), getActor());
		SkillTable.getInstance().getInfo(14789, 1).getEffects(getActor(), getActor());
		SkillTable.getInstance().getInfo(14790, 1).getEffects(getActor(), getActor());
		getActor().setCurrentHpMp(getActor().getMaxHp(), getActor().getMaxMp());
		
		// Artificially using the NPC heal whenever possible
		if (getIntention() == CtrlIntention.AI_INTENTION_IDLE && !player.isInCombat() &&
				(player.getPvpFlag() == 0 || player.isInsideZone(Creature.ZONE_PEACE))) {
			player.setCurrentHp(player.getMaxHp());
			player.setCurrentMp(player.getMaxMp());
			player.setCurrentCp(player.getMaxCp());
			player.broadcastUserInfo();
		}
		
		if (!player.isNoblesseBlessed()) {
			player.setTarget(player);
			Skill skill = player.getKnownSkill(1323);
			if (skill != null) {
				player.useMagic(skill, true, false);
			}
		}
		
		boolean forceEnabled = false;
		for (Abnormal e : player.getAllEffects()) {
			if (e.getSkill().getName().endsWith(player.getName() + " Force")) {
				forceEnabled = true;
				break;
			}
		}
		
		if (!forceEnabled) {
			for (Skill force : player.getAllSkills()) {
				if (force.getName().endsWith(" Force")) {
					player.useMagic(force, true, false);
				}
			}
		}

		/*if ((player.getTarget() == null
				|| (player.getTarget() instanceof Creature
				&& ((Creature)player.getTarget()).isDead()))
				&& player.getCurrentHp() < player.getMaxHp() * 0.6)
		{
			// Unstuck
			IUserCommandHandler handler = UserCommandHandler.getInstance().getUserCommandHandler(52);
			handler.useUserCommand(52, player);
			return;
		}*/
		
		// Decide a target to follow or attack
		Creature target = decideTarget();
		
		// If there's no target, go to the PvP zone
		if (target == null) {
			setIntention(CtrlIntention.AI_INTENTION_IDLE);
			if (player.isInParty()) {
				for (Player member : player.getParty().getPartyMembers()) {
					if (member != player && (player.isInsideRadius(member, 30, false, false) ||
							!player.isInsideRadius(member, 200, false, false) && player.isInsideRadius(member, 3000, false, false))) {
						setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,
								new L2CharPosition(member.getX() + Rnd.get(100) - 50, member.getY() + Rnd.get(100) - 50, member.getZ(), 0));
						return;
					}
				}
			} else {
				Player mostPvP = World.getInstance().getMostPvP(player.isInParty(), true);
				
				if (mostPvP != null && player.getPvpFlag() == 0) {
					player.teleToLocation(mostPvP.getX(), mostPvP.getY(), mostPvP.getZ());
				}
				return;
			}
		}

		/*if (player.getDistanceSq(target) > 2000 * 2000)
			travelTo(target);
		else*/
		interactWith(target);
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker) {
		if (attacker instanceof Attackable && !attacker.isCoreAIDisabled()) {
			clientStartAutoAttack();
		}
	}
	
	@Override
	public void run() {
		think();
	}
	
	// -----------------------------------------------------
	
	/**
	 * Finds the first occurrence of the abnormalType specified, if it's present.
	 *
	 * @param player       The Player to check
	 * @param abnormalType The abnormalType to check
	 * @return Returns true if exists at last one occurrence of given abnormalType
	 */
	protected boolean hasAbnormalType(Player player, String abnormalType) {
		if (player != null && !abnormalType.equals("none")) {
			for (Abnormal e : player.getAllEffects()) {
				for (String stackType : e.getStackType()) {
					if (stackType.equals(abnormalType)) {
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Finds the first occurrence of Effect if it's present, searching by it's Skill ID.
	 *
	 * @param player  The Player to check
	 * @param skillId The ID of the skill to check
	 * @return Returns true if exists at last one occurrence of given Effect
	 */
	protected boolean hasSkillEffects(Player player, int skillId) {
		if (player != null) {
			for (Abnormal e : player.getAllEffects()) {
				if (e.getSkill().getId() == skillId) {
					return true;
				}
			}
		}
		
		return false;
	}
}
