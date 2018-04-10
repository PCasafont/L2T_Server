package l2server.gameserver.datatables;

import l2server.Config;
import l2server.gameserver.model.Skill;
import l2server.util.loader.annotations.Load;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Warning: must be loaded after loading SkillTable
 *
 * @author DrHouse
 */
public class ResidentialSkillTable {
	private static Logger log = LoggerFactory.getLogger(ResidentialSkillTable.class.getName());

	private static ResidentialSkillTable instance = null;
	private static Map<Integer, ArrayList<Skill>> list;

	private ResidentialSkillTable() {
	}
	
	@Load(dependencies = SkillTable.class)
	public void load() {
		list = new HashMap<>();

		if (Config.IS_CLASSIC) {
			return;
		}

		File file = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "residentialSkills.xml");
		XmlDocument doc = new XmlDocument(file);

		for (XmlNode d : doc.getChildren()) {
			if (d.getName().equalsIgnoreCase("skill")) {
				int entityId = d.getInt("entityId");
				int skillId = d.getInt("id");
				int skillLvl = d.getInt("level");

				Skill sk = SkillTable.getInstance().getInfo(skillId, skillLvl);
				if (sk == null) {
					log.warn("ResidentialSkillTable: SkillTable has returned null for ID/level: " + skillId + "/" + skillLvl);
					continue;
				}

				if (!list.containsKey(entityId)) {
					ArrayList<Skill> aux = new ArrayList<>();
					aux.add(sk);
					list.put(entityId, aux);
				} else {
					list.get(entityId).add(sk);
				}
			}
		}

		log.info("ResidentialSkillTable: Loaded " + list.size() + " entities with associated skills.");
	}

	public ArrayList<Skill> getSkills(int entityId) {
		if (list.containsKey(entityId)) {
			return list.get(entityId);
		}

		return null;
	}

	public static ResidentialSkillTable getInstance() {
		if (instance == null) {
			instance = new ResidentialSkillTable();
		}

		return instance;
	}
}
