/*
 * $Header: Item.java, 2/08/2005 00:49:12 luisantonioa Exp $
 *
 * $Author: luisantonioa $ $Date: 2/08/2005 00:49:12 $ $Revision: 1 $ $Log:
 * Item.java,v $ Revision 1 2/08/2005 00:49:12 luisantonioa Added copyright
 * notice
 *
 *
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

package l2server.gameserver

import l2server.gameserver.templates.StatsSet
import l2server.gameserver.templates.item.ItemTemplate

class Item {
	var id: Int = 0

	var type: String? = null

	var name: String? = null

	var set: StatsSet? = null

	var currentLevel: Int = 0

	var item: ItemTemplate? = null
}
