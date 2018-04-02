package l2server.gameserver.model.actor.instance;

import l2server.gameserver.events.instanced.EventTeam;
import l2server.gameserver.events.instanced.EventsManager;
import l2server.gameserver.events.instanced.types.DestroyTheGolem;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.templates.chars.NpcTemplate;

/**
 * @author Pere
 */
public class EventGolemInstance extends MonsterInstance {
	private EventTeam team = null;
	private int maxHp;
	
	public EventGolemInstance(int objectId, NpcTemplate template) {
		super(objectId, template);
	}
	
	@Override
	public void reduceCurrentHp(double i, Creature attacker, Skill skill) {
		reduceCurrentHp(i, attacker, true, false, skill);
	}
	
	@Override
	public void reduceCurrentHpByDOT(double i, Creature attacker, Skill skill) {
		reduceCurrentHp(i, attacker, true, false, skill);
	}
	
	@Override
	public void reduceCurrentHp(double i, Creature attacker, boolean awake, boolean isDOT, Skill skill) {
		if (EventsManager.getInstance().isPlayerParticipant(attacker.getObjectId()) && !team.containsPlayer(attacker.getObjectId())) {
			getStatus().reduceHp(1, attacker, awake, isDOT, false);
		} else {
			getStatus().reduceHp(0, attacker, awake, isDOT, false);
		}
	}
	
	@Override
	public boolean doDie(Creature killer) {
		if (killer instanceof Player && ((Player) killer).getEvent() instanceof DestroyTheGolem) {
			((DestroyTheGolem) ((Player) killer).getEvent()).onGolemDestroyed((Player) killer, getTeam());
		}
		return super.doDie(killer);
	}
	
	public EventTeam getTeam() {
		return team;
	}
	
	public void setTeam(EventTeam team) {
		this.team = team;
	}
	
	public void setMaxHp(int maxHp) {
		this.maxHp = maxHp;
	}
	
	@Override
	public int getMaxHp() {
		return maxHp;
	}
}
