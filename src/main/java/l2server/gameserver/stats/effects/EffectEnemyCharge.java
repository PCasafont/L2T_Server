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
import l2server.gameserver.templates.skills.AbnormalType;
import l2server.gameserver.templates.skills.EffectTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EffectEnemyCharge extends L2Effect {
	private static Logger log = LoggerFactory.getLogger(EffectEnemyCharge.class.getName());


	private int x, y, z;
	
	public EffectEnemyCharge(Env env, EffectTemplate template) {
		super(env, template);
	}
	
	@Override
	public AbnormalType getAbnormalType() {
		return AbnormalType.BUFF;
	}
	
	@Override
	public boolean onStart() {
		// Get current position of the Creature
		final int curX = getEffector().getX();
		final int curY = getEffector().getY();
		final int curZ = getEffector().getZ();
		
		// Calculate distance (dx,dy) between current position and destination
		double dx = getEffected().getX() - curX;
		double dy = getEffected().getY() - curY;
		double dz = getEffected().getZ() - curZ;
		double distance = Math.sqrt(dx * dx + dy * dy);
		if (distance > 2000) {
			log.info("EffectEnemyCharge was going to use invalid coordinates for characters, getEffector: " + curX + "," + curY +
					" and getEffected: " + getEffected().getX() + "," + getEffected().getY());
			return false;
		}
		int offset = Math.max((int) distance - getSkill().getFlyRadius(), 30);
		
		double cos;
		double sin;
		
		// approximation for moving closer when z coordinates are different
		// TODO: handle Z axis movement better
		offset -= Math.abs(dz);
		if (offset < 5) {
			offset = 5;
		}
		
		// If no distance
		if (distance < 1 || distance - offset <= 0) {
			return false;
		}
		
		// Calculate movement angles needed
		sin = dy / distance;
		cos = dx / distance;
		
		// Calculate the new destination with offset included
		x = curX + (int) ((distance - offset) * cos);
		y = curY + (int) ((distance - offset) * sin);
		z = getEffected().getZ();
		
		if (Config.GEODATA > 0) {
			Location destiny = GeoData.getInstance()
					.moveCheck(getEffector().getX(), getEffector().getY(), getEffector().getZ(), x, y, z, getEffector().getInstanceId());
			if (destiny.getX() != x || destiny.getY() != y) {
				x = destiny.getX() - (int) (cos * 10);
				y = destiny.getY() - (int) (sin * 10);
			}
		}
		getEffector().broadcastPacket(new FlyToLocation(getEffector(), x, y, z, FlyType.CHARGE));
		
		// maybe is need force set X,Y,Z
		getEffector().setXYZ(x, y, z);
		getEffector().broadcastPacket(new ValidateLocation(getEffector()));
		
		return true;
	}
	
	@Override
	public boolean onActionTime() {
		return false;
	}
}
