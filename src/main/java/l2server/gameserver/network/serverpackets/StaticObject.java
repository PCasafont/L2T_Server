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

import l2server.gameserver.model.actor.instance.L2DoorInstance;
import l2server.gameserver.model.actor.instance.L2StaticObjectInstance;

/**
 * @author KenM
 */
public class StaticObject extends L2GameServerPacket
{
	private final int staticObjectId;
	private final int objectId;
	private final int type;
	private final boolean isTargetable;
	private final int meshIndex;
	private final boolean isClosed;
	private final boolean isEnemy;
	private final int maxHp;
	private final int currentHp;
	private final boolean showHp;
	private final int damageGrade;

	public StaticObject(L2StaticObjectInstance staticObject)
	{
		staticObjectId = staticObject.getStaticObjectId();
		objectId = staticObject.getObjectId();
		type = 0;
		isTargetable = true;
		meshIndex = staticObject.getMeshIndex();
		isClosed = false;
		isEnemy = false;
		maxHp = 0;
		currentHp = 0;
		showHp = false;
		damageGrade = 0;
	}

	public StaticObject(L2DoorInstance door, boolean targetable)
	{
		staticObjectId = door.getDoorId();
		objectId = door.getObjectId();
		type = 1;
		isTargetable = door.isTargetable() || targetable;
		meshIndex = door.getMeshIndex();
		isClosed = !door.getOpen();
		isEnemy = door.isEnemy();
		maxHp = door.getMaxVisibleHp();
		currentHp = (int) door.getCurrentHp();
		showHp = door.getIsShowHp();
		damageGrade = door.getDamage();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(staticObjectId);
		writeD(objectId);
		writeD(type);
		writeD(isTargetable ? 1 : 0);
		writeD(meshIndex);
		writeD(isClosed ? 1 : 0);
		writeD(isEnemy ? 1 : 0);
		writeD(currentHp);
		writeD(maxHp);
		writeD(showHp ? 1 : 0);
		writeD(damageGrade);
	}
}
