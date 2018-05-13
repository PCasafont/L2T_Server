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
import l2server.gameserver.instancemanager.RaidBossPointsManager;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Summon;
import l2server.gameserver.model.olympiad.HeroesManager;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.chars.NpcTemplate;
import l2server.util.Rnd;

/**
 * This class manages all Grand Bosses.
 *
 * @version $Revision: 1.0.0.0 $ $Date: 2006/06/16 $
 */
public final class GrandBossInstance extends MonsterInstance {
	private static final int BOSS_MAINTENANCE_INTERVAL = 10000;
	private boolean useRaidCurse = true;
	
	/**
	 * Constructor for GrandBossInstance. This represent all grandbosses.
	 *
	 * @param objectId ID of the instance
	 * @param template NpcTemplate of the instance
	 */
	public GrandBossInstance(int objectId, NpcTemplate template) {
		super(objectId, template);
		setInstanceType(InstanceType.L2GrandBossInstance);
		setIsRaid(true);
	}
	
	@Override
	protected int getMaintenanceInterval() {
		return BOSS_MAINTENANCE_INTERVAL;
	}
	
	@Override
	public void onSpawn() {
		super.onSpawn();
	}
	
	/**
	 * @see MonsterInstance#doDie(Creature)
	 */
	@Override
	public boolean doDie(Creature killer) {
		if (!super.doDie(killer)) {
			return false;
		}
		Player player = null;
		
		if (killer instanceof Player) {
			player = (Player) killer;
		} else if (killer instanceof Summon) {
			player = ((Summon) killer).getOwner();
		}
		
		if (player != null) {
			broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.RAID_WAS_SUCCESSFUL));
			if (player.getParty() != null) {
				for (Player member : player.getParty().getPartyMembers()) {
					RaidBossPointsManager.getInstance().addPoints(member, getNpcId(), getLevel() / 2 + Rnd.get(-5, 5));
					if (member.isNoble()) {
						HeroesManager.getInstance().setRBkilled(member.getObjectId(), getNpcId());
					}
				}
			} else {
				RaidBossPointsManager.getInstance().addPoints(player, getNpcId(), getLevel() / 2 + Rnd.get(-5, 5));
				if (player.isNoble()) {
					HeroesManager.getInstance().setRBkilled(player.getObjectId(), getNpcId());
				}
			}
		}
		return true;
	}
	
	@Override
	public float getVitalityPoints(int damage) {
		return -super.getVitalityPoints(damage) / 100;
	}
	
	@Override
	public boolean useVitalityRate() {
		return false;
	}
	
	public void setUseRaidCurse(boolean val) {
		useRaidCurse = val;
	}
	
	/* (non-Javadoc)
	 * @see l2server.gameserver.model.actor.Creature#giveRaidCurse()
	 */
	@Override
	public boolean giveRaidCurse() {
		return useRaidCurse;
	}
}
