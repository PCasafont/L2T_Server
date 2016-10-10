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

/**
 * @author Migi
 */

package handlers.itemhandlers;

import l2server.gameserver.communitybbs.Manager.CustomCommunityBoard;
import l2server.gameserver.handler.IItemHandler;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.actor.L2Playable;
import l2server.gameserver.model.actor.instance.L2MonsterInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;

public class MagicVisor implements IItemHandler
{
    /**
     * @see net.sf.l2j.gameserver.handler.IItemHandler#useItem(net.sf.l2j.gameserver.model.actor.instance.L2Playable, net.sf.l2j.gameserver.model.L2ItemInstance)
     */
    public void useItem(L2Playable playable, L2ItemInstance visorItem, boolean forcedUse)
    {
        if (!(playable instanceof L2PcInstance))
        {
            return;
        }

        L2PcInstance player = (L2PcInstance) playable;

        if (!(player.getTarget() instanceof L2MonsterInstance))
        {
            player.sendMessage("You should target a monster to see its drop list!");
            return;
        }

        L2MonsterInstance mob = (L2MonsterInstance) player.getTarget();

        CustomCommunityBoard.sendDropPage(player, mob.getNpcId(), 1);
    }
}
