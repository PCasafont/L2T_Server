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

import l2server.Config;
import l2server.gameserver.GeoData;
import l2server.gameserver.model.L2Effect;
import l2server.gameserver.model.Location;
import l2server.gameserver.network.serverpackets.FlyToLocation;
import l2server.gameserver.network.serverpackets.FlyToLocation.FlyType;
import l2server.gameserver.network.serverpackets.ValidateLocation;
import l2server.gameserver.stats.Env;
import l2server.gameserver.templates.skills.L2AbnormalType;
import l2server.gameserver.templates.skills.L2EffectTemplate;
import l2server.log.Log;

/**
 * @author Pere
 */
public class EffectMagicPull extends L2Effect
{
	private int _x, _y, _z;

	public EffectMagicPull(Env env, L2EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2AbnormalType getAbnormalType()
	{
		return L2AbnormalType.PULL;
	}

	/**
	 * @see l2server.gameserver.model.L2Abnormal#onStart()
	 */
	@Override
	public boolean onStart()
	{
		// Get current position of the L2Character
		final int curX = getEffected().getX();
		final int curY = getEffected().getY();
		final int curZ = getEffected().getZ();

		// Calculate distance between effector and effected current position
		double dx = getEffector().getX() - curX;
		double dy = getEffector().getY() - curY;
		double dz = getEffector().getZ() - curZ;
		double distance = Math.sqrt(dx * dx + dy * dy);
		if (distance > 2000)
		{
			Log.info("EffectMagicDrag (skill id: " + getSkill().getId() +
					") was going to use invalid coordinates for characters, getEffected: " + curX + "," + curY +
					" and getEffector: " + getEffector().getX() + "," + getEffector().getY());
			return false;
		}

		int offset = Math.min((int) distance, 100);

		double cos;
		double sin;

		// approximation for moving futher when z coordinates are different
		// TODO: handle Z axis movement better
		offset += Math.abs(dz);
		if (offset < 5)
		{
			offset = 5;
		}

		// If no distance
		if (distance < 1)
		{
			return false;
		}

		// Calculate movement angles needed
		sin = dy / distance;
		cos = dx / distance;

		// Calculate the new destination with offset included
		_x = curX + (int) (offset * cos);
		_y = curY + (int) (offset * sin);
		_z = curZ;

		if (Config.GEODATA > 0)
		{
			Location destiny = GeoData.getInstance()
					.moveCheck(getEffected().getX(), getEffected().getY(), getEffected().getZ(), _x, _y, _z,
							getEffected().getInstanceId());
			if (destiny.getX() != _x || destiny.getY() != _y)
			{
				_x = destiny.getX() - (int) (cos * 10);
				_y = destiny.getY() - (int) (sin * 10);
			}
		}
		getEffected().broadcastPacket(new FlyToLocation(getEffected(), _x, _y, _z, FlyType.DRAG));
		getEffected().setXYZ(_x, _y, _z);
		getEffected().broadcastPacket(new ValidateLocation(getEffected()));
		return true;
	}

	/**
	 * @see l2server.gameserver.model.L2Abnormal#onActionTime()
	 */
	@Override
	public boolean onActionTime()
	{
		return false;
	}

	/**
	 * @see l2server.gameserver.model.L2Abnormal#onExit()
	 */
	@Override
	public void onExit()
	{
	}
}
