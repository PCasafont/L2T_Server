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

package l2server.gameserver.network.clientpackets;

import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.olympiad.HeroesManager;

/**
 * Format chS
 * c (id) 0xD0
 * h (subid) 0x0C
 * S the hero's words :)
 *
 * @author -Wooden-
 */
public final class RequestWriteHeroWords extends L2GameClientPacket {
	
	private String heroWords;
	
	/**
	 */
	@Override
	protected void readImpl() {
		heroWords = readS();
	}
	
	@Override
	protected void runImpl() {
		final Player player = getClient().getActiveChar();
		if (player == null || !player.isHero()) {
			return;
		}
		
		if (heroWords == null || heroWords.length() > 300) {
			return;
		}
		
		HeroesManager.getInstance().setHeroMessage(player, heroWords);
	}
}
