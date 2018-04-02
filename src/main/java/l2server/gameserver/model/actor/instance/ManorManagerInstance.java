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

package l2server.gameserver.model.actor.instance;

import l2server.gameserver.instancemanager.CastleManorManager;
import l2server.gameserver.templates.chars.NpcTemplate;

public class ManorManagerInstance extends MerchantInstance {
	public ManorManagerInstance(int objectId, NpcTemplate template) {
		super(objectId, template);
		setInstanceType(InstanceType.L2ManorManagerInstance);
	}
	
	@Override
	public String getHtmlPath(int npcId, int val) {
		return "manormanager/manager.htm";
	}
	
	@Override
	public void showChatWindow(Player player) {
		if (CastleManorManager.getInstance().isDisabled()) {
			showChatWindowByFileName(player, "npcdefault.htm");
			return;
		}
		
		if (!player.isGM() && getCastle() != null && getCastle().getCastleId() > 0 && player.getClan() != null &&
				getCastle().getOwnerId() == player.getClanId() && player.isClanLeader()) {
			showChatWindowByFileName(player, "manormanager/manager-lord.htm");
		} else {
			showChatWindowByFileName(player, "manormanager/manager.htm");
		}
	}
}
