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
package l2tserver.gameserver.network.clientpackets;

import l2tserver.gameserver.datatables.CompoundTable;
import l2tserver.gameserver.datatables.CompoundTable.Combination;
import l2tserver.gameserver.model.L2ItemInstance;
import l2tserver.gameserver.model.actor.instance.L2PcInstance;
import l2tserver.gameserver.network.serverpackets.ExCompoundFail;
import l2tserver.gameserver.network.serverpackets.ExCompoundSuccess;
import l2tserver.util.Rnd;

/**
 * @author Pere
 */
public final class RequestCompoundStart extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		L2ItemInstance compoundItem1 = activeChar.getCompoundItem1();
		L2ItemInstance compoundItem2 = activeChar.getCompoundItem2();
		activeChar.setCompoundItem1(null);
		activeChar.setCompoundItem2(null);
		
		if (compoundItem1 == null || compoundItem2 == null)
			return;
		
		Combination combination = CompoundTable.getInstance().getCombination(compoundItem1.getItemId(), compoundItem2.getItemId());
		if (combination == null)
			return;
		
		int rnd = Rnd.get(100);
		if (rnd >= combination.getChance())
		{
			// Randomly swap both items, to randomize which item is lost
			if (Rnd.get(2) == 0)
			{
				L2ItemInstance tmp = compoundItem1;
				compoundItem1 = compoundItem2;
				compoundItem2 = tmp;
			}
			
			activeChar.destroyItem("Compound", compoundItem2, activeChar, true);
			sendPacket(new ExCompoundFail(compoundItem1.getItemId()));
			return;
		}

		int newItemId = combination.getResult();
		activeChar.addItem("Compound", newItemId, 1, activeChar, false);
		activeChar.destroyItem("Compound", compoundItem1, activeChar, false);
		activeChar.destroyItem("Compound", compoundItem2, activeChar, false);
		sendPacket(new ExCompoundSuccess(newItemId));
	}
	
	/* (non-Javadoc)
	 * @see l2tserver.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return "RequestCompoundStart";
	}
}
