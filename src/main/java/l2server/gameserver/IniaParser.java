package l2server.gameserver;

import l2server.gameserver.Nodes.NodesManager;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.L2GameClient;

import java.util.StringTokenizer;

/**
 * @author Vasper
 * @since 4/17/2017
 */
public class IniaParser
{
	public void handleCommands(L2GameClient client, String command)
	{
		L2PcInstance activeChar = client.getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		StringTokenizer st = new StringTokenizer(command, ";");
		st.nextToken();
		
		
		String val = st.nextToken();
		if (val.startsWith("_node"))
		{
			parseNode(activeChar, command);
		}
	
	}
	
	public void parseNode(L2PcInstance player, String command)
	{
		StringTokenizer st = new StringTokenizer(command, ";");
		st.nextToken();
		
		String val = st.nextToken();
		val = st.nextToken();
		switch (val)
		{
			case "conquer":
			{
				NodesManager.getInstance().tryOwnNode(player, (L2Npc)player.getTarget());
				break;
			}
		}
	
		
	}
	
	public static IniaParser getInstance()
	{
		return IniaParser.SingletonHolder.instance;
	}
	
	private static class SingletonHolder
	{
		protected static final IniaParser instance = new IniaParser();
	}
}
