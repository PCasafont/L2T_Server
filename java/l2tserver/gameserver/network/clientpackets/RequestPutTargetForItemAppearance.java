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

import l2tserver.gameserver.ThreadPoolManager;
import l2tserver.gameserver.model.L2ItemInstance;
import l2tserver.gameserver.model.actor.instance.L2PcInstance;
import l2tserver.gameserver.network.SystemMessageId;
import l2tserver.gameserver.network.serverpackets.ExPutTargetResultForItemAppearance;
import l2tserver.gameserver.network.serverpackets.ExShowScreenMessage;
import l2tserver.gameserver.network.serverpackets.SystemMessage;

/**
 * @author Pere
 */
public final class RequestPutTargetForItemAppearance extends L2GameClientPacket
{
	private int _objectId;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}
	
	/**
	 * @see l2tserver.util.network.BaseRecievePacket.ClientBasePacket#runImpl()
	 */
	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;
		
		L2ItemInstance stone = player.getActiveAppearanceStone();
		if (stone == null)
			return;
		
		L2ItemInstance item = player.getInventory().getItemByObjectId(_objectId);
		if (item == null)
			return;
		
		if (!item.getItem().canBeUsedAsApp())
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ITEM_CANNOT_APPEARENCE_WEAPON));
			return;
		}

		int type = stone.getStoneType();
		
		if (item.getItem().getItemGradePlain() != stone.getItem().getItemGradePlain()
				|| (type != -1 && item.getItem().getType2() != type))
		{
			sendPacket(new ExPutTargetResultForItemAppearance(0, 0));
			return;
		}
		
		// TODO adena price
		sendPacket(new ExPutTargetResultForItemAppearance(1, 30));
		
		// TODO fix that
		sendPacket(new ExShowScreenMessage("Warning: adding the appearance item will start the transformation right away...", 3000));
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			public void run()
			{
				sendPacket(new ExShowScreenMessage("...destroying the stone!", 3000));
			}
		}, 3000);
	}
	
	/**
	 * @see l2tserver.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return "RequestWorldStatistics";
	}
}
