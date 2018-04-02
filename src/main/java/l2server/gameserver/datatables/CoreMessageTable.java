package l2server.gameserver.datatables;

import l2server.Config;
import l2server.gameserver.model.CoreMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import l2server.util.loader.annotations.Load;
import l2server.util.loader.annotations.Reload;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Pere
 */
public class CoreMessageTable {
	private static Logger log = LoggerFactory.getLogger(CoreMessageTable.class.getName());


	private static CoreMessageTable instance;

	private static Map<Integer, CoreMessage> messages = new HashMap<>();

	public static CoreMessageTable getInstance() {
		if (instance == null) {
			instance = new CoreMessageTable();
		}

		return instance;
	}

	private CoreMessageTable() {
	}

	@Load
	public void readMessageTable() {
		
		CoreMessage cm = new CoreMessage("(Unknown Text)");
		messages.put(-1, cm);
		cm = new CoreMessage("$s1");
		messages.put(0, cm);
		
		File file = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "coreMessages.xml");
		XmlDocument doc = new XmlDocument(file);

		for (XmlNode n : doc.getChildren()) {
			if (n.getName().equalsIgnoreCase("coreMessage")) {
				int id = n.getInt("id");
				String text = n.getString("text");
				messages.put(id, new CoreMessage(text));
			}
		}

		log.info("Message Table: Loading " + messages.size() + " Core Messages Sucessfully");
	}

	public CoreMessage getMessage(int id) {
		if (messages.containsKey(id)) {
			return messages.get(id);
		} else {
			log.warn("Unknown text: " + id);
			return messages.get(-1);
		}
	}

	@Reload("messages")
	public void reload() {
		messages.clear();
		readMessageTable();
	}
}
