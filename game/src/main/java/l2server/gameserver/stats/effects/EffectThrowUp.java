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
import l2server.gameserver.model.actor.Attackable;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.serverpackets.FlyToLocation;
import l2server.gameserver.network.serverpackets.FlyToLocation.FlyType;
import l2server.gameserver.network.serverpackets.ValidateLocation;
import l2server.gameserver.stats.Env;
import l2server.gameserver.templates.skills.EffectTemplate;
import l2server.util.Rnd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EffectThrowUp extends L2Effect {
	private static Logger log = LoggerFactory.getLogger(EffectThrowUp.class.getName());

	private int x, y, z;

	public EffectThrowUp(Env env, EffectTemplate template) {
		super(env, template);
	}

	/**
	 * @see Abnormal#onStart()
	 */
	@Override
	public boolean onStart() {
		if (getEffected() instanceof Attackable && getEffected().isImmobilized() || getEffected().isRaid()) {
			return false;
		}

		//TW bug restrictions for avoid players with TW flags stuck his char into the walls, under live test
		if (getEffected() instanceof Player && ((Player) getEffected()).isCombatFlagEquipped()) {
			return false;
		}

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
			log.info(
					"EffectThrowUp (skill id: " + getSkill().getId() + ") was going to use invalid coordinates for characters, getEffected: " + curX +
							"," + curY + " and getEffector: " + getEffector().getX() + "," + getEffector().getY());
			return false;
		}
		int offset = Math.min((int) distance + getSkill().getFlyRadius(), 1400);

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

		if (getSkill().getFlyRadius() == -1) {
			x = getEffector().getX() + Rnd.get(10);
			y = getEffector().getY() + Rnd.get(10);
			z = getEffector().getZ() + 5;
			if (Config.GEODATA > 0) {
				Location destiny = GeoData.getInstance()
						.moveCheck(getEffected().getX(), getEffected().getY(), getEffected().getZ(), x, y, z, getEffected().getInstanceId());
				x = destiny.getX();
				y = destiny.getY();
			}
		} else {
			// Calculate the new destination with offset included
			x = getEffector().getX() - (int) (offset * cos);
			y = getEffector().getY() - (int) (offset * sin);
			z = getEffected().getZ();

			if (Config.GEODATA > 0) {
				Location destiny = GeoData.getInstance()
						.moveCheck(getEffected().getX(), getEffected().getY(), getEffected().getZ(), x, y, z, getEffected().getInstanceId());
				if (destiny.getX() != x || destiny.getY() != y) {
					x = destiny.getX() + (int) (cos * 10);
					y = destiny.getY() + (int) (sin * 10);
				}
			}
		}

		getEffected().startStunning();
		getEffected().broadcastPacket(new FlyToLocation(getEffected(), x, y, z, FlyType.THROW_UP));
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
		getEffected().stopStunning(false);
		getEffected().setXYZ(x, y, z);
		getEffected().broadcastPacket(new ValidateLocation(getEffected()));
	}
}
