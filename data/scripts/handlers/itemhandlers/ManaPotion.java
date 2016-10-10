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

import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.actor.L2Playable;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.instance.L2PetInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.SystemMessage;

public class ManaPotion extends ItemSkillsTemplate
{
    /**
     * @see l2server.gameserver.handler.IItemHandler#useItem(l2server.gameserver.model.actor.L2Playable, l2server.gameserver.model.L2ItemInstance, boolean)
     */
    @Override
    public void useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
    {
        L2PcInstance activeChar; // use activeChar only for L2PcInstance checks where cannot be used PetInstance

        if (playable instanceof L2PcInstance)
        {
            activeChar = (L2PcInstance) playable;
        }
        else if (playable instanceof L2PetInstance)
        {
            activeChar = ((L2PetInstance) playable).getOwner();
        }
        else
        {
            return;
        }

        if (activeChar.isInOlympiadMode())
        {
            playable.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOTHING_HAPPENED));
            return;
        }

        if (activeChar.getEvent() != null && !activeChar.getEvent().onPotionUse(activeChar.getObjectId()))
        {
            playable.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        super.useItem(playable, item, forceUse);
    }
}
