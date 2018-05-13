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
import l2server.gameserver.GeoData;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.model.L2Spawn;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.MyTargetSelected;
import l2server.gameserver.network.serverpackets.StatusUpdate;
import l2server.gameserver.network.serverpackets.ValidateLocation;
import l2server.gameserver.templates.chars.NpcTemplate;

import java.util.ArrayList;
import java.util.List;

public class ControlTowerInstance extends Npc {
	private List<L2Spawn> guards;
	
	public ControlTowerInstance(int objectId, NpcTemplate template) {
		super(objectId, template);
		setInstanceType(InstanceType.L2ControlTowerInstance);
		setInvul(false);
	}
	
	@Override
	public boolean isAttackable() {
		// Attackable during siege by attacker only
		return getCastle() != null && getCastle().getCastleId() > 0 && getCastle().getSiege().getIsInProgress();
	}
	
	@Override
	public boolean isAutoAttackable(Creature attacker) {
		// Attackable during siege by attacker only
		return attacker != null && attacker instanceof Player && getCastle() != null && getCastle().getCastleId() > 0 &&
				getCastle().getSiege().getIsInProgress() && getCastle().getSiege().checkIsAttacker(((Player) attacker).getClan());
	}
	
	@Override
	public void onForcedAttack(Player player) {
		onAction(player);
	}
	
	@Override
	public void onAction(Player player, boolean interact) {
		if (!canTarget(player)) {
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
			if (isAutoAttackable(player) && Math.abs(player.getZ() - getZ()) < 100 && GeoData.getInstance().canSeeTarget(player, this)) {
				// Notify the Player AI with AI_INTENTION_INTERACT
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
			}
		}
		// Send a Server->Client ActionFailed to the Player in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	@Override
	public boolean doDie(Creature killer) {
		if (getCastle().getSiege().getIsInProgress()) {
			getCastle().getSiege().killedCT(this);
			
			if (guards != null && !guards.isEmpty()) {
				for (L2Spawn spawn : guards) {
					if (spawn == null) {
						continue;
					}
					try {
						spawn.stopRespawn();
						//spawn.getNpc().doDie(spawn.getNpc());
					} catch (Exception ignored) {
					}
				}
				guards.clear();
			}
		}
		return super.doDie(killer);
	}
	
	public void registerGuard(L2Spawn guard) {
		getGuards().add(guard);
	}
	
	public final List<L2Spawn> getGuards() {
		if (guards == null) {
			synchronized (this) {
				if (guards == null) {
					guards = new ArrayList<>();
				}
			}
		}
		
		return guards;
	}
}
