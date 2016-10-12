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

package l2server.gameserver.network.serverpackets;

import l2server.gameserver.datatables.MapRegionTable;
import l2server.gameserver.instancemanager.CastleManager;
import l2server.gameserver.instancemanager.FortManager;
import l2server.gameserver.model.L2Clan;
import l2server.gameserver.model.L2SiegeClan;
import l2server.gameserver.model.actor.L2Attackable;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.entity.Castle;
import l2server.gameserver.model.entity.Fort;

/**
 * sample
 * 0b
 * 952a1048	 objectId
 * 00000000 00000000 00000000 00000000 00000000 00000000
 * <p>
 * format  dddddd   rev 377
 * format  ddddddd   rev 417
 *
 * @version $Revision: 1.3.2.1.2.5 $ $Date: 2005/03/27 18:46:18 $
 */
public class Die extends L2GameServerPacket
{
	private int _charObjId;
	private boolean _canTeleport;
	private boolean _sweepable;
	private boolean _allowFixedRess;
	private L2Clan _clan;
	L2Character _activeChar;

	/**
	 * @param cha
	 */
	public Die(L2Character cha)
	{
		_activeChar = cha;

		_canTeleport =
				!(cha instanceof L2PcInstance && ((L2PcInstance) cha).isPlayingEvent() || cha.isPendingRevive()) ||
						cha.getX() == 0 && cha.getY() == 0;

		_charObjId = cha.getObjectId();

		if (cha instanceof L2Attackable)
		{
			_sweepable = ((L2Attackable) cha).isSweepActive();
		}
		else if (cha instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) cha;
			_allowFixedRess = player.getAccessLevel().allowFixedRes();
			_clan = player.getClan();

			if (_canTeleport && player.getIsInsideGMEvent())
			{
				_canTeleport = false;
			}
		}

		if (cha.getX() == 0 && cha.getY() == 0)
		{
			cha.teleToLocation(MapRegionTable.TeleportWhereType.Town);
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_charObjId);
		// NOTE:
		// 6d 00 00 00 00 - to nearest village
		// 6d 01 00 00 00 - to hide away
		// 6d 02 00 00 00 - to castle
		// 6d 03 00 00 00 - to siege HQ
		// sweepable
		// 6d 04 00 00 00 - FIXED

		writeD(_canTeleport ? 0x01 : 0); // 6d 00 00 00 00 - to nearest village
		if (_canTeleport && _clan != null)
		{
			boolean isInCastleDefense = false;
			boolean isInFortDefense = false;

			L2SiegeClan siegeClan = null;
			Castle castle = CastleManager.getInstance().getCastle(_activeChar);
			Fort fort = FortManager.getInstance().getFort(_activeChar);
			if (castle != null && castle.getSiege().getIsInProgress())
			{
				//siege in progress
				siegeClan = castle.getSiege().getAttackerClan(_clan);
				if (siegeClan == null && castle.getSiege().checkIsDefender(_clan))
				{
					isInCastleDefense = true;
				}
			}
			else if (fort != null && fort.getSiege().getIsInProgress())
			{
				//siege in progress
				siegeClan = fort.getSiege().getAttackerClan(_clan);
				if (siegeClan == null && fort.getSiege().checkIsDefender(_clan))
				{
					isInFortDefense = true;
				}
			}

			writeD(_clan.getHasHideout() > 0 ? 0x01 : 0x00); // 6d 01 00 00 00 - to hide away
			writeD(_clan.getHasCastle() > 0 || isInCastleDefense ? 0x01 : 0x00); // 6d 02 00 00 00 - to castle
			writeD(siegeClan != null && !isInCastleDefense && !isInFortDefense && !siegeClan.getFlag().isEmpty() ?
					0x01 : 0x00); // 6d 03 00 00 00 - to siege HQ
			writeD(_sweepable ? 0x01 : 0x00); // sweepable  (blue glow)
			writeD(_allowFixedRess ? 0x01 : 0x00); // 6d 04 00 00 00 - to FIXED
			writeD(_clan.getHasFort() > 0 || isInFortDefense ? 0x01 : 0x00); // 6d 05 00 00 00 - to fortress
		}
		else
		{
			writeD(0x00); // 6d 01 00 00 00 - to hide away
			writeD(0x00); // 6d 02 00 00 00 - to castle
			writeD(0x00); // 6d 03 00 00 00 - to siege HQ
			writeD(_sweepable ? 0x01 : 0x00); // sweepable  (blue glow)
			writeD(_allowFixedRess ? 0x01 : 0x00); // 6d 04 00 00 00 - to FIXED
			writeD(0x00); // 6d 05 00 00 00 - to fortress
		}
		//TODO: protocol 152
		/*
        writeC(0); //show die animation
		writeD(0); //agathion ress button
		writeD(0); //additional free space
		 */
	}
}
