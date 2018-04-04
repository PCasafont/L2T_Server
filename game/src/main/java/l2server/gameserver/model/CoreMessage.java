package l2server.gameserver.model;

import l2server.gameserver.datatables.CoreMessageTable;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;

import java.util.Vector;

/**
 * @author Pere
 */
public class CoreMessage {
	private final String message;
	private final Vector<String> value = new Vector<>();

	public CoreMessage(String message) {
		this.message = message;
	}

	public CoreMessage(int cmId) {
		CoreMessage cm = CoreMessageTable.getInstance().getMessage(cmId);
		message = cm.message;
	}

	public CoreMessage(CoreMessage cm) {
		message = cm.message;
	}

	public String getString() {
		return message;
	}

	public void addString(String text) {
		value.add(text);
	}

	public void addNumber(double num) {
		value.add(String.valueOf(num));
	}

	public void addNumber(long num) {
		value.add(String.valueOf(num));
	}

	public String renderMsg(String language) {
		String message = this.message;
		int i = 0;
		for (String text : value) {
			i++;
			message = message.replace("$s" + i, text);
		}
		return message;
	}

	public void sendMessage(Player player) {
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1);
		sm.addString(renderMsg("en"));
		player.sendPacket(sm);
	}
}
