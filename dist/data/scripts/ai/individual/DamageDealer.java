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

package ai.individual;

import l2server.Config;
import l2server.gameserver.events.DamageManager;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;

import ai.group_template.L2AttackableAIScript;

/**
 * @author LasTravel
 */

public class DamageDealer extends L2AttackableAIScript
{
	private static final int DAMAGE_DEALER = 80350;

	public DamageDealer(int questId, String name, String descr)
	{
		super(questId, name, descr);

		if (Config.ENABLE_CUSTOM_DAMAGE_MANAGER)
		{
			L2Npc scarecrow = addSpawn(80350, -114361, 253054, -1542, 14661, false, 0);
			
			scarecrow.setIsImmobilized(true);
			scarecrow.setIsMortal(false);
			scarecrow.disableCoreAI(true);
			addAttackId(DAMAGE_DEALER);
		}
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if (npc.getNpcId() == DAMAGE_DEALER)
		{
			if (attacker.isGM())
			{
				return "";
			}

			if (attacker.isSubClassActive() || attacker.isTransformed() || attacker.getClassId() < 148)
			{
				attacker.sendMessage("You don't meet the conditions to use this!");
				return "";
			}

			DamageManager.getInstance().giveDamage(attacker, damage);
		}

		return super.onAttack(npc, attacker, damage, isPet);
	}

	public static void main(String[] args)
	{
		new DamageDealer(-1, "DamageDealer", "ai");
	}
}
