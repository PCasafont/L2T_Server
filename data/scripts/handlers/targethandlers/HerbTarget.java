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

package handlers.targethandlers;

import l2server.gameserver.handler.ISkillTargetTypeHandler;
import l2server.gameserver.handler.SkillTargetTypeHandler;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Summon;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.instance.L2PetInstance;
import l2server.gameserver.templates.skills.L2SkillTargetType;

import java.util.ArrayList;

/**
 * Used by all herb skills.
 *
 * @author ZaKaX.
 */
public class HerbTarget implements ISkillTargetTypeHandler
{
	@Override
	public L2Object[] getTargetList(L2Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		L2PcInstance aPlayer = null;
		L2Summon aSummon = null;

		final ArrayList<L2Character> _aResult = new ArrayList<L2Character>();

		if (activeChar instanceof L2PcInstance)
		{
			aPlayer = (L2PcInstance) activeChar;
			aSummon = aPlayer.getPet();
		}
		else if (activeChar instanceof L2Summon)
		{
			aSummon = (L2Summon) activeChar;
		}
		else if (activeChar instanceof L2PetInstance)
		{
			aSummon = (L2PetInstance) activeChar;
		}

		// If it's a player that picked up the herb...
		if (aPlayer != null)
		{
			// Affect the player.
			_aResult.add(aPlayer);

			// As well as his summon, if it's a summon and NOT a pet.
			if (aSummon != null && !(aSummon instanceof L2PetInstance))
			{
				_aResult.add(aSummon);
			}
		}
		else
		{
			// Otherwise, a summon picked it up. Only the summon is affected in this case.
			_aResult.add(aSummon);
		}

		return _aResult.toArray(new L2Character[_aResult.size()]);
	}

	@Override
	public Enum<L2SkillTargetType> getTargetType()
	{
		return L2SkillTargetType.HERB_TARGET;
	}

	public static void main(String[] args)
	{
		SkillTargetTypeHandler.getInstance().registerSkillTargetType(new HerbTarget());
	}
}
