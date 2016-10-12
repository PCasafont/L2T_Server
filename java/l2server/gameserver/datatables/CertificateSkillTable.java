/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package l2server.gameserver.datatables;

import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.base.SubClass;
import l2server.gameserver.network.serverpackets.ExAcquireSkillList;
import l2server.gameserver.network.serverpackets.ExAcquireSkillList.SkillType;
import l2server.gameserver.util.Broadcast;

import java.util.HashMap;
import java.util.Map;

public class CertificateSkillTable
{
	public static final int SUBCLASS_CERTIFICATE = 10280;
	public static final int DUALCLASS_CERTIFICATE = 36078;

	public class CertificateSkillLearn
	{
		private int _skillId;
		private int _maxLevel;
		private int _cost;

		public CertificateSkillLearn(int skillId, int maxLevel, int cost)
		{
			_skillId = skillId;
			_maxLevel = maxLevel;
			_cost = cost;
		}

		public int getSkillId()
		{
			return _skillId;
		}

		public int getMaxLevel()
		{
			return _maxLevel;
		}

		public int getCost()
		{
			return _cost;
		}
	}

	private Map<Integer, CertificateSkillLearn> _subClassSkills = new HashMap<>();
	private Map<Integer, CertificateSkillLearn> _dualClassSkills = new HashMap<>();

	public static CertificateSkillTable getInstance()
	{
		return SingletonHolder._instance;
	}

	private CertificateSkillTable()
	{
		load();
	}

	// Hardcoded load for now
	private void load()
	{
		// Sub - P. Atk./M. Atk. Increase
		_subClassSkills.put(1956, new CertificateSkillLearn(1956, 4, 1));
		// Sub - P. Def./M. Def. Increase
		_subClassSkills.put(1957, new CertificateSkillLearn(1957, 4, 1));
		// Sub - Atk. Spd./Casting Spd. Increase
		_subClassSkills.put(1958, new CertificateSkillLearn(1958, 4, 1));
		// Sub - Critical Rate Increase
		_subClassSkills.put(1959, new CertificateSkillLearn(1959, 4, 1));
		// Sub - P. Accuracy/ M. Accuracy Increase
		_subClassSkills.put(1960, new CertificateSkillLearn(1960, 4, 1));
		// Sub - P. Evasion/ M. Evasion Increase
		_subClassSkills.put(1961, new CertificateSkillLearn(1961, 4, 1));

		// Dual - P. Atk./M. Atk. Increase
		_dualClassSkills.put(1962, new CertificateSkillLearn(1962, 4, 1));
		// Dual - P. Def./M. Def. Increase
		_dualClassSkills.put(1963, new CertificateSkillLearn(1963, 4, 1));
		// Dual - Atk. Spd./Casting Spd. Increase
		_dualClassSkills.put(1964, new CertificateSkillLearn(1964, 4, 1));
		// Dual - Max HP Increase
		_dualClassSkills.put(1965, new CertificateSkillLearn(1965, 4, 1));
		// Dual - Max CP Increase
		_dualClassSkills.put(1966, new CertificateSkillLearn(1966, 4, 1));
		// Dual - Max MP Increase
		_dualClassSkills.put(1967, new CertificateSkillLearn(1967, 4, 1));
		// Dual - HP Drain
		_dualClassSkills.put(1968, new CertificateSkillLearn(1968, 1, 2));
		// Dual - MP Drain
		_dualClassSkills.put(1969, new CertificateSkillLearn(1969, 1, 2));
		// Dual - Specialized for P. Atk.
		_dualClassSkills.put(1970, new CertificateSkillLearn(1970, 1, 2));
		// Dual - Specialized for M. Atk.
		_dualClassSkills.put(1971, new CertificateSkillLearn(1971, 1, 2));
		// Dual - Physical Trait Increase
		_dualClassSkills.put(1972, new CertificateSkillLearn(1972, 1, 2));
		// Dual - Mental Trait Increase
		_dualClassSkills.put(1973, new CertificateSkillLearn(1973, 1, 2));
		// Dual - Berserker's Rage
		_dualClassSkills.put(1974, new CertificateSkillLearn(1974, 1, 3));
		// Dual - Master's Rage
		_dualClassSkills.put(1976, new CertificateSkillLearn(1976, 1, 3));
		// Dual - Light of Protection
		_dualClassSkills.put(1978, new CertificateSkillLearn(1978, 1, 3));
		// Dual - Light of Blessing
		_dualClassSkills.put(1980, new CertificateSkillLearn(1980, 1, 3));
		// Dual - Shackles of the Giants
		_dualClassSkills.put(1982, new CertificateSkillLearn(1982, 1, 4));
		// Dual - Silence of the Giants
		_dualClassSkills.put(1983, new CertificateSkillLearn(1983, 1, 4));
		// Dual - Yoke of the Giants
		_dualClassSkills.put(1984, new CertificateSkillLearn(1984, 1, 4));
		// Dual - Shield of the Giants
		_dualClassSkills.put(1985, new CertificateSkillLearn(1985, 1, 4));
	}

	public Map<Integer, CertificateSkillLearn> getSubClassSkills()
	{
		return _subClassSkills;
	}

	public Map<Integer, CertificateSkillLearn> getDualClassSkills()
	{
		return _dualClassSkills;
	}

	public void sendSubClassSkillList(L2PcInstance player)
	{
		ExAcquireSkillList asl = new ExAcquireSkillList(SkillType.SubClass);
		boolean skillAdded = false;
		for (CertificateSkillLearn csl : _subClassSkills.values())
		{
			int curLevel = 0;
			for (L2Skill skill : player.getAllSkills())
			{
				if (skill.getId() == csl.getSkillId())
				{
					curLevel = skill.getLevelHash();
				}
			}

			if (curLevel < csl.getMaxLevel())
			{
				asl.addSkill(csl.getSkillId(), curLevel + 1, csl.getMaxLevel(), 0, 0);
				skillAdded = true;
			}
		}

		if (skillAdded)
		{
			player.sendPacket(asl);
		}
	}

	public void sendDualClassSkillList(L2PcInstance player)
	{
		ExAcquireSkillList asl = new ExAcquireSkillList(SkillType.DualClass);
		boolean skillAdded = false;
		for (CertificateSkillLearn csl : _dualClassSkills.values())
		{
			int curLevel = 0;
			for (L2Skill skill : player.getAllSkills())
			{
				if (skill.getId() == csl.getSkillId())
				{
					curLevel = skill.getLevelHash();
				}
			}

			if (curLevel < csl.getMaxLevel())
			{
				asl.addSkill(csl.getSkillId(), curLevel + 1, csl.getMaxLevel(), 0, 0);
				skillAdded = true;
			}
		}

		if (skillAdded)
		{
			player.sendPacket(asl);
		}
	}

	public int getSubClassSkillCost(int skillId)
	{
		CertificateSkillLearn skill = _subClassSkills.get(skillId);
		if (skill != null)
		{
			return skill.getCost();
		}

		return 0;
	}

	public int getDualClassSkillCost(int skillId)
	{
		CertificateSkillLearn skill = _dualClassSkills.get(skillId);
		if (skill != null)
		{
			return skill.getCost();
		}

		return 0;
	}

	public void checkPlayer(L2PcInstance player)
	{
		int totalSubCerts = 12;
		//for (SubClass sub : player.getSubClasses().values())
		//	totalSubCerts += Math.min(sub.getCertificates(), 4);

		int actualSubCerts = 0;
		L2ItemInstance certsItem = player.getInventory().getItemByItemId(SUBCLASS_CERTIFICATE);
		if (certsItem != null)
		{
			actualSubCerts = (int) certsItem.getCount();
		}
		for (CertificateSkillLearn csl : _subClassSkills.values())
		{
			int skillLevel = player.getSkillLevelHash(csl.getSkillId());
			if (skillLevel > 0)
			{
				actualSubCerts += csl.getCost() * skillLevel;
			}
		}

		if (actualSubCerts > totalSubCerts)
		{
			Broadcast.toGameMasters("Player " + player.getName() + " has invalid subclass certificates! Reverting...");
			resetSubClassCertificates(player);
		}

		int totalDualCerts = 4;
		//for (SubClass sub : player.getSubClasses().values())
		//	totalDualCerts += Math.max(sub.getCertificates() - 4, 0);

		int actualDualCerts = 0;
		certsItem = player.getInventory().getItemByItemId(DUALCLASS_CERTIFICATE);
		if (certsItem != null)
		{
			actualDualCerts = (int) certsItem.getCount();
		}
		for (CertificateSkillLearn csl : _dualClassSkills.values())
		{
			int skillLevel = player.getSkillLevelHash(csl.getSkillId());
			if (skillLevel > 0)
			{
				actualDualCerts += csl.getCost() * skillLevel;
			}
		}

		if (actualDualCerts > totalDualCerts)
		{
			Broadcast
					.toGameMasters("Player " + player.getName() + " has invalid dual class certificates! Reverting...");
			resetDualClassCertificates(player);
		}
	}

	public int getDualClassCertificatesAmount(final L2PcInstance player)
	{
		int actualDualCerts = 0;
		L2ItemInstance certsItem = player.getInventory().getItemByItemId(DUALCLASS_CERTIFICATE);
		if (certsItem != null)
		{
			actualDualCerts = (int) certsItem.getCount();
		}
		for (CertificateSkillLearn csl : _dualClassSkills.values())
		{
			int skillLevel = player.getSkillLevelHash(csl.getSkillId());
			if (skillLevel > 0)
			{
				actualDualCerts += csl.getCost() * skillLevel;
			}
		}

		return actualDualCerts;
	}

	public int getSubclassCertificatesAmount(final L2PcInstance player)
	{
		int actualSubCerts = 0;
		L2ItemInstance certsItem = player.getInventory().getItemByItemId(SUBCLASS_CERTIFICATE);
		if (certsItem != null)
		{
			actualSubCerts = (int) certsItem.getCount();
		}
		for (CertificateSkillLearn csl : _subClassSkills.values())
		{
			int skillLevel = player.getSkillLevelHash(csl.getSkillId());
			if (skillLevel > 0)
			{
				actualSubCerts += csl.getCost() * skillLevel;
			}
		}

		return actualSubCerts;
	}

	public void resetSubClassCertificates(L2PcInstance player)
	{
		L2ItemInstance certsItem = player.getInventory().getItemByItemId(SUBCLASS_CERTIFICATE);
		if (certsItem != null)
		{
			player.destroyItem("SubCerts", certsItem, player, true);
		}

		for (L2Skill skill : player.getAllSkills())
		{
			if (_subClassSkills.containsKey(skill.getId()))
			{
				player.removeSkill(skill, true);
			}
		}

		for (SubClass sub : player.getSubClasses().values())
		{
			sub.setCertificates(0);
		}
	}

	public void resetDualClassCertificates(L2PcInstance player)
	{
		L2ItemInstance certsItem = player.getInventory().getItemByItemId(DUALCLASS_CERTIFICATE);
		if (certsItem != null)
		{
			player.destroyItem("SubCerts", certsItem, player, true);
		}

		for (L2Skill skill : player.getAllSkills())
		{
			if (_dualClassSkills.containsKey(skill.getId()))
			{
				player.removeSkill(skill, true);
			}
		}

		for (SubClass sub : player.getSubClasses().values())
		{
			if (!sub.isDual())
			{
				continue;
			}

			sub.setCertificates(0);
		}
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final CertificateSkillTable _instance = new CertificateSkillTable();
	}
}
