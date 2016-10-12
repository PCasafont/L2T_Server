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
	private final String _message;
	private final Vector<String> _value = new Vector<>();

	public CoreMessage(String message)
	{
		_message = message;
	}

	public CoreMessage(int cmId)
	{
		CoreMessage cm = CoreMessageTable.getInstance().getMessage(cmId);
		_message = cm._message;
	}

	public CoreMessage(CoreMessage cm)
	{
		_message = cm._message;
	}

	public String getString()
	{
		return _message;
	}

	public void addString(String text)
	{
		_value.add(text);
	}

	public void addNumber(double num)
	{
		_value.add(String.valueOf(num));
	}

	public void addNumber(long num)
	{
		_value.add(String.valueOf(num));
	}

	public String renderMsg(String language)
	{
		String message = _message;
		int i = 0;
		for (String text : _value)
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
