package l2server.gameserver.model.actor.instance;

import l2server.gameserver.events.instanced.EventTeam;
import l2server.gameserver.events.instanced.EventsManager;
import l2server.gameserver.events.instanced.types.DestroyTheGolem;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.templates.chars.L2NpcTemplate;

/**
 * @author Pere
 */
public class L2EventGolemInstance extends L2MonsterInstance
{
	private EventTeam _team = null;
	private int _maxHp;

	public L2EventGolemInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void reduceCurrentHp(double i, L2Character attacker, L2Skill skill)
	{
		reduceCurrentHp(i, attacker, true, false, skill);
	}

	@Override
	public void reduceCurrentHpByDOT(double i, L2Character attacker, L2Skill skill)
	{
		reduceCurrentHp(i, attacker, true, false, skill);
	}

	@Override
	public void reduceCurrentHp(double i, L2Character attacker, boolean awake, boolean isDOT, L2Skill skill)
	{
		if (EventsManager.getInstance().isPlayerParticipant(attacker.getObjectId()) &&
				!_team.containsPlayer(attacker.getObjectId()))
		{
			getStatus().reduceHp(1, attacker, awake, isDOT, false);
		}
		else
		{
			getStatus().reduceHp(0, attacker, awake, isDOT, false);
		}
	}

	@Override
	public boolean doDie(L2Character killer)
	{
		if (killer instanceof L2PcInstance && ((L2PcInstance) killer).getEvent() instanceof DestroyTheGolem)
		{
			((DestroyTheGolem) ((L2PcInstance) killer).getEvent()).onGolemDestroyed((L2PcInstance) killer, getTeam());
		}
		return super.doDie(killer);
	}

	public EventTeam getTeam()
	{
		return _team;
	}

	public void setTeam(EventTeam team)
	{
		_team = team;
	}

	public void setMaxHp(int maxHp)
	{
		_maxHp = maxHp;
	}

	@Override
	public int getMaxHp()
	{
		return _maxHp;
	}
}
