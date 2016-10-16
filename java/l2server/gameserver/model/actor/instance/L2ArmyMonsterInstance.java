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
	private MoveTask mt2;
	private int type;
	private int movesDone = 0;
	private boolean isDoingAMove = false;
	private boolean isTheLastMob = false;

	public L2ArmyMonsterInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		this.type = getNpcId() % 10;
		setIsInvul(true);
		setTitle(template.Title);
	}

	public void move(int x, int y, int z)
	{
		getSpawn().setX(x);
		getSpawn().setY(y);
		getSpawn().setZ(z);

		if (this.isDoingAMove)
		{
			this.mt2 = new MoveTask(x, y, z);
		}
		else
		{
			this.isDoingAMove = true;
			MoveTask mt = new MoveTask(x, y, z);
			ThreadPoolManager.getInstance().scheduleGeneral(mt, 1000L);
		}
	}

	public void stopMove()
	{
		this.isDoingAMove = false;
		this.movesDone++;
		if (this.movesDone == 1)
		{
			if (this.type == 0)
			{
				shout("TO YOUR POSITIONS!");
				CommanderPatienceTask pt = new CommanderPatienceTask();
				ThreadPoolManager.getInstance().scheduleGeneral(pt, 200000L);
			}
			ThreadPoolManager.getInstance().scheduleGeneral(this.mt2, 1000L);
		}
		else if (this.movesDone == 2 && this.isTheLastMob)
		{
			MonsterInvasion.getInstance().startInvasionFight();
		}
		broadcastPacket(new ValidateLocation(this));
	}

	class MoveTask implements Runnable
	{
		protected int x;
		protected int y;
		protected int z;

		protected MoveTask(int x, int y, int z)
		{
			this.x = x;
			this.y = y;
			this.z = z;
		}

		@Override
		public void run()
		{
			if (getAI().getIntention() != CtrlIntention.AI_INTENTION_MOVE_TO &&
					(Math.abs(getX() - this.x) > 5 || Math.abs(getY() - this.y) > 5))
			{
				getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(this.x, this.y, this.z, 0));
			}
			if (Math.abs(getX() - this.x) > 5 || Math.abs(getY() - this.y) > 5)
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
		this.isTheLastMob = last;
	}

	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
		{
			return false;
		}

		if (this.type == 0)
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
		if (this.type == 0)
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
