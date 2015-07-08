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
package l2tserver.gameserver.network.serverpackets;

import java.util.Set;

import l2tserver.gameserver.model.actor.instance.L2PcInstance;
import l2tserver.gameserver.stats.VisualEffect;

/**
 * @author Pere
 */
public final class ExUserEffects extends L2GameServerPacket
{
	private int _objectId;
	private int _transformId;
	private Set<Integer> _abnormals;
	
	public ExUserEffects(L2PcInstance character)
	{
		_objectId = character.getObjectId();
		_transformId = character.getTransformationId();
		_abnormals = character.getAbnormalEffect();
		if (character.getAppearance().getInvisible())
			_abnormals.add(VisualEffect.STEALTH.getId());
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xfe);
		writeH(0x158);
		
		writeD(_objectId);
		writeD(_transformId);
		writeD(_abnormals.size());
		for (int abnormalId : _abnormals)
			writeH(abnormalId);
	}
	
	/* (non-Javadoc)
	 * @see l2tserver.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return "ExUserEffects";
	}
}
