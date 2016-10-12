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

package l2server.gameserver.model.zone.type;

import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.zone.L2ZoneType;
import l2server.gameserver.stats.VisualEffect;

/**
 * L2AbnormalZone zones give entering players abnormal effects
 * Default effect is big head
 *
 * @author durgus
 */
public class L2AbnormalZone extends L2ZoneType
{
	private int abnormal = VisualEffect.BIG_HEAD.getId();

	public L2AbnormalZone(int id)
	{
		super(id);
	}

	@Override
	public void setParameter(String name, String value)
	{
		switch (name)
		{
			case "AbnormalMask":
				abnormal = Integer.parseInt(value);
				break;
			case "SpecialMask":
				abnormal = Integer.parseInt(value);
				break;
			default:
				super.setParameter(name, value);
				break;
		}
	}

	@Override
	protected void onEnter(L2Character character)
	{
		character.startVisualEffect(abnormal);
	}

	@Override
	protected void onExit(L2Character character)
	{
		character.stopVisualEffect(abnormal);
	}

	@Override
	public void onDieInside(L2Character character, L2Character killer)
	{
		onExit(character);
	}

	@Override
	public void onReviveInside(L2Character character)
	{
		onEnter(character);
	}
}
