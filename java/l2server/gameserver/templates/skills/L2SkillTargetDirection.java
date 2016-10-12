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

package l2server.gameserver.templates.skills;

/**
 * @author Max
 */
public enum L2SkillTargetDirection
{
	DEFAULT,
	// General Skills
	SINGLE,
	AROUND,
	BEHIND,
	FRONT,
	UNDEAD,
	DEAD_PLAYABLE,
	DEAD_PARTY_MEMBER,
	DEAD_CLAN_MEMBER,
	DEAD_ALLY_MEMBER,
	DEAD_PARTY_AND_CLAN_MEMBER,
	DEAD_PET,
	DEAD_MONSTER,
	ONE_NOT_SUMMONS,
	ENNEMY_SUMMON,
	ALL_SUMMONS,
	MONSTERS,
	// Friendly Skills
	PARTY_ONE,
	PARTY_ONE_NOTME,
	PARTY_ALL,
	PARTY_ALL_NOTME,
	PARTY_AND_CLAN,
	CLAN,
	ALLIANCE,
	// Special Skills
	SUBLIMES,
	PARTY_ANYWHERE,
	INVISIBLE_TRAP,
	CHAIN_HEAL,

	ME,
	// ????
	PLAYER, // PVP-ONLY SKILL
}
