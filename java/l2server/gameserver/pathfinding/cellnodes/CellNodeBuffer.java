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

package l2server.gameserver.pathfinding.cellnodes;

import l2server.Config;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author DS
 *         Credits to Diamond
 */
public class CellNodeBuffer
{
	private static final byte EAST = 1;
	private static final byte WEST = 2;
	private static final byte SOUTH = 4;
	private static final byte NORTH = 8;
	private static final byte NSWE_ALL = 15;
	private static final byte NSWE_NONE = 0;

	private static final int MAX_ITERATIONS = 3500;

	private final ReentrantLock lock = new ReentrantLock();
	private final int mapSize;
	private final CellNode[][] buffer;

	private int baseX = 0;
	private int baseY = 0;

	private int targetX = 0;
	private int targetY = 0;
	private short targetZ = 0;

	private long timeStamp = 0;
	private long lastElapsedTime = 0;

	private CellNode current = null;

	public CellNodeBuffer(int size)
	{
		this.mapSize = size;
		this.buffer = new CellNode[this.mapSize][this.mapSize];
	}

	public final boolean lock()
	{
		return this.lock.tryLock();
	}

	public final CellNode findPath(int x, int y, short z, int tx, int ty, short tz)
	{
		this.timeStamp = System.currentTimeMillis();
		this.baseX = x + (tx - x - this.mapSize) / 2; // middle of the line (x,y) - (tx,ty)
		this.baseY = y + (ty - y - this.mapSize) / 2; // will be in the center of the buffer
		this.targetX = tx;
		this.targetY = ty;
		this.targetZ = tz;
		this.current = getNode(x, y, z);
		this.current.setCost(getCost(x, y, z, Config.HIGH_WEIGHT));

		for (int count = 0; count < MAX_ITERATIONS; count++)
		{
			if (this.current.getLoc().getNodeX() == this.targetX && this.current.getLoc().getNodeY() == this.targetY &&
					Math.abs(this.current.getLoc().getZ() - this.targetZ) < 64)
			{
				return this.current; // found
			}

			getNeighbors();
			if (this.current.getNext() == null)
			{
				return null; // no more ways
			}

			this.current = this.current.getNext();
		}
		return null;
	}

	public final void free()
	{
		this.current = null;

		CellNode node;
		for (int i = 0; i < this.mapSize; i++)
		{
			for (int j = 0; j < this.mapSize; j++)
			{
				node = this.buffer[i][j];
				if (node != null)
				{
					node.free();
				}
			}
		}

		this.lock.unlock();
		this.lastElapsedTime = System.currentTimeMillis() - this.timeStamp;
	}

	public final long getElapsedTime()
	{
		return this.lastElapsedTime;
	}

	public final ArrayList<CellNode> debugPath()
	{
		ArrayList<CellNode> result = new ArrayList<>();

		for (CellNode n = this.current; n.getParent() != null; n = (CellNode) n.getParent())
		{
			result.add(n);
			n.setCost(-n.getCost());
		}

		for (int i = 0; i < this.mapSize; i++)
		{
			for (int j = 0; j < this.mapSize; j++)
			{
				CellNode n = this.buffer[i][j];
				if (n == null || !n.isInUse() || n.getCost() <= 0)
				{
					continue;
				}

				result.add(n);
			}
		}

		return result;
	}

	private void getNeighbors()
	{
		final short NSWE = ((NodeLoc) this.current.getLoc()).getNSWE();
		if (NSWE == NSWE_NONE)
		{
			return;
		}

		final int x = this.current.getLoc().getNodeX();
		final int y = this.current.getLoc().getNodeY();
		final short z = this.current.getLoc().getZ();

		CellNode nodeE = null;
		CellNode nodeS = null;
		CellNode nodeW = null;
		CellNode nodeN = null;

		// East
		if ((NSWE & EAST) != 0)
		{
			nodeE = addNode(x + 1, y, z, false);
		}

		// South
		if ((NSWE & SOUTH) != 0)
		{
			nodeS = addNode(x, y + 1, z, false);
		}

		// West
		if ((NSWE & WEST) != 0)
		{
			nodeW = addNode(x - 1, y, z, false);
		}

		// North
		if ((NSWE & NORTH) != 0)
		{
			nodeN = addNode(x, y - 1, z, false);
		}

		if (Config.ADVANCED_DIAGONAL_STRATEGY)
		{
			// SouthEast
			if (nodeE != null && nodeS != null)
			{
				if ((((NodeLoc) nodeE.getLoc()).getNSWE() & SOUTH) != 0 &&
						(((NodeLoc) nodeS.getLoc()).getNSWE() & EAST) != 0)
				{
					addNode(x + 1, y + 1, z, true);
				}
			}

			// SouthWest
			if (nodeS != null && nodeW != null)
			{
				if ((((NodeLoc) nodeW.getLoc()).getNSWE() & SOUTH) != 0 &&
						(((NodeLoc) nodeS.getLoc()).getNSWE() & WEST) != 0)
				{
					addNode(x - 1, y + 1, z, true);
				}
			}

			// NorthEast
			if (nodeN != null && nodeE != null)
			{
				if ((((NodeLoc) nodeE.getLoc()).getNSWE() & NORTH) != 0 &&
						(((NodeLoc) nodeN.getLoc()).getNSWE() & EAST) != 0)
				{
					addNode(x + 1, y - 1, z, true);
				}
			}

			// NorthWest
			if (nodeN != null && nodeW != null)
			{
				if ((((NodeLoc) nodeW.getLoc()).getNSWE() & NORTH) != 0 &&
						(((NodeLoc) nodeN.getLoc()).getNSWE() & WEST) != 0)
				{
					addNode(x - 1, y - 1, z, true);
				}
			}
		}
	}

	private CellNode getNode(int x, int y, short z)
	{
		final int aX = x - this.baseX;
		if (aX < 0 || aX >= this.mapSize)
		{
			return null;
		}

		final int aY = y - this.baseY;
		if (aY < 0 || aY >= this.mapSize)
		{
			return null;
		}

		CellNode result = this.buffer[aX][aY];
		if (result == null)
		{
			result = new CellNode(new NodeLoc(x, y, z));
			this.buffer[aX][aY] = result;
		}
		else if (!result.isInUse())
		{
			result.setInUse();
			// reinit node if needed
			if (result.getLoc() != null)
			{
				((NodeLoc) result.getLoc()).set(x, y, z);
			}
			else
			{
				result.setLoc(new NodeLoc(x, y, z));
			}
		}

		return result;
	}

	private CellNode addNode(int x, int y, short z, boolean diagonal)
	{
		CellNode newNode = getNode(x, y, z);
		if (newNode == null)
		{
			return null;
		}
		if (newNode.getCost() >= 0)
		{
			return newNode;
		}

		final short geoZ = newNode.getLoc().getZ();

		final int stepZ = Math.abs(geoZ - this.current.getLoc().getZ());
		float weight = diagonal ? Config.DIAGONAL_WEIGHT : Config.LOW_WEIGHT;

		if (((NodeLoc) newNode.getLoc()).getNSWE() != NSWE_ALL || stepZ > 16)
		{
			weight = Config.HIGH_WEIGHT;
		}
		else
		{
			if (isHighWeight(x + 1, y, geoZ))
			{
				weight = Config.MEDIUM_WEIGHT;
			}
			else if (isHighWeight(x - 1, y, geoZ))
			{
				weight = Config.MEDIUM_WEIGHT;
			}
			else if (isHighWeight(x, y + 1, geoZ))
			{
				weight = Config.MEDIUM_WEIGHT;
			}
			else if (isHighWeight(x, y - 1, geoZ))
			{
				weight = Config.MEDIUM_WEIGHT;
			}
		}

		newNode.setParent(this.current);
		newNode.setCost(getCost(x, y, geoZ, weight));

		CellNode node = this.current;
		int count = 0;
		while (node.getNext() != null && count < MAX_ITERATIONS * 4)
		{
			count++;
			if (node.getNext().getCost() > newNode.getCost())
			{
				// insert node into a chain
				newNode.setNext(node.getNext());
				break;
			}
			else
			{
				node = node.getNext();
			}
		}
		if (count == MAX_ITERATIONS * 4)
		{
			System.err.println("Pathfinding: too long loop detected, cost:" + newNode.getCost());
		}

		node.setNext(newNode); // add last

		return newNode;
	}

	private boolean isHighWeight(int x, int y, short z)
	{
		final CellNode result = getNode(x, y, z);
		if (result == null)
		{
			return true;
		}

		if (((NodeLoc) result.getLoc()).getNSWE() != NSWE_ALL)
		{
			return true;
		}
		return Math.abs(result.getLoc().getZ() - z) > 16;

	}

	private double getCost(int x, int y, short z, float weight)
	{
		final int dX = x - this.targetX;
		final int dY = y - this.targetY;
		final int dZ = z - this.targetZ;
		// Math.abs(dx) + Math.abs(dy) + Math.abs(dz) / 16
		double result = Math.sqrt(dX * dX + dY * dY + dZ * dZ / 256);
		if (result > weight)
		{
			result += weight;
		}

		if (result > Float.MAX_VALUE)
		{
			result = Float.MAX_VALUE;
		}

		return result;
	}
}
