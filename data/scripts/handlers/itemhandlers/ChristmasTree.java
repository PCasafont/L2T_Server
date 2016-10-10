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

import l2server.gameserver.datatables.NpcTable;
import l2server.gameserver.handler.IItemHandler;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Spawn;
import l2server.gameserver.model.actor.L2Playable;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.templates.chars.L2NpcTemplate;

public class ChristmasTree implements IItemHandler
{
    /**
     * @see l2server.gameserver.handler.IItemHandler#useItem(l2server.gameserver.model.actor.L2Playable, l2server.gameserver.model.L2ItemInstance, boolean)
     */
    @Override
    public void useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
    {
        L2PcInstance activeChar = (L2PcInstance) playable;
        L2NpcTemplate template1 = null;

        switch (item.getItemId())
        {
            case 5560:
                template1 = NpcTable.getInstance().getTemplate(13006);
                break;
            case 5561:
                template1 = NpcTable.getInstance().getTemplate(13007);
                break;
        }

        if (template1 == null)
        {
            return;
        }

        L2Object target = activeChar.getTarget();
        if (target == null)
        {
            target = activeChar;
        }

        try
        {
            L2Spawn spawn = new L2Spawn(template1);
            spawn.setX(target.getX());
            spawn.setY(target.getY());
            spawn.setZ(target.getZ());
            spawn.doSpawn(false);

            activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);

            activeChar.sendMessage(
                    "Created " + template1.Name + " at x: " + spawn.getX() + " y: " + spawn.getY() + " z: " +
                            spawn.getZ());
        }
        catch (Exception e)
        {
            activeChar.sendMessage("Target is not ingame.");
        }
    }
}
