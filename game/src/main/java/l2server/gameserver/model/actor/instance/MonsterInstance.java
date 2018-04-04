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

import l2server.Config;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.model.CoreMessage;
import l2server.gameserver.model.actor.Attackable;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.knownlist.MonsterKnownList;
import l2server.gameserver.network.clientpackets.Say2;
import l2server.gameserver.network.serverpackets.CreatureSay;
import l2server.gameserver.templates.chars.NpcTemplate;
import l2server.gameserver.util.MinionList;
import l2server.util.Rnd;

import java.util.Map;
import java.util.concurrent.ScheduledFuture;

/**
 * This class manages all Monsters.
 * <p>
 * MonsterInstance :<BR><BR>
 * <li>L2MinionInstance</li>
 * <li>RaidBossInstance </li>
 * <li>GrandBossInstance </li>
 *
 * @version $Revision: 1.20.4.6 $ $Date: 2005/04/06 16:13:39 $
 */
public class MonsterInstance extends Attackable {
	//
	
	private boolean enableMinions = true;
	
	private MonsterInstance master = null;
	private MinionList minionList = null;
	
	protected ScheduledFuture<?> maintenanceTask = null;
	
	private static final int MONSTER_MAINTENANCE_INTERVAL = 1000;
	
	/**
	 * Constructor of MonsterInstance (use Creature and NpcInstance constructor).<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Call the Creature constructor to set the template of the MonsterInstance (copy skills from template to object and link calculators to NPC_STD_CALCULATOR) </li>
	 * <li>Set the name of the MonsterInstance</li>
	 * <li>Create a RandomAnimation Task that will be launched after the calculated delay if the server allow it </li><BR><BR>
	 *
	 * @param objectId Identifier of the object to initialized
	 */
	public MonsterInstance(int objectId, NpcTemplate template) {
		super(objectId, template);
		setInstanceType(InstanceType.L2MonsterInstance);
		setAutoAttackable(true);
	}
	
	@Override
	public final MonsterKnownList getKnownList() {
		return (MonsterKnownList) super.getKnownList();
	}
	
	@Override
	public void initKnownList() {
		setKnownList(new MonsterKnownList(this));
	}
	
	/**
	 * Return True if the attacker is not another MonsterInstance.<BR><BR>
	 */
	@Override
	public boolean isAutoAttackable(Creature attacker) {
		return super.isAutoAttackable(attacker) && getNpcId() != 50101;
	}
	
	/**
	 * Return True if the MonsterInstance is Agressive (aggroRange > 0).<BR><BR>
	 */
	@Override
	public boolean isAggressive() {
		return getTemplate().Aggressive && !isRaid();
	}
	
	@Override
	public void onSpawn() {
		if (!isTeleporting()) {
			if (getLeader() != null) {
				setIsRaidMinion(getLeader().isRaid());
				getLeader().getMinionList().onMinionSpawn(this);
			}
			
			// delete spawned minions before dynamic minions spawned by script
			if (hasMinions()) {
				getMinionList().onMasterSpawn();
			}
			
			startMaintenanceTask();
		}
		
		// dynamic script-based minions spawned here, after all preparations.
		super.onSpawn();
	}
	
	@Override
	public void onTeleported() {
		super.onTeleported();
		
		if (hasMinions()) {
			getMinionList().onMasterTeleported();
		}
	}
	
	protected int getMaintenanceInterval() {
		return MONSTER_MAINTENANCE_INTERVAL;
	}
	
	/**
	 * Spawn all minions at a regular interval
	 */
	protected void startMaintenanceTask() {
		// maintenance task now used only for minions spawn
		if (getTemplate().getMinionData() == null && getTemplate().getRandomMinionData() == null) {
			return;
		}
		
		if (maintenanceTask == null) {
			maintenanceTask = ThreadPoolManager.getInstance().scheduleGeneral(() -> {
				if (enableMinions) {
					getMinionList().spawnMinions();
				}
			}, getMaintenanceInterval() + Rnd.get(1000));
		}
	}
	
	@Override
	public boolean doDie(Creature killer) {
		if (!super.doDie(killer)) {
			return false;
		}
		
		Map<Integer, Player> knownPlayers = getKnownList().getKnownPlayers();
		
		if (killer instanceof Player) {
			if (Config.isServer(Config.TENKAI) && Rnd.get(30) == 0 && knownPlayers.size() > 0 && !(this instanceof RaidBossInstance) &&
					!(this instanceof GrandBossInstance) && !(this instanceof ChessPieceInstance)) {
				CoreMessage cm = new CoreMessage(47001 + Rnd.get(90));
				cm.addString(killer.getName());
				CreatureSay cs = new CreatureSay(getObjectId(), Say2.ALL_NOT_RECORDED, getName(), cm.renderMsg("en"));
				broadcastPacket(cs);
			}
		}
		
		if (maintenanceTask != null) {
			maintenanceTask.cancel(false); // doesn't do it?
			maintenanceTask = null;
		}
		
		return true;
	}
	
	@Override
	public void deleteMe() {
		if (maintenanceTask != null) {
			maintenanceTask.cancel(false);
			maintenanceTask = null;
		}
		
		if (hasMinions()) {
			getMinionList().onMasterDie(true);
		}
		
		if (getLeader() != null) {
			getLeader().getMinionList().onMinionDie(this, 0);
		}
		
		super.deleteMe();
	}
	
	@Override
	public MonsterInstance getLeader() {
		return master;
	}
	
	public void setLeader(MonsterInstance leader) {
		master = leader;
	}
	
	public void enableMinions(boolean b) {
		enableMinions = b;
	}
	
	public boolean hasMinions() {
		return minionList != null;
	}
	
	public MinionList getMinionList() {
		if (minionList == null) {
			minionList = new MinionList(this);
		}
		
		return minionList;
	}
	
	@Override
	public int getMaxMp() {
		/*
        if (getTemplate().isMiniRaid())
			return getStat().getMaxMp() * 10;
		 */
		
		return getStat().getMaxMp();
	}
	
	@Override
	public int getMaxHp() {
        /*
		if (getTemplate().isMiniRaid())
			return getStat().getMaxMp() * 10;
		 */
		
		return getStat().getMaxHp();
	}
}
