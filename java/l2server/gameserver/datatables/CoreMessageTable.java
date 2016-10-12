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
	private static CoreMessageTable _instance;

	private static Map<Integer, CoreMessage> _messages = new HashMap<>();

	public static CoreMessageTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new CoreMessageTable();
		}

		return _instance;
	}

	private CoreMessageTable()
	{
		CoreMessage cm = new CoreMessage("(Unknown Text)");
		_messages.put(-1, cm);
		cm = new CoreMessage("$s1");
		_messages.put(0, cm);
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
				_messages.put(id, new CoreMessage(text));
			}
		}

		Log.info("Message Table: Loading " + _messages.size() + " Core Messages Sucessfully");
	}

	public CoreMessage getMessage(int id)
	{
		if (_messages.containsKey(id))
		{
			return _messages.get(id);
		}
		else
		{
			Log.warning("Unknown text: " + id);
			return _messages.get(-1);
		}
	}

	public void reload()
	{
		_messages.clear();
		readMessageTable();
	}
}
