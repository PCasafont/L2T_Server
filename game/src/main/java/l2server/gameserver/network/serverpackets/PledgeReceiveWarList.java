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

import l2server.Config;
import l2server.gameserver.model.L2Clan;
import l2server.gameserver.model.entity.ClanWarManager.ClanWar;
import l2server.gameserver.model.entity.ClanWarManager.ClanWar.WarState;

/**
 * @author -Wooden-
 */
public class PledgeReceiveWarList extends L2GameServerPacket {
	private L2Clan clan;
	@SuppressWarnings("unused")
	private int tab;
	private int scores;
	private int pkedPlayers;
	private int state;
	
	public PledgeReceiveWarList(L2Clan clan, int tab) {
		this.clan = clan;
		this.tab = tab;
	}
	
	@Override
	protected final void writeImpl() {
		writeD(0); // ???
		writeD(clan.getWars().size());
		
		for (ClanWar war : clan.getWars()) {
			L2Clan other = war.getClan1() != clan ? war.getClan1() : war.getClan2();
			
			state = 0;
			if (war.getElapsedTime() >= Config.PREPARE_NORMAL_WAR_PERIOD * 24 * 3600 && war.getState() == WarState.DECLARED) {
				state = 1;
			} else if (war.getState() == WarState.STARTED) {
				state = 2;
			} else if (war.getState() == WarState.REPOSE) {
				if (war.getTie()) {
					state = 5;
				} else if (war.getWinner() == clan) {
					state = 3;
				} else if (war.getLoser() == clan) {
					state = 4;
				}
			}
			
			scores = 0;
			pkedPlayers = 0;
			if (clan == war.getClan1()) {
				scores = war.getClan1Scores();
			} else if (clan == war.getClan2()) {
				scores = war.getClan2Scores();
			}
			
			// Needs confirmation, show only for clan which declared war or for both!
			if (state < 2) {
				pkedPlayers = 5 - war.getClan1DeathsForClanWar();
			}
			
			writeS(other.getName());
			writeD(state); // 0: Declaration; 1: Blood Declaration; 2: At war; 3: Victory; 4: Defeat; 5: Tie
			writeD(war.getElapsedTime()); // Time elapsed in seconds
			writeD(scores); // Scores.
			writeD(scores); // Scores in information. Needs confirmation if this is true scores in information.
			writeD(pkedPlayers); // Players PK'ed by other clan.
		}
	}
}
