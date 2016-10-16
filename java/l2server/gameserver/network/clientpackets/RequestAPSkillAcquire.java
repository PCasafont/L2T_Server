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

import gnu.trove.TIntIntHashMap;
import l2server.gameserver.datatables.AbilityTable;
import l2server.gameserver.datatables.AbilityTable.Ability;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.serverpackets.ExAcquireAPSkillList;

/**
 * @author Pere
 */
public final class RequestAPSkillAcquire extends L2GameClientPacket
{
	private int totalCount;
	private TIntIntHashMap abilities = new TIntIntHashMap();

	@Override
	protected void readImpl()
	{
		this.totalCount = readD(); // Total count

		// Knight skills
		int count = readD();
		for (int i = 0; i < count; i++)
		{
			int id = readD();
			int level = readD();
			this.abilities.put(id, level);
		}

		// Knight skills
		count = readD();
		for (int i = 0; i < count; i++)
		{
			int id = readD();
			int level = readD();
			this.abilities.put(id, level);
		}

		// Knight skills
		count = readD();
		for (int i = 0; i < count; i++)
		{
			int id = readD();
			int level = readD();
			this.abilities.put(id, level);
		}
	}

	/**
	 */
	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null || player.getLevel() < 99)
		{
			return;
		}

		if (this.totalCount != this.abilities.size())
		{
			sendPacket(new ExAcquireAPSkillList(getClient().getActiveChar(), false));
			return;
		}

		int[] counts = new int[3];
		for (int i = 0; i < 3; i++)
		{
			counts[i] = 0;
		}

		// We don't trust the client, let's count how many of each type we got
		for (int skillId : this.abilities.keys())
		{
			counts[AbilityTable.getInstance().getAbility(skillId).getType() - 1] += this.abilities.get(skillId);
		}

		int remainingPoints = player.getAbilityPoints();
		for (int skillId : this.abilities.keys())
		{
			int level = this.abilities.get(skillId);
			Ability ability = AbilityTable.getInstance().getAbility(skillId);
			if (level > ability.getMaxLevel() || level > remainingPoints ||
					counts[ability.getType() - 1] < ability.getReqPoints() || ability.getReqSkill() > 0 &&
					(!this.abilities.containsKey(ability.getReqSkill()) ||
							this.abilities.get(ability.getReqSkill()) < ability.getReqSkillLvl()))
			{
				sendPacket(new ExAcquireAPSkillList(getClient().getActiveChar(), false));
				return;
			}

			remainingPoints -= level;
		}

		player.setAbilities(this.abilities);

		sendPacket(new ExAcquireAPSkillList(getClient().getActiveChar(), true));
	}
}
