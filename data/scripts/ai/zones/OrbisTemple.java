
package ai.zones;

import l2server.Config;
import l2server.gameserver.datatables.DoorTable;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.model.zone.L2ZoneType;

/**
 * @author LasTravel
 */

public class OrbisTemple extends Quest
{
	public OrbisTemple(int id, String name, String descr)
	{
		super(id, name, descr);
		addEnterZoneId(60011); //Out of Temple ---> 1 Floor
		addEnterZoneId(60012); //1 Floor ---> Out of Temple
		addEnterZoneId(60013); //1 Floor ----> 2 Floor
		addEnterZoneId(60014); //2 Floor ----> 1 Floor
		addEnterZoneId(60015); //2 Floor ----> 3 Floor
		addEnterZoneId(60016); //3 Floor ----> 2 Floor
		
		//Keep some doors always open
		for (int a = 26210003; a <= 26210006; a++)
		{
			DoorTable.getInstance().getDoor(a).openMe();
		}
	}
	
	@Override
	public String onEnterZone(L2Character character, L2ZoneType zone)
	{
		//System.out.println("Zone Id = " + zone.getId());
		switch (zone.getId())
		{
			case 60011:
				if (!Config.isServer(Config.DREAMS))
					character.teleToLocation(213983, 53250, -8176);
				break;
			case 60012:
				if (!Config.isServer(Config.DREAMS))
					character.teleToLocation(198022, 90032, -192);
				break;
			case 60013:
				character.teleToLocation(213799, 53253, -14432);
				break;
			case 60014:
				character.teleToLocation(215056, 50467, -8416);
				break;
			case 60015:
				character.teleToLocation(211641, 115547, -12736);
				break;
			case 60016:
				character.teleToLocation(211137, 50501, -14624);
				break;
		}
		return "";
	}
	
	public static void main(String[] args)
	{
		new OrbisTemple(-1, "OrbisTemple", "ai");
	}
}
