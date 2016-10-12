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

package l2server.gameserver.model.zone.type;

import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.zone.L2ZoneType;
import l2server.gameserver.network.serverpackets.SystemMessage;

/**
 * A mother-trees zone
 * Basic type zone for Hp, MP regen
 *
 * @author durgus
 */
public class L2MotherTreeZone extends L2ZoneType
{
	private int _enterMsg;
	private int _leaveMsg;
	private int _mpRegen;
	private int _hpRegen;

	public L2MotherTreeZone(int id)
	{
		super(id);
	}

	@Override
	public void setParameter(String name, String value)
	{
		switch (name)
		{
			case "enterMsgId":
				_enterMsg = Integer.valueOf(value);
				break;
			case "leaveMsgId":
				_leaveMsg = Integer.valueOf(value);
				break;
			case "MpRegenBonus":
				_mpRegen = Integer.valueOf(value);
				break;
			case "HpRegenBonus":
				_hpRegen = Integer.valueOf(value);
				break;
			default:
				super.setParameter(name, value);
				break;
		}
	}

	@Override
	protected void onEnter(L2Character character)
	{
		if (character instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) character;
			player.setInsideZone(L2Character.ZONE_MOTHERTREE, true);
			if (_enterMsg != 0)
			{
				player.sendPacket(SystemMessage.getSystemMessage(_enterMsg));
			}
		}
	}

	@Override
	protected void onExit(L2Character character)
	{
		if (character instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) character;
			player.setInsideZone(L2Character.ZONE_MOTHERTREE, false);
			if (_leaveMsg != 0)
			{
				player.sendPacket(SystemMessage.getSystemMessage(_leaveMsg));
			}
		}
	}

	@Override
	public void onDieInside(L2Character character, L2Character killer)
	{
	}

	@Override
	public void onReviveInside(L2Character character)
	{
	}

	/**
	 * @return the _mpRegen
	 */
	public int getMpRegenBonus()
	{
		return _mpRegen;
	}

	/**
	 * @return the _hpRegen
	 */
	public int getHpRegenBonus()
	{
		return _hpRegen;
	}
}
