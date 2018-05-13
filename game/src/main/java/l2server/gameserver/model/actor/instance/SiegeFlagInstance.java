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

package l2server.gameserver.model.actor.instance;

import l2server.gameserver.model.InstanceType;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.instancemanager.CastleSiegeManager;
import l2server.gameserver.instancemanager.FortSiegeManager;
import l2server.gameserver.model.L2Clan;
import l2server.gameserver.model.L2SiegeClan;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.status.SiegeFlagStatus;
import l2server.gameserver.model.entity.Siegable;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.*;
import l2server.gameserver.templates.chars.NpcTemplate;

public class SiegeFlagInstance extends Npc {
	private L2Clan clan;
	private Player player;
	private Siegable siege;
	private final boolean isAdvanced;
	private boolean canTalk;
	
	public SiegeFlagInstance(Player player, int objectId, NpcTemplate template, boolean advanced, boolean outPost) {
		super(objectId, template);
		setInstanceType(InstanceType.L2SiegeFlagInstance);
		
		clan = player.getClan();
		this.player = player;
		canTalk = true;
		siege = CastleSiegeManager.getInstance().getSiege(player.getX(), player.getY(), player.getZ());
		if (siege == null) {
			siege = FortSiegeManager.getInstance().getSiege(player.getX(), player.getY(), player.getZ());
		}
		if (clan == null || siege == null) {
			throw new NullPointerException(getClass().getSimpleName() + ": Initialization failed.");
		} else {
			L2SiegeClan sc = siege.getAttackerClan(clan);
			if (sc == null) {
				throw new NullPointerException(getClass().getSimpleName() + ": Cannot find siege clan.");
			} else {
				sc.addFlag(this);
			}
		}
		isAdvanced = advanced;
		getStatus();
		setIsInvul(false);
	}
	
	/**
	 * Use SiegeFlagInstance(Player, int, NpcTemplate, boolean) instead
	 */
	@Deprecated
	public SiegeFlagInstance(Player player, int objectId, NpcTemplate template) {
		super(objectId, template);
		isAdvanced = false;
	}
	
	@Override
	public boolean isAttackable() {
		return !isInvul();
	}
	
	@Override
	public boolean isAutoAttackable(Creature attacker) {
		return !isInvul(attacker);
	}
	
	@Override
	public boolean doDie(Creature killer) {
		if (!super.doDie(killer)) {
			return false;
		}
		if (siege != null && clan != null) {
			L2SiegeClan sc = siege.getAttackerClan(clan);
			if (sc != null) {
				sc.removeFlag(this);
			}
		}
		
		return true;
	}
	
	@Override
	public void onForcedAttack(Player player) {
		onAction(player);
	}
	
	@Override
	public void onAction(Player player, boolean interact) {
		if (player == null || !canTarget(player)) {
			return;
		}
		
		// Check if the Player already target the NpcInstance
		if (this != player.getTarget()) {
			// Set the target of the Player player
			player.setTarget(this);
			
			// Send a Server->Client packet MyTargetSelected to the Player player
			MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel() - getLevel());
			player.sendPacket(my);
			
			// Send a Server->Client packet StatusUpdate of the NpcInstance to the Player to update its HP bar
			StatusUpdate su = new StatusUpdate(this);
			su.addAttribute(StatusUpdate.CUR_HP, (int) getStatus().getCurrentHp());
			su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
			player.sendPacket(su);
			
			// Send a Server->Client packet ValidateLocation to correct the NpcInstance position and heading on the client
			player.sendPacket(new ValidateLocation(this));
		} else if (interact) {
			if (isAutoAttackable(player) && Math.abs(player.getZ() - getZ()) < 100) {
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
			} else {
				// Send a Server->Client ActionFailed to the Player in order to avoid that the client wait another packet
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
		}
	}
	
	public boolean isAdvancedHeadquarter() {
		return isAdvanced;
	}
	
	@Override
	public SiegeFlagStatus getStatus() {
		return (SiegeFlagStatus) super.getStatus();
	}
	
	@Override
	public void initCharStatus() {
		setStatus(new SiegeFlagStatus(this));
	}
	
	@Override
	public void reduceCurrentHp(double damage, Creature attacker, Skill skill) {
		super.reduceCurrentHp(damage, attacker, skill);
		if (canTalk()) {
			if (getCastle() != null && getCastle().getSiege().getIsInProgress()) {
				if (clan != null) {
					// send warning to owners of headquarters that theirs base is under attack
					clan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.BASE_UNDER_ATTACK));
					setCanTalk(false);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleTalkTask(), 20000);
				}
			} else if (getFort() != null && getFort().getSiege().getIsInProgress()) {
				if (clan != null) {
					// send warning to owners of headquarters that theirs base is under attack
					clan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.BASE_UNDER_ATTACK));
					setCanTalk(false);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleTalkTask(), 20000);
				}
			}
		}
	}
	
	private class ScheduleTalkTask implements Runnable {
		
		public ScheduleTalkTask() {
		}
		
		@Override
		public void run() {
			setCanTalk(true);
		}
	}
	
	void setCanTalk(boolean val) {
		canTalk = val;
	}
	
	private boolean canTalk() {
		return canTalk;
	}
}
