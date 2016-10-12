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
 * @author Pere
 */
public enum L2EffectType
{
	NONE(0x0000000000000000L),
	RELAXING(0x0000000000000001L),
	STUN(0x0000000000000002L),
	ROOT(0x0000000000000004L),
	SLEEP(0x0000000000000008L),
	FAKE_DEATH(0x0000000000000010L),
	CONFUSION(0x0000000000000020L),
	MUTE(0x0000000000000040L),
	FEAR(0x0000000000000080L),
	SILENT_MOVE(0x0000000000000100L),
	PARALYZE(0x0000000000000200L),
	PHYSICAL_MUTE(0x0000000000000400L),
	PHYSICAL_ATTACK_MUTE(0x0000000000000800L),
	BETRAY(0x0000000000001000L),
	NOBLESSE_BLESSING(0x0000000000002000L),
	PHOENIX_BLESSING(0x0000000000004000L),
	CHARM_OF_LUCK(0x0000000000008000L),
	INVINCIBLE(0x0000000000010000L),
	DISARM(0x0000000000020000L),
	CHARMOFCOURAGE(0x0000000000040000L),
	PROTECTION_BLESSING(0x0000000000080000L),
	BLOCK_RESURRECTION(0x0000000000100000L),
	NEVITS_BLESSING(0x0000000000200000L),
	UNTARGETABLE(0x0000000000400000L),
	LOVE(0x0000000000800000L),
	DOUBLE_CASTING(0x0000000001000000L),
	DISARM_ARMOR(0x0000000002000000L),
	SPATIAL_TRAP(0x0000000004000000L),
	PETRIFY(0x0000000008000000L),
	BLOCK_INVUL(0x0000000010000000L),
	BLOCK_HIDE(0x0000000020000000L),
	BLOCK_TALISMANS(0x0000000040000000L);

	private long _mask;

	L2EffectType(long mask)
	{
		_mask = mask;
	}

	public long getMask()
	{
		return _mask;
	}
}
