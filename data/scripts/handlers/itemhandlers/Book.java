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

import l2server.gameserver.cache.HtmCache;
import l2server.gameserver.handler.IItemHandler;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.actor.L2Playable;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.NpcHtmlMessage;

public class Book implements IItemHandler
{
    /**
     * @see l2server.gameserver.handler.IItemHandler#useItem(l2server.gameserver.model.actor.L2Playable, l2server.gameserver.model.L2ItemInstance, boolean)
     */
    @Override
    public void useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
    {
        if (!(playable instanceof L2PcInstance))
        {
            return;
        }
        L2PcInstance activeChar = (L2PcInstance) playable;
        final int itemId = item.getItemId();

        String filename = "help/" + itemId + ".htm";
        String content = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), filename);

        if (content == null)
        {
            NpcHtmlMessage html = new NpcHtmlMessage(1);
            html.setHtml("<html><body>My Text is missing:<br>" + filename + "</body></html>");
            activeChar.sendPacket(html);
        }
        else
        {
            NpcHtmlMessage itemReply = new NpcHtmlMessage(5, itemId);
            itemReply.setHtml(content);
            itemReply.disableValidation();
            activeChar.sendPacket(itemReply);
        }

        activeChar.sendPacket(ActionFailed.STATIC_PACKET);
    }
}
