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
package handlers.bypasshandlers;

import java.util.StringTokenizer;

import l2tserver.gameserver.handler.IBypassHandler;
import l2tserver.gameserver.instancemanager.CastleManager;
import l2tserver.gameserver.instancemanager.CastleManorManager;
import l2tserver.gameserver.model.L2Clan;
import l2tserver.gameserver.model.actor.L2Npc;
import l2tserver.gameserver.model.actor.instance.L2CastleChamberlainInstance;
import l2tserver.gameserver.model.actor.instance.L2ManorManagerInstance;
import l2tserver.gameserver.model.actor.instance.L2MerchantInstance;
import l2tserver.gameserver.model.actor.instance.L2PcInstance;
import l2tserver.gameserver.model.entity.Castle;
import l2tserver.gameserver.network.SystemMessageId;
import l2tserver.gameserver.network.serverpackets.ActionFailed;
import l2tserver.gameserver.network.serverpackets.BuyListSeed;
import l2tserver.gameserver.network.serverpackets.ExShowCropInfo;
import l2tserver.gameserver.network.serverpackets.ExShowCropSetting;
import l2tserver.gameserver.network.serverpackets.ExShowManorDefaultInfo;
import l2tserver.gameserver.network.serverpackets.ExShowProcureCropDetail;
import l2tserver.gameserver.network.serverpackets.ExShowSeedInfo;
import l2tserver.gameserver.network.serverpackets.ExShowSeedSetting;
import l2tserver.gameserver.network.serverpackets.ExShowSellCropList;
import l2tserver.gameserver.network.serverpackets.SystemMessage;

public class ManorManager implements IBypassHandler
{
	private static final String[] COMMANDS =
	{
		"manor_menu_select"
	};
	
	public boolean useBypass(String command, L2PcInstance activeChar, L2Npc target)
	{
		final L2Npc manager = activeChar.getLastFolkNPC();
		final boolean isCastle = manager instanceof L2CastleChamberlainInstance;
		if (!(manager instanceof L2ManorManagerInstance || isCastle))
			return false;
		
		if (!activeChar.isInsideRadius(manager, L2Npc.DEFAULT_INTERACTION_DISTANCE, true, false))
			return false;
		
		try
		{
			final Castle castle = manager.getCastle();
			if (isCastle)
			{
				if (activeChar.getClan() == null
						|| castle.getOwnerId() != activeChar.getClanId()
						|| (activeChar.getClanPrivileges() & L2Clan.CP_CS_MANOR_ADMIN) != L2Clan.CP_CS_MANOR_ADMIN)
				{
					manager.showChatWindowByFileName(activeChar, "chamberlain/chamberlain-noprivs.htm");
					return false;
				}
				if (castle.getSiege().getIsInProgress())
				{
					manager.showChatWindowByFileName(activeChar, "chamberlain/chamberlain-busy.htm");
					return false;
				}
			}
			
			if (CastleManorManager.getInstance().isUnderMaintenance())
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THE_MANOR_SYSTEM_IS_CURRENTLY_UNDER_MAINTENANCE));
				return true;
			}
			
			final StringTokenizer st = new StringTokenizer(command, "&");
			final int ask = Integer.parseInt(st.nextToken().split("=")[1]);
			final int state = Integer.parseInt(st.nextToken().split("=")[1]);
			final int time = Integer.parseInt(st.nextToken().split("=")[1]);
			
			final int castleId;
			if (state < 0)
				castleId = castle.getCastleId(); // info for current manor
			else
				castleId = state; // info for requested manor
			
			switch (ask)
			{
				case 1: // Seed purchase
					if (isCastle)
						break;
					if (castleId != castle.getCastleId())
					{
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.HERE_YOU_CAN_BUY_ONLY_SEEDS_OF_S1_MANOR);
						sm.addString(manager.getCastle().getName());
						activeChar.sendPacket(sm);
					}
					else
						activeChar.sendPacket(new BuyListSeed(activeChar.getAdena(), castleId, castle.getSeedProduction(CastleManorManager.PERIOD_CURRENT)));
					break;
				case 2: // Crop sales
					if (isCastle)
						break;
					activeChar.sendPacket(new ExShowSellCropList(activeChar, castleId, castle.getCropProcure(CastleManorManager.PERIOD_CURRENT)));
					break;
				case 3: // Current seeds (Manor info)
					if (time == 1 && !CastleManager.getInstance().getCastleById(castleId).isNextPeriodApproved())
						activeChar.sendPacket(new ExShowSeedInfo(castleId, null));
					else
						activeChar.sendPacket(new ExShowSeedInfo(castleId, CastleManager.getInstance().getCastleById(castleId).getSeedProduction(time)));
					break;
				case 4: // Current crops (Manor info)
					if (time == 1 && !CastleManager.getInstance().getCastleById(castleId).isNextPeriodApproved())
						activeChar.sendPacket(new ExShowCropInfo(castleId, null));
					else
						activeChar.sendPacket(new ExShowCropInfo(castleId, CastleManager.getInstance().getCastleById(castleId).getCropProcure(time)));
					break;
				case 5: // Basic info (Manor info)
					activeChar.sendPacket(new ExShowManorDefaultInfo());
					break;
				case 6: // Buy harvester
					if (isCastle)
						break;
					((L2MerchantInstance)manager).showBuyWindow(activeChar, 300000 + manager.getNpcId());
					break;
				case 7: // Edit seed setup
					if (!isCastle)
						break;
					if (castle.isNextPeriodApproved())
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.A_MANOR_CANNOT_BE_SET_UP_BETWEEN_6_AM_AND_8_PM));
					else
						activeChar.sendPacket(new ExShowSeedSetting(castle.getCastleId()));
					break;
				case 8: // Edit crop setup
					if (!isCastle)
						break;
					if (castle.isNextPeriodApproved())
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.A_MANOR_CANNOT_BE_SET_UP_BETWEEN_6_AM_AND_8_PM));
					else
						activeChar.sendPacket(new ExShowCropSetting(castle.getCastleId()));
					break;
				case 9: // Edit sales (Crop sales)
					if (isCastle)
						break;
					activeChar.sendPacket(new ExShowProcureCropDetail(state));
					break;
				default:
					return false;
			}
			return true;
		}
		catch (Exception e)
		{
			_log.info(e.getMessage());
		}
		return false;
	}
	
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}
