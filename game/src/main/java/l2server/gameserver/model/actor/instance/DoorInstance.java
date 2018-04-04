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

import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.ai.CreatureAI;
import l2server.gameserver.ai.DoorAI;
import l2server.gameserver.datatables.DoorTable;
import l2server.gameserver.instancemanager.CastleManager;
import l2server.gameserver.instancemanager.FortManager;
import l2server.gameserver.instancemanager.InstanceManager;
import l2server.gameserver.model.Item;
import l2server.gameserver.model.L2Clan;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Playable;
import l2server.gameserver.model.actor.knownlist.DoorKnownList;
import l2server.gameserver.model.actor.stat.DoorStat;
import l2server.gameserver.model.actor.status.DoorStatus;
import l2server.gameserver.model.entity.Castle;
import l2server.gameserver.model.entity.ClanHall;
import l2server.gameserver.model.entity.Fort;
import l2server.gameserver.model.entity.Instance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.DoorStatusUpdate;
import l2server.gameserver.network.serverpackets.EventTrigger;
import l2server.gameserver.network.serverpackets.StaticObject;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.StatsSet;
import l2server.gameserver.templates.chars.DoorTemplate;
import l2server.gameserver.templates.item.WeaponTemplate;
import l2server.util.Rnd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * This class ...
 *
 * @version $Revision: 1.3.2.2.2.5 $ $Date: 2005/03/27 15:29:32 $
 */
public class DoorInstance extends Creature {
	private static final byte OPEN_BY_CLICK = 1;
	private static final byte OPEN_BY_TIME = 2;
	private static final byte OPEN_BY_ITEM = 4;
	private static final byte OPEN_BY_SKILL = 8;
	private static final byte OPEN_BY_CYCLE = 16;
	
	/**
	 * The castle index in the array of L2Castle this NpcInstance belongs to
	 */
	private int castleIndex = -2;
	/**
	 * The fort index in the array of L2Fort this NpcInstance belongs to
	 */
	private int fortIndex = -2;
	private ClanHall clanHall;
	private boolean open = false;
	private boolean isAttackableDoor = false;
	private boolean isTargetable;
	private boolean checkCollision;
	private int openType = 0;
	private int meshindex = 1;
	private int level = 0;
	private int closeTime = -1;
	private int openTime = -1;
	private int randomTime = -1;
	// used for autoclose on open
	private Future<?> autoCloseTask;
	
	public DoorInstance(int objectId, DoorTemplate template, StatsSet data) {
		super(objectId, template);
		setInstanceType(InstanceType.L2DoorInstance);
		setIsInvul(false);
		isTargetable = data.getInteger("targetable", 1) == 1;
		if (getGroupName() != null) {
			DoorTable.addDoorGroup(getGroupName(), getDoorId());
		}
		if (data.getString("default_status", "close").equals("open")) {
			open = true;
		}
		closeTime = data.getInteger("close_time", -1);
		level = data.getInteger("level", 0);
		openType = data.getInteger("open_method", 0);
		checkCollision = data.getInteger("check_collision", 1) == 1;
		if (isOpenableByTime()) {
			closeTime = data.getInteger("open_time");
			randomTime = data.getInteger("random_time", -1);
			startTimerOpen();
		}
	}
	
	@Override
	protected CreatureAI initAI() {
		return new DoorAI(this);
	}
	
	private void startTimerOpen() {
		int delay = open ? openTime : closeTime;
		if (randomTime > 0) {
			delay += Rnd.get(randomTime);
		}
		ThreadPoolManager.getInstance().scheduleGeneral(new TimerOpen(), delay * 1000);
	}
	
	@Override
	public final DoorKnownList getKnownList() {
		return (DoorKnownList) super.getKnownList();
	}
	
	@Override
	public void initKnownList() {
		setKnownList(new DoorKnownList(this));
	}
	
	@Override
	public final DoorStat getStat() {
		return (DoorStat) super.getStat();
	}
	
	@Override
	public DoorTemplate getTemplate() {
		return (DoorTemplate) super.getTemplate();
	}
	
	@Override
	public void initCharStat() {
		setStat(new DoorStat(this));
	}
	
	@Override
	public final DoorStatus getStatus() {
		return (DoorStatus) super.getStatus();
	}
	
	@Override
	public void initCharStatus() {
		setStatus(new DoorStatus(this));
	}
	
	public final boolean isOpenableBySkill() {
		return (openType & OPEN_BY_SKILL) != 0;
	}
	
	public final boolean isOpenableByItem() {
		return (openType & OPEN_BY_ITEM) != 0;
	}
	
	public final boolean isOpenableByClick() {
		return (openType & OPEN_BY_CLICK) != 0;
	}
	
	public final boolean isOpenableByTime() {
		return (openType & OPEN_BY_TIME) != 0;
	}
	
	public final boolean isOpenableByCycle() {
		return (openType & OPEN_BY_CYCLE) != 0;
	}
	
	@Override
	public final int getLevel() {
		return level;
	}
	
	/**
	 * @return Returns the doorId.
	 */
	public int getDoorId() {
		return getTemplate().doorId;
	}
	
	/**
	 * @return Returns the open.
	 */
	public boolean getOpen() {
		return open;
	}
	
	/**
	 * @param open The open to set.
	 */
	private void setOpen(boolean open) {
		this.open = open;
		if (getChildId() > 0) {
			getSiblingDoor(getChildId()).notifyChildEvent(open);
		}
	}
	
	public boolean getIsAttackableDoor() {
		return isAttackableDoor;
	}
	
	public boolean getIsShowHp() {
		return getTemplate().showHp;
	}
	
	public void setIsAttackableDoor(boolean val) {
		isAttackableDoor = val;
	}
	
	public int getDamage() {
		int dmg = 6 - (int) Math.ceil(getCurrentHp() / getMaxHp() * 6);
		if (dmg > 6) {
			return 6;
		}
		if (dmg < 0) {
			return 0;
		}
		return dmg;
	}
	
	public final Castle getCastle() {
		if (castleIndex < 0) {
			castleIndex = CastleManager.getInstance().getCastleIndex(this);
		}
		if (castleIndex < 0) {
			return null;
		}
		return CastleManager.getInstance().getCastles().get(castleIndex);
	}
	
	public final Fort getFort() {
		if (fortIndex < 0) {
			fortIndex = FortManager.getInstance().getFortIndex(this);
		}
		if (fortIndex < 0) {
			return null;
		}
		return FortManager.getInstance().getForts().get(fortIndex);
	}
	
	public void setClanHall(ClanHall clanhall) {
		this.clanHall = clanhall;
	}
	
	public ClanHall getClanHall() {
		return clanHall;
	}
	
	public boolean isEnemy() {
		if (getCastle() != null && getCastle().getCastleId() > 0 && getCastle().getZone().isActive() && getIsShowHp()) {
			return true;
		}
		return getFort() != null && getFort().getFortId() > 0 && getFort().getZone().isActive() && getIsShowHp();
	}
	
	@Override
	public boolean isAutoAttackable(Creature attacker) {
		// Doors can`t be attacked by NPCs
		if (!(attacker instanceof Playable)) {
			return false;
		}
		
		if (getIsAttackableDoor()) {
			return true;
		}
		
		if (!getIsShowHp()) {
			return false;
		}
		
		// Attackable  only during siege by everyone (not owner)
		boolean isCastle = getCastle() != null && getCastle().getCastleId() > 0 && getCastle().getZone().isActive();
		boolean isFort = getFort() != null && getFort().getFortId() > 0 && getFort().getZone().isActive();
		int activeSiegeId = getFort() != null ? getFort().getFortId() : getCastle() != null ? getCastle().getCastleId() : 0;
		Player actingPlayer = attacker.getActingPlayer();
		
		if (isFort) {
			L2Clan clan = actingPlayer.getClan();
			if (clan != null && clan == getFort().getOwnerClan()) {
				return false;
			}
		} else if (isCastle) {
			L2Clan clan = actingPlayer.getClan();
			if (clan != null && clan.getClanId() == getCastle().getOwnerId()) {
				return false;
			}
		}
		return isCastle || isFort;
	}
	
	public boolean isAttackable(Creature attacker) {
		return isAutoAttackable(attacker);
	}
	
	@Override
	public void updateAbnormalEffect() {
	}
	
	/**
	 * Return null.<BR><BR>
	 */
	@Override
	public Item getActiveWeaponInstance() {
		return null;
	}
	
	@Override
	public WeaponTemplate getActiveWeaponItem() {
		return null;
	}
	
	@Override
	public Item getSecondaryWeaponInstance() {
		return null;
	}
	
	@Override
	public WeaponTemplate getSecondaryWeaponItem() {
		return null;
	}
	
	@Override
	public void broadcastStatusUpdate() {
		Collection<Player> knownPlayers = getKnownList().getKnownPlayers().values();
		if (knownPlayers == null || knownPlayers.isEmpty()) {
			return;
		}
		
		StaticObject su = new StaticObject(this, false);
		DoorStatusUpdate dsu = new DoorStatusUpdate(this);
		EventTrigger oe = null;
		if (getEmitter() > 0) {
			oe = new EventTrigger(this, getOpen());
		}
		for (Player player : knownPlayers) {
			if (player == null) {
				continue;
			}
			
			if (player.isGM()) {
				su = new StaticObject(this, true);
			}
			
			player.sendPacket(su);
			player.sendPacket(dsu);
			if (oe != null) {
				player.sendPacket(oe);
			}
		}
	}
	
	public final void openMe() {
		if (getGroupName() != null) {
			manageGroupOpen(true, getGroupName());
			return;
		}
		setOpen(true);
		broadcastStatusUpdate();
		startAutoCloseTask();
	}
	
	public final void closeMe() {
		//remove close task
		Future<?> oldTask = autoCloseTask;
		if (oldTask != null) {
			autoCloseTask = null;
			oldTask.cancel(false);
		}
		if (getGroupName() != null) {
			manageGroupOpen(false, getGroupName());
			return;
		}
		setOpen(false);
		broadcastStatusUpdate();
	}
	
	private void manageGroupOpen(boolean open, String groupName) {
		Set<Integer> set = DoorTable.getDoorsByGroup(groupName);
		DoorInstance first = null;
		for (Integer id : set) {
			DoorInstance door = getSiblingDoor(id);
			if (first == null) {
				first = door;
			}
			
			if (door.getOpen() != open) {
				door.setOpen(open);
				door.broadcastStatusUpdate();
			}
		}
		if (open) {
			first.startAutoCloseTask(); //only one from group
		}
	}
	
	/**
	 * Door notify child about open state change
	 *
	 * @param open true if opened
	 */
	private void notifyChildEvent(boolean open) {
		byte openThis = open ? getTemplate().masterDoorOpen : getTemplate().masterDoorClose;
		
		if (openThis == 0) {
		} else if (openThis == 1) {
			openMe();
		} else if (openThis == -1) {
			closeMe();
		}
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + getTemplate().doorId + "](" + getObjectId() + ")";
	}
	
	public String getDoorName() {
		return getTemplate().name;
	}
	
	public int getX(int i) {
		return getTemplate().nodeX[i];
	}
	
	public int getY(int i) {
		return getTemplate().nodeY[i];
	}
	
	public int getZMin() {
		return getTemplate().nodeZ;
	}
	
	public int getZMax() {
		return getTemplate().nodeZ + getTemplate().height;
	}
	
	public Collection<DefenderInstance> getKnownDefenders() {
		ArrayList<DefenderInstance> result = new ArrayList<>();
		
		Collection<WorldObject> objs = getKnownList().getKnownObjects().values();
		for (WorldObject obj : objs) {
			if (obj instanceof DefenderInstance) {
				result.add((DefenderInstance) obj);
			}
		}
		return result;
	}
	
	public void setMeshIndex(int mesh) {
		meshindex = mesh;
	}
	
	public int getMeshIndex() {
		return meshindex;
	}
	
	public int getEmitter() {
		return getTemplate().emmiter;
	}
	
	public boolean isWall() {
		return getTemplate().isWall;
	}
	
	public String getGroupName() {
		return getTemplate().groupName;
	}
	
	public int getChildId() {
		return getTemplate().childDoorId;
	}
	
	@Override
	public void reduceCurrentHp(double damage, Creature attacker, boolean awake, boolean isDOT, Skill skill) {
		if (isWall() && !(attacker instanceof SiegeSummonInstance)) {
			return;
		}
		
		super.reduceCurrentHp(damage, attacker, awake, isDOT, skill);
	}
	
	@Override
	public void reduceCurrentHpByDOT(double i, Creature attacker, Skill skill) {
		// doors can't be damaged by DOTs
	}
	
	@Override
	public boolean doDie(Creature killer) {
		if (!super.doDie(killer)) {
			return false;
		}
		
		boolean isFort = getFort() != null && getFort().getFortId() > 0 && getFort().getSiege().getIsInProgress();
		boolean isCastle = getCastle() != null && getCastle().getCastleId() > 0 && getCastle().getSiege().getIsInProgress();
		
		if (isFort || isCastle) {
			broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.CASTLE_GATE_BROKEN_DOWN));
		}
		return true;
	}
	
	@Override
	public void sendInfo(Player activeChar) {
		if (getEmitter() > 0) {
			activeChar.sendPacket(new EventTrigger(this, getOpen()));
		}
		
		activeChar.sendPacket(new StaticObject(this, activeChar.isGM()));
	}
	
	public void setTargetable(boolean b) {
		isTargetable = b;
		broadcastStatusUpdate();
	}
	
	public boolean isTargetable() {
		return isTargetable;
	}
	
	public boolean checkCollision() {
		return checkCollision;
	}
	
	/**
	 * All doors are stored at DoorTable except instance doors
	 *
	 */
	private DoorInstance getSiblingDoor(int doorId) {
		if (getInstanceId() == 0) {
			return DoorTable.getInstance().getDoor(doorId);
		} else {
			Instance inst = InstanceManager.getInstance().getInstance(getInstanceId());
			if (inst != null) {
				return inst.getDoor(doorId);
			} else {
				return null; // 2 late
			}
		}
	}
	
	private void startAutoCloseTask() {
		if (closeTime < 0 || isOpenableByTime()) {
			return;
		}
		Future<?> oldTask = autoCloseTask;
		if (oldTask != null) {
			autoCloseTask = null;
			oldTask.cancel(false);
		}
		autoCloseTask = ThreadPoolManager.getInstance().scheduleGeneral(new AutoClose(), closeTime * 1000);
	}
	
	private class AutoClose implements Runnable {
		@Override
		public void run() {
			if (getOpen()) {
				closeMe();
			}
		}
	}
	
	private class TimerOpen implements Runnable {
		@Override
		public void run() {
			boolean open = getOpen();
			if (open) {
				closeMe();
			} else {
				openMe();
			}
			
			//Logozo.info("Door "+DoorInstance.this+ " switched state "+open);
			int delay = open ? closeTime : openTime;
			if (randomTime > 0) {
				delay += Rnd.get(randomTime);
			}
			ThreadPoolManager.getInstance().scheduleGeneral(this, delay * 1000);
		}
	}
}
