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

/**
 * sample
 * <p>
 * 0000: 5a  d8 a8 10 48  d8 a8 10 48  10 04 00 00  01 00 00	Z...H...H.......
 * 0010: 00  f0 1a 00 00  68 28 00 00						 .....h(..
 * <p>
 * format   dddddd dddh (h)
 *
 * @version $Revision: 1.4.2.1.2.4 $ $Date: 2005/03/27 15:29:39 $
 */
public final class MagicSkillUse extends L2GameServerPacket
{
	private int _targetId, _tx, _ty, _tz;
	private int _gauge;
	private int _skillId;
	private int _skillLevel;
	private int _hitTime;
	private int _reuseDelay;
	private int _reuseGroup;
	private int _charObjId, _x, _y, _z;
	private boolean _ground = false;
	private int _groundX, _groundY, _groundZ;
	private int _skillActionId;

	//private int _flags;

	public MagicSkillUse(L2Character cha, L2Character target, int skillId, int skillLevel, int hitTime, int reuseDelay, int reuseGroup, int gauge, int skillActionId)
	{
		this(cha, target, skillId, skillLevel, hitTime, reuseDelay, reuseGroup, gauge, false, skillActionId);
	}

	public MagicSkillUse(L2Character cha, L2Character target, int skillId, int skillLevel, int hitTime, int reuseDelay, int skillActionId)
	{
		this(cha, target, skillId, skillLevel, hitTime, reuseDelay, reuseDelay > 0 ? 0 : -1, 0, skillActionId);
	}

	public MagicSkillUse(L2Character cha, L2Character target, int skillId, int skillLevel, int hitTime, int reuseDelay, int reuseGroup, int gauge, boolean ground, int skillActionId)
	{
		_charObjId = cha.getObjectId();
		_targetId = target.getObjectId();
		_gauge = gauge;
		_skillId = skillId;
		_skillLevel = skillLevel;
		_hitTime = hitTime;
		_reuseDelay = reuseDelay;
		_reuseGroup = reuseGroup;
		_x = cha.getX();
		_y = cha.getY();
		_z = cha.getZ();
		_tx = target.getX();
		_ty = target.getY();
		_tz = target.getZ();
		//_flags |= 0x20;

		_ground = ground;
		if (_ground)
		{
			_groundX = cha.getSkillCastPosition().getX();
			_groundY = cha.getSkillCastPosition().getY();
			_groundZ = _z + 10;
		}

		_skillActionId = skillActionId;
	}

	public MagicSkillUse(L2Character cha, int skillId, int skillLevel, int hitTime, int reuseDelay)
	{
		_charObjId = cha.getObjectId();
		_targetId = cha.getTargetId();
		_gauge = 0;
		_skillId = skillId;
		_skillLevel = skillLevel;
		_hitTime = hitTime;
		_reuseDelay = reuseDelay;
		_reuseGroup = reuseDelay > 0 ? 0 : -1;
		_x = cha.getX();
		_y = cha.getY();
		_z = cha.getZ();
		_tx = cha.getX();
		_ty = cha.getY();
		_tz = cha.getZ();
		//_flags |= 0x20;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_gauge); // Don't show casting bar if 1
		writeD(_charObjId);
		writeD(_targetId);
		writeD(_skillId);
		writeD(_skillLevel);
		writeD(_hitTime);
		writeD(_reuseGroup);
		writeD(_reuseDelay);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeH(0x00);

		if (_ground)
		{
			writeH(0x01);
			writeD(_groundX);
			writeD(_groundY);
			writeD(_groundZ);
		}
		else
		{
			writeH(0x00);
		}

		writeD(_tx);
		writeD(_ty);
		writeD(_tz);

		if (_skillActionId == 0)
		{
			writeD(0x00);
			writeD(0x00);
		}
		else
		{
			writeD(0x01);
			writeD(_skillActionId);
		}
	}
}
