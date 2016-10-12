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

package l2server.gameserver.model.entity;

import l2server.gameserver.model.L2Clan;
import l2server.gameserver.model.L2SiegeClan;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;

import java.util.Calendar;
import java.util.List;

/**
 * @author JIV
 */
public interface Siegable
{
	void startSiege();

	void endSiege();

	L2SiegeClan getAttackerClan(int clanId);

	L2SiegeClan getAttackerClan(L2Clan clan);

	List<L2SiegeClan> getAttackerClans();

	List<L2PcInstance> getAttackersInZone();

	boolean checkIsAttacker(L2Clan clan);

	L2SiegeClan getDefenderClan(int clanId);

	L2SiegeClan getDefenderClan(L2Clan clan);

	List<L2SiegeClan> getDefenderClans();

	boolean checkIsDefender(L2Clan clan);

	List<L2Npc> getFlag(L2Clan clan);

	Calendar getSiegeDate();

	boolean giveFame();

	int getFameFrequency();

	int getFameAmount();
}
