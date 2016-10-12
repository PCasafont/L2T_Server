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
public enum L2SkillTargetType
{
	TARGET_NONE,
	TARGET_ITEM,
	TARGET_AREA_CORPSE_MOB,
	TARGET_UNLOCKABLE,
	TARGET_FRIEND_NOTME,
	TARGET_ENEMY_SUMMON,
	TARGET_OWNER_PET,
	TARGET_GROUND,
	TARGET_AROUND_CORPSE_MOB,
	// Used for fail server Dreams

	/**
	 * Targets Rework
	 **/
	TARGET_SINGLE,
	TARGET_MY_SUMMON,
	TARGET_AROUND_CASTER,
	TARGET_AROUND_TARGET,
	TARGET_FRIENDS,
	TARGET_SPECIAL,
	TRAP_TARGET,
	HERB_TARGET,
	/**
	 * TODO: Remove that shit once target rework is completed.
	 **/
	TARGET_SELF,
	// Replaced by: <set name="target" val="TARGET_SINGLE" /> | <set name="targetDirection" val="ME" />
	TARGET_ONE,
	// Replaced by: <set name="target" val="TARGET_SINGLE" />
	TARGET_PARTY,
	// Replaced by: <set name="target" val="TARGET_FRIENDS" /> | <set name="targetDirection" val="PARTY_ALL" />
	TARGET_PARTY_ANYWHERE,
	// dunno what to write there...just as target party but without any range check o.o (used by gate chant)

	// NOT PRESENT??
	TARGET_ALLY,
	// Replaced by: <set name="target" val="TARGET_FRIENDS" /> | <set name="targetDirection" val="ALLIANCE" />

	TARGET_CLAN,
	// Replaced by: <set name="target" val="TARGET_FRIENDS" /> | <set name="targetDirection" val="CLAN" />

	// NOT PRESENT??
	TARGET_CLANPARTY,
	// Replaced by: <set name="target" val="TARGET_FRIENDS" /> | <set name="targetDirection" val="PARTY_AND_CLAN" />

	TARGET_AREA,
	// Replaced by: <set name="target" val="TARGET_AROUND_TARGET" /> | <set name="targetDirection" val="AROUND" />
	TARGET_FRONT_AREA,
	// Replaced by: <set name="target" val="TARGET_AROUND_TARGET" /> | <set name="targetDirection" val="FRONT" />

	// NOT PRESENT??
	TARGET_BEHIND_AREA,
	// Replaced by: <set name="target" val="TARGET_AROUND_TARGET" /> | <set name="targetDirection" val="BEHIND" />

	// NOT PRESENT??
	TARGET_MULTIFACE,
	// Replaced by: <set name="target" val="TARGET_AROUND_TARGET" /> | <set name="targetDirection" val="FRONT" />

	TARGET_AURA,
	// Replaced by: <set name="target" val="TARGET_AROUND_CASTER" /> | <set name="targetDirection" val="AROUND" />
	TARGET_FRONT_AURA,
	// Replaced by: <set name="target" val="TARGET_AROUND_CASTER" /> | <set name="targetDirection" val="FRONT" />
	TARGET_BEHIND_AURA,
	// Replaced by: <set name="target" val="TARGET_AROUND_CASTER" /> | <set name="targetDirection" val="BEHIND" />
	TARGET_PARTY_MEMBER,
	// Replaced by: <set name="target" val="TARGET_FRIENDS" /> | <set name="targetDirection" val="PARTY_ONE" />
	TARGET_PARTY_OTHER,
	// Replaced by: <set name="target" val="TARGET_FRIENDS" /> | <set name="targetDirection" val="PARTY_ONE_NOTME" />

	// NOT PRESENT??
	TARGET_PARTY_OTHERS,
	// Replaced by: <set name="target" val="TARGET_FRIENDS" /> | <set name="targetDirection" val="PARTY_ALL_NOTME" />

	// NOT REPLACED
	TARGET_UNDEAD,
	// Replaced by: <set name="target" val="TARGET_SINGLE" /> / Target Undead Condition.

	TARGET_AREA_UNDEAD,
	// Replaced by: <set name="target" val="TARGET_AROUND_TARGET" /> / <set name="targetDirection" val="TARGET_UNDEAD" />
	TARGET_PET,
	TARGET_SUMMON,
	TARGET_MONSTER,
	TARGET_CORPSE,
	TARGET_CORPSE_ALLY,
	TARGET_CORPSE_CLAN,
	TARGET_CORPSE_PLAYER,
	TARGET_CORPSE_PET,
	TARGET_CORPSE_MOB,
	TARGET_AREA_SUMMON,
	TARGET_CHAIN_HEAL,
	TARGET_AREA_CORPSE,
	// Used for custom area sweep

	/*
	TARGET_PVP,
	TARGET_ALL_SUMMONS,
	TARGET_PVP_AREA,
	TARGET_PVP_AURA,*/
	TARGET_CORPSE_PARTY_CLAN,
	TARGET_CORPSE_PARTY,
	TARGET_AURA_CORPSE_MOB,
	TARGET_HOLY,
	TARGET_FLAGPOLE,
	TARGET_PARTY_CLAN,
	TARGET_GROUND_AREA,
	TARGET_PARTY_NOTME,
	TARGET_ALLY_NOTME,
	TARGET_CLAN_MEMBER,
	TARGET_EVENT,
	TARGET_MENTEE,
	TARGET_LINE
}
