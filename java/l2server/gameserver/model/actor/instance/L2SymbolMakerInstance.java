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

package l2server.gameserver.model.actor.instance;

import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.templates.chars.L2NpcTemplate;

public class L2SymbolMakerInstance extends L2Npc
{
	public L2SymbolMakerInstance(int objectID, L2NpcTemplate template)
	{
		super(objectID, template);
		setInstanceType(InstanceType.L2SymbolMakerInstance);
	}

	@Override
	public String getHtmlPath(int npcId, int val)
	{
		return "symbolmaker/SymbolMaker.htm";
	}

	/* (non-Javadoc)
	 * @see l2server.gameserver.model.L2Object#isAttackable()
	 */
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return false;
	}
}
