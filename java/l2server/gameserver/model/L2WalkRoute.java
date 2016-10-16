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

package l2server.gameserver.model;

import java.util.List;

/**
 * @author GKR
 */
public class L2WalkRoute
{
	private final int id;
	private final List<L2NpcWalkerNode> nodeList; // List of nodes
	private final boolean repeatWalk; // Does repeat walk, after arriving into last point in list, or not
	private boolean stopAfterCycle; // Make only one cycle or endlessly
	private final byte repeatType;
	// Repeat style: 0 - go back, 1 - go to first point (circle style), 2 - teleport to first point (conveyor style), 3 - random walking between points
	private boolean debug;

	public L2WalkRoute(int id, List<L2NpcWalkerNode> route, boolean repeat, boolean once, byte repeatType)
	{

		this.id = id;
		this.nodeList = route;
		this.repeatType = repeatType;
		this.repeatWalk = (this.repeatType >= 0 && this.repeatType <= 2) && repeat;
		this.debug = false;
	}

	public int getId()
	{
		return this.id;
	}

	public List<L2NpcWalkerNode> getNodeList()
	{
		return this.nodeList;
	}

	public L2NpcWalkerNode getLastNode()
	{
		return this.nodeList.get(this.nodeList.size() - 1);
	}

	public boolean repeatWalk()
	{
		return this.repeatWalk;
	}

	public boolean doOnce()
	{
		return this.stopAfterCycle;
	}

	public byte getRepeatType()
	{
		return this.repeatType;
	}

	public int getNodesCount()
	{
		return this.nodeList.size();
	}

	public void setDebug(boolean val)
	{
		this.debug = val;
	}

	public boolean debug()
	{
		return this.debug;
	}
}
