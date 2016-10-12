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

	private static ResidentialSkillTable _instance = null;
	private static TIntObjectHashMap<ArrayList<L2Skill>> _list;

	ResidentialSkillTable()
	{
		load();
	}

	private void load()
	{
		_list = new TIntObjectHashMap<>();

		if (Config.IS_CLASSIC)
		{
			return;
		}

		File file = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "residentialSkills.xml");
		XmlDocument doc = new XmlDocument(file);

		for (XmlNode n : doc.getChildren())
		{
			if (n.getName().equalsIgnoreCase("list"))
			{
				for (XmlNode d : n.getChildren())
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

						if (!_list.containsKey(entityId))
						{
							ArrayList<L2Skill> aux = new ArrayList<>();
							aux.add(sk);
							_list.put(entityId, aux);
						}
						else
						{
							_list.get(entityId).add(sk);
						}
					}
				}
			}
		}

		Log.info("ResidentialSkillTable: Loaded " + _list.size() + " entities with associated skills.");
	}

	public ArrayList<L2Skill> getSkills(int entityId)
	{
		if (_list.containsKey(entityId))
		{
			return _list.get(entityId);
		}

		return null;
	}

	public static ResidentialSkillTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new ResidentialSkillTable();
		}

		return _instance;
	}
}
