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

package l2server.gameserver.pathfinding;

import l2server.Config;
import l2server.gameserver.model.L2World;
import l2server.gameserver.pathfinding.cellnodes.CellPathFinding;
import l2server.gameserver.pathfinding.geonodes.GeoPathFinding;

import java.util.List;

/**
 * @author -Nemesiss-
 */
public abstract class PathFinding
{
	public static PathFinding getInstance()
	{
		if (!Config.GEODATA_CELLFINDING)
		{
			//Higher Memory Usage, Smaller Cpu Usage
			return GeoPathFinding.getInstance();
		}
		else
		// Cell pathfinding, calculated directly from geodata files
		{
			return CellPathFinding.getInstance();
		}
	}

	public abstract boolean pathNodesExist(short regionoffset);

	public abstract List<AbstractNodeLoc> findPath(int x, int y, int z, int tx, int ty, int tz, int instanceId, boolean playable);

	/*
	public List<AbstractNodeLoc> search(AbstractNode start, AbstractNode end, int instanceId)
	{
		// The simplest grid-based pathfinding.
		// Drawback is not having higher cost for diagonal movement (means funny routes)
		// Could be optimized e.g. not to calculate backwards as far as forwards.

		// List of Visited Nodes
		LinkedList<AbstractNode> visited = new LinkedList<AbstractNode>();

		// List of Nodes to Visit
		LinkedList<AbstractNode> to_visit = new LinkedList<AbstractNode>();
		to_visit.add(start);

		int i = 0;
		while (i < 800)
		{
			AbstractNode node;
			try
			{
				node = to_visit.removeFirst();
			}
			catch (Exception e)
			{
				// No Path found
				return null;
			}
			if (node.equals(end)) //path found!
				return constructPath(node, instanceId);
			else
			{
				i++;
				visited.add(node);
				node.attachNeighbors();
				Node[] neighbors = node.getNeighbors();
				if (neighbors == null)
					continue;
				for (XmlNode n : neighbors)
				{
					if (!visited.contains(n) && !to_visit.contains(n))
					{
						n.setParent(node);
						to_visit.add(n);
					}
				}
			}
		}
		//No Path found
		return null;
	}
	 */
    /*
	public List<AbstractNodeLoc> searchAStar(XmlNode start, Node end, int instanceId)
	{
		// Not operational yet?
		int start_x = start.getLoc().getX();
		int start_y = start.getLoc().getY();
		int end_x = end.getLoc().getX();
		int end_y = end.getLoc().getY();
		//List of Visited Nodes
		FastNodeList visited = new FastNodeList(800);//TODO! Add limit to cfg

		// List of Nodes to Visit
		BinaryNodeHeap to_visit = new BinaryNodeHeap(800);
		to_visit.add(start);

		int i = 0;
		while (i < 800)//TODO! Add limit to cfg
		{
			AbstractNode node;
			try
			{
				node = to_visit.removeFirst();
			}
			catch (Exception e)
			{
				// No Path found
				return null;
			}
			if (node.equals(end)) //path found!
				return constructPath(node, instanceId);
			else
			{
				visited.add(node);
				node.attachNeighbors();
				for (XmlNode n : node.getNeighbors())
				{
					if (!visited.contains(n) && !to_visit.contains(n))
					{
						i++;
						n.setParent(node);
						n.setCost(Math.abs(start_x - n.getLoc().getNodeX()) + Math.abs(start_y - n.getLoc().getNodeY())
								+ Math.abs(end_x - n.getLoc().getNodeX()) + Math.abs(end_y - n.getLoc().getNodeY()));
						to_visit.add(n);
					}
				}
			}
		}
		//No Path found
		return null;
	}
	 */

	/**
	 * Convert geodata position to pathnode position
	 *
	 * @param geo_pos
	 * @return pathnode position
	 */
	public short getNodePos(int geo_pos)
	{
		return (short) (geo_pos >> 3); //OK?
	}

	/**
	 * Convert node position to pathnode block position
	 *
	 * @return pathnode block position (0...255)
	 */
	public short getNodeBlock(int node_pos)
	{
		return (short) (node_pos % 256);
	}

	public byte getRegionX(int node_pos)
	{
		return (byte) ((node_pos >> 8) + Config.WORLD_X_MIN);
	}

	public byte getRegionY(int node_pos)
	{
		return (byte) ((node_pos >> 8) + Config.WORLD_Y_MIN);
	}

	public short getRegionOffset(byte rx, byte ry)
	{
		return (short) ((rx << 5) + ry);
	}

	/**
	 * Convert pathnode x to World x position
	 *
	 * @param node_x, rx
	 * @return
	 */
	public int calculateWorldX(short node_x)
	{
		return L2World.MAP_MIN_X + node_x * 128 + 48;
	}

	/**
	 * Convert pathnode y to World y position
	 *
	 * @param node_y
	 * @return
	 */
	public int calculateWorldY(short node_y)
	{
		return L2World.MAP_MIN_Y + node_y * 128 + 48;
	}

	public String[] getStat()
	{
		return null;
	}
}
