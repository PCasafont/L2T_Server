package l2server.gameserver.model;

import l2server.gameserver.datatables.CoreMessageTable;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;

import java.util.Vector;

/**
 * @author Pere
 */
public class CoreMessage
{
	private final String message;
	private final Vector<String> value = new Vector<>();

	public CoreMessage(String message)
	{
		this.message = message;
	}

	public CoreMessage(int cmId)
	{
		CoreMessage cm = CoreMessageTable.getInstance().getMessage(cmId);
		this.message = cm.message;
	}

	public CoreMessage(CoreMessage cm)
	{
		this.message = cm.message;
	}

	public String getString()
	{
		return this.message;
	}

	public void addString(String text)
	{
		this.value.add(text);
	}

	public void addNumber(double num)
	{
		this.value.add(String.valueOf(num));
	}

	public void addNumber(long num)
	{
		this.value.add(String.valueOf(num));
	}

	public String renderMsg(String language)
	{
		String message = this.message;
		int i = 0;
		for (String text : this.value)
		{
			i++;
			message = message.replace("$s" + i, text);
		}
		return message;
	}

	public void sendMessage(L2PcInstance player)
	{
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1);
		sm.addString(renderMsg("en"));
		player.sendPacket(sm);
	}
}
