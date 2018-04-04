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
import l2server.gameserver.model.Abnormal;
import l2server.gameserver.model.L2Effect;
import l2server.gameserver.model.Location;
import l2server.gameserver.network.serverpackets.FlyToLocation;
import l2server.gameserver.network.serverpackets.FlyToLocation.FlyType;
import l2server.gameserver.network.serverpackets.ValidateLocation;
import l2server.gameserver.stats.Env;
import l2server.gameserver.templates.skills.AbnormalType;
import l2server.gameserver.templates.skills.EffectTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Pere
 */
public class EffectMagicPull extends L2Effect {
	private static Logger log = LoggerFactory.getLogger(EffectMagicPull.class.getName());


	private int x, y, z;

	public EffectMagicPull(Env env, EffectTemplate template) {
		super(env, template);
	}

	@Override
	public AbnormalType getAbnormalType() {
		return AbnormalType.PULL;
	}

	/**
	 * @see Abnormal#onStart()
	 */
	@Override
	public boolean onStart() {
		// Get current position of the Creature
		final int curX = getEffected().getX();
		final int curY = getEffected().getY();
		final int curZ = getEffected().getZ();

		// Calculate distance between effector and effected current position
		double dx = getEffector().getX() - curX;
		double dy = getEffector().getY() - curY;
		double dz = getEffector().getZ() - curZ;
		double distance = Math.sqrt(dx * dx + dy * dy);
		if (distance > 2000) {
			log.info("EffectMagicDrag (skill id: " + getSkill().getId() + ") was going to use invalid coordinates for characters, getEffected: " +
					curX + "," + curY + " and getEffector: " + getEffector().getX() + "," + getEffector().getY());
			return false;
		}

		int offset = Math.min((int) distance, 100);

		double cos;
		double sin;

		// approximation for moving futher when z coordinates are different
		// TODO: handle Z axis movement better
		offset += Math.abs(dz);
		if (offset < 5) {
			offset = 5;
		}

		// If no distance
		if (distance < 1) {
			return false;
		}

		// Calculate movement angles needed
		sin = dy / distance;
		cos = dx / distance;

		// Calculate the new destination with offset included
		x = curX + (int) (offset * cos);
		y = curY + (int) (offset * sin);
		z = curZ;

		if (Config.GEODATA > 0) {
			Location destiny = GeoData.getInstance()
					.moveCheck(getEffected().getX(), getEffected().getY(), getEffected().getZ(), x, y, z, getEffected().getInstanceId());
			if (destiny.getX() != x || destiny.getY() != y) {
				x = destiny.getX() - (int) (cos * 10);
				y = destiny.getY() - (int) (sin * 10);
			}
		}
		getEffected().broadcastPacket(new FlyToLocation(getEffected(), x, y, z, FlyType.DRAG));
		getEffected().setXYZ(x, y, z);
		getEffected().broadcastPacket(new ValidateLocation(getEffected()));
		return true;
	}

	/**
	 * @see Abnormal#onActionTime()
	 */
	@Override
	public boolean onActionTime() {
		return false;
	}

	/**
	 * @see Abnormal#onExit()
	 */
	@Override
	public void onExit() {
	}
}
