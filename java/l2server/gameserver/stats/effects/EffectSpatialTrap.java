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

package l2server.gameserver.stats.effects;

import l2server.gameserver.model.L2Effect;
import l2server.gameserver.stats.Env;
import l2server.gameserver.templates.skills.L2AbnormalType;
import l2server.gameserver.templates.skills.L2EffectTemplate;

/**
 * @author ZaKaX
 */
public class EffectSpatialTrap extends L2Effect
{
	private int _trapX;
	private int _trapY;
	private int _trapZ;

	public EffectSpatialTrap(Env env, L2EffectTemplate template)
	{
		super(env, template);
	}

	public EffectSpatialTrap(Env env, L2Effect effect)
	{
		super(env, effect);
	}

	@Override
	public L2AbnormalType getAbnormalType()
	{
		return L2AbnormalType.SPATIAL_TRAP;
	}

	/**
	 * @see l2server.gameserver.model.L2Abnormal#onStart()
	 */
	@Override
	public boolean onStart()
	{
		_trapX = getEffector().getX();
		_trapY = getEffector().getY();
		_trapZ = getEffector().getZ();

		return true;
	}

	/**
	 * @see l2server.gameserver.model.L2Abnormal#onActionTime()
	 */
	@Override
	public boolean onActionTime()
	{
		return true;
	}

	public final int getTrapX()
	{
		return _trapX;
	}

	public final int getTrapY()
	{
		return _trapY;
	}

	public final int getTrapZ()
	{
		return _trapZ;
	}
}
