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

import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2StatueInstance;

/**
 * @author devScarlet & mrTJO
 */
public final class ServerObjectInfo extends L2GameServerPacket
{
	private L2Npc _activeChar;
	private int _x, _y, _z, _heading;
	private int _idTemplate;
	private boolean _isAttackable;
	private double _collisionHeight, _collisionRadius;
	private String _name;
	private int _type;

	public ServerObjectInfo(L2Npc activeChar, L2Character actor)
	{
		_activeChar = activeChar;
		_idTemplate = _activeChar.getTemplate().TemplateId + 1000000;
		_isAttackable = _activeChar.isAutoAttackable(actor);
		_collisionHeight = _activeChar.getCollisionHeight();
		_collisionRadius = _activeChar.getCollisionRadius();
		_x = _activeChar.getX();
		_y = _activeChar.getY();
		_z = _activeChar.getZ();
		_heading = _activeChar.getHeading();
		_name = _activeChar.getTemplate().ServerSideName ? _activeChar.getTemplate().Name : "";
		_type = 4;

		if (_activeChar instanceof L2StatueInstance)
		{
			_idTemplate = 0;
			_name = _activeChar.getName();
			_isAttackable = false;
			_collisionHeight = 30;
			_collisionRadius = 40;
			_type = 7;
		}
	}

	/**
	 * @see l2server.gameserver.network.serverpackets.L2GameServerPacket#writeImpl()
	 */
	@Override
	protected final void writeImpl()
	{
		writeD(_activeChar.getObjectId());
		writeD(_idTemplate);
		writeS(_name); // name
		writeD(_isAttackable ? 1 : 0);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeD(_heading);
		writeF(1.0); // movement multiplier
		writeF(1.0); // attack speed multiplier
		writeF(_collisionRadius);
		writeF(_collisionHeight);
		writeD((int) (_isAttackable ? _activeChar.getCurrentHp() : 0));
		writeD(_isAttackable ? _activeChar.getMaxVisibleHp() : 0);
		writeD(_type); // object type
		writeD(0x00); // special effects

		if (_type == 7)
		{
			L2StatueInstance statue = (L2StatueInstance) _activeChar;
			writeD(statue.getRecordId());
			writeD(0x00); // ???
			writeD(statue.getSocialId());
			writeD(statue.getSocialFrame());
			writeD(statue.getClassId());
			writeD(statue.getRace());
			writeD(statue.getSex());
			writeD(statue.getHairStyle());
			writeD(statue.getHairColor());
			writeD(statue.getFace());
			writeD(statue.getNecklace());
			writeD(statue.getHead());
			writeD(statue.getRHand());
			writeD(statue.getLHand());
			writeD(statue.getGloves());
			writeD(statue.getChest());
			writeD(statue.getPants());
			writeD(statue.getBoots());
			writeD(statue.getCloak());
			writeD(statue.getHair1());
			writeD(statue.getHair2());
		}
	}
}
