package l2server.gameserver.datatables;

import l2server.Config;
import l2server.gameserver.model.CoreMessage;
import l2server.log.Log;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Pere
 */
public class CoreMessageTable
{
	private static CoreMessageTable instance;

	private static Map<Integer, CoreMessage> messages = new HashMap<>();

	public static CoreMessageTable getInstance()
	{
		if (instance == null)
		{
			instance = new CoreMessageTable();
		}

		return instance;
	}

	private CoreMessageTable()
	{
		CoreMessage cm = new CoreMessage("(Unknown Text)");
		this.messages.put(-1, cm);
		cm = new CoreMessage("$s1");
		this.messages.put(0, cm);
		readMessageTable();
	}

	private void readMessageTable()
	{
		File file = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "coreMessages.xml");
		XmlDocument doc = new XmlDocument(file);

		for (XmlNode n : doc.getFirstChild().getChildren())
		{
			if (n.getName().equalsIgnoreCase("coreMessage"))
			{
				int id = n.getInt("id");
				String text = n.getString("text");
				this.messages.put(id, new CoreMessage(text));
			}
		}

		Log.info("Message Table: Loading " + this.messages.size() + " Core Messages Sucessfully");
	}

	public CoreMessage getMessage(int id)
	{
		if (this.messages.containsKey(id))
		{
			return this.messages.get(id);
		}
		else
		{
			Log.warning("Unknown text: " + id);
			return this.messages.get(-1);
		}
	}

	public void reload()
	{
		this.messages.clear();
		readMessageTable();
	}
}
