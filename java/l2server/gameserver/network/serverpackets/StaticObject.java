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
		this.staticObjectId = staticObject.getStaticObjectId();
		this.objectId = staticObject.getObjectId();
		this.type = 0;
		this.isTargetable = true;
		this.meshIndex = staticObject.getMeshIndex();
		this.isClosed = false;
		this.isEnemy = false;
		this.maxHp = 0;
		this.currentHp = 0;
		this.showHp = false;
		this.damageGrade = 0;
	}

	public StaticObject(L2DoorInstance door, boolean targetable)
	{
		this.staticObjectId = door.getDoorId();
		this.objectId = door.getObjectId();
		this.type = 1;
		this.isTargetable = door.isTargetable() || targetable;
		this.meshIndex = door.getMeshIndex();
		this.isClosed = !door.getOpen();
		this.isEnemy = door.isEnemy();
		this.maxHp = door.getMaxVisibleHp();
		this.currentHp = (int) door.getCurrentHp();
		this.showHp = door.getIsShowHp();
		this.damageGrade = door.getDamage();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(this.staticObjectId);
		writeD(this.objectId);
		writeD(this.type);
		writeD(this.isTargetable ? 1 : 0);
		writeD(this.meshIndex);
		writeD(this.isClosed ? 1 : 0);
		writeD(this.isEnemy ? 1 : 0);
		writeD(this.currentHp);
		writeD(this.maxHp);
		writeD(this.showHp ? 1 : 0);
		writeD(this.damageGrade);
	}
}
