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
import l2server.gameserver.network.serverpackets.StatusUpdate;
import l2server.gameserver.stats.Env;
import l2server.gameserver.templates.skills.L2EffectTemplate;

/**
 * @author Zoey76
 */
public class EffectCpDamPercent extends L2Effect
{
	public EffectCpDamPercent(Env env, L2EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public boolean onActionTime()
	{
		if (getEffected().isDead())
		{
			return false;
		}

		double cp = getEffected().getCurrentCp() * (100 - getAbnormal().getLandRate()) / 100;
		getEffected().setCurrentCp(cp);

		StatusUpdate sucp = new StatusUpdate(getEffected());
		sucp.addAttribute(StatusUpdate.CUR_CP, (int) cp);
		getEffected().sendPacket(sucp);
		return false;
	}
}
