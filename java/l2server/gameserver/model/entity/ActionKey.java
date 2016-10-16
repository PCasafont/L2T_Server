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

package l2server.gameserver.model.entity;

/**
 * @author mrTJO
 */
public class ActionKey
{
	private int cat;
	private int cmd;
	private int key;
	private int tgKey1;
	private int tgKey2;
	private int show;

	/**
	 * L2ActionKey Initialization
	 *
	 * @param cat:    Category ID
	 * @param cmd:    Command ID
	 * @param key:    User Defined Primary Key
	 * @param tgKey1: 1st Toogled Key (eg. Alt, Ctrl or Shift)
	 * @param tgKey2: 2nd Toogled Key (eg. Alt, Ctrl or Shift)
	 * @param show:   Show Action in UI
	 */
	public ActionKey(int cat, int cmd, int key, int tgKey1, int tgKey2, int show)
	{
		this.cat = cat;
		this.cmd = cmd;
		this.key = key;
		this.tgKey1 = tgKey1;
		this.tgKey2 = tgKey2;
		this.show = show;
	}

	public int getCategory()
	{
		return this.cat;
	}

	public int getCommandId()
	{
		return this.cmd;
	}

	public int getKeyId()
	{
		return this.key;
	}

	public int getToogleKey1()
	{
		return this.tgKey1;
	}

	public int getToogleKey2()
	{
		return this.tgKey2;
	}

	public int getShowStatus()
	{
		return this.show;
	}

	public String getSqlSaveString(int playerId, int order)
	{
		return "(" + playerId + ", " + this.cat + ", " + order + ", " + this.cmd + "," + this.key + ", " + this.tgKey1 + ", " +
				this.tgKey2 + ", " + this.show + ")";
	}
}
