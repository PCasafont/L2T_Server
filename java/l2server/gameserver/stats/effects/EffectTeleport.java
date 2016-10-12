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
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.model.L2Effect;
import l2server.gameserver.model.Location;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.network.serverpackets.FlyToLocation;
import l2server.gameserver.network.serverpackets.FlyToLocation.FlyType;
import l2server.gameserver.network.serverpackets.ValidateLocation;
import l2server.gameserver.stats.Env;
import l2server.gameserver.templates.skills.L2EffectTemplate;
import l2server.gameserver.util.Util;

/**
 * This class handles warp effects, disappear and quickly turn up in a near
 * location. If geodata enabled and an object is between initial and final
 * point, flight is stopped just before colliding with object. Flight course and
 * radius are set as skill properties (flyCourse and flyRadius):
 * <p>
 * <li>Fly Radius means the distance between starting point and final point, it
 * must be an integer.</li> <li>Fly Course means the movement direction: imagine
 * a compass above player's head, making north player's heading. So if fly
 * course is 180, player will go backwards (good for blink, e.g.). By the way,
 * if flyCourse = 360 or 0, player will be moved in in front of him. <br>
 * <br>
 * <p>
 * If target is effector, put in XML self = "1". This will make _actor =
 * getEffector(). This, combined with target type, allows more complex actions
 * like flying target's backwards or player's backwards.<br>
 * <br>
 *
 * @author House
 */
public class EffectTeleport extends L2Effect
{
	private L2Character _actor;

	public EffectTeleport(Env env, L2EffectTemplate template)
	{
		super(env, template);
	}

	/**
	 * @see l2server.gameserver.model.L2Abnormal#onStart()
	 */
	@Override
	public boolean onStart()
	{
		_actor = getAbnormal().isSelfEffect() ? getEffector() : getEffected();

		if (_actor.isMovementDisabled())
		{
			return false;
		}

		int radius = getSkill().getFlyRadius();

		double angle = Util.convertHeadingToDegree(_actor.getHeading());
		double radian = Math.toRadians(angle);
		double course = Math.toRadians(getSkill().getFlyCourse());

		float x1 = (float) Math.cos(Math.PI + radian + course);
		float y1 = (float) Math.sin(Math.PI + radian + course);

		int x = _actor.getX() + (int) (x1 * radius);
		int y = _actor.getY() + (int) (y1 * radius);
		int z = _actor.getZ();

		if (Config.GEODATA > 0)
		{
			Location destiny = GeoData.getInstance()
					.moveCheck(_actor.getX(), _actor.getY(), _actor.getZ(), x, y, z, _actor.getInstanceId());
			if (destiny.getX() != x || destiny.getY() != y)
			{
				x = destiny.getX() - (int) (x1 * 10);
				y = destiny.getY() - (int) (y1 * 10);
				z = destiny.getZ();
			}
		}

		// TODO: check if this AI intention is retail-like. This stops player's
		// previous movement
		_actor.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);

		_actor.broadcastPacket(new FlyToLocation(_actor, x, y, z, FlyType.MAGIC));

		_actor.setXYZ(x, y, z);
		_actor.broadcastPacket(new ValidateLocation(_actor));

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
}
