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

import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.stats.VisualEffect;

import java.util.Set;

/**
 * @author Pere
 */
public final class ExUserEffects extends L2GameServerPacket
{
	private int objectId;
	private int transformId;
	private Set<Integer> abnormals;

	public ExUserEffects(L2PcInstance character)
	{
		this.objectId = character.getObjectId();
		this.transformId = character.getTransformationId();
		this.abnormals = character.getAbnormalEffect();
		if (character.getAppearance().getInvisible())
		{
			this.abnormals.add(VisualEffect.STEALTH.getId());
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeD(this.objectId);
		writeD(this.transformId);
		writeD(this.abnormals.size());
		for (int abnormalId : this.abnormals)
		{
			writeH(abnormalId);
		}
	}
}
