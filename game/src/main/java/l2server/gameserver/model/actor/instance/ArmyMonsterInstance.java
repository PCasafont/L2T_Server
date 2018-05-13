package l2server.gameserver.model.actor.instance;

import l2server.gameserver.model.InstanceType;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.events.MonsterInvasion;
import l2server.gameserver.model.L2CharPosition;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.network.clientpackets.Say2;
import l2server.gameserver.network.serverpackets.CreatureSay;
import l2server.gameserver.network.serverpackets.ValidateLocation;
import l2server.gameserver.templates.chars.NpcTemplate;

/**
 * @author Pere
 */
public final class ArmyMonsterInstance extends MonsterInstance {
	private MoveTask mt2;
	private int type;
	private int movesDone = 0;
	private boolean isDoingAMove = false;
	private boolean isTheLastMob = false;
	
	public ArmyMonsterInstance(int objectId, NpcTemplate template) {
		super(objectId, template);
		type = getNpcId() % 10;
		setInvul(true);
		setTitle(template.Title);
	}
	
	public void move(int x, int y, int z) {
		getSpawn().setX(x);
		getSpawn().setY(y);
		getSpawn().setZ(z);
		
		if (isDoingAMove) {
			mt2 = new MoveTask(x, y, z);
		} else {
			isDoingAMove = true;
			MoveTask mt = new MoveTask(x, y, z);
			ThreadPoolManager.getInstance().scheduleGeneral(mt, 1000L);
		}
	}
	
	public void stopMove() {
		isDoingAMove = false;
		movesDone++;
		if (movesDone == 1) {
			if (type == 0) {
				shout("TO YOUR POSITIONS!");
				CommanderPatienceTask pt = new CommanderPatienceTask();
				ThreadPoolManager.getInstance().scheduleGeneral(pt, 200000L);
			}
			ThreadPoolManager.getInstance().scheduleGeneral(mt2, 1000L);
		} else if (movesDone == 2 && isTheLastMob) {
			MonsterInvasion.getInstance().startInvasionFight();
		}
		broadcastPacket(new ValidateLocation(this));
	}
	
	class MoveTask implements Runnable {
		protected int x;
		protected int y;
		protected int z;
		
		protected MoveTask(int x, int y, int z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}
		
		@Override
		public void run() {
			if (getAI().getIntention() != CtrlIntention.AI_INTENTION_MOVE_TO && (Math.abs(getX() - x) > 5 || Math.abs(getY() - y) > 5)) {
				getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(x, y, z, 0));
			}
			if (Math.abs(getX() - x) > 5 || Math.abs(getY() - y) > 5) {
				ThreadPoolManager.getInstance().scheduleGeneral(this, 1000L);
			} else {
				stopMove();
			}
		}
	}
	
	class CommanderPatienceTask implements Runnable {
		@Override
		public void run() {
			MonsterInvasion.getInstance().startInvasionFight();
		}
	}
	
	public void shout(String message) {
		CreatureSay cs;
		for (Player player : getKnownList().getKnownPlayers().values()) {
			cs = new CreatureSay(getObjectId(), Say2.BATTLEFIELD, getName(), message);
			player.sendPacket(cs);
		}
	}
	
	private void whisp(Player player, String message) {
		player.sendPacket(new CreatureSay(getObjectId(), Say2.TELL, getName(), message));
	}
	
	public void setIsTheLastMob(boolean last) {
		isTheLastMob = last;
	}
	
	@Override
	public boolean doDie(Creature killer) {
		if (!super.doDie(killer)) {
			return false;
		}
		
		if (type == 0) {
			shout("WE'LL BE BACK!");
			MonsterInvasion.getInstance().onCommanderDeath();
		}
		
		return true;
	}
	
	@Override
	public void addDamageHate(Creature attacker, int damage, int aggro) {
		if (!isInvul(attacker)) {
			super.addDamageHate(attacker, damage, aggro);
		} else if (attacker instanceof Player) {
			SkillTable.getInstance().getInfo(1069, 1).getEffects(attacker, attacker);
			whisp((Player) attacker, "Not now! Don't you see that we are marching?");
		}
	}
	
	@Override
	public boolean isAggressive() {
		if (type == 0) {
			return false;
		}
		
		return !isInvul();
	}
	
	@Override
	public int getAggroRange() {
		if (isInvul()) {
			return 0;
		} else {
			return 2000;
		}
	}
}
