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

package l2server.gameserver.model.actor.poly;

import l2server.gameserver.model.WorldObject;

public class ObjectPoly {
	// =========================================================
	// Data Field
	private WorldObject activeObject;
	private int polyId;
	private String polyType;

	// =========================================================
	// Constructor
	public ObjectPoly(WorldObject activeObject) {
		this.activeObject = activeObject;
	}

	// =========================================================
	// Method - Public
	public void setPolyInfo(String polyType, String polyId) {
		setPolyId(Integer.parseInt(polyId));
		setPolyType(polyType);
	}

	// =========================================================
	// Method - Private

	// =========================================================
	// Property - Public
	public final WorldObject getActiveObject() {
		return activeObject;
	}

	public final boolean isMorphed() {
		return getPolyType() != null;
	}

	public final int getPolyId() {
		return polyId;
	}

	public final void setPolyId(int value) {
		polyId = value;
	}

	public final String getPolyType() {
		return polyType;
	}

	public final void setPolyType(String value) {
		polyType = value;
	}
}
