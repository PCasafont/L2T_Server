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

package l2server.gameserver.network.clientpackets;

import l2server.Config;
import l2server.gameserver.datatables.*;
import l2server.gameserver.datatables.SubPledgeSkillTree.SubUnitSkill;
import l2server.gameserver.model.*;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.instance.L2TransformManagerInstance;
import l2server.gameserver.network.serverpackets.AcquireSkillInfo;
import l2server.gameserver.network.serverpackets.ExAcquireSkillInfo;
import l2server.log.Log;

/**
 * This class ...
 *
 * @version $Revision: 1.5.2.1.2.5 $ $Date: 2005/04/06 16:13:48 $
 */
public class RequestAcquireSkillInfo extends L2GameClientPacket
{

	private int _id;
	private int _level;
	private int _skillType;

	@Override
	protected void readImpl()
	{
		_id = readD();
		_level = readD();
		_skillType = readD();
	}

	@Override
	protected void runImpl()
	{
		if (_id <= 0 || _level <= 0) // minimal sanity check
		{
			return;
		}

		final L2PcInstance activeChar = getClient().getActiveChar();

		if (activeChar == null)
		{
			return;
		}

		final L2Npc trainer = activeChar.getLastFolkNPC();

		final L2Skill skill = SkillTable.getInstance().getInfo(_id, _level);

		boolean canteach = false;

		if (skill == null)
		{
			if (Config.DEBUG)
			{
				Log.warning("skill id " + _id + " level " + _level + " is undefined. aquireSkillInfo failed.");
			}

			return;
		}

		if (_skillType == 0)
		{
			if (trainer instanceof L2TransformManagerInstance)
			{
				int itemId = 0;
				L2TransformSkillLearn[] skillst = SkillTreeTable.getInstance().getAvailableTransformSkills(activeChar);

				for (L2TransformSkillLearn s : skillst)
				{
					if (s.getId() == _id && s.getLevel() == _level)
					{
						canteach = true;
						itemId = s.getItemId();
						break;
					}
				}

				if (!canteach)
				{
					return; // cheater
				}

				int requiredSp = 0;
				AcquireSkillInfo asi = new AcquireSkillInfo(skill.getId(), skill.getLevelHash(), requiredSp, 0);

				// all transformations require scrolls
				asi.addRequirement(99, itemId, 1, 50);
				sendPacket(asi);
				return;
			}

			L2SkillLearn skillToLearn = null;
			L2SkillLearn[] skills = SkillTreeTable.getInstance().getAvailableClassSkills(activeChar);

			for (L2SkillLearn s : skills)
			{
				if (s.getId() == _id && s.getLevel() == _level)
				{
					skillToLearn = s;
					break;
				}
			}

			if (skillToLearn == null)
			{
				return; // cheater
			}

			sendPacket(new ExAcquireSkillInfo(skillToLearn, activeChar));
		}
		else if (_skillType == 2)
		{
			int requiredRep = 0;
			L2PledgeSkillLearn[] skills = PledgeSkillTree.getInstance().getAvailableSkills(activeChar);

			for (L2PledgeSkillLearn s : skills)
			{
				if (s.getId() == _id && s.getLevel() == _level)
				{
					canteach = true;
					requiredRep = s.getRepCost();
					break;
				}
			}

			if (!canteach)
			{
				return; // cheater
			}

			AcquireSkillInfo asi = new AcquireSkillInfo(skill.getId(), skill.getLevelHash(), requiredRep, 2);
			sendPacket(asi);
		}
		else if (_skillType == 3)
		{
			if (trainer instanceof L2SquadTrainer)
			{
				SubUnitSkill sus = SubPledgeSkillTree.getInstance().getSkill(SkillTable.getSkillHashCode(skill));
				AcquireSkillInfo asi =
						new AcquireSkillInfo(skill.getId(), skill.getLevelHash(), sus.getReputation(), 3);
				asi.addRequirement(0, sus.getItemId(), sus.getCount(), 0);
				sendPacket(asi);
			}
		}
		else if (_skillType == 4)
		{
			int cost = CertificateSkillTable.getInstance().getSubClassSkillCost(skill.getId());
			if (cost > 0)
			{
				AcquireSkillInfo asi = new AcquireSkillInfo(skill.getId(), skill.getLevelHash(), 0, 4);
				asi.addRequirement(99, CertificateSkillTable.SUBCLASS_CERTIFICATE, cost, 50);
				sendPacket(asi);
			}
		}
		else if (_skillType == 5)
		{
			int cost = CertificateSkillTable.getInstance().getDualClassSkillCost(skill.getId());
			if (cost > 0)
			{
				AcquireSkillInfo asi = new AcquireSkillInfo(skill.getId(), skill.getLevelHash(), 0, 5);
				asi.addRequirement(99, CertificateSkillTable.DUALCLASS_CERTIFICATE, cost, 50);
				sendPacket(asi);
			}
		}
		else if (_skillType == 6)
		{
			int costid = 0;
			int costcount = 0;
			L2SkillLearn[] skillsc = SkillTreeTable.getInstance().getAvailableSpecialSkills(activeChar);
			for (L2SkillLearn s : skillsc)
			{
				L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());

				if (sk == null || sk != skill)
				{
					continue;
				}

				canteach = true;
				costid = 0;
				costcount = 0;
			}

			AcquireSkillInfo asi = new AcquireSkillInfo(skill.getId(), skill.getLevelHash(), 0, 6);
			asi.addRequirement(5, costid, costcount, 0);
			sendPacket(asi);
		}
		else
		// Common Skills
		{
			L2SkillLearn skillToLearn = null;
			L2SkillLearn[] skillsc = SkillTreeTable.getInstance().getAvailableSkills(activeChar);

			for (L2SkillLearn s : skillsc)
			{
				L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());

				if (sk == null || sk != skill)
				{
					continue;
				}

				skillToLearn = s;
			}

			if (skillToLearn == null)
			{
				return;
			}

			sendPacket(new ExAcquireSkillInfo(skillToLearn, activeChar));
		}
	}
}
