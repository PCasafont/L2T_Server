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
package handlers.itemhandlers;

import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.handler.IItemHandler;
import l2server.gameserver.instancemanager.CastleManorManager;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Playable;
import l2server.gameserver.model.actor.instance.L2MonsterInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.SystemMessage;

/**
 * @author l3x
 */
public class Harvester implements IItemHandler
{
    L2PcInstance _activeChar;
    L2MonsterInstance _target;

    /**
     * @see l2server.gameserver.handler.IItemHandler#useItem(l2server.gameserver.model.actor.L2Playable, l2server.gameserver.model.L2ItemInstance, boolean)
     */
    public void useItem(L2Playable playable, L2ItemInstance _item, boolean forceUse)
    {
        if (!(playable instanceof L2PcInstance))
        {
            return;
        }

        if (CastleManorManager.getInstance().isDisabled())
        {
            return;
        }

        _activeChar = (L2PcInstance) playable;

        if (!(_activeChar.getTarget() instanceof L2MonsterInstance))
        {
            _activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
            _activeChar.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }

        _target = (L2MonsterInstance) _activeChar.getTarget();

        if (_target == null || !_target.isDead())
        {
            _activeChar.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }

        L2Skill skill = SkillTable.getInstance().getInfo(2098, 1); //harvesting skill
        if (skill != null)
        {
            _activeChar.useMagic(skill, false, false);
        }
    }
}
