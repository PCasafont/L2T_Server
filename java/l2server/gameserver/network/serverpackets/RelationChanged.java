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

import l2server.gameserver.model.actor.L2Playable;

/**
 * @author Luca Baldi
 */
public final class RelationChanged extends L2GameServerPacket
{
	public static final int RELATION_PARTY1 = 0x00001; // party member
	public static final int RELATION_PARTY2 = 0x00002; // party member
	public static final int RELATION_PARTY3 = 0x00004; // party member
	public static final int RELATION_PARTY4 = 0x00008; // party member (for information, see L2PcInstance.getRelation())
	public static final int RELATION_PARTYLEADER = 0x00010; // true if is party leader
	public static final int RELATION_HAS_PARTY = 0x00020; // true if is in party
	public static final int RELATION_CLAN_MEMBER = 0x00040; // true if is in clan
	public static final int RELATION_LEADER = 0x00080; // true if is clan leader
	public static final int RELATION_CLAN_MATE = 0x00100; // true if is in same clan
	public static final int RELATION_INSIEGE = 0x00200; // true if in siege
	public static final int RELATION_ATTACKER = 0x00400; // true when attacker
	public static final int RELATION_ALLY = 0x00800; // blue siege icon, cannot have if red
	public static final int RELATION_ENEMY = 0x01000; // true when red icon, doesn't matter with blue
	public static final int RELATION_WAR_STARTED = 0x04000; // double fist
	public static final int RELATION_WAR_ABOUT_TO_BEGIN = 0x08000; // single fist
	public static final int RELATION_ALLY_MEMBER = 0x10000; // clan is in alliance

	private static class Relation
	{
		int _objId, _relation, _autoAttackable, _reputation, _pvpFlag;
	}

	private Relation _relation;

	public RelationChanged(L2Playable activeChar, int relation, boolean autoattackable)
	{
		_relation = new Relation();
		_relation._objId = activeChar.getObjectId();
		_relation._relation = relation;
		_relation._autoAttackable = autoattackable ? 1 : 0;
		_relation._reputation = activeChar.getReputation();
		_relation._pvpFlag = activeChar.getPvpFlag();
		_invisibleCharacter = activeChar.getActingPlayer().getAppearance().getInvisible() ?
				activeChar.getActingPlayer().getObjectId() : 0;
	}

	/**
	 */
	@Override
	protected final void writeImpl()
	{
		writeC(2);
		writeD(_relation._objId);
		writeD(_relation._relation);
		writeC(_relation._autoAttackable);
		writeD(_relation._reputation);
		writeC(_relation._pvpFlag);
	}
}
