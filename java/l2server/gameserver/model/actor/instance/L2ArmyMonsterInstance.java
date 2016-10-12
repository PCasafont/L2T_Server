package l2server.gameserver.model.actor.instance;

import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.events.MonsterInvasion;
import l2server.gameserver.model.L2CharPosition;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.network.clientpackets.Say2;
import l2server.gameserver.network.serverpackets.CreatureSay;
import l2server.gameserver.network.serverpackets.ValidateLocation;
import l2server.gameserver.templates.chars.L2NpcTemplate;

/**
 * @author Pere
 */
public final class L2ArmyMonsterInstance extends L2MonsterInstance
{
	private MoveTask _mt2;
	private int _type;
	private int _movesDone = 0;
	private boolean _isDoingAMove = false;
	private boolean _isTheLastMob = false;

	public L2ArmyMonsterInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		_type = getNpcId() % 10;
		setIsInvul(true);
		setTitle(template.Title);
	}

	public void move(int x, int y, int z)
	{
		getSpawn().setX(x);
		getSpawn().setY(y);
		getSpawn().setZ(z);

		if (_isDoingAMove)
		{
			_mt2 = new MoveTask(x, y, z);
		}
		else
		{
			_isDoingAMove = true;
			MoveTask mt = new MoveTask(x, y, z);
			ThreadPoolManager.getInstance().scheduleGeneral(mt, 1000L);
		}
	}

	public void stopMove()
	{
		_isDoingAMove = false;
		_movesDone++;
		if (_movesDone == 1)
		{
			if (_type == 0)
			{
				shout("TO YOUR POSITIONS!");
				CommanderPatienceTask pt = new CommanderPatienceTask();
				ThreadPoolManager.getInstance().scheduleGeneral(pt, 200000L);
			}
			ThreadPoolManager.getInstance().scheduleGeneral(_mt2, 1000L);
		}
		else if (_movesDone == 2 && _isTheLastMob)
		{
			MonsterInvasion.getInstance().startInvasionFight();
		}
		broadcastPacket(new ValidateLocation(this));
	}

	class MoveTask implements Runnable
	{
		protected int _x;
		protected int _y;
		protected int _z;

		protected MoveTask(int x, int y, int z)
		{
			_x = x;
			_y = y;
			_z = z;
		}

		@Override
		public void run()
		{
			if (getAI().getIntention() != CtrlIntention.AI_INTENTION_MOVE_TO &&
					(Math.abs(getX() - _x) > 5 || Math.abs(getY() - _y) > 5))
			{
				getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(_x, _y, _z, 0));
			}
			if (Math.abs(getX() - _x) > 5 || Math.abs(getY() - _y) > 5)
			{
				ThreadPoolManager.getInstance().scheduleGeneral(this, 1000L);
			}
			else
			{
				stopMove();
			}
		}
	}

	class CommanderPatienceTask implements Runnable
	{
		@Override
		public void run()
		{
			MonsterInvasion.getInstance().startInvasionFight();
		}
	}

	public void shout(String message)
	{
		CreatureSay cs;
		for (L2PcInstance player : getKnownList().getKnownPlayers().values())
		{
			cs = new CreatureSay(getObjectId(), Say2.BATTLEFIELD, getName(), message);
			player.sendPacket(cs);
		}
	}

	private void whisp(L2PcInstance player, String message)
	{
		player.sendPacket(new CreatureSay(getObjectId(), Say2.TELL, getName(), message));
	}

	public void setIsTheLastMob(boolean last)
	{
		_isTheLastMob = last;
	}

	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
		{
			return false;
		}

		if (_type == 0)
		{
			shout("WE'LL BE BACK!");
			MonsterInvasion.getInstance().onCommanderDeath();
		}

		return true;
	}

	@Override
	public void addDamageHate(L2Character attacker, int damage, int aggro)
	{
		if (!isInvul(attacker))
		{
			super.addDamageHate(attacker, damage, aggro);
		}
		else if (attacker instanceof L2PcInstance)
		{
			SkillTable.getInstance().getInfo(1069, 1).getEffects(attacker, attacker);
			whisp((L2PcInstance) attacker, "Not now! Don't you see that we are marching?");
		}
	}

	@Override
	public boolean isAggressive()
	{
		if (_type == 0)
		{
			return false;
		}

		return !isInvul();
	}

	@Override
	public int getAggroRange()
	{
		if (isInvul())
		{
			return 0;
		}

		else
		{
			return 2000;
		}
	}
}
