package l2server.gameserver.datatables;

import gnu.trove.TIntObjectHashMap;
import l2server.Config;
import l2server.gameserver.model.L2Skill;
import l2server.log.Log;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;

import java.io.File;
import java.util.ArrayList;

/**
 * Warning: must be loaded after loading SkillTable
 *
 * @author DrHouse
 */
public class ResidentialSkillTable
{

	private static ResidentialSkillTable instance = null;
	private static TIntObjectHashMap<ArrayList<L2Skill>> list;

	ResidentialSkillTable()
	{
		load();
	}

	private void load()
	{
		list = new TIntObjectHashMap<>();

		if (Config.IS_CLASSIC)
		{
			return;
		}

		File file = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "residentialSkills.xml");
		XmlDocument doc = new XmlDocument(file);

		for (XmlNode d : doc.getChildren())
		{
            if (d.getName().equalsIgnoreCase("skill"))
            {
                int entityId = d.getInt("entityId");
                int skillId = d.getInt("id");
                int skillLvl = d.getInt("level");

                L2Skill sk = SkillTable.getInstance().getInfo(skillId, skillLvl);
                if (sk == null)
                {
                    Log.warning("ResidentialSkillTable: SkillTable has returned null for ID/level: " + skillId +
                            "/" + skillLvl);
                    continue;
                }

                if (!list.containsKey(entityId))
                {
                    ArrayList<L2Skill> aux = new ArrayList<>();
                    aux.add(sk);
                    list.put(entityId, aux);
                }
                else
                {
                    list.get(entityId).add(sk);
                }
            }
        }

		Log.info("ResidentialSkillTable: Loaded " + list.size() + " entities with associated skills.");
	}

	public ArrayList<L2Skill> getSkills(int entityId)
	{
		if (list.containsKey(entityId))
		{
			return list.get(entityId);
		}

		return null;
	}

	public static ResidentialSkillTable getInstance()
	{
		if (instance == null)
		{
			instance = new ResidentialSkillTable();
		}

		return instance;
	}
}
