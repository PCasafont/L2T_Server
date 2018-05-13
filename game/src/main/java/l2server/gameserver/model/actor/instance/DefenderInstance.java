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
import l2server.Config;
import l2server.gameserver.ai.CreatureAI;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.ai.FortSiegeGuardAI;
import l2server.gameserver.ai.SiegeGuardAI;
import l2server.gameserver.instancemanager.CastleManager;
import l2server.gameserver.instancemanager.FortManager;
import l2server.gameserver.model.L2CharPosition;
import l2server.gameserver.model.actor.Attackable;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Playable;
import l2server.gameserver.model.actor.knownlist.DefenderKnownList;
import l2server.gameserver.model.entity.Castle;
import l2server.gameserver.model.entity.Fort;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.MyTargetSelected;
import l2server.gameserver.network.serverpackets.StatusUpdate;
import l2server.gameserver.network.serverpackets.ValidateLocation;
import l2server.gameserver.templates.chars.NpcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefenderInstance extends Attackable {
	private static Logger log = LoggerFactory.getLogger(DefenderInstance.class.getName());

	private Castle castle = null; // the castle which the instance should defend
	private Fort fort = null; // the fortress which the instance should defend
	
	public DefenderInstance(int objectId, NpcTemplate template) {
		super(objectId, template);
		setInstanceType(InstanceType.L2DefenderInstance);
	}
	
	@Override
	public DefenderKnownList getKnownList() {
		return (DefenderKnownList) super.getKnownList();
	}
	
	@Override
	public DefenderKnownList initialKnownList() {
		return new DefenderKnownList(this);
	}
	
	@Override
	protected CreatureAI initAI() {
		if (getCastle(10000) == null) {
			return new FortSiegeGuardAI(this);
		}
		return new SiegeGuardAI(this);
	}
	
	/**
	 * Return True if a siege is in progress and the Creature attacker isn't a Defender.<BR><BR>
	 *
	 * @param attacker The Creature that the L2SiegeGuardInstance try to attack
	 */
	@Override
	public boolean isAutoAttackable(Creature attacker) {
		// Attackable during siege by all except defenders
		if (!(attacker instanceof Playable)) {
			return false;
		}
		
		Player player = attacker.getActingPlayer();
		
		// Check if siege is in progress
		if (fort != null && fort.getZone().isActive() || castle != null && castle.getZone().isActive()) {
			int activeSiegeId = fort != null ? fort.getFortId() : castle != null ? castle.getCastleId() : 0;
			
			// Check if player is an enemy of this defender npc
			if (player != null &&
					(player.getSiegeState() == 2 && !player.isRegisteredOnThisSiegeField(activeSiegeId) || player.getSiegeState() == 0)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean hasRandomAnimation() {
		return false;
	}
	
	/**
	 * This method forces guard to return to home location previously set
	 */
	@Override
	public void returnHome() {
		if (getWalkSpeed() <= 0) {
			return;
		}
		if (getSpawn() == null) // just in case
		{
			return;
		}
		if (!isInsideRadius(getSpawn().getX(), getSpawn().getY(), 40, false)) {
			if (Config.DEBUG) {
				log.info(getObjectId() + ": moving home");
			}
			setisReturningToSpawnPoint(true);
			clearAggroList();
			
			if (hasAI()) {
				getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,
						new L2CharPosition(getSpawn().getX(), getSpawn().getY(), getSpawn().getZ(), 0));
			}
		}
	}
	
	@Override
	public void onSpawn() {
		super.onSpawn();
		
		fort = FortManager.getInstance().getFort(getX(), getY(), getZ());
		castle = CastleManager.getInstance().getCastle(getX(), getY(), getZ());
		if (fort == null && castle == null) {
			log.warn(
					"DefenderInstance spawned outside of Fortress or Castle Zone! NpcId: " + getNpcId() + " x=" + getX() + " y=" + getY() + " z=" +
							getZ());
		}
	}
	
	/**
	 * Custom onAction behaviour. Note that super() is not called because guards need
	 * extra check to see if a player should interact or ATTACK them when clicked.
	 */
	@Override
	public void onAction(Player player, boolean interact) {
		if (!canTarget(player)) {
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Check if the Player already target the NpcInstance
		if (this != player.getTarget()) {
			if (Config.DEBUG) {
				log.info("new target selected:" + getObjectId());
			}
			
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
			if (isAutoAttackable(player) && !isAlikeDead()) {
				if (Math.abs(player.getZ() - getZ()) < 600) // this max heigth difference might need some tweaking
				{
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
				}
			}
			if (!isAutoAttackable(player)) {
				if (!canInteract(player)) {
					// Notify the Player AI with AI_INTENTION_INTERACT
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
				}
			}
		}
		//Send a Server->Client ActionFailed to the Player in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	@Override
	public void addDamageHate(Creature attacker, int damage, int aggro) {
		if (attacker == null) {
			return;
		}
		
		if (!(attacker instanceof DefenderInstance)) {
			if (damage == 0 && aggro <= 1 && attacker instanceof Playable) {
				Player player = attacker.getActingPlayer();
				// Check if siege is in progress
				if (fort != null && fort.getZone().isActive() || castle != null && castle.getZone().isActive()) {
					int activeSiegeId = fort != null ? fort.getFortId() : castle != null ? castle.getCastleId() : 0;
					if (player != null && (player.getSiegeState() == 2 && player.isRegisteredOnThisSiegeField(activeSiegeId))) {
						return;
					}
				}
			}
			super.addDamageHate(attacker, damage, aggro);
		}
	}
}
