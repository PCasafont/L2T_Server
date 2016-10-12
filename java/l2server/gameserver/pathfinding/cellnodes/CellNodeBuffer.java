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

	private final ReentrantLock _lock = new ReentrantLock();
	private final int _mapSize;
	private final CellNode[][] _buffer;

	private int _baseX = 0;
	private int _baseY = 0;

	private int _targetX = 0;
	private int _targetY = 0;
	private short _targetZ = 0;

	private long _timeStamp = 0;
	private long _lastElapsedTime = 0;

	private CellNode _current = null;

	public CellNodeBuffer(int size)
	{
		_mapSize = size;
		_buffer = new CellNode[_mapSize][_mapSize];
	}

	public final boolean lock()
	{
		return _lock.tryLock();
	}

	public final CellNode findPath(int x, int y, short z, int tx, int ty, short tz)
	{
		_timeStamp = System.currentTimeMillis();
		_baseX = x + (tx - x - _mapSize) / 2; // middle of the line (x,y) - (tx,ty)
		_baseY = y + (ty - y - _mapSize) / 2; // will be in the center of the buffer
		_targetX = tx;
		_targetY = ty;
		_targetZ = tz;
		_current = getNode(x, y, z);
		_current.setCost(getCost(x, y, z, Config.HIGH_WEIGHT));

		for (int count = 0; count < MAX_ITERATIONS; count++)
		{
			if (_current.getLoc().getNodeX() == _targetX && _current.getLoc().getNodeY() == _targetY &&
					Math.abs(_current.getLoc().getZ() - _targetZ) < 64)
			{
				return _current; // found
			}

			getNeighbors();
			if (_current.getNext() == null)
			{
				return null; // no more ways
			}

			_current = _current.getNext();
		}
		return null;
	}

	public final void free()
	{
		_current = null;

		CellNode node;
		for (int i = 0; i < _mapSize; i++)
		{
			for (int j = 0; j < _mapSize; j++)
			{
				node = _buffer[i][j];
				if (node != null)
				{
					node.free();
				}
			}
		}

		_lock.unlock();
		_lastElapsedTime = System.currentTimeMillis() - _timeStamp;
	}

	public final long getElapsedTime()
	{
		return _lastElapsedTime;
	}

	public final ArrayList<CellNode> debugPath()
	{
		ArrayList<CellNode> result = new ArrayList<>();

		for (CellNode n = _current; n.getParent() != null; n = (CellNode) n.getParent())
		{
			result.add(n);
			n.setCost(-n.getCost());
		}

		for (int i = 0; i < _mapSize; i++)
		{
			for (int j = 0; j < _mapSize; j++)
			{
				CellNode n = _buffer[i][j];
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
		final short NSWE = ((NodeLoc) _current.getLoc()).getNSWE();
		if (NSWE == NSWE_NONE)
		{
			return;
		}

		final int x = _current.getLoc().getNodeX();
		final int y = _current.getLoc().getNodeY();
		final short z = _current.getLoc().getZ();

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
		final int aX = x - _baseX;
		if (aX < 0 || aX >= _mapSize)
		{
			return null;
		}

		final int aY = y - _baseY;
		if (aY < 0 || aY >= _mapSize)
		{
			return null;
		}

		CellNode result = _buffer[aX][aY];
		if (result == null)
		{
			result = new CellNode(new NodeLoc(x, y, z));
			_buffer[aX][aY] = result;
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

		final int stepZ = Math.abs(geoZ - _current.getLoc().getZ());
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

		newNode.setParent(_current);
		newNode.setCost(getCost(x, y, geoZ, weight));

		CellNode node = _current;
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
		final int dX = x - _targetX;
		final int dY = y - _targetY;
		final int dZ = z - _targetZ;
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
