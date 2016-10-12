/* This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */

package l2server.gameserver.model.actor.position;

import l2server.gameserver.model.L2WorldRegion;
import l2server.gameserver.model.actor.L2Character;

/**
 * @author Erb
 */
public class CharPosition extends ObjectPosition
{
	// =========================================================
	// Constructor
	public CharPosition(L2Character activeObject)
	{
		super(activeObject);
	}

	@Override
	protected void badCoords()
	{
		getActiveObject().decayMe();
	}

	@Override
	public final void setWorldRegion(L2WorldRegion value)
	{
		if (getWorldRegion() != null &&
				getActiveObject() instanceof L2Character) // confirm revalidation of old region's zones
		{
			if (value != null)
			{
				getWorldRegion().revalidateZones((L2Character) getActiveObject()); // at world region change
			}
			else
			{
				getWorldRegion().removeFromZones((L2Character) getActiveObject()); // at world region change
			}
		}

		super.setWorldRegion(value);
	}
}
