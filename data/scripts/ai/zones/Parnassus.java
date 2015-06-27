package ai.zones;

import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.network.serverpackets.EventTrigger;

/**
 * @author LasTravel
 */

public class Parnassus extends Quest
{
	private static final int 	_crystalPrisonEntrance 	= 33523;
	private static final int 	_crystalCavernsEntrance	= 33522;
	
	public Parnassus(int id, String name, String descr)
	{
		super(id, name, descr);
		
		addCreatureSeeId(_crystalPrisonEntrance);
		addCreatureSeeId(_crystalCavernsEntrance);
	}
	
	@Override
	public String onCreatureSee(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		if (player != null)
		{
			if (npc.getNpcId() == _crystalPrisonEntrance)
			{	
				player.sendPacket(new EventTrigger(24230010, true));
				player.sendPacket(new EventTrigger(24230012, true));
			}	
			else
			{
				player.sendPacket(new EventTrigger(24230014, true));
				player.sendPacket(new EventTrigger(24230016, true));
			}	
		}
		return "";
	}

	public static void main(String[] args)
	{
		new Parnassus(-1, "Parnassus", "ai");
	}
}